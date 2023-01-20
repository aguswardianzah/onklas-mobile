package id.onklas.app.pages.perpus

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.RentItemBinding
import id.onklas.app.utils.PagingAdapter

class RentAdapter : PagingAdapter<BookRentTable, RentAdapter.Viewholder>(
    object : DiffUtil.ItemCallback<BookRentTable>() {
        override fun areItemsTheSame(oldItem: BookRentTable, newItem: BookRentTable): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BookRentTable, newItem: BookRentTable): Boolean =
            oldItem == newItem
    }
) {

    override fun createItemViewholder(parent: ViewGroup, viewType: Int): Viewholder =
        Viewholder(parent)

    override fun bindItemViewholder(holder: Viewholder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun bindItemViewholder(holder: Viewholder, position: Int, payloads: MutableList<Any>) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class Viewholder(
        parent: ViewGroup,
        val binding: RentItemBinding = RentItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookRentTable) {
            binding.item = item
            binding.executePendingBindings()
        }
    }
}