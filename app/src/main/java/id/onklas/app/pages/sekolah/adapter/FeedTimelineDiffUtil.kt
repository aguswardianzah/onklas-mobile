package id.onklas.app.pages.sekolah.adapter

import androidx.recyclerview.widget.DiffUtil
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline

class FeedTimelineDiffUtil: DiffUtil.ItemCallback<FeedTimeline>() {
    override fun areItemsTheSame(oldTable: FeedTimeline, newTable: FeedTimeline): Boolean =
        oldTable.feed.feed_id == newTable.feed.feed_id

    override fun areContentsTheSame(oldItem: FeedTimeline, newItem: FeedTimeline): Boolean =
        oldItem.feed.timeString == newItem.feed.timeString &&
                oldItem.feed.count_comments == newItem.feed.count_comments &&
                oldItem.feed.count_likes == newItem.feed.count_likes

    override fun getChangePayload(oldTable: FeedTimeline, newTable: FeedTimeline): Any =
        !areContentsTheSame(oldTable, newTable)
}