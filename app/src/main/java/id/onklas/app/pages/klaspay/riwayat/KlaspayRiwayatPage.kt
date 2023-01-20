package id.onklas.app.pages.klaspay.riwayat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import id.onklas.app.databinding.KlaspayRiwayatPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class KlaspayRiwayatPage : Privatepage() {

    private val binding by lazy { KlaspayRiwayatPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<KlaspayRiwayatViewModel> { component.KlaspayRiwayatVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        }

        viewModel.titleBar.observe(this, {
            binding.toolbar.title = it
        })

        viewModel.errorString.observe(this, {
            if (it.isNotEmpty()) Toast.makeText(applicationContext, it, Toast.LENGTH_SHORT).show()
        })
    }
}