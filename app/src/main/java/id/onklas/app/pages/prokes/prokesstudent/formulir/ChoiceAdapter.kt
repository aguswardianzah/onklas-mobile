package id.onklas.app.pages.prokes.prokesstudent.formulir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.ProkesChoice1Binding
import id.onklas.app.pages.prokes.ListChoice

class ChoiceAdapter(
        val onParentSelected: (ListChoice, Int) -> Unit
) : ListAdapter<ListChoice, ChoiceAdapter.choiceViewholder>(object : DiffUtil.ItemCallback<ListChoice>() {
    override fun areItemsTheSame(oldItem: ListChoice, newItem: ListChoice): Boolean =
            oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ListChoice, newItem: ListChoice): Boolean =
            oldItem.isChosen == newItem.isChosen

    override fun getChangePayload(oldItem: ListChoice, newItem: ListChoice): Any? =
            newItem.isChosen

}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): choiceViewholder =
            choiceViewholder(parent)

    override fun onBindViewHolder(holder: choiceViewholder, position: Int) =
            holder.bind(getItem(position), position)

    override fun onBindViewHolder(holder: choiceViewholder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
//            Timber.e("payload $payloads")
            holder.update(payloads.first() as Boolean)
        } else {
            onBindViewHolder(holder, position)
        }
    }


    inner class choiceViewholder(
            parent: ViewGroup,
            val binding: ProkesChoice1Binding = ProkesChoice1Binding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ListChoice, position: Int) {
            binding.item = item
            binding.rdChoice.setOnClickListener { select(item) }
            binding.root.setOnClickListener { select(item) }
        }

        fun update(isChosen: Boolean) {
//            Timber.e("updated $isChosen")
            binding.chosen = isChosen
            binding.executePendingBindings()
        }

        fun select(item: ListChoice) {
//            Timber.e("select")
            onParentSelected.invoke(item, adapterPosition)
        }

    }


}

