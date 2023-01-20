package id.onklas.app.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.UserNameItemBinding
import id.onklas.app.pages.sekolah.sosmed.UserTable

class UsernameAdapter(var onClick: (item: UserTable) -> Unit = {}) :
    ListAdapter<UserTable, UsernameAdapter.UsernameViewholder>(
        object : DiffUtil.ItemCallback<UserTable>() {
            override fun areItemsTheSame(oldItem: UserTable, newItem: UserTable): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: UserTable, newItem: UserTable): Boolean =
                oldItem == newItem
        }
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsernameViewholder =
        UsernameViewholder(parent)

    override fun onBindViewHolder(holder: UsernameViewholder, position: Int) =
        holder.bind(getItem(position), onClick)

    inner class UsernameViewholder(
        parent: ViewGroup,
        val binding: UserNameItemBinding = UserNameItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserTable, onClick: (item: UserTable) -> Unit) {
            binding.item = item
            binding.executePendingBindings()
            binding.root.setOnClickListener { onClick.invoke(item) }
            binding.image.setOnClickListener { onClick.invoke(item) }
            binding.name.setOnClickListener { onClick.invoke(item) }
            binding.username.setOnClickListener { onClick.invoke(item) }
        }
    }
}