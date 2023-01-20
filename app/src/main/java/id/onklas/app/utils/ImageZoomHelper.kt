package id.onklas.app.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import android.view.MotionEvent.PointerCoords
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import timber.log.Timber
import java.lang.ref.WeakReference
import kotlin.math.pow
import kotlin.math.sqrt

class ImageZoomHelper {

    private var zoomableView: View? = null
    private var parentOfZoomableView: ViewGroup? = null
    private var zoomableViewLP: ViewGroup.LayoutParams? = null
    private var zoomableViewFrameLP: FrameLayout.LayoutParams? = null
    private var dialog: Dialog? = null
    private var placeholderView: View? = null
    private var viewIndex = 0
    private var darkView: View? = null
    private var originalDistance = 0.0
    private lateinit var twoPointCenter: IntArray
    private lateinit var originalXY: IntArray
    private var pivotX = 0
    private var pivotY = 0
    private var activityWeakReference: WeakReference<Activity?>? = null
    private var isAnimatingDismiss = false

    fun ImageZoomHelper(activity: Activity?) {
        activityWeakReference = WeakReference<Activity?>(activity)
    }

    fun onDispatchTouchEvent(ev: MotionEvent?): Boolean {
        var activity: Activity? = null
        return if (activityWeakReference?.get()?.also { activity = it } == null) {
            false
        } else {
            val currentDistance: Int
            if (ev!!.pointerCount == 2) {
                if (zoomableView != null) {
                    val pointerCoords1 = PointerCoords()
                    ev.getPointerCoords(0, pointerCoords1)
                    val pointerCoords2 = PointerCoords()
                    ev.getPointerCoords(1, pointerCoords2)
                    val newCenter = intArrayOf(
                        ((pointerCoords2.x + pointerCoords1.x) / 2.0f).toInt(),
                        ((pointerCoords2.y + pointerCoords1.y) / 2.0f).toInt()
                    )
                    currentDistance = getDistance(
                        pointerCoords1.x.toDouble(),
                        pointerCoords2.x.toDouble(),
                        pointerCoords1.y.toDouble(),
                        pointerCoords2.y.toDouble()
                    ).toInt()

                    val pctIncrease =
                        (currentDistance.toDouble() - originalDistance) / originalDistance

                    zoomableView!!.pivotX = pivotX.toFloat()
                    zoomableView!!.pivotY = pivotY.toFloat()
                    zoomableView!!.scaleX = (1.0 + pctIncrease).toFloat()
                    zoomableView!!.scaleY = (1.0 + pctIncrease).toFloat()
                    updateZoomableViewMargins(
                        (newCenter[0] - twoPointCenter[0] + originalXY[0]).toFloat(),
                        (newCenter[1] - twoPointCenter[1] + originalXY[1]).toFloat()
                    )

                    darkView!!.alpha = (pctIncrease / 8.0).toFloat()
                    return true
                }


                val view = findZoomableView(ev, activity?.findViewById(android.R.id.content))
                if (view != null) {
                    zoomableView = view
                    originalXY = IntArray(2)
                    view.getLocationInWindow(originalXY)
                    val frameLayout = FrameLayout(view.context)
                    darkView = View(view.context)
                    darkView!!.setBackgroundColor(-16777216)
                    darkView!!.alpha = 0.0f
                    frameLayout.addView(darkView, FrameLayout.LayoutParams(-1, -1))
                    dialog =
                        Dialog(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
                    dialog!!.addContentView(frameLayout, ViewGroup.LayoutParams(-1, -1))
                    dialog!!.show()
                    parentOfZoomableView = zoomableView!!.parent as ViewGroup
                    viewIndex = parentOfZoomableView!!.indexOfChild(zoomableView)
                    zoomableViewLP = zoomableView!!.layoutParams
                    zoomableViewFrameLP = FrameLayout.LayoutParams(view.width, view.height)
                    zoomableViewFrameLP!!.leftMargin = originalXY[0]
                    zoomableViewFrameLP!!.topMargin = originalXY[1]
                    placeholderView = View(activity)
                    zoomableView!!.isDrawingCacheEnabled = true
                    val placeholderDrawable = BitmapDrawable(
                        activity!!.resources, Bitmap.createBitmap(
                            zoomableView!!.drawingCache
                        )
                    )
                    placeholderView!!.background = placeholderDrawable
                    parentOfZoomableView!!.addView(placeholderView, zoomableViewLP)
                    parentOfZoomableView!!.removeView(zoomableView)
                    frameLayout.addView(zoomableView, zoomableViewFrameLP)
                    zoomableView!!.post {
                        if (dialog != null) {
                            placeholderView!!.background = null
                            zoomableView!!.isDrawingCacheEnabled = false
                        }
                    }
                    val pointerCoords1 = PointerCoords()
                    ev.getPointerCoords(0, pointerCoords1)
                    val pointerCoords2 = PointerCoords()
                    ev.getPointerCoords(1, pointerCoords2)
                    originalDistance = getDistance(
                        pointerCoords1.x.toDouble(),
                        pointerCoords2.x.toDouble(),
                        pointerCoords1.y.toDouble(),
                        pointerCoords2.y.toDouble()
                    ).toInt().toDouble()
                    twoPointCenter = intArrayOf(
                        ((pointerCoords2.x + pointerCoords1.x) / 2.0f).toInt(),
                        ((pointerCoords2.y + pointerCoords1.y) / 2.0f).toInt()
                    )
                    pivotX = ev.rawX.toInt() - originalXY[0]
                    pivotY = ev.rawY.toInt() - originalXY[1]
                    return true
                }
            } else if (zoomableView != null && !isAnimatingDismiss) {
                isAnimatingDismiss = true
                val scaleYStart = zoomableView!!.scaleY
                val scaleXStart = zoomableView!!.scaleX
                val leftMarginStart = zoomableViewFrameLP!!.leftMargin
                currentDistance = zoomableViewFrameLP!!.topMargin
                val alphaStart = darkView!!.alpha
                val scaleYEnd = 1.0f
                val scaleXEnd = 1.0f
                val leftMarginEnd = originalXY[0]
                val topMarginEnd = originalXY[1]
                val alphaEnd = 0.0f
                val valueAnimator = ValueAnimator.ofFloat(*floatArrayOf(0.0f, 1.0f))
                valueAnimator!!.duration = activity!!.resources.getInteger(17694720).toLong()
                valueAnimator.addUpdateListener { valueAnimator ->
                    val animatedFraction = valueAnimator!!.animatedFraction
                    if (zoomableView != null) {
                        updateZoomableView(
                            animatedFraction,
                            scaleYStart,
                            scaleXStart,
                            leftMarginStart,
                            currentDistance,
                            1.0f,
                            1.0f,
                            leftMarginEnd,
                            topMarginEnd
                        )
                    }
                    if (darkView != null) {
                        darkView!!.alpha =
                            (0.0f - alphaStart) * animatedFraction + alphaStart
                    }
                }
                valueAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationCancel(animation: Animator?) {
                        super.onAnimationCancel(animation)
                        end()
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        end()
                    }

                    fun end() {
                        if (zoomableView != null) {
                            updateZoomableView(
                                1.0f,
                                scaleYStart,
                                scaleXStart,
                                leftMarginStart,
                                currentDistance,
                                1.0f,
                                1.0f,
                                leftMarginEnd,
                                topMarginEnd
                            )
                        }
                        dismissDialogAndViews()
                        valueAnimator.removeAllListeners()
                        valueAnimator.removeAllUpdateListeners()
                    }
                })
                valueAnimator.start()
                return true
            }
            false
        }
    }

    private fun updateZoomableView(
        animatedFraction: Float,
        scaleYStart: Float,
        scaleXStart: Float,
        leftMarginStart: Int,
        topMarginStart: Int,
        scaleXEnd: Float,
        scaleYEnd: Float,
        leftMarginEnd: Int,
        topMarginEnd: Int
    ) {
        zoomableView!!.scaleX = (scaleXEnd - scaleXStart) * animatedFraction + scaleXStart
        zoomableView!!.scaleY = (scaleYEnd - scaleYStart) * animatedFraction + scaleYStart
        updateZoomableViewMargins(
            (leftMarginEnd - leftMarginStart).toFloat() * animatedFraction + leftMarginStart.toFloat(),
            (topMarginEnd - topMarginStart).toFloat() * animatedFraction + topMarginStart.toFloat()
        )
    }

    fun updateZoomableViewMargins(left: Float, top: Float) {
        if (zoomableView != null && zoomableViewFrameLP != null) {
            zoomableViewFrameLP!!.leftMargin = left.toInt()
            zoomableViewFrameLP!!.topMargin = top.toInt()
            zoomableView!!.layoutParams = zoomableViewFrameLP
        }
    }

    fun loadBitmapFromView(v: View): Bitmap {
        val b = Bitmap.createBitmap(
            v.layoutParams.width,
            v.layoutParams.height,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(b)
        v.layout(v.left, v.top, v.right, v.bottom)
        v.draw(c)
        return b
    }

    private fun dismissDialogAndViews() {
//        zoomableView?.apply {
//            visibility = View.GONE
//            background = BitmapDrawable(
//                resources, Bitmap.createBitmap(loadBitmapFromView(this))
//            )
//
//            (parent as? ViewGroup)?.removeView(this)
//
//        }
        if (zoomableView != null) {
            zoomableView!!.visibility = View.GONE
            zoomableView!!.isDrawingCacheEnabled = true
            val placeholderDrawable = BitmapDrawable(
                zoomableView!!.resources, Bitmap.createBitmap(
                    zoomableView!!.drawingCache
                )
            )
            placeholderView!!.background = placeholderDrawable
            val parent = zoomableView!!.parent as ViewGroup
            parent.removeView(zoomableView)
            parentOfZoomableView!!.addView(zoomableView, viewIndex, zoomableViewLP)
            parentOfZoomableView!!.removeView(placeholderView)
            val finalZoomView = zoomableView
            zoomableView!!.isDrawingCacheEnabled = false
            zoomableView!!.post {
                dismissDialog()
                finalZoomView!!.invalidate()
            }
        } else {
            dismissDialog()
        }
        isAnimatingDismiss = false
    }

    private fun dismissDialog() {
        dialog?.dismiss()
        dialog = null
        darkView = null
        resetOriginalViewAfterZoom()
    }

    private fun resetOriginalViewAfterZoom() {
        zoomableView?.invalidate()
        zoomableView = null
    }

    private fun getDistance(x1: Double, x2: Double, y1: Double, y2: Double): Double {
        return sqrt((x2 - x1).pow(2.0) + (y2 - y1).pow(2.0))
    }

    private fun findZoomableView(event: MotionEvent?, view: View?): View? {
        try {
            if (view is ViewGroup) {
                val childCount = view.childCount
                val pointerCoords1 = PointerCoords()
                event?.getPointerCoords(0, pointerCoords1)
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

    fun setViewZoomable(view: View) {
        view.setTag(com.viven.imagezoom.R.id.zoomable, Any())
    }

    fun setZoom(view: View, enabled: Boolean) {
        view.setTag(com.viven.imagezoom.R.id.unzoomable, if (enabled) null else Any())
    }

    interface OnZoomListener {
        fun onImageZoomStarted(var1: View?)
        fun onImageZoomEnded(var1: View?)
    }
}