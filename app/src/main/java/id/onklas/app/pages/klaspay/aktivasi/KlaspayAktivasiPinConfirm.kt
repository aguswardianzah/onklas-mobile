package id.onklas.app.pages.klaspay.aktivasi

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import id.onklas.app.R
import id.onklas.app.databinding.KlaspayAktivasiPinBinding
import id.onklas.app.databinding.KlaspayAktivasiSuccessBinding
import id.onklas.app.di.component
import id.onklas.app.pages.klaspay.topup.KlaspayTopupPage
import kotlinx.coroutines.launch

class KlaspayAktivasiPinConfirm : Fragment() {

    private lateinit var binding: KlaspayAktivasiPinBinding
    private val viewModel by activityViewModels<KlaspayAktivasiViewmodel> { component.klaspayAktivasiVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        KlaspayAktivasiPinBinding.inflate(inflater, container, false).also { binding = it }.root

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.pageStep.postValue(3)
        binding.labelTitle.text = "KONFIRMASI PIN ULANG"
        binding.labelDescription.text = "Ketik ulang  pin keamanan yang sudah Anda atur sebelumnya"

        binding.pinField.doAfterTextChanged {
            viewModel.pinConfirm.postValue(it.toString())
        }

        viewModel.pinConfirm.observe(viewLifecycleOwner, {
            binding.buttonConfirm.isEnabled = (it.toString() == viewModel.pin.value)
            if ((it.length == 6) && (it.toString() != viewModel.pin.value)) {
                binding.pinField.fieldColor = R.color.red
                binding.labelError.visibility = View.VISIBLE
            } else {
                binding.pinField.fieldColor = R.color.textBlack
                binding.labelError.visibility = View.GONE
            }
        })

        binding.buttonConfirm.setOnClickListener {
            lifecycleScope.launch {
                val loading = ProgressDialog.show(requireContext(), "", "mohon tunggu")
                val result = viewModel.klaspayActivate(
                    viewModel.password.value,
                    binding.pinField.text.toString()
                )
                loading.dismiss()
                if (result) showDialogSuccessAktivasi()
            }
        }
    }

    private fun showDialogSuccessAktivasi() {
        val dialogBinding = KlaspayAktivasiSuccessBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

        dialogBinding.buttonTopUp.setOnClickListener {
            dialog.dismiss()
            requireActivity().startActivity(Intent(requireActivity(), KlaspayTopupPage::class.java))
            requireActivity().finish()
        }

        dialogBinding.buttonLater.setOnClickListener {
            dialog.dismiss()
            requireActivity().finish()
        }
    }

}