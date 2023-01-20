package id.onklas.app.pages.klaspay.tagihan

import android.os.Bundle
import androidx.activity.viewModels
import id.onklas.app.databinding.KlaspayTagihanDetailPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import timber.log.Timber

class KlaspayTagihanDetailPage : Privatepage() {

    private val binding by lazy { KlaspayTagihanDetailPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<KlaspayTagihanViewModel> { component.KlaspayTagihanVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        }

        viewModel.invoiceItem.observe(this, { invoice ->
            Timber.e("invoiceItem: $invoice")
            binding.textDateTopup.text = invoice.created_date
            binding.textStatusTopup.text = invoice.transaction_status
            binding.textBiayaAdmin.text = viewModel.numberUtil.formatCurrency( invoice.details!!.fee_admin!! )
            binding.textJenisPembayaran.text = invoice.type
            binding.textTotalTopup.text = viewModel.numberUtil.formatCurrency( invoice.details.total_amount!!.toInt() )
        })

    }
}