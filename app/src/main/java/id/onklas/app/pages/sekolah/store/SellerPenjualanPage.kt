package id.onklas.app.pages.sekolah.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.MerchantProductItem1Binding
import id.onklas.app.databinding.SellerPenjualanPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.PagingAdapter
import timber.log.Timber


class SellerPenjualanPage : Fragment() {

    private lateinit var binding: SellerPenjualanPageBinding
    private val viewmodel by activityViewModels<StoreVm> { component.storeVmFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }

    private var firstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = SellerPenjualanPageBinding.inflate(inflater, container, false)
            .also { binding = it }.root


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewmodel.Merchant.observe(viewLifecycleOwner, Observer {
            binding.txtJumlahReviewer.text = it.amountOfReviewer.toString() + " review"
            binding.txtRating.text = it.salesRating.toString()
            binding.txtTotalTerjual.text = it.successTransactions.toString()
            initData(it.id)
        })

        viewmodel.LoadingShow.observe(viewLifecycleOwner, Observer {
            binding.swipeRefresh.isRefreshing = it
        })

        binding.rvProduct.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = ProductAdapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewmodel.Merchant.observe(viewLifecycleOwner, Observer {
                binding.txtJumlahReviewer.text = it.amountOfReviewer.toString() + " review"
                binding.txtRating.text = it.salesRating.toString()
                binding.txtTotalTerjual.text = it.successTransactions.toString()
                initData(it.id)
            })
        }

        binding.empLay.imgEmp.apply { setImageDrawable(resources.getDrawable(R.drawable.ic_belum_ada_penjualan)) }
        binding.empLay.txtTitle.text ="Belum Ada Penjualan"
        binding.empLay.txtSubtitle.text = "Yuk tambah produkmu dan mulai berjualan"
        binding.empLay.actionBtn.visibility = View.INVISIBLE



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
            binding.isProdukTerlarisEmpty = it.isEmpty()
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
        object : PagingAdapter<ProductMerchantTable, GoodieDetailCardViewHolder>(object : DiffUtil.ItemCallback<ProductMerchantTable>() {
            override fun areItemsTheSame(oldItem: ProductMerchantTable, newItem: ProductMerchantTable): Boolean =
                    oldItem == newItem

            override fun areContentsTheSame(oldItem: ProductMerchantTable, newItem: ProductMerchantTable): Boolean =
                    oldItem == newItem
        }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): GoodieDetailCardViewHolder =
                    GoodieDetailCardViewHolder(parent)


            override fun bindItemViewholder(holder: GoodieDetailCardViewHolder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(holder: GoodieDetailCardViewHolder, position: Int, payloads: MutableList<Any>) {
                getItem(position)?.let { holder.bind(it) }
            }

        }
    }

    private inner class GoodieDetailCardViewHolder(
            parent: ViewGroup,
            val binding: MerchantProductItem1Binding = MerchantProductItem1Binding.inflate(
                    LayoutInflater.from(
                            parent.context
                    ), parent, false
            )
    ) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductMerchantTable) {
            binding.productImg.clipToOutline = true
            binding.item = item
            binding.stringUtil = viewmodel.stringUtil


            binding.lblHarga.text = "Rp. " + viewmodel.stringUtil.formatCurrency2(item.price)

            binding.root.setOnClickListener {
                DetailProduk.open(requireContext() as Privatepage, item.product_id)
            }

        }
    }


}