package id.onklas.app.pages.sekolah

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.OnKlasDbUtil
import id.onklas.app.pages.login.SekolahItem
import id.onklas.app.pages.login.StudentItem
import id.onklas.app.pages.sekolah.sosmed.FeedResponse
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.pages.sekolah.sosmed.FeedUserCrossRef
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.pages.theory.TeacherItem
import id.onklas.app.socket.SocketClass
import id.onklas.app.utils.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.net.SocketTimeoutException
import javax.inject.Inject

class SosmedViewModel @Inject constructor(
    val moshi: Moshi,
    val pref: PreferenceClass,
    val db: MemoryDB,
    val fileUtils: FileUtils,
    val dbUtil: OnKlasDbUtil,
    val intentUtil: IntentUtil,
    val stringUtil: StringUtil,
    val api: ApiWrapper,
    val apiService: ApiService,
    val socketClass: SocketClass
) : ViewModel() {

    val countUnreadChat = db.chat().countAllUnread()

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
    val teacher: TeacherItem by lazy {
        try {
            moshi.adapter(TeacherItem::class.java).fromJson(pref.getString("teacher"))
                ?: TeacherItem()
        } catch (e: Exception) {
            TeacherItem()
        }
    }
    val user by lazy {
        MutableLiveData<UserTable>(
            try {
                moshi.adapter(UserTable::class.java).fromJson(pref.getString("user"))
                    ?: UserTable(pref.getInt("user_id"))
            } catch (e: Exception) {
                UserTable(pref.getInt("user_id"))
            }
        )
    }

    val isEmailVerified by lazy { MutableLiveData(pref.getBoolean("is_email_verified")) }

    var postRvWidth = 640
    var ebookRvWidth = 640

    val adapter by lazy { moshi.adapter(FeedResponse::class.java) }
    val errorString by lazy { MutableLiveData<String>() }
    private val pageSize = 10

    val loadingPost by lazy { MutableLiveData(true) }
    val errorLoadPost by lazy { MutableLiveData(false) }
    var nextPostAvailable = true
    val listPost by lazy {
        db.feed().getFeed().toLiveData(
            pageSize,
            boundaryCallback = PagedListBoundaryCallback(
                { viewModelScope.launch { fetchTimeline() } },
                {
                    viewModelScope.launch {
                        val count = db.feed().countFeed()
                        if (nextPostAvailable && count >= pageSize)
                            fetchTimeline(db.feed().countFeed())
                    }
                })
        )
    }

    val loadingEbook by lazy { MutableLiveData(true) }
    val errorLoadEbook by lazy { MutableLiveData(false) }
    var nextEbookAvailable = true
    val listEbook by lazy {
        db.feed().getFeed("ebook")
            .toLiveData(
                pageSize,
                boundaryCallback = object : PagedList.BoundaryCallback<FeedTimeline>() {
                    override fun onZeroItemsLoaded() {
                        viewModelScope.launch {
                            fetchTimeline(type = "ebook")
                        }
                    }

                    override fun onItemAtEndLoaded(itemAtEnd: FeedTimeline) {
                        viewModelScope.launch {
                            val count = db.feed().countFeed("ebook")
                            if (nextEbookAvailable && count >= pageSize)
                                fetchTimeline(count, "ebook")
                        }
                    }
                })
    }

    suspend fun refreshTimeline(type: String = "text") {
//        db.feed().deleteFeed(type)
        fetchTimeline(0, type)
    }

    var prevPost = -1
    var prevEbook = -1
    suspend fun fetchTimeline(start: Int = 0, type: String = "text") {
        if ((type == "text" && prevPost == start) || (type == "ebook" && prevEbook == start)) {
            return
        }

        if (type == "text") {
            if (start == 0)
                errorLoadPost.postValue(false)
            prevPost = start
        } else {
            if (start == 0)
                errorLoadEbook.postValue(false)
            prevEbook = start
        }

        try {
            val response = if (type == "ebook")
                apiService.ebooks(pageSize, start)
            else
                apiService.feeds(pageSize, start)

            if (start == 0)
                db.feed().nukeFeed(type)

            val hasMore =
                dbUtil.processFeedItem(
                    response.data,
                    if (type == "text") postRvWidth else ebookRvWidth
                ) && response.data.size >= pageSize
            if (type == "text")
                nextPostAvailable = hasMore
            else
                nextEbookAvailable = hasMore
        } catch (e: Exception) {
            val isNoConnection =
                e is SocketTimeoutException || e.cause is SocketTimeoutException || e.cause?.cause is SocketTimeoutException || e.cause?.cause?.cause is SocketTimeoutException
            if (!isNoConnection)
                errorString.postValue(e.localizedMessage)

            Timber.e(e)

//            if (start == 0)
//                if (type == "text")
//                    errorLoadPost.postValue(true)
//                else
//                    errorLoadEbook.postValue(true)
        } finally {
            if (type == "text")
                loadingPost.postValue(false)
            else
                loadingEbook.postValue(false)
        }
    }

    val jobs by lazy { mutableMapOf<Int, Job>() }

    fun likePost(
        item: FeedTimeline,
        like: Boolean = true
    ) {
        viewModelScope.launch {
            try {
                val feedId = item.feed.feed_id
                jobs[feedId]?.cancel()
                if (like) {
                    db.feed().insertLike(FeedUserCrossRef(feedId, pref.getInt("user_id")))
                    db.feed().likeFeed(feedId)

//                api.likeFeed(item.feed.feed_id)
                    jobs[feedId] = viewModelScope.launch {
                        async {
                            delay(1000)
                            apiService.likeFeed(
                                mapOf(
                                    "user_id" to pref.getInt("user_id"),
                                    "feed_id" to feedId
                                )
                            )
                        }
                    }
                } else {
                    db.feed().deleteLike(FeedUserCrossRef(feedId, pref.getInt("user_id")))
                    db.feed().unlikeFeed(feedId)

//                api.unlikeFeed(item.feed.feed_id)
                    jobs[feedId] = viewModelScope.launch {
                        async {
                            delay(1000)
                            apiService.unlikeFeed(feedId)
                        }
                    }
                }
                jobs[feedId]?.start()


            } catch (e: Exception) {
                errorString.postValue(e.localizedMessage)
                Timber.e(e)
            }
        }
    }

    suspend fun getUser(): UserTable? = try {
        UserTable(
            apiService.getUser(pref.getInt("user_id")).data
        ).also {
            db.feed().insertUser(it)
            user.postValue(it)
        }
    } catch (e: Exception) {
        errorString.postValue(e.localizedMessage)
        Timber.e(e)
        null
    }

    suspend fun deleteFeed(feedId: Int): Boolean =
        try {
            api.deleteFeed(feedId)
            db.feed().deletePost(feedId)
            true
        } catch (e: Exception) {
            errorString.postValue(e.localizedMessage)
            Timber.e(e)
            false
        }

    suspend fun checkUser() = try {
        val nis =
            if ((user.value?.nis_nik ?: "").isEmpty()) user.value?.nisn_nik else user.value?.nis_nik
        val userData = api.checkAccount(nis, school.uuid)
        pref.putBoolean("is_verified", userData.is_verified)
        pref.putBoolean("is_email_verified", userData.is_email_verified)
        isEmailVerified.postValue(userData.is_email_verified)
        pref.putBoolean("klaspayActive", userData.is_klaspay_activated)
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
    }
}