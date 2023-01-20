package id.onklas.app.pages.pembayaran

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import id.onklas.app.R
import id.onklas.app.databinding.PaymentDetailPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class PaymentDetailPage : Privatepage() {

    private val binding by lazy { PaymentDetailPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<PaymentViewModel> { component.paymentVmFactory }
    private val trxId by lazy { intent.getStringExtra("trx_id").orEmpty() }
    private val allowPay by lazy { intent.getBooleanExtra("allow_pay", false) }
    private val allowCancel by lazy { intent.getBooleanExtra("allow_cancel", false) }

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

        lifecycleScope.launchWhenCreated {
            viewmodel.getDetailTransaction(
                trxId, this@PaymentDetailPage::setView
            ) {
                loading(msg = "menampilkan data")
                viewmodel.fetchDetailTransaction(
                    trxId,
                    this@PaymentDetailPage::setView,
                    this@PaymentDetailPage::showError
                )
                dismissloading()
            }
        }
    }

    private fun setView(item: PaymentInvoice) {
        val topupLabel = if (item.type.equals("topup", true)) "top up" else ""
        val adminLabel = if (item.channel.equals("alfamart", true)) "Minimarket" else "Bank"

        binding.rvCheckout.adapter = RowAdapter(
            arrayListOf(
                "Jenis Pembayaran" to item.type,
                "ID Transaksi" to item.trx_id,
                "Tanggal $topupLabel" to item.created_at_label,
                "Status $topupLabel" to item.status,
                "Metode Bayar" to item.channel_label,
                "Jumlah $topupLabel" to "Rp ${viewmodel.stringUtil.formatCurrency2(item.amount)}",
                "Biaya Layanan" to "Rp ${viewmodel.stringUtil.formatCurrency2(item.fee_service)}",
                "Admin $adminLabel" to "Rp ${viewmodel.stringUtil.formatCurrency2(item.fee_admin)}"
            ).apply {
                if (allowCancel)
                    add(3, "Tanggal expired" to item.expired_at_label)

                if (item.paid_at_label.isNotEmpty())
                    add(4, "Tanggal dibayar" to item.paid_at_label)

                if (item.fee_admin == 0) removeLast()
            }
        )
        binding.rvCheckout.addItemDecoration(
            DividerItemDecoration(this@PaymentDetailPage, OrientationHelper.VERTICAL)
        )

        binding.info.visibility =
            if (item.channel.equals("alfamart", true)) View.VISIBLE else View.GONE

//                binding.labelTotal.text = "Total $topupLabel"
        binding.textTotal.text = viewmodel.stringUtil.formatCurrency2(item.total)

        if (allowCancel) {
            binding.buttonCancel.visibility = View.VISIBLE
            binding.buttonCancel.setOnClickListener {
                ConfirmPinPage.getPin(this@PaymentDetailPage)
            }
        } else {
            binding.buttonCancel.visibility = View.GONE
        }

        if (allowPay) {
            binding.buttonPay.visibility = View.VISIBLE
            binding.buttonPay.setOnClickListener {
                PaymentGuidePage.open(this@PaymentDetailPage, trxId)
            }
        } else {
            binding.buttonPay.visibility = View.GONE
        }
    }

    private fun showError() {
        AlertDialog.Builder(this@PaymentDetailPage, R.style.DialogTheme)
            .setTitle("Perhatian")
            .setMessage("Data transaksi tidak ditemukan")
            .setPositiveButton("Kembali") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ConfirmPinPage.RC && resultCode == RESULT_OK) {
            lifecycleScope.launch {
                loading(msg = "memproses pembatalan")
                val res = viewmodel.cancelInvoice(trxId, data?.getStringExtra("pin"))
                dismissloading()

                if (res) {
                    viewmodel.db.payment().delete(trxId)
                    setResult(RESULT_OK)
                    finish()
                }
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {

        const val RC = 2801
        fun open(
            activity: Activity,
            trxId: String,
            allowPay: Boolean = false,
            allowCancel: Boolean = false
        ) {
            Timber.e("open detail trx: $trxId -- allowPay: $allowPay -- allowCancel: $allowCancel")
            activity.startActivityForResult(
                Intent(
                    activity,
                    PaymentDetailPage::class.java
                )
                    .putExtra("trx_id", trxId)
                    .putExtra("allow_pay", allowPay)
                    .putExtra("allow_cancel", allowCancel),
                RC
            )
        }
    }
}