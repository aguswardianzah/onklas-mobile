package id.onklas.app.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.onklas.app.R
import id.onklas.app.di.component
import id.onklas.app.pages.akm.AkmPage
import id.onklas.app.pages.akm.AkmStatus
import id.onklas.app.pages.akm.ExamType
import id.onklas.app.pages.login.Loginpage
import timber.log.Timber

class AkmUploader(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val akmId = inputData.getInt("id", 0)

        if (akmId < 1) return Result.success()
        val db = component.memoryDB
        val akmDao = db.akm()
        val akm = db.akm().getSingle(akmId) ?: return Result.success()
        Timber.e("upload jawaban, id: $akmId, workId: $id, examId: ${akm.schedule.student_exam_id}, dateEnd: ${akm.schedule.date_end}")

        if (akm.schedule.student_exam_id < 1 || akm.schedule.status > AkmStatus.AKM_STATUS_UPLOADED) return Result.success()

        return try {
            val api = component.apiService
            val pref = component.preference

            val list = mutableListOf<Any>()
            db.withTransaction {
                akmDao.insert(akm.schedule.apply { status = AkmStatus.AKM_STATUS_FINISHED })
                akmDao.getExamInstructionAsync(akmId).forEach { examInst ->
                    examInst.instructions.forEach { inst ->
                        akmDao.getInstQuestion(inst.id).questions.filter { it.answered }
                            .forEach { question ->
                                val data = mutableMapOf(
                                    "instruction_id" to inst.id,
                                    "question_id" to question.id,
                                    "answerType" to question.type_label,
                                    "answer" to when (question.type_label) {
                                        "MULTIPLE CHOICE" -> akmDao.getSelectedAnswers(question.id)
                                            .firstOrNull()?.id ?: 0
                                        "STATEMENT" -> akmDao.getAnswers(question.id).map {
                                            mapOf(
                                                "id" to it.id,
                                                "isTrue" to if (it.is_true) 1 else 0,
                                                "answered" to if (it.selected) 1 else 0
                                            )
                                        }
                                        "PAIR" -> akmDao.getAnswers(question.id).map {
                                            mapOf(
                                                "id" to it.id,
                                                "answered" to it.selected_id
                                            )
                                        }
                                        else -> question.answer_essay
                                    }
                                )
                                list.add(data)
                            }
                    }
                }
            }

            if (akm.schedule.exam_type == ExamType.TRYOUT)
                api.uploadJawabanTryOut(akmId, akm.schedule.student_exam_id, list)
            if (akm.schedule.is_school_scope)
                api.uploadJawabanUjianSchool(akmId, akm.schedule.student_exam_id, list)
            else
                api.uploadJawabanAkm(akmId, akm.schedule.student_exam_id, list)

            val notifContent = "Jawaban ujian ${akm.schedule.name} telah berhasil dikirim"
            val builder =
                NotificationCompat.Builder(
                    applicationContext,
                    appContext.getString(R.string.app_name)
                )
                    .setSmallIcon(R.drawable.ic_logo_notif)
                    .setContentTitle("Jawaban terkirim")
                    .setContentText(notifContent)
                    .setAutoCancel(true)
                    .setStyle(
                        NotificationCompat
                            .BigTextStyle()
                            .bigText(notifContent)
                    )
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            applicationContext,
                            0,
                            Intent(
                                applicationContext,
                                if (pref.getBoolean("logged_in")) AkmPage::class.java else Loginpage::class.java
                            )
                                .putExtra("goto", AkmPage::class.qualifiedName)
                                .putExtra("isSchoolScope", akm.schedule.is_school_scope),
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )

            NotificationManagerCompat.from(applicationContext)
                .notify("akm_upload_${akm.schedule.id}".hashCode(), builder.build())

            akmDao.insert(akm.schedule.copy(status = AkmStatus.AKM_STATUS_UPLOADED))

            Result.success()
        } catch (e: Exception) {
            Timber.e(e)
//            if (e is ApiException && e.responseCode?.div(100)?.equals(4) == true) {
//                Result.failure()
//            } else
            Result.retry()
        }
    }
}