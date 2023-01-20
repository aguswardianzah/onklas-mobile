package id.onklas.app.pages.ppob.bpjs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.*
import id.onklas.app.databinding.ListBpjsPageBinding
import id.onklas.app.databinding.PdamItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.PageDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class ListBpjsPage : PageDialogFragment() {

    private lateinit var binding: ListBpjsPageBinding
    private val viewmodel by activityViewModels<BpjsViewModel> { component.bpjsVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ListBpjsPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rvBpjs.apply {
            adapter = this@ListBpjsPage.adapter
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    OrientationHelper.VERTICAL
                )
            )
        }
    }

    private val dateFormat by lazy { SimpleDateFormat("MMMM yyyy", Locale("id")) }
    private val listBpjs by lazy {
        (0 until 12).map {
            (it + 1) to Calendar.getInstance()
                .apply { add(Calendar.MONTH, it) }
                .let { dateFormat.format(it.time) }
        }
    }

    private val adapter by lazy {
        object : RecyclerView.Adapter<Viewholder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(listBpjs[position])

            override fun getItemCount(): Int = listBpjs.size
        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: PdamItemBinding = PdamItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<Int, String>) {
            binding.labelTop.text = item.second
            binding.imageTop.visibility = View.GONE
            binding.root.setOnClickListener { select(item) }
            binding.labelTop.setOnClickListener { select(item) }
            binding.imageTop.setOnClickListener { select(item) }
        }

        private fun select(item: Pair<Int, String>) {
            viewmodel.inputPeriode.postValue(item)
            dismiss()
        }
    }
}