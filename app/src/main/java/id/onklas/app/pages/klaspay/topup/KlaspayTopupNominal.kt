package id.onklas.app.pages.klaspay.topup

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import id.onklas.app.R
import id.onklas.app.databinding.KlaspayTopupDialogBinding
import id.onklas.app.databinding.KlaspayTopupNominalBinding
import id.onklas.app.databinding.ProgressDialogBinding
import id.onklas.app.di.component
import id.onklas.app.pages.pembayaran.PaymentGuidePage
import kotlinx.coroutines.launch

class KlaspayTopupNominal : Fragment() {

    private lateinit var binding: KlaspayTopupNominalBinding
    private val viewModel by activityViewModels<KlaspayTopupViewModel> { component.KlaspayTopupVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        KlaspayTopupNominalBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.titleBar.postValue("Pilih Nominal")

        binding.button25k.setOnClickListener { setButton(binding.button25k) }
        binding.button50k.setOnClickListener { setButton(binding.button50k) }
        binding.button100k.setOnClickListener { setButton(binding.button100k) }
        binding.button500k.setOnClickListener { setButton(binding.button500k) }

        binding.editNominal.doAfterTextChanged {
            binding.buttonPay.isEnabled = it.toString().isNotEmpty()
            binding.editNominal.setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.textBlack
                )
            )

            when (binding.editNominal.text.toString()) {
                "25000" -> setButton(binding.button25k)
                "50000" -> setButton(binding.button50k)
                "100000" -> setButton(binding.button100k)
                "500000" -> setButton(binding.button500k)
                else -> setButton(null)
            }
            binding.labelError.visibility = View.GONE
        }

        binding.buttonPay.setOnClickListener {
            val nominal = binding.editNominal.text.toString().toInt()
            if (nominal >= 20000) {
                lifecycleScope.launch {
                    val progress = showProgress()
                    if (viewModel.klaspayTopupInq(nominal)) {
                        PaymentGuidePage.open(requireActivity(), viewModel.klaspayTopupTrx())
                        requireActivity().finish()
                    }
//                        PaymentGuidePage.open(requireActivity(), )
//                    viewModel.klaspayTopupInq(nominal) {
//                        if (it) showDialog()
//                    }
                    progress.dismiss()
                }
            } else {
                binding.editNominal.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.red
                    )
                )
                binding.labelError.visibility = View.VISIBLE
            }
//                Toast.makeText(
//                        requireActivity(),
//                        "Minimal Topup 20.000",
//                        Toast.LENGTH_SHORT)
//                        .show()
        }
    }

    private val buttons by lazy {
        listOf(
            binding.button100k,
            binding.button25k,
            binding.button50k,
            binding.button500k
        )
    }

    private fun setButton(button: MaterialButton?) {
        buttons.forEach { it.setStrokeColorResource(R.color.white) }

        button?.let {
            it.setStrokeColorResource(R.color.green)
            val newText = it.text.toString().replace(".", "")
            if (binding.editNominal.text.toString() != newText)
                binding.editNominal.setText(newText)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDialog() {
        val dialogBinding = KlaspayTopupDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_white)

        dialogBinding.content.text =
            "Anda akan melakukan topup sebesar \nRp ${binding.editNominal.text}"
        dialogBinding.button.setOnClickListener {
//            lifecycleScope.launch {
//                val progress = showProgress()
//                viewModel.klaspayTopupTrx(
//                    paymentCode.toString(),
//                    viewModel.transactionIdInquiry.value
//                ) {
//                    if (it) {
//                        startActivity(
//                            Intent(requireActivity(), KlaspayBayarPage::class.java)
//                                .putExtra("from", "topup")
//                                .putExtra("topup", viewModel.bayarTopup.value)
//                        )
//                        dialog.dismiss()
//                        requireActivity().finish()
//                    }
//                }
//                progress.dismiss()
//            }
        }
    }

    private fun showProgress(): AlertDialog {
        val bindingDialog = ProgressDialogBinding.inflate(layoutInflater)
        return AlertDialog.Builder(requireContext())
            .setView(bindingDialog.root)
            .show()
    }
}