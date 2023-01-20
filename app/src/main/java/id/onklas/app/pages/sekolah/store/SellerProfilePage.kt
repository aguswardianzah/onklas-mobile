package id.onklas.app.pages.sekolah.store

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.SellerProfilePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class SellerProfilePage : Privatepage() {

    companion object {
        fun open(activity: AppCompatActivity, sellerName: String, sellerId: Int) {
            activity.startActivity(
                    Intent(activity, SellerProfilePage::class.java)
                            .putExtra("sellerName", sellerName)
                            .putExtra("sellerId", sellerId)
            )
        }
    }

    private val binding by lazy { SellerProfilePageBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodel by viewModels<StoreVm> { component.storeVmFactory }
    private val navController by lazy { findNavController(R.id.page_container) }

    private val colorPrimary by lazy {
        ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
    }
    private val colorGray by lazy {
        ResourcesCompat.getColor(resources, R.color.gray, null)
    }
    private val colorWhite by lazy {
        ResourcesCompat.getColor(resources, R.color.white, null)
    }

    private var sellerId = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        sellerId = intent.getIntExtra("sellerId", 0)

        lifecycleScope.launchWhenCreated {
            viewmodel.loadMerchantGoodie(sellerId)
        }

        viewmodel.Merchant.observe(this, Observer {
            binding.item = it
        })


        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title ="Profil Penjual"
            binding.toolbar.title ="Profil Penjual"
        }
        binding.btnFilter.visibility = View.VISIBLE


        binding.actionProduct.setOnClickListener {
            navController.popBackStack()
            navController.navigate(R.id.action_global_sellerProduct)

            binding.actionProduct.apply { setColorFilter(colorPrimary) }

            binding.actionOrderHistory.apply { setColorFilter(colorGray) }

            binding.btnFilter.visibility = View.VISIBLE
        }

        binding.actionOrderHistory.setOnClickListener {
            navController.popBackStack()
            navController.navigate(R.id.action_global_sellerPenjualan)

            binding.actionProduct.apply { setColorFilter(colorGray) }

            binding.actionOrderHistory.apply { setColorFilter(colorPrimary) }

            binding.btnFilter.visibility = View.GONE
        }

        binding.btnFilter.setOnClickListener {
            val intent = Intent("OnklasBroadcast")
            intent.putExtra("FilterSellerProduct", "FilterSellerProduct")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)//local broadcast
        }

    }
}