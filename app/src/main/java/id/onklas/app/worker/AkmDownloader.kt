package id.onklas.app.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import id.onklas.app.BuildConfig
import id.onklas.app.R
import id.onklas.app.di.component
import id.onklas.app.pages.akm.*
import id.onklas.app.pages.login.Loginpage
import timber.log.Timber
import java.io.File

class AkmDownloader(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val db by lazy { component.memoryDB }
    private val api by lazy { component.apiService }
    private val pref by lazy { component.preference }
    private val fileUtils by lazy { component.fileUtils }
    private val anyAdapter by lazy { component.moshi.adapter(Any::class.java) }

    override suspend fun doWork(): Result {
        val id = inputData.getInt("id", 0)

        if (id < 1) return Result.success()

        Timber.e("start akm download with id: $id")
        val akm = db.akm().getSingle(id) ?: return Result.failure()

        return try {
            val numQuestion = akm.exams.sumOf { it.num_question }

//            if (akm.schedule.status != AKM_STATUS_NEW)
//                return Result.success()

            db.akm().insert(akm.schedule.apply { status = AkmStatus.AKM_STATUS_DOWNLOADING })
            showProgress(akm, numQuestion, 0)

            var downloadProgress = 0
            val data = (
                    if(akm.schedule.exam_type == ExamType.SCHOOL){
                        if (akm.schedule.is_school_scope)
                            api.downloadSoalUjianSchool(id)
                        else
                            api.downloadSoalAkm(id)
                    }else{
                        api.downloadSoalUjianTryout(id)
                    }).data
            db.akm().insert(akm.schedule.apply { student_exam_id = data.studentExam.id })

            data.exams.forEach { exam ->
                if (exam.numberOfQuestions > 0 && exam.instructions.any { it.questions.isNotEmpty() }) {
                    db.akm()
                        .insertExam(AkmExamsTable(id, exam))
                }

                exam.instructions.forEach { inst ->
                    if (inst.questions.isNotEmpty())
                        db.akm().insertInstruction(AkmInstructionTable(exam.id, inst).apply {
                            num_question = inst.questions.size
                        })

                    inst.questions.forEach { question ->
                        if (question.image.isNotEmpty()) {
                            if (!question.image.startsWith("http")) {
                                question.image =
                                    "https://${if (BuildConfig.BUILD_TYPE != "release") "dev." else ""}assets.onklas.id/${question.image}"
                            }

                            question.image = downloadImage(
                                "akm-exam${exam.id}",
                                "q${question.id}.jpg",
                                question.image
                            ).absolutePath
                        }
                        db.akm().insertQuestion(
                            AkmQuestionTable(
                                inst.id,
                                question
                            )
                        )

                        if (question.answerType == "PAIR") {
                            val randIds = question.answers.map { it.id }.shuffled()
                            val newList = mutableListOf<AkmAnswer>()
                            question.answers.forEachIndexed { index, akmAnswer ->
                                val target = question.answers.first { it.id == randIds[index] }
                                newList.add(
                                    akmAnswer.copy(
                                        secondStatement = target.secondStatement,
                                        secondFilePath = target.secondFilePath,
                                        selected_id = randIds[index]
                                    )
                                )
                            }
                            question.answers = newList
                        }

                        question.answers.shuffled().forEachIndexed { index, answer ->
                            answer.answer = answer.answer.replaceFirst("<p>", "")
                            answer.firstStatement = answer.firstStatement.replaceFirst("<p>", "")
                            answer.secondStatement = answer.secondStatement.replaceFirst("<p>", "")

                            if (answer.filePath.isNotEmpty()) {
                                if (!answer.filePath.startsWith("http")) {
                                    answer.filePath =
                                        "https://${if (BuildConfig.BUILD_TYPE != "release") "dev." else ""}assets.onklas.id/${answer.filePath}"
                                }

                                answer.filePath = downloadImage(
                                    "akm-exam${exam.id}",
                                    "a${question.id}_${answer.id}.jpg",
                                    answer.filePath
                                ).absolutePath
                            }

                            if (answer.firstFilePath.isNotEmpty()) {
                                if (!answer.firstFilePath.startsWith("http")) {
                                    answer.firstFilePath =
                                        "https://${if (BuildConfig.BUILD_TYPE != "release") "dev." else ""}assets.onklas.id/${answer.firstFilePath}"
                                }

                                answer.firstFilePath = downloadImage(
                                    "akm-exam${exam.id}",
                                    "a${question.id}_${answer.id}_1.jpg",
                                    answer.firstFilePath
                                ).absolutePath
                            }

                            if (answer.secondFilePath.isNotEmpty()) {
                                if (!answer.secondFilePath.startsWith("http")) {
                                    answer.secondFilePath =
                                        "https://${if (BuildConfig.BUILD_TYPE != "release") "dev." else ""}assets.onklas.id/${answer.secondFilePath}"
                                }

                                answer.secondFilePath = downloadImage(
                                    "akm-exam${exam.id}",
                                    "a${question.id}_${answer.id}_2.jpg",
                                    answer.secondFilePath
                                ).absolutePath
                            }

                            db.akm().insertAnswer(
                                AkmAnswerTable(
                                    question.id,
                                    index,
                                    answer
                                )
                            )
                        }

                        downloadProgress++
                        showProgress(akm, numQuestion, downloadProgress)
                        db.akm()
                            .insert(akm.schedule.apply { download_progress = downloadProgress })
                    }
                }
            }

            NotificationManagerCompat.from(applicationContext).cancel("akm_download_$id".hashCode())
            db.akm().insert(akm.schedule.apply { status = AkmStatus.AKM_STATUS_DOWNLOADED })

            Result.success()
        } catch (e: Exception) {
            Timber.e(e)
            showDownloadFail(akm, e.message.orEmpty())
            db.akm().insert(akm.schedule.apply { status = AkmStatus.AKM_STATUS_NEW })
            Result.failure(workDataOf("message" to e.message))
        }
    }

    private suspend fun downloadImage(folderName: String, fileName: String, url: String): File {
        val folder = File(
            appContext.filesDir, folderName
        ).also { if (!it.exists()) it.mkdir() }

        val file = File(
            folder, fileName
        ).also { if (!it.exists()) it.createNewFile() }

        fileUtils.downloadImage(url, file)

        return file
    }

    private fun showProgress(akm: AkmSchedule, numQuestion: Int, progress: Int) {
        val notifContent = "Proses mendownload soal ($progress/$numQuestion)"
        val builder =
            NotificationCompat.Builder(
                applicationContext,
                "${appContext.getString(R.string.app_name)}.silent"
            )
                .setSmallIcon(R.drawable.ic_logo_notif)
                .setContentTitle("Mendownload soal AKM - ${akm.schedule.name}")
                .setProgress(numQuestion, progress, false)
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
                            if (pref.getBoolean("logged_in")) AkmDetailPage::class.java else Loginpage::class.java
                        )
                            .putExtra("goto", AkmDetailPage::class.qualifiedName)
                            .putExtra("id", akm.schedule.id),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )

        NotificationManagerCompat.from(applicationContext)
            .notify("akm_download_${akm.schedule.id}".hashCode(), builder.build())
    }

    private fun showDownloadFail(akm: AkmSchedule, message: String = "") {
        val notifContent =
            "Proses mendownload soal ${akm.schedule.name} gagal${if (message.isNotEmpty()) ", silahkan ulangi download soal beberapa saat lagi" else message}"
        val builder =
            NotificationCompat.Builder(
                applicationContext,
                "${appContext.getString(R.string.app_name)}.silent"
            )
                .setSmallIcon(R.drawable.ic_logo_notif)
                .setContentTitle("Gagal mendownload soal")
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
                            if (pref.getBoolean("logged_in")) AkmDetailPage::class.java else Loginpage::class.java
                        )
                            .putExtra("goto", AkmDetailPage::class.qualifiedName)
                            .putExtra("id", akm.schedule.id),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )

        NotificationManagerCompat.from(applicationContext)
            .notify("akm_download_${akm.schedule.id}".hashCode(), builder.build())
    }
}