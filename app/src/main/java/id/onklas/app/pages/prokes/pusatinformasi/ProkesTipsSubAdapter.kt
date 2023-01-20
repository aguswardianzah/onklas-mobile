package id.onklas.app.pages.prokes.pusatinformasi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.ProkesinfoTipsItem2Binding
import id.onklas.app.pages.prokes.TipsItem

class ProkesTipsSubAdapter(
    val list: List<TipsItem>,
) : RecyclerView.Adapter<ProkesTipsSubAdapter.ProkesInfoViewHolder>() {

    inner class ProkesInfoViewHolder(
        parent: ViewGroup,
        val binding: ProkesinfoTipsItem2Binding = ProkesinfoTipsItem2Binding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bin(item: TipsItem) {
            binding.img.setImageResource(item.iconId)
            binding.txt.text = item.tipsTextt
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProkesInfoViewHolder =
        ProkesInfoViewHolder(parent)

    override fun onBindViewHolder(holder: ProkesInfoViewHolder, position: Int) =
        holder.bin(list[position])

    override fun getItemCount(): Int = list.size
}