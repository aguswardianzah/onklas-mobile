package id.onklas.app.pages.entrepreneurs

import androidx.paging.DataSource
import androidx.room.*
import id.onklas.app.pages.login.SekolahItem
import id.onklas.app.pages.sekolah.store.CategoryTable
import kotlinx.coroutines.flow.Flow

@Dao
interface EnterepreneurDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(data: TransaksiTable)

    @Query("DELETE FROM kwu_order WHERE page = :page and role =:role")
    suspend fun delete(page: String,role: String)

    @Query("select count(*) from kwu_order where page = :page and role = :role  and date_formater2 LIKE '%' || :dateFilter || '%' ")
    suspend fun countListSellerOrder(page: String,role: String,dateFilter:String): Int


    @Transaction
    @Query("select * from kwu_order where page = :page and role = :role and date_formater2 LIKE '%' || :dateFilter || '%'  order by date_formater DESC ")
    fun getListSellerOrder(page: String,role:String,dateFilter:String):  DataSource.Factory<Int, TransactionWithProduct>


    @Transaction
    @Query("select * from kwu_order where page = :page and role = :role and status = :status and date_formater2 LIKE '%' || :dateFilter || '%' order by date_formater DESC")
    fun getIncomeSeller(page: String,role:String,status:String,dateFilter:String):  DataSource.Factory<Int, TransactionWithProduct>


    @Query("select count(*) from kwu_order where page = :page and role = :role and status = :status and date_formater2 LIKE '%' || :dateFilter || '%' ")
    suspend fun countIncomeSeller(page: String,role: String,status: String,dateFilter:String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderProduct(data: TransaksiProductTable)

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertReviewUser(data: ReviewUserTable)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertReviewMerchant(data: ReviewMerchantTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewData(data: ReviewData)



    @Query("select count(*) from kwu_order where page = 'incoming'")
    suspend fun countIncomingIn() :Int

    @Query("select count(*) from kwu_order where page = 'processed'")
    suspend fun countIncomingProcessed() :Int


    @Query("select count(*) from kwu_order where role = 'buyer' and page ='processed' ")
    suspend fun countOutcomingProcessed() :Int

    @Query("select count(*) from kwu_order where role =  'buyer'  and page = 'done' ")
    suspend fun countOutcomingDone() :Int


    // detail transaksi

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetailTransaksi(data: DetailTransaksi)

    @Query("delete from kwu_detail_order")
    suspend fun DeleteDetailTransaksi()

    @Transaction
    @Query("select * from kwu_detail_order where id = :orderId limit 1")
    fun getDetailTransaksi(orderId:Int): Flow<DetailTransactionWithProduct>

    @Query("select count(*) from kwu_detail_order where id = :orderId ")
    suspend fun countDetailTransaksi(orderId:Int):  Int

    @Query("select * from kwu_review_data where goody_review_id  = :goodyReviewId and order_id = :orderId and goody_id = :goodyId and tipe =:tipe")
    fun getReviewData(orderId:Int, goodyReviewId:Int,goodyId:Int,tipe:String):  Flow<ReviewData>



    // merchant

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insetMerchant(data: MerchantTable)

    @Query("delete from kwu_merchant")
    suspend fun deleteMerchant()

    @Query("select count(*) from kwu_merchant where id = :MerchantId")
    suspend fun countMerchant(MerchantId: String): Int

    @Query("select * from kwu_merchant ")
    fun getMerchant():  Flow<MerchantTable>


    // merchnat summary


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insetMerchantSummary(data: MerchantSummaryTable)

    @Query("delete from kwu_merchant_summary")
    suspend fun deleteMerchantSummary()

    @Query("select count(*) from kwu_merchant_summary where summaryCode = :SuCode  and merchantId = :MerchantId")
    suspend fun countMerchantSummary(SuCode :String, MerchantId: Int): Int

    @Query("select * from kwu_merchant_summary where summaryCode = :SuCode and merchantId = :MerchantId")
    fun getMerchantSummary(SuCode: String, MerchantId: Int):  Flow<MerchantSummaryTable>


    // tracking detail

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun InsetTrackingDetail(data: TrackingDetail)

    @Query("delete from kwu_tracking_detail where transaksi_id = :transaksiId")
    suspend fun DeleteTrackingDetail(transaksiId:Int)

    @Transaction
    @Query("select * from kwu_tracking_detail where transaksi_id = :transaksiId ")
    fun getTrackingDetail(transaksiId:Int): Flow<List<TrackingDetail>>

    @Query("select count(*) from kwu_tracking_detail where transaksi_id = :transaksiId ")
    suspend fun countTrackingDetail(transaksiId:Int):  Int










}