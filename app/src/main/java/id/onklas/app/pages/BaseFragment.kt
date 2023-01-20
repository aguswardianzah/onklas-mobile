package id.onklas.app.pages

import android.app.ProgressDialog
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.PrettyAlertDialogBinding
import id.onklas.app.utils.hideKeyboard
import timber.log.Timber

open class BaseFragment : Fragment() {

    override fun onDestroy() {
        hideKeyboard()
        dismissloading()
        super.onDestroy()
    }

    var loadingDialog: ProgressDialog? = null
    fun loading(
        title: String = "Mohon tunggu",
        msg: String = "Sedang memproses ...",
        indeterminate: Boolean = true
    ) {
        loadingDialog?.dismiss()
        loadingDialog = ProgressDialog.show(requireContext(), title, msg, indeterminate)
    }

    fun updateLoading(
        title: String = "Mohon tunggu",
        msg: String = "Sedang memproses ...",
        newProgress: Int = 100
    ) {
        loadingDialog?.apply {
            setTitle(title)
            setMessage(msg)
            progress = newProgress
        }
    }

    fun dismissloading() {
        loadingDialog?.dismiss()
    }

    fun toast(msg: String) {
        Timber.e("show toast: $msg\nfrom: ${javaClass.simpleName}")
        if (msg.isNotEmpty())
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    var alertDialog: AlertDialog? = null
    fun alert(
        title: String = "Perhatian",
        msg: String = "",
        okLabel: String = "Tutup",
        okClick: () -> Unit = {},
        abortLabel: String = "Batal",
        abortClick: () -> Unit = {}
    ) {
        dismissAlert()
        alertDialog = MaterialAlertDialogBuilder(requireContext(), R.style.DialogTheme)
            .setTitle(title)
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton(okLabel) { dialog, which ->
                dialog.dismiss()
                okClick.invoke()
            }
            .setNeutralButton(abortLabel) { dialog, which ->
                dialog.dismiss()
                abortClick.invoke()
            }
            .show()
    }

    fun prettyAlert(
        showImage: Boolean = false,
        isSuccess: Boolean = true,
        customImg: Any? = null,
        titleText: String = "",
        msg: String = "",
        okLabel: String = "Tutup",
        okClick: () -> Unit = {},
        abortLabel: String = "Batal",
        abortClick: () -> Unit = {}
    ) {
        dismissAlert()

        val dialogBinding = PrettyAlertDialogBinding.inflate(layoutInflater).apply {
            image.visibility = if (showImage) View.VISIBLE else View.GONE
            if (showImage) {
                if (customImg != null)
                    GlideApp.with(requireContext()).load(customImg).into(image)
                else
                    image.setImageResource(if (isSuccess) R.drawable.img_pay_success else R.drawable.img_danger)
            }

            title.text = titleText
            content.text = msg

            btnOk.visibility = if (okLabel.isEmpty()) View.GONE else View.VISIBLE
            btnOk.text = okLabel
            btnOk.setOnClickListener {
                dismissAlert()
                okClick.invoke()
            }

            btnAbort.visibility = if (abortLabel.isEmpty()) View.GONE else View.VISIBLE
            btnAbort.text = abortLabel
            btnAbort.setOnClickListener {
                dismissAlert()
                abortClick.invoke()
            }
        }

        alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }
    }

    fun alertSelect(
        title: String = "",
        items: Array<String> = emptyArray(),
        onSelect: (pos: Int) -> Unit = {}
    ) {
        dismissAlert()
        alertDialog = MaterialAlertDialogBuilder(requireContext(), R.style.DialogTheme)
            .setTitle(title)
//            .setMessage(msg)
            .setItems(items) { dialog, which ->
                dialog.dismiss()
                onSelect.invoke(which)
            }
            .show()
    }

    fun dismissAlert() {
        alertDialog?.dismiss()
    }
}