package id.onklas.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.Keep
import androidx.core.app.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.moshi.JsonClass
import id.onklas.app.R
import id.onklas.app.di.component
import id.onklas.app.pages.chat.ChatResponse
import id.onklas.app.pages.comment.CommentPage
import id.onklas.app.pages.home.HomePage
import id.onklas.app.pages.homework.HomeWorkPage
import id.onklas.app.pages.login.Loginpage
import id.onklas.app.pages.pembayaran.spp.SppPaymentPage
import id.onklas.app.pages.presensi.PresensiPage
import id.onklas.app.worker.ChatIncomingHandler
import timber.log.Timber
import java.util.concurrent.TimeUnit

class NotifService : FirebaseMessagingService(), LifecycleOwner {

    private val moshi by lazy { component.moshi }
    private val pref by lazy { component.preference }
    private val intentUtil by lazy { component.intentUtil }
    private val socketClass by lazy { component.socketClass }
    private val notifUtil by lazy { component.notifUtil }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data

        Timber.e("notif - received notif: ${message.notification}")
        Timber.e("notif - received data: $data")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val descriptionText = "onklas desc"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(getString(R.string.app_name), name, importance).apply {
                    description = descriptionText
                }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        try {
            val builder =
                NotificationCompat.Builder(applicationContext, getString(R.string.app_name))
                    .setSmallIcon(R.drawable.ic_logo_notif)
                    .setContentTitle(data["title"])
                    .setContentText(data["body"])
                    .setAutoCancel(true)
                    .setStyle(
                        NotificationCompat.BigTextStyle().bigText(data["body"])
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

            // set notif data
            data["data"]?.let { pref.putString("notif_data", it) }

            val pageData = (data["page"]) ?: ""
            if (pageData.isNotEmpty()) {
                val adapter = moshi.adapter(NotifPage::class.java)
                try {
                    adapter.fromJson(pageData)
                } catch (e: Exception) {
                    null
                }?.let { page ->
                    Timber.e("notif --> page --> id: ${page.id}")
                    when (page.menu) {
                        "logout" -> intentUtil.logOut(this@NotifService)
                        "email_verified" -> pref.putBoolean("is_verified", true)
                        else -> {
                            builder.setContentIntent(getPendingIntent(page.menu, page.id))

                            // notificationId is a unique int for each notification that you must define
                            NotificationManagerCompat.from(applicationContext)
                                .notify(page.id, builder.build())
                        }
                    }
                } ?: run {
                    builder.setContentIntent(getPendingIntent())

                    // notificationId is a unique int for each notification that you must define
                    NotificationManagerCompat.from(applicationContext)
                        .notify(System.currentTimeMillis().toInt(), builder.build())
                }
            } else if (data["body"].orEmpty().isNotEmpty()) {
                val newChat = try {
                    moshi.adapter(ChatResponse::class.java).fromJson(data["body"].orEmpty())
                } catch (e: Exception) {
                    Timber.e(e)
                    null
                }

                if (newChat != null) {
                    // start worker to handle incoming message
                    val workRequest = OneTimeWorkRequestBuilder<ChatIncomingHandler>()
                        .setInputData(
                            workDataOf("resp" to data["body"])
                        )
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .setBackoffCriteria(
                            BackoffPolicy.LINEAR,
                            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS
                        )
                        .addTag("chat_incoming_handler")
                        .build()

                    WorkManager.getInstance(applicationContext)
                        .enqueueUniqueWork(
                            "chat_incoming_handler_${newChat.id}",
                            ExistingWorkPolicy.REPLACE,
                            workRequest
                        )
                } else {
                    builder.setContentIntent(getPendingIntent())
                    NotificationManagerCompat.from(applicationContext)
                        .notify(0, builder.build())
                }
            } else {
                builder.setContentIntent(getPendingIntent())
                NotificationManagerCompat.from(applicationContext)
                    .notify(0, builder.build())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun getPendingIntent(menu: String = "", id: Int = 0) =
        TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(
                if (pref.getBoolean("logged_in"))
                    when (menu) {
                        "feed-single" -> Intent(
                            applicationContext,
                            CommentPage::class.java
                        ).putExtra("feed_id", id)
                        "new-spp" -> Intent(applicationContext, SppPaymentPage::class.java)
                        "spp" -> Intent(applicationContext, SppPaymentPage::class.java).putExtra(
                            "page",
                            "paid"
                        )
                        "presensi" -> Intent(applicationContext, PresensiPage::class.java)
                        "penilaian" -> Intent(applicationContext, HomeWorkPage::class.java)
                        else -> Intent(applicationContext, HomePage::class.java)
                    }
                else {
                    if (menu.isNotEmpty()) pref.putString("notif_goto", menu)
                    Intent(applicationContext, Loginpage::class.java)
                }
            )
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        pref.putString("token", token)
        if (pref.getString("user_uuid").isNotEmpty()) {
            component.apiWrapper.updateFcmAsync()
        }
    }

    override fun onCreate() {
        super.onCreate()

        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override fun onDestroy() {
        super.onDestroy()

        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    private lateinit var lifecycleRegistry: LifecycleRegistry
    override fun getLifecycle(): Lifecycle = lifecycleRegistry
}

@Keep
@JsonClass(generateAdapter = true)
data class NotifPage(val id: Int = 0, val menu: String = "", val uuid: String? = "")