package id.onklas.app.utils

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper


class SnapOnScrollListener(
    private val snapHelper: SnapHelper,
    var behavior: Behavior = Behavior.NOTIFY_ON_SCROLL,
    var onSnapPositionChangeListener: (pos: Int) -> Unit
) : RecyclerView.OnScrollListener() {

    enum class Behavior {
        NOTIFY_ON_SCROLL,
        NOTIFY_ON_SCROLL_STATE_IDLE
    }

    private var snapPosition = RecyclerView.NO_POSITION

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        maybeNotifySnapPositionChange(recyclerView)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            maybeNotifySnapPositionChange(recyclerView)
        }
    }

    private fun maybeNotifySnapPositionChange(recyclerView: RecyclerView) {
        val snapPosition = recyclerView.layoutManager?.let { lm ->
            snapHelper.findSnapView(lm) ?.let { v ->
                lm.getPosition(v)
            } ?: RecyclerView.NO_POSITION
        } ?: RecyclerView.NO_POSITION
        val snapPositionChanged = this.snapPosition != snapPosition
        if (snapPositionChanged) {
            onSnapPositionChangeListener.invoke(snapPosition)
            this.snapPosition = snapPosition
        }
    }

}