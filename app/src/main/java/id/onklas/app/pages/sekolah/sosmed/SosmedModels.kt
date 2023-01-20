package id.onklas.app.pages.sekolah.sosmed

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString

@Keep
@JsonClass(generateAdapter = true)
data class FeedResponse(val data: List<FeedItem> = emptyList())

@Keep
@JsonClass(generateAdapter = true)
data class FeedSingleResponse(val data: FeedItem = FeedItem())

@Keep
@JsonClass(generateAdapter = true)
data class FeedCommentResponse(val data: List<FeedComment> = emptyList())

@Keep
@JsonClass(generateAdapter = true)
data class FeedUserResponse(val data: List<FeedUser> = emptyList())

@Keep
@JsonClass(generateAdapter = true)
data class FeedUsernameResponse(val data: FeedUser = FeedUser())

@Keep
@JsonClass(generateAdapter = true)
data class FeedLikeResponse(val data: List<FeedLike> = emptyList())

@Keep
@JsonClass(generateAdapter = true)
data class FeedItem(
    @Json(name = "id") val row_id: Int = 0,
    @Json(name = "row_id") val id: Int = 0,
    @NullToEmptyString val feed_type: String = "image",
    @NullToEmptyString val created_at: String = "",
    @NullToEmptyString val created_at_label: String = "",
    @NullToEmptyString val feed_title: String = "",
    @NullToEmptyString val feed_body: String = "",
    @NullToEmptyString val feed_author: String = "",
    @NullToEmptyString val feed_thumbnail_image: String = "",
    val users: FeedUser = FeedUser(),
    val file: FeedFileItem = FeedFileItem(),
    val count_comments: Int = 0,
    val count_likes: Int = 0,
    val likes: List<FeedLike> = emptyList(),
    val comments: List<FeedComment> = emptyList(),
    val is_likes: Boolean = false
)

@Keep
@JsonClass(generateAdapter = true)
data class GetUserResp(val data: FeedUser = FeedUser())

@Keep
@JsonClass(generateAdapter = true)
data class UserItem(
    val user_id: Int = 0,
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val email: String = "",
    @NullToEmptyString val phone: String = "",
    @NullToEmptyString val user_avatar_image: String = "",
    @NullToEmptyString val id: String = "",
    @NullToEmptyString val nisn_nik: String = "",
    @NullToEmptyString val user_username: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class FeedUser(
    val id: Int = 0,
    @NullToEmptyString val uuid: String = "",
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val email: String = "",
    @NullToEmptyString val user_username: String = "",
    @NullToEmptyString val nisn_nik: String = "",
    @NullToEmptyString val nis_nik: String = "",
    @NullToEmptyString val phone: String = "",
    @NullToEmptyString val user_avatar_image: String = "",
    val is_verified: Boolean = false
)

@Keep
@JsonClass(generateAdapter = true)
data class FeedComment(
    val user: FeedUser = FeedUser(),
    @Json(name = "id") val row_id: Int = 0,
    @NullToEmptyString val feed_comments_body: String = "",
    @NullToEmptyString val created_at_label: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class FeedLike(
    val id: Int = 0,
    val created_at_label: String = "",
    val user: FeedUser = FeedUser()
)

@Keep
@JsonClass(generateAdapter = true)
data class FeedFileItem(
    @NullToEmptyString val feed_files_name: String = "",
    @NullToEmptyString val feed_files_path: String = "",
    @NullToEmptyString val feed_files_size: String? = "",
    @NullToEmptyString val feed_files_type: String = "",
    @NullToEmptyString val feed_files_format: String = "",
    val feed_files_width: String = "",
    val feed_files_height: String = "",
    val feed_files_id: Int = 0
)

@Keep
@JsonClass(generateAdapter = true)
data class HashtagResponse(val data: List<HashtagTable> = emptyList())

@Keep
@JsonClass(generateAdapter = true)
@Entity(tableName = "hashtag", indices = [Index("name", unique = true)])
data class HashtagTable(
    @PrimaryKey val name: String = "",
    val total: Int = 0
)

@Keep
@JsonClass(generateAdapter = true)
data class PolicyResponse(val data: PolicyData = PolicyData())

@Keep
@JsonClass(generateAdapter = true)
data class PolicyData(val content: String = "")
