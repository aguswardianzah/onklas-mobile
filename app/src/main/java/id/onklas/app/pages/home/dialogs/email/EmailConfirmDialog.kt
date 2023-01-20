package id.onklas.app.pages.home.dialogs.email

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import id.onklas.app.R
import id.onklas.app.databinding.EmailConfigDialogBinding
import id.onklas.app.di.component
import id.onklas.app.pages.akun.SettingAkunViewmodel

class EmailConfirmDialog(
    private val showChangeEmail: Boolean = false,
    private val onClickChangeEmail: () -> Unit = {},
    private val onDismiss: () -> Unit = {}
) : DialogFragment() {

    private lateinit var binding: EmailConfigDialogBinding
    private val viewmodel by activityViewModels<SettingAkunViewmodel> { component.settingAkunVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        EmailConfigDialogBinding.inflate(inflater, container, false).also {
            dialog?.window?.setBackgroundDrawableResource(R.drawable.rounded_white)
            binding = it
        }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.btnChange.visibility = if (showChangeEmail) View.VISIBLE else View.GONE
        binding.btnChange.setOnClickListener {
            dismiss()
            onClickChangeEmail.invoke()
        }

        binding.btnVerify.setOnClickListener {
            dismiss()
//            viewmodel.intentUtil.openEmail(requireActivity()) { dismiss() }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss.invoke()
    }
}