package id.onklas.app.pages.klaspay.topup

import android.os.Bundle
import androidx.activity.viewModels
import id.onklas.app.databinding.KlaspayTopupPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class KlaspayTopupPage : Privatepage() {

    private val binding by lazy { KlaspayTopupPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<KlaspayTopupViewModel> { component.KlaspayTopupVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener {
                if (viewModel.titleBar.value!! == "Selesaikan Pembayaran") finish()
                else onBackPressed()
            }
        }

        viewModel.titleBar.observe(this, {
            binding.toolbar.title = it
        })

        viewModel.errorString.observe(this, {
            if (it.isNotEmpty()) toast(it)
        })
    }
}