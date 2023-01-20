package id.onklas.app.pages.entrepreneurs.myMerchant.IncomingOrder.procesedOrderPage

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.*
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.OrderVM
import id.onklas.app.pages.entrepreneurs.TransactionWithProduct
import id.onklas.app.pages.entrepreneurs.TransaksiProductTable
import id.onklas.app.pages.entrepreneurs.OrderDetailPage
import id.onklas.app.pages.entrepreneurs.OrderHistoryDeliveryPage
import id.onklas.app.pages.entrepreneurs.myMerchant.addProduct.AddProductPage
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch
import timber.log.Timber


class ProcessedProductOrderPage : Fragment() {

    private lateinit var binding: ProcessedProductOrderPageBinding
    private val viewModel by activityViewModels<OrderVM> { component.entrepreneursOrderFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }

    private var firstLoad = true

    private val colorPrimary by lazy {
        ResourcesCompat.getColor(
                resources,
                R.color.colorPrimary,
                null
        )
    }
    private val colorGray by lazy {
        ResourcesCompat.getColor(
                resources,
                R.color.bg_gray,
                null
        )
    }
    private val colorWhite by lazy {
        ResourcesCompat.getColor(
                resources,
                R.color.white,
                null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = ProcessedProductOrderPageBinding.inflate(inflater, container, false)
        .also { binding = it }.root


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

    private fun initData(date:String=""){
        viewModel.isEmptyOrder.postValue(false)
        lifecycleScope.launch { viewModel.fetchListSellerOrder(0, "processed", "seller", date) }
        initTransaksi(date)
    }

    private var currLiveData: LiveData<PagedList<TransactionWithProduct>>? = null
    private fun initTransaksi(dateFilter: String) {
        firstLoad = true
        viewModel.lastProduct = -1
        viewModel.hasNextProduct = true
        currLiveData?.removeObserver(listObserver)
        currLiveData = viewModel.listOrder("processed", "seller", dateFilter).apply {
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
                    "Detail Order",
                    "seller"
                )
            }

            //pesanan diproses
            // APPROVED = Pesanan diproses penjual (tampil menunggu konfirmasi penjual)
            // SENT = Pesanan dikirim penjual (tampil menunggu pengiriman)
            // DELIVERED = Pesanan terkirim (menurut pihak ekspedisi) (tampil laccak terima)


            if (item.order.status == "APPROVED") {
                binding.lacakReview.root.visibility = View.GONE
                binding.kirimSekarang.root.visibility = View.VISIBLE
                binding.kirimSekarang.btnKirimSekarang.setOnClickListener {
                    button2Dialog(
                        item.order.status,
                        item.order.id,
                        "Konfirmasi Pengiriman",
                        "Apa kamu sudah mengantar produk ke lokasi pengiriman ?",
                        "Sudah",
                        "Belum",
                        item.order.courier_name,
                        item.order.destination_address
                    )
                }
            } else if (item.order.status == "SENT") {
                binding.kirimSekarang.root.visibility = View.GONE
                binding.lacakReview.root.visibility = View.VISIBLE
                binding.lacakReview.btnReview.visibility = View.GONE
                binding.lacakReview.btnLacak.setOnClickListener {
                    OrderHistoryDeliveryPage.open(requireActivity() as Privatepage, item.order.id)
                }
            } else {//DELIVERED
                binding.kirimSekarang.root.visibility = View.GONE
                binding.lacakReview.root.visibility = View.VISIBLE
                binding.lacakReview.btnTerima.visibility = View.GONE
                binding.lacakReview.btnReview.apply {
                    setTextColor(colorWhite)
                }
                binding.lacakReview.btnReview.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#C2C2C2"))

                binding.lacakReview.btnLacak.setOnClickListener {
                    OrderHistoryDeliveryPage.open(requireActivity() as Privatepage, item.order.id)
                }
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

    private fun button2Dialog(
        status: String,
        transaksiID: Int,
        title: String,
        desc: String,
        btnAction: String,
        btnDone: String,
        courier: String,
        address: String
    ) {
        val bindingDialog = EntrepreneursCostomDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(bindingDialog.root)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }


        if (status == "APPROVED") {
            bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_pay_success) }
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
            if (status == "APPROVED") {
                addResiDialog(transaksiID,courier,address)
//                lifecycleScope.launch {
//                    viewModel.acceptTransaksi(transaksiID)
//                }
            } else {
//                lifecycleScope.launch {
//                    viewModel.acceptTransaksi(transaksiID)
//                }
            }
        }
        bindingDialog.button2.btnDone.setOnClickListener {
            dialog.dismiss()
        }


    }

    private fun addResiDialog(transaksiID: Int,courier:String,address:String) {
        val bindingDialog = EntrepreneursDialogMasukanResiBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(bindingDialog.root)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

        bindingDialog.courier = courier
        bindingDialog.address = address
        bindingDialog.actionKonfirmasi.setOnClickListener {
            if (!bindingDialog.inResi.text.isNullOrEmpty()) {
                // launch
                lifecycleScope.launch {
                    viewModel.inputResi(transaksiID,bindingDialog.inResi.text.toString(),courier)
                }
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Nomor resi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
        bindingDialog.actionBatal.setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun button1Dialog(
        img: String,
        title: String,
        desc: String,
        btnDone: String, ) {
        val bindingDialog = EntrepreneursCostomDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(bindingDialog.root)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

        if (img == "danger") {
            bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_danger) }
        } else {
            bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_pay_success) }
        }

        bindingDialog.txtTitle.text = title
        bindingDialog.txtDesc.text = desc

        bindingDialog.button1.root.visibility = View.VISIBLE
        bindingDialog.button1.btnDone.text = btnDone
        bindingDialog.button1.btnDone.setOnClickListener {
            findNavController().navigate(R.id.action_global_processed_product)
            initData()
            dialog.dismiss()
        }

    }
}