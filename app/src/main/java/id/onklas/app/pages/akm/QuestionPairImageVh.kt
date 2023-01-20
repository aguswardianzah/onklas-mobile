package id.onklas.app.pages.akm

import android.annotation.SuppressLint
import android.content.ClipData
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.AkmAnswerPairImage1Binding
import id.onklas.app.databinding.AkmQuestionPairImageItemBinding
import id.onklas.app.utils.LinearSpaceDecoration
import timber.log.Timber

class QuestionPairImageVh(
    parent: ViewGroup,
    private val onPair: (answers: List<AkmAnswerTable>) -> Unit,
    private val onImageClick: (path: String) -> Unit = {},
    val binding: AkmQuestionPairImageItemBinding = AkmQuestionPairImageItemBinding.inflate(
        LayoutInflater.from(parent.context), parent, false
    )
) : QuestionAdapter.QuestionViewHolder(binding.root) {

    private val listAlphabet by lazy { ('A'..'Z').toList() }

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
                newItem
        }
    }

    private val answerAdapter by lazy {
        object : ListAdapter<AkmAnswerTable, ImagePairVH>(answerDiffUtil) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagePairVH =
                ImagePairVH(parent)

            @SuppressLint("ClickableViewAccessibility")
            override fun onBindViewHolder(holder: ImagePairVH, position: Int) {
                val item = getItem(position)
                holder.binding.choice = item
                holder.binding.choiceLabel = listAlphabet[position].toString()

                holder.binding.image.setOnClickListener { onImageClick.invoke(item.first_file_path) }
                holder.binding.dragArea.setOnClickListener { onImageClick.invoke(item.second_file_path) }

                holder.binding.root.setOnDragListener { v, event ->
                    try {
                        when (event.action) {
                            DragEvent.ACTION_DRAG_ENTERED -> {
                                Timber.e("entered target[$position]: $event")

                                val fromPos = event.clipDescription.label.toString().toInt()
                                val toTop = fromPos > position
                                val lastVisible =
                                    (binding.rvFirst.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() - 1
                                val firstVisible =
                                    (binding.rvFirst.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() + 1
                                val vhToScroll = if (toTop) firstVisible else lastVisible
                                Timber.e("fromPos: $fromPos -- position: $position")
                                Timber.e("firstFisible: $firstVisible -- lastVisible: $lastVisible")
                                Timber.e("toTop: $toTop -- vhToScroll: $vhToScroll")
                                if (fromPos != position && position == vhToScroll)
                                    binding.scrollView.postDelayed({
                                        binding.scrollView.smoothScrollTo(
                                            0,
                                            binding.rvFirst.top +
                                                    (if (toTop) holder.binding.root.bottom else holder.binding.root.top)
                                        )
                                    }, 100)
                            }
                            DragEvent.ACTION_DROP -> {
                                Timber.e("drop target[$position]: ${event.clipData.getItemAt(0).text}")

                                (event.localState as View).visibility = View.VISIBLE

                                val fromPos = event.clipData.getItemAt(0).text.toString().toInt()

                                if (fromPos != position) {

                                    val oldFromItem = masterList[fromPos]
                                    val oldToItem = masterList[position]

                                    val newFromItem = masterList[fromPos].copy(
                                        selected_id = oldToItem.selected_id,
                                        second_file_path = oldToItem.second_file_path
                                    )
                                    val newToItem = masterList[position].copy(
                                        selected_id = oldFromItem.selected_id,
                                        second_file_path = oldFromItem.second_file_path
                                    )

                                    masterList[fromPos] = newFromItem
                                    masterList[position] = newToItem

                                    printAnswer(masterList)
                                    onPair(masterList)
                                }
                            }
                            DragEvent.ACTION_DRAG_ENDED -> {
                                (event.localState as View).visibility = View.VISIBLE
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                    true
                }

                holder.binding.dragArea.setOnLongClickListener { view ->
                    val data = ClipData.newPlainText("$position", "$position")
                    val shadowBuilder = View.DragShadowBuilder(view)
                    view.startDrag(data, shadowBuilder, view, 0)
                    view.visibility = View.INVISIBLE
                    true
                }

                holder.binding.executePendingBindings()
            }

            override fun onBindViewHolder(
                holder: ImagePairVH,
                position: Int,
                payloads: MutableList<Any>
            ) {
                if (payloads.isNotEmpty()) {
                    val item = payloads.first() as AkmAnswerTable
                    holder.binding.choice = item
                    holder.binding.executePendingBindings()
                } else
                    onBindViewHolder(holder, position)
            }
        }
    }

    private val itemSpaceDecoration = LinearSpaceDecoration(
        space = binding.root.resources.getDimensionPixelSize(
            R.dimen._8sdp
        )
    )

    init {
        binding.rvFirst.addItemDecoration(itemSpaceDecoration)
        binding.rvFirst.adapter = answerAdapter
        binding.rvFirst.setHasFixedSize(true)
        binding.rvFirst.isNestedScrollingEnabled = false
    }

    private val masterList: MutableList<AkmAnswerTable> = mutableListOf()

    override fun bind(item: QuestionAnswers) {
        binding.item = item
        binding.image.setOnClickListener { onImageClick.invoke(item.question.file_path) }
        binding.question.text = item.question.question.replace(formulaRegex) { match -> "\\(${match.value}\\)" }

        masterList.clear()
        masterList.addAll(item.answers)
        answerAdapter.submitList(item.answers)

        binding.executePendingBindings()
    }

    override fun update(item: QuestionAnswers) {
        masterList.clear()
        masterList.addAll(item.answers)
        answerAdapter.submitList(item.answers)
    }

    private fun printAnswer(items: List<AkmAnswerTable>) {
        items.forEach {
            Timber.e("${it.id} --> ${it.selected_id} --- ${it.first_file_path} --> ${it.second_file_path}")
        }
    }

    private inner class ImagePairVH(
        parent: ViewGroup,
        val binding: AkmAnswerPairImage1Binding = AkmAnswerPairImage1Binding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)
}