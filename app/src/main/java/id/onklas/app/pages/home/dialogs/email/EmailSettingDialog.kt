package id.onklas.app.pages.home.dialogs.email

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import id.onklas.app.R
import id.onklas.app.databinding.EmailSettingDialogBinding
import id.onklas.app.di.component
import id.onklas.app.pages.akun.SettingAkunViewmodel
import id.onklas.app.utils.hideKeyboard
import kotlinx.coroutines.launch

class EmailSettingDialog(
    private val onSuccess: () -> Unit = {},
    private val onDismiss: () -> Unit = {}
) : DialogFragment() {

    private lateinit var binding: EmailSettingDialogBinding
    private val viewmodel by activityViewModels<SettingAkunViewmodel> { component.settingAkunVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        EmailSettingDialogBinding.inflate(inflater, container, false).also {
            dialog?.window?.setBackgroundDrawableResource(R.drawable.rounded_white)
            binding = it

            isCancelable = true
        }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewmodel = viewmodel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.inEmail.doAfterTextChanged { viewmodel.hasChange.postValue(viewmodel.hasChange()) }

        binding.btnVerify.setOnClickListener {
            lifecycleScope.launch {
                hideKeyboard()
                viewmodel.email.value?.let {
                    if (it.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                        val loading =
                            ProgressDialog.show(requireContext(), "", "mengirimkan verifikasi")
                        if (viewmodel.updateProfile() && viewmodel.sendEmail()) {
                            dismiss()
                            onSuccess.invoke()
                        }
                        loading.dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Email tidak sesuai", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
            onDismiss.invoke()
        }

        viewmodel.errorString.observe(viewLifecycleOwner) {
            Toast.makeText(requireActivity(), it, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onDismiss.invoke()
    }
}