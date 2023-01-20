package id.onklas.app.pages.entrepreneurs

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.*
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.myMerchant.Review.AddReviewPage
import id.onklas.app.pages.entrepreneurs.pembelian.review.AddReviewBuyerPage
import id.onklas.app.pages.sekolah.store.DetailProduk
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber


class OrderDetailPage : Privatepage() {

    companion object {
        fun open(
            activity: AppCompatActivity,
            transaksiId: Int,
            title: String,
            role: String,
            default: Boolean = false,
            onReview: Boolean = false
        ) {
            val i = Intent(activity, OrderDetailPage::class.java)
                .putExtra("transaksiId", transaksiId)
                .putExtra("title", title)
                .putExtra("role", role)// role -> seller/ buyer
                .putExtra("default", default)// tampilan defaut tanpa button dan top info income page
                .putExtra("onReview", onReview) //  untuk review
            activity.startActivityForResult(i, 310)
        }
    }

    private val binding by lazy { OrderDetailPageBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodel by viewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val viewModelOrder by viewModels<OrderVM> { component.entrepreneursOrderFactory }
    private val viewModelPembelian by viewModels<PembelianVM> { component.entrepreneursPembelianFactory }
    private val navController by lazy { findNavController(R.id.page_container) }
    private var titleIntent = ""
    private var transaksiId = 0
    private var role = ""
    private var default = false
    private var onReview = false
    private var orderStatus = ""
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
        setContentView(binding.root)
        titleIntent = intent.getStringExtra("title").toString()
        transaksiId = intent.getIntExtra("transaksiId", 0)
        role = intent.getStringExtra("role").toString()
        default = intent.getBooleanExtra("default", false)
        onReview = intent.getBooleanExtra("onReview", false)
        Timber.e("onreview------ $onReview")
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = titleIntent
            binding.toolbar.title = titleIntent
        }

        viewModelOrder.errorString.observe(this, Observer {
            toast(it)
        })

        viewmodel.loadingShow.observe(this, Observer {
            Timber.e("loading show $it")
            if (it) {
                loading()
            } else {
                dismissloading()
            }
        })
        viewModelOrder.LoadingShow.observe(this, Observer {
            Timber.e("loading show $it")
            if (it) {
                loading()
            } else {
                dismissloading()
            }
        })

        binding.rvProduk.apply {
            layoutManager = LinearLayoutManager(this@OrderDetailPage, RecyclerView.VERTICAL, false)
            adapter = ProductAdapter
        }


//        viewModelOrder.incActResponseBoolean.observe(this, Observer {
//            Timber.e("in action boolean $it")
//            if (it) {
        viewModelOrder.incActResponse.removeObservers(this)
        viewModelOrder.incActResponse.observe(this, Observer {
            Timber.e("in action status ${it.data.status}")
            orderStatus = it.data.status
            if (it.data.status == "APPROVED") {// diterima penjual
                button1Dialog(
                    "approve",
                    "Orderan Sudah Diterima",
                    "Orderan dari ${viewModelOrder.buyerName.value} sudah berhasil diterima",
                    "Oke, Terimakasih",
                )
            } else if (it.data.status == "DECLINED") { //  ditolak penjual
                button1Dialog(
                    "decline",
                    "Orderan Sudah Ditolak",
                    "Orderan dari ${viewModelOrder.buyerName.value} sudah berhasil ditolak",
                    "Oke, Terimakasih",
                )
            }
        })
        viewModelOrder.inpResiResponse.removeObservers(this)
        viewModelOrder.inpResiResponse.observe(this, Observer {
            Timber.e("inp resi response  $it")
            if (it.error.isEmpty()) {
                button1Dialog(
                    "success",
                    "Konfirmasi Pengiriman",
                    "Kami akan memberitahukan kepada pembeli bahwa sedang dalam pengiriman",
                    "Oke, Terimakasih",
                )
            }
        })
        binding.btnDetailLacak.setOnClickListener {
            OrderHistoryDeliveryPage.open(this, transaksiId)
        }

        viewModelPembelian.BuyerActResponse.observe(this, Observer {
            lifecycleScope.launch {
                viewmodel.loadDetail(
                    transaksiId,
                    role
                ) // refresh ketika buyer klik cancle pesanan atau teima pesanan
            }
        })

        viewmodel.errorString.observe(this, Observer(this::toast))
        viewModelPembelian.errorString.observe(this, Observer(this::toast))
        viewModelOrder.errorString.observe(this, Observer(this::toast))

        val data by lazy { MutableLiveData<DetailTransactionWithProduct>() }
        lifecycleScope.launchWhenCreated {
            Timber.e("transaksi sis---- $transaksiId")
            Timber.e("role----- $role")
            viewmodel.loadDetail(transaksiId, role).collectLatest {
                try {
                    ProductAdapter.submitList(it.product)
                    binding.item = it.detailOrder
                    binding.dateUtil = viewmodel.dateUtil
                    binding.stringUtil = viewmodel.stringUtil
                    binding.perfMerchantId = it.detailOrder.id
                    binding.onReview = onReview && role == "sellerReview"

                    if(role == "buyer" || role =="buyerReview"){
                        binding.nameBuyerSeller = it.detailOrder.seller
                        binding.imgBuyerSeller = it.detailOrder.seller_avatar
                        binding.buyerSeller = "Penjual"
                    }else{
                        binding.nameBuyerSeller = it.detailOrder.buyer
                        binding.imgBuyerSeller = ""
                        binding.buyerSeller = "Pembeli"
                    }



                    Timber.e("detail order ------$it")
                    if (default) {
                        if (role == "buyer") {

                            if (it.detailOrder.status == "PAID") {
                                defaultView()
                                binding.includeTop.root.visibility = View.VISIBLE
                                binding.includeTop.txtInfo.apply {
                                    text = "Menunggu Konfirmasi Penjual"
                                    gravity = Gravity.CENTER
                                }
                                binding.includeTop.imageView4.visibility = View.INVISIBLE
                                binding.includeTop.backgroundTop.setBackgroundColor(
                                    resources.getColor(
                                        R.color.yellow
                                    )
                                )

                                binding.includeBatalkanPembelian.root.visibility = View.VISIBLE
                                binding.includeBatalkanPembelian.btnBatalkanPembelian.text =
                                    "Batalkan Pembelian"

                            } else if (it.detailOrder.status == "APPROVED") {
                                defaultView()
                                binding.includeTop.root.visibility = View.VISIBLE
                                binding.includeTop.txtInfo.apply {
                                    text = "Menunggu Pengiriman"
                                    gravity = Gravity.CENTER
                                }
                                binding.includeTop.imageView4.visibility = View.INVISIBLE
                                binding.includeTop.backgroundTop.setBackgroundColor(
                                    resources.getColor(
                                        R.color.blue2
                                    )
                                )

//                                binding.includeBatalkanPembelian.root.visibility = View.VISIBLE
//                                binding.includeBatalkanPembelian.btnBatalkanPembelian.text = "Batalkan Pembelian"

                            } else if (it.detailOrder.status == "SENT" || it.detailOrder.status == "DELIVERED") {
                                defaultView()
                                binding.includeTerimaProduk.root.visibility = View.VISIBLE
                            } else if (it.detailOrder.status == "ACCEPTED") {
                                defaultView()
                                binding.includeTop.root.visibility = View.VISIBLE
                                binding.includeTop.txtInfo.apply {
                                    text = "Pembelian Selesai"
                                    gravity = Gravity.CENTER
                                }
                                binding.includeTop.imageView4.visibility = View.INVISIBLE
                                binding.includeTop.backgroundTop.setBackgroundColor(
                                    resources.getColor(
                                        R.color.blue2
                                    )
                                )

                            } else {//Cancle
                                defaultView()
                                binding.includeTop.root.visibility = View.VISIBLE
                                binding.includeTop.txtInfo.apply {
                                    text = "Pembelian ditolak Pembeli"
                                    gravity = Gravity.CENTER
                                }
                                binding.includeTop.imageView4.visibility = View.INVISIBLE
                                binding.includeTop.backgroundTop.setBackgroundColor(
                                    resources.getColor(
                                        R.color.red2
                                    )
                                )
                                binding.imgChat.visibility = View.GONE
                            }


//                        pesanan masuk -> PAID
//                        pesanan diproses -> APPROVED, SENT,DELIVERED
//                        pesanan selesai -> DECLINED, ACCEPTED

                        } else { // seler
                            if (it.detailOrder.status == "PAID") {
                                defaultView()
                                binding.includeTop.root.visibility = View.VISIBLE
                                binding.includeTop.txtInfo.apply {
                                    text =
                                        "Segera proses sebelum " + viewmodel.dateUtil.getDateTimeTomorrow(
                                            viewmodel.dateUtil.formatDate(it.detailOrder.date)
                                        )
                                    gravity = Gravity.CENTER
                                }
                                binding.includeTop.imageView4.visibility = View.INVISIBLE
                                binding.includeTop.backgroundTop.setBackgroundColor(
                                    resources.getColor(
                                        R.color.blue2
                                    )
                                )
                                binding.segeraKirimLay.visibility = View.VISIBLE
                                binding.includeTolakProses.root.visibility = View.VISIBLE
                            } else if (it.detailOrder.status == "SENT") {// barang dikirim
                                defaultView()
                                binding.includeLacak.root.visibility = View.VISIBLE
                                binding.includeLacak.btnReview.apply {
                                    setTextColor(colorWhite)
                                    setBackgroundColor(colorGray)
                                }
                            } else if (it.detailOrder.status == "DELIVERED") {//diterima buyer
                                defaultView()
                                binding.includeLacak.root.visibility = View.VISIBLE
                                binding.includeLacak.btnReview.visibility = View.GONE
                                binding.includeLacak.btnLacak.text = "Barang Terkirim"
                            } else if (it.detailOrder.status == "APPROVED") { // diterima seller otw kirim
                                defaultView()
                                binding.includeTop.root.visibility = View.VISIBLE
                                binding.includeTop.txtInfo.text =
                                    "Segera proses sebelum " + viewmodel.dateUtil.getDateTimeTomorrow(
                                        viewmodel.dateUtil.formatDate(it.detailOrder.date)
                                    )
                                binding.includeTop.backgroundTop.setBackgroundColor(
                                    resources.getColor(
                                        R.color.red2
                                    )
                                )
                                binding.layoutLacak.visibility = View.GONE
                                binding.includeKirimSekarang.root.visibility = View.VISIBLE
                            } else if (it.detailOrder.status == "ACCEPTED") { // diterima user
                                defaultView()
                                binding.includeTop.root.visibility = View.VISIBLE
                                binding.includeTop.txtInfo.apply {
                                    text = "Penjualan Selesai"
                                    gravity = Gravity.CENTER
                                }
                                binding.includeTop.imageView4.visibility = View.INVISIBLE
                                binding.includeTop.backgroundTop.setBackgroundColor(
                                    resources.getColor(
                                        R.color.blue2
                                    )
                                )

                                binding.includeLihatReview.root.visibility = View.VISIBLE
                                binding.includeLihatReview.btnLihatReview.setOnClickListener {
                                }
                            } else if (it.detailOrder.status == "DECLINED") { // ditolak penjual
                                defaultView()
                                binding.includeTop.root.visibility = View.VISIBLE
                                binding.includeTop.txtInfo.apply {
                                    text = "Penjualan Ditolak"
                                    gravity = Gravity.CENTER
                                }
                                binding.includeTop.backgroundTop.setBackgroundColor(
                                    resources.getColor(
                                        R.color.red2
                                    )
                                )
                                binding.includeTop.imageView4.visibility = View.INVISIBLE
                            }
                        }

                        val item = it

                        binding.includeBatalkanPembelian.btnBatalkanPembelian.setOnClickListener {
                            button2Dialog(
                                "buyerCancle",
                                item.detailOrder.id,
                                "Pembatalan Pembelian",
                                "Apa kamu yakin ingin membatalkan pembelian ?",
                                "Iya Batalkan ",
                                "Tidak Jadi",
                                "",
                                ""
                            )
                        }
                        binding.includeTerimaProduk.btnTerimaProduk.setOnClickListener {
                            button2Dialog(
                                "buyerAccept",
                                item.detailOrder.id,
                                "Produk Sudah Kamu Terima",
                                "Pastikan kamu menerima produk dalam keadaan baik sesuai pesanan. Uang akan diteruskan ke penjual jika kamu mengkonfirmasi penerimaan produk",
                                "Sudah Saya Terima",
                                "Batalkan",
                                "",
                                ""
                            )
                        }
                        binding.includeTolakProses.btnProses.setOnClickListener {
                            viewModelOrder.buyerName.postValue(item.detailOrder.buyer)
                            button2Dialog(
                                "proses",
                                item.detailOrder.id,
                                "Terima Orderan",
                                "Terima orderan sekarang juga",
                                "Terima Orderan",
                                "Batalkan",
                                "",
                                ""
                            )
                        }
                        binding.includeTolakProses.btnTolak.setOnClickListener {
                            viewModelOrder.buyerName.postValue(item.detailOrder.buyer)
                            button2Dialog(
                                "tolak",
                                item.detailOrder.id,
                                "Tolak Orderan",
                                "Orderan yang ditolak berpotensi mengecewakan pembeli",
                                "Tolak Orderan",
                                "Batalkan",
                                "",
                                ""
                            )
                        }

                        binding.includeKirimSekarang.btnKirimSekarang.setOnClickListener {
                            button2Dialog(
                                item.detailOrder.status,
                                item.detailOrder.id,
                                "Konfirmasi Pengiriman",
                                "Apa kamu sudah mengantar produk ke lokasi pengiriman ?",
                                "Sudah",
                                "Belum",
                                item.detailOrder.courrier_name,
                                item.detailOrder.destination_address
                            )
                        }

                        binding.includeLacak.btnLacak.setOnClickListener {
                            OrderHistoryDeliveryPage.open(this@OrderDetailPage, transaksiId)
                        }

                    }

                } catch (e: Exception) {
                }
            }
        }


        lifecycleScope.launchWhenCreated {
            viewmodel.loadTrackingDetail(transaksiId).collectLatest {
                try {
                    binding.txtLastStatus.text = "(${it[0].datetime}) ${it[0].description}"
                } catch (e: Exception) {
                }
            }
        }


    }

    private fun defaultView() {
        binding.includeTop.root.visibility = View.GONE
        binding.includeBatalkanPembelian.root.visibility = View.GONE
        binding.includeTerimaProduk.root.visibility = View.GONE
        binding.segeraKirimLay.visibility = View.GONE
        binding.includeTolakProses.root.visibility = View.GONE
        binding.includeLacak.root.visibility = View.GONE
        binding.includeKirimSekarang.root.visibility = View.GONE
        binding.includeLihatReview.root.visibility = View.GONE

    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(RESULT_OK, returnIntent)
        finish()
    }


    private val ProductAdapter by lazy {
        object : ListAdapter<TransaksiProductTable, Viewholder>(object :
            DiffUtil.ItemCallback<TransaksiProductTable>() {
            override fun areItemsTheSame(
                oldItem: TransaksiProductTable,
                newItem: TransaksiProductTable
            ): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(
                oldItem: TransaksiProductTable,
                newItem: TransaksiProductTable
            ): Boolean =
                oldItem == newItem
        }) {
            override fun getItemViewType(position: Int): Int = getItem(position)?.let {
                if (onReview) 101 else 102
            } ?: 102

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                if (viewType == 101) {
                    Timber.e("view type $viewType")
                    ProductRatingVH(parent)
                } else {
                    Timber.e("view type $viewType")
                    ProductDefautVH(parent)

                }


            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(getItem(position))

        }
    }

    private abstract class Viewholder(view: View) : RecyclerView.ViewHolder(view) {

        abstract fun bind(item: TransaksiProductTable)
    }

    private inner class ProductDefautVH(
        parent: ViewGroup,
        val binding: OrderDetailProductItemBinding = OrderDetailProductItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : Viewholder(binding.root) {

        override fun bind(item: TransaksiProductTable) {
            binding.imgProduct.clipToOutline = true
            binding.item = item
            binding.stringUtil = viewModelOrder.stringUtil

            binding.root.setOnClickListener {
                DetailProduk.open(this@OrderDetailPage, item.goody_id)
            }
        }

    }


    private inner class ProductRatingVH(
        parent: ViewGroup,
        val binding: KwuReviewProductItemBinding = KwuReviewProductItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : Viewholder(binding.root) {

        override fun bind(item: TransaksiProductTable) {
            binding.imgProduct.clipToOutline = true
            binding.item = item
            binding.stringUtil = viewModelOrder.stringUtil
            var ratingBuyer = 0
            var ratingSeller = 0
            lifecycleScope.launch {
                viewmodel.loadReview(transaksiId, item.goody_review_id, item.goody_id, role, "buyer").collectLatest {
                    Timber.e("load review--- buyer$it")
                    try {
                        binding.reviewData = it
                        binding.executePendingBindings()
                    } catch (e: Exception) { }

                    val data = it
                    val rating = try { data.rating } catch (e: Exception) { 0 }
                    ratingBuyer = rating
                    if (role == "sellerReview") { binding.root.setBackgroundColor(resources.getColor(R.color.white)) }
                }
            }
            lifecycleScope.launch {
                viewmodel.loadReview(transaksiId, item.goody_review_id, item.goody_id, role, "merchant").collectLatest {
                    Timber.e("load review--- merchant $it")
                    val data = it
                    val rating = try { data.rating } catch (e: Exception) { 0 }
                    ratingSeller = rating
                }
            }

            binding.root.setOnClickListener {
                Timber.e("ratingBuyer $ratingBuyer")
                Timber.e("ratingSeller $ratingSeller")
                if (role == "buyerReview") {
                    if (ratingBuyer > 0) {
                        DetailReviewPage.open(
                            this@OrderDetailPage,
                            item.order_id,
                            item.goody_review_id,
                            role,
                            item.goody_id
                        )
                    } else {
                        AddReviewBuyerPage.open(
                            this@OrderDetailPage,
                            item.order_id,
                            item.goody_review_id,
                            role
                        )
                    }
                } else if (role == "sellerReview") {
                    if (ratingBuyer > 0 && ratingSeller > 0) {
                        DetailReviewPage.open(
                            this@OrderDetailPage,
                            item.order_id,
                            item.goody_review_id,
                            role,
                            item.goody_id
                        )
                    } else if (ratingBuyer > 0) {
                        AddReviewPage.open(
                            this@OrderDetailPage,
                            item.order_id,
                            item.goody_review_id,
                            role
                        )
                    } else {
                        DetailProduk.open(this@OrderDetailPage, item.goody_id)
                    }
                } else {
                    DetailProduk.open(this@OrderDetailPage, item.goody_id)
                }
            }


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
        val dialog = AlertDialog.Builder(this)
            .setView(bindingDialog.root)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

//        pesanan masuk -> PAID
//        pesanan diproses -> APPROVED, SENT,DELIVERED
//        pesanan selesai -> DECLINED, ACCEPTED
        if (status == "tolak" || status == "PAID" || status == "APPROVED") {
            bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_danger) }
        } else if (status == "proses" || status == "APPROVED" || status == "SENT" || status == "DELIVERED") {
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
                    viewModelOrder.rejectTransaksi(transaksiID) // tolak transaksi seller
                }
            } else if (status == "APPROVED") {
                addResiDialog(transaksiID, courier, address) //  add resi seller
            } else if (status == "proses") {
                lifecycleScope.launch {
                    viewModelOrder.acceptTransaksi(transaksiID) // terima pesanan seller
                }
            } else if (status == "buyerAccept") {
                lifecycleScope.launch {
                    viewModelPembelian.acceptTransaksiBuyer(transaksiID) // terima pesanan buyer
                }
            } else if (status == "buyerCancle") {
                lifecycleScope.launch {
                    viewModelPembelian.CancleTransaksiBuyer(transaksiID) // cancle transaksi buyer
                }
            }
        }
        bindingDialog.button2.btnDone.setOnClickListener {
            dialog.dismiss()
        }


    }

    private fun button1Dialog(
        status: String,
        title: String,
        desc: String,
        btnDone: String,
    ) {
        val bindingDialog = EntrepreneursCostomDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(bindingDialog.root)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

        if (status == "decline") {
            bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_danger) }
        } else {
            bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_pay_success) }
        }


        bindingDialog.txtTitle.text = title
        bindingDialog.txtDesc.text = desc

        lifecycleScope.launch {
//            viewModelOrder.fetchListSellerOrder(
//                0,
//                "incoming",
//                "seller",
//                ""
//            )
            Handler().postDelayed({
                viewmodel.loadDetail(transaksiId, role)
            }, 400)
        }

        bindingDialog.button1.root.visibility = View.VISIBLE
        bindingDialog.button1.btnDone.text = btnDone
        bindingDialog.button1.btnDone.setOnClickListener {
//            viewModelOrder.incActResponseBoolean.postValue(false)
//            Timber.e("status dialog 1 $status")
            dialog.dismiss()
        }

    }


    private fun addResiDialog(transaksiID: Int, courier: String, address: String) {
        val bindingDialog = EntrepreneursDialogMasukanResiBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(bindingDialog.root)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

        bindingDialog.courier = courier
        bindingDialog.address = address
        bindingDialog.actionKonfirmasi.setOnClickListener {
            if (!bindingDialog.inResi.text.isNullOrEmpty()) {
                // launch
                lifecycleScope.launch {
                    viewModelOrder.inputResi(
                        transaksiID,
                        bindingDialog.inResi.text.toString(),
                        courier
                    )
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Nomor resi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
        bindingDialog.actionBatal.setOnClickListener {
            dialog.dismiss()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode === 311) {
            if (resultCode === Activity.RESULT_OK) {
                viewmodel.loadDetail(transaksiId, role)
            }
        }  else if (requestCode === 312) {
            if (resultCode === Activity.RESULT_OK) {
                viewmodel.loadDetail(transaksiId, role)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


}