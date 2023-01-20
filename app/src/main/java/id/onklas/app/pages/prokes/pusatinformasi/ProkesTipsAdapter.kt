package id.onklas.app.pages.prokes.pusatinformasi

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.ProkesinfoTipsItemBinding
import id.onklas.app.pages.prokes.TipsMain

class ProkesTipsAdapter(
        val onParentSelected : (TipsMain, Int)->Unit
) : ListAdapter<TipsMain, ProkesTipsAdapter.ProkesViewHolder>(
        object : DiffUtil.ItemCallback<TipsMain>() {
            override fun areItemsTheSame(oldItem: TipsMain, newItem: TipsMain): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(
                    oldItem: TipsMain,
                    newItem: TipsMain
            ): Boolean = oldItem.isShowChild == newItem.isShowChild

            override fun getChangePayload(oldItem: TipsMain, newItem: TipsMain): Any? =
                    newItem.isShowChild

        }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProkesViewHolder =
            ProkesViewHolder(parent)

    override fun onBindViewHolder(holder: ProkesViewHolder, position: Int) =
            holder.bind(getItem(position))

    override fun onBindViewHolder(holder: ProkesViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            holder.update(payloads.first() as Boolean)
        } else {
            onBindViewHolder(holder, position)
        }
    }

    inner class ProkesViewHolder(
            parent: ViewGroup,
            val binding: ProkesinfoTipsItemBinding = ProkesinfoTipsItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    ) : RecyclerView.ViewHolder(binding.root) {
        fun update(showChild: Boolean) {
            binding.showChild = showChild
            binding.executePendingBindings()
        }

        fun bind(item: TipsMain) {
            binding.item = item
            binding.txtDesc.text = Html.fromHtml(item.title)
            binding.labelNote.text = Html.fromHtml(item.note)
            binding.showChild = item.isShowChild
            binding.root.setOnClickListener {select(item)}
            binding.rvTips.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                adapter = ProkesTipsSubAdapter(item.tipsitem)
            }
        }
        fun select(item: TipsMain){

            onParentSelected.invoke(item,adapterPosition)
        }
    }


}