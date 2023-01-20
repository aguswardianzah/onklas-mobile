package id.onklas.app.pages.sekolah

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.graphics.Rect
import android.view.*
import android.view.MotionEvent.PointerCoords
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.viven.imagezoom.ImageZoomHelper
import id.onklas.app.R
import timber.log.Timber

class MyImageZoom(val activity: Activity, val rootView: View) : ImageZoomHelper(activity) {

    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {

            private val likeInitSize by lazy { activity.resources.getDimensionPixelSize(R.dimen._32sdp) }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                Timber.e("image zoomable - on double click")
                e?.let { motionEvent ->
                    (findZoomableView(motionEvent, rootView) as? ImageView)?.let { imageView ->
                        (imageView.parent as? FrameLayout)?.let { parentView ->
                            if (parentView.childCount == 1) {
                                try {
                                    (parentView.parent as View).findViewById<View>(R.id.btn_like)
                                        .performClick()
                                    val likeView = ImageView(activity).apply {
                                        id = R.id.ic_like
                                        layoutParams =
                                            FrameLayout.LayoutParams(
                                                likeInitSize,
                                                likeInitSize,
                                                Gravity.CENTER
                                            ).apply {
                                                setImageDrawable(
                                                    ResourcesCompat.getDrawable(
                                                        activity.resources,
                                                        R.drawable.ic_heart,
                                                        null
                                                    )
                                                )
                                            }
                                        alpha = 0f
                                    }
                                    parentView.addView(likeView)
                                    likeView.animate()
                                        .alpha(1f)
                                        .scaleX(4f)
                                        .scaleY(4f)
                                        .setDuration(200)
                                        .setListener(object : AnimatorListenerAdapter() {
                                            override fun onAnimationEnd(animation: Animator?) {
                                                likeView.animate()
                                                    .alpha(0f).duration = 500
                                            }
                                        })
                                } catch (e: Exception) {
                                    Timber.e(e)
                                }
                            }
                        }
                    }
                }
                return true
            }
        })
    }

    private fun findZoomableView(event: MotionEvent, view: View?): View? {
        try {
            if (view is ViewGroup) {
                val childCount = view.childCount
                val pointerCoords1 = PointerCoords()
                event.getPointerCoords(0, pointerCoords1)
                for (i in 0 until childCount) {
                    val child = view.getChildAt(i)
                    if (child.getTag(com.viven.imagezoom.R.id.unzoomable) == null) {
                        val visibleRect = Rect()
                        val location = IntArray(2)
                        child.getLocationOnScreen(location)
                        visibleRect.left = location[0]
                        visibleRect.top = location[1]
                        visibleRect.right = visibleRect.left + child.width
                        visibleRect.bottom = visibleRect.top + child.height
                        if (visibleRect.contains(
                                pointerCoords1.x.toInt(),
                                pointerCoords1.y.toInt()
                            )
                        ) {
                            return if (child.getTag(com.viven.imagezoom.R.id.zoomable) != null) {
                                child
                            } else findZoomableView(event, child)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return null
    }

    override fun onDispatchTouchEvent(ev: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(ev) || super.onDispatchTouchEvent(ev)
    }
}