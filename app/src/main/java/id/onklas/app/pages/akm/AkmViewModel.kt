package id.onklas.app.pages.akm

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import androidx.room.withTransaction
import androidx.work.*
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.OnKlasDbUtil
import id.onklas.app.pages.login.SekolahItem
import id.onklas.app.pages.login.StudentItem
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import id.onklas.app.utils.Utils
import id.onklas.app.worker.AkmDownloader
import id.onklas.app.worker.AkmUploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AkmViewModel @Inject constructor(
    val context: Context,
    val api: ApiService,
    val db: MemoryDB,
    val dbUtil: OnKlasDbUtil,
    val moshi: Moshi,
    val pref: PreferenceClass,
    val utils: Utils
) : ViewModel() {

    var isSchoolScope = false

    val errorString by lazy { MutableLiveData<String>() }

    val school: SekolahItem by lazy {
        try {
            moshi.adapter(SekolahItem::class.java).fromJson(pref.getString("school"))
                ?: SekolahItem()
        } catch (e: Exception) {
            SekolahItem()
        }
    }
    val student: StudentItem by lazy {
        try {
            moshi.adapter(StudentItem::class.java).fromJson(pref.getString("student"))
                ?: StudentItem()
        } catch (e: Exception) {
            StudentItem()
        }
    }

    private val pageSize = 20
    val initLoadingList by lazy { MutableLiveData(true) }
    val moreLoadingList by lazy { MutableLiveData(true) }
    val emptyList by lazy { MutableLiveData(false) }

    val initLoadingScored by lazy { MutableLiveData(true) }
    val moreLoadingScored by lazy { MutableLiveData(true) }
    val emptyScored by lazy { MutableLiveData(false) }

    private var loadAkmJob: Job? = null
    val listAkm by lazy {
        db.akm().listAkm()
            .toLiveData(
                pageSize,
                boundaryCallback = PagedListBoundaryCallback(this::loadAkm) {
                    viewModelScope.launch {
                        val count = db.akm().countList()
                        if (count > pageSize && hasMoreList)
                            loadAkm(count)
                    }
                }
            )
    }

    val listScoreAkm by lazy {
        db.akm().listNilaiAkm()
            .toLiveData(
                pageSize,
                boundaryCallback = PagedListBoundaryCallback(
                    {
                        viewModelScope.launch {
                            loadAkmScored()
                        }
                    },
                    {
                        viewModelScope.launch {
                            val count = db.akm().countScoreList()
                            if (count > pageSize && hasMoreScored)
                                loadAkmScored(count)
                        }
                    }
                )
            )
    }

    private var isLoadingList = false
    var hasMoreList = true
    fun loadAkm(start: Int = 0) {
        if (isLoadingList || !hasMoreList) {
            initLoadingList.postValue(false)
            moreLoadingList.postValue(false)
            isLoadingList = false
            return
        }

        loadAkmJob?.cancel()
        loadAkmJob = viewModelScope.launch { fetchAkm(start) }
    }

    private suspend fun fetchAkm(start: Int = 0) {
        try {
            isLoadingList = true
            val data = api.listAkm(pageSize, start).data.filterNotNull()
            if (start == 0) {
//                db.akm().nuke()
                hasMoreList = true
                initLoadingList.postValue(true)
            } else {
                moreLoadingList.postValue(true)
            }

            dbUtil.processAkmResponse(data)

            val count = db.akm().countList()
            emptyList.postValue(count == 0)
            hasMoreList = count >= pageSize
        } catch (e: Exception) {
            Timber.e(e)
            if (e !is CancellationException)
                errorString.postValue(e.message)
        } finally {
            initLoadingList.postValue(false)
            moreLoadingList.postValue(false)
            isLoadingList = false
        }
    }

    var isLoadingScored = false
    var hasMoreScored = true
    suspend fun loadAkmScored(start: Int = 0) {
        if (isLoadingScored || !hasMoreScored)
            return

        try {
            isLoadingScored = true
            val data = api.listScoreAkm(pageSize, start).data.filterNotNull()
            if (start == 0) {
//                db.akm().nuke()
                hasMoreScored = true
                initLoadingScored.postValue(true)
            } else {
                moreLoadingScored.postValue(true)
            }

            dbUtil.processAkmResponse(data)

            val count = db.akm().countScoreList()
            emptyScored.postValue(count == 0)
            hasMoreScored = count >= pageSize
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.message)
        } finally {
            initLoadingScored.postValue(false)
            moreLoadingScored.postValue(false)
            isLoadingScored = false
        }
    }

    // list ujian school -- start //


    private var loadUjianJob: Job? = null
    fun listUjianSchool() = db.akm().listUjianSchool(akmDateFormat.format(calUjian.value?.time),ExamType.SCHOOL)
        .toLiveData(
            pageSize,
            boundaryCallback = PagedListBoundaryCallback(this::loadUjianSchool) {
                viewModelScope.launch {
                    val count = db.akm().countUjianSchool()
                    if (count > pageSize && hasMoreUjianSchool)
                        loadUjianSchool(count)
                }
            }
        )

    val listUjianSchoolScored by lazy {
        db.akm().listUjianSchoolScored()
            .toLiveData(
                pageSize,
                boundaryCallback = PagedListBoundaryCallback(
                    {
                        viewModelScope.launch {
                            loadUjianSchoolScored()
                        }
                    },
                    {
                        viewModelScope.launch {
                            val count = db.akm().countUjianSchoolScored()
                            if (count > pageSize && hasMoreUjianSchoolScored)
                                loadUjianSchoolScored(count)
                        }
                    }
                )
            )
    }

    private var isLoadingUjianSchool = false
    var hasMoreUjianSchool = true
    val dateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", Locale("id")) }
    val akmDateFormat by lazy { SimpleDateFormat("dd MMMM yyyy", Locale("id")) }

    val calUjian by lazy { MutableLiveData<Calendar>(Calendar.getInstance()) }

    fun addCalendarUjian() {
        val cal = (calUjian.value ?: Calendar.getInstance()).apply { add(Calendar.DAY_OF_YEAR, 1) }
        calUjian.postValue(cal)
        hasMoreUjianSchool = true
        isLoadingUjianSchool = false
        initLoadingList.postValue(true)
        viewModelScope.launch { loadUjianSchool() }
    }

    fun substractCalendarUjian() {
        val cal = (calUjian.value ?: Calendar.getInstance()).apply { add(Calendar.DAY_OF_YEAR, -1) }
        calUjian.postValue(cal)
        isLoadingUjianSchool = false
        hasMoreUjianSchool = true
        initLoadingList.postValue(true)
        viewModelScope.launch { loadUjianSchool() }
    }

    fun loadUjianSchool(start: Int = 0) {
        Timber.e("load ujian school start: $start -- isLoading: $isLoadingUjianSchool -- hasMore: $hasMoreUjianSchool")
        if (isLoadingUjianSchool || !hasMoreUjianSchool) {
            initLoadingList.postValue(false)
            moreLoadingList.postValue(false)
            isLoadingUjianSchool = false
            return
        }

        loadUjianJob?.cancel()
        loadUjianJob = viewModelScope.launch { fetchUjianSchool(start) }
    }

    private suspend fun fetchUjianSchool(start: Int = 0) {
        try {
            isLoadingUjianSchool = true
            val data = api.listUjian(
                dateFormat.format(calUjian.value?.time),
                pageSize,
                start
            ).data.filterNotNull()
            if (start == 0) {
                hasMoreUjianSchool = true
                initLoadingList.postValue(true)
            } else {
                moreLoadingList.postValue(true)
            }

            dbUtil.processAkmResponse(data, true)

            val count = db.akm().countList()
            emptyList.postValue(count == 0)
            hasMoreUjianSchool = count >= pageSize
        } catch (e: Exception) {
            Timber.e(e)
            if (e !is CancellationException)
                errorString.postValue(e.message)
        } finally {
            initLoadingList.postValue(false)
            moreLoadingList.postValue(false)
            isLoadingUjianSchool = false
        }
    }

    var isLoadingUjianSchoolScored = false
    var hasMoreUjianSchoolScored = true
    suspend fun loadUjianSchoolScored(start: Int = 0) {
        if (isLoadingUjianSchoolScored || !hasMoreUjianSchoolScored)
            return

        try {
            isLoadingUjianSchoolScored = true
            val data = api.listUjianScored(pageSize, start).data.filterNotNull()
            if (start == 0) {
                hasMoreUjianSchoolScored = true
                initLoadingScored.postValue(true)
            } else {
                moreLoadingScored.postValue(true)
            }

            dbUtil.processAkmResponse(data, true)

            val count = db.akm().countScoreList()
            emptyScored.postValue(count == 0)
            hasMoreUjianSchoolScored = count >= pageSize
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.message)
        } finally {
            initLoadingScored.postValue(false)
            moreLoadingScored.postValue(false)
            isLoadingUjianSchoolScored = false
        }
    }

    // list ujian school -- end

    val loadingDetail by lazy { MutableLiveData(true) }

    suspend fun fetchDetailAkm(akmId: Int, examType: Int) = try {
        loadAkmJob?.cancel()
        loadUjianJob?.cancel()
        val resp = if (isSchoolScope) api.detailUjianSekolah(akmId) else api.detailAkm(akmId)
        dbUtil.processAkmResponse(listOf(resp.data), isSchoolScope, examType)
    } catch (e: Exception) {
        Timber.e(e)
    } finally {
        loadingDetail.postValue(false)
    }

    fun detailAkm(akmId: Int) = db.akm().get(akmId)

    fun downloadSoal(akmId: Int): UUID {
        val workRequest = OneTimeWorkRequestBuilder<AkmDownloader>()
            .setInputData(
                workDataOf("id" to akmId)
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag("akm_downloader_$akmId")
            .build()
        val workId = workRequest.id

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "akm_downloader_$akmId",
                ExistingWorkPolicy.KEEP,
                workRequest
            )

        return workId
    }

    val ujianStarted by lazy { MutableLiveData<Boolean>() }
    fun startUjian(akmId: Int) {
        viewModelScope.launch {
            try {
                val akm = db.akm().getSingle(akmId) ?: return@launch
                val timeLeft = akm.schedule.date_end.time - System.currentTimeMillis()

                val workRequest = OneTimeWorkRequestBuilder<AkmUploader>()
                    .setInputData(
                        workDataOf("id" to akmId)
                    )
                    .setInitialDelay(timeLeft, TimeUnit.MILLISECONDS)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS
                    )
                    .addTag("akm_uploader_$akmId")
                    .build()

                WorkManager.getInstance(context).cancelUniqueWork("akm_uploader_$akmId")
                WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                        "akm_uploader_$akmId",
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )

                val workId = workRequest.id
                db.akm().insert(akm.schedule.apply {
                    most_significant_bits = workId.mostSignificantBits
                    least_significant_bits = workId.leastSignificantBits
                })

                ujianStarted.postValue(true)
            } catch (e: Exception) {
                Timber.e(e)
                ujianStarted.postValue(false)
                errorString.postValue("Terjadi kesalahan memulai ujian. ${e.message}. Silahkan ulangi beberapa saat lagi")
            }
        }
    }

    fun instQuestionAnswer(instId: Int) =
        db.akm().instQuestion(instId)
            .flatMapConcat { concatAnswer(it) }

    private fun concatAnswer(item: InstuctionQuestion) = flow {
        emit(InstuctionQuestionAnswer(item.instruction, item.questions.map {
            QuestionAnswers(it, db.akm().getAnswers(it.id))
        }))
    }

    val uploadStatus by lazy { MutableLiveData(false) }
    fun uploadAnswer(akmId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    if (utils.isInternetAvailable()) {
//                val akm = db.akm().getSingle(akmId) ?: run {
//                    uploadStatus.postValue(true)
//                    errorString.postValue("Terjadi kesalahan dalam proses upload jawaban, silahkan ulangi beberapa saat lagi")
//                    return@launch
//                }

                        val list = mutableListOf<Any>()
                        val akmDao = db.akm()
                        db.withTransaction {
                            akmDao.updateStatusAkm(akmId, AkmStatus.AKM_STATUS_FINISHED)
                            akmDao.getExamInstructionAsync(akmId).forEach { examInst ->
                                examInst.instructions.forEach { inst ->
                                    akmDao.getInstQuestion(inst.id).questions.forEach { question ->
                                        val data = mutableMapOf(
                                            "instruction_id" to inst.id,
                                            "question_id" to question.id,
                                            "answerType" to question.type_label,
                                            "answer" to when (question.type_label) {
                                                "MULTIPLE CHOICE" -> akmDao.getSelectedAnswers(
                                                    question.id
                                                )
                                                    .firstOrNull()?.id
                                                "STATEMENT" -> akmDao.getAnswers(question.id).map {
                                                    mapOf(
                                                        "id" to it.id,
                                                        "isTrue" to if (it.is_true) 1 else 0,
                                                        "answered" to if (it.selected) 1 else 0
                                                    )
                                                }
                                                "PAIR" -> akmDao.getAnswers(question.id).map {
                                                    mapOf(
                                                        "id" to it.id,
                                                        "answered" to it.selected_id
                                                    )
                                                }
                                                else -> question.answer_essay
                                            }
                                        )
                                        list.add(data)
                                    }
                                }
                            }

                            val schedule = akmDao.getSingle(akmId)

                            if (schedule?.schedule?.exam_type == ExamType.TRYOUT)
                                api.uploadJawabanTryOut(akmId, akmDao.getStudentExamId(akmId), list)
                            else if (isSchoolScope)
                                api.uploadJawabanUjianSchool(
                                    akmId,
                                    akmDao.getStudentExamId(akmId),
                                    list
                                )
                            else
                                api.uploadJawabanAkm(akmId, akmDao.getStudentExamId(akmId), list)

                            akmDao.updateStatusAkm(akmId, AkmStatus.AKM_STATUS_UPLOADED)
                            WorkManager.getInstance(context).cancelUniqueWork("akm_uploader_$akmId")
                        }
                    } else {
                        val workRequest = OneTimeWorkRequestBuilder<AkmUploader>()
                            .setInputData(
                                workDataOf("id" to akmId)
                            )
                            .setConstraints(
                                Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build()
                            )
                            .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS
                            )
                            .addTag("akm_uploader_$akmId")
                            .build()

                        WorkManager.getInstance(context)
                            .enqueueUniqueWork(
                                "akm_uploader_$akmId",
                                ExistingWorkPolicy.REPLACE,
                                workRequest
                            )
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                    errorString.postValue(e.message)
                } finally {
                    uploadStatus.postValue(true)
                }
            }
        }
    }
}