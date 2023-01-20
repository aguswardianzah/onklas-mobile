package id.onklas.app.pages.magang

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.presensi.ScheduleTable
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import id.onklas.app.utils.Utils
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MagangViewModel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val db: MemoryDB,
    val apiService: ApiService,
    val apiWrapper: ApiWrapper,
    val utils: Utils
) : ViewModel() {

    val stringError by lazy { MutableLiveData<String>() }
    val loadingSchedule by lazy { MutableLiveData(true) }
    val loadingReport by lazy { MutableLiveData(true) }

    val currSchedule by lazy { MutableLiveData<MagangScheduleCompany>() }
    suspend fun getJadwal(scheduleId: Int) {
        currSchedule.postValue(db.magang().getSchedule(scheduleId))
    }

    private val pageSize = 10

    private val userTableAdapter by lazy { moshi.adapter(UserTable::class.java) }
    val userTable: UserTable by lazy {
        try {
            userTableAdapter.fromJson(pref.getString("user"))
                ?: UserTable(pref.getInt("user_id"))
        } catch (e: Exception) {
            UserTable(pref.getInt("user_id"))
        }
    }

    val listJadwal by lazy {
        db.magang().schedulePagingSource().toLiveData(
            pageSize,
//            boundaryCallback = PagedListBoundaryCallback(
//                { viewModelScope.launch { fetchSchedule() } },
//                { viewModelScope.launch { fetchSchedule() } }
//            )
        )
    }

    private val dateFormatSrc by lazy { SimpleDateFormat("yyyy-MM-dd", Locale("id")) }
    private val dateFormatDst by lazy { SimpleDateFormat("dd MMM yyyy", Locale("id")) }

    private var isRequestingSchedule = false
    suspend fun fetchSchedule() {
        if (isRequestingSchedule)
            return

        isRequestingSchedule = true
        try {
            loadingSchedule.postValue(true)
            val response = apiService.magangSchedule()

            db.magang().deleteSchedule()

            if (response.data.isNotEmpty()) {
                response.data.map {
                    val schedule = MagangSchedule(
                        it.id,
                        it.company?.id,
                        it.schedule?.date.orEmpty(),
                        try {
                            dateFormatDst.format(dateFormatSrc.parse(it.schedule?.date.orEmpty()))
                        } catch (e: Exception) {
                            ""
                        },
                        it.schedule?.start_time.orEmpty(),
                        it.schedule?.end_time.orEmpty(),
                        it.attendance?.status.orEmpty(),
//                        true,
                        it.attendance?.can_attend ?: false,
                        it.attendance?.can_leave ?: false,
                    )

                    db.magang().insertSchedule(schedule)
                    it.company?.let { it1 -> db.magang().insertCompany(it1) }
                }
            } else {
                stringError.postValue("tidak terdapat jadwal")
            }
        } catch (e: Exception) {
            Timber.e(e)
            stringError.postValue(e.localizedMessage)
        } finally {
            isRequestingSchedule = false
            loadingSchedule.postValue(false)
        }
    }

    val listReport by lazy {
        db.magang().reportPagingSource().toLiveData(
            pageSize,
//            boundaryCallback = PagedListBoundaryCallback(
//                { viewModelScope.launch { fetchSchedule() } },
//                { viewModelScope.launch { fetchSchedule() } }
//            )
        )
    }

    suspend fun fetchReport() {
        try {
            loadingReport.postValue(true)
            val response = apiService.magangReport()

            db.magang().deleteReport()

            if (response.data.isNotEmpty()) {
                db.magang().insertReport(response.data.map {
                    MagangReportEntity(
                        it.id,
                        it.check_in_at,
                        it.check_in_status,
                        it.check_out_at,
                        it.check_out_status,
                        it.daily_report,
                        if (it.location.latitide != 0.0) it.location.latitide.toString() else it.location.latitude.toString(),
                        it.location.longitude.toString(),
                        it.company.name,
                        it.company.address,
                        it.company.latitude,
                        it.company.longitude,
                    )
                })
            }
        } catch (e: Exception) {
            Timber.e(e)
            stringError.postValue(e.localizedMessage)
        } finally {
            loadingReport.postValue(false)
        }
    }

    suspend fun attend(scheduleId: Int, loc: Location): Boolean = try {
        apiService.attendMagang(
            mapOf(
                "id" to scheduleId,
                "lat" to loc.latitude,
                "lng" to loc.longitude,
            )
        )
//        fetchSchedule()
//        fetchReport()
        true
    } catch (e: Exception) {
        Timber.e(e)
        stringError.postValue(e.localizedMessage)
        false
    }

    suspend fun sendReport(scheduleId: Int, report: String): Boolean = try {
        apiService.reportMagang(
            mapOf(
                "id" to scheduleId,
                "daily_report" to report
            )
        )
//        fetchSchedule()
//        fetchReport()
        true
    } catch (e: Exception) {
        Timber.e(e)
        stringError.postValue(e.localizedMessage)
        false
    }
}