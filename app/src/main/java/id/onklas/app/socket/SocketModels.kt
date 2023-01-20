package id.onklas.app.socket

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "socket_queue")
data class SocketQueueItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val event: String = "",
    val data: String = "",
    val processed: Boolean = false
)