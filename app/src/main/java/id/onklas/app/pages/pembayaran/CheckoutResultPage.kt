package id.onklas.app.pages.pembayaran

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.R
import id.onklas.app.databinding.CheckoutResultPageBinding
import id.onklas.app.pages.PageDialogFragment
import timber.log.Timber

class CheckoutResultPage(val url: String) : PageDialogFragment() {

    private lateinit var binding: CheckoutResultPageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        CheckoutResultPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        try {
            requireActivity().finish()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { dismiss() }

        binding.webview.settings.javaScriptEnabled = true
        binding.webview.settings.useWideViewPort = true
        binding.webview.webViewClient = MyWebViewClient()
        binding.webview.webChromeClient = MyWebChromeClient()

        binding.webview.loadUrl(url)
    }

    inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView,
            url: String
        ): Boolean {
            val uri = Uri.parse(url)
            if (url.toLowerCase().startsWith("tel:")) {
                startActivity(Intent(Intent.ACTION_DIAL, uri))
                return true
            }
            return false
        }

        override fun onPageFinished(view: WebView, url: String) {
            binding.progressBar.visibility = View.GONE
        }

        override fun onPageStarted(
            view: WebView,
            url: String,
            favicon: Bitmap?
        ) {
            binding.progressBar.visibility = View.VISIBLE
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            MaterialAlertDialogBuilder(requireContext(), R.style.DialogTheme)
                .setTitle("Perhatian")
                .setMessage("Gagal memuat halaman")
                .setPositiveButton("Muat ulang") { dialog, which ->
                    dialog.dismiss()
                    binding.webview.loadUrl(url)
                }
                .show()
        }
    }

    inner class MyWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (newProgress == 100) {
                binding.progressBar.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.VISIBLE
                binding.progressBar.isIndeterminate = true
            }
            if (newProgress > 0) {
                binding.progressBar.isIndeterminate = false
                binding.progressBar.progress = newProgress
            }
        }

        override fun onJsAlert(
            view: WebView,
            url: String,
            message: String,
            result: JsResult
        ): Boolean {
            MaterialAlertDialogBuilder(requireContext(), R.style.DialogTheme)
                .setTitle("Perhatian")
                .setMessage(message)
                .setPositiveButton("Ok", { dialog, which -> dialog.dismiss() })
                .show()
            return true
        }
    }
}