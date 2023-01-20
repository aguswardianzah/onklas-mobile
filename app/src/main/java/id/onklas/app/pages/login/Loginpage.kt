package id.onklas.app.pages.login

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.BuildConfig
import id.onklas.app.R
import id.onklas.app.databinding.LoginPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.PublicPage
import id.onklas.app.pages.home.HomePage
import id.onklas.app.pages.onboard.OnBoardPage
import id.onklas.app.viewmodels.GeneralViewModel
import id.onklas.app.worker.LogUploader
import java.util.concurrent.TimeUnit

class Loginpage : PublicPage() {

    private val binding by lazy { LoginPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<GeneralViewModel> { component.generalVmFactory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenCreated {
            // setNotificationChannel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = getString(R.string.app_name)

                // Register the channel with the system
                (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.apply {

                    // create default notification channel
                    createNotificationChannel(
                        NotificationChannel(
                            name,
                            name,
                            NotificationManager.IMPORTANCE_DEFAULT
                        ).apply {
                            description = name
                        }
                    )

                    // create silent notification channel
                    createNotificationChannel(
                        NotificationChannel(
                            "${name}.silent",
                            "${name}.silent",
                            NotificationManager.IMPORTANCE_MIN
                        ).apply {
                            description = name
                            setSound(null, null)
                            enableLights(false)
                            enableVibration(false)
                        }
                    )
                }
            }

            // run periodic worker to upload log file
            WorkManager.getInstance(applicationContext)
                .enqueueUniquePeriodicWork(
                    "upload_log",
                    ExistingPeriodicWorkPolicy.KEEP,
                    PeriodicWorkRequestBuilder<LogUploader>(6, TimeUnit.HOURS).build()
                )

            // check current apps version
            viewmodel.checkVersion()?.let {
                val latestVersion = it.version.split(".").map { it.toInt() }
                val currentVersion = BuildConfig.VERSION_NAME.split(".").map { it.toInt() }

                val needUpdate = latestVersion
                    .zip(currentVersion)
                    .firstOrNull { it.first != it.second }?.let { it.first > it.second }
                    ?: false

                if (needUpdate) {
                    MaterialAlertDialogBuilder(
                        this@Loginpage,
                        R.style.DialogTheme
                    )
                        .setTitle("Update Tersedia")
                        .setMessage("Silahkan update ke versi terbaru aplikasi")
                        .setCancelable(false)
                        .setPositiveButton("Update") { dialog, _ ->
                            // try to open play store
                            try {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=$packageName")
                                    )
                                )
                            } catch (anfe: ActivityNotFoundException) {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                                    )
                                )
                            }
                            finish()
                        }
                        .show()
                } else goNext()
            } ?: goNext()
        }
    }

    private fun goNext() {
        if (!viewmodel.pref.getBoolean("onboard", false)) {
            startActivity(Intent(this@Loginpage, OnBoardPage::class.java))
            finish()
        } else if (viewmodel.pref.getBoolean("logged_in")) {
            // try to open page based on params/extra "goto"
            try {
                startActivity(
                    Intent(
                        this@Loginpage,
                        Class.forName(intent.getStringExtra("goto"))
                    )
                )
            } catch (e: Exception) {
                startActivity(Intent(this@Loginpage, HomePage::class.java))
            }
            finish()
        } else {
            window.setBackgroundDrawable(null)
            setContentView(binding.root)
        }
    }
}