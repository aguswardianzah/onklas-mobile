package id.onklas.app.pages.jelajah

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.OnKlasDbUtil
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.pages.sekolah.sosmed.HashtagTable
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.utils.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class JelajahViewModel @Inject constructor(
        val pref: PreferenceClass,
        val moshi: Moshi,
        val db: MemoryDB,
        val dbUtil: OnKlasDbUtil,
        val fileUtils: FileUtils,
        val stringUtil: StringUtil,
        val intentUtil: IntentUtil,
        private val apiService: ApiService
) : ViewModel() {

    private val urlApi by lazy { pref.getString("url_api") }
    val pageSize = 20
    val errorString by lazy { MutableLiveData<String>() }
    var rvWidth = 0

    val search by lazy { MutableLiveData("") }

    val loadingPopular by lazy { MutableLiveData(true) }
    val loadingUser by lazy { MutableLiveData(true) }
    val loadingHashtag by lazy { MutableLiveData(true) }

    fun listPopular(search: String = "") = db.feed().exploreFeed("%$search%")
            .toLiveData(pageSize,
                    boundaryCallback = PagedListBoundaryCallback<FeedTimeline>(
                            {
                                viewModelScope.launch {
                                    fetchPopular(0, search)
                                }
                            }, {
                        viewModelScope.launch {
                            val count = db.feed().countExploreFeed("%$search%")
                            if (count >= pageSize && hasMorePost)
                                fetchPopular(count, search)
                        }
                    }
                    )
            )

    var isRequestPost = false
    var hasMorePost = true
    suspend fun fetchPopular(start: Int = 0, search: String = "") {
        if (isRequestPost || !hasMorePost)
            return

        isRequestPost = true
        hasMorePost = try {
            val data = requestPopular(start, search)
            dbUtil.processFeedItem(data, rvWidth) && data.size >= pageSize
        } catch (e: Exception) {
            Timber.e(e)
            if (start == 0) errorString.postValue(e.localizedMessage)
            false
        } finally {
            loadingPopular.postValue(false)
            isRequestPost = false
        }
    }

    suspend fun requestPopular(start: Int = 0, search: String = "") = try {
        apiService.exploreFeed(pageSize, start, search).data
    } catch (e: Exception) {
        Timber.e(e)
        emptyList()
    } finally {
        loadingPopular.postValue(false)
    }

    fun listUser(search: String = "") = db.feed().getUser("%$search%")
            .toLiveData(pageSize,
                    boundaryCallback = PagedListBoundaryCallback<UserTable>(
                            {
                                viewModelScope.launch {
                                    fetchUser(0, search)
                                }
                            }, {
                        viewModelScope.launch {
                            val count = db.feed().countUser("%$search%")
                            if (count >= pageSize && hasMoreUser)
                                fetchUser(count, search)
                        }
                    }
                    )
            )

    var isRequestUser = false
    var hasMoreUser = true
    suspend fun fetchUser(start: Int = 0, search: String = "") {
        if (isRequestUser || !hasMoreUser)
            return

        isRequestUser = true
        try {
            val resp = apiService.exploreUser(pageSize, start, search)

            db.feed().insertUser(resp.data.map { UserTable(it) })
            hasMoreUser = resp.data.size >= pageSize
        } catch (e: Exception) {
            Timber.e(e)
            if (start == 0) errorString.postValue(e.localizedMessage)
            hasMoreUser = false
        } finally {
            loadingUser.postValue(false)
            isRequestUser = false
        }
    }

    fun listHashtag(search: String = "") = db.feed().getHashtag("%$search%")
            .toLiveData(pageSize,
                    boundaryCallback = PagedListBoundaryCallback<HashtagTable>(
                            {
                                viewModelScope.launch {
                                    fetchHashtag(0, search)
                                }
                            }, {
                        viewModelScope.launch {
                            val count = db.feed().countHashtag("%$search%")
                            if (count >= pageSize && hasMoreHashtag)
                                fetchHashtag(count, search)
                        }
                    }
                    )
            )

    var isRequestHashtag = false
    var hasMoreHashtag = true
    suspend fun fetchHashtag(start: Int = 0, search: String = "") {
        if (isRequestHashtag || !hasMoreHashtag)
            return

        isRequestHashtag = true
        try {
            val resp = apiService.exploreHashtag(pageSize, start, search)

            db.feed().insertHashtag(resp.data)
            hasMoreHashtag = resp.data.size >= pageSize
        } catch (e: Exception) {
            Timber.e(e)
            if (start == 0) errorString.postValue(e.localizedMessage)
            hasMoreHashtag = false
        } finally {
            loadingHashtag.postValue(false)
            isRequestHashtag = false
        }
    }
}