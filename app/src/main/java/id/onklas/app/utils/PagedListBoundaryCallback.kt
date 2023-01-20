package id.onklas.app.utils

import androidx.paging.PagedList

class PagedListBoundaryCallback<T>(
    private val onEmpty: () -> Unit = {},
    private val onEnd: () -> Unit = {}
) : PagedList.BoundaryCallback<T>() {

    override fun onZeroItemsLoaded() = onEmpty.invoke()
    override fun onItemAtEndLoaded(itemAtEnd: T) = onEnd.invoke()
}