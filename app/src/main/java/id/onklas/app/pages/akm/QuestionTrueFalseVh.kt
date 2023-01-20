package id.onklas.app.pages.akm

import android.view.LayoutInflater
import android.view.ViewGroup
import id.onklas.app.databinding.AkmQuestionTrueFalseItemBinding
import timber.log.Timber

class QuestionTrueFalseVh(
    parent: ViewGroup,
    private val onAnswer: (item: AkmAnswerTable) -> Unit,
    private val onImageClick: (path: String) -> Unit = {},
    val binding: AkmQuestionTrueFalseItemBinding = AkmQuestionTrueFalseItemBinding.inflate(
        LayoutInflater.from(parent.context), parent, false
    )
) : QuestionAdapter.QuestionViewHolder(binding.root) {

    override fun bind(item: QuestionAnswers) {
        Timber.e("bind: $item")
        binding.item = item
        binding.answered = item.question.answered
        binding.image.setOnClickListener { onImageClick.invoke(item.question.file_path) }
        binding.question.text = item.question.question.replace(formulaRegex) { match -> "\\(${match.value}\\)" }

        val answer = item.answers.first()
        binding.answer = answer

        binding.tvTrue.setOnClickListener { onAnswer.invoke(answer.copy(selected = true)) }
        binding.tvFalse.setOnClickListener { onAnswer.invoke(answer.copy(selected = false)) }

        binding.executePendingBindings()
    }

    override fun update(item: QuestionAnswers) {
        Timber.e("update: $item")
        binding.answer = item.answers.first()
        binding.answered = item.question.answered
        binding.executePendingBindings()
    }
}