package id.onklas.app.pages.akun

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.PostItemGridBinding
import id.onklas.app.pages.comment.CommentPage
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.utils.PagingAdapter

class PostGridAdapter(val activity: Activity) :
    PagingAdapter<FeedTimeline, PostGridAdapter.GridViewholder>(object :
        DiffUtil.ItemCallback<FeedTimeline>() {
        override fun areItemsTheSame(oldTable: FeedTimeline, newTable: FeedTimeline): Boolean =
            oldTable.feed.feed_id == newTable.feed.feed_id

        override fun areContentsTheSame(oldItem: FeedTimeline, newItem: FeedTimeline): Boolean =
            oldItem.feed.timeString == newItem.feed.timeString &&
                    oldItem.feed.count_comments == newItem.feed.count_comments &&
                    oldItem.feed.count_likes == newItem.feed.count_likes
    }) {

    override fun createItemViewholder(parent: ViewGroup, viewType: Int): GridViewholder =
        GridViewholder(parent)

    override fun bindItemViewholder(holder: GridViewholder, position: Int) {
        getItem(position)?.let {
            it.files.firstOrNull()?.path?.let { it1 -> holder.bind(it1, it.feed.feed_id) }
        }
    }

    override fun bindItemViewholder(
        holder: GridViewholder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        getItem(position)?.let {
            it.files.firstOrNull()?.path?.let { it1 -> holder.bind(it1, it.feed.feed_id) }
        }
    }

    inner class GridViewholder(
        parent: ViewGroup,
        val binding: PostItemGridBinding = PostItemGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(path: String, feedId: Int) {
            binding.imagePath = path
            binding.image.setOnClickListener { CommentPage.open(activity, feedId) }
        }
    }
}