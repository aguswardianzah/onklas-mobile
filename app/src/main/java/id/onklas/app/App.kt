package id.onklas.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import androidx.multidex.MultiDexApplication
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import id.onklas.app.di.DaggerAppComponent
import timber.log.Timber

class  App : MultiDexApplication(), Application.ActivityLifecycleCallbacks {

    val appComponent by lazy { DaggerAppComponent.factory().create(this, this) }
    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

    override fun onCreate() {
        super.onCreate()

//        if (BuildConfig.BUILD_TYPE != "release") {
            Timber.plant(Timber.DebugTree())
//        }

        // listen to phone connectivity
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//            (getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.registerDefaultNetworkCallback(
//                object :
//                    ConnectivityManager.NetworkCallback() {
//                    override fun onAvailable(network: Network) {
//                        appComponent.socketClass.initSocket()
//                        appComponent.socketClass.connect()
//                    }
//
//                    override fun onLost(network: Network) {
//                        appComponent.socketClass.disconnect()
//                    }
//                })
//        }

        checkForUpdate()
        initFirebaseServices()

        // install keyboard emoji provider
        EmojiManager.install(GoogleEmojiProvider())
    }

    override fun onTerminate() {
        super.onTerminate()

        appComponent.socketClass.off()
        appComponent.socketClass.disconnect()
    }

    override fun onTrimMemory(level: Int) {
        if (level == TRIM_MEMORY_RUNNING_LOW || level == TRIM_MEMORY_RUNNING_CRITICAL) {
            Runtime.getRuntime().gc()
        }
        super.onTrimMemory(level)
    }

    private fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnCompleteListener {
            if (it.isSuccessful) {
                val appUpdateInfo = it.result
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    currentAct?.let { activity ->
                        appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,
                            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                            AppUpdateType.IMMEDIATE,
                            // The current activity making the update request.
                            activity,
                            // Include a request code to later monitor this update request.
                            126
                        )
                    }
                }
            }
        }
    }

    private fun initFirebaseServices() {
        // subscribe to notif topic unlogged
        FirebaseMessaging.getInstance().subscribeToTopic("unlogged")

        // get firebase token and id
        FirebaseInstallations.getInstance().id.addOnSuccessListener {
            appComponent.preference.putString("firebase_id", it)
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            appComponent.preference.putString("token", it)
        }

        // get firebase remote config
//        Firebase.remoteConfig.apply {
//            setConfigSettingsAsync(remoteConfigSettings {
//                minimumFetchIntervalInSeconds = 3600
//            }).addOnCompleteListener {
//                if (it.isSuccessful) {
//                    setDefaultsAsync(DefaultRemoteConfig.minVersion).addOnCompleteListener {
//                        if (it.isSuccessful) {
//                            fetchAndActivate().addOnCompleteListener {
//                                if (it.isSuccessful) {
//                                    try {
//                                        appComponent.moshi.adapter(MinVersionClass::class.java)
//                                            .fromJson(getString("minVersion").also { Timber.e(it) })
//                                            ?.let {
//                                                if (BuildConfig.VERSION_CODE < it.minVersion) {
//                                                    currentAct?.let { activity ->
//                                                        MaterialAlertDialogBuilder(
//                                                            activity,
//                                                            R.style.DialogTheme
//                                                        )
//                                                            .setTitle("Update Tersedia")
//                                                            .setMessage("Silahkan update ke versi terbaru aplikasi")
//                                                            .setCancelable(it.needUpdate)
//                                                            .setPositiveButton("Update") { dialog, _ ->
//                                                                try {
//                                                                    startActivity(
//                                                                        Intent(
//                                                                            Intent.ACTION_VIEW,
//                                                                            Uri.parse("market://details?id=$packageName")
//                                                                        )
//                                                                    )
//                                                                } catch (anfe: ActivityNotFoundException) {
//                                                                    startActivity(
//                                                                        Intent(
//                                                                            Intent.ACTION_VIEW,
//                                                                            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
//                                                                        )
//                                                                    )
//                                                                }
//                                                                dialog.dismiss()
//                                                            }
//                                                            .show()
//                                                    }
//                                                }
//                                            }
//                                    } catch (e: Exception) {
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

    var currentAct: Activity? = null
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentAct = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        currentAct = null
    }

    override fun onActivityResumed(activity: Activity) {
        currentAct = activity
    }

    override fun onActivityPaused(activity: Activity) {
        currentAct = null
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}