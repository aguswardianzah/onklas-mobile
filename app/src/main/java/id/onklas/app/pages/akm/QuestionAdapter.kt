package id.onklas.app.pages.akm

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.utils.LinearSpaceDecoration

class QuestionAdapter(
    private val onUpdateEssay: (answer: String, item: QuestionAnswers) -> Unit,
    private val onAnswer: (item: AkmAnswerTable) -> Unit,
    private val onPair: (answers: List<AkmAnswerTable>) -> Unit,
    private val onImageClick: (path: String) -> Unit = {}
) : ListAdapter<QuestionAnswers, QuestionAdapter.QuestionViewHolder>(
    object : DiffUtil.ItemCallback<QuestionAnswers>() {
        override fun areItemsTheSame(
            oldItem: QuestionAnswers,
            newItem: QuestionAnswers
        ): Boolean = oldItem.question.id == newItem.question.id

        override fun areContentsTheSame(
            oldItem: QuestionAnswers,
            newItem: QuestionAnswers
        ): Boolean = oldItem == newItem

        override fun getChangePayload(
            oldItem: QuestionAnswers,
            newItem: QuestionAnswers
        ): Any = newItem
    }) {

    private val choiceItemDecor by lazy { LinearSpaceDecoration() }

    private val answerDiffUtil by lazy {
        object : DiffUtil.ItemCallback<AkmAnswerTable>() {
            override fun areItemsTheSame(
                oldItem: AkmAnswerTable,
                newItem: AkmAnswerTable
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: AkmAnswerTable,
                newItem: AkmAnswerTable
            ): Boolean = oldItem == newItem

            override fun getChangePayload(oldItem: AkmAnswerTable, newItem: AkmAnswerTable): Any =
                newItem.selected
        }
    }

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val item = getItem(viewType)
        return when (item.question.type) {
            in arrayOf(
                AkmAnswerType.ANSWER_ESSAY,
                AkmAnswerType.ANSWER_ESSAY_NUM,
                AkmAnswerType.ANSWER_ESSAY_WORD
            ) -> QuestionEssayVh(
                parent,
                item.question.type,
                onUpdateEssay,
                onImageClick
            )
            AkmAnswerType.ANSWER_MULTIPLE_CHOICE_MULTIPLE_CORRECT_TABLE -> QuestionTableVh(
                parent,
                item.question.answered,
                answerDiffUtil,
                onAnswer,
                onImageClick
            )
            AkmAnswerType.ANSWER_PAIRING -> QuestionPairVh(
                parent,
//                answerDiffUtil,
                onPair,
                onImageClick
            )
            AkmAnswerType.ANSWER_PAIRING_IMAGE -> QuestionPairImageVh(
                parent,
//                answerDiffUtil,
                onPair,
                onImageClick
            )
            AkmAnswerType.ANSWER_MULTIPLE_CHOICE_TRUE_FALSE -> QuestionTrueFalseVh(
                parent,
                onAnswer,
                onImageClick
            )
            else -> QuestionMultipleVh(
                parent,
                item.question.type,
                answerDiffUtil,
                choiceItemDecor,
                onAnswer,
                onImageClick
            )
        }
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is QuestionEssayVh -> holder.bind(item)
            is QuestionMultipleVh -> holder.bind(item)
            is QuestionTableVh -> holder.bind(item)
            is QuestionPairVh -> holder.bind(item)
            is QuestionPairImageVh -> holder.bind(item)
            is QuestionTrueFalseVh -> holder.bind(item)
        }
    }

    override fun onBindViewHolder(
        holder: QuestionViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val item = payloads.first() as QuestionAnswers
            when (holder) {
                is QuestionEssayVh -> holder.update(item)
                is QuestionMultipleVh -> holder.update(item)
                is QuestionTableVh -> holder.update(item)
                is QuestionPairVh -> holder.update(item)
                is QuestionPairImageVh -> holder.update(item)
                is QuestionTrueFalseVh -> holder.update(item)
            }
        } else
            onBindViewHolder(holder, position)
    }

    abstract class QuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: QuestionAnswers)
        abstract fun update(item: QuestionAnswers)

        val formulaRegex = Regex("(?<=(data-value=\"))(.*?)(?=\")")
    }
}