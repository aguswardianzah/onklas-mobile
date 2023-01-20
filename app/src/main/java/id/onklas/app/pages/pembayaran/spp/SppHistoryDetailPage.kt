package id.onklas.app.pages.pembayaran.spp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import id.onklas.app.databinding.SppHistoryDetailPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.pembayaran.CheckoutPage
import id.onklas.app.pages.pembayaran.CheckoutResultPage
import id.onklas.app.pages.pembayaran.RowAdapter
import id.onklas.app.pages.PageDialogFragment

class SppHistoryDetailPage(private val sppInvoicePayment: SppInvoicePayment) :
    PageDialogFragment() {

    private lateinit var binding: SppHistoryDetailPageBinding
    private val viewmodel by activityViewModels<SppViewModel> { component.sppVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        SppHistoryDetailPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val items = ArrayList<Pair<String, String>>()
        items.add("Jenis Pembayaran" to sppInvoicePayment.payment.jenis)
        items.add("ID Transaksi" to sppInvoicePayment.payment.reffId)
        items.add("Periode" to sppInvoicePayment.invoice.joinToString(", ") { it.name })
        items.add("Tanggal Dibayar" to sppInvoicePayment.payment.paidAt)
        items.add("Status Pembayaran" to if (sppInvoicePayment.payment.isPaid) "Berhasil" else if (sppInvoicePayment.payment.isExpired) "Kadaluarsa" else "Proses")
        items.add("Metode Pembayaran" to sppInvoicePayment.payment.paymentMethod)
        items.add("Kode Pembayaran" to sppInvoicePayment.payment.paymentCode)
        items.add("Harga" to "Rp" + viewmodel.stringUtil.formatCurrency(sppInvoicePayment.payment.total))
        items.add("Diskon" to "0%")
        items.add(
            "Total Tagihan (${sppInvoicePayment.invoice.size})" to viewmodel.stringUtil.formatCurrency(
                sppInvoicePayment.payment.total
            )
        )

        binding.toolbar.setNavigationOnClickListener { dismiss() }

        binding.rvDetail.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                OrientationHelper.VERTICAL
            )
        )
        binding.rvDetail.adapter = RowAdapter(items)

        binding.btnCheckout.visibility =
            if (sppInvoicePayment.invoice.isNotEmpty() && !sppInvoicePayment.payment.isExpired) View.VISIBLE else View.GONE

        val keys = ArrayList<String>()
        val values = ArrayList<String>()
        val spp_ids = ArrayList<String>()
        var total = 0.0
        sppInvoicePayment.invoice.forEach {
            spp_ids.add(it.id.toString())
            keys.add(it.name)
            values.add("Rp" + viewmodel.stringUtil.formatCurrency(it.total_fee))
            total += it.total_fee
        }

        binding.btnCheckout.setOnClickListener {
            if (sppInvoicePayment.payment.paymentUrl.isNotEmpty()) {
                CheckoutResultPage(sppInvoicePayment.payment.paymentUrl).show(
                    requireActivity().supportFragmentManager,
                    ""
                )
            } else {
                CheckoutPage.open(
                    requireActivity(),
                    sppInvoicePayment.payment.jenis,
                    keys.toTypedArray(),
                    values.toTypedArray(),
                    "Rp" + viewmodel.stringUtil.formatCurrency(total),
                    spp_ids.toTypedArray(),
                    total.toInt()
                )
            }
            dismiss()
        }
    }
}