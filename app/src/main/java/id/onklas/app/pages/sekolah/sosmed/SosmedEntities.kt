package id.onklas.app.pages.sekolah.sosmed

import androidx.annotation.Keep
import androidx.room.*
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString
import id.onklas.app.pages.chat.ContactItem

@Entity(
    tableName = "feed",
    indices = [
        Index(name = "feedIdx", value = ["feed_id"], unique = true),
        Index("user_id_owner", unique = false)
    ]
)
data class FeedTable(
    @PrimaryKey var feed_id: Int = 0,
    var created_at: Long = 0L,
    var feed_type: String = "image",
    var feed_body: String = "",
    var user_id_owner: Int = 0,
    var count_comments: Int = 0,
    var count_likes: Int = 0,
    var timeString: String = "",
    var isLike: Boolean = false,
    var feed_thumbnail_image: String = "",
    var feed_author: String = ""
)

@Entity(
    tableName = "feed_file",
//    foreignKeys = [ForeignKey(
//        entity = FeedTable::class,
//        parentColumns = ["feed_id"],
//        childColumns = ["feed_id"],
//        onDelete = ForeignKey.CASCADE
//    )],
    indices = [
        Index("feed_file_id", unique = true),
        Index("feed_id", "path", unique = true)
    ]
)
data class FeedFileTable(
    @PrimaryKey(autoGenerate = true) var feed_file_id: Int,
    var feed_id: Int = 0,
    var path: String = "",
    var size: String = "",
    var type: String = "",
    var name: String = "",
    var width: Int = 0,
    var height: Int = 0
)

@Entity(
    tableName = "feed_comment",
//    foreignKeys = [ForeignKey(
//        entity = FeedTable::class,
//        parentColumns = ["feed_id"],
//        childColumns = ["feed_id"],
//        onDelete = ForeignKey.CASCADE
//    )],
    indices = [
        Index("cid", unique = true),
        Index("feed_id", unique = false)
    ]
)
data class FeedCommentTable(
    @PrimaryKey(autoGenerate = true) var cid: Int,
    var feed_id: Int = 0,
    var comment: String = "",
    var commenter_id: Int = 0,
    var created_at_label: String = ""
)

data class FeedCommentWithUser(
    @Embedded val comment: FeedCommentTable,
    @Relation(parentColumn = "commenter_id", entityColumn = "id")
    val userTable: UserTable?
)

@Keep
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "user",
    indices = [Index("uuid", "id", unique = true)]
)
data class UserTable(
    @PrimaryKey var id: Int = 0,
    @NullToEmptyString var uuid: String = "",
    @NullToEmptyString var name: String = "",
    @NullToEmptyString var email: String = "",
    @NullToEmptyString var nisn_nik: String = "",
    @NullToEmptyString var nis_nik: String = "",
    @NullToEmptyString var phone: String = "",
    @NullToEmptyString var user_avatar_image: String = "",
    @NullToEmptyString var username: String = "",
    var school_id: Int = 0,
    val is_verified: Boolean = false,
    var wallet_id: String = "",
    var class_name: String = "",
    var majors: String = ""
) {

    constructor(item: FeedUser) : this(
        item.id,
        item.uuid,
        item.name,
        item.email,
        item.nisn_nik,
        item.nis_nik,
        item.phone,
        item.user_avatar_image,
        item.user_username,
        is_verified = item.is_verified
    )

    constructor(item: ContactItem) : this(
        item.user_id,
        item.user_uuid,
        item.name,
        user_avatar_image = item.foto,
        wallet_id = item.wallet_id,
        class_name = item.kelas,
        majors = item.jurusan
    )
}

@Entity(
    tableName = "feed_like",
//    foreignKeys = [
//        ForeignKey(
//            entity = FeedTable::class,
//            parentColumns = ["feed_id"],
//            childColumns = ["feed_id"],
//            onDelete = ForeignKey.CASCADE
//        ),
//        ForeignKey(
//            entity = UserTable::class,
//            parentColumns = ["id"],
//            childColumns = ["id"],
//            onDelete = ForeignKey.CASCADE
//        )
//    ],
    primaryKeys = ["feed_id", "id"], indices = [
        Index(name = "feeduserfeedIdx", value = ["feed_id"], unique = false),
        Index(name = "feeduseruserIdx", value = ["id"], unique = false)
    ]
)
data class FeedUserCrossRef(
    var feed_id: Int = 0,
    var id: Int = 0,
    val created_at_label: String = ""
)

data class FeedListLike(
    @Embedded val cross: FeedUserCrossRef,
    @Relation(parentColumn = "id", entityColumn = "id")
    val userTable: UserTable?
)

data class FeedTimeline(
    @Embedded val feed: FeedTable,
    @Relation(parentColumn = "feed_id", entityColumn = "feed_id")
    val files: List<FeedFileTable> = emptyList(),
    @Relation(parentColumn = "user_id_owner", entityColumn = "id")
    val userTable: UserTable,
    @Relation(
        parentColumn = "feed_id",
        entityColumn = "id",
        associateBy = Junction(FeedUserCrossRef::class)
    )
    val likes: List<UserTable> = emptyList()
)

data class FeedMedia(
    @Embedded val file: FeedFileTable,
    @Relation(parentColumn = "feed_id", entityColumn = "feed_id")
    val feed: FeedTable
)