package id.onklas.app.pages.partisipasi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.PartisipasiDetailPageBinding
import id.onklas.app.databinding.PartisipasiPaymentDetailItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.pembayaran.PaymentTypePage
import kotlinx.coroutines.flow.collectLatest

class PartisipasiDetailPage : Privatepage() {

    private val binding by lazy { PartisipasiDetailPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PartisipasiViewModel> { component.partisipasiVmFactory }
    private val id by lazy { intent.getStringExtra("id").orEmpty() }
    private var amount: Int = 0

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
        binding.showPayment = true

        binding.rvPayment.addItemDecoration(DividerItemDecoration(this, OrientationHelper.VERTICAL))
        binding.rvPayment.adapter = paymentAdapter

        binding.switchPayment.setOnClickListener {
            binding.showPayment = !binding.rvPayment.isShown
        }

        binding.paymentLabel.setOnClickListener {
            binding.showPayment = !binding.rvPayment.isShown
        }

        binding.executePendingBindings()

        loading(msg = "menampilkan detail kegiatan")

        viewModel.errorString.observe(this, Observer(this::toast))

        viewModel.loadingDetail.observe(this) {
            if (!it)
                dismissloading()
        }

        lifecycleScope.launchWhenCreated {
            viewModel.detail(id).collectLatest {
                binding.item = it.item
                amount = it.item.final_amount - it.item.current_amount
                binding.date.text = viewModel.dateFormat.format(it.item.deadline)

                if (it.payment.isNotEmpty()) {
                    listPayment.clear()
                    listPayment.addAll(it.payment)
                    paymentAdapter.notifyDataSetChanged()

                    binding.paymentLabel.visibility = View.VISIBLE
                    binding.switchPayment.visibility = View.VISIBLE
                    binding.dividerLabelPayment.visibility = View.VISIBLE
                    binding.rvPayment.visibility = View.VISIBLE
                    binding.dividerPayment.visibility = View.VISIBLE
                    binding.dividerItem.visibility = View.VISIBLE
                } else {
                    binding.paymentLabel.visibility = View.GONE
                    binding.switchPayment.visibility = View.GONE
                    binding.dividerLabelPayment.visibility = View.GONE
                    binding.rvPayment.visibility = View.GONE
                    binding.dividerPayment.visibility = View.GONE
                    binding.dividerItem.visibility = View.GONE
                }

                binding.progressSize = it.item.current_amount

                binding.btnPartisipasi.setOnClickListener { _ ->
                    openAmountPage("KLASPAY", "Klaspay")
                }

                binding.btnVa.setOnClickListener {
                    startActivityForResult(
                        Intent(
                            this@PartisipasiDetailPage,
                            PaymentTypePage::class.java
                        ).putExtra("get_channel_only", true), 238
                    )
                }

                binding.executePendingBindings()
            }
        }
    }

    private fun openAmountPage(channelCode: String, channelName: String) {
        startActivityForResult(
            Intent(
                this@PartisipasiDetailPage,
                PartisipasiAmountPage::class.java
            )
                .putExtra("channel_code", channelCode)
                .putExtra("channel_name", channelName)
                .putExtra("id", id)
                .putExtra("product_info", binding.name.text)
                .putExtra("amount", amount), 923
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 923 && resultCode == RESULT_OK)
            viewModel.fetchDetail(id)
        else if (requestCode == 238 && resultCode == RESULT_OK && data != null)
            openAmountPage(
                data.getStringExtra("payment_code").orEmpty(),
                data.getStringExtra("payment_name").orEmpty()
            )
        else
            super.onActivityResult(requestCode, resultCode, data)
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
        val binding: PartisipasiPaymentDetailItemBinding = PartisipasiPaymentDetailItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PartisipasiPayment) {
            binding.item = item
            binding.position = adapterPosition + 1
            binding.stringUtil = viewModel.stringUtil
            binding.dateFormat = viewModel.dateFormat
            binding.executePendingBindings()
        }
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launchWhenCreated {
            viewModel.balance.postValue(viewModel.api.klaspayWallet().data.balance)
        }
    }
}