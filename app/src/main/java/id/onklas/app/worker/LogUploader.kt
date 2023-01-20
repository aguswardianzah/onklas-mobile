package id.onklas.app.worker

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import id.onklas.app.di.component
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LogUploader(private val appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val pref by lazy { component.preference }

    override fun doWork(): Result {

        val userId = pref.getInt("user_id", 0)
        if (userId == 0) return Result.success()

        val dateLabel = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

        val logFileName = "${userId}_${dateLabel}.txt"
        val logFile = File(appContext.filesDir, logFileName)

        if (!logFile.exists()) return Result.success()

        return try {
            Tasks.await(
                Firebase
                    .storage
                    .reference
                    .child("logs/$logFileName")
                    .putFile(Uri.fromFile(logFile))
            )
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}