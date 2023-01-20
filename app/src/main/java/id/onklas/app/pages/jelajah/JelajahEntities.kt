package id.onklas.app.pages.jelajah

import androidx.room.*
import id.onklas.app.pages.sekolah.sosmed.FeedFileTable
import id.onklas.app.pages.sekolah.sosmed.FeedTable
import id.onklas.app.pages.sekolah.sosmed.FeedUserCrossRef
import id.onklas.app.pages.sekolah.sosmed.UserTable

@Entity(
    tableName = "explore_feed",
    indices = [
        Index("feed_id", unique = true)
    ]
)
data class ExploreFeedTable(
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

data class FeedExplore(
    @Embedded val feed: ExploreFeedTable,
    @Relation(parentColumn = "feed_id", entityColumn = "feed_id")
    val files: List<FeedFileTable>,
    @Relation(parentColumn = "user_id_owner", entityColumn = "id")
    val userTable: UserTable,
    @Relation(
        parentColumn = "feed_id",
        entityColumn = "id",
        associateBy = Junction(FeedUserCrossRef::class)
    )
    val likes: List<UserTable>
)
