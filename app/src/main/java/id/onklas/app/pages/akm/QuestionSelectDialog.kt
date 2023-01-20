package id.onklas.app.pages.akm

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.AkmQuestionSelectItemBinding
import id.onklas.app.databinding.SelectQuestionDialogBinding

class QuestionSelectDialog(
    context: Context,
    val listQuestions: List<AkmQuestionTable>,
    val onClick: (pos: Int) -> Unit
) : AlertDialog(context) {

    private val binding by lazy { SelectQuestionDialogBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        binding.rvQuestionNumbers.adapter = adapter
        adapter.submitList(listQuestions)
    }

    private val adapter by lazy {
        object : ListAdapter<AkmQuestionTable, Viewholder>(object :
            DiffUtil.ItemCallback<AkmQuestionTable>() {
            override fun areItemsTheSame(
                oldItem: AkmQuestionTable,
                newItem: AkmQuestionTable
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: AkmQuestionTable,
                newItem: AkmQuestionTable
            ): Boolean = oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: AkmQuestionSelectItemBinding = AkmQuestionSelectItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AkmQuestionTable) {
            binding.text = "${adapterPosition + 1}"
            binding.item = item
            binding.root.setOnClickListener {
                onClick.invoke(adapterPosition)
                dismiss()
            }
            binding.executePendingBindings()
        }
    }
}