package id.onklas.app.pages.ppob.listrik

import android.os.Bundle
import id.onklas.app.databinding.KlaspayListrikDetailPageBinding
import id.onklas.app.pages.Privatepage

class KlaspayListrikDetailPage : Privatepage() {

    private val binding by lazy { KlaspayListrikDetailPageBinding.inflate(layoutInflater) }

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