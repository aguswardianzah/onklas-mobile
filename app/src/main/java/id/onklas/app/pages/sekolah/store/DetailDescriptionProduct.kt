package id.onklas.app.pages.sekolah.store

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.onklas.app.databinding.BottomSheetAddCartBinding
import id.onklas.app.databinding.DetailDescriptionProductBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import kotlinx.coroutines.launch

class DetailDescriptionProduct : Privatepage() {

    companion object {
        fun open(
            activity: AppCompatActivity,
            goodiesId: Int,
            stock: Int,
            fullDesc: String,
            merchantId: Int
        ) {
            activity.startActivity(
                Intent(activity, DetailDescriptionProduct::class.java)
                    .putExtra("goodiesId", goodiesId)
                    .putExtra("stock", stock)
                    .putExtra("fullDesc", fullDesc)
                    .putExtra("merchantId", merchantId)
            )
        }
    }

    private val binding by lazy { DetailDescriptionProductBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<StoreVm> { component.storeVmFactory }
    private var stock = 0
    private var goodiesId = 0
    private var merchantId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        stock = intent.getIntExtra("stock", 0)
        goodiesId = intent.getIntExtra("goodiesId", 0)
        merchantId = intent.getIntExtra("merchantId", 0)

        lifecycleScope.launchWhenCreated {
            viewmodel.loadDetailGoodie(goodiesId)
        }
        binding.txtDesc.text = intent.getStringExtra("fullDesc")

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.merchantId = merchantId
        binding.perfMerchantId = viewmodel.pref.getString("merchantId").toInt()

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
        var stock = 0
        var goodieId = 0

        viewmodel.goodieDetailProduk.observe(this, Observer {
            bindingAddCart.imgProduct.clipToOutline = true
            bindingAddCart.item = it
            bindingAddCart.stringUtil = viewmodel.stringUtil
            bindingAddCart.viewholder = viewmodel
            stock = it.stock
            goodieId = it.id
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
                            CartPage.open(this@DetailDescriptionProduct)
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