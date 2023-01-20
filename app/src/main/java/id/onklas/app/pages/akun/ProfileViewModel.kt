package id.onklas.app.pages.akun

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.OnKlasDbUtil
import id.onklas.app.pages.login.SekolahItem
import id.onklas.app.pages.login.StudentItem
import id.onklas.app.pages.login.UserResponse
import id.onklas.app.pages.sekolah.sosmed.FeedUsernameResponse
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.pages.theory.TeacherItem
import id.onklas.app.utils.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val memoryDB: MemoryDB,
    val dbUtil: OnKlasDbUtil,
    val intentUtil: IntentUtil,
    val stringUtil: StringUtil,
    val apiWrapper: ApiWrapper,
    val apiService: ApiService
) : ViewModel() {

    val school by lazy {
        try {
            moshi.adapter(SekolahItem::class.java).fromJson(pref.getString("school"))
        } catch (e: Exception) {
            SekolahItem()
        }
    }
    val student by lazy {
        try {
            moshi.adapter(StudentItem::class.java).fromJson(pref.getString("student"))
        } catch (e: Exception) {
            StudentItem()
        }
    }
    val teacher by lazy {
        try {
            moshi.adapter(TeacherItem::class.java).fromJson(pref.getString("teacher"))
        } catch (e: Exception) {
            TeacherItem()
        }
    }

    val userTableAdapter by lazy { moshi.adapter(UserTable::class.java) }
    val userTable: UserTable by lazy {
        try {
            userTableAdapter.fromJson(pref.getString("user"))
                ?: UserTable(pref.getInt("user_id"))
        } catch (e: Exception) {
            UserTable(pref.getInt("user_id"))
        }
    }

    fun saveUser() {
        pref.putString("user", userTableAdapter.toJson(userTable))
    }

    private val pageSize = 10

    val nisnNik by lazy { MutableLiveData<String>() }
    val userId by lazy { MutableLiveData<Int>() }
    val username by lazy { MutableLiveData<String>() }

    val myUsername by lazy { MutableLiveData<String>() }
    val myProfilePicture by lazy { MutableLiveData<String>(userTable.user_avatar_image) }

    val userData by lazy { MutableLiveData<UserResponse>() }

    suspend fun getUserData() {
        try {
            userData.postValue(apiWrapper.checkAccount(nisnNik.value, school?.uuid))
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    suspend fun getUser() {
        try {
            userId.value?.let { apiService.getUser(it) }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    suspend fun getUserByUsername() {
        try {
            if (!username.value.isNullOrEmpty()) {
                val data = apiService.getUsername(username.value).data
                nisnNik.postValue(if (data.nisn_nik.isNotEmpty()) data.nisn_nik else data.nis_nik)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private var postHasMore = true
    private var imageHasMore = true
    private var ebookHasMore = true
    fun listPost(userId: Int, type: String = "post") =
        memoryDB.feed().getFeedByUser(userId, if (type == "ebook") type else "text").toLiveData(
            pageSize,
            boundaryCallback = PagedListBoundaryCallback(
                {
                    viewModelScope.launch {
                        when (type) {
                            "post" -> loadListPost(0)
                            "image" -> loadListImage(0)
                            else -> loadListEbook(0)
                        }
                    }
                },
                {
                    viewModelScope.launch {
                        val count = memoryDB.feed()
                            .countFeedByUser(userId, if (type == "ebook") type else "text")
                        if (count >= pageSize)
                            when (type) {
                                "post" -> if (postHasMore)
                                    loadListPost(count)
                                "image" -> if (imageHasMore)
                                    loadListImage(count)
                                else -> if (ebookHasMore)
                                    loadListEbook(count)
                            }
                    }
                }
            )
        )

    var isLoadingPost = false
    var prevPostId = -1
    suspend fun loadListPost(start: Int): Boolean {
        Timber.e("load list post: $start -- isLoadingPost: $isLoadingPost -- prevPostId: $prevPostId")
        if (isLoadingPost || start == prevPostId)
            return false

        isLoadingPost = true
        prevPostId = start
        return try {
            val response = apiService.getUserPost(userData.value?.data?.id, pageSize, start)
            prevPostId = start + response.data.size
            isLoadingPost = false

            if (start == 0 && response.data.isNotEmpty())
                userData.value?.data?.id?.let { memoryDB.feed().nukeFeedByUserId(it) }

            dbUtil.processFeedItem(response.data, 0)
        } catch (e: Exception) {
            Timber.e(e)
            postHasMore = false
            isLoadingPost = false
            false
        }
    }

    var isLoadingImage = false
    var prevImageId = -1
    suspend fun loadListImage(start: Int): Boolean {
        if (isLoadingImage || start == prevImageId)
            return false

        isLoadingImage = true
        prevImageId = start
        return try {
            val response = apiService.getUserImage(userData.value?.data?.id, pageSize, start)
            prevImageId = start + response.data.size
            isLoadingImage = false
            true
        } catch (e: Exception) {
            Timber.e(e)
            imageHasMore = false
            false
        }
    }

    var isLoadingEbook = false
    var prevEbookId = -1
    suspend fun loadListEbook(start: Int): Boolean {
        if (isLoadingEbook || start == prevEbookId)
            return false

        isLoadingEbook = true
        prevEbookId = start
        return try {
            val response = apiService.getUserEbook(userData.value?.data?.id, pageSize, start)
            prevEbookId = start + response.data.size
            isLoadingEbook = false
            dbUtil.processFeedItem(response.data, 0)
            true
        } catch (e: Exception) {
            Timber.e(e)
            ebookHasMore = false
            false
        }
    }

    suspend fun verifyEmail(google_token: String) = try {
        apiService.verifyEmail(google_token)
    } catch (e: Exception) {

    }
}