package id.onklas.app.pages.entrepreneurs.myMerchant.Review

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.AddReviewBuyerPageBinding
import id.onklas.app.databinding.AddReviewPageBinding
import id.onklas.app.databinding.EntrepreneursCostomDialogBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM
import id.onklas.app.pages.entrepreneurs.OrderVM
import id.onklas.app.pages.entrepreneurs.PembelianVM
import id.onklas.app.pages.entrepreneurs.pembelian.review.AddReviewBuyerPage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class AddReviewPage : Privatepage() {

    companion object {
        fun open(
                activity: Privatepage,
                TransaksiId: Int,
                TransaksiSubId: Int,
                role: String,
        ) {
            val i = Intent(activity, AddReviewPage::class.java)
                    .putExtra("TransaksiId", TransaksiId)
                    .putExtra("TransaksiSubId", TransaksiSubId)
                    .putExtra("role", role)
            activity.startActivityForResult(i, 312)
        }
    }

    private val binding by lazy { AddReviewPageBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodelOrder by viewModels<OrderVM> { component.entrepreneursOrderFactory }
    private val viewmodel by viewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val colorGray by lazy { ResourcesCompat.getColor(resources, R.color.Black3, null) }
    private val colorGold by lazy { ResourcesCompat.getColor(resources, R.color.gold, null) }
    private val colorBlue by lazy { ResourcesCompat.getColor(resources, R.color.colorPrimary, null) }


    private var TransaksiSubId = 0
    private var TransaksiId = 0
    private var role = ""

    private var star = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        TransaksiId = intent.getIntExtra("TransaksiId", 0)
        TransaksiSubId = intent.getIntExtra("TransaksiSubId", 0)
        role = intent.getStringExtra("role").toString()


        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Review Penjualan"
            binding.toolbar.title = "Review penjualan"
        }

        lifecycleScope.launch {
            viewmodel.loadDetail(TransaksiId, role).collectLatest {
                binding.stringUtil = viewmodel.stringUtil
                for (i in it.product_with_review.indices){
                    for(u in it.product_with_review[i].review_data.indices){
                        val data = it.product_with_review[i].review_data[u]
                        if(data.tipe == "buyer"){
                            binding.reviewDataBuyer = data
                            binding.dateUtil = viewmodel.dateUtil
                            binding.executePendingBindings()
                        }
                        if(data.tipe == "merchant"){
                            binding.reviewDataMerchant = data
                            binding.executePendingBindings()
                        }
                    }

                }
                for (i in it.product.indices){
                    val data= it.product[i]
                    if(data.order_id == TransaksiId && data.goody_review_id == TransaksiSubId){
                        binding.item = data
                        binding.executePendingBindings()
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
                    viewmodelOrder.PostReview(TransaksiSubId, star, binding.inputComment.text.toString())
                }
            }

        }

        viewmodelOrder.BuyerAnyResponse.observe(this, Observer {
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