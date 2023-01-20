package id.onklas.app.pages.pembayaran

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import id.onklas.app.databinding.CheckoutPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class CheckoutPage : Privatepage() {

    private val binding by lazy { CheckoutPageBinding.inflate(layoutInflater) }
    private val items by lazy { ArrayList<Pair<String, String>>() }
    private val viewmodel by viewModels<PaymentViewModel> { component.paymentVmFactory }
    private val type by lazy { intent.getStringExtra("type").orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        items.add("Jenis pembayaran" to type)
        val keys = intent.getStringArrayExtra("keys")
        val values = intent.getStringArrayExtra("values")
        keys?.forEachIndexed { index, s ->
            items.add(s to values?.get(index).orEmpty())
        }
        items.add(
            "Total Tagihan: (${keys?.size ?: 0}x)" to intent.getStringExtra("total").orEmpty()
        )

        binding.metode.setOnClickListener {
            SelectPaymentPage {
                viewmodel.selectedPaymentType.postValue(it)
            }.show(supportFragmentManager, "payment_type")
        }

        viewmodel.selectedPaymentType.observe(this, {
            binding.metode.text = it.name
            binding.btnCheckout.isEnabled = true
        })

        binding.rvCheckout.adapter = RowAdapter(items)
        binding.rvCheckout.addItemDecoration(
            DividerItemDecoration(this, OrientationHelper.VERTICAL)
        )

        binding.btnCheckout.setOnClickListener {

            if (type.equals("spp", true))
                startActivityForResult(
                    Intent(this, PaymentTypePage::class.java).putExtras(intent),
                    8392
                )
            else
                startActivityForResult(
                    Intent(this, ConfirmPinPage::class.java).putExtras(intent),
                    8392
                )


//            lifecycleScope.launch {
//                loading(msg = "Memproses pembayaran...")
//                intent.getStringArrayExtra("others")?.let { ids ->
//                    val payResponse = viewmodel.pay(ids)
//                    dismissloading()
//                    if (payResponse?.rc == "00") {
//                        CheckoutResultPage(payResponse.data.spi_status_url).show(
//                            supportFragmentManager,
//                            ""
//                        )
//                    } else
//                        toast(
//                            (payResponse?.rd ?: viewmodel.errorString.value)
//                                ?: "Terjadi kesalahan dalam proses pembayaran, silahkan ulangi beberapa saat lagi"
//                        )
//                } ?: run {
//                    alert(
//                        msg = "Terjadi kesalahan dalam proses pembayaran, silahkan ulangi beberapa saat lagi",
//                        okClick = { finish() })
//                }
//            }
        }

        viewmodel.errorString.observe(this, Observer(this::toast))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 8392 && resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {

        const val RC = 3281
        fun open(
            activity: Activity,
            type: String,
            keys: Array<String>,
            values: Array<String>,
            total: String,
            others: Array<String> = emptyArray(),
            totalInt: Int
        ) {
            activity.startActivityForResult(
                Intent(activity, CheckoutPage::class.java)
                    .putExtra("type", type)
                    .putExtra("total", total)
                    .putExtra("keys", keys)
                    .putExtra("values", values)
                    .putExtra("total", total)
                    .putExtra("amount", totalInt)
                    .putExtra("others", others),
                RC
            )
        }
    }
}