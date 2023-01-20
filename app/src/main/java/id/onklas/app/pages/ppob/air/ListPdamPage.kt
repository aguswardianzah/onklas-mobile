package id.onklas.app.pages.ppob.air

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import id.onklas.app.databinding.ListPdamPageBinding
import id.onklas.app.databinding.PdamItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.PageDialogFragment

class ListPdamPage : PageDialogFragment() {

    private lateinit var binding: ListPdamPageBinding
    private val viewmodel by activityViewModels<AirViewModel> { component.airVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ListPdamPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        lifecycleScope.launchWhenCreated { viewmodel.loadPdam() }

        binding.viewmodel = viewmodel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.toolbar.setNavigationOnClickListener { dismiss() }

        binding.rvPdam.adapter = adapter
        binding.rvPdam.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                OrientationHelper.VERTICAL
            )
        )

        viewmodel.listPdam.observe(viewLifecycleOwner) {
            adapter.submitList(it.toMutableList())
        }

        binding.inputSearch.doAfterTextChanged {
            handler.removeCallbacks(searchPdam)
            handler.postDelayed(searchPdam, 100)
        }

        binding.executePendingBindings()
    }

    private val handler by lazy { Handler() }
    private val searchPdam by lazy {
        Runnable {
            viewmodel.searchPdam(binding.inputSearch.text.toString())
        }
    }

    private val adapter by lazy {
        object : ListAdapter<PdamItem, Viewholder>(object : DiffUtil.ItemCallback<PdamItem>() {
            override fun areItemsTheSame(oldItem: PdamItem, newItem: PdamItem): Boolean =
                oldItem.product_id == newItem.product_id

            override fun areContentsTheSame(oldItem: PdamItem, newItem: PdamItem): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(getItem(position))

        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: PdamItemBinding = PdamItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PdamItem) {
            binding.labelTop.text = item.name
            binding.root.setOnClickListener { select(item) }
            binding.labelTop.setOnClickListener { select(item) }
            binding.imageTop.setOnClickListener { select(item) }
        }

        private fun select(item: PdamItem) {
            viewmodel.inputWilayah.postValue(item)
            dismiss()
        }
    }

}