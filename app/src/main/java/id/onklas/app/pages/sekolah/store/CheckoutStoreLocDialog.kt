package id.onklas.app.pages.sekolah.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.CheckoutStoreLocDialogBinding
import id.onklas.app.databinding.TextItemBinding
import id.onklas.app.pages.PageDialogFragment
import id.onklas.app.utils.PagingAdapter

class CheckoutStoreLocDialog<T : Any>(
    val type: Class<T>,
    val data: LiveData<PagedList<T>>,
    val onSelect: (T) -> Unit,
    private val head: String = ""
) :
    PageDialogFragment() {

    private lateinit var binding: CheckoutStoreLocDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        CheckoutStoreLocDialogBinding.inflate(inflater, container, false).apply {
            binding = this

            header = head
            toolbar.title = when (type) {
                Province::class.java -> "Pilih Provinsi"
                City::class.java -> "Pilih Kota"
                else -> "Pilih Kecamatan"
            }
            toolbar.setNavigationOnClickListener { dismiss() }
        }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rvItems.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                OrientationHelper.VERTICAL
            )
        )
        binding.rvItems.adapter = adapter

        data.observe(viewLifecycleOwner, Observer(adapter::submitList))
    }

    private inner class ProvinceVH(parent: ViewGroup) : ViewHolder(parent) {
        override fun bindItem(item: Any) {
            val province = item as Province
            bind(province.id to province.name, item as T)
        }
    }

    private inner class CityVH(parent: ViewGroup) : ViewHolder(parent) {
        override fun bindItem(item: Any) {
            val city = item as City
            bind(city.id to city.name, item as T)
        }
    }

    private inner class DistrictVH(parent: ViewGroup) : ViewHolder(parent) {
        override fun bindItem(item: Any) {
            val district = item as District
            bind(district.id to district.name, item as T)
        }
    }

    private abstract inner class ViewHolder(
        parent: ViewGroup,
        val binding: TextItemBinding = TextItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        abstract fun bindItem(item: Any)

        fun bind(pair: Pair<String, String>, item: T) {
            binding.textview.text = pair.second
            binding.textview.setOnClickListener {
                onSelect(item)
                dismiss()
            }
            binding.executePendingBindings()
        }
    }

    private val adapter by lazy {
        object : PagingAdapter<T, ViewHolder>(object :
            DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem

            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = false
        }) {
            override fun createItemViewholder(
                parent: ViewGroup,
                viewType: Int
            ): CheckoutStoreLocDialog<T>.ViewHolder {
                return when (type) {
                    Province::class.java -> ProvinceVH(parent)
                    City::class.java -> CityVH(parent)
                    else -> DistrictVH(parent)
                }
            }

            override fun bindItemViewholder(
                holder: CheckoutStoreLocDialog<T>.ViewHolder,
                position: Int
            ) {
                getItem(position)?.let { holder.bindItem(it) }
            }

            override fun bindItemViewholder(
                holder: CheckoutStoreLocDialog<T>.ViewHolder,
                position: Int,
                payloads: MutableList<Any>
            ) = bindItemViewholder(holder, position)
        }
    }
}