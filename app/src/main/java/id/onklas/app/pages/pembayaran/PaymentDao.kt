package id.onklas.app.pages.pembayaran

import androidx.paging.DataSource
import androidx.room.*

@Dao
interface PaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: PaymentInvoice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<PaymentInvoice>)

    @Update(entity = PaymentInvoice::class)
    suspend fun update(data: PaymentInvoiceFromDetail)

    @Query("delete from payment_invoice where trx_id = :idTrx")
    suspend fun delete(idTrx: String)

    @Query("select * from payment_invoice where trx_id = :trxId limit 1")
    suspend fun getSingle(trxId: String = ""): PaymentInvoice?

    @Query("select count(*) from payment_invoice where trx_id = :trxId")
    suspend fun count(trxId: String = ""): Int

    @Query("select * from payment_invoice where is_paid = 0 order by created_at desc")
    fun listInvoice(): DataSource.Factory<Int, PaymentInvoice>

    @Query("select count(*) from payment_invoice where is_paid = 0")
    suspend fun countInvoice(): Int

    @Query("select * from payment_invoice where is_paid order by created_at desc")
    fun paidInvoice(): DataSource.Factory<Int, PaymentInvoice>

    @Query("select count(*) from payment_invoice where is_paid")
    suspend fun countPaid(): Int

    @Query("select channel_icon from payment_invoice where channel = :channel limit 1")
    suspend fun channelIcon(channel: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuideType(item: PaymentGuideType): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuideItem(item: List<PaymentGuideItem>)

    @Query("select updated_at from pay_guide_channel where channel = :channel limit 1")
    suspend fun lastUpdate(channel: String): Long?

    @Transaction
    @Query("select * from pay_guide_channel where channel = :channel")
    suspend fun paymentGuide(channel: String): List<PaymentGuideChannelItems>
}