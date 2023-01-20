package id.onklas.app.services

import android.app.IntentService
import android.content.Intent
import androidx.core.app.RemoteInput
import androidx.work.*
import id.onklas.app.di.component
import id.onklas.app.worker.ChatOutgoingHandler
import timber.log.Timber
import java.util.concurrent.TimeUnit

class DirectReplyChat : IntentService("directReplyChat") {

    private val stringUtil by lazy { component.stringUtil }
    private val pref by lazy { component.preference }
    private val myId by lazy { pref.getString("klaspay_id") }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return

        Timber.e("direct reply with data: ${intent.extras}")

        val reply =
            RemoteInput.getResultsFromIntent(intent)?.getCharSequence("reply_message").toString()
        Timber.e("user's reply: $reply")

        val with = intent.getStringExtra("with") ?: return
        val chatId = stringUtil.md5("$myId-$with-${System.currentTimeMillis()}")

        // start worker to handle outgoing message
        val workRequest = OneTimeWorkRequestBuilder<ChatOutgoingHandler>()
            .setInputData(
                workDataOf("with" to with, "reply" to reply, "chatId" to chatId)
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
            .addTag("chat_outgoing_handler")
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "chat_outgoing_handler_$chatId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }
}