package id.onklas.app.pages.partisipasi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.PartisipasiAmountPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.pembayaran.PaymentTypePage
import kotlinx.coroutines.flow.collectLatest

class PartisipasiAmountPage : Privatepage() {

    private val binding by lazy { PartisipasiAmountPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PartisipasiViewModel> { component.partisipasiVmFactory }
    private val id by lazy { intent.getStringExtra("id").orEmpty() }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (id.isEmpty()) {
            alert(msg = "Kegiatan tidak ditemukan", okClick = this::finish)
            return
        }

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.editNominal.doAfterTextChanged {
            binding.btnConfirm.isEnabled = it.toString().isNotEmpty()
        }

        binding.btnConfirm.setOnClickListener { checkout() }

        loading(msg = "menampilkan detail kegiatan")

        lifecycleScope.launchWhenCreated {
            viewModel.detail(id).collectLatest {
                binding.item = it.item
                binding.date.text = viewModel.dateFormat.format(it.item.deadline)
            }
        }

        viewModel.errorString.observe(this, Observer(this::toast))

        viewModel.loadingDetail.observe(this) {
            if (!it)
                dismissloading()
        }

        viewModel.loadingPayBill.observe(this) {
            if (it)
                loading(msg = "Memproses pembayaran")
            else
                dismissloading()
        }

        viewModel.successPayBill.observe(this) {
            if (it) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun checkout() {
        startActivityForResult(
            Intent(
                this,
                PaymentTypePage::class.java
            ).putExtra("get_channel_only", true), 238
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 238 && resultCode == RESULT_OK && data != null)
            startActivityForResult(
                Intent(this, PartisipasiCheckoutPage::class.java)
                    .putExtras(intent)
                    .putExtra("product_info", binding.title.text)
                    .putExtra("amount", binding.editNominal.text.toString().toInt())
                    .putExtra("channel_code", data.getStringExtra("payment_code").orEmpty())
                    .putExtra("channel_name", data.getStringExtra("payment_name").orEmpty()), 237
            )
        else if (requestCode == 237 && resultCode == RESULT_OK)
            finish()
        else
            super.onActivityResult(requestCode, resultCode, data)
    }
}