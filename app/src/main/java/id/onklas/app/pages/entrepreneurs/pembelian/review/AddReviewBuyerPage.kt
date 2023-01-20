package id.onklas.app.pages.entrepreneurs.pembelian.review

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.AddReviewBuyerPageBinding
import id.onklas.app.databinding.EntrepreneursCostomDialogBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM
import id.onklas.app.pages.entrepreneurs.PembelianVM
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class AddReviewBuyerPage : Privatepage() {
    companion object {
        fun open(
                activity: Privatepage,
                TransaksiId: Int,
                TransaksiSubId: Int,
                role: String,
        ) {
            val i = Intent(activity, AddReviewBuyerPage::class.java)
                    .putExtra("TransaksiId", TransaksiId)
                    .putExtra("TransaksiSubId", TransaksiSubId)
                    .putExtra("role", role)
            activity.startActivityForResult(i, 311)
        }
    }


    private val binding by lazy { AddReviewBuyerPageBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodelPembelian by viewModels<PembelianVM> { component.entrepreneursPembelianFactory }
    private val viewmodel by viewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val colorGray by lazy { ResourcesCompat.getColor(resources, R.color.Black3, null) }
    private val colorGold by lazy { ResourcesCompat.getColor(resources, R.color.gold, null) }
    private val colorBlue by lazy { ResourcesCompat.getColor(resources, R.color.colorPrimary, null) }

    private var star = 0
    private var jenis = ""

    private var TransaksiSubId = 0
    private var TransaksiId = 0
    private var role = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Review Penjualan"
            binding.toolbar.title = "Review penjualan"
        }


        binding.stringUtil = viewmodel.stringUtil

        TransaksiId = intent.getIntExtra("TransaksiId", 0)
        TransaksiSubId = intent.getIntExtra("TransaksiSubId", 0)
        role = intent.getStringExtra("role").toString()


        lifecycleScope.launch {
            viewmodel.loadDetail(TransaksiId, role).collectLatest {
                for (i in it.product.indices) {
                    val data = it.product[i]
                    if (data.order_id == TransaksiId && data.goody_review_id == TransaksiSubId) {
                        binding.item = data
                        binding.stringUtil = viewmodel.stringUtil
                    }
                }
            }
        }
        starListener()


        binding.actionSendReview.setOnClickListener {
            if (star == 0 || binding.inputComment.text.toString().isEmpty()) {
                toast("rating dan komentar tidak boleh kosong ")
            } else {
                lifecycleScope.launch {
                    viewmodelPembelian.PostReview(TransaksiSubId, star, binding.inputComment.text.toString())
                }
            }

        }

        viewmodelPembelian.BuyerAnyResponse.observe(this, Observer {
            Timber.e("buyer any response -- $it")
            button1Dialog()
        })
        viewmodel.errorString.observe(this, Observer(this::toast))


    }

    private fun starListener() {

        binding.star1.setOnClickListener {
            binding.star1.apply { setColorFilter(colorGold) }

            binding.star2.apply { setColorFilter(colorGray) }
            binding.star3.apply { setColorFilter(colorGray) }
            binding.star4.apply { setColorFilter(colorGray) }
            binding.star5.apply { setColorFilter(colorGray) }

            star = 1
        }

        binding.star2.setOnClickListener {
            binding.star1.apply { setColorFilter(colorGold) }
            binding.star2.apply { setColorFilter(colorGold) }

            binding.star3.apply { setColorFilter(colorGray) }
            binding.star4.apply { setColorFilter(colorGray) }
            binding.star5.apply { setColorFilter(colorGray) }

            star = 2
        }
        binding.star3.setOnClickListener {
            binding.star1.apply { setColorFilter(colorGold) }
            binding.star2.apply { setColorFilter(colorGold) }
            binding.star3.apply { setColorFilter(colorGold) }

            binding.star4.apply { setColorFilter(colorGray) }
            binding.star5.apply { setColorFilter(colorGray) }

            star = 3
        }
        binding.star4.setOnClickListener {
            binding.star1.apply { setColorFilter(colorGold) }
            binding.star2.apply { setColorFilter(colorGold) }
            binding.star3.apply { setColorFilter(colorGold) }
            binding.star4.apply { setColorFilter(colorGold) }

            binding.star5.apply { setColorFilter(colorGray) }

            star = 4
        }
        binding.star5.setOnClickListener {
            binding.star1.apply { setColorFilter(colorGold) }
            binding.star2.apply { setColorFilter(colorGold) }
            binding.star3.apply { setColorFilter(colorGold) }
            binding.star4.apply { setColorFilter(colorGold) }
            binding.star5.apply { setColorFilter(colorGold) }

            star = 5
        }

    }

    private fun button1Dialog() {
        val bindingDialog = EntrepreneursCostomDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
                .setView(bindingDialog.root)
                .show()
                .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

//        bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_danger) }
        bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_pay_success) }

        bindingDialog.txtTitle.text = "Review Berhasil Dikirim"
        bindingDialog.txtDesc.text = "Selamat melakukan penjualan kepada pembeli berikutnya ya"


        bindingDialog.button1.root.visibility = View.VISIBLE
        bindingDialog.button1.btnDone.text = "Oke Terimakasih"
        bindingDialog.button1.btnDone.setOnClickListener {
            dialog.dismiss()
            val returnIntent = Intent()
            setResult(RESULT_OK, returnIntent)
            finish()
        }

    }

}