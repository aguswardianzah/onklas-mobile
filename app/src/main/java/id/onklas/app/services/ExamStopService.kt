package id.onklas.app.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import id.onklas.app.App
import id.onklas.app.R
import id.onklas.app.di.component
import id.onklas.app.pages.login.Loginpage
import id.onklas.app.pages.ujian.TakeUjianPage
import id.onklas.app.pages.ujian.UjianPage
import id.onklas.app.worker.ExamEndWorker
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ExamStopService : Service() {

    private val memoryDB by lazy { component.memoryDB }
    private val persistDB by lazy { component.persistentDB }
    private val pref by lazy { component.preference }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getIntExtra("id", 0) ?: 0
        val name = intent?.getStringExtra("name").orEmpty()

        GlobalScope.launch {
            try {
                endExam(id)
                if (answerExam(id))
                    memoryDB.ujian().deleteTestCompletely(id)
                else
                    runWorker(id, name)
            } catch (e: Exception) {
                runWorker(id, name)
            }

//            memoryDB.ujian().endUjian(id)
            val builder =
                NotificationCompat.Builder(applicationContext, getString(R.string.app_name))
                    .setSmallIcon(R.drawable.ic_logo_notif)
                    .setContentTitle("Ujian Berakhir")
                    .setContentText("Ujian $name telah berakhir")
                    .setAutoCancel(true)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("Ujian $name telah berakhir")
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            applicationContext,
                            0,
                            Intent(
                                applicationContext,
                                if (pref.getBoolean("logged_in")) UjianPage::class.java else Loginpage::class.java
                            ).putExtra("goto", UjianPage::class.qualifiedName),
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )

            NotificationManagerCompat.from(applicationContext)
                .notify("ujian$id".hashCode(), builder.build())

            ((application as App).currentAct as? TakeUjianPage)?.finish()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private suspend fun runWorker(id: Int, name: String) {
        memoryDB.ujian().get(id)?.let { persistDB.ujian().insert(it) }
        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "ujian_$id",
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<ExamEndWorker>()
                    .setInputData(
                        workDataOf(
                            "id" to id,
                            "name" to name
                        )
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
                    .addTag("ujian_$id")
                    .build()
            )
    }

    private suspend fun answerExam(id: Int): Boolean = try {
        component.apiWrapper.answerExam(id.toString())
        true
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    private suspend fun endExam(id: Int): Boolean = try {
        component.apiService.endExam(id.toString())
        true
    } catch (e: Exception) {
        Timber.e(e)
        false
    }
}