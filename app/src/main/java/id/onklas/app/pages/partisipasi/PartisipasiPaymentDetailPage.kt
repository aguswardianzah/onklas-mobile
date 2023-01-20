package id.onklas.app.pages.partisipasi

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.CheckoutItemBinding
import id.onklas.app.databinding.CheckoutItemRowBinding
import id.onklas.app.databinding.PartisipasiPaymentDetailPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.pembayaran.RowAdapter
import java.text.SimpleDateFormat
import java.util.*

class PartisipasiPaymentDetailPage : Privatepage() {

    private val binding by lazy { PartisipasiPaymentDetailPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PartisipasiViewModel> { component.partisipasiVmFactory }
    private val id by lazy { intent.getStringExtra("id").orEmpty() }
    private val info by lazy { intent.getStringExtra("product_info").orEmpty() }
    val dateFormat by lazy { SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id")) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.rvCheckout.addItemDecoration(
            DividerItemDecoration(
                this,
                OrientationHelper.VERTICAL
            )
        )
        binding.rvCheckout.adapter = adapter

        lifecycleScope.launchWhenCreated {
            viewModel.payment(id)?.let { item ->
                binding.rvCheckout.addItemDecoration(
                    object : DividerItemDecoration(
                        this@PartisipasiPaymentDetailPage,
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

                val amountLabel = viewModel.stringUtil.formatCurrency2(item.amount)

                items.clear()
                items.addAll(
                    mutableListOf(
                        Triple("Tanggal Transaksi", dateFormat.format(item.date), false),
                        Triple("Status Pembayaran", "Berhasil", false),
                        Triple("ID Transaksi", item.id, false),
                        Triple("Metode bayar", item.channel_name, false),
                        Triple("Pembayaran", info, true),
                        Triple("Harga", "Rp $amountLabel", false)
                    )
                )
                adapter.notifyDataSetChanged()

                binding.textTotal.text = amountLabel
            }
        }
    }

    private val items: MutableList<Triple<String, String, Boolean>> = mutableListOf()

    private val adapter by lazy {
        object : RecyclerView.Adapter<Viewholder>() {

            override fun getItemViewType(position: Int): Int = if (items[position].third) 1 else 0

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                if (viewType == 0)
                    SingleRow(parent)
                else
                    DoubleRow(parent)

            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(items[position])

            override fun getItemCount(): Int = items.size
        }
    }

    private abstract class Viewholder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: Triple<String, String, Boolean>)
    }

    private inner class SingleRow(
        parent: ViewGroup,
        val binding: CheckoutItemBinding = CheckoutItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : Viewholder(binding.root) {
        override fun bind(item: Triple<String, String, Boolean>) {
            binding.key = item.first
            binding.values = item.second
            binding.executePendingBindings()
        }
    }

    private inner class DoubleRow(
        parent: ViewGroup,
        val binding: CheckoutItemRowBinding = CheckoutItemRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : Viewholder(binding.root) {
        override fun bind(item: Triple<String, String, Boolean>) {
            binding.key = item.first
            binding.values = item.second
            binding.executePendingBindings()
        }
    }
}