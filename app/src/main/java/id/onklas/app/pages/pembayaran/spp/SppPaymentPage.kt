package id.onklas.app.pages.pembayaran.spp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.SppPaymentPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import kotlinx.coroutines.launch

class SppPaymentPage : Privatepage() {

    private val binding by lazy { SppPaymentPageBinding.inflate(layoutInflater) }
    private val navController by lazy { findNavController(R.id.page_container) }
    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private val viewmodel by viewModels<SppViewModel> { component.sppVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewmodel.apiService.sppInvoice()
        }

        binding.lifecycleOwner = this

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { supportFinishAfterTransition() }
        }

        binding.viewmodel = viewmodel

        binding.btnPaid.setOnClickListener {
            navController.navigate(R.id.action_global_sppPaidPage)
            binding.btnPaid.setBackgroundColor(colorPrimary)
            binding.btnPaid.setStrokeColorResource(R.color.colorPrimary)
            binding.btnPaid.setTextColor(Color.WHITE)
            binding.btnTagihan.setBackgroundColor(Color.WHITE)
            binding.btnTagihan.setStrokeColorResource(R.color.gray)
            binding.btnTagihan.setTextColor(colorGray)
        }
        binding.btnTagihan.setOnClickListener {
            navController.navigate(R.id.action_global_sppTagihanPage)
            binding.btnTagihan.setBackgroundColor(colorPrimary)
            binding.btnTagihan.setStrokeColorResource(R.color.colorPrimary)
            binding.btnTagihan.setTextColor(Color.WHITE)
            binding.btnPaid.setBackgroundColor(Color.WHITE)
            binding.btnPaid.setStrokeColorResource(R.color.gray)
            binding.btnPaid.setTextColor(colorGray)
        }

        viewmodel.errorString.observe(this, Observer(this::toast))

        if (intent.hasExtra("page") && intent.getStringExtra("page") == "paid")
            binding.btnPaid.performClick()

        binding.executePendingBindings()
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launchWhenCreated {
            viewmodel.balance.postValue(viewmodel.apiService.klaspayWallet().data.balance?.toInt())
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_spp, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        startActivity(Intent(this, SppHistoryPage::class.java))
//        return super.onOptionsItemSelected(item)
//    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }
}