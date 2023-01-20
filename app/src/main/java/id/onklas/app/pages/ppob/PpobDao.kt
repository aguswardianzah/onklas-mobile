package id.onklas.app.pages.ppob

import androidx.room.*

@Dao
interface PpobDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: PpobTransaction)

    @Delete
    suspend fun delete(data: PpobTransaction)

    @Query("select * from ppob where createdAt = :createdAt limit 1")
    suspend fun getByCreatedAt(createdAt: Long): PpobTransaction?

    @Query("select * from ppob where trxId = :trxId limit 1")
    suspend fun getById(trxId: String): PpobTransaction?
}