package id.onklas.app.pages.sekolah.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.CheckoutShippingPageBinding
import id.onklas.app.databinding.ShippingNameItemBinding
import id.onklas.app.databinding.ShippingTypeItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.PageDialogFragment

class CheckoutShippingPage(val onDismiss: () -> Unit) : PageDialogFragment() {

    private lateinit var binding: CheckoutShippingPageBinding
    private val viewModel by activityViewModels<CheckoutStoreViewModel> { component.checkoutStoreVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        CheckoutShippingPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { dismiss() }

        binding.rvShip.adapter = adapter

        viewModel.shipItemLive.observe(viewLifecycleOwner, Observer(adapter::submitList))

        binding.btnConfirm.setOnClickListener {
            val selectedValue = viewModel.shipItemLive.value?.firstOrNull { !it.isParent && it.selected }
            viewModel.selectedShip.postValue(selectedValue)
            viewModel.total.postValue((viewModel.subtotal.value ?: 0) + (selectedValue?.cost ?: 0))
            onDismiss.invoke()
            dismiss()
        }
    }

    private abstract class Viewholder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: ListShipData)
        abstract fun update(item: ListShipData)
    }

    private inner class NameVH(
        parent: ViewGroup,
        val binding: ShippingNameItemBinding = ShippingNameItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : Viewholder(binding.root) {
        override fun bind(item: ListShipData) {
            binding.item = item
            binding.root.setOnClickListener {
                val currentList = (viewModel.shipItemLive.value ?: mutableListOf()).toMutableList()
                val newList = mutableListOf<ListShipData>()
                currentList.forEachIndexed { index, it ->
                    val newItem = it.copy()
                    if (newItem.isParent) newItem.selected = false
                    if (index == bindingAdapterPosition) newItem.selected = true
                    newList.add(newItem)
                }
                viewModel.shipItemLive.postValue(newList)
            }
            update(item)
        }

        override fun update(item: ListShipData) {
            binding.selected = item.selected
            binding.executePendingBindings()
        }
    }

    private inner class ShippingVH(
        parent: ViewGroup,
        val binding: ShippingTypeItemBinding = ShippingTypeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : Viewholder(binding.root) {
        override fun bind(item: ListShipData) {
            binding.item = item
            binding.islast = item.isLast
            binding.stringUtil = viewModel.stringUtil

            binding.radioSelected.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    val currentList = (viewModel.shipItemLive.value ?: mutableListOf()).toMutableList()
                    val newList = mutableListOf<ListShipData>()
                    currentList.forEachIndexed { index, it ->
                        val newItem =
                            it.copy(selected = index == bindingAdapterPosition || (it.isParent && it.courier_name == item.courier_name))
                        newList.add(newItem)
                    }
                    viewModel.shipItemLive.postValue(newList)
                }
            }

            update(item)
        }

        override fun update(item: ListShipData) {
            binding.selected = item.selected
            binding.executePendingBindings()
        }
    }

    private val adapter by lazy {
        object :
            ListAdapter<ListShipData, Viewholder>(object : DiffUtil.ItemCallback<ListShipData>() {
                override fun areItemsTheSame(
                    oldItem: ListShipData,
                    newItem: ListShipData
                ): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: ListShipData,
                    newItem: ListShipData
                ): Boolean =
                    oldItem == newItem

                override fun getChangePayload(oldItem: ListShipData, newItem: ListShipData): Any =
                    newItem
            }) {

            override fun getItemViewType(position: Int): Int =
                if (getItem(position).isParent) 100 else 200

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                if (viewType == 100) NameVH(parent) else ShippingVH(parent)

            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(getItem(position))

            override fun onBindViewHolder(
                holder: Viewholder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                if (payloads.isNotEmpty())
                    holder.update(payloads.first() as ListShipData)
                else onBindViewHolder(holder, position)
            }
        }
    }
}