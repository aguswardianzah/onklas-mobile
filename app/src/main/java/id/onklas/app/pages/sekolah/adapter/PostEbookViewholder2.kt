package id.onklas.app.pages.sekolah.adapter

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import id.onklas.app.GlideRequests
import id.onklas.app.R
import id.onklas.app.pages.sekolah.PostAdapterCallback
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.utils.StringUtil

class PostEbookViewholder2(view: View) : PostTextViewholder(view) {

    private val ebookCover: ImageView by lazy { view.findViewById<ImageView>(R.id.cover) }
    private val ebookInfo: TextView by lazy { view.findViewById<TextView>(R.id.ebook_info) }
    private val ebookSize: TextView by lazy { view.findViewById<TextView>(R.id.size) }
    private val ebookDownload: TextView by lazy { view.findViewById<TextView>(R.id.lihat) }

    @SuppressLint("SetTextI18n")
    override fun bind(
        item: FeedTimeline,
        callback: PostAdapterCallback<FeedTimeline>,
        isMine: Boolean,
        glide: GlideRequests,
        stringUtil: StringUtil
    ) {
        super.bind(item, callback, isMine, glide, stringUtil)

        glide.load(item.feed.feed_thumbnail_image)
            .override(
                ebookCover.context.resources.getDimensionPixelSize(R.dimen._32sdp),
                ebookCover.context.resources.getDimensionPixelSize(R.dimen._72sdp)
            )
            .thumbnail(0.1f)
            .into(ebookCover)

        ebookInfo.text = item.feed.feed_author
        item.files.firstOrNull()?.size?.let {
            ebookSize.visibility = View.VISIBLE
            ebookSize.text = "Ukuran $it"
        } ?: run {
            ebookSize.visibility = View.GONE
        }

        ebookDownload.setOnClickListener { callback.onClickDownloadEbook(item) }
    }
}