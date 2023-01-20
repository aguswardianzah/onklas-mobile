package id.onklas.app.pages.partisipasi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.PartisipasiHistoryPageBinding
import id.onklas.app.databinding.PartisipasiPaymentHistoryItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.flow.collectLatest

class PartisipasiHistoryPage : Privatepage() {

    private val binding by lazy { PartisipasiHistoryPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PartisipasiViewModel> { component.partisipasiVmFactory }
    private val id by lazy { intent.getStringExtra("id").orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (id.isEmpty()) {
            alert(msg = "Kegiatan tidak ditemukan", okClick = this::finish)
            return
        }

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
        binding.stringUtil = viewModel.stringUtil

        binding.rvPayment.addItemDecoration(
            LinearSpaceDecoration(space = resources.getDimensionPixelSize(R.dimen._8sdp))
        )
        binding.rvPayment.adapter = paymentAdapter

        binding.executePendingBindings()

        loading(msg = "menampilkan riwayat pembayaran kegiatan")

        lifecycleScope.launchWhenCreated {
            viewModel.detail(id).collectLatest {
                binding.item = it.item
                listPayment.clear()
                listPayment.addAll(it.payment)
                paymentAdapter.notifyDataSetChanged()
                binding.executePendingBindings()
                dismissloading()
            }
        }
    }

    private val listPayment: MutableList<PartisipasiPayment> by lazy { mutableListOf() }
    private val paymentAdapter by lazy {
        object : RecyclerView.Adapter<PaymentVH>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentVH =
                PaymentVH(parent)

            override fun onBindViewHolder(holder: PaymentVH, position: Int) =
                holder.bind(listPayment[position])

            override fun getItemCount(): Int = listPayment.size
        }
    }

    private inner class PaymentVH(
        parent: ViewGroup,
        val binding: PartisipasiPaymentHistoryItemBinding = PartisipasiPaymentHistoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PartisipasiPayment) {
            binding.item = item
            binding.position = adapterPosition + 1
            binding.stringUtil = viewModel.stringUtil
            binding.dateFormat = viewModel.dateFormat

            binding.btnDetail.setOnClickListener {
                startActivity(
                    Intent(
                        this@PartisipasiHistoryPage,
                        PartisipasiPaymentDetailPage::class.java
                    ).putExtra("id", item.id).putExtra(
                        "product_info",
                        this@PartisipasiHistoryPage.binding.labelName.text
                    )
                )
            }

            binding.executePendingBindings()
        }
    }

}