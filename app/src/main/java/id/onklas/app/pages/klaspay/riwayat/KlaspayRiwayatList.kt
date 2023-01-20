package id.onklas.app.pages.klaspay.riwayat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import id.onklas.app.R
import id.onklas.app.databinding.KlaspayRiwayatListBinding
import id.onklas.app.di.component
import id.onklas.app.pages.pembayaran.PaymentDetailPage
import id.onklas.app.pages.ppob.PpobPaymentDetailPage
import kotlinx.coroutines.launch

class KlaspayRiwayatList : Fragment() {

    private lateinit var binding: KlaspayRiwayatListBinding
    private val viewModel by activityViewModels<KlaspayRiwayatViewModel> { component.KlaspayRiwayatVmFactory }

    init {
        lifecycleScope.launchWhenCreated {
            viewModel.transactionHistory()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        KlaspayRiwayatListBinding.inflate(inflater, container, false).also { binding = it }.root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.titleBar.postValue("RIWAYAT PEMBAYARAN")

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.buttonAll.setOnClickListener { colorButton(it) }
        binding.buttonIn.setOnClickListener { colorButton(it) }
        binding.buttonOut.setOnClickListener { colorButton(it) }

        binding.listItem.addItemDecoration(
            DividerItemDecoration(
                requireActivity(),
                LinearLayoutManager.VERTICAL
            )
        )

        val adapter = KlaspayRiwayatAdapter { item ->
            if (item.type.equals("SPP", true))
                PaymentDetailPage.open(requireActivity(), item.transaction_id)
            else
                PpobPaymentDetailPage.open(requireActivity(), item.transaction_id)
//                        findNavController().navigate(
//                            R.id.action_klaspayRiwayatList_to_klaspayRiwayatDetail,
//                            bundleOf("history_id" to item.transaction_id)
//                        )
        }

        binding.listItem.adapter = adapter

        viewModel.historyList.observe(viewLifecycleOwner, {
            adapter.submitList(it)
            viewModel.loadingList.postValue(false)
        })

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewModel.transactionHistory()
            }
        }

        viewModel.loadingList.observe(
            viewLifecycleOwner,
            Observer(binding.swipeRefresh::setRefreshing)
        )
    }


    private fun colorButton(view: View) {
        listOf<MaterialButton>(binding.buttonAll, binding.buttonIn, binding.buttonOut).forEach {
            it.background.setTint(ContextCompat.getColor(requireActivity(), R.color.white))
            it.setTextColor(ContextCompat.getColor(requireActivity(), R.color.gray))
        }
        (view as MaterialButton).background.setTint(
            ContextCompat.getColor(
                requireActivity(),
                R.color.colorPrimary
            )
        )
        view.setTextColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.white
            )
        )
    }
}