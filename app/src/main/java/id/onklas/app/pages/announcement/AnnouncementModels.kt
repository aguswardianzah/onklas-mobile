package id.onklas.app.pages.announcement

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class AnnouncementItem(
    val id: Int = 0,
    val title: String = "",
    val body: String = "",
    val image: String = "",
    val created: String = "",
    val created_at_label: String = ""

)

@Keep
@JsonClass(generateAdapter = true)
data class AnnouncementResponse(
    val data: List<AnnouncementItem> = emptyList()
)

@Entity(
    tableName = "announcement",
    indices = [Index(name = "annIdx", unique = true, value = ["id"])]
)
data class AnnouncementTable(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String = "",
    val body: String = "",
    val image: String = "",
    val created_label: String = "",
    val img_res: Int = 0
)