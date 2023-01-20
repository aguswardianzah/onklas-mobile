package id.onklas.app.pages.sekolah.store

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.BottomSheetAddCartBinding
import id.onklas.app.databinding.DetailProdukBinding
import id.onklas.app.databinding.StoreDetailproductImgItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.chat.ChatPage
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM
import id.onklas.app.pages.klaspay.aktivasi.KlaspayAktivasiPage
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class DetailProduk : Privatepage() {

    companion object {
        fun open(activity: Privatepage, goodieId: Int) {
            activity.startActivity(
                Intent(activity, DetailProduk::class.java)
                    .putExtra("goodieId", goodieId)
            )
        }
    }

    private val binding by lazy { DetailProdukBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodel by viewModels<StoreVm> { component.storeVmFactory }
    private var goodieId = 0
    private var merchantId = 0
    private var stock = 0
    private var fullDesc = ""

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_cart, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Timber.e("click meenu ------------- ")
        CartPage.open(this)
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        goodieId = intent.getIntExtra("goodieId", 0)

        lifecycleScope.launchWhenCreated {
            viewmodel.loadDetailGoodie(goodieId)
        }
        viewmodel.LoadingShow.observe(this, Observer {
            binding.swipeRefresh.isRefreshing = it
        })

        viewmodel.goodieDetailProduk.observe(this, Observer {
            binding.txtMax.text = it.image.size.toString()
            binding.item = it
            binding.stringUtil = viewmodel.stringUtil
            binding.viewholder = viewmodel

            ImgAdapter.submitList(it.image)
            merchantId = it.merchant?.id ?: 0
            stock = it.stock

            fullDesc = it.description
            binding.perfMerchantId = viewmodel.pref.getString("merchantId", "0").toInt()
            if (it.rating == "" || it.rating == "0" || it.rating.isEmpty()) {
                binding.actionRating.visibility = View.GONE
            } else {
                binding.actionRating.visibility = View.VISIBLE
            }
        })

        binding.swipeRefresh.apply {
            setOnRefreshListener {
                lifecycleScope.launch {
                    viewmodel.loadDetailGoodie(goodieId)
                }
                viewmodel.goodieDetailProduk.observe(this@DetailProduk, Observer {
                    binding.txtMax.text = it.image.size.toString()

                    binding.item = it
                    binding.stringUtil = viewmodel.stringUtil
                    binding.viewholder = viewmodel
                    ImgAdapter.submitList(it.image)

                    merchantId = it.merchant?.id ?: 0
                    stock = it.stock

                    fullDesc = it.description
                    if (it.rating == "" || it.rating == "0" || it.rating.isEmpty()) {
                        binding.actionRating.visibility = View.GONE
                    } else {
                        binding.actionRating.visibility = View.VISIBLE
                    }
                })
            }
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Detail Produk"
            binding.toolbar.title = "Detail Produk"
        }

        binding.viewPager2.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.txtPosisi.text = (position + 1).toString()
            }
        })

        binding.viewPager2.adapter = ImgAdapter

        binding.actionDetailDesc.setOnClickListener {
            DetailDescriptionProduct.open(
                this,
                goodieId,
                stock,
                fullDesc,
                merchantId
            )
        }
        binding.actionRating.setOnClickListener {
            RatingReviewProduct.open(this, goodieId, stock, merchantId)
        }

        binding.actionBuyNow.setOnClickListener {
            if (stock > 0) {
                bottomSeetAddtoCart(
                    this,
                    layoutInflater,
                    "Beli Produk",
                    0
                )
            } else {
                alert(msg = "Stok produk Kosong")
            }

        }
        binding.actionAddCart.setOnClickListener {
            if (stock > 0) {
                bottomSeetAddtoCart(
                    this,
                    layoutInflater,
                    "Tambah ke Keranjang",
                    1
                )
            } else {
                alert(msg = "Stok produk Kosong")
            }
        }

        binding.actionSellerProfile.setOnClickListener {
            SellerProfilePage.open(this, binding.textView30.text.toString(), merchantId)
        }

        viewmodel.apiWrapper.errorString.observe(this, Observer {
            // sementara error di buat statis karena api belum menyediakan
            alert(msg = "Produk ini sudah mencapai batas stok di keranjang anda")
            viewmodel.apiWrapper.errorString.removeObserver { this }
        })

        binding.actionChat.setOnClickListener {
            if (viewmodel.pref.getBoolean("klaspayActive")) {
                lifecycleScope.launchWhenCreated {
                    try {
                        loading("Memproses permintaan")
                        val resp = viewmodel.apiService.merchantKlaspay(merchantId)
                        dismissloading()

                        val detailProduk = viewmodel.goodieDetailProduk.value
                        startActivity(
                            Intent(this@DetailProduk, ChatPage::class.java)
                                .putExtra("with", resp.data.chat_id)
                                .putExtra("name", detailProduk?.merchant?.name)
                                .putExtra("image", detailProduk?.merchant?.avatar)
                                .putExtra("payload_productId", detailProduk?.id)
                                .putExtra("payload_productName", detailProduk?.name)
                                .putExtra(
                                    "payload_productImage",
                                    detailProduk?.image?.firstOrNull()?.image
                                )
                                .putExtra("payload_productPrice", detailProduk?.price)
                        )
                    } catch (e: Exception) {
                    } finally {
                        dismissloading()
                    }
                }
            } else {
                prettyAlert(
                    showImage = true,
                    isSuccess = false,
                    titleText = "Aktivasi Klaspay Lebih Dahulu",
                    msg = "Kamu belum mengaktifkan KlasPay, silahkan aktifkan KlasPay terlebih dahulu",
                    okLabel = "Aktifasi Sekarang",
                    abortLabel = "",
                    okClick = {
                        startActivity(
                            Intent(
                                this@DetailProduk,
                                KlaspayAktivasiPage::class.java
                            )
                        )
                    })
            }
        }
    }

    private val ImgAdapter by lazy {
        object : ListAdapter<GoodieDetailImg, ImgViewHolder>(object :
            DiffUtil.ItemCallback<GoodieDetailImg>() {
            override fun areItemsTheSame(
                oldItem: GoodieDetailImg,
                newItem: GoodieDetailImg
            ): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: GoodieDetailImg,
                newItem: GoodieDetailImg
            ): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImgViewHolder =
                ImgViewHolder(parent)

            override fun onBindViewHolder(holder: ImgViewHolder, position: Int) =
                holder.bind(getItem(position))

        }
    }

    private inner class ImgViewHolder(
        parent: ViewGroup,
        val binding: StoreDetailproductImgItemBinding = StoreDetailproductImgItemBinding.inflate(
            LayoutInflater.from(
                parent.context
            ), parent, false
        )
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GoodieDetailImg) {
            glide.load(item.image)
                .centerCrop()
                .into(binding.imgProduct)
        }
    }

    fun bottomSeetAddtoCart(
        context: Context,
        layoutInflater: LayoutInflater,
        judul: String,
        position: Int
    ) {
        val dialog = BottomSheetDialog(context)
        val bindingAddCart by lazy { BottomSheetAddCartBinding.inflate(layoutInflater) }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = bindingAddCart.root

        viewmodel.goodieDetailProduk.observe(this, Observer {
            bindingAddCart.imgProduct.clipToOutline = true
            bindingAddCart.item = it
            bindingAddCart.stringUtil = viewmodel.stringUtil
            bindingAddCart.viewholder = viewmodel
        })

        bindingAddCart.txtJudul.text = judul
        bindingAddCart.actionExit.setOnClickListener {
            dialog.dismiss()
        }
        bindingAddCart.actionMin.setOnClickListener {
            var data = bindingAddCart.txtQty.text.toString().toInt()
            if (data > 1) {
                bindingAddCart.txtQty.text = (data - 1).toString()
            }
        }
        bindingAddCart.actionPlus.setOnClickListener {
            var data = bindingAddCart.txtQty.text.toString().toInt()
            if (data < stock) {
                if (data > 0) {
                    bindingAddCart.txtQty.text = (data + 1).toString()

                }
            } else {
                alert(msg = "Jumlah barang tidak dapat melebihi stok")
            }
        }
        bindingAddCart.actionAddCart.setOnClickListener {
            if (stock > 0) {
                var data = bindingAddCart.txtQty.text.toString().toInt()
                lifecycleScope.launch {
                    loading(msg = "memproses permintaan")
                    viewmodel.apiWrapper.addToCart(
                        goodieId,
                        data
                    )
                    dismissloading()
                    dialog.dismiss()
                    if (position == 0) {
                        Handler().postDelayed({
                            CartPage.open(this@DetailProduk)
                        }, 800)
                    } else {
                        dialog.dismiss()
                    }
                }
            } else {
                alert(msg = "Stok produk Kosong")
            }
        }


        dialog.setContentView(view)
        dialog.show()
    }

}