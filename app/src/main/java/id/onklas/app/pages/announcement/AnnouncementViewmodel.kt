package id.onklas.app.pages.announcement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.OnKlasDbUtil
import id.onklas.app.utils.PreferenceClass
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AnnouncementViewmodel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val db: MemoryDB,
    val dbUtil: OnKlasDbUtil,
    private val apiService: ApiService
) : ViewModel() {

    private val pageSize = 20
    private var nextDataAvailable = true

    val dataAvailable by lazy { MutableLiveData<Boolean>() }

    val listAnnouncement by lazy {
        db.announcement().getAll().toLiveData(
            pageSize,
            boundaryCallback = object : PagedList.BoundaryCallback<AnnouncementTable>() {
                override fun onZeroItemsLoaded() {
                    viewModelScope.launch { fetchData() }
                }

                override fun onItemAtEndLoaded(itemAtEnd: AnnouncementTable) {
                    viewModelScope.launch {
                        val count = db.announcement().count()
                        if (nextDataAvailable && count >= pageSize)
                            fetchData(count)
                    }
                }
            })
    }

    private var isRun = false
    private var prevStart = -1
    suspend fun fetchData(start: Int = 0) {
        if (isRun && prevStart == start) {
            return
        }

        isRun = true
        prevStart = start
        try {
            val response = apiService.announcement(pageSize, start)
            nextDataAvailable = dbUtil.processAnnouncementResponse(response)
            if (start == 0)
                dataAvailable.postValue(true)
        } catch (e: Exception) {
            Timber.e(e)
            if (start == 0)
                dataAvailable.postValue(false)
        } finally {
            isRun = false
            prevStart = start
        }
    }

    fun getDetail(id: Int): LiveData<AnnouncementTable> {
        val res = MutableLiveData<AnnouncementTable>()
        viewModelScope.launch {
            res.postValue(db.announcement().getSingle(id))
        }
        return res
    }
}