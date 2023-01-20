package id.onklas.app.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.onklas.app.R
import id.onklas.app.di.component
import id.onklas.app.pages.login.Loginpage
import id.onklas.app.pages.ujian.UjianPage
import kotlinx.coroutines.delay
import timber.log.Timber

class ExamEndWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val persistentDB by lazy { component.persistentDB }
    private val pref by lazy { component.preference }

    override suspend fun doWork(): Result {
        val id = inputData.getString("id")?.toInt() ?: return Result.success()
        val name = inputData.getString("name").orEmpty()

        return try {
//            persistentDB.ujian().sendUjian(id)
            val builder =
                NotificationCompat.Builder(
                    applicationContext,
                    appContext.getString(R.string.app_name)
                )
                    .setSmallIcon(R.drawable.ic_logo_notif)
                    .setContentTitle("Mengumpulkan Ujian")
                    .setContentText("Proses mendapatkan nilai dari Ujian $name")
                    .setAutoCancel(true)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("Proses mendapatkan nilai dari Ujian $name")
                    )
                    .setProgress(100, 0, true)
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
                .notify("ujian_$id".hashCode(), builder.build())

            endExam(id)
            if (answerExam(id)) {
//            persistentDB.ujian().endUjian(id)
                persistentDB.ujian().deleteTestCompletely(id)

                NotificationManagerCompat.from(applicationContext)
                    .cancel("ujian_$id".hashCode())
                Result.success()
            } else {
                NotificationManagerCompat.from(applicationContext)
                    .cancel("ujian_$id".hashCode())
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e)
            Result.retry()
        }
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