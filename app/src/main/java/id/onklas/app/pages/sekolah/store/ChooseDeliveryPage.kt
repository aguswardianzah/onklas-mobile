package id.onklas.app.pages.sekolah.store

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import id.onklas.app.GlideApp
import id.onklas.app.databinding.ChooseDeliveryPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class ChooseDeliveryPage : Privatepage() {

    companion object {
        fun open(activity: AppCompatActivity) {
            activity.startActivity(
                    Intent(activity, ChooseDeliveryPage::class.java))
        }
    }

    private val binding by lazy { ChooseDeliveryPageBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodel by viewModels<StoreVm> { component.storeVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Pilih Metode Pengiriman" // untuk title jumlah produk
            binding.toolbar.title = "Pilih Metode Pengiriman "
        }


    }
}