package id.onklas.app.pages.ujian

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import androidx.paging.toLiveData
import androidx.work.*
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiException
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.OnKlasDbUtil
import id.onklas.app.db.PersistentDB
import id.onklas.app.pages.login.SekolahItem
import id.onklas.app.pages.login.StudentItem
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import id.onklas.app.utils.Utils
import id.onklas.app.worker.ExamEndWorker
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UjianViewModel @Inject constructor(
    val context: Context,
    val pref: PreferenceClass,
    val moshi: Moshi,
    val memoryDB: MemoryDB,
    val persistDB: PersistentDB,
    val dbUtil: OnKlasDbUtil,
    val apiService: ApiService,
    val apiWrapper: ApiWrapper,
    val utils: Utils
) : ViewModel() {

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

    val pageSize = 20

    val errorString by lazy { MutableLiveData<String>() }
    val dateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", Locale("id")) }

    val loadingList by lazy { MutableLiveData(true) }
    val loadingNilai by lazy { MutableLiveData(true) }

    val calKelas by lazy { MutableLiveData<Calendar>(Calendar.getInstance()) }

    val lastTime by lazy {
        Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }
    }
    var pauseThresholds = 30

    fun addCalendarKelas() {
        val cal = (calKelas.value ?: Calendar.getInstance()).apply { add(Calendar.DAY_OF_YEAR, 1) }
        calKelas.postValue(cal)
        loadingList.postValue(true)
    }

    fun substractCalendarKelas() {
        val cal = (calKelas.value ?: Calendar.getInstance()).apply { add(Calendar.DAY_OF_YEAR, -1) }
        calKelas.postValue(cal)
        loadingList.postValue(true)
    }

    fun listUjianStudent() =
        memoryDB.ujian().listUjian(dateFormat.format(calKelas.value?.time))
            .toLiveData(pageSize, boundaryCallback = PagedListBoundaryCallback<ExamTable>(
                {
                    viewModelScope.launch {
                        loadingList.postValue(true)
                        fetchUjian(0)
                    }
                },
                {
                    viewModelScope.launch {
                        val countUjian =
                            memoryDB.ujian().countUjian(dateFormat.format(calKelas.value?.time))
                        if (hasNextUjian && countUjian > ujianStart)
                            fetchUjian(ujianStart)
                    }
                }
            ))

    var ujianStart = -1
    var hasNextUjian = true
    suspend fun fetchUjian(start: Int = 0) {
        if (ujianStart == start && hasNextUjian)
            return

        ujianStart = start

        try {
            val response =
                apiService.listExam(dateFormat.format(calKelas.value?.time), pageSize, start)

            hasNextUjian = dbUtil.processListUjianStudent(response)
        } catch (e: Exception) {
            Timber.e(e)
            (e as? ApiException)?.let {
                if (it.responseCode ?: 0 > 0)
                    errorString.postValue(e.localizedMessage)
            }
        }
    }

    val listUjianScoredStudent by lazy {
        memoryDB.ujian().listUjianScoredSync()
            .toLiveData(pageSize, boundaryCallback = PagedListBoundaryCallback<ExamTable>(
                {
                    viewModelScope.launch {
                        loadingNilai.postValue(true)
                        fetchScored(0)
                    }
                },
                {
                    viewModelScope.launch {
                        val countUjian =
                            memoryDB.ujian().countUjianScored()
                        if (hasNextScored && countUjian > scoredStart)
                            fetchScored(scoredStart)
                    }
                }
            ))
    }

    val listUjianScoredStudent2 by lazy {
        object : DataSource.Factory<Int, ExamTable>() {
            override fun create(): DataSource<Int, ExamTable> =
                ScoredDataSource().also {
                    scoredDataSource.postValue(it)
                    scoredPagingKey = 0
                }
        }.toLiveData(pageSize)
    }

    val scoredDataSource by lazy { MutableLiveData<ScoredDataSource>() }

    var scoredPagingKey = 0

    inner class ScoredDataSource : ItemKeyedDataSource<Int, ExamTable>() {
        override fun getKey(item: ExamTable): Int = scoredPagingKey

        override fun loadInitial(
            params: LoadInitialParams<Int>,
            callback: LoadInitialCallback<ExamTable>
        ) {
            viewModelScope.launch {
                fetchScored()
                val listApi = memoryDB.ujian()
                    .listUjianScored(params.requestedLoadSize, params.requestedInitialKey ?: 0)
                val listApiIds = listApi.map { it.id }
                val listLocal = persistDB.ujian()
                    .listUjianScored(params.requestedLoadSize, params.requestedInitialKey ?: 0)

                scoredPagingKey = params.requestedLoadSize

//                persistDB.ujian().delete(listLocal.filter { listApiIds.contains(it.id) })

                val result = listLocal.also { Timber.e("local: $it") }
                    .filterNot { listApiIds.contains(it.id) }
                    .toMutableList().also { Timber.e("local minus api: $it") }
                    .apply {
                        addAll(listApi)
                        sortByDescending { it.id }
                    }
                    .also { Timber.e("final result: $it") }

                callback.onResult(result)
            }
        }

        override fun loadAfter(
            params: LoadParams<Int>,
            callback: LoadCallback<ExamTable>
        ) {
            viewModelScope.launch {
                fetchScored(params.key)
                val listApi = memoryDB.ujian().listUjianScored(params.requestedLoadSize, params.key)
                val listApiIds = listApi.map { it.id }
                val listLocal =
                    persistDB.ujian().listUjianScored(params.requestedLoadSize, params.key)

                scoredPagingKey += params.requestedLoadSize

//                persistDB.ujian().delete(listLocal.filter { listApiIds.contains(it.id) })

                val result = listLocal
                    .filterNot { listApiIds.contains(it.id) }
                    .toMutableList()
                    .apply {
                        addAll(listApi)
                        sortByDescending { it.id }
                    }
                    .also { Timber.e("final result: $it") }

                callback.onResult(result)
            }
        }

        override fun loadBefore(
            params: LoadParams<Int>,
            callback: LoadCallback<ExamTable>
        ) {
        }
    }

    var scoredStart = -1
    var hasNextScored = true
    suspend fun fetchScored(start: Int = 0) {
        if (scoredStart == start && hasNextScored)
            return

        scoredStart = start

        try {
            val response = apiService.examScored(pageSize, start)

            hasNextScored = dbUtil.processUjianScoreStudent(response)
        } catch (e: Exception) {
            Timber.e(e)
            (e as? ApiException)?.let {
                if (it.responseCode ?: 0 > 0)
                    errorString.postValue(e.localizedMessage)
            }
        }
        loadingNilai.postValue(false)
    }

    val isDownloadingSoal by lazy { MutableLiveData(true) }
    val successDownloadSoal by lazy { MutableLiveData(false) }
    suspend fun downloadSoal(id: String) {
        isDownloadingSoal.postValue(true)
        try {
            successDownloadSoal.postValue(
                if (persistDB.ujian().getUjianQuestionList(id.toInt()).isEmpty()) {
                    val response = apiWrapper.downloadExam(id)
                    dbUtil.processSoalUjian(response, id.toInt())
                } else true
            )
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.localizedMessage)
            successDownloadSoal.postValue(false)
        } finally {
            isDownloadingSoal.postValue(false)
        }
    }

    suspend fun startExam(id: String, password: String): Boolean = try {
        apiWrapper.startExam(id, password)
        true
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        false
    }

    fun getSoal(id: String, scored: Boolean) =
        if (scored) memoryDB.ujian().getUjianQuestion(id.toInt())
        else persistDB.ujian().getUjianQuestion(id.toInt())

    suspend fun answerExam(id: String, name: String): Boolean = try {
        memoryDB.ujian().endUjian(id.toInt())
        memoryDB.ujian().get(id.toInt())?.let { persistDB.ujian().insert(it) }
        try {
            apiService.endExam(id)
            apiWrapper.answerExam(id)
            persistDB.ujian().deleteTestCompletely(id.toInt())
            true
        } catch (e: Exception) {
            // check if not connected
            if (e is ApiException && e.responseCode == 0) {
                runWorker(id.toInt(), name)
                true
            } else {
                Timber.e(e)
                errorString.postValue(e.localizedMessage)
                false
            }
        }
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        false
    }

    private suspend fun runWorker(id: Int, name: String = "") {
        memoryDB.ujian().get(id)?.let { persistDB.ujian().insert(it) }
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "ujian_$id",
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<ExamEndWorker>()
                    .setInputData(
                        workDataOf(
                            "id" to id,
                            "name" to name
                        )
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
                    .addTag("ujian_$id")
                    .build()
            )
    }

    suspend fun detailUjian(id: String) = try {
        dbUtil.processDetailUjian(apiService.detailExam(id))
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        false
    }

    suspend fun sendUjian() {
        persistDB.ujian().getIds().forEach {
            runWorker(it)
        }
    }
}