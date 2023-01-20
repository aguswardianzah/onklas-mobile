package id.onklas.app.pages.ppob

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.PpobCheckoutPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.pembayaran.ConfirmPinPage
import id.onklas.app.pages.pembayaran.RowAdapter
import id.onklas.app.pages.pembayaran.SuccessPayPage
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PpobCheckoutPage : Privatepage() {

    private val binding by lazy { PpobCheckoutPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<PpobViewModel> { component.ppobVmFactory }
    private lateinit var item: PpobTransaction
    private val dividerDecoration by lazy {
        object : DividerItemDecoration(
            this,
            OrientationHelper.VERTICAL
        ) {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                if (parent.getChildAdapterPosition(view) == state.itemCount - 1)
                    return
                else
                    super.getItemOffsets(outRect, view, parent, state)
            }
        }
    }

    init {
        lifecycleScope.launchWhenCreated {
            try {
                viewmodel.klaspayBalance.postValue(viewmodel.apiService.klaspayWallet().data.balance)
            } catch (e: Exception) {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        }

        trxId = intent.getStringExtra("trxId").orEmpty()

        binding.lifecycleOwner = this
        binding.viewmodel = viewmodel

        lifecycleScope.launchWhenCreated {
            loading(msg = "mohon tunggu")
            val row = viewmodel.db.ppob().getByCreatedAt(createdAt) ?: viewmodel.db.ppob()
                .getById(trxId)
            Timber.e("checkout data: $row")

            row?.let {
                item = it

                binding.productImg.setImageResource(it.productIcon)
                binding.productName.text = it.productLabel

                generateProductInfo(it)
                generatePaymentInfo(it)

                binding.textTotal.text = viewmodel.stringUtil.formatCurrency2(it.totalAmount)

                binding.buttonPay.setOnClickListener {
                    ConfirmPinPage.getPin(this@PpobCheckoutPage)
                }

                binding.cashbackValue = it.cashback

                binding.executePendingBindings()
            } ?: showError()
            dismissloading()
        }

        viewmodel.errorString.observe(this, Observer(this::toast))
    }

    override fun onBackPressed() {
        lifecycleScope.launch {
            try {
                loading(msg = "mohon tunggu")
                viewmodel.db.ppob().delete(item)
                dismissloading()
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                finish()
            }
        }
    }

    private fun showError() {
        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("Perhatian")
            .setMessage("Data transaksi tidak ditemukan")
            .setPositiveButton("Kembali") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private fun generateProductInfo(item: PpobTransaction) {
        binding.rvProduct.adapter = RowAdapter(
            when (item.productType) {
                PpobPulsa -> when (item.productSubType) {
                    PulsaPrabayar -> arrayListOf(
                        "No. Handphone" to item.custId,
                        "Provider" to item.productProvider,
                        "Jenis Layanan" to item.productId
                    )
                    PulsaPascabayar -> arrayListOf(
                        "Nomor Telpon" to item.custId,
                        "Nama Lengkap" to item.custName,
                        "Jenis Layanan" to item.productName
                    )
                    PulsaPaket -> arrayListOf()
                    PulsaSms -> arrayListOf()
                    else -> ArrayList()
                }
                PpobGame -> arrayListOf(
                    "ID Player" to item.custId,
                    "Jenis Layanan" to item.productProvider,
                    "Nama Voucher" to item.productName
                )
                PpobListrik -> arrayListOf(
                    "ID Pelanggan" to item.custId,
                    "Nama Lengkap" to item.custName,
                    "Jenis Layanan" to item.productName
                )
                PpobAir -> arrayListOf(
                    "Nama PAM" to item.productProvider,
                    "ID Pelanggan" to item.custId,
                    "Stand Meter" to item.custRef2,
                    "Nama Lengkap" to item.custName,
                    "Jenis Layanan" to item.productLabel
                )
                PpobInternet -> arrayListOf(
                    "No. Internet/TV Kabel" to item.custId,
                    "Nama Pelanggan" to item.custName,
                    "Provider" to item.productProvider,
                    "Jenis Layanan" to item.productSubType
                )
                PpobBpjs -> arrayListOf(
                    "No. VA Keluarga" to item.custId,
                    "Nama Pelanggan" to item.custName,
//                    "Jumlah Peserta" to item.productProvider,
                    "Bayar Hingga" to SimpleDateFormat("MMMM yyyy", Locale("id")).format(
                        Calendar.getInstance()
                            .apply { add(Calendar.MONTH, item.custRef1.toInt()) }
                    ),
                    "Periode" to item.custRef1,
                    "Jenis Layanan" to item.productSubType
                )
                else -> ArrayList()
            }
        )
        binding.rvProduct.addItemDecoration(dividerDecoration)
    }

    private fun generatePaymentInfo(item: PpobTransaction) {
        binding.rvPayment.adapter = RowAdapter(
            when (item.productType) {
                PpobPulsa -> when (item.productSubType) {
                    PulsaPascabayar -> arrayListOf("Bulan/Tahun" to "")
                    else -> ArrayList()
                }

                PpobListrik -> if (item.productSubType == ListrikTagihan && item.custRef1.isNotEmpty())
                    arrayListOf("Bulan/Tahun" to item.custRef1)
                else ArrayList()

                PpobAir -> arrayListOf("Bulan/Tahun" to item.custRef1)
                else -> ArrayList()
            }.apply {
                addAll(
                    arrayListOf(
                        "Harga" to "Rp " + viewmodel.stringUtil.formatCurrency2(item.amount),
                        "Biaya Layanan" to "Rp " + viewmodel.stringUtil.formatCurrency2(item.feeService),
                        "Admin" to "Rp " + viewmodel.stringUtil.formatCurrency2(item.feeAdmin),
                        "Diskon" to "0"
                    )
                )
            }
        )
        binding.rvPayment.addItemDecoration(dividerDecoration)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ConfirmPinPage.RC && resultCode == RESULT_OK && data != null) {
            lifecycleScope.launchWhenCreated {
                loading(msg = "Memproses pembelian/pembayaran")
                val newTrxId = viewmodel.processPay(item, data.getStringExtra("pin"))
                dismissloading()

                if (newTrxId.isNotEmpty()) {
                    SuccessPayPage.open(
                        this@PpobCheckoutPage,
                        true,
                        if (item.productSubType.isNotEmpty()) item.productSubType else item.productLabel,
                        viewmodel.stringUtil.formatCurrency2(item.totalAmount),
                        newTrxId,
                        isSuccess = !arrayOf(
                            PulsaPrabayar,
                            PulsaSms,
                            PulsaPaket
                        ).contains(item.productSubType)
                    )

                    setResult(RESULT_OK)
                    finish()
                }
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private var trxId = ""

    private val createdAt by lazy { intent.getLongExtra("createdAt", 0) }

    companion object {

        const val RC = 820
        fun openByCreatedAt(fragment: Fragment, createdAt: Long) {
            fragment.startActivityForResult(
                Intent(
                    fragment.requireContext(),
                    PpobCheckoutPage::class.java
                ).putExtra("createdAt", createdAt), RC
            )
        }

        fun openByTrxId(fragment: Fragment, trxId: String) {
            fragment.startActivityForResult(
                Intent(
                    fragment.requireContext(),
                    PpobCheckoutPage::class.java
                ).putExtra("trxId", trxId), RC
            )
        }

        fun openByTrxId(activity: Activity, trxId: String) {
            activity.startActivityForResult(
                Intent(activity, PpobCheckoutPage::class.java).putExtra("trxId", trxId), RC
            )
        }
    }
}