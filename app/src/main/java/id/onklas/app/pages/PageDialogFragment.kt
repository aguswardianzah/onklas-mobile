package id.onklas.app.pages

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.R
import id.onklas.app.utils.hideKeyboard
import timber.log.Timber

open class PageDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(
            STYLE_NORMAL,
            R.style.AppThemeLightStatusBar
        )
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val view = decorView
                var flags = view.systemUiVisibility
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
////            flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
////            flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                view.systemUiVisibility = flags
            } else {
                statusBarColor = ContextCompat.getColor(this.context, R.color.colorPrimary)
            }
        }
    }

    override fun onDestroy() {
        hideKeyboard()
        dismissloading()
        super.onDestroy()
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

    fun dismissAlert() {
        alertDialog?.dismiss()
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

    fun toast(msg: String?) {
        Timber.e("show toast: $msg\nfrom: ${javaClass.simpleName}")
        if (!msg.isNullOrEmpty())
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}