package id.onklas.app.pages.klaspay.topup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.onklas.app.databinding.KlaspayTopupProductItemBinding

class TopupProductAdapter (var items: List<VirtualAccountItem>, val listener: AdapterListener):
    RecyclerView.Adapter<TopupProductAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder (
            KlaspayTopupProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.name.text = items[position].payment_name
        Glide.with(holder.binding.container)
                .load(items[position].payment_logo).into(holder.binding.image);
        holder.binding.container.setOnClickListener {
            listener.onClick( items[position] )
        }
    }

    class ViewHolder(val binding: KlaspayTopupProductItemBinding): RecyclerView.ViewHolder(binding.root)

    interface AdapterListener {
        fun onClick(item: VirtualAccountItem)
    }
}