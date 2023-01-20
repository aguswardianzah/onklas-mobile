package id.onklas.app.utils

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import id.onklas.app.R
import timber.log.Timber
import kotlin.math.roundToInt

@BindingAdapter("app:textStyle")
fun setTextStyle(view: TextView, type: String) {
    when (type) {
        "bold" -> view.setTypeface(null, Typeface.BOLD)
        "italic" -> view.setTypeface(null, Typeface.ITALIC)
        else -> view.setTypeface(null, Typeface.NORMAL)
    }
}

@BindingAdapter("app:tint")
fun setTint(view: ImageView, tint: Int) {
    view.setColorFilter(tint)
}

@BindingAdapter("android:layout_marginTop")
fun setTopMargin(view: View, topMargin: Float) {
    val layoutParams = view.layoutParams as MarginLayoutParams
    layoutParams.setMargins(
        layoutParams.leftMargin, topMargin.roundToInt(),
        layoutParams.rightMargin, layoutParams.bottomMargin
    )
    view.layoutParams = layoutParams
}

fun thumbnail(url: Any?, width: Int) = url?.let {
    var newUrl = it
    if (newUrl is String && newUrl.contains("assets.onklas.id/", true)) {
        newUrl = newUrl.replace("assets.onklas.id/", "thumbnail.onklas.id/")
        newUrl = newUrl.plus(if (!newUrl.contains('?')) "?" else "&")
        newUrl = newUrl.plus("width=${width}")
    }
    Timber.e("load thumbnail: $newUrl")
    newUrl
}

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: Any?) {
//    Timber.e("load image: $url")
//    view.doOnLayout {
//        Glide.with(view.context).load(thumbnail(url, view.measuredWidth))
    Glide.with(view.context).load(url)
        .thumbnail(0.1f)
//            .error(Glide.with(view.context).load(url).thumbnail(0.1f))
//            .placeholder(R.color.ltgray)
        .into(view)
//    }
}

@BindingAdapter("imageUrlWithLoading", "imageError", requireAll = false)
fun loadImageLoading(view: ImageView, url: Any?, imageError: Drawable?) {
    Timber.e("load image loading: $url")
    Glide.with(view.context).load(url)
        .thumbnail(0.1f)
        .placeholder(CircularProgressDrawable(view.context).apply {
            colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(view.context, R.color.colorPrimary),
                PorterDuff.Mode.SRC_IN
            )
            strokeWidth = 5f
            centerRadius = 30f
            start()
        })
        .error(imageError)
        .into(view)
}

@BindingAdapter("imageFitUrl")
fun loadImageFit(view: ImageView, url: Any?) {
    Glide.with(view.context).load(url)
        .thumbnail(0.1f)
        .fitCenter()
        .transform(RoundedCorners(view.context.resources.getDimensionPixelSize(R.dimen._8sdp)))
        .into(view)
}

@BindingAdapter("imageFitUrlRounded")
fun loadImageFitRounded(view: ImageView, url: Any?) {
    Glide.with(view.context).load(url)
        .thumbnail(0.1f)
        .optionalFitCenter()
        .transform(RoundedCorners(view.context.resources.getDimensionPixelSize(R.dimen._8sdp)))
        .into(view)
}


@BindingAdapter("imageCenterUrl")
fun loadImageCenter(view: ImageView, url: Any?) {
    Glide.with(view.context).load(url)
        .thumbnail(0.1f)
        .centerInside()
        .into(view)
}

@BindingAdapter("imageCenterCropUrl")
fun loadImageCenterCrop(view: ImageView, url: Any?) {
    Glide.with(view.context).load(url)
        .thumbnail(0.1f)
        .centerCrop()
        .into(view)
}

@BindingAdapter("imageCircleUrl")
fun loadCircleImage(view: ImageView, url: Any?) {
    Timber.e("load image circle: $url")
//    view.doOnLayout {
    Glide.with(view.context).load(url).override(200)
//    Glide.with(view.context).load(thumbnail(url, view.width))
        .thumbnail(0.1f)
        .circleCrop()
        .override(view.width)
        .error(R.drawable.ic_baseline_account_circle_24)
//            .error(Glide.with(view.context).load(R.color.ltgray).circleCrop())
        .into(view)
//    }
}