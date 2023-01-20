package id.onklas.app.pages.akm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.AkmAnswerStatementTableBinding
import id.onklas.app.databinding.AkmQuestionStatementTableItemBinding
import timber.log.Timber

class QuestionTableVh(
    parent: ViewGroup,
    var answered: Boolean,
    val answerDiffUtil: DiffUtil.ItemCallback<AkmAnswerTable>,
    val onAnswer: (item: AkmAnswerTable) -> Unit,
    private val onImageClick: (path: String) -> Unit = {},
    val binding: AkmQuestionStatementTableItemBinding = AkmQuestionStatementTableItemBinding.inflate(
        LayoutInflater.from(parent.context), parent, false
    )
) : QuestionAdapter.QuestionViewHolder(binding.root) {

    private val answerAdapter by lazy {
        object : ListAdapter<AkmAnswerTable, AnswerStatementTableVh>(answerDiffUtil) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): AnswerStatementTableVh = AnswerStatementTableVh(parent)

            override fun onBindViewHolder(holder: AnswerStatementTableVh, position: Int) {
                val item = getItem(position)
                holder.binding.choice = item
                holder.binding.selected = item.selected
                holder.binding.answered = answered
                holder.binding.image.setOnClickListener { onImageClick.invoke(item.file_path) }
                holder.binding.radioTrue.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked)
                        onAnswer.invoke(item.copy(selected = true))
                }
                holder.binding.radioFalse.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked)
                        onAnswer.invoke(item.copy(selected = false))
                }
                holder.binding.executePendingBindings()
            }

            override fun onBindViewHolder(
                holder: AnswerStatementTableVh,
                position: Int,
                payloads: MutableList<Any>
            ) {
                Timber.e("update table[$position]: $payloads")
                if (payloads.isNotEmpty()) {
                    holder.binding.answered = answered
                    holder.binding.selected = payloads.first() as Boolean
//                    holder.binding.executePendingBindings()
                } else
                    onBindViewHolder(holder, position)
            }
        }
    }

    init {
        binding.rvAnswer.adapter = answerAdapter
        binding.rvAnswer.setHasFixedSize(true)
    }

    override fun bind(item: QuestionAnswers) {
        binding.item = item
        binding.image.setOnClickListener { onImageClick.invoke(item.question.file_path) }
        binding.question.text = item.question.question.replace(formulaRegex) { match -> "\\(${match.value}\\)" }
        answerAdapter.submitList(item.answers)
        binding.executePendingBindings()
    }

    override fun update(item: QuestionAnswers) {
        answered = item.question.answered
        answerAdapter.submitList(item.answers)
    }

    private inner class AnswerStatementTableVh(
        parent: ViewGroup,
        val binding: AkmAnswerStatementTableBinding = AkmAnswerStatementTableBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)
}