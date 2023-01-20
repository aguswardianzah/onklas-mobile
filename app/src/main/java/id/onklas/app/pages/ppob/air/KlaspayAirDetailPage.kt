package id.onklas.app.pages.ppob.air

import android.os.Bundle
import id.onklas.app.databinding.KlaspayAirDetailPageBinding
import id.onklas.app.pages.Privatepage

class KlaspayAirDetailPage : Privatepage() {

    private val binding by lazy { KlaspayAirDetailPageBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }
    }
}