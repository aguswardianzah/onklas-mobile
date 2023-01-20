package id.onklas.app.pages.tryout

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString


@JsonClass(generateAdapter = true)
@Keep
data class ListTryoutResponse(
    val data: List<ListTryoutData> = emptyList()
)

@JsonClass(generateAdapter = true)
@Keep
data class ListTryoutData(
    val id: Int = 0,
    @NullToEmptyString val level: String = "",
    val isActive: Int? = null,
    @NullToEmptyString val date: String = "",
    @NullToEmptyString val startAt: String = "",
    @NullToEmptyString val endAt: String = "",
    val show_score: Boolean = true,
    val isDone: Boolean = false,
    val isAssessed: Boolean = false,
    val exams: List<TryoutExams>? = emptyList(),
    val score: List<TryoutScore> = emptyList(),
    @NullToEmptyString val type: String = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class TryoutExams(
    val id: Int = 0,
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val type: String = "",
    val grade: Any? = 0,
    val author: TryoutAuthor? = TryoutAuthor(),
    val numberOfQuestions: Int = 0,
    var score: Int = 0,
    var schoolType: List<String>? = emptyList(),
    @NullToEmptyString var schoolCities: String = "",
)

@JsonClass(generateAdapter = true)
@Keep
data class TryoutScore(
    val id: Int = 0,
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val type: String = "",
    val scored: Int = 0
)

@JsonClass(generateAdapter = true)
@Keep
data class TryoutAuthor(
    val id: Int = 0,
    @NullToEmptyString val nip: String = "",
    @NullToEmptyString val name: String = ""
)



