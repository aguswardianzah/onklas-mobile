package id.onklas.app.pages.comment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.OnKlasDbUtil
import id.onklas.app.pages.sekolah.sosmed.*
import id.onklas.app.utils.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CommentViewModel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val stringUtil: StringUtil,
    val dateUtil: DateUtil,
    val fileUtils: FileUtils,
    val db: MemoryDB,
    val dbUtil: OnKlasDbUtil,
    val api: ApiWrapper,
    val apiService: ApiService
) : ViewModel() {

    val errorString by lazy { MutableLiveData("") }
    val commentBody by lazy { MutableLiveData("") }
    val post by lazy { MutableLiveData<FeedTimeline>() }
    val feedId by lazy { MutableLiveData<Int>() }
    var pageSize = 20

    fun comments(feedId: Int) =
        db.feed().getFeedComment(feedId)
            .toLiveData(
                pageSize, boundaryCallback = PagedListBoundaryCallback<FeedCommentWithUser>(
                    {
                        viewModelScope.launch { fetchComments() }
                    },
                    {
                        viewModelScope.launch {
                            val count = db.feed().countFeed(feedId)
                            if (count >= pageSize && hasMoreComment)
                                fetchComments(count)
                        }
                    }
                )
            )

    var commentStart = -1
    var hasMoreComment = true
    suspend fun fetchComments(start: Int = 0) {
        if (commentStart == start)
            return

        commentStart = start
        hasMoreComment = try {
            api.getFeedComment(feedId.value!!, pageSize, start).data.size >= pageSize
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    suspend fun getPost(feedId: Int) {
        if (this.feedId.value ?: 0 > 0)
            try {
                fetchPost(feedId)
            } catch (e: Exception) {
                Timber.e(e)
                try {
                    post.postValue(db.feed().getFeed(feedId))
                } catch (e: Exception) {
                    errorString.postValue(e.localizedMessage)
                }
            }
        else {
            errorString.postValue("Post tidak tersedia")
        }
    }

    suspend fun fetchPost(feedId: Int) {
        val resp = apiService.feedDetail(feedId)

        val it = resp.data
        val createdAt = dateUtil.formatDate(it.created_at)
        post.postValue(
            FeedTimeline(
                FeedTable(
                    it.row_id,
                    createdAt,
                    it.feed_type,
                    it.feed_body,
                    it.users.id,
                    it.count_comments,
                    it.count_likes,
                    it.created_at_label,
                    it.is_likes,
                    it.feed_thumbnail_image,
                    it.feed_author
                ),
                listOf(
                    FeedFileTable(
                        it.file.feed_files_id,
                        it.row_id,
                        it.file.feed_files_path,
                        fileUtils.getStringSizeLengthFile(
                            (it.file.feed_files_size ?: "0").toLong()
                        ),
                        it.file.feed_files_type,
                        it.file.feed_files_name,
                        0,
                        0
                    )
                ),
                UserTable(resp.data.users),
                it.likes.map { UserTable(it.user) }
            )
        )
    }

    suspend fun sendCommend(): Boolean =
        try {
            api.commentFeed(feedId.value!!, commentBody.value)
            db.feed().incComment(feedId.value!!)
            true
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.localizedMessage)
            false
        }

    val listUser by lazy { MutableLiveData<List<UserTable>>() }

    suspend fun searchUsername(search: String) {
        try {
            listUser.postValue(db.feed().searchUsername("%$search%"))
            apiService.searchUsername(search).data
            listUser.postValue(db.feed().searchUsername("%$search%"))
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}