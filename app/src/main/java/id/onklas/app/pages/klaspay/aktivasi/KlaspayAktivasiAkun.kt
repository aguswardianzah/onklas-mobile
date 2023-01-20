package id.onklas.app.pages.klaspay.aktivasi

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.KlaspayAktivasiAkunBinding
import id.onklas.app.di.component
import id.onklas.app.pages.akun.SettingContactPage
import kotlinx.coroutines.launch

class KlaspayAktivasiAkun : Fragment() {

    private lateinit var binding: KlaspayAktivasiAkunBinding
    private val viewModel by activityViewModels<KlaspayAktivasiViewmodel> { component.klaspayAktivasiVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        KlaspayAktivasiAkunBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editPassword.doAfterTextChanged {
            viewModel.password.postValue(it.toString())
        }

        viewModel.password.observe(viewLifecycleOwner, {
            binding.buttonConfirm.isEnabled = it.isNotEmpty()
        })

        viewModel.errorPassword.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                binding.labelError.text = it
                binding.labelError.visibility = View.VISIBLE
            } else binding.labelError.visibility = View.GONE
        })

        binding.buttonConfirm.setOnClickListener {
            lifecycleScope.launch {
                val loading = ProgressDialog.show(requireContext(), "", "Memverifikasi data")
                viewModel.klaspayCheck() { success, errorTypes ->
                    loading.dismiss()
                    if (success) findNavController().navigate(R.id.action_KlaspayAktivasiAkun_to_KlaspayAktivasiPin)
                    else if (errorTypes.contains("email")) {
                        AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                            .setTitle("KlasPay")
                            .setMessage("Kamu belum memverifikasi email, cek email yang telah kami kirimkan untuk memverifikasi email atau buka halaman setting kontak & email untuk merubah emailmu")
                            .setNeutralButton("Setting") { dialog, _ ->
                                startActivity(
                                    Intent(
                                        requireActivity(),
                                        SettingContactPage::class.java
                                    )
                                )
                                dialog.dismiss()
                            }
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .show()
                            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }
                    }
                }
            }
        }
    }
}