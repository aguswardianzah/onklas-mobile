package id.onklas.app.pages.announcement

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AnnouncementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<AnnouncementTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: AnnouncementTable): Long

    @Query("select * from announcement order by id desc")
    fun getAll(): DataSource.Factory<Int, AnnouncementTable>

    @Query("select * from announcement where id = :id")
    suspend fun getSingle(id: Int): AnnouncementTable

    @Query("select count(*) from announcement")
    suspend fun count(): Int
}