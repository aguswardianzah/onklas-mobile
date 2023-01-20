package id.onklas.app.pages.entrepreneurs.pembelian.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.IncomeProductItemBinding
import id.onklas.app.databinding.IncomingOrderProductItem2Binding
import id.onklas.app.databinding.WaitingReviewBuyerBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.*
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch
import timber.log.Timber

class WaitingReviewBuyer : Privatepage() {
    private val binding by lazy { WaitingReviewBuyerBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewModel by viewModels<PembelianVM> { component.entrepreneursPembelianFactory }

    private var firstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            binding.toolbar.inflateMenu(R.menu.menu_calender)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Menunggu Review"
            binding.toolbar.title = "Menunggu Review"
        }

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.empLay.imgEmp.apply { setImageDrawable(resources.getDrawable(R.drawable.img_emp_waiting_review)) }
        binding.empLay.txtTitle.text = "Belum Ada Review"
        binding.empLay.txtSubtitle.text =
            "Lakukan pemberian review kepada produk dan pembeli atas pembelian yang mereka lakukan"



        binding.rvTransaksi.apply {
            layoutManager =
                LinearLayoutManager(this@WaitingReviewBuyer, RecyclerView.VERTICAL, false)
            adapter = TransaksiAdapter
        }
        initData()

        viewModel.LoadingShow.observe(
            this,
            Observer { binding.swipeRefresh.isRefreshing = it })


        binding.swipeRefresh.setOnRefreshListener {
            initData()
        }

        viewModel.dateFilterLiveData.observe(this, Observer {
            initData(it)
        })


    }

    private fun initData(date: String = "") {
        viewModel.isEmptyOrder.postValue(false)
        lifecycleScope.launch { viewModel.fetchListSellerOrder(0, "review",  date,"buyer") }
        initTransaksi(date)
    }

    private var currLiveData: LiveData<PagedList<TransactionWithProduct>>? = null
    private fun initTransaksi(dateFilter: String) {
        firstLoad = true
        viewModel.lastProduct = -1
        viewModel.hasNextProduct = true
        currLiveData?.removeObserver(listObserver)
        currLiveData = viewModel.listOrder("review", "buyer", dateFilter).apply {
            observe(this@WaitingReviewBuyer, listObserver)
        }
    }

    private val listObserver by lazy {
        Observer<PagedList<TransactionWithProduct>> {
            Timber.e("loaded data: ${it.size}")
            TransaksiAdapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager = (binding.rvTransaksi.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvTransaksi.scrollToPosition(position)
                    }
                } else {
                    binding.rvTransaksi.scrollToPosition(0)
                    firstLoad = false
                }

            }
        }
    }

    private val TransaksiAdapter by lazy {
        object : PagingAdapter<TransactionWithProduct, TransaksiDateViewholder>(object :
            DiffUtil.ItemCallback<TransactionWithProduct>() {
            override fun areItemsTheSame(
                oldItem: TransactionWithProduct,
                newItem: TransactionWithProduct
            ): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(
                oldItem: TransactionWithProduct,
                newItem: TransactionWithProduct
            ): Boolean =
                oldItem == newItem
        }) {
            override fun createItemViewholder(
                parent: ViewGroup,
                viewType: Int
            ): TransaksiDateViewholder =
                TransaksiDateViewholder(parent)


            override fun bindItemViewholder(holder: TransaksiDateViewholder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: TransaksiDateViewholder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                getItem(position)?.let { holder.bind(it) }
            }


        }
    }

    private inner class TransaksiDateViewholder(
        parent: ViewGroup,
        val binding: IncomeProductItemBinding = IncomeProductItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionWithProduct) {
            Timber.e("date format 2 ${item.order.date_formater2}")
            Timber.e("date format 1 ${item.order.date_formater}")
            binding.item = item.order
            binding.stringUtil = viewModel.stringUtil
            binding.dateUtil = viewModel.dateUtil
            binding.frontName = item.order.seller_name


            val adapterProduct = ProductAdapter(listOf(item.product.get(0)))
            binding.txtCountProduct.text = "+" + item.product.size + " produk lainya"
            binding.rvProduct.apply {
                layoutManager =
                    LinearLayoutManager(this@WaitingReviewBuyer, RecyclerView.VERTICAL, false)
                adapter = adapterProduct
            }
            binding.actionDetail.setOnClickListener {
                Timber.e("onreview click true------")
                OrderDetailPage.open(
                    this@WaitingReviewBuyer,
                    item.order.id,
                    "Review Produk",
                    "buyerReview",
                    onReview = true,
                    default = true
                )
            }

            // DECLINED|ACCEPTED if( page == done

            if (item.order.status == "ACCEPTED") {
                binding.pembelianSelesai.root.visibility = View.VISIBLE
                binding.pembelianSelesai.btnDecline.text = "Penjualan Selesai"
                binding.pembelianDitolak.root.visibility = View.GONE
            } else {
                binding.pembelianSelesai.root.visibility = View.GONE
                binding.pembelianDitolak.root.visibility = View.VISIBLE
                binding.pembelianDitolak.btnKonfirmasiSelesai.text = "Penjualan Ditolak"
            }

        }
    }

    private inner class ProductAdapter(val data: List<TransaksiProductTable>) :
        RecyclerView.Adapter<ProductViewholder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewholder =
            ProductViewholder(parent)

        override fun onBindViewHolder(holder: ProductViewholder, position: Int) =
            holder.bind(data[position])

        override fun getItemCount(): Int = data.size
    }

    private inner class ProductViewholder(
        parent: ViewGroup,
        val binding: IncomingOrderProductItem2Binding = IncomingOrderProductItem2Binding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransaksiProductTable) {
            binding.item = item
            binding.imgProduct.clipToOutline = true
            binding.txtTotal.text = "Rp." + viewModel.stringUtil.formatCurrency2(item.goody_price)
            binding.imgDivider.visibility = View.GONE
        }

    }
}