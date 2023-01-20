package id.onklas.app.pages.ppob.bayar

import android.os.Bundle
import androidx.activity.viewModels
import id.onklas.app.databinding.KlaspayBayarPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.klaspay.tagihan.KlaspayTransactionInvoiceItem

class KlaspayBayarPage : Privatepage() {

    private val binding by lazy { KlaspayBayarPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<KlaspayBayarViewModel> { component.klaspayBayarVmFactory }
    private val from by lazy { intent.getStringExtra("from") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            binding.toolbar.setNavigationOnClickListener {
                if (viewModel.titleBar.value == "Selesaikan Pembayaran") finish()
                else onBackPressed()
            }
        }

        when (from) {
            "invoice" -> {
                viewModel.fromInvoice(intent.getSerializableExtra("invoice") as KlaspayTransactionInvoiceItem)
            }
            "topup" -> {
                viewModel.fromTopup(intent.getSerializableExtra("topup") as KlaspayBayarResponse)
            }
        }

        viewModel.titleBar.observe(this, {
            binding.toolbar.title = it
        })
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        super.onBackPressed()
    }
}