package id.onklas.app.pages.listlike

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import com.squareup.moshi.Moshi
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.sekolah.sosmed.FeedListLike
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ListLikeViewModel @Inject constructor(
    val moshi: Moshi,
    val pref: PreferenceClass,
    val db: MemoryDB,
    val api: ApiWrapper
) : ViewModel() {

    var pageSize = 20

    fun getListLike(feedId: Int) =
        db.feed().getFeedListLike(feedId).toLiveData(
            pageSize = pageSize,
            boundaryCallback = PagedListBoundaryCallback<FeedListLike>(
                {
                    viewModelScope.launch {
                        fetchLike(feedId)
                    }
                },
                {
                    viewModelScope.launch {
                        if (hasMore) {
                            val count = db.feed().countFeedListLike(feedId)
                            if (count >= pageSize)
                                fetchLike(feedId, count)
                        }
                    }
                })
        )

    var lastStart = -1
    var hasMore = true
    suspend fun fetchLike(feedId: Int, start: Int = 0) {
        if (lastStart == start)
            return

        lastStart = start
        hasMore = try {
            api.getFeedLike(feedId, pageSize, 0).data.size >= pageSize
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }
}