package id.onklas.app.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView

tailrec fun Context?.getActivity(): Activity? =
    this as? Activity ?: (this as? ContextWrapper)?.baseContext?.getActivity()

class ImagePostView : AppCompatImageView {

    private var posX: Float = 0f
    private var posY: Float = 0f
    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            save()
            translate(posX, posY)
            drawable.draw(this)
            restore()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                posX += event.x - lastTouchX
                posY += event.y - lastTouchY

                lastTouchX = event.x
                lastTouchY = event.y

                invalidate()
            }
        }
        return true
    }
}