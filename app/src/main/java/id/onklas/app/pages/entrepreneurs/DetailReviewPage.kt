package id.onklas.app.pages.entrepreneurs

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import id.onklas.app.GlideApp
import id.onklas.app.databinding.DetailReviewPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class DetailReviewPage : Privatepage() {
    companion object {
        fun open(
                activity: Privatepage,
                TransaksiId: Int,
                TransaksiSubId: Int,
                role:String,
                GoodyId:Int,
        ) {
            val i = Intent(activity, DetailReviewPage::class.java)
                    .putExtra("role", role)
                    .putExtra("TransaksiId", TransaksiId)
                    .putExtra("TransaksiSubId", TransaksiSubId)
                    .putExtra("goodyId", GoodyId)
            activity.startActivity(i)
        }
    }

    private val binding by lazy { DetailReviewPageBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodelPembelian by viewModels<PembelianVM> { component.entrepreneursPembelianFactory }
    private val viewmodel by viewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private var TransaksiSubId = 0
    private var TransaksiId = 0
    private var role = ""
    private var GoodyId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        TransaksiId = intent.getIntExtra("TransaksiId", 0)
        TransaksiSubId = intent.getIntExtra("TransaksiSubId", 0)
        GoodyId = intent.getIntExtra("goodyId", 0)
        role = intent.getStringExtra("role").toString()



        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Review Produk"
            binding.toolbar.title = "Review Produk"
        }

        lifecycleScope.launch {
            viewmodel.loadReview(
                TransaksiId, TransaksiSubId, GoodyId, role, "buyer"
            ).collectLatest {
                binding.reviewDataBuyer = it
                binding.dateUtil = viewmodel.dateUtil
                binding.executePendingBindings()
            }

        }
        lifecycleScope.launch {
            viewmodel.loadReview(
                TransaksiId, TransaksiSubId, GoodyId, role, "merchant"
            ).collectLatest {
                binding.reviewDataMerchant = it
                binding.executePendingBindings()
            }

        }


        lifecycleScope.launch {
            viewmodel.loadDetail(TransaksiId, role).collectLatest {
                binding.stringUtil = viewmodel.stringUtil
                for (i in it.product.indices){
                    val data= it.product[i]
                    if(data.order_id == TransaksiId && data.goody_review_id == TransaksiSubId){
                        binding.item = data
                        binding.executePendingBindings()
                    }
                }


            }
        }

    }
}