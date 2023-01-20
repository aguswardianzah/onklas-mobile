package id.onklas.app.db

import android.content.Context
import androidx.room.withTransaction
import androidx.work.*
import com.squareup.moshi.Moshi
import id.onklas.app.pages.akm.*
import id.onklas.app.pages.announcement.AnnouncementResponse
import id.onklas.app.pages.announcement.AnnouncementTable
import id.onklas.app.pages.homework.HomeworkLink
import id.onklas.app.pages.homework.HomeworkLinkSource
import id.onklas.app.pages.homework.HomeworkResponse
import id.onklas.app.pages.homework.HomeworkTable
import id.onklas.app.pages.jelajah.ExploreFeedTable
import id.onklas.app.pages.klaspay.tagihan.KlaspayTagihanSppResult
import id.onklas.app.pages.login.SekolahItem
import id.onklas.app.pages.pembayaran.spp.*
import id.onklas.app.pages.perpus.BookRent
import id.onklas.app.pages.perpus.BookRentTable
import id.onklas.app.pages.sekolah.sosmed.*
import id.onklas.app.pages.theory.*
import id.onklas.app.pages.tryout.ListTryoutData
import id.onklas.app.pages.tryout.TryoutExams
import id.onklas.app.pages.ujian.*
import id.onklas.app.utils.DateUtil
import id.onklas.app.utils.FileUtils
import id.onklas.app.utils.PreferenceClass
import id.onklas.app.worker.AkmUploader
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

class OnKlasDbUtil @Inject constructor(
    val context: Context,
    val dateUtil: DateUtil,
    val memoryDB: MemoryDB,
    val persistDB: PersistentDB,
    val fileUtils: FileUtils,
    val pref: PreferenceClass,
    val moshi: Moshi
) {

    val school: SekolahItem by lazy {
        try {
            moshi.adapter(SekolahItem::class.java).fromJson(pref.getString("school"))
                ?: SekolahItem()
        } catch (e: Exception) {
            SekolahItem()
        }
    }

    suspend fun processFeedItem(
        feedItems: List<FeedItem>,
        rvWidth: Int,
        isTimeline: Boolean = true
    ): Boolean = try {
//        memoryDB.withTransaction {
        feedItems.forEach {
            val createdAt = dateUtil.formatDate(it.created_at)
            memoryDB.feed().insertUser(UserTable(it.users))
            val postId = if (it.row_id > 0) it.row_id else it.id
            if (isTimeline)
                memoryDB.feed().insert(
                    FeedTable(
                        postId,
                        createdAt,
                        it.feed_type,
                        it.feed_body,
                        it.users.id,
                        it.count_comments,
                        it.count_likes,
                        it.created_at_label,
                        it.is_likes,
                        it.feed_thumbnail_image,
                        it.feed_author
                    )
                )
            else
                memoryDB.explore().insert(
                    ExploreFeedTable(
                        postId,
                        createdAt,
                        it.feed_type,
                        it.feed_body,
                        it.users.id,
                        it.count_comments,
                        it.count_likes,
                        it.created_at_label,
                        it.is_likes,
                        it.feed_thumbnail_image,
                        it.feed_author
                    )
                )
            it.comments.forEach { c ->
                memoryDB.feed().insertUser(
                    UserTable(c.user)
                )
                memoryDB.feed().insertComment(
                    FeedCommentTable(
                        c.row_id,
                        postId,
                        c.feed_comments_body,
                        c.user.id,
                        c.created_at_label
                    )
                )
            }

            if (it.file.feed_files_path.isNotEmpty()) {
//                val file = File(
//                    context.filesDir, "post_${it.id}_${it.file.feed_files_id}"
//                ).also { file ->
//                    if (!file.exists()) file.createNewFile()
//                }
//
//                val filePath = if (fileUtils.downloadImage(it.file.feed_files_path, file))
//                    file.absolutePath
//                else it.file.feed_files_path

                val width: Int
                var height = 0
                if (it.feed_type != "ebook" && rvWidth > 0) {
                    width = try {
                        val res = it.file.feed_files_width.toInt()
                        if (res <= 0) rvWidth
                        else res
                    } catch (e: Exception) {
                        rvWidth
                    }
                    height = try {
                        val res = it.file.feed_files_height.toInt()
                        if (res <= 0) rvWidth
                        else res
                    } catch (e: Exception) {
                        rvWidth
                    }
                    height = rvWidth * height / width
                }

                memoryDB.feed().insertFile(
                    FeedFileTable(
                        0,
                        postId,
                        it.file.feed_files_path,
                        fileUtils.getStringSizeLengthFile(
                            try {
                                (it.file.feed_files_size ?: "0").toLong()
                            } catch (e: Exception) {
                                0L
                            }
                        ),
                        it.file.feed_files_type,
                        it.file.feed_files_name,
                        rvWidth,
                        height
                    )
                )
            }

            it.likes.forEach { l ->
                memoryDB.feed().insertUser(
                    UserTable(l.user)
                )
                memoryDB.feed()
                    .insertLike(
                        FeedUserCrossRef(
                            postId,
                            l.user.id,
                            l.created_at_label
                        )
                    )
            }
//            }
        }
        true
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    suspend fun processAnnouncementResponse(response: AnnouncementResponse): Boolean = try {
        if (response.data.isNotEmpty()) {
            memoryDB.announcement().insert(response.data.map {
                AnnouncementTable(
                    it.id.toLong(),
                    it.title,
                    it.body,
                    it.image,
                    it.created_at_label
                )
            })
            true
        } else false
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    suspend fun processMapelResponse(response: MapelResponse): Boolean = try {
        if (response.data.isNotEmpty()) {
            response.data.forEach {
                memoryDB.theory().insertMapel(MapelTable.fromMapelItem(it))
                if (it.teacher.id > 0) {
                    memoryDB.theory().insertTeacher(
                        TeacherTable.fromTeacherItem(it.teacher, it.teacher.user.name)
                    )
                    memoryDB.feed().insertUser(UserTable(it.teacher.user))
                }
            }
            true
        } else false
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    suspend fun processMateriResponse(response: MateriResponse, subjectId: Int = 0): Boolean = try {
        if (response.data.isNotEmpty()) {
            response.data.forEach {
//                if (it.school.id == school.id) {
                memoryDB.theory().insertMapel(MapelTable.fromMapelItem(it.subject))
                if (it.teacher.id > 0) {
                    memoryDB.theory().insertTeacher(
                        TeacherTable.fromTeacherItem(it.teacher, it.teacher.user.name)
                    )
                    memoryDB.feed().insertUser(UserTable(it.teacher.user))
                }

                memoryDB.theory().insertMateri(MateriTable.fromMateriItem(it))
                if (it.uri != null) {
                    memoryDB.theory().insertMateriLink(it.uri.link.map { link ->
                        MateriLinkTable(
                            0,
                            it.id.toLong(),
                            link
                        )
                    })
                }
//                }
            }
            true
        } else false
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    suspend fun processUploadMateriResponse(response: UploadMateriResponse): Boolean = try {
        if (response.data.id > 0) {
            memoryDB.theory().insertMateri(MateriTable.fromMateriItem(response.data))
            if (response.data.uri != null) {
                memoryDB.theory().insertMateriLink(response.data.uri.link.map { link ->
                    MateriLinkTable(
                        0,
                        response.data.id.toLong(),
                        link
                    )
                })
            }
            if (response.data.teacher.id > 0) {
                memoryDB.theory().insertTeacher(
                    TeacherTable.fromTeacherItem(
                        response.data.teacher,
                        response.data.teacher.user.name
                    )
                )
                memoryDB.feed().insertUser(UserTable(response.data.teacher.user))
            }
            true
        } else false
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    suspend fun processSppResponse(response: SppResponse): Boolean = try {
        memoryDB.spp().insert(response.data)
        true
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    //    private val formatDateSpp by lazy { SimpleDateFormat("dd-MMM-yyyy, HH:mm", Locale.ENGLISH) }
    suspend fun processKlaspayTagihanSpp(response: KlaspayTagihanSppResult) = try {
        memoryDB.spp().insert(response.data.transaction_invoice.map {
            SppTable(
                it.transaction_id.hashCode(),
                it.note,
//                dateFormatUjian.format(formatDateSpp.parse(it.created_date)),
                it.created_date,
                total_fee = it.price.toDouble(),
                paid_at = it.paid_date,
                transaction_id = it.transaction_id,
                is_inquiry_channel = it.is_inquiry_channel
            )
        })
        true
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    suspend fun processSppPaymentListRespose(response: SppProcessListResponse): Boolean = try {
        response.data.forEach {
            memoryDB.spp().insertPayment(
                SppProcess(
                    it.id,
                    it.jenis,
                    it.reffId,
                    it.paymentMethod,
                    it.payment_code,
                    it.payment_status_url,
                    it.expiredAt,
                    it.paidAt,
                    it.isPaid,
                    it.isExpired,
                    it.total
                )
            )
            it.school_fee_invoice.forEach { inv ->
//                memoryDB.spp().insert(inv)
                memoryDB.spp().insertInvoicePayment(SppProcessCrossRef(inv.id, it.id))
            }
        }
        true
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    suspend fun processHomeworkRespones(response: HomeworkResponse, type: Int = 0): Boolean = try {
        response.data.forEach {
            if (it.school.uuid == school.uuid) {
                memoryDB.homework().insertHomework(
                    HomeworkTable(
                        it.id,
                        it.title,
                        it.description,
                        type,
                        it.is_overdue,
                        it.checked,
                        it.downloded,
                        it.uploaded,
                        it.information_label,
                        it.file_name,
                        it.file_path,
                        it.file_format,
                        it.file_type,
                        it.file_size,
                        it.message_label,
                        it.end_at_label,
                        it.upload_at_label,
                        it.file_student_name,
                        it.file_student_path,
                        it.file_student_format,
                        it.file_student_type,
                        it.file_student_size,
                        it.score,
                        it.subject.id,
                        it.teacher.id,
                        it.teacher.user.id,
                        it.classRoom.id,
                        it.schedule.id,
                        it.schedule.day,
                        it.schedule.time_plot.start_at + " - " + it.schedule.time_plot.end_at
                    )
                )

                if (it.uri != null)
                    memoryDB.homework().insertHomeworkLink(it.uri.link.map { link ->
                        HomeworkLink(0, it.id, link)
                    })

                if (it.uri_student != null)
                    memoryDB.homework().insertHomeworkLink(it.uri_student.link.map { link ->
                        HomeworkLink(0, it.id, link, HomeworkLinkSource.SRC_STUDENT)
                    })

                memoryDB.theory()
                    .insertTeacher(TeacherTable.fromTeacherItem(it.teacher, it.teacher.user.name))
                memoryDB.theory().insertMapel(MapelTable.fromMapelItem(it.subject))
                memoryDB.theory().insertMapelTeacherCrossRef(
                    MapelTeacherCrossRef(
                        it.subject.id.toLong(),
                        it.teacher.id.toLong()
                    )
                )
                memoryDB.feed().insertUser(UserTable(it.teacher.user))
                memoryDB.homework().insertClassRoom(it.classRoom)
            }
        }
        true
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    suspend fun processListUjianStudent(response: TestStudentResponse): Boolean = try {
        val local = persistDB.ujian().getIds()
        memoryDB.ujian().insert(response.data.map {
            val isFinished = local.contains(it.id) && persistDB.ujian().get(it.id)?.status == 3
            ExamTable(
                it.id,
                it.layout.icon_image,
                it.layout.subject,
                it.layout.teacher,
                it.layout.subject,
                it.layout.password,
                it.layout.date,
                it.layout.date_human,
                it.layout.start_at,
                it.layout.end_at,
                it.layout.start_at + " - " + it.layout.end_at,
                if (isFinished) 3
                else if (!it.ready_to_download && !it.ready_to_start) 0
                else if (it.ready_to_download) 1
                else if (it.ready_to_start) 2
                else 3,
//                if (db.ujian().ujianEnded(it.id) == true) 3
//                else
//                    getStatusUjian(
//                        it.layout.date + " " + it.layout.start_at,
//                        it.layout.date + " " + it.layout.end_at
//                    )
//            1,
                if (isFinished) "Selesai" else it.message
            )
        })
        response.data.isNotEmpty()
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    suspend fun processUjianScoreStudent(response: TestStudentResponse): Boolean = try {
        response.data.forEach {
            val item = ExamTable(
                it.id,
                it.layout.icon_image,
                it.layout.subject,
                it.layout.teacher,
                it.layout.subject,
                it.layout.password,
                it.layout.date,
                it.layout.date_human,
                it.layout.start_at,
                it.layout.end_at,
                it.layout.start_at + " - " + it.layout.end_at,
                3,
                it.message,
                it.message.isNotEmpty(),
                it.layout.score
            )
            if (memoryDB.ujian().update(item) < 1)
                memoryDB.ujian().insert(item)

            try {
                File(context.filesDir, "exam-$it.id").also {
                    if (it.exists()) {
                        if (it.isDirectory) {
                            it.list()?.forEach { File(it).delete() }
                        }
                        it.delete()
                    }
                }
            } catch (e: Exception) {
            }
        }

        response.data.isNotEmpty()
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    suspend fun processDetailUjian(response: TestDetailResponse) = try {
        val testId = response.data.id
        response.data.template.questions.forEach {
            val qId = it.id
            memoryDB.ujian().insertQuestion(
                QuestionTable(
                    qId,
                    testId,
                    it.layout.question.orEmpty(),
                    it.layout.image,
                    0,
                    if (it.layout.choices.isNotEmpty()) it.layout.choices.any { it.answered } else it.layout.is_essay_answer_true.equals(
                        "true",
                        false
                    ),
                    it.layout.choices.isNotEmpty() && it.layout.choices.any { it.answered && it.is_true }
                )
            )

            if (it.layout.choices.isNotEmpty())
                it.layout.choices.forEach {
                    memoryDB.ujian().insertAnswer(
                        AnswerTable(
                            it.id,
                            testId,
                            qId,
                            it.answer,
                            it.file_path,
                            it.is_true
                        )
                    )
                    if (it.answered)
                        memoryDB.ujian().insertMyAnswer(MyAnswerTable(qId, it.id, testId))
                }
            else memoryDB.ujian()
                .insertMyAnswer(MyAnswerTable(qId, it.id, testId, it.layout.essay_answer))
        }
        true
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    private val dateFormatUjian by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("id")) }
    private fun getStatusUjian(start: String, end: String): Int = try {
        val calStart = Calendar.getInstance().apply { time = dateFormatUjian.parse(start) }
        val calEnd = Calendar.getInstance().apply { time = dateFormatUjian.parse(end) }
        val prepareTime = Calendar.getInstance().apply {
            time = dateFormatUjian.parse(start)
            add(Calendar.MINUTE, -30)
        }
        val currentTime = System.currentTimeMillis()
        if (currentTime < prepareTime.timeInMillis)
            0
        else if (currentTime >= prepareTime.timeInMillis && currentTime <= calStart.timeInMillis)
            1
        else if (currentTime >= calStart.timeInMillis && currentTime <= calEnd.timeInMillis)
            2
        else 3
    } catch (e: Exception) {
        Timber.e(e)
        2
    }

    suspend fun processSoalUjian(response: DownloadSoalResponse, testId: Int): Boolean = try {
        val data = response.data.filter { it.layout.question != null }
        val orders = List(data.size) { Random.nextInt(0, response.data.size) }
        data.forEachIndexed { index, it ->
            var imgUri = it.layout.image
            val folder =
                File(context.filesDir, "exam-$testId").also { if (!it.exists()) it.mkdir() }
            if (imgUri.isNotEmpty()) {
                try {
                    val file = File(
                        folder,
                        "${testId}_${it.id}.jpg"
                    ).also { if (!it.exists()) it.createNewFile() }
                    if (fileUtils.downloadImage(imgUri, file))
                        imgUri = file.absolutePath
                } catch (e: Exception) {
                }
            }

            val qid = it.id
            persistDB.ujian()
                .insertQuestion(
                    QuestionTable(
                        qid,
                        testId,
                        it.layout.question.orEmpty(),
                        imgUri,
                        orders[index]
                    )
                )

            if (it.layout.choices.isNotEmpty())
                persistDB.ujian().insertAnswer(it.layout.choices.shuffled().map {
                    if (it.file_path.isNotEmpty())
                        try {
                            val file = File(
                                folder,
                                "${testId}_${qid}_${it.id}.jpg"
                            ).also { if (!it.exists()) it.createNewFile() }
                            if (fileUtils.downloadImage(it.file_path, file))
                                it.file_path = file.absolutePath
                        } catch (e: Exception) {
                        }

                    AnswerTable(
                        it.id,
                        testId,
                        qid,
                        it.answer,
                        it.file_path,
                        it.is_true
                    )
                })
        }

        persistDB.ujian().setDownloaded(testId)
        response.data.isNotEmpty()
    } catch (e: Exception) {
        Timber.e(e)
        try {
            persistDB.ujian().deleteTestCompletely(testId)
        } catch (e: Exception) {
        }
        false
    }

    private val rentRespFormat by lazy { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    private val rentFormat by lazy { SimpleDateFormat("dd MMMM yyyy", Locale("id")) }
    suspend fun processBookRent(response: List<BookRent>) = try {
        memoryDB.perpus().insertRent(response.map {
            val returnDate = try {
                rentRespFormat.parse(it.retur_at)
            } catch (e: Exception) {
                Timber.e(e)
                null
            }
            val returnTime = returnDate?.time ?: 0L

            BookRentTable(
                it.id,
                it.start_at,
                if (returnTime > 0 && returnDate != null) rentFormat.format(returnDate) else it.retur_at,
                returnTime,
                if (it.status == "retur") 2 else if (it.is_late) 1 else 0,
                it.code,
                it.title,
                it.author,
                it.cover_image_url
            ).also { Timber.e("inserting $it") }
        })
        response.isNotEmpty()
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    private val akmDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("id")) }
    private val akmDateLabelFormat by lazy { SimpleDateFormat("dd MMMM yyyy", Locale("id")) }
    suspend fun processAkmResponse(
        response: List<ListAkmData?>,
        isSchoolScope: Boolean = false,
        examType: Int = ExamType.SCHOOL
    ) =
        try {
            val data = response.filterNotNull()
            memoryDB.withTransaction {
                val idStatus = memoryDB.akm().getIdAndStatus()
                val ids = idStatus.map { it.id }

                WorkManager.getInstance(context).cancelAllWorkByTag("akm_uploader")

                data.forEach {
                    val exams =
                        mutableListOf<AkmExams>().apply {
                            it.exams?.let { addAll(it) }
                            it.exam?.let { it1 -> add(it1) }
                        }

                    if (exams.isEmpty()) return@forEach

                    val dateStart = try {
                        akmDateFormat.parse("${it.date} ${it.startAt}") ?: Date()
                    } catch (e: Exception) {
                        Date()
                    }

                    val akmTable = AkmTable(
                        it,
                        when {
                            it.isAssessed -> AkmStatus.AKM_STATUS_SCORED
                            it.isDone -> AkmStatus.AKM_STATUS_FINISHED
                            it.isQueued -> AkmStatus.AKM_STATUS_UPLOADED
                            ids.contains(it.id) -> idStatus.first { ids -> ids.id == it.id }.status
                            else -> 0
                        },
                        exams.distinctBy { exam -> exam.name }
                            .joinToString(", ") { exam -> exam.name },
                        exams.filter { exam -> exam.numberOfQuestions > 0 }
                            .joinToString(", ") { exam -> exam.type },
                        dateStart,
                        try {
                            akmDateFormat.parse("${it.date} ${it.endAt}") ?: Date()
                        } catch (e: Exception) {
                            Date()
                        },
                        akmDateLabelFormat.format(dateStart), "${it.startAt} - ${it.endAt}",
                        show_score = it.showScore,
                        exam_type = examType,
                        exam_template = it.type
                    ).apply {
                        if (ids.contains(it.id))
                            student_exam_id =
                                idStatus.first { ids -> ids.id == it.id }.student_exam_id

                        password = it.password
                        is_school_scope = isSchoolScope
                    }

                    if (akmTable.status < AkmStatus.AKM_STATUS_UPLOADED) {
                        // insert worker to upload jawaban
                        val timeLeft = akmTable.date_end.time - System.currentTimeMillis()

                        val workRequest = OneTimeWorkRequestBuilder<AkmUploader>()
                            .setInputData(
                                workDataOf("id" to akmTable.id)
                            )
                            .setInitialDelay(timeLeft, TimeUnit.MILLISECONDS)
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
                            .addTag("akm_uploader")
                            .build()

                        WorkManager.getInstance(context)
                            .enqueueUniqueWork(
                                "akm_uploader_${akmTable.id}",
                                ExistingWorkPolicy.REPLACE,
                                workRequest
                            )

                        val workId = workRequest.id
                        akmTable.apply {
                            most_significant_bits = workId.mostSignificantBits
                            least_significant_bits = workId.leastSignificantBits
                        }
                    }

                    memoryDB.akm().insert(akmTable)

                    if (it.score.isNotEmpty())
                        it.score.forEach { score ->
                            exams.firstOrNull { it.id == score.id }?.score = score.scored
                        }
                    else if (it.exam_score != null && it.exam_score > -1) {
                        exams.onEach { exam -> exam.score = it.exam_score }

                        Timber.e("exam scored: $exams")
                    }

                    memoryDB.akm().insert(exams.filter { it.numberOfQuestions > 0 }
                        .map { exam -> AkmExamsTable(it.id, exam) })
                }

//            (ids - data.map { it.id }).forEach {
//                memoryDB.akm().delete(it)
//            }
            }

            data.isNotEmpty()
        } catch (e: Exception) {
            Timber.e(e)
            false
        }

    suspend fun processTryoutResponse(
        response: List<ListTryoutData?>,
        isSchoolScope: Boolean = false,
        examType: Int = ExamType.TRYOUT
    ) =
        try {
            val data = response.filterNotNull()
            memoryDB.withTransaction {
                val idStatus = memoryDB.akm().getIdAndStatus()
                val ids = idStatus.map { it.id }

                WorkManager.getInstance(context).cancelAllWorkByTag("akm_uploader")

                data.forEach {
                    val exams =
                        mutableListOf<TryoutExams>().apply {
                            it.exams?.let { addAll(it) }
//                            it.exam?.let { it1 -> add(it1) }
                        }

                    if (exams.isEmpty()) return@forEach

                    val dateStart = try {
                        akmDateFormat.parse("${it.date} ${it.startAt}") ?: Date()
                    } catch (e: Exception) {
                        Date()
                    }

                    val akmTable = AkmTable.tryoutToTabel(
                        it,
                        when {
                            it.isAssessed -> AkmStatus.AKM_STATUS_SCORED
                            it.isDone -> AkmStatus.AKM_STATUS_FINISHED
//                            it.isQueued -> AkmStatus.AKM_STATUS_UPLOADED
                            ids.contains(it.id) -> idStatus.first { ids -> ids.id == it.id }.status
                            else -> 0
                        },
                        exams.distinctBy { exam -> exam.name }
                            .joinToString(", ") { exam -> exam.name },
                        exams.filter { exam -> exam.numberOfQuestions > 0 }
                            .joinToString(", ") { exam -> exam.type },
                        dateStart,
                        try {
                            akmDateFormat.parse("${it.date} ${it.endAt}") ?: Date()
                        } catch (e: Exception) {
                            Date()
                        },
                        akmDateLabelFormat.format(dateStart), "${it.startAt} - ${it.endAt}",
                        show_score = it.show_score,
                        exam_type = examType,
                        exam_template = it.type
                    ).apply {
                        if (ids.contains(it.id))
                            student_exam_id =
                                idStatus.first { ids -> ids.id == it.id }.student_exam_id

//                        password = it.password
                        is_school_scope = isSchoolScope
                    }

                    if (akmTable.status < AkmStatus.AKM_STATUS_UPLOADED) {
                        // insert worker to upload jawaban
                        val timeLeft = akmTable.date_end.time - System.currentTimeMillis()

                        val workRequest = OneTimeWorkRequestBuilder<AkmUploader>()
                            .setInputData(
                                workDataOf("id" to akmTable.id)
                            )
                            .setInitialDelay(timeLeft, TimeUnit.MILLISECONDS)
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
                            .addTag("akm_uploader")
                            .build()

                        WorkManager.getInstance(context)
                            .enqueueUniqueWork(
                                "akm_uploader_${akmTable.id}",
                                ExistingWorkPolicy.REPLACE,
                                workRequest
                            )

                        val workId = workRequest.id
                        akmTable.apply {
                            most_significant_bits = workId.mostSignificantBits
                            least_significant_bits = workId.leastSignificantBits
                        }
                    }

                    memoryDB.akm().insert(akmTable)

                    if (it.score.isNotEmpty())
                        it.score.forEach { score ->
                            exams.firstOrNull { it.id == score.id }?.score = score.scored
                        }
//                    else if (it.exam_score != null && it.exam_score > -1) {
//                        exams.onEach { exam -> exam.score = it.exam_score }
//
//                        Timber.e("exam scored: $exams")
//                    }

                    memoryDB.akm().insert(exams.filter { it.numberOfQuestions > 0 }
                        .map { exam -> AkmExamsTable.tryoutToTabel(it.id, exam) })
                }

//            (ids - data.map { it.id }).forEach {
//                memoryDB.akm().delete(it)
//            }
            }

            data.isNotEmpty()
        } catch (e: Exception) {
            Timber.e(e)
            false
        }

}