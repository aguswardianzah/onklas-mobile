package id.onklas.app.pages.sekolah.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import id.onklas.app.GlideRequests
import id.onklas.app.R
import id.onklas.app.pages.sekolah.*
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.utils.PagingAdapter
import id.onklas.app.utils.StringUtil

const val POST_TYPE_TEXT = 1
const val POST_TYPE_MEDIA = 2
const val POST_TYPE_MEDIA_SINGLE = 3
const val POST_TYPE_EBOOK = 4

class PostAdapter2(
    val callback: PostAdapterCallback<FeedTimeline>,
    val glide: GlideRequests,
    val stringUtil: StringUtil,
    val rvWidth: Int,
    private val myUserId: Int = -1
) : PagingAdapter<FeedTimeline, PostTextViewholder>(FeedTimelineDiffUtil()) {

    override fun getItemViewType(position: Int): Int = getItem(position)?.let {
        if (it.feed.feed_type == "text" && it.files.isNotEmpty())
            if (it.files.size > 1) POST_TYPE_MEDIA else POST_TYPE_MEDIA_SINGLE
        else if (it.feed.feed_type == "ebook")
            POST_TYPE_EBOOK
        else
            POST_TYPE_TEXT
    } ?: super.getItemViewType(position)

    override fun createItemViewholder(parent: ViewGroup, viewType: Int): PostTextViewholder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            POST_TYPE_MEDIA_SINGLE -> PostImageSingleViewholder(
                inflater.inflate(R.layout.post_image_single_item, parent, false), rvWidth
            )
            POST_TYPE_EBOOK -> PostEbookViewholder2(
                inflater.inflate(R.layout.post_ebook_item, parent, false)
            )
            else -> PostTextViewholder(
                inflater.inflate(R.layout.post_item2, parent, false)
            )
        }
    }

    override fun bindItemViewholder(holder: PostTextViewholder, position: Int) {
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
        holder: PostTextViewholder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        getItem(position)?.let { holder.update(it, glide, callback) }
    }
}