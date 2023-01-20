package id.onklas.app.pages.akm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.AkmAnswerChoiceBinding
import id.onklas.app.databinding.AkmAnswerChoiceCheckBinding
import id.onklas.app.databinding.AkmAnswerChoiceImageBinding
import id.onklas.app.databinding.AkmQuestionMultipleItemBinding
import id.onklas.app.utils.LinearSpaceDecoration

class QuestionMultipleVh(
    parent: ViewGroup,
    val answerType: Int,
    val answerDiffUtil: DiffUtil.ItemCallback<AkmAnswerTable>,
    choiceItemDecor: LinearSpaceDecoration,
    val onAnswer: (item: AkmAnswerTable) -> Unit,
    private val onImageClick: (path: String) -> Unit = {},
    val binding: AkmQuestionMultipleItemBinding = AkmQuestionMultipleItemBinding.inflate(
        LayoutInflater.from(parent.context), parent, false
    )
) : QuestionAdapter.QuestionViewHolder(binding.root) {

    private val listAlphabet by lazy { ('A'..'Z').toList() }

    private val answerAdapter by lazy {
        object : ListAdapter<AkmAnswerTable, RecyclerView.ViewHolder>(answerDiffUtil) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder = when (answerType) {
                AkmAnswerType.ANSWER_MULTIPLE_CHOICE_SINGLE_CORRECT_IMAGE -> AnswerChoiceImageVh(parent)
                AkmAnswerType.ANSWER_MULTIPLE_CHOICE_MULTIPLE_CORRECT -> AnswerChoiceCheckVh(parent)
                else -> AnswerChoiceVh(parent)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val item = getItem(position)
                when (holder) {
                    is AnswerChoiceVh -> {
                        holder.binding.choice = item
                        holder.binding.choiceLabel = listAlphabet[position].toString()
                        holder.binding.scored = false
                        holder.binding.image.setOnClickListener { onImageClick.invoke(item.file_path) }
                        holder.binding.root.setOnClickListener {
                            onAnswer.invoke(item.copy(selected = true))
                        }
                        holder.binding.executePendingBindings()
                    }
                    is AnswerChoiceImageVh -> {
                        holder.binding.choice = item
                        holder.binding.choiceLabel = listAlphabet[position].toString()
                        holder.binding.scored = false
                        holder.binding.image.setOnClickListener { onImageClick.invoke(item.file_path) }
                        holder.binding.root.setOnClickListener {
                            onAnswer.invoke(item.copy(selected = true))
                        }
                        holder.binding.executePendingBindings()
                    }
                    is AnswerChoiceCheckVh -> {
                        holder.binding.choice = item
                        holder.binding.scored = false
                        holder.binding.image.setOnClickListener { onImageClick.invoke(item.file_path) }
                        holder.binding.label.setOnCheckedChangeListener { buttonView, isChecked ->
                            onAnswer.invoke(item.copy(selected = isChecked))
                        }
                        holder.binding.executePendingBindings()
                    }
                }
            }

            override fun onBindViewHolder(
                holder: RecyclerView.ViewHolder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                if (payloads.isNotEmpty()) {
                    val item = getItem(position).copy(selected = payloads.first() as Boolean)
                    when (holder) {
                        is AnswerChoiceVh -> {
                            holder.binding.choice = item
                            holder.binding.executePendingBindings()
                        }
                        is AnswerChoiceImageVh -> {
                            holder.binding.choice = item
                            holder.binding.executePendingBindings()
                        }
                        is AnswerChoiceCheckVh -> {
                            holder.binding.choice = item
                            holder.binding.executePendingBindings()
                        }
                    }
                } else
                    onBindViewHolder(holder, position)
            }
        }
    }

    init {
        binding.rvAnswer.addItemDecoration(choiceItemDecor)
        binding.rvAnswer.adapter = answerAdapter
        binding.rvAnswer.setHasFixedSize(true)
    }

    override fun bind(item: QuestionAnswers) {
        binding.item = item
        binding.question.text = item.question.question.replace(formulaRegex) { match -> "\\(${match.value}\\)" }
        binding.image.setOnClickListener { onImageClick.invoke(item.question.file_path) }
        answerAdapter.submitList(item.answers)
        binding.executePendingBindings()
    }

    override fun update(update: QuestionAnswers) {
        answerAdapter.submitList(update.answers)
    }

    private inner class AnswerChoiceVh(
        parent: ViewGroup,
        val binding: AkmAnswerChoiceBinding = AkmAnswerChoiceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    private inner class AnswerChoiceImageVh(
        parent: ViewGroup,
        val binding: AkmAnswerChoiceImageBinding = AkmAnswerChoiceImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    private inner class AnswerChoiceCheckVh(
        parent: ViewGroup,
        val binding: AkmAnswerChoiceCheckBinding = AkmAnswerChoiceCheckBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)
}