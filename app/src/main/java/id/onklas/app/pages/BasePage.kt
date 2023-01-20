package id.onklas.app.pages

import android.app.ProgressDialog
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.PrettyAlertDialogBinding
import id.onklas.app.databinding.SelectAlertNewDialogBinding
import id.onklas.app.pages.createpost.CameraPage
import id.onklas.app.pages.home.HomePage
import id.onklas.app.pages.klaspay.aktivasi.KlaspayAktivasiPage
import id.onklas.app.pages.pembayaran.SuccessPayPage
import id.onklas.app.utils.hideKeyboard
import timber.log.Timber

open class BasePage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (this is HomePage || this is CameraPage || this is SuccessPayPage || this is KlaspayAktivasiPage) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val view = window.decorView
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
////            flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
////            flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            view.systemUiVisibility = flags
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }

//        if (BuildConfig.BUILD_TYPE != "release") {
//            window.statusBarColor =
//                Color.parseColor(if (BuildConfig.BUILD_TYPE == "debug") "#53C0F1" else "#F49C4D")
//        }
    }

    override fun onDestroy() {
        hideKeyboard()
        dismissloading()
        dismissAlert()
        super.onDestroy()
    }

    var loadingDialog: ProgressDialog? = null
    fun loading(
        title: String = "Mohon tunggu",
        msg: String = "Sedang memproses ...",
        indeterminate: Boolean = true
    ) {
        dismissloading()
        loadingDialog = ProgressDialog.show(this, title, msg, indeterminate)
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
        Timber.e("show toast: $msg\nfrom: $localClassName")
        if (!msg.isNullOrEmpty())
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
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
        alertDialog = MaterialAlertDialogBuilder(this, R.style.DialogTheme)
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
                    GlideApp.with(this@BasePage).load(customImg).into(image)
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

        try {
            alertDialog = MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.root)
                .show()
                .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }
        } catch (_: Exception) {}
    }

    fun alertSelect(
        title: String = "",
        items: Array<String> = emptyArray(),
        onSelect: (pos: Int) -> Unit = {}
    ) {
        dismissAlert()
        alertDialog = MaterialAlertDialogBuilder(this, R.style.DialogTheme)
            .setTitle(title)
//            .setMessage(msg)
            .setItems(items) { dialog, which ->
                dialog.dismiss()
                onSelect.invoke(which)
            }
            .show()
    }

    fun alertSelectNew(
        titleText: String = "Perhatian",
        msg: String = "",
        okLabel: String = "Tutup",
        okClick: () -> Unit = {},
        abortLabel: String = "Batal",
        abortClick: () -> Unit = {}
    ) {
        dismissAlert()
        val alertSelectNew = SelectAlertNewDialogBinding.inflate(layoutInflater).apply {
            title.text = titleText
            content.text = msg
            btnAbort.text  = abortLabel
            btnAbort.setOnClickListener {
                dismissAlert()
                abortClick.invoke() }
            btnOk.text = okLabel
            btnOk.setOnClickListener {
                dismissAlert()
                okClick.invoke() }
        }

        alertDialog = MaterialAlertDialogBuilder(this)
            .setView(alertSelectNew.root)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }
    }

    fun dismissAlert() {
        alertDialog?.dismiss()
    }
}