package id.onklas.app.pages

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import id.onklas.app.BuildConfig
import id.onklas.app.R
import id.onklas.app.databinding.DeepLinkPageBinding
import id.onklas.app.databinding.EmailVerifiedDialogBinding
import id.onklas.app.di.component
import id.onklas.app.pages.home.HomePage
import id.onklas.app.pages.login.Loginpage
import id.onklas.app.pages.onboard.OnBoardPage
import id.onklas.app.pages.pembayaran.SetPinKlaspayPage
import id.onklas.app.viewmodels.GeneralViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URLDecoder

class DeepLinkPage : PublicPage() {

    private val binding by lazy { DeepLinkPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<GeneralViewModel> { component.generalVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        // check deeplink uri
        // https://sekolah.onklas.id/verify-email?url=https%3A%2F%2Fapi.onklas.id%2Fapi%2Fmobile%2Femail%2Fverify%2F00b1b667812540c9b26a2346992594c4%2F5b70a6b8a61f91c4c125d32323d8a2b3cf1ab42d%3Fexpires%3D1610898353%26signature%3Dbf5d25b46c8968bce07217740baeee5751ed0f16d5136a740720d1b50f78ecad
        intent?.data?.let {
            if (it.host?.endsWith("sekolah.onklas.id", true) == true
                && !it.getQueryParameter("url").isNullOrEmpty()
            ) {
                lifecycleScope.launchWhenCreated {
                    loading(msg = "memverifikasi email Anda")
                    try {
                        viewmodel.apiService.download(
                            URLDecoder.decode(
                                it.getQueryParameter("url"),
                                "UTF-8"
                            )
                        )
                        viewmodel.pref.putBoolean("is_email_verified", true)
                        viewmodel.pref.putBoolean("is_email_verifying", false)
                        val dialogBinding = EmailVerifiedDialogBinding.inflate(layoutInflater)
                        val dialog = AlertDialog.Builder(this@DeepLinkPage)
                            .setView(dialogBinding.root)
                            .show()
                            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

                        dialogBinding.btnVerify.setOnClickListener {
                            dialog.dismiss()
                            goNext()
                        }
                    } catch (e: Exception) {
                        Timber.e(e)
                        toast(e.message)

                        AlertDialog.Builder(this@DeepLinkPage)
                            .setTitle("Perhatian")
                            .setMessage("Terjadi kesalahan proses verifikasi email Anda, mohon ulangi beberapa saat lagi.")
                            .setPositiveButton("Tutup") { dialog, _ ->
                                dialog.dismiss()
                                goNext()
                            }
                            .show()
                            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

                    } finally {
                        dismissloading()
                    }
                }
            } else if (it.host?.endsWith("api.onklas.id", true) == true) {
                lifecycleScope.launch {
                    loading(msg = "memproses permintaan Anda")
                    try {
                        viewmodel.apiService.download(it.toString())
                        startActivity(
                            Intent(
                                this@DeepLinkPage,
                                SetPinKlaspayPage::class.java
                            ).putExtra("token", it.lastPathSegment)
                        )
                        finish()
                    } catch (e: Exception) {
                        goNext()
                    } finally {
                        dismissloading()
                    }
                }
            } else {
                goNext()
            }
        }
    }

    private fun goNext() {
        if (!viewmodel.pref.getBoolean("onboard", false)) {
            startActivity(Intent(this, OnBoardPage::class.java))
        } else if (viewmodel.pref.getBoolean("logged_in")) {
            // try to open page based on params/extra "goto"
            try {
                startActivity(
                    Intent(
                        this, Class.forName(intent.getStringExtra("goto"))
                    )
                )
            } catch (e: Exception) {
                startActivity(Intent(this, HomePage::class.java))
            }
        } else {
            startActivity(Intent(this, Loginpage::class.java))
        }
        finish()
    }
}