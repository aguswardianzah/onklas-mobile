package id.onklas.app.socket

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SocketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: SocketQueueItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<SocketQueueItem>): List<Long>

    @Query("select * from socket_queue where processed = 0")
    suspend fun getUnprocessedQueue(): List<SocketQueueItem>

    @Query("update socket_queue set processed = 1 where id = :id")
    suspend fun setProcessedQueue(id: Long)
}