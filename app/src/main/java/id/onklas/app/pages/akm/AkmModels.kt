package id.onklas.app.pages.akm

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString
import id.onklas.app.di.modules.ObjectToList

@JsonClass(generateAdapter = true)
@Keep
data class ListAkmResponse(
    val data: List<ListAkmData?> = emptyList()
)

@JsonClass(generateAdapter = true)
@Keep
data class DetailAkmResponse(
    val data: ListAkmData = ListAkmData()
)

@JsonClass(generateAdapter = true)
@Keep
data class ListAkmData(
    val id: Int = 0,
    val level: Int = 0,
    val isActive: Int = 0,
    @NullToEmptyString val password: String = "",
    @NullToEmptyString val date: String = "",
    @NullToEmptyString val startAt: String = "",
    @NullToEmptyString val endAt: String = "",
    @NullToEmptyString val status: String = "",
    val showScore: Boolean = true,
    val isDone: Boolean = false,
    val isAssessed: Boolean = false,
    val isQueued: Boolean = false,
    val participant: AkmParticipant = AkmParticipant(),
    val exam: AkmExams? = AkmExams(),
    val exams: List<AkmExams>? = emptyList(),
    val score: List<AkmScore> = emptyList(),
    val exam_score: Int? = -1,
    @NullToEmptyString val type:String = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class AkmParticipant(
    @NullToEmptyString val uuid: String = "",
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val nisn_nik: String = "",
    @NullToEmptyString val school: String = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class AkmExams(
    val id: Int = 0,
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val type: String = "",
    val grade: Any? = 0,
    val author: AkmAuthor? = AkmAuthor(),
    val numberOfQuestions: Int = 0,
    val instructions: List<AkmInstruction> = emptyList(),
    var score: Int = 0
)

@JsonClass(generateAdapter = true)
@Keep
data class AkmScore(
    val id: Int = 0,
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val type: String = "",
    val scored: Int = 0
)

@JsonClass(generateAdapter = true)
@Keep
data class AkmAuthor(
    val id: Int = 0,
    @NullToEmptyString val nip: String = "",
    @NullToEmptyString val name: String = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class AkmDownloadResponse(val data: AkmDownloadData = AkmDownloadData())

@JsonClass(generateAdapter = true)
@Keep
data class AkmDownloadData(
    val id: Int = 0,
    @ObjectToList val exams: List<AkmExams> = emptyList(),
    val studentExam: StudentExam = StudentExam()
)

@JsonClass(generateAdapter = true)
@Keep
data class StudentExam(val id: Int = 0, val student_id: Int = 0)

@JsonClass(generateAdapter = true)
@Keep
data class AkmInstruction(
    val id: Int = 0,
    @NullToEmptyString val instruction: String = "",
    @NullToEmptyString val description: String = "",
    val sequence: Int = 0,
    val isRandom: Int = 0,
    val questions: List<AkmQuestion> = emptyList()
)

@JsonClass(generateAdapter = true)
@Keep
data class AkmQuestion(
    val id: Int = 0,
    @NullToEmptyString val question: String = "",
    @NullToEmptyString var image: String = "",
    @NullToEmptyString val answerType: String = "",
//    val score: Int = 0,
    var answers: List<AkmAnswer> = emptyList()
)

@JsonClass(generateAdapter = true)
@Keep
data class AkmAnswer(
    val id: Int = 0,
    @NullToEmptyString var answer: String = "",
    @NullToEmptyString var filePath: String = "",
    val isTrue: Int = 0,
    val showFalse: Int = 1,
    @NullToEmptyString var firstStatement: String = "",
    @NullToEmptyString var firstFilePath: String = "",
    @NullToEmptyString var secondStatement: String = "",
    @NullToEmptyString var secondFilePath: String = "",
    var selected_id: Int = 0
)