package id.onklas.app.pages.entrepreneurs.myMerchant.IncomingOrder.incomingOrderPage

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.EntrepreneursCostomDialogBinding
import id.onklas.app.databinding.IncomeProductItemBinding
import id.onklas.app.databinding.IncomingOrderProductItem2Binding
import id.onklas.app.databinding.IncomingProductPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.entrepreneurs.OrderVM
import id.onklas.app.pages.entrepreneurs.TransactionWithProduct
import id.onklas.app.pages.entrepreneurs.TransaksiProductTable
import id.onklas.app.pages.entrepreneurs.OrderDetailPage
import id.onklas.app.pages.entrepreneurs.myMerchant.addProduct.AddProductPage
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch
import timber.log.Timber


class IncomingProductPage : Fragment() {

    private lateinit var binding: IncomingProductPageBinding
    private val viewModel by activityViewModels<OrderVM> { component.entrepreneursOrderFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }

    private var firstLoad = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        IncomingProductPageBinding.inflate(inflater, container, false).also { binding = it }.root


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.kosongLay.imgEmp.apply { setImageDrawable(resources.getDrawable(R.drawable.img_emp_incoming_order)) }
        binding.kosongLay.txtTitle.text = "Belum Ada Orderan"
        binding.kosongLay.txtSubtitle.text =
            "Upload produk dan berilah deskripsi yang lengkap untuk menarik pembeli"
        binding.kosongLay.actionBtn.text = "Upload Produk"
        binding.kosongLay.actionBtn.setOnClickListener {
            startActivity(Intent(context, AddProductPage::class.java))
        }

        binding.rvProduct.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = TransaksiAdapter
        }
        initData()

        viewModel.LoadingShow.observe(viewLifecycleOwner, Observer {
            binding.swipeRefresh.isRefreshing = it
        })


        binding.swipeRefresh.setOnRefreshListener {
            initData()
        }

        viewModel.dateFilterLiveData.observe(viewLifecycleOwner, Observer {
            initData(it)
        })


    }

    private var currLiveData: LiveData<PagedList<TransactionWithProduct>>? = null
    private fun initIncomingTransaksi(dateFilter: String) {
        Timber.e("init transaksi ------------ $dateFilter")
        firstLoad = true
        viewModel.lastProduct = -1
        viewModel.hasNextProduct = true
        currLiveData?.removeObserver(listObserver)
        currLiveData = viewModel.listOrder("incoming", "seller", dateFilter).apply {
            observe(viewLifecycleOwner, listObserver)
        }

    }

    private val listObserver by lazy {
        Observer<PagedList<TransactionWithProduct>> {
            Timber.e("loaded data: ${it.size}")
            TransaksiAdapter.submitList(it) {
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
            binding.item = item.order
            binding.stringUtil = viewModel.stringUtil
            binding.dateUtil = viewModel.dateUtil
            binding.frontName = item.order.buyer_name

            val adapterProduct = ProductAdapter(listOf(item.product.get(0)))
            binding.rvProduct.apply {
                layoutManager =
                    LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
                adapter = adapterProduct
            }
            if (item.product.size>1) {
                binding.txtCountProduct.text = "+" + (item.product.size - 1) + " produk lainya"
            }else{
                binding.txtCountProduct.visibility = View.GONE
            }

            binding.actionDetail.setOnClickListener {
                OrderDetailPage.open(
                    requireActivity() as AppCompatActivity,
                    item.order.id,
                    "Detail Orderan Masuk",
                    "seller"
                )
            }

//            pesanan masuk -> PAID


            if (item.order.status == "PAID") {
                binding.tolakProses.root.visibility = View.VISIBLE
            }


            binding.tolakProses.btnProses.setOnClickListener {
                viewModel.buyerName.postValue(item.order.buyer_name)
                button2Dialog(
                    "proses",
                    item.order.id,
                    "Terima Orderan",
                    "Terima orderan sekarang juga",
                    "Terima Orderan",
                    "Batalkan",
                )
            }

            binding.tolakProses.btnTolak.setOnClickListener {
                viewModel.buyerName.postValue(item.order.buyer_name)
                button2Dialog(
                    "tolak",
                    item.order.id,
                    "Tolak Orderan",
                    "Orderan yang ditolak berpotensi mengecewakan pembeli",
                    "Tolak Orderan",
                    "Batalkan",
                )
            }


        }
    }


    private fun initData(date:String=""){
        viewModel.isEmptyOrder.postValue(false)
        lifecycleScope.launch { viewModel.fetchListSellerOrder(0, "incoming", "seller", date) }
        initIncomingTransaksi(date)
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

    private fun button2Dialog(
        status: String,
        transaksiID: Int,
        title: String,
        desc: String,
        btnAction: String,
        btnDone: String,
    ) {
        val bindingDialog = EntrepreneursCostomDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(bindingDialog.root)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }


        if (status == "tolak") {
            bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_danger) }
        } else {
            bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_pay_success) }
        }

        bindingDialog.txtTitle.text = title
        bindingDialog.txtDesc.text = desc
        bindingDialog.button2.root.visibility = View.VISIBLE
        bindingDialog.button2.btnAction.text = btnAction
        bindingDialog.button2.btnDone.text = btnDone
        bindingDialog.button2.btnAction.setOnClickListener {
            dialog.dismiss()
            if (status == "tolak") {
                lifecycleScope.launch {
                    viewModel.rejectTransaksi(transaksiID)
                }
            } else {
                lifecycleScope.launch {
                    viewModel.acceptTransaksi(transaksiID)
                }
            }
        }
        bindingDialog.button2.btnDone.setOnClickListener {
            dialog.dismiss()
        }


    }


}