package id.onklas.app.pages.partisipasi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.PartisipasiChecoutPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.klaspay.tagihan.KlaspayTagihanPage
import id.onklas.app.pages.pembayaran.ConfirmPinPage
import java.text.SimpleDateFormat
import java.util.*

class PartisipasiCheckoutPage : Privatepage() {

    val binding by lazy { PartisipasiChecoutPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PartisipasiViewModel> { component.partisipasiVmFactory }
    private val id by lazy { intent.getStringExtra("id").orEmpty() }
    private val productInfo by lazy { intent.getStringExtra("product_info").orEmpty() }
    private val channelCode by lazy { intent.getStringExtra("channel_code").orEmpty() }
    private val channelName by lazy { intent.getStringExtra("channel_name").orEmpty() }
    private val amount by lazy { intent.getIntExtra("amount", 0) }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.lifecycleOwner = this
        binding.viewmodel = viewModel

        binding.paymentType.text = productInfo
        binding.channel.text = channelName

        val amountformat = viewModel.stringUtil.formatCurrency2(amount)
        binding.amount.text = "Rp $amountformat"
        binding.textTotal.text = amountformat

        viewModel.errorString.observe(this, Observer(this::toast))

        viewModel.loadingPayBill.observe(this) {
            if (it)
                loading(msg = "Memproses pembayaran")
            else
                dismissloading()
        }

        viewModel.successPayBill.observe(this) {
            if (it) {
                if (channelCode.equals("klaspay", true)) {
                    startActivity(
                        Intent(
                            this@PartisipasiCheckoutPage,
                            PartisipasiSuccessPage::class.java
                        ).putExtras(intent).putExtra(
                            "date_label",
                            SimpleDateFormat("dd MMMM yyyy", Locale("id")).format(Date())
                        ).putExtra("amount_label", viewModel.stringUtil.formatCurrency2(amount))
                    )
                } else {
                    startActivity(Intent(this, KlaspayTagihanPage::class.java))
                }
                setResult(RESULT_OK)
                finish()
            }
        }

        binding.buttonPay.setOnClickListener { ConfirmPinPage.getPin(this) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ConfirmPinPage.RC && resultCode == RESULT_OK) {
            val pin = data?.getStringExtra("pin").orEmpty()
            viewModel.payBill(id, channelCode, amount, pin)
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launchWhenCreated {
            viewModel.balance.postValue(viewModel.api.klaspayWallet().data.balance)
        }
    }
}