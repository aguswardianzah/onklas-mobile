package id.onklas.app.pages.sekolah.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import id.onklas.app.GlideRequests
import id.onklas.app.R
import id.onklas.app.pages.sekolah.PostAdapterCallback
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.utils.PagingAdapter
import id.onklas.app.utils.StringUtil

class PostEbookAdapter(
    val callback: PostAdapterCallback<FeedTimeline>,
    val glide: GlideRequests,
    val stringUtil: StringUtil,
    private val myUserId: Int = -1
) : PagingAdapter<FeedTimeline, PostEbookViewholder2>(FeedTimelineDiffUtil()) {

    override fun createItemViewholder(parent: ViewGroup, viewType: Int): PostEbookViewholder2 =
        PostEbookViewholder2(
            LayoutInflater.from(parent.context).inflate(R.layout.post_ebook_item, parent, false)
        )

    override fun bindItemViewholder(holder: PostEbookViewholder2, position: Int) {
        getItem(position)?.let {
            holder.bind(
                it, callback,
                it.userTable.id == myUserId,
                glide,
                stringUtil
            )
        }
    }

    override fun bindItemViewholder(
        holder: PostEbookViewholder2,
        position: Int,
        payloads: MutableList<Any>
    ) {
        getItem(position)?.let { holder.update(it, glide, callback) }
    }
}