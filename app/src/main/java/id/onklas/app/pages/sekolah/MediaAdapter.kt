package id.onklas.app.pages.sekolah

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideRequests
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline

class MediaAdapter(
    var item: FeedTimeline,
    val onClick: (item: FeedTimeline) -> Unit,
    val glide: GlideRequests
) : RecyclerView.Adapter<MediaViewholder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewholder =
        MediaViewholder(parent)

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: MediaViewholder, position: Int) =
        holder.bind(item.files[position], onClick, item, glide)

    override fun onBindViewHolder(
        holder: MediaViewholder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        holder.bind(item.files[position], onClick, item, glide)
    }
}