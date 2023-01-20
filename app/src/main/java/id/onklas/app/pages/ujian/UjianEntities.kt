package id.onklas.app.pages.ujian

import androidx.annotation.Keep
import androidx.room.*
import com.squareup.moshi.JsonClass

@Entity(tableName = "exam", indices = [Index(value = ["id"], name = "exam_idx", unique = true)])
data class ExamTable(
    @PrimaryKey val id: Int = 0,
    val mapelIcon: String = "",
    val mapelName: String = "",
    val teacherName: String = "",
    val subject: String = "",
    val password: String = "",
    val date: String = "",
    val dateLabel: String = "",
    val startAt: String = "",
    val endAt: String = "",
    val time: String = "",
    val status: Int = 0 /* [0: Not started, 1: Preparation, 2: Started, 3: Ended] */,
    val message: String = "",
    val scored: Boolean = false,
    val score: Int = 0,
    val downloaded: Boolean = false,
    var doneAt: Long = 0
)

@Entity(
    tableName = "exam_questions",
    indices = [Index(name = "exam_question_idx", value = ["id"], unique = true)]
)
data class QuestionTable(
    @PrimaryKey val id: Int = 0,
    val testId: Int = 0,
    val question: String = "",
    val image: String = "",
    val order: Int = 0,
    var answered: Boolean = false,
    val is_correct: Boolean = false
)

@Entity(
    tableName = "exam_answers",
    indices = [Index(name = "exam_answers_idx", value = ["id"], unique = true)]
)
data class AnswerTable(
    @PrimaryKey val id: Int = 0,
    val testId: Int = 0,
    val qId: Int = 0,
    val answer: String = "",
    val image: String = "",
    val isCorrect: Boolean = false
)

@Keep
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "exam_my_answers",
    indices = [Index("qId", "testId", unique = true)]
)
data class MyAnswerTable(
    @PrimaryKey val qId: Int = 0,
    val answerId: Int = 0,
    val testId: Int = 0,
    val answerEssay: String = ""
)

data class QuestionAnswered(
    @Embedded val question: QuestionTable,
    @Relation(parentColumn = "id", entityColumn = "qId")
    val answers: List<AnswerTable> = emptyList(),
    @Relation(parentColumn = "id", entityColumn = "qId")
    var myAnswer: MyAnswerTable?
)