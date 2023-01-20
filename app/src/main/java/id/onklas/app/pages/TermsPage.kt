package id.onklas.app.pages

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.R
import id.onklas.app.databinding.CheckoutResultPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.sekolah.SosmedViewModel
import timber.log.Timber

class TermsPage : Privatepage() {

    private val binding: CheckoutResultPageBinding by lazy {
        CheckoutResultPageBinding.inflate(layoutInflater)
    }
    private val sosmedViewModel by viewModels<SosmedViewModel> { component.sosmedVmFactory }

    private lateinit var content: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Kebijakan & Privasi"
            binding.toolbar.title = "Kebijakan & Privasi"
        }

        binding.webview.settings.javaScriptEnabled = true
        binding.webview.webViewClient = MyWebViewClient()
        binding.webview.webChromeClient = MyWebChromeClient()

        loadData()
    }

    private fun loadData() {
        lifecycleScope.launchWhenCreated {
            binding.progressBar.visibility = View.VISIBLE
            try {
                binding.webview.loadData(
                    sosmedViewModel.apiService.policy(sosmedViewModel.pref.getInt("user_id")).data.content,
                    "text/html; charset=utf-8",
                    "UTF-8"
                )
            } catch (e: Exception) {
                Timber.e(e)
                dialogErrorLoading()
            }
        }
    }

    private fun dialogErrorLoading() {
        MaterialAlertDialogBuilder(this, R.style.DialogTheme)
            .setTitle("Perhatian")
            .setMessage("Gagal memuat halaman")
            .setPositiveButton("Muat ulang") { dialog, which ->
                dialog.dismiss()
                loadData()
            }
            .show()
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
            dialogErrorLoading()
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
            MaterialAlertDialogBuilder(this@TermsPage, R.style.DialogTheme)
                .setTitle("Perhatian")
                .setMessage(message)
                .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                .show()
            return true
        }
    }
}