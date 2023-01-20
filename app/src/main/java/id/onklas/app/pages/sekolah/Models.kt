package id.onklas.app.pages.sekolah

import androidx.annotation.Keep
import androidx.room.*
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "person",
    indices = [Index(name = "personIdx", value = ["personId"], unique = true)]
)
data class Person(@PrimaryKey val personId: Long, val name: String, val img: String)

@Keep
@JsonClass(generateAdapter = true)
data class PostItem(
    @Embedded val post: PostOnly,
    @Relation(parentColumn = "postId", entityColumn = "") val person: Person,
    @Relation(
        parentColumn = "postId",
        entityColumn = "personId"
    ) val media: List<PostMedia> = emptyList(),
    val likes: List<Person> = emptyList(),
    val comments: List<Comment> = emptyList()
)

//@Entity(tableName = "post", indices = [Index(name = "postidx", value = ["postId"], unique = true)])
@Keep
@JsonClass(generateAdapter = true)
data class PostOnly(
    @PrimaryKey
    val postId: Long = 0,
    val content: String = "",
    val time: Long = System.currentTimeMillis(),
    val liked: Boolean = false,
    @Transient @Ignore val timeString: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
//@Entity(
//    tableName = "post_media",
//    indices = [Index(name = "mediaIdx", value = ["mediaId"], unique = true)]
//)
data class PostMedia(
    @PrimaryKey val mediaId: Int,
    val mPostId: Long,
    val type: String,
    val url: String
)

@Keep
@JsonClass(generateAdapter = true)
//@Entity(
//    tableName = "post_comment",
//    indices = [Index(name = "commentIdx", value = ["commentId"], unique = true)]
//)
data class Comment(
    @PrimaryKey val commentId: Long,
    val mPostId: Long,
    val person: Person,
    val content: String,
    val time: Long = System.currentTimeMillis()
)