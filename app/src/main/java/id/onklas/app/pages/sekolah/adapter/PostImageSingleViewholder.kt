package id.onklas.app.pages.sekolah.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.viven.imagezoom.ImageZoomHelper
import id.onklas.app.GlideRequests
import id.onklas.app.R
import id.onklas.app.pages.sekolah.PostAdapterCallback
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.utils.StringUtil

class PostImageSingleViewholder(view: View, val rvWidth: Int) : PostTextViewholder(view) {

    val image by lazy { view.findViewById<ImageView>(R.id.image) }
    val imageLayout by lazy { view.findViewById<ViewGroup>(R.id.image_layout) }

    override fun bind(
        item: FeedTimeline,
        callback: PostAdapterCallback<FeedTimeline>,
        isMine: Boolean,
        glide: GlideRequests,
        stringUtil: StringUtil
    ) {
        super.bind(item, callback, isMine, glide, stringUtil)

        glide.load(item.files.first().path)
            .thumbnail(0.1f)
            .centerCrop()
            .override(rvWidth)
//                    .placeholder(loading)
            .placeholder(R.drawable.img_post_placeholder)
            .error(R.drawable.img_not_found)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean = false

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    ImageZoomHelper.setViewZoomable(image)
                    return false
                }
            })
//            )
            .into(image)
    }

    @SuppressLint("SetTextI18n")
    override fun update(
        item: FeedTimeline,
        glide: GlideRequests,
        callback: PostAdapterCallback<FeedTimeline>
    ) {
        super.update(item, glide, callback)

        if (item.feed.isLike && imageLayout.childCount == 1) {
            imageLayout.addView(View(imageLayout.context))
        }

        btnLike.setOnClickListener {
            val isLike = !item.feed.isLike
            if (!isLike && imageLayout.childCount > 1) {
                imageLayout.removeViewAt(1)
            }
            setLike(!item.feed.isLike)
            val countLike = item.feed.count_likes + (if (isLike) 1 else -1)
            info.text =
                (if (countLike > 0) "$countLike Suka - " else "") + "${item.feed.count_comments} Komentar"
            callback.onClickLike(item, isLike)
        }
    }
}