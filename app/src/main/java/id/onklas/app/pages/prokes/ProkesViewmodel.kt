package id.onklas.app.pages.prokes

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import androidx.room.withTransaction
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiException
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ProkesViewmodel @Inject constructor(
    val context: Context,
    val moshi: Moshi,
    val pref: PreferenceClass,
    val intentUtil: IntentUtil,
    val db: MemoryDB,
    val apiService: ApiService
) : ViewModel() {
    private val pageSize = 20
    var lastProduct = -1
    var hasNextProduct = true

    var Date: Date
    var dateNow = ""
    var timeNow = ""

    init {
        val c: Calendar = Calendar.getInstance()
        c.add(Calendar.DATE, 0) //  coba   c.add(Calendar.DATE,10)
        Date = c.time

        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateNow = df.format(Date)

        val tf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        timeNow = tf.format(Date)

    }

    val errorString by lazy { MutableLiveData<String>() }
    val LoadingShow by lazy { MutableLiveData(true) }
    val isVaccined by lazy { MutableLiveData<Boolean>(false) }


    val dateTomorow = try {
        val c: Calendar = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, 1) //  besok = plus 1
        val dt = c.time
        val df = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val tf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        Timber.e("date formate ${df.format(dt)}")
        df.format(dt)
    } catch (e: Exception) {
        ""
    }


    fun dateStringFormat(stringDate: String = ""): String {
        return try {
            Timber.e("string date $stringDate")
            var spf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val newDate = spf.parse(stringDate)
            Timber.e("newdate $newDate")
            spf = SimpleDateFormat("dd MMMM yyyy", Locale("id"))
            spf.format(newDate)
        } catch (e: Exception) {
            "-"
        }
    }


    val formEaryDetectionResponse by lazy { MutableLiveData<FormEarlyDetectionProkes>() }
    suspend fun loadFormProkesStudent() = try {
        LoadingShow.postValue(true)
        formEaryDetectionResponse.postValue(apiService.getformProkesStudent().data)
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        errorString.postValue(e.message)
    } finally {
        LoadingShow.postValue(false)
    }

    val choiceResponse by lazy { MutableLiveData<ChoiceResponse>() }
    fun ChoiceResponseMoshi(setting_globals_value: String) {
        choiceResponse.postValue(
            moshi.adapter(ChoiceResponse::class.java).fromJson(setting_globals_value)
        )
        Timber.e(
            "loadFormProkesStudent ${
                moshi.adapter(ChoiceResponse::class.java).fromJson(setting_globals_value)
            }"
        )
    }


    val sendProkseResponse by lazy { MutableLiveData<ResponseCheckReport>() }
    suspend fun sendProkesStudent(wayOfTravel: String, publicTransportationChoice: List<String>) =
        try {
            LoadingShow.postValue(true)

            var surveyor = ""
            if (pref.getBoolean("is_student")) {
                surveyor = "STUDENT"
            } else {
                surveyor = "TEACHER"
            }
            sendProkseResponse.postValue(
                apiService.sendProksesStudent(
                    mutableMapOf(
                        "surveyor" to surveyor,
                        "way_of_travel" to wayOfTravel,
                        "already_vaccinated" to isVaccined.value,
                        "public_transportion_choice" to publicTransportationChoice
                    )
                )
            )
        } catch (e: Exception) {
            LoadingShow.postValue(false)
            Timber.e("sendProkesStudent $e")
            errorString.postValue(e.message)
        } finally {
            LoadingShow.postValue(false)
        }

    val cekStudentReport by lazy { MutableLiveData<ResponseCheckReport>() }
    suspend fun cekStudentReport() = try {
        LoadingShow.postValue(true)
        cekStudentReport.postValue(apiService.cekProkesStudent())
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        Timber.e("cekStudentReport $e")
        errorString.postValue(e.message)
        (e as? ApiException)?.let {
            if (it.responseCode ?: 0 > 0) {
                if (e.responseCode == 400) {
                    cekStudentReport.postValue(
                        ResponseCheckReport("", "", null)
                    )
                }
            }
        }
    } finally {
        LoadingShow.postValue(false)
    }


    val cekVaksinasi by lazy { MutableLiveData<CheckReportItem>() }
    suspend fun loadCekVaksin() = try {
        LoadingShow.postValue(true)
        cekVaksinasi.postValue(apiService.cekVaksinasi().data)
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        errorString.postValue(e.message)
    } finally {
        LoadingShow.postValue(false)
    }


    val saveVaksinasi by lazy { MutableLiveData<ResponseCheckReport>() }
    suspend fun saveVaksinasi(vaccinated: Boolean) = try {
        LoadingShow.postValue(true)
        saveVaksinasi.postValue(
            apiService.saveVaksinasi(
                mutableMapOf(
//                        "date_input" to dateNow,
                    "vaccinated" to vaccinated
                )
            )
        )
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        errorString.postValue(e.message)
    } finally {
        LoadingShow.postValue(false)
    }


    // prokes teacher---------------------------------------------------------------------------------

    suspend fun loadCekVaksinTeacher() = try {
        LoadingShow.postValue(true)
        cekVaksinasi.postValue(apiService.cekVaksinasiTeacher().data)
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        errorString.postValue(e.message)
    } finally {
        LoadingShow.postValue(false)
    }

    suspend fun saveVaksinasiTeacher(vaccinated: Boolean) = try {
        LoadingShow.postValue(true)
        saveVaksinasi.postValue(
            apiService.saveVaksinasiTeacher(
                mutableMapOf(
//                        "date_input" to dateNow,
                    "vaccinated" to vaccinated
                )
            )
        )
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        errorString.postValue(e.message)
    } finally {
        LoadingShow.postValue(false)
    }


    fun ListClassItem() = db.prokes().getClassProkes()
        .toLiveData(
            pageSize, boundaryCallback = PagedListBoundaryCallback(
                { viewModelScope.launch { fetchClass(0) } },
                {
                    viewModelScope.launch {
                        val count = db.prokes().countClass()
                        Timber.e("count product: $count -- hasNext = $hasNextProduct")
                        if (hasNextProduct)
                            fetchClass(count)
                    }
                }
            ))

    suspend fun fetchClass(start: Int = 0) { //merchnat tabs  -> best seller dan all
        if (start == lastProduct) {
            LoadingShow.postValue(false)
            return
        }
        lastProduct = start
        hasNextProduct = try {
            apiService.listClass()
                .run {
                    data.forEach {
                        db.prokes().insertClass(
                            ListClass(
                                it.id,
                                it.name,
                                it.grade,
                                it.major?.id ?: 0,
                                it.major?.name ?: "",
                                it.total_student,
                                it.total_student_screening,
                                it.total_student_remaining
                            )
                        )
                    }
                    data.size >= pageSize
                }
        } catch (e: Exception) {
            Timber.e(e)
            false
        } finally {
            LoadingShow.postValue(false)
        }
    }

//    val ListClassItem by lazy { MutableLiveData<List<ListClassItem>>() }
//    suspend fun loadClass() = try {
//        LoadingShow.postValue(true)
//        ListClassItem.postValue(apiService.listClass().data)
//    } catch (e: Exception) {
//        LoadingShow.postValue(false)
//        errorString.postValue(e.message)
//    } finally {
//        LoadingShow.postValue(false)
//    }


//    val ListStudentItem by lazy { MutableLiveData<List<ListStudentItem>>() }
//    suspend fun loadStudent(ClassId: Int) = try {
//        LoadingShow.postValue(true)
//        ListStudentItem.postValue(apiService.listStudent(ClassId).data)
//    } catch (e: Exception) {
//        LoadingShow.postValue(false)
//        errorString.postValue(e.message)
//    } finally {
//        LoadingShow.postValue(false)
//    }


    fun ListStudentItem(searchKey: String = "", classId: Int = 0, MajorName: String = "") =
        db.prokes().getStudent(searchKey, classId)
            .toLiveData(
                pageSize, boundaryCallback = PagedListBoundaryCallback(
                    { viewModelScope.launch { fetchStudent(0, classId, MajorName) } },
                    {
                        viewModelScope.launch {
                            val count = db.prokes().countStudent(searchKey, classId)
                            Timber.e("count product: $count -- hasNext = $hasNextProduct")
                            if (hasNextProduct)
                                fetchStudent(count, classId, MajorName)
                        }

                    }
                ))

    suspend fun fetchStudent(
        start: Int = 0,
        classId: Int = 0,
        majorName: String
    ) { //merchnat tabs  -> best seller dan all
        if (start == lastProduct) {
            LoadingShow.postValue(false)
            return
        }
        lastProduct = start
        hasNextProduct = try {
            apiService.listStudent(classId)
                .run {
                    data.forEach {
                        db.prokes().insertStudent(
                            id.onklas.app.pages.prokes.ListStudentItem(
                                it.id,
                                classId,
                                it.name,
                                majorName,
                                it.nisn_nik,
                                it.nisn,
                                it.nis,
                                it.user_avatar_image,
                                it.already_screening
                            )
                        )
                    }
                    data.size >= pageSize
                }
        } catch (e: Exception) {
            Timber.e(e)
            false
        } finally {
            LoadingShow.postValue(false)
        }
    }


    suspend fun loadTeacherEarlyDetection() = try {
        LoadingShow.postValue(true)
        formEaryDetectionResponse.postValue(apiService.teacherFormEarlyDetetion().data)
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        errorString.postValue(e.message)
    } finally {
        LoadingShow.postValue(false)
    }

    val ScreeningChoiceResponse by lazy { MutableLiveData<ScreningResponse>() }
    fun ScreeningChoice(setting_globals_value: String) {
        ScreeningChoiceResponse.postValue(
            moshi.adapter(ScreningResponse::class.java).fromJson(setting_globals_value)
        )
        Timber.e(
            "loadFormProkesStudent ${
                moshi.adapter(ScreningResponse::class.java).fromJson(setting_globals_value)
            }"
        )
    }

    private val items: MutableList<Triple<String, String, Boolean>> = mutableListOf()

    val ScreeningMain by lazy { MutableLiveData<List<ListChoice>>() }
    val ScreeningChoiceSub by lazy { MutableLiveData<List<ListChoice>>() }
    val SelectedStudentId by lazy { MutableLiveData<Int>(0) }
    val suhu by lazy { MutableLiveData<String>("") }

    val sendScreeningResponse by lazy { MutableLiveData<SaveScreeningResp>() }
    suspend fun sendScreening(
        historyOfIllnes: String,
        feelIndicator: List<String>,
        temperature: String,
        studentId: Int
    ) = try {
        LoadingShow.postValue(true)
        sendScreeningResponse.postValue(
            apiService.saveScreening(
                studentId,
                mutableMapOf(
//                        "date_input" to dateNow,
//                        "time_input" to timeNow,
                    "history_of_illness" to historyOfIllnes,
                    "temperature" to temperature,
                    "feel_indication" to feelIndicator
                )
            )
        )
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        Timber.e("sendScreening $e")
        errorString.postValue(e.message)
    } finally {
        LoadingShow.postValue(false)
    }


    val ScreeningStudent by lazy { MutableLiveData<ScreeningCheckItem>() }
    suspend fun loadScreenigStudent() = try {
        LoadingShow.postValue(true)
        ScreeningStudent.postValue(apiService.screeningCheck().data)
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        errorString.postValue(e.message)
    } finally {
        LoadingShow.postValue(false)
    }


    val loadHistoryReportStudent by lazy { MutableLiveData<ResponseCheckReport>() }
    suspend fun loadHistoryReportStudent(studentId: Int) = try {
        LoadingShow.postValue(true)
        loadHistoryReportStudent.postValue(apiService.historyReportStudent(studentId))
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        errorString.postValue(e.message)
    } finally {
        LoadingShow.postValue(false)
    }


    fun loadChoice(MainKey: String): Flow<List<ChoiceTable>> {
        var count = 0
        viewModelScope.launch {
            db.withTransaction {
                count = db.prokes().countChoice(MainKey)
                if (count == 0) {
                    fectchChoice(count)
                }
                LoadingShow.postValue(false)
                Timber.e("count $count")

            }
        }
        var orderBy = ""
        if (pref.getBoolean("is_student")) {
            orderBy = "id asc"
        } else {
            orderBy = "text desc"
        }
        return db.prokes().getChoice(MainKey, orderBy)
    }


    fun fectchChoice(count: Int) { //merchnat tabs  -> best seller dan all
        viewModelScope.launch {
            LoadingShow.postValue(true)
            try {
                if (pref.getBoolean("is_student")) {
                    apiService.getformProkesStudent()
                        .run {
                            LoadingShow.postValue(false)
                            data!!.setting_globals_value.way_of_travel!!.forEach {
                                db.prokes().insertChoice(
                                    ChoiceTable(
                                        id = "way_of_travel-${it!!.key}".hashCode(),
                                        mainKey = "way_of_travel",
                                        key = it!!.key,
                                        text = it!!.text,
                                    )
                                )
                            }
                            data!!.setting_globals_value.public_transportion_choice!!.forEach {
                                db.prokes().insertChoice(
                                    ChoiceTable(
                                        id = "public_transportion_choice-${it!!.key}".hashCode(),
                                        mainKey = "public_transportion_choice",
                                        key = it!!.key,
                                        text = it!!.text,
                                    )
                                )
                            }
                        }
                } else {
                    apiService.teacherFormEarlyDetetion()
                        .run {
                            LoadingShow.postValue(false)
                            data!!.setting_globals_value.feel_indication!!.forEach {
                                db.prokes().insertChoice(
                                    ChoiceTable(
                                        id = "feel_indication-${it!!.key}".hashCode(),
                                        mainKey = "feel_indication",
                                        key = it!!.key,
                                        text = it!!.text,
                                    )
                                )
                            }
                            data!!.setting_globals_value.history_of_illness!!.forEach {
                                db.prokes().insertChoice(
                                    ChoiceTable(
                                        id = "history_of_illness-${it!!.key}".hashCode(),
                                        mainKey = "history_of_illness",
                                        key = it!!.key,
                                        text = it!!.text,
                                    )
                                )
                            }
                        }
                }

            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
                LoadingShow.postValue(false)
            } finally {
                LoadingShow.postValue(false)
            }
        }
    }
}