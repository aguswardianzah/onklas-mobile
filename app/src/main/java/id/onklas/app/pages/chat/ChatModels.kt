package id.onklas.app.pages.chat

import androidx.annotation.Keep
import androidx.room.*
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString
import java.util.*

object ChatType {
    const val CHAT_TYPE_MESSAGE = 0
    const val CHAT_TYPE_INFO = 1
    const val CHAT_TYPE_DATE = 2
    const val CHAT_TYPE_PRODUCT = 3
    const val CHAT_TYPE_KWU_TRX = 4
}

object ChatStatus {
    const val CHAT_STATUS_NEW = 0
    const val CHAT_STATUS_SENT = 1
    const val CHAT_STATUS_RECEIVED = 2
    const val CHAT_STATUS_READ = 3
    const val CHAT_STATUS_DELETED = 4
}

@Entity(tableName = "chat", indices = [Index("to", unique = false)])
data class ChatItem(
    @PrimaryKey val id: String = "",
    val with: String = "",
    val date: Date = Date(),
    val first: Boolean = false,
    val from: String = "",
    val to: String = "",
    val message: String = "",
    var type: Int = ChatType.CHAT_TYPE_MESSAGE,
    var status: Int = ChatStatus.CHAT_STATUS_NEW,
    val is_deleted: Boolean = false,
    var processed: Boolean = false,
    var show_notif: Boolean = true,
    var product_id: Int = 0,
    var product_name: String = "",
    var product_image: String = "",
    var product_price: Int = 0
)

object ChatPresence {
    const val PRESENCE_ONLINE = "available"
    const val PRESENCE_OFFLINE = "unavailable"
    const val PRESENCE_TYPING = "composing"
}

@Entity(tableName = "conversation")
data class ConversationItem(
    @PrimaryKey val with: String = "",
    var name: String = "",
    var img_profile: String = "",
    var last_chat_id: String = "",
    var unread: Int = 0,
    var last_update: Date = Date(),
    var presence: String = ChatPresence.PRESENCE_OFFLINE,
    var is_open: Boolean = false,
    var show_notif: Boolean = true,
    var type: String = ""
)

data class ConversationWithLastMessage(
    @Embedded
    val conversation: ConversationItem,
    @Relation(parentColumn = "last_chat_id", entityColumn = "id")
    val chat: ChatItem?
)

@JsonClass(generateAdapter = true)
@Keep
data class PresenceItem(
    @NullToEmptyString val id: String = "",
    @NullToEmptyString val type: String = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class ChatResponse(
    @NullToEmptyString var cmd: String = "",
    @NullToEmptyString val id: String = "",
    @NullToEmptyString val text: String = "",
    @NullToEmptyString val from: String = "",
    @NullToEmptyString val to: String = "",
    var ack: Int = 0,
    @NullToEmptyString var event: String = "",
    val created_t: Long = System.currentTimeMillis(),
    val type: String = "default", // default|marketplace
    val payload: String = ""
) {

    constructor(item: ChatItem) : this(
        id = item.id,
        text = item.message,
        from = item.from,
        to = item.to
    )
}

@JsonClass(generateAdapter = true)
@Keep
data class ContactResponse(val data_list: List<ContactItem> = emptyList())

@JsonClass(generateAdapter = true)
@Keep
data class ContactItem(
    val user_id: Int = 0,
    @NullToEmptyString val user_uuid: String = "",
    @NullToEmptyString val foto: String = "",
    @NullToEmptyString val wallet_id: String = "",
    @NullToEmptyString val nisn: String = "",
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val kelas: String = "",
    @NullToEmptyString val jurusan: String = "",
)
