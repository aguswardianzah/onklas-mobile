package id.onklas.app.pages.sekolah

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.viven.imagezoom.ImageZoomHelper
import id.onklas.app.GlideRequests
import id.onklas.app.R
import id.onklas.app.databinding.MediaItemBinding
import id.onklas.app.pages.sekolah.sosmed.FeedFileTable
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import timber.log.Timber

class MediaViewholder(
    parent: ViewGroup,
    val binding: MediaItemBinding = MediaItemBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
    )
) : RecyclerView.ViewHolder(binding.root) {

    val context: Context = binding.postMediaImage.context
    val loading = CircularProgressDrawable(context).apply {
        colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(context, R.color.colorPrimary),
            PorterDuff.Mode.SRC_IN
        )
        strokeWidth = 5f
        centerRadius = 30f
        start()
    }

    fun bind(
        media: FeedFileTable,
        onClick: (item: FeedTimeline) -> Unit = {},
        item: FeedTimeline? = null,
        glide: GlideRequests
    ) {
//        glide.load(thumbnail(media.path, media.width))
//            .thumbnail(0.1f)
//            .centerCrop()
//            .override(media.width)
//            .placeholder(R.drawable.img_post_placeholder)
//          .placeholder(loading)
//            .error(
        glide.load(media.path.also { Timber.e("load failed, try to load old path: $it") })
            .thumbnail(0.1f)
            .centerCrop()
            .override(media.width)
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
                    ImageZoomHelper.setViewZoomable(binding.postMediaImage)
                    return false
                }
            })
//            )
            .into(binding.postMediaImage)

//        binding.postMediaImage.setOnClickListener { item?.let { it1 -> onClick.invoke(it1) } }
    }

    fun thumbnail(url: Any?, width: Int, height: Int = 0) = url?.let {
        var newUrl = it
        if (newUrl is String && newUrl.contains("assets.onklas.id/", true)) {
            newUrl = newUrl.replace("assets.onklas.id/", "thumbnail.onklas.id/")
            newUrl = newUrl.plus(if (!newUrl.contains('?')) "?" else "&")
            newUrl = newUrl.plus("width=${width}")
            if (height > 0)
                newUrl = newUrl.plus("&height=${height}")
        }
        Timber.e("load thumbnail: $newUrl")
        newUrl
    }
}