package id.onklas.app.pages.klaspay.tagihan

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.KlaspayAktivasiSuccessBinding
import id.onklas.app.databinding.KlaspayTagihanMenungguBinding
import id.onklas.app.databinding.KlaspayTagihanMenungguItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.pembayaran.ConfirmPinPage
import id.onklas.app.pages.pembayaran.PaymentDetailPage
import id.onklas.app.pages.pembayaran.PaymentGuidePage
import id.onklas.app.pages.pembayaran.PaymentInvoice
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class KlaspayTagihanMenunggu : Fragment() {

    private lateinit var binding: KlaspayTagihanMenungguBinding
    private val viewModel by activityViewModels<KlaspayTagihanViewModel> { component.KlaspayTagihanVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        KlaspayTagihanMenungguBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.listItem.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(
                    R.dimen._8sdp
                ),
                includeTop = true,
                includeBottom = true
            )
        )
        binding.listItem.adapter = pageAdapter

        viewModel.unpaidList.observe(viewLifecycleOwner, Observer(pageAdapter::submitList))

        viewModel.loadingList.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
            if (!it)
                binding.listItem.scrollToPosition(0)
        }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewModel.hasMoreUnpaid = true
                viewModel.loadingList.value = false
                viewModel.fetchInvoice()
            }
        }
    }

    private val pageAdapter by lazy {
        object : PagingAdapter<PaymentInvoice, Viewholder>(
            object : DiffUtil.ItemCallback<PaymentInvoice>() {
                override fun areItemsTheSame(
                    oldItem: PaymentInvoice,
                    newItem: PaymentInvoice
                ): Boolean = newItem.trx_id == oldItem.trx_id

                override fun areContentsTheSame(
                    oldItem: PaymentInvoice,
                    newItem: PaymentInvoice
                ): Boolean = oldItem == newItem
            }
        ) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun bindItemViewholder(holder: Viewholder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: Viewholder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                getItem(position)?.let { holder.bind(it) }
            }
        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: KlaspayTagihanMenungguItemBinding = KlaspayTagihanMenungguItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PaymentInvoice) {
            binding.item = item
            binding.isExpired = item.expired_at?.before(Date())
            binding.textDetail.setOnClickListener {
                Timber.e("onclick, expired at: ${item.expired_at_label}")
                PaymentDetailPage.open(
                    requireActivity(),
                    item.trx_id,
                    item.expired_at?.before(Date()) == false,
                    item.cancelable
                )
            }
            binding.buttonPay.setOnClickListener {
                PaymentGuidePage.open(requireActivity(), item.trx_id)
            }
            binding.buttonCancel.setOnClickListener {
                trxIdCancel = item.trx_id
                ConfirmPinPage.getPin(this@KlaspayTagihanMenunggu)
            }
            binding.executePendingBindings()
        }
    }

    var trxIdCancel = ""

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ConfirmPinPage.RC && resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                val loading = ProgressDialog.show(requireActivity(), "", "memproses pembatalan")
                val res = viewModel.cancelInvoice(trxIdCancel, data?.getStringExtra("pin"))
                loading.dismiss()

                if (res) {
                    val dialogBinding =
                        KlaspayAktivasiSuccessBinding.inflate(layoutInflater).apply {
                            labelTitle.text = "Berhasil Dibatalkan"
                            labelDescription.text = "Tagihan berhasil dibatalkan"
                            buttonTopUp.text = "Oke, Terima Kasih"
                            buttonLater.visibility = View.GONE
                        }
                    val dialog = AlertDialog.Builder(requireContext())
                        .setView(dialogBinding.root)
                        .show()
                        .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }
                    dialogBinding.buttonTopUp.setOnClickListener { dialog.dismiss() }
                }
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }
}