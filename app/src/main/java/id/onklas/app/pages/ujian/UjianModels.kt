package id.onklas.app.pages.ujian

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString
import id.onklas.app.pages.theory.MapelItem
import id.onklas.app.pages.theory.TeacherItem

@Keep
@JsonClass(generateAdapter = true)
data class TestData(
    val id: Int = 0,
    @NullToEmptyString val message: String = "",
    val ready_to_download: Boolean = false,
    val ready_to_start: Boolean = false,
    val ready_to_end: Boolean = false,
    val layout: TestLayout = TestLayout()
)

@Keep
@JsonClass(generateAdapter = true)
data class TestLayout(
    @NullToEmptyString val password: String = "",
    @NullToEmptyString val date: String = "",
    @NullToEmptyString val date_human: String = "",
    @NullToEmptyString val start_at: String = "",
    @NullToEmptyString val end_at: String = "",
    @NullToEmptyString val subject: String = "",
    @NullToEmptyString val icon_image: String = "",
    @NullToEmptyString val teacher: String = "",
    val score: Int = 0
)

@Keep
@JsonClass(generateAdapter = true)
data class TestDetailResponse(val data: TestDetailData = TestDetailData())

@Keep
@JsonClass(generateAdapter = true)
data class TestDetailData(
    val id: Int = 0,
    val start_at: String = "",
    val end_at: String = "",
    val template: ExamTemplate = ExamTemplate()
)

@Keep
@JsonClass(generateAdapter = true)
data class ExamTemplate(
    val id: Int = 0,
    val name: String = "",
    val subject: MapelItem = MapelItem(),
    val teacher: TeacherItem = TeacherItem(),
    val questions: List<QuestionItem> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class TestStudentResponse(val data: List<TestData> = emptyList())

@Keep
@JsonClass(generateAdapter = true)
data class QuestionResponse(val data: QuestionData = QuestionData())

@Keep
@JsonClass(generateAdapter = true)
data class QuestionData(val id: Int = 0, val template: QuestionTemplate = QuestionTemplate())

@Keep
@JsonClass(generateAdapter = true)
data class QuestionTemplate(val questions: List<QuestionItem> = emptyList())

@Keep
@JsonClass(generateAdapter = true)
data class DownloadSoalResponse(val data: List<QuestionItem> = emptyList())

@Keep
@JsonClass(generateAdapter = true)
data class QuestionItem(
    val id: Int = 0,
    val layout: QuestionLayout = QuestionLayout()
)

@Keep
@JsonClass(generateAdapter = true)
data class QuestionLayout(
    val question: String? = "",
    @NullToEmptyString val image: String = "",
    val choices: List<AnswerItem> = emptyList(),
    @NullToEmptyString val essay_answer: String = "",
    @NullToEmptyString val is_essay_answer_true: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class AnswerItem(
    val id: Int = 0,
    @NullToEmptyString val answer: String = "",
    val is_true: Boolean = false,
    @NullToEmptyString var file_path: String = "",
    val answered: Boolean = false
)