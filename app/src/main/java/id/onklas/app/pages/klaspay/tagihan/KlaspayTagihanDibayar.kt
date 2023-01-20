package id.onklas.app.pages.klaspay.tagihan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.KlaspayTagihanDibayarItemBinding
import id.onklas.app.databinding.KlaspayTagihanMenungguBinding
import id.onklas.app.di.component
import id.onklas.app.pages.pembayaran.PaymentDetailPage
import id.onklas.app.pages.pembayaran.PaymentInvoice
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch
import java.util.*

class KlaspayTagihanDibayar : Fragment() {

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
        binding.titleTagihanEmpty.text = "Belum Ada Tagihan"

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

        viewModel.paidList.observe(viewLifecycleOwner, Observer(pageAdapter::submitList))

        viewModel.loadingPaid.observe(
            viewLifecycleOwner,
            Observer(binding.swipeRefresh::setRefreshing)
        )

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewModel.hasMorePaid = true
                viewModel.loadingPaid.value = false
                viewModel.fetchInvoice(true)
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
        val binding: KlaspayTagihanDibayarItemBinding = KlaspayTagihanDibayarItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PaymentInvoice) {
            binding.item = item
            binding.textDetail.setOnClickListener {
                PaymentDetailPage.open(
                    requireActivity(),
                    item.trx_id
//                    item.expired_at?.before(Date()) == true,
//                    item.cancelable
                )
            }
            binding.executePendingBindings()
        }
    }
}