package id.onklas.app.pages.pembayaran.spp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.SppHistoryItemBinding
import id.onklas.app.databinding.SppHistoryPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch

class SppHistoryPage : Privatepage() {

    private val binding by lazy { SppHistoryPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<SppViewModel> { component.sppVmFactory }
    private var firstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                firstLoad = true
                viewmodel.fetchPaymentSpp()
                binding.swipeRefresh.isRefreshing = false
            }
        }

        binding.rvHistory.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(
                    R.dimen._8sdp
                ),
                includeTop = true
            )
        )
        binding.rvHistory.adapter = adapter

        viewmodel.listPayment.observe(this, {
            adapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager =
                        (binding.rvHistory.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvHistory.scrollToPosition(position)
                    }
                } else {
                    binding.rvHistory.scrollToPosition(0)
                    firstLoad = false
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        })

        binding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            viewmodel.pagePayment = 0
            viewmodel.fetchPaymentSpp()
        }
    }

    private val adapter by lazy {
        object : PagingAdapter<SppInvoicePayment, Viewholder>(
            object : DiffUtil.ItemCallback<SppInvoicePayment>() {
                override fun areItemsTheSame(
                    oldItem: SppInvoicePayment,
                    newItem: SppInvoicePayment
                ): Boolean =
                    oldItem.payment.id == newItem.payment.id

                override fun areContentsTheSame(
                    oldItem: SppInvoicePayment,
                    newItem: SppInvoicePayment
                ): Boolean =
                    oldItem == newItem
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
        val binding: SppHistoryItemBinding = SppHistoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SppInvoicePayment) {
            binding.item = item.payment
            binding.allowPay = item.invoice.isNotEmpty()
            binding.stringUtil = viewmodel.stringUtil
            binding.executePendingBindings()

            binding.detail.setOnClickListener {
                SppHistoryDetailPage(item).show(
                    supportFragmentManager, ""
                )
            }
        }
    }
}