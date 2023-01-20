package id.onklas.app.pages.homework

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.utils.PagingAdapter

abstract class HomeworkAdapter<T : RecyclerView.ViewHolder> :
    PagingAdapter<HomeworkItemTable, T>(object : DiffUtil.ItemCallback<HomeworkItemTable>() {
        override fun areItemsTheSame(
            oldItem: HomeworkItemTable,
            newItem: HomeworkItemTable
        ): Boolean =
            oldItem.homework.id == newItem.homework.id

        override fun areContentsTheSame(
            oldItem: HomeworkItemTable,
            newItem: HomeworkItemTable
        ): Boolean =
            oldItem == newItem
    }) {
    abstract override fun createItemViewholder(parent: ViewGroup, viewType: Int): T

    abstract override fun bindItemViewholder(holder: T, position: Int)

    override fun bindItemViewholder(holder: T, position: Int, payloads: MutableList<Any>) {
    }
}