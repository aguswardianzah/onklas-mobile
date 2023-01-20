package id.onklas.app.pages.klaspay.riwayat

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.KlaspayRiwayatItemBinding
import java.util.*

class KlaspayRiwayatAdapter(
    val listener: (TransactionHistory) -> Unit
) :
    ListAdapter<TransactionHistory, KlaspayRiwayatAdapter.ViewHolder>(object :
        DiffUtil.ItemCallback<TransactionHistory>() {
        override fun areItemsTheSame(
            oldItem: TransactionHistory,
            newItem: TransactionHistory
        ): Boolean = oldItem.transaction_id == newItem.transaction_id

        override fun areContentsTheSame(
            oldItem: TransactionHistory,
            newItem: TransactionHistory
        ): Boolean = oldItem == newItem
    }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        KlaspayRiwayatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.icon.setImageResource(
            when (item.type.toUpperCase(Locale.ROOT)) {
                "TOPUP" -> R.drawable.ic_topup
                "SPP" -> R.drawable.ic_pay_spp
                "PULSA" -> R.drawable.ic_pulsa
                "PLN" -> R.drawable.ic_bayar_listrik
                "PDAM" -> R.drawable.ic_bayar_air
                "INTERNET" -> R.drawable.ic_internet
                "GAME" -> R.drawable.ic_internet
                else -> R.drawable.ic_history_bordered
            }
        )

        holder.binding.textId.text = "ID: ${item.transaction_id}"
        holder.binding.textType.text = item.type
        holder.binding.textStatus.text = item.transaction_status
        holder.binding.textPrice.text = item.priceLabel
        holder.binding.textDate.text = item.created_at
        holder.binding.container.setOnClickListener {
            listener.invoke(item)
        }
    }

    class ViewHolder(val binding: KlaspayRiwayatItemBinding) : RecyclerView.ViewHolder(binding.root)
}