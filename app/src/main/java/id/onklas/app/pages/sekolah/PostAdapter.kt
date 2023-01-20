package id.onklas.app.pages.sekolah

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideRequests
import id.onklas.app.R
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.utils.PagingAdapter
import id.onklas.app.utils.StringUtil

const val POST_TYPE_TEXT = 1
const val POST_TYPE_MEDIA = 2
const val POST_TYPE_MEDIA_SINGLE = 4
const val POST_TYPE_EBOOK = 3

class PostAdapter(
    val callback: PostAdapterCallback<FeedTimeline>,
    private val myUserId: Int = -1,
    val glide: GlideRequests,
    val stringUtil: StringUtil
) :
    PagingAdapter<FeedTimeline, PostViewholder>(object : DiffUtil.ItemCallback<FeedTimeline>() {
        override fun areItemsTheSame(oldTable: FeedTimeline, newTable: FeedTimeline): Boolean =
            oldTable.feed.feed_id == newTable.feed.feed_id

        override fun areContentsTheSame(oldItem: FeedTimeline, newItem: FeedTimeline): Boolean =
            oldItem.feed.timeString == newItem.feed.timeString &&
                    oldItem.feed.count_comments == newItem.feed.count_comments &&
                    oldItem.feed.count_likes == newItem.feed.count_likes

        override fun getChangePayload(oldTable: FeedTimeline, newTable: FeedTimeline): Any? =
            !areContentsTheSame(oldTable, newTable)
    }) {

    private val viewPool by lazy { RecyclerView.RecycledViewPool() }

    override fun getItemViewType(position: Int): Int = getItem(position)?.let {
        if (it.feed.feed_type == "text" && it.files.isNotEmpty())
            POST_TYPE_MEDIA
        else if (it.feed.feed_type == "ebook")
            POST_TYPE_EBOOK
        else
            POST_TYPE_TEXT
    } ?: super.getItemViewType(position)

    override fun createItemViewholder(parent: ViewGroup, viewType: Int): PostViewholder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            POST_TYPE_MEDIA -> PostMediaViewholder(
                inflater.inflate(R.layout.post_media_item, parent, false), viewPool
            )
            POST_TYPE_EBOOK -> PostEbookViewholder(
                inflater.inflate(R.layout.ebook_item, parent, false)
            )
            else -> PostViewholder(
                inflater.inflate(R.layout.post_item, parent, false)
            )
        }
    }

    override fun bindItemViewholder(holder: PostViewholder, position: Int) {
        getItem(position)?.let {
            holder.bind(
                it,
                callback,
                it.userTable.id == myUserId,
                glide,
                stringUtil
            )
        }
    }

    override fun bindItemViewholder(
        holder: PostViewholder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        getItem(position)?.let { holder.update(it) }
    }
}