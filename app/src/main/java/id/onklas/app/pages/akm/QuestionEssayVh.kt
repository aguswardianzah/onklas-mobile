package id.onklas.app.pages.akm

import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import id.onklas.app.databinding.AkmQuestionEssayItemBinding
import java.util.*

class QuestionEssayVh(
    parent: ViewGroup,
    val answerType: Int,
    private val onUpdate: (answer: String, item: QuestionAnswers) -> Unit,
    private val onImageClick: (path: String) -> Unit = {},
    val binding: AkmQuestionEssayItemBinding = AkmQuestionEssayItemBinding.inflate(
        LayoutInflater.from(parent.context), parent, false
    )
) : QuestionAdapter.QuestionViewHolder(binding.root) {

    private var timer: Timer? = null
    override fun bind(item: QuestionAnswers) {
        binding.item = item
        binding.image.setOnClickListener { onImageClick.invoke(item.question.file_path) }
        binding.question.text = item.question.question.replace(formulaRegex) { match -> "\\(${match.value}\\)" }

        // hide input essay temporarily
//        binding.layoutEssay.visibility = View.GONE
//        onUpdate.invoke("", item)

        binding.layoutEssay.isCounterEnabled = answerType == AkmAnswerType.ANSWER_ESSAY

        if (answerType == AkmAnswerType.ANSWER_ESSAY_NUM) {
            binding.inputEssay.inputType = InputType.TYPE_CLASS_NUMBER
        } else {
            binding.inputEssay.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        binding.inputEssay.doAfterTextChanged {
            timer?.cancel()
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    var text = binding.inputEssay.text.toString()

                    if (answerType == AkmAnswerType.ANSWER_ESSAY_WORD && text.contains(" "))
                        Handler(Looper.getMainLooper()).post {
                            text = text.replace(" ", "")
                            binding.inputEssay.setText(text)
                            binding.inputEssay.setSelection(text.length)
                        }

                    onUpdate.invoke(text, item)
                }
            }, 200)
        }

        binding.executePendingBindings()
    }

    override fun update(item: QuestionAnswers) {
//        binding.item = update
//        binding.executePendingBindings()
    }
}