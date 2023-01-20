package id.onklas.app.pages.presensi

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.PreferenceClass
import id.onklas.app.utils.Utils
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PresensiViewModel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val db: MemoryDB,
    val apiService: ApiService,
    val apiWrapper: ApiWrapper,
    val utils: Utils
) : ViewModel() {

    val stringError by lazy { MutableLiveData<String>() }

    val dateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", Locale("id")) }
    val absensiDateFormat by lazy { SimpleDateFormat("d MMM yyyy", Locale("id")) }
    val hourMinuteFormat by lazy { SimpleDateFormat("HH:mm", Locale("id")) }

    //    val rekapFormat by lazy { SimpleDateFormat("MMMM yyyy", Locale("id")) }
    val fullFormat by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("id")) }

    val calKelas by lazy { MutableLiveData<Calendar>(Calendar.getInstance()) }
    val calAbsensi by lazy { MutableLiveData<Calendar>(Calendar.getInstance()) }

    val loadingKelas by lazy { MutableLiveData(true) }
    val loadingAbsensi by lazy { MutableLiveData(true) }

    val listJadwal by lazy { MutableLiveData<List<ScheduleTable>>(emptyList()) }
    val listAbsensi by lazy { MutableLiveData<List<AbsensiTable>>(emptyList()) }
    val listRekapAbsensi by lazy { MutableLiveData<List<RekapAbsensiTable>>(emptyList()) }

    private val userTableAdapter by lazy { moshi.adapter(UserTable::class.java) }
    val userTable: UserTable by lazy {
        try {
            userTableAdapter.fromJson(pref.getString("user"))
                ?: UserTable(pref.getInt("user_id"))
        } catch (e: Exception) {
            UserTable(pref.getInt("user_id"))
        }
    }

    fun getSchedule() {
        viewModelScope.launch {
            loadingKelas.postValue(true)
            listJadwal.postValue(db.schedule().getJadwal(dateFormat.format(calKelas.value?.time)))
            fetchSchedule()
            loadingKelas.postValue(false)
        }
    }

    private var isRequestingSchedule = false
    private suspend fun fetchSchedule() {
        if (isRequestingSchedule)
            return

        isRequestingSchedule = true
        try {
            val response = apiService.schedule(dateFormat.format(calKelas.value?.time))

            if (response.data.isNotEmpty()) {
                val data = response.data.onEach {
                    if (it.status == "ongoing" && it.attend_at.isNotEmpty() && it.leave_at.isNotEmpty())
                        it.status = "passed"
//                    else if (it.status == "ongoing" && it.late_at.isEmpty() && pref.getBoolean("is_student"))
////                    else if (it.status == "ongoing" && it.late_at.isEmpty())
//                        it.status = "toward"
//                    else if (it.status == "ongoing" && it.late_at.isNotEmpty() && pref.getBoolean("is_student"))
////                    else if (it.status == "ongoing" && it.late_at.isNotEmpty())
//                        it.status = "late"
                }

                db.schedule().deleteScheduleByDate(dateFormat.format(calKelas.value?.time))
                db.schedule().insert(data)
                listJadwal.postValue(data)
            } else {
                stringError.postValue("tidak terdapat jadwal")
            }
        } catch (e: Exception) {
            Timber.e(e)
            stringError.postValue(e.localizedMessage)
        } finally {
            isRequestingSchedule = false
        }
    }

    fun addCalendarKelas() {
        val cal = (calKelas.value ?: Calendar.getInstance()).apply { add(Calendar.DAY_OF_YEAR, 1) }
        calKelas.postValue(cal)
        getSchedule()
    }

    fun substractCalendarKelas() {
        val cal = (calKelas.value ?: Calendar.getInstance()).apply { add(Calendar.DAY_OF_YEAR, -1) }
        calKelas.postValue(cal)
        getSchedule()
    }

    fun getAbsensi() {
        viewModelScope.launch {
            loadingAbsensi.postValue(true)
            calAbsensi.value?.let {
                listAbsensi.postValue(
                    db.schedule().getAbsensi(it.get(Calendar.MONTH), it.get(Calendar.YEAR))
                )
            }
            fetchAbsensi()
            loadingAbsensi.postValue(false)
        }
    }

    suspend fun fetchAbsensi() {
        try {
            val response =
                if (isStudent) apiService.studentMonthlyAttendance(
                    calAbsensi.value?.get(Calendar.YEAR),
                    calAbsensi.value?.get(Calendar.MONTH)?.plus(1)
                )
                else apiService.teacherMonthlyAttendance(
                    calAbsensi.value?.get(Calendar.YEAR),
                    calAbsensi.value?.get(Calendar.MONTH)?.plus(1)
                )
//            apiService.attendance(
//                calAbsensi.value?.get(Calendar.MONTH)?.plus(1),
//                calAbsensi.value?.get(Calendar.YEAR)
//            )

            if (response.data.isNotEmpty()) {
                val data = response.data.onEach {
                    dateFormat.parse(it.date)?.let { date ->
                        val cal = Calendar.getInstance().apply { time = date }
                        it.dateLabel = absensiDateFormat.format(date)
                        it.month = cal.get(Calendar.MONTH)
                        it.year = cal.get(Calendar.YEAR)

                        try {
                            it.attend_at = hourMinuteFormat.format(fullFormat.parse(it.attend_at))
                        } catch (e: Exception) {
                        }

                        try {
                            it.leave_at = hourMinuteFormat.format(fullFormat.parse(it.leave_at))
                        } catch (e: Exception) {
                        }
                    }
                }
                db.schedule().insertAbsensi(data)
                listAbsensi.postValue(data)
            } else
                stringError.postValue("data kehadiran tidak tersedia")
        } catch (e: Exception) {
            Timber.e(e)
            stringError.postValue(e.localizedMessage)
        }
    }

    fun addCalendarAbsensi() {
        val cal = (calAbsensi.value ?: Calendar.getInstance()).apply { add(Calendar.MONTH, 1) }
        calAbsensi.postValue(cal)
        getAbsensi()
    }

    fun substractCalendarAbsensi() {
        val cal = (calAbsensi.value ?: Calendar.getInstance()).apply { add(Calendar.MONTH, -1) }
        calAbsensi.postValue(cal)
        getAbsensi()
    }

    fun getRekapAbsensi() {
        viewModelScope.launch {
            listRekapAbsensi.postValue(
                db.schedule().getRekapAbsensi(Calendar.getInstance().get(Calendar.YEAR))
            )
            fetchRekapAbsensi()
        }
    }

    suspend fun fetchRekapAbsensi() {
        try {
            val response =
                if (isStudent) apiService.studentAnnualAttendance(
                    Calendar.getInstance().get(Calendar.YEAR)
                )
                else apiService.teacherAnnualAttendance(Calendar.getInstance().get(Calendar.YEAR))

            if (response.data.isNotEmpty()) {
                val data = response.data.onEachIndexed { index, it ->
                    it.date = "${it.month} ${it.year}"
                    it.order = index
                }
                db.schedule().insertRekap(data)
                listRekapAbsensi.postValue(data)
            } else
                stringError.postValue("data rekap tidak tersedia")
        } catch (e: Exception) {
            Timber.e(e)
            stringError.postValue(e.localizedMessage)
        }
    }

    suspend fun detailClass(id: Int, date: String): MutableLiveData<ScheduleDetailAttendance> {
        val res = MutableLiveData<ScheduleDetailAttendance>(db.schedule().getDetailSchedule(id))
        try {
            db.withTransaction {
                val response = apiService.attendanceSchedule(id, date)
                db.schedule().insert(response.data)

                val recap = apiService.recapAbsensi(date, id)
                val attendances = recap.data.attendances.map {
                    ScheduleAttendanceTable(
                        0,
                        recap.data.id,
                        it.name,
                        if (it.nisn.isNotEmpty()) it.nisn else it.nis,
                        it.is_late == true,
                        it.start_time.isNotEmpty()
                    )
                }.distinctBy { it.nisn }
                db.schedule().insertAttendance(attendances)

                res.postValue(ScheduleDetailAttendance(response.data, attendances))
            }
        } catch (e: Exception) {
            Timber.e(e)
            stringError.postValue(e.localizedMessage)
        }
        return res
    }

    suspend fun startClass(
        scheduleId: Int,
        pass: String,
        late: Int,
        lat: Double,
        long: Double
    ): Boolean = try {
        apiWrapper.startClass(scheduleId, pass, late, lat, long)
        true
    } catch (e: Exception) {
        Timber.e(e)
        stringError.postValue(e.localizedMessage)
        false
    }

    suspend fun attendClass(scheduleId: Int, lat: Double, long: Double): Boolean = try {
        apiWrapper.attendClass(scheduleId, lat, long)
        true
    } catch (e: Exception) {
        Timber.e(e)
        stringError.postValue(e.localizedMessage)
        false
    }

    suspend fun endClass(scheduleId: Int): Boolean = try {
        apiWrapper.endClass(scheduleId)
        true
    } catch (e: Exception) {
        Timber.e(e)
        stringError.postValue(e.localizedMessage)
        false
    }

    suspend fun leaveClass(scheduleId: Int): Boolean = try {
        apiWrapper.leaveClass(scheduleId)
        true
    } catch (e: Exception) {
        Timber.e(e)
        stringError.postValue(e.localizedMessage)
        false
    }

    val isStudent by lazy { pref.getBoolean("is_student") }

    val allowAbsent by lazy { MutableLiveData(false) }
    val absenType by lazy { MutableLiveData<String>() }
    val buttonLabel by lazy { MutableLiveData<String>() }
    val errorLabel by lazy { MutableLiveData<String>() }
    suspend fun checkAbsent() = try {
        val res =
            if (isStudent) apiService.studentCheckPresensi() else apiService.teacherCheckPresensi()
        allowAbsent.postValue(res.data.allow_attendance)
        absenType.postValue(
            if (res.data.type_attendance.equals("checkin", true)) "Masuk"
            else "Pulang"
        )
        buttonLabel.postValue(res.data.attendanceResponseTextButton)
        errorLabel.postValue(res.data.attendanceResponseText)
    } catch (e: Exception) {
        Timber.e(e)
        stringError.postValue(e.localizedMessage)
    }

    suspend fun doAbsensi(lat: Double, lng: Double) = try {
        absenType.value?.let {
            val data = mapOf("lat" to lat.toString(), "lng" to lng.toString())
            if (isStudent) {
                if (it == "Masuk")
                    apiService.studentCheckIn(data)
                else
                    apiService.studentCheckOut(data)
            } else {
                if (it == "Masuk")
                    apiService.teacherCheckIn(data)
                else
                    apiService.teacherCheckOut(data)
            }
        }
        true
    } catch (e: Exception) {
        Timber.e(e)
        stringError.postValue(e.localizedMessage)
        false
    }

    val currSchedule by lazy { MutableLiveData<ScheduleTable>() }
    suspend fun getJadwal(scheduleId: Int) {
        currSchedule.postValue(db.schedule().getJadwal(scheduleId))
    }
}