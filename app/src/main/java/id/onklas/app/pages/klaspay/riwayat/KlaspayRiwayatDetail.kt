package id.onklas.app.pages.klaspay.riwayat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.KlaspayRiwayatDetailBinding
import id.onklas.app.di.component

class KlaspayRiwayatDetail : Fragment() {

    private lateinit var binding: KlaspayRiwayatDetailBinding
    private val viewModel by activityViewModels<KlaspayRiwayatViewModel> { component.KlaspayRiwayatVmFactory }
    private val historyId by lazy { requireArguments().getString("history_id") }

    init {
        lifecycleScope.launchWhenCreated {
            viewModel.transactionHistoryDetail(historyId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        KlaspayRiwayatDetailBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        viewModel.titleBar.postValue("DETAIL PEMBAYARAN")

        viewModel.historyDetail.observe(viewLifecycleOwner, {
            binding.detail = it.apply {
//                created_date = viewModel.dstFormat.format(viewModel.srcFormat.parse(created_date))
            }
            binding.totalBayar = "Rp ${viewModel.stringUtil.formatCurrency2(it.price!!.toInt())}"
            viewModel.loadingDetail.postValue(false)
        })

        viewModel.loadingDetail.observe(
                viewLifecycleOwner,
                Observer(binding.swipeRefresh::setRefreshing)
        )

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launchWhenCreated {
                viewModel.transactionHistoryDetail(historyId)
            }
        }
    }
}