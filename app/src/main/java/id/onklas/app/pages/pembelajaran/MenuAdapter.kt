package id.onklas.app.pages.pembelajaran

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.PembelajaranMenuItemBinding

class MenuAdapter(
    val list: List<Triple<Int, String, Boolean>>,
    val onClick: (Int, View) -> Unit = { _, _ -> }
): RecyclerView.Adapter<MenuAdapter.MenuViewholder>() {

    inner class MenuViewholder(
        parent: ViewGroup,
        val binding: PembelajaranMenuItemBinding = PembelajaranMenuItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Triple<Int, String, Boolean>) {
            binding.icon.setImageResource(item.first)
            binding.name = item.second
            binding.soon.visibility = if (item.third) View.VISIBLE else View.GONE
            binding.root.setOnClickListener {
                onClick.invoke(
                    adapterPosition,
                    binding.label
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewholder =
        MenuViewholder(parent)

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: MenuViewholder, position: Int) =
        holder.bind(list[position])
}