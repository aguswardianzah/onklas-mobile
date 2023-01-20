package id.onklas.app.pages.home.dialogs

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import id.onklas.app.R
import id.onklas.app.databinding.ChangePasswordSuccessDialogBinding
import id.onklas.app.databinding.SetUsernameSuccessDialogBinding
import id.onklas.app.pages.home.dialogs.email.EmailConfirmDialog
import id.onklas.app.pages.home.dialogs.email.EmailSettingDialog
import id.onklas.app.pages.home.dialogs.password.ChangePasswordDialog
import id.onklas.app.pages.home.dialogs.username.SetUsernameDialog

object HomeDialog {

    fun showEmailSettingDialog(
        fragmentManager: FragmentManager,
        onDismissConfirm: () -> Unit = {}
    ) {
        EmailSettingDialog({
            EmailConfirmDialog(onDismiss = onDismissConfirm).show(
                fragmentManager,
                "config_email_dialog"
            )
        }, onDismissConfirm).apply { isCancelable = false }.show(fragmentManager, "email_dialog")
    }

    fun showEmailConfirmDialog(
        fragmentManager: FragmentManager,
        onDismissConfirm: () -> Unit = {}
    ) {
        EmailConfirmDialog(
            true,
            { showEmailConfirmDialog(fragmentManager) },
            onDismissConfirm
        ).show(
            fragmentManager,
            "config_email_dialog"
        )
    }

    fun showChangePasswordDialog(
        context: Context,
        fragmentManager: FragmentManager,
        layoutInflater: LayoutInflater,
        onSuccess: () -> Unit = {}
    ) {
        ChangePasswordDialog {
            val successBinding = ChangePasswordSuccessDialogBinding.inflate(layoutInflater)
            val dialog = AlertDialog.Builder(context)
                .setView(successBinding.root)
                .show()
                .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

            successBinding.btnVerify.setOnClickListener {
                dialog.dismiss()
                onSuccess.invoke()
            }
        }.apply {
            isCancelable = false
        }.show(fragmentManager, "change_pass_dialog")
    }

    fun showSetUsernameDialog(
        context: Context,
        fragmentManager: FragmentManager,
        layoutInflater: LayoutInflater
    ) {
        SetUsernameDialog {
            val successBinding = SetUsernameSuccessDialogBinding.inflate(layoutInflater)
            val dialog = AlertDialog.Builder(context)
                .setView(successBinding.root)
                .show()
                .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

            successBinding.btnVerify.setOnClickListener { dialog.dismiss() }
        }.apply {
            isCancelable = false
        }.show(fragmentManager, "username_dialog")
    }
}