package id.onklas.app.pages.ppob.pulsa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.PascabayarPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BaseFragment
import id.onklas.app.pages.pembayaran.ConfirmPinPage
import id.onklas.app.pages.ppob.*
import kotlinx.coroutines.launch

class PascabayarPage : BaseFragment() {

    private lateinit var binding: PascabayarPageBinding
    private val viewmodel by activityViewModels<PulsaViewModel> { component.pulsaVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        PascabayarPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewmodel.productPasca.observe(viewLifecycleOwner) {
            binding.btnCheck.isEnabled = it != null
        }

        binding.btnCheck.setOnClickListener {
            ConfirmPinPage.getPin(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ConfirmPinPage.RC && resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                loading(msg = "sedang mencek tagihan Anda")
                val res = viewmodel.inqPulsa(data?.getStringExtra("pin").orEmpty())

                if (res.isNotEmpty())
                    PpobCheckoutPage.openByTrxId(this@PascabayarPage, res)
                dismissloading()
            }
        } else if (requestCode == PpobCheckoutPage.RC && resultCode == Activity.RESULT_OK) {
            requireActivity().finish()
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }
}