package id.onklas.app.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView

class LinearSpaceDecoration(
    private var orientation: Int = OrientationHelper.VERTICAL,
    private var space: Int = 10,
    private val includeTop: Boolean = false,
    private val includeBottom: Boolean = false,
    private val customEdge: Int = space,
    private val crossEdge: Int = 0
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        if (position > 0)
            if (orientation == OrientationHelper.VERTICAL)
                outRect.top = space
            else outRect.left = space
        else {
            if (includeTop)
                if (orientation == OrientationHelper.VERTICAL)
                    outRect.top = customEdge
                else outRect.left = customEdge
        }

        parent.adapter?.let {
            if (position == it.itemCount - 1 && includeBottom)
                if (orientation == OrientationHelper.VERTICAL)
                    outRect.bottom = customEdge
                else outRect.right = customEdge
        }

        if (crossEdge > 0) {
            outRect.left = crossEdge
            outRect.right = crossEdge
        }
    }
}