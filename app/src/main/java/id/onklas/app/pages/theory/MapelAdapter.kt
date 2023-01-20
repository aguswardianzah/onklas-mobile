package id.onklas.app.pages.theory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.MapelItemBinding
import id.onklas.app.utils.PagingAdapter

class MapelAdapter(val onClick: (item: MapelTeacher, transitionView: View?) -> Unit = { _, _ -> }) :
    PagingAdapter<MapelTeacher, MapelAdapter.MapelViewholder>(object :
        DiffUtil.ItemCallback<MapelTeacher>() {
        override fun areItemsTheSame(oldItem: MapelTeacher, newItem: MapelTeacher): Boolean =
            oldItem.mapel.id == newItem.mapel.id

        override fun areContentsTheSame(oldItem: MapelTeacher, newItem: MapelTeacher): Boolean =
            oldItem == newItem
    }) {
    override fun createItemViewholder(
        parent: ViewGroup,
        viewType: Int
    ): MapelViewholder =
        MapelViewholder(parent)

    override fun bindItemViewholder(holder: MapelViewholder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun bindItemViewholder(
        holder: MapelViewholder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class MapelViewholder(
        parent: ViewGroup,
        val binding: MapelItemBinding = MapelItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MapelTeacher) {
            binding.item = item
            binding.executePendingBindings()
            binding.root.setOnClickListener { onClick.invoke(item, binding.title) }
        }
    }
}