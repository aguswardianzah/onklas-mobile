package id.onklas.app.pages.pembayaran

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.PembayaranMenuItemBinding

class PaymentItemAdapter(
    val list: List<Triple<Int, String, Boolean>>,
    val onClick: (Int) -> Unit = { _ -> }
) : RecyclerView.Adapter<PaymentItemAdapter.Viewholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
        Viewholder(parent)

    override fun onBindViewHolder(holder: Viewholder, position: Int) = holder.bind(list[position])

    override fun getItemCount(): Int = list.size

    inner class Viewholder(
        parent: ViewGroup,
        val binding: PembayaranMenuItemBinding = PembayaranMenuItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Triple<Int, String, Boolean>) {
            binding.resIcon = item.first
            binding.name = item.second
            binding.iconSoon = item.third
            binding.executePendingBindings()
            binding.root.setOnClickListener { onClick.invoke(adapterPosition) }
            binding.icon.setOnClickListener { onClick.invoke(adapterPosition) }
            binding.label.setOnClickListener { onClick.invoke(adapterPosition) }
        }
    }
}