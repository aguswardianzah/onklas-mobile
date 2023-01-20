package id.onklas.app.pages.ppob.bayar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import id.onklas.app.databinding.KlaspayBayarDetailBinding
import id.onklas.app.di.component

// TODO total pemabayaran masih dicek

class KlaspayBayarDetail : Fragment() {

    private lateinit var binding: KlaspayBayarDetailBinding
    private val viewModel by activityViewModels<KlaspayBayarViewModel> { component.klaspayBayarVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = KlaspayBayarDetailBinding.inflate(inflater, container, false).also { binding = it }.root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.titleBar.postValue("Detail Pembayaran")

        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.bayarData.observe(viewLifecycleOwner, {
            binding.data = it
//            val topup: Int = it.total_amount!! - it.fee_admin!!
//            binding.topup = "Rp ${viewModel.numberUtil.formatCurrency(topup)}"
            binding.topup = "Rp ${viewModel.numberUtil.formatCurrency(it.total_amount!!)}"
            binding.feeAdmin = "Rp ${viewModel.numberUtil.formatCurrency(it.fee_admin!!)}"
            binding.totalAmount = "Rp ${viewModel.numberUtil.formatCurrency(it.total_amount)}"
            binding.feeService = "Rp 0"
        })
    }
}