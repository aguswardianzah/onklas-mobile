package id.onklas.app.pages.studentcard

import android.graphics.Color
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import id.onklas.app.BuildConfig
import id.onklas.app.di.modules.NullToEmptyString

@JsonClass(generateAdapter = true)
@Keep
data class TemplateResponse(
    val data: TemplateData = TemplateData(),
    val school: SchoolTemplate = SchoolTemplate()
)

@JsonClass(generateAdapter = true)
@Keep
data class TemplateData(
    val id: Int = 0,
    @NullToEmptyString val theme: String = "",
    @NullToEmptyString val sign_file: String = "",
    @NullToEmptyString val back_title: String = "",
    @NullToEmptyString val back_content: String = "",
    @NullToEmptyString val instruction: String = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class SchoolTemplate(@NullToEmptyString val address: String = "")

data class ThemeData(
    val file: String = "",
    val textColor: Int = Color.BLACK,
    val titleColor: Int = Color.BLACK
)

val cmsUrl by lazy { if (BuildConfig.BUILD_TYPE == "release") "https://sekolah.onklas.id/pengguna" else "https://dev.sekolah.onklas.id" }

val themes by lazy {
    mapOf(
        "theme_1" to ThemeData(
            "$cmsUrl/img/template_1.jpg",
            Color.BLACK,
            Color.parseColor("#002FAA")
        ),
        "theme_2" to ThemeData(
            "$cmsUrl/img/template_2.jpg",
            Color.BLACK,
            Color.parseColor("#2C51B0")
        ),
        "theme_3" to ThemeData(
            "$cmsUrl/img/template_3.jpg",
            Color.BLACK,
            Color.parseColor("#0D6CDC")
        ),
        "theme_4" to ThemeData(
            "$cmsUrl/img/template_4.jpg",
            Color.BLACK,
            Color.parseColor("#002FAA")
        ),
        "theme_5" to ThemeData(
            "$cmsUrl/img/template_5.jpg",
            Color.BLACK,
            Color.parseColor("#357D00")
        ),
        "theme_6" to ThemeData(
            "$cmsUrl/img/template_6.jpg",
            Color.BLACK,
            Color.parseColor("#E47900")
        ),
        "theme_7" to ThemeData(
            "$cmsUrl/img/template_7.jpg",
            Color.WHITE,
            Color.parseColor("#FFD200")
        ),
        "theme_8" to ThemeData(
            "$cmsUrl/img/template_8.jpg",
            Color.WHITE,
            Color.parseColor("#00FFF0")
        ),
        "theme_9" to ThemeData(
            "$cmsUrl/img/template_9.jpg",
            Color.WHITE,
            Color.parseColor("#FF2E2E")
        ),
        "theme_10" to ThemeData(
            "$cmsUrl/img/template_10.jpg",
            Color.WHITE,
            Color.parseColor("#FFE81D")
        ),
        "theme_11" to ThemeData(
            "$cmsUrl/img/template_11.jpg",
            Color.WHITE,
            Color.parseColor("#FFE978")
        ),
        "theme_12" to ThemeData(
            "$cmsUrl/img/template_12.jpg",
            Color.WHITE,
            Color.parseColor("#FFE978")
        )
    )
}