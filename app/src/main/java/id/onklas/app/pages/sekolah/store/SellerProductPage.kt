package id.onklas.app.pages.sekolah.store

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.onklas.app.GlideApp
import id.onklas.app.databinding.MerchantProductItemFullBinding
import id.onklas.app.databinding.SellerProductPageBinding
import id.onklas.app.databinding.SellerProfileFilterBinding
import id.onklas.app.databinding.StoreFilterItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch
import timber.log.Timber


class SellerProductPage : Fragment() {
    private lateinit var binding: SellerProductPageBinding
    private val viewmodel by activityViewModels<StoreVm> { component.storeVmFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }

    private var listFilterUrutan = ArrayList<StoreFilterModel>()
    private var listFilterTampilan = ArrayList<StoreFilterModel>()

    private var filterUrutanAdapter: FilterUrutanAdapter? = null
    private var filterTampilanAdapter: FilterTampilanAdapter? = null

    private var firstLoad = true
//    private var merchantId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = SellerProductPageBinding.inflate(inflater, container, false)
            .also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        viewmodel.Merchant.observe(viewLifecycleOwner, Observer {
            initData(it.id)
            lifecycleScope.launch {
                viewmodel.fetchproductMerchant(0, it.id, "all")
            }

        })


        binding.swipeRefresh.setOnRefreshListener {
            viewmodel.Merchant.observe(viewLifecycleOwner, Observer {
                initData(it.id)
                lifecycleScope.launch {
                    viewmodel.fetchproductMerchant(0, it.id, "all")
                }

            })

        }

        viewmodel.LoadingShow.observe(viewLifecycleOwner, Observer {
            binding.swipeRefresh.isRefreshing = it
        })

        binding.rvProduct.apply {
            layoutManager = GridLayoutManager(context, 2, )
            adapter = ProductAdapter
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                mMessageReceiver,
                IntentFilter("OnklasBroadcast")
        )
        listFilterUrutan.addAll(viewmodel.sellerFilterUrutan)
        listFilterTampilan.addAll(viewmodel.sellerFilterTampilan)

        filterUrutanAdapter = FilterUrutanAdapter(listFilterUrutan)
        filterTampilanAdapter = FilterTampilanAdapter(listFilterTampilan)
    }

    private var currLiveData: LiveData<PagedList<ProductMerchantTable>>? = null
    private fun initData(merchant_id: Int) {
        firstLoad = true
        binding.swipeRefresh.isRefreshing = true
        viewmodel.lastProduct = -1
        viewmodel.hasNextProduct = true
        currLiveData?.removeObservers(this)
        currLiveData = viewmodel.listProductMerchant(merchant_id, "all").apply {
            observe(viewLifecycleOwner, listObserver)
        }

    }

    private val listObserver by lazy {
        Observer<PagedList<ProductMerchantTable>> {
            Timber.e("loaded data: ${it.size}")
            ProductAdapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager = (binding.rvProduct.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvProduct.scrollToPosition(position)
                    }
                } else {
                    binding.rvProduct.scrollToPosition(0)
                    firstLoad = false
                }

            }
        }
    }


    private val ProductAdapter by lazy {
        object : PagingAdapter<ProductMerchantTable, ProductMerchantVh>(object : DiffUtil.ItemCallback<ProductMerchantTable>() {
            override fun areItemsTheSame(oldItem: ProductMerchantTable, newItem: ProductMerchantTable): Boolean =
                    oldItem == newItem

            override fun areContentsTheSame(oldItem: ProductMerchantTable, newItem: ProductMerchantTable): Boolean =
                    oldItem == newItem
        }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): ProductMerchantVh =
                    ProductMerchantVh(parent)

            override fun bindItemViewholder(holder: ProductMerchantVh, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(holder: ProductMerchantVh, position: Int, payloads: MutableList<Any>) {
                getItem(position)?.let { holder.bind(it) }
            }


        }
    }

    private inner class ProductMerchantVh(
            parent: ViewGroup,
            val binding: MerchantProductItemFullBinding = MerchantProductItemFullBinding.inflate(
                    LayoutInflater.from(
                            parent.context
                    ), parent, false
            )
    ) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductMerchantTable) {
            binding.productImg.clipToOutline = true
            binding.stokKosongImg.clipToOutline = true
            binding.item = item
            binding.stringUtil = viewmodel.stringUtil

            if (item.stock == 0) {
                binding.stokKosongLay.visibility = View.VISIBLE
            } else {
                binding.stokKosongLay.visibility = View.GONE
            }

            binding.lblHarga.text = "Rp. " + viewmodel.stringUtil.formatCurrency2(item.price)

            binding.root.setOnClickListener {
                DetailProduk.open(requireActivity() as Privatepage, item.product_id)
            }
        }
    }

    var mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            // Get extra data included in the Intent
            val data = intent.getStringExtra("FilterSellerProduct")
            if (data == "FilterSellerProduct") {
                if (isAdded) {
                    bottomSeetFilter(layoutInflater)
                }
            }
        }
    }

    fun bottomSeetFilter(layoutInflater: LayoutInflater) {
        val dialog = BottomSheetDialog(requireContext())
        val bindingDialog by lazy { SellerProfileFilterBinding.inflate(layoutInflater) }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = bindingDialog.root

        bindingDialog.rvUrutanProduk.apply {
            layoutManager = LinearLayoutManager(view.context, RecyclerView.VERTICAL, false)
            adapter = filterUrutanAdapter
        }
        bindingDialog.rvTampilan.apply {
            layoutManager = LinearLayoutManager(view.context, RecyclerView.VERTICAL, false)
            adapter = filterTampilanAdapter
        }
        bindingDialog.actionExit.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.actionReset.setOnClickListener {
            listFilterTampilan.clear()
            listFilterUrutan.clear()

            listFilterUrutan.addAll(viewmodel.sellerFilterUrutan)
            listFilterTampilan.addAll(viewmodel.sellerFilterTampilan)

            filterTampilanAdapter?.notifyDataSetChanged()
            filterUrutanAdapter?.notifyDataSetChanged()
        }



        bindingDialog.actionTampilkanProduk.setOnClickListener {
        }

        dialog.setContentView(view)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val bottomSheetDialog = dialog
        val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        parentLayout?.let { it ->
            val behaviour = BottomSheetBehavior.from(it)
            setupFullHeight(it)
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    private inner class FilterUrutanAdapter(
            val list: List<StoreFilterModel>
    ) :
            RecyclerView.Adapter<UrutanViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrutanViewHolder =
                UrutanViewHolder(parent)

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: UrutanViewHolder, position: Int) =
                holder.bind(position, list[position])


    }

    private inner class UrutanViewHolder(
            parent: ViewGroup,
            val bindingitem: StoreFilterItemBinding = StoreFilterItemBinding.inflate(
                    LayoutInflater.from(
                            parent.context
                    ), parent, false
            )
    ) :
            RecyclerView.ViewHolder(bindingitem.root) {
        fun bind(position: Int, item: StoreFilterModel) {
            bindingitem.filterName.text = item.name
            bindingitem.rdFilter.isChecked = item.is_selected

            bindingitem.rdFilter.setOnClickListener {
                //reset value
                if (listFilterUrutan.find { it.is_selected } != null) {
                    val Item = listFilterUrutan.find { it.is_selected }
                    val posisiItem = listFilterUrutan.indexOf(Item)
                    val olddata = listFilterUrutan[posisiItem]
                    listFilterUrutan.set(posisiItem, StoreFilterModel(
                            olddata.id,
                            olddata.name,
                            olddata.filter_code,
                            false
                    ))
                    filterUrutanAdapter?.notifyItemChanged(posisiItem)
                }

                //add new value
                listFilterUrutan.set(position, StoreFilterModel(
                        item.id,
                        item.name,
                        item.filter_code,
                        true
                ))
                viewmodel.selectedFilter.postValue(item.filter_code)
                filterUrutanAdapter?.notifyItemChanged(position)

            }
        }
    }

    private inner class FilterTampilanAdapter(
            val list: List<StoreFilterModel>
    ) :
            RecyclerView.Adapter<TampilanViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TampilanViewHolder =
                TampilanViewHolder(parent)

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: TampilanViewHolder, position: Int) =
                holder.bind(position, list[position])


    }

    private inner class TampilanViewHolder(
            parent: ViewGroup,
            val bindingitem: StoreFilterItemBinding = StoreFilterItemBinding.inflate(
                    LayoutInflater.from(
                            parent.context
                    ), parent, false
            )
    ) :
            RecyclerView.ViewHolder(bindingitem.root) {
        fun bind(position: Int, item: StoreFilterModel) {
            bindingitem.filterName.text = item.name
            bindingitem.rdFilter.isChecked = item.is_selected

            bindingitem.rdFilter.setOnClickListener {
                //reset value
                if (listFilterTampilan.find { it.is_selected } != null) {
                    val Item = listFilterTampilan.find { it.is_selected }
                    val posisiItem = listFilterTampilan.indexOf(Item)
                    val olddata = listFilterTampilan[posisiItem]
                    listFilterTampilan.set(posisiItem, StoreFilterModel(
                            olddata.id,
                            olddata.name,
                            olddata.filter_code,
                            false
                    ))
                    filterTampilanAdapter?.notifyItemChanged(posisiItem)
                }

                //add new value
                listFilterTampilan.set(position, StoreFilterModel(
                        item.id,
                        item.name,
                        item.filter_code,
                        true
                ))
                viewmodel.selectedFilter.postValue(item.filter_code)
                filterTampilanAdapter?.notifyItemChanged(position)

            }
        }
    }


}