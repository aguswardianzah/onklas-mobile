package id.onklas.app.pages.akun

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PairingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<PairingTable>)

    @Query("select * from pairingTable ")
    fun getListPairing(): DataSource.Factory<Int, PairingTable>

    @Query("select count(*) from PairingTable")
    suspend fun countlistPairing(): Int

    @Query("DELETE FROM PairingTable")
    suspend fun delete()


}