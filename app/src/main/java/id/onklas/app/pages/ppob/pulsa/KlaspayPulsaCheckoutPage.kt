package id.onklas.app.pages.ppob.pulsa

import android.os.Bundle
import id.onklas.app.databinding.KlaspayPulsaCheckoutPageBinding
import id.onklas.app.pages.Privatepage

class KlaspayPulsaCheckoutPage : Privatepage() {

    private val binding by lazy { KlaspayPulsaCheckoutPageBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}