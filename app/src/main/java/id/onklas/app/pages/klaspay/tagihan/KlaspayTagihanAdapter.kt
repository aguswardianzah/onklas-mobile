package id.onklas.app.pages.klaspay.tagihan

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.KlaspayTagihanMenungguItemBinding
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class KlaspayTagihanAdapter(
    var items: List<KlaspayTransactionInvoiceItem>,
    val listener: AdapterListener
) :
    RecyclerView.Adapter<KlaspayTagihanAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        KlaspayTagihanMenungguItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount() = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.textId.text = item.transaction_id
        holder.binding.textDate.text = item.created_date
        item.details!!.expired_date?.let { date ->
            holder.binding.textDateExpired.text = dateFormat(date)
            if (dateAfter(dateFormat(date)))
                holder.binding.buttonPay.apply {
                    isEnabled = false
                    text = "Kadaluarsa"
                }
            else
                holder.binding.buttonPay.apply {
                    isEnabled = true
                    text = "Bayar Sekarang"
                }
        }
        if (item.details!!.expired_date.isNullOrEmpty()) {
            holder.binding.buttonPay.apply {
                isEnabled = false
                text = "Kadaluarsa"
            }
        }
        holder.binding.textTransaksi.text = "${item.type} ${item.priceLabel}"
        holder.binding.textBank.text = item.details.payment_method
        holder.binding.buttonPay.setOnClickListener {
            listener.onClick(items[position])
        }
    }

    class ViewHolder(val binding: KlaspayTagihanMenungguItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface AdapterListener {
        fun onClick(item: KlaspayTransactionInvoiceItem)
    }

    private fun dateFormat(date: String?): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val outputFormatDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id"))
        val resDate: Date = inputFormat.parse(date)
        return outputFormatDate.format(resDate)
    }

    private fun dateAfter(date: String?): Boolean {
        val currentDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val dateAfter = (dateFormat.parse(currentDate)!!.after(dateFormat.parse(date!!)))
        Timber.e("dateAfter $dateAfter")
        return dateAfter
    }
}