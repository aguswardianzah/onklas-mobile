package id.onklas.app.pages.entrepreneurs.myMerchant.addProduct

import android.os.Bundle
import androidx.activity.viewModels
import id.onklas.app.databinding.AddProductMetodePengirimanBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM

class AddProductMetodePengiriman : Privatepage() {
    private val binding by lazy { AddProductMetodePengirimanBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<EntrepreneursVM> { component.entrepreneursFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Metode Pengiriman"
            binding.toolbar.title = "Metode Pengiriman"
        }
    }
}