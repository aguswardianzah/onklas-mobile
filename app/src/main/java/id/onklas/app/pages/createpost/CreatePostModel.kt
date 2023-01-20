package id.onklas.app.pages.createpost

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class CreatePostResponse(
    val data: CreatePostRespData = CreatePostRespData()
)

@Keep
@JsonClass(generateAdapter = true)
data class CreatePostRespData(
    val row_id: Int = 0,
    val feed_type: String = "image",
    val school_id: Int = 0,
    val user_id: Int = 0,
    val feed_body: String = "",
    val published: Int = 0,
    val actived: Int = 0,
    val updated_at: String = "",
    val created_at: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class UploadPostResponse(val data: UploadPostRespData = UploadPostRespData())

@Keep
@JsonClass(generateAdapter = true)
data class UploadPostRespData(
    val feed_id: String = "",
    val feed_files_name: String = "",
    val id: Int = 0
)