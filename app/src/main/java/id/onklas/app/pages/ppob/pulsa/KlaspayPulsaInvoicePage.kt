package id.onklas.app.pages.ppob.pulsa

import android.os.Bundle
import id.onklas.app.databinding.KlaspayPulsaInvoicePageBinding
import id.onklas.app.pages.Privatepage

class KlaspayPulsaInvoicePage : Privatepage() {

    private val binding by lazy { KlaspayPulsaInvoicePageBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}