package id.onklas.app.pages.theory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.MateriItemBinding
import id.onklas.app.utils.PagingAdapter

class MateriAdapter(
    val onClick: (item: MateriMapelTeacher, transitionView: View) -> Unit = { _, _ -> },
    val myId: Int = -1,
    val onClickOption: (item: MateriMapelTeacher) -> Unit = { _ -> }
) :
    PagingAdapter<MateriMapelTeacher, MateriAdapter.MateriViewholder>(
        object :
            DiffUtil.ItemCallback<MateriMapelTeacher>() {
            override fun areItemsTheSame(
                oldItem: MateriMapelTeacher,
                newItem: MateriMapelTeacher
            ): Boolean = oldItem.materi.id == newItem.materi.id

            override fun areContentsTheSame(
                oldItem: MateriMapelTeacher,
                newItem: MateriMapelTeacher
            ): Boolean = oldItem == newItem
        }
    ) {

    override fun createItemViewholder(parent: ViewGroup, viewType: Int): MateriViewholder =
        MateriViewholder(parent)

    override fun bindItemViewholder(holder: MateriViewholder, position: Int) {
        getItem(position)?.let { holder.bind(it, myId == it.teacher?.sosmed_user_id) }
    }

    override fun bindItemViewholder(
        holder: MateriViewholder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        getItem(position)?.let { holder.bind(it, myId == it.teacher?.sosmed_user_id) }
    }

    inner class MateriViewholder(
        parent: ViewGroup,
        val binding: MateriItemBinding = MateriItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MateriMapelTeacher, isMine: Boolean = false) {
            binding.item = item
            binding.isMine = isMine
            binding.executePendingBindings()
            binding.option.setOnClickListener {
                if (isMine) onClickOption.invoke(item)
            }
            binding.root.setOnClickListener { onClick.invoke(item, binding.title) }
        }
    }
}