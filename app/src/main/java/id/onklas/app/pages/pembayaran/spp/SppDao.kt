package id.onklas.app.pages.pembayaran.spp

import androidx.paging.DataSource
import androidx.room.*

@Dao
interface SppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<SppTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: SppTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(data: SppProcess)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoicePayment(data: SppProcessCrossRef)

    @Query("select * from spp where paid_at is null or paid_at = '' order by issued_at")
    fun getTagihanSpp(): DataSource.Factory<Int, SppTable>

    @Query("select count(*) from spp where paid_at is null or paid_at = ''")
    suspend fun countTagihanSpp(): Int

    @Query("select * from spp where paid_at != '' order by issued_at")
    fun getPaidSpp(): DataSource.Factory<Int, SppTable>

    @Query("select count(*) from spp where paid_at != ''")
    suspend fun countPaidSpp(): Int

    @Transaction
    @Query("select * from spp_payment_cross_ref order by payment_id desc")
    fun getPaymentSpp(): DataSource.Factory<Int, SppInvoicePayment>

    @Query("delete from spp where paid_at is null or paid_at = ''")
    suspend fun deleteTagihanSpp()

    @Query("delete from spp where paid_at != ''")
    suspend fun deletePaidSpp()

    @Query("delete from spp")
    suspend fun deleteSpp()

    @Query("delete from spp_payment")
    suspend fun deletePaymentSpp()

    @Query("delete from spp_payment_cross_ref")
    suspend fun deleteInvoiceSpp()
}