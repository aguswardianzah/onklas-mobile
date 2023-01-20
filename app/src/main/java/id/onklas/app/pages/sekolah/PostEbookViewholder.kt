package id.onklas.app.pages.sekolah

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import id.onklas.app.GlideRequests
import id.onklas.app.R
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.utils.StringUtil

class PostEbookViewholder(view: View) :
    PostViewholder(view) {

    private val ebookLayout: ViewGroup by lazy { view.findViewById<ViewGroup>(R.id.post_content) }
    private val ebookCover: ImageView by lazy { ebookLayout.findViewById<ImageView>(R.id.cover) }
    private val ebookInfo: TextView by lazy { ebookLayout.findViewById<TextView>(R.id.info) }
    private val ebookSize: TextView by lazy { ebookLayout.findViewById<TextView>(R.id.size) }
    private val ebookDownload: TextView by lazy { ebookLayout.findViewById<TextView>(R.id.lihat) }

    @SuppressLint("SetTextI18n")
    override fun bind(
        item: FeedTimeline, callback: PostAdapterCallback<FeedTimeline>,
        isMine: Boolean,
        glide: GlideRequests,
        stringUtil: StringUtil
    ) {
        super.bind(item, callback, isMine, glide, stringUtil)

        glide.load(item.feed.feed_thumbnail_image)
            .thumbnail(0.1f)
            .into(ebookCover)

        ebookInfo.text = "by ${item.feed.feed_author}"
        item.files.firstOrNull()?.size?.let {
            ebookSize.visibility = View.VISIBLE
            ebookSize.text = "Ukuran $it"
        } ?: run {
            ebookSize.visibility = View.GONE
        }

        ebookDownload.setOnClickListener { callback.onClickDownloadEbook(item) }
    }
}