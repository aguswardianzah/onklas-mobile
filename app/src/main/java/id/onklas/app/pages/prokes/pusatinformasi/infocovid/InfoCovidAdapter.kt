package id.onklas.app.pages.prokes.pusatinformasi.infocovid

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.InfocovidBannerItemBinding
import id.onklas.app.databinding.InfocovidItem1Binding
import id.onklas.app.databinding.InfocovidItem2Binding
import id.onklas.app.pages.prokes.ListInfoCovid

class InfoCovidAdapter(
        val onParentSelected: (ListInfoCovid, Int) -> Unit,
) : ListAdapter<ListInfoCovid, RecyclerView.ViewHolder>(
        object : DiffUtil.ItemCallback<ListInfoCovid>() {
            override fun areItemsTheSame(oldItem: ListInfoCovid, newItem: ListInfoCovid): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(
                    oldItem: ListInfoCovid,
                    newItem: ListInfoCovid
            ): Boolean = oldItem.isShowChild == newItem.isShowChild


            override fun getChangePayload(oldItem: ListInfoCovid, newItem: ListInfoCovid): Any? =
                    newItem.isShowChild
        }
) {
    override fun getItemViewType(position: Int): Int =
            when {
                getItem(position).infoCode == 1 -> 1
                getItem(position).infoCode == 2 -> 2
                else -> 3
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                1 -> BannerViewholder(parent)
                2 -> TitleViewholder(parent)
                else -> ItemViewholder(parent)
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is BannerViewholder -> holder.bind(item)
            is TitleViewholder -> holder.bind(item)
            is ItemViewholder -> holder.bind(item)
            is ItemViewholder -> holder.bind(item)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty() && holder is TitleViewholder) {
//            Timber.e("payload $payloads")
            holder.update(payloads.first() as Boolean)
        } else {
            onBindViewHolder(holder, position)
        }
    }

    inner class BannerViewholder(
            parent: ViewGroup,
            val binding: InfocovidBannerItemBinding = InfocovidBannerItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListInfoCovid) {
            binding.item = item
        }
    }

    inner class TitleViewholder(
            parent: ViewGroup,
            val binding: InfocovidItem1Binding = InfocovidItem1Binding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListInfoCovid) {
            binding.item = item
            binding.showChild = item.isShowChild
            binding.root.setOnClickListener { select(item) }
            binding.txtTitle.setOnClickListener { select(item) }
            binding.actionExpand.setOnClickListener { select(item) }
        }

        fun select(item: ListInfoCovid) {
            onParentSelected.invoke(item, adapterPosition)
        }

        fun update(showChild: Boolean) {
            binding.showChild = showChild
            binding.executePendingBindings()
        }
    }

    inner class ItemViewholder(
            parent: ViewGroup,
            val binding: InfocovidItem2Binding = InfocovidItem2Binding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListInfoCovid) {
            binding.item = item
        }
    }
}