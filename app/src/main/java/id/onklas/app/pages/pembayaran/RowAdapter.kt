package id.onklas.app.pages.pembayaran

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.CheckoutItemBinding

class RowAdapter(
    val items: ArrayList<Pair<String, String>>,
    val icCopy: Drawable? = null,
    val onClickCopy: (text: String) -> Unit = {}
) :
    RecyclerView.Adapter<RowAdapter.Viewholder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
        Viewholder(parent)

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Viewholder, position: Int) =
        holder.bind(items[position])

    inner class Viewholder(
        parent: ViewGroup,
        val binding: CheckoutItemBinding = CheckoutItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("WrongConstant")
        fun bind(pair: Pair<String, String>) {
            binding.key = pair.first
            binding.values = pair.second

            if (copyable.contains(pair.first) && pair.second.isNotEmpty()) {
                binding.tvValues.setCompoundDrawablesWithIntrinsicBounds(null, null, icCopy, null)
                binding.tvValues.setOnClickListener { onClickCopy.invoke(pair.second) }
            } else {
                binding.tvValues.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                binding.tvValues.setOnClickListener {}
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                binding.tvValues.breakStrategy = 0
            }
            binding.executePendingBindings()
        }
    }

    private val copyable by lazy { arrayOf("Serial Token", "SN") }
}

