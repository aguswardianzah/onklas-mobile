package id.onklas.app.pages.home.dialogs.username

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import id.onklas.app.R
import id.onklas.app.databinding.SetUsernameDialogBinding
import id.onklas.app.di.component
import id.onklas.app.pages.akun.SettingAkunViewmodel
import id.onklas.app.utils.hideKeyboard
import kotlinx.coroutines.launch

class SetUsernameDialog(private val onSuccess: () -> Unit = {}) : DialogFragment() {

    private lateinit var binding: SetUsernameDialogBinding
    private val viewmodel by activityViewModels<SettingAkunViewmodel> { component.settingAkunVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = SetUsernameDialogBinding.inflate(inflater, container, false).also {
        dialog?.window?.setBackgroundDrawableResource(R.drawable.rounded_white)
        binding = it
    }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewmodel = viewmodel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.inEmail.doAfterTextChanged { viewmodel.hasChange.postValue(viewmodel.hasChange()) }

        binding.btnVerify.setOnClickListener {
            lifecycleScope.launch {
                hideKeyboard()
                viewmodel.username.value?.let {
                    if (it.isNotEmpty() && it.trim().contains(' ')) {
                        Toast.makeText(
                            requireContext(),
                            "Username tidak boleh mengandung spasi",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val loading =
                            ProgressDialog.show(requireContext(), "", "membuat username")
                        if (viewmodel.updateProfile()) {
                            dismiss()
                            onSuccess.invoke()
                        }
                        loading.dismiss()
                    }
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        viewmodel.errorString.observe(viewLifecycleOwner) {
            Toast.makeText(requireActivity(), it, Toast.LENGTH_SHORT).show()
        }
    }
}