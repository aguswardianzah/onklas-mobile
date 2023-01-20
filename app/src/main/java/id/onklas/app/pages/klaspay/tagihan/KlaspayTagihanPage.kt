package id.onklas.app.pages.klaspay.tagihan

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.KlaspayTagihanPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class KlaspayTagihanPage : Privatepage() {

    private val binding by lazy { KlaspayTagihanPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<KlaspayTagihanViewModel> { component.KlaspayTagihanVmFactory }
    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private val navController by lazy { findNavController(R.id.page_container) }

    init {
        lifecycleScope.launchWhenCreated {
            viewModel.fetchInvoice()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        viewModel.errorString.observe(this, Observer(this::toast))

        binding.buttonWait.setOnClickListener {
            binding.buttonWait.setBackgroundColor(colorPrimary)
            binding.buttonWait.setStrokeColorResource(R.color.colorPrimary)
            binding.buttonWait.setTextColor(Color.WHITE)
            binding.buttonPaid.setBackgroundColor(Color.WHITE)
            binding.buttonPaid.setStrokeColorResource(R.color.gray)
            binding.buttonPaid.setTextColor(colorGray)

            navController.navigate(R.id.action_global_klaspayTagihanMenunggu)
        }

        binding.buttonPaid.setOnClickListener {
            binding.buttonPaid.setBackgroundColor(colorPrimary)
            binding.buttonPaid.setStrokeColorResource(R.color.colorPrimary)
            binding.buttonPaid.setTextColor(Color.WHITE)
            binding.buttonWait.setBackgroundColor(Color.WHITE)
            binding.buttonWait.setStrokeColorResource(R.color.gray)
            binding.buttonWait.setTextColor(colorGray)

            navController.navigate(R.id.action_global_klaspayTagihanDibayar)
        }
    }

    override fun onBackPressed() {
        finish()
    }
}