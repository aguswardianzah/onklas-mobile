package id.onklas.app.pages.home.dialogs.password

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import id.onklas.app.R
import id.onklas.app.databinding.ChangePasswordConfirmDialogBinding
import id.onklas.app.databinding.ChangePasswordDialogBinding
import id.onklas.app.di.component
import id.onklas.app.pages.changepass.ChangePassViewmodel
import id.onklas.app.utils.hideKeyboard
import kotlinx.coroutines.launch

class ChangePasswordDialog(private val onSuccess: () -> Unit = {}) : DialogFragment() {

    private lateinit var binding: ChangePasswordDialogBinding
    private val viewmodel by activityViewModels<ChangePassViewmodel> { component.changePassVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ChangePasswordDialogBinding.inflate(inflater, container, false).also {
        dialog?.window?.setBackgroundDrawableResource(R.drawable.rounded_white)
        binding = it

        isCancelable = true
    }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewmodel = viewmodel
        binding.inPass.doAfterTextChanged { checkAllowSave() }
        binding.inPassNew.doAfterTextChanged { checkAllowSave() }
        binding.inPassConf.doAfterTextChanged { checkAllowSave() }

        binding.btnVerify.setOnClickListener {
            if (viewmodel.newPass.value != viewmodel.confirmPass.value)
                Toast.makeText(requireContext(), "Kkonfirmasi password baru tidak sesuai", Toast.LENGTH_SHORT)
                    .show()
            else if (viewmodel.newPass.value == viewmodel.oldPass.value)
                Toast.makeText(
                    requireContext(),
                    "Password baru tidak boleh sama dengan password lama",
                    Toast.LENGTH_SHORT
                )
                    .show()
            else {
                hideKeyboard()
                val confirmBinding = ChangePasswordConfirmDialogBinding.inflate(layoutInflater)
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(confirmBinding.root)
                    .show()
                    .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

                confirmBinding.btnCancel.setOnClickListener { dialog.dismiss() }
                confirmBinding.btnConfirm.setOnClickListener {
                    dialog.dismiss()
                    lifecycleScope.launch {
                        val loading = ProgressDialog.show(requireContext(), "", "mohon tunggu")
                        viewmodel.changePass {
                            loading.dismiss()
                            if (it) {
                                dismiss()
                                onSuccess.invoke()
                            }
                        }
                    }
                }
            }
        }

        binding.btnCancel.setOnClickListener { dismiss() }

        viewmodel.errorString.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAllowSave() {
        viewmodel.allowChange.postValue(
            viewmodel.oldPass.value?.isNotEmpty() == true &&
                    viewmodel.newPass.value?.isNotEmpty() == true &&
                    viewmodel.confirmPass.value?.isNotEmpty() == true
        )
    }
}