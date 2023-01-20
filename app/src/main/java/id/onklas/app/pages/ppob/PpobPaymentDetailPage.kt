package id.onklas.app.pages.ppob

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.PpobPaymentDetailPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.pembayaran.RowAdapter
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class PpobPaymentDetailPage : Privatepage() {

    private val binding by lazy { PpobPaymentDetailPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<PpobViewModel> { component.ppobVmFactory }
    val dateFormat by lazy { SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id")) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        }

        viewmodel.errorString.observe(this, Observer(this::toast))
        lifecycleScope.launchWhenCreated {
            try {
                loading(msg = "menampilkan data")
                setView(viewmodel.fetchDetail(trxId)!!)
            } catch (e: Exception) {
                try {
                    setView(viewmodel.db.ppob().getById(trxId)!!)
                } catch (e: Exception) {
                    showError()
                }
            } finally {
                dismissloading()
            }
        }
    }

    private val icCopy by lazy {
        BitmapDrawable(
            resources,
            Bitmap.createScaledBitmap(
                (ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_copy,
                    null
                ) as BitmapDrawable).bitmap,
                resources.getDimensionPixelSize(R.dimen._16sdp),
                resources.getDimensionPixelSize(R.dimen._16sdp),
                true
            )
        )
    }

    @SuppressLint("SetTextI18n")
    private fun setView(item: PpobTransaction) {
        Timber.e("detail trx: $item")
        binding.productName.text = item.productLabel
        binding.productImg.setImageResource(item.productIcon)

        binding.rvCheckout.adapter = RowAdapter(
            arrayListOf(
                "Tanggal Transaksi" to dateFormat.format(Date().apply {
                    time = item.createdAt
                }),
                "Status Pembayaran" to item.status,
                "ID Transaksi" to item.trxId,
                "Diskon" to "0",
                "Admin Bank" to "Rp " + viewmodel.stringUtil.formatCurrency2(item.feeAdmin),
                "Biaya Layanan" to "Rp " + viewmodel.stringUtil.formatCurrency2(item.feeService)
            ).apply {
                addAll(3, generateTrxData(item))
            },
            icCopy
        ) {
            viewmodel.intentUtil.copyText(this, it)
            toast("Text tersalin")
        }

        binding.rvCheckout.addItemDecoration(
            object : DividerItemDecoration(
                this@PpobPaymentDetailPage,
                OrientationHelper.VERTICAL
            ) {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    if (parent.getChildAdapterPosition(view) != state.itemCount - 1)
                        super.getItemOffsets(outRect, view, parent, state)
                }
            }
        )

        binding.textTotal.text = viewmodel.stringUtil.formatCurrency2(item.totalAmount)

        if (item.cashback > 0) {
            binding.cashback.visibility = View.VISIBLE
            binding.cashbackLabel.visibility = View.VISIBLE
            binding.cashback.text = "Rp ${viewmodel.stringUtil.formatCurrency2(item.cashback)}"
        } else {
            binding.cashback.visibility = View.GONE
            binding.cashbackLabel.visibility = View.GONE
        }
    }

    private fun generateTrxData(item: PpobTransaction) = when (item.productType) {
        PpobPulsa -> when (item.productSubType) {
            PulsaPascabayar -> listOf(
                "No. Handphone" to item.custId,
                "Nama Lengkap" to item.custName,
                "Provider" to item.productProvider,
                "Bulan/Tahun" to "",
                "Jumlah Tagihan" to "Rp " + viewmodel.stringUtil.formatCurrency2(item.amount)
            )
            else -> listOf(
                "No. Handphone" to item.custId,
                "Provider" to item.productProvider,
                "Jenis Layanan" to item.productSubType,
                "Harga" to "Rp " + viewmodel.stringUtil.formatCurrency2(item.amount)
            )
        }
        PpobGame -> listOf(
            "ID Player" to item.custId,
            "Jenis Layanan" to "Voucher Game",
            "Nama Voucher" to item.productName,
            "SN" to item.custRef1,
            "Harga" to "Rp " + viewmodel.stringUtil.formatCurrency2(item.amount)
        )
        PpobListrik -> when (item.productSubType) {
            ListrikToken -> listOf(
                "ID Pelanggan" to item.custId,
                "Nama Lengkap" to item.custName,
                "Jenis Layanan" to item.productName,
                "Serial Token" to item.custRef1,
                "Harga" to "Rp " + viewmodel.stringUtil.formatCurrency2(item.amount)
            )
            ListrikTagihan -> listOf(
                "ID Pelanggan" to item.custId,
                "Nama Lengkap" to item.custName,
                "Jenis Layanan" to item.productName,
                "Bulan/Tahun" to item.custRef1,
                "Harga" to "Rp " + viewmodel.stringUtil.formatCurrency2(item.amount)
            )
            else -> emptyList()
        }
        PpobAir -> listOf(
            "Nama PAM" to item.productName,
            "ID Pelanggan" to item.custId,
            "Stand Meter" to item.custRef2,
            "Nama Lengkap" to item.custName,
            "Jenis Layanan" to item.productLabel,
            "Bulan/Tahun" to item.custRef1,
            "Jumlah Tagihan" to "Rp " + viewmodel.stringUtil.formatCurrency2(item.amount)
        )
        PpobInternet -> listOf(
            "No Internet/TV Kabel" to item.custId,
            "Nama Pelanggan" to item.custName,
            "Penyedia" to item.productName,
            "Jenis Layanan" to item.productSubType,
            "Harga" to "Rp " + viewmodel.stringUtil.formatCurrency2(item.amount)
        )
        PpobBpjs -> listOf(
            "No. VA Keluarga" to item.custId,
            "Nama Pelanggan" to item.custName,
//                    "Jumlah Peserta" to item.productProvider,
            "Bayar Hingga" to SimpleDateFormat("MMMM yyyy", Locale("id")).format(
                Calendar.getInstance()
                    .apply { add(Calendar.MONTH, item.custRef1.toInt()) }
            ),
            "Periode" to item.custRef1,
            "Jenis Layanan" to item.productSubType,
            "Harga" to "Rp " + viewmodel.stringUtil.formatCurrency2(item.amount)
        )
        else -> emptyList()
    }

    private fun showError() {
        AlertDialog.Builder(this@PpobPaymentDetailPage, R.style.DialogTheme)
            .setTitle("Perhatian")
            .setMessage("Data transaksi tidak ditemukan")
            .setPositiveButton("Kembali") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private val trxId by lazy { intent.getStringExtra("trxId").orEmpty() }

    companion object {

        const val RC = 182
        fun open(
            activity: Activity,
            trxId: String
        ) {
            activity.startActivityForResult(
                Intent(activity, PpobPaymentDetailPage::class.java)
                    .putExtra("trxId", trxId), RC
            )
        }
    }
}