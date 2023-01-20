package id.onklas.app.pages.klaspay.topup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.KlaspayTypeItemBinding
import id.onklas.app.databinding.TopupItemBinding
import id.onklas.app.databinding.TopupTypeItemBinding
import timber.log.Timber

class TopUpAdapter(
    val onParentSelect: (TypeTopupItem, Int) -> Unit,
    val onChildSelect: (TypeTopupItem) -> Unit,
    val onClickTopup: () -> Unit = {}
) : ListAdapter<TypeTopupItem, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<TypeTopupItem>() {
        override fun areItemsTheSame(oldItem: TypeTopupItem, newItem: TypeTopupItem): Boolean =
            oldItem.nameId == newItem.nameId

        override fun areContentsTheSame(
            oldItem: TypeTopupItem,
            newItem: TypeTopupItem
        ): Boolean = oldItem.showChild == newItem.showChild

        override fun getChangePayload(oldItem: TypeTopupItem, newItem: TypeTopupItem): Any =
            newItem.showChild
    }
) {

    override fun getItemViewType(position: Int): Int =
        when {
            getItem(position).nameId.equals("klaspay", true) -> 2
            getItem(position).parentName.isEmpty() -> 0
            else -> 1
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder =
        when (viewType) {
            2 -> KlaspayViewholder(parent)
            0 -> TypeViewholder(parent)
            else -> ItemViewholder(parent)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is KlaspayViewholder -> holder.bind(item)
            is TypeViewholder -> holder.bind(item)
            is ItemViewholder -> holder.bind(item)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty() && holder is TypeViewholder) {
            Timber.e("payload ${getItem(position).nameId}: $payloads")
            holder.update(payloads.first() as Boolean)
        } else {
            onBindViewHolder(holder, position)
        }
    }

    inner class KlaspayViewholder(
        parent: ViewGroup,
        val binding: KlaspayTypeItemBinding = KlaspayTypeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TypeTopupItem) {
            binding.item = item
            binding.btnTopup.setOnClickListener { onClickTopup.invoke() }
            binding.image.setOnClickListener { select(item) }
            binding.tvName.setOnClickListener { select(item) }
            binding.tvInfo.setOnClickListener { select(item) }
            binding.container.setOnClickListener { select(item) }
            binding.executePendingBindings()
        }

        private fun select(item: TypeTopupItem) {
            if (!item.needTopup)
                onChildSelect.invoke(item)
        }
    }

    inner class TypeViewholder(
        parent: ViewGroup,
        val binding: TopupTypeItemBinding = TopupTypeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TypeTopupItem) {
            binding.item = item
            binding.showChild = item.showChild
            binding.image.setOnClickListener { select(item) }
            binding.tvName.setOnClickListener { select(item) }
            binding.tvInfo.setOnClickListener { select(item) }
            binding.imgSwitch.setOnClickListener { select(item) }
            binding.container.setOnClickListener { select(item) }
            binding.executePendingBindings()
        }

        private fun select(item: TypeTopupItem) {
            onParentSelect.invoke(item, adapterPosition)
        }

        fun update(showChild: Boolean) {
            binding.showChild = showChild
            binding.executePendingBindings()
        }
    }

    inner class ItemViewholder(
        parent: ViewGroup,
        val binding: TopupItemBinding = TopupItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TypeTopupItem) {
            binding.item = item
            binding.root.setOnClickListener { select(item) }
            binding.image.setOnClickListener { select(item) }
            binding.name.setOnClickListener { select(item) }
            binding.executePendingBindings()
        }

        private fun select(item: TypeTopupItem) {
            onChildSelect.invoke(item)
        }
    }
}