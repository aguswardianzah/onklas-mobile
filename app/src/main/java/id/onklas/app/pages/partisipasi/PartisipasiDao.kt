package id.onklas.app.pages.partisipasi

import androidx.paging.DataSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PartisipasiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: PartisipasiItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<PartisipasiItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: PartisipasiPayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(data: List<PartisipasiPayment>)

    @Query("select * from partisipasi where is_active = 1 and is_expired = 0 order by deadline")
    fun pagingOngoing(): DataSource.Factory<Int, PartisipasiItem>

    @Query("select count(*) from partisipasi where is_active = 1 and is_expired = 0")
    suspend fun countOngoing(): Int

    @Query("select * from partisipasi where is_active = 1 and is_expired = 1 order by deadline")
    fun pagingFinished(): DataSource.Factory<Int, PartisipasiItem>

    @Query("select count(*) from partisipasi where is_active = 1 and is_expired = 1 ")
    suspend fun countFinished(): Int

    @Query("select * from partisipasi where id = :id")
    fun getSingle(id: String): Flow<PartisipasiItem>

    @Query("select * from partisipasi_payment where id = :paymentId")
    suspend fun payment(paymentId: String): PartisipasiPayment?

    @Query("select * from partisipasi_payment where partisipasi_id = :partisipasiId order by date")
    suspend fun payments(partisipasiId: String): List<PartisipasiPayment>

    @Query("delete from partisipasi")
    suspend fun nukeItem()

    @Query("delete from partisipasi_payment")
    suspend fun nukePayment()

    @Transaction
    suspend fun nuke() {
        nukeItem()
        nukePayment()
    }
}