package id.onklas.app.pages.tryout

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.OnKlasDbUtil
import id.onklas.app.pages.akm.ExamType
import id.onklas.app.pages.login.SekolahItem
import id.onklas.app.pages.login.StudentItem
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import id.onklas.app.utils.Utils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.CancellationException
import javax.inject.Inject

class TryOutViewModel @Inject constructor(
    val context: Context,
    val api: ApiService,
    val db: MemoryDB,
    val dbUtil: OnKlasDbUtil,
    val moshi: Moshi,
    val pref: PreferenceClass,
    val utils: Utils
) : ViewModel(){

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
    private var isLoadingTryout = false
    var hasMoreTryout = true
    var hasMoreList = true

    val initLoadingList by lazy { MutableLiveData(true) }
    val moreLoadingList by lazy { MutableLiveData(true) }
    val emptyList by lazy { MutableLiveData(false) }

    val emptyScored by lazy { MutableLiveData(false) }
    val initLoadingScored by lazy { MutableLiveData(true) }
    val moreLoadingScored by lazy { MutableLiveData(true) }

    private var loadUjianJob: Job? = null
    fun listTryout() = db.akm().listUjianTryout(ExamType.TRYOUT)
        .toLiveData(
            pageSize,
            boundaryCallback = PagedListBoundaryCallback(this::loadTryout) {
                viewModelScope.launch {
                    val count = db.akm().countUjianSchool(ExamType.TRYOUT)
                    Timber.e("List tryout count $count")
                    if (count > pageSize && hasMoreTryout)
                        loadTryout(count)
                }
            }
        )

    fun loadTryout(start: Int = 0) {
        Timber.e("load ujian school start: $start -- isLoading: $isLoadingTryout -- hasMore: $hasMoreTryout")
        if (isLoadingTryout || !hasMoreTryout) {
            initLoadingList.postValue(false)
            moreLoadingList.postValue(false)
            isLoadingTryout = false
            return
        }

        loadUjianJob?.cancel()
        loadUjianJob = viewModelScope.launch { fetchTryout(start) }
    }

    private suspend fun fetchTryout(start: Int = 0) {
        try {
            isLoadingTryout = true
            val data = api.listTryOutSchedule(
                pageSize,
                start
            ).data.filterNotNull()
            if (start == 0) {
                hasMoreTryout = true
                initLoadingList.postValue(true)
            } else {
                moreLoadingList.postValue(true)
            }

            dbUtil.processTryoutResponse(data, true, ExamType.TRYOUT)

            val count = db.akm().countList(ExamType.TRYOUT)
            emptyList.postValue(count == 0)
            hasMoreTryout = count >= pageSize
        } catch (e: Exception) {
            Timber.e(e)
            if (e !is CancellationException)
                errorString.postValue(e.message)
        } finally {
            initLoadingList.postValue(false)
            moreLoadingList.postValue(false)
            isLoadingTryout = false
        }
    }


    val listtryoutScore by lazy {
        db.akm().listUjianSchoolScored(ExamType.TRYOUT)
            .toLiveData(
                pageSize,
                boundaryCallback = PagedListBoundaryCallback(
                    {
                        viewModelScope.launch {
                            loadTryoutScored()
                        }
                    },
                    {
                        viewModelScope.launch {
                            val count = db.akm().countUjianSchoolScored(ExamType.TRYOUT)
                            Timber.e("tryout scored count $count")
                            if (count > pageSize && hasMoreTryoutScored)
                                loadTryoutScored(count)
                        }
                    }
                )
            )
    }
    var isLoadingTryoutScored = false
    var hasMoreTryoutScored = true
    suspend fun loadTryoutScored(start: Int = 0) {
        if (isLoadingTryoutScored || !hasMoreTryoutScored){
            initLoadingScored.postValue(false)
            moreLoadingScored.postValue(false)
            isLoadingTryout = false
            return
        }

        try {
            isLoadingTryoutScored = true
            val data = api.listTryOutScored(pageSize, start).data.filterNotNull()
            if (start == 0) {
                hasMoreTryoutScored = true
                initLoadingScored.postValue(true)
            } else {
                moreLoadingScored.postValue(true)
            }

            dbUtil.processTryoutResponse(data, true,ExamType.TRYOUT)

            val count = db.akm().countScoreList(ExamType.TRYOUT)
            emptyScored.postValue(count == 0)
            hasMoreTryoutScored = count >= pageSize
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.message)
        } finally {
            initLoadingScored.postValue(false)
            moreLoadingScored.postValue(false)
            isLoadingTryoutScored = false
        }
    }
    

}