package id.onklas.app.pages.entrepreneurs

import androidx.room.*
import id.onklas.app.di.modules.NullToEmptyString


@Entity(tableName = "kwu_merchant")
data class MerchantTable(
    @PrimaryKey val id: Int = 0,
    val merchant_name: String,
    val avatar: String,
    val owner_name: String,
    val role_id: Int,
    val role_Name: String,
    val school_id: Int,
    val school_name: String,
    val class_id: Int,
    val class_name: String,
    val class_major_id: Int,
    val class_major_name: String,
    val sales_rating: Int = 0,
    val amount_of_review: Int = 0,
    val success_transaction: Int = 0,
    var klaspay_id: String = "",
    var chat_id: String = ""
)

@Entity(tableName = "kwu_merchant_summary")
data class MerchantSummaryTable(
    @PrimaryKey val summaryCode: String = "",
    val merchantId:Int = 0 ,
    val product: Int = 0 ,
    val incoming_order: Int = 0,
    val incoming_amount: Int = 0,
    val review: Int = 0,
    val history_order: Int = 0,
    val purchase :Int = 0,
    val reviewable_order_purchase : Int = 0
)

@Entity(tableName = "kwu_order")
data class TransaksiTable(
    @PrimaryKey val id: Int = 0,
    val buyer_name: String,
    val status: String,
    val page: String, // seller ->incoming , processed , riwayat || buyer -> processed , done
    val role: String, //  sellerIncoming dan buyer
    val date: String,
    val date_formater: String?,
    val date_formater2: String?, //  untuk filter tanggal
    val goodies_count: Int,
    val sub_total: Int,
    val courier_name: String,
    val courier_fee: Int,
    val total: Int,
    val destination_address: String,
    val seller_name:String,
    val seller_img:String,
)

@Entity(tableName = "kwu_product")
data class TransaksiProductTable(
    @PrimaryKey val id: Int,
    val goody_id: Int,
    val order_id: Int,
    val goody_image: String,
    val goody_name: String,
    val goody_price: Int,
    val goody_quantity: Int,
    val goody_review_id:Int = 0,
)


data class TransactionWithProduct(
    @Embedded
    val order: TransaksiTable,
    @Relation(parentColumn = "id", entityColumn = "order_id")
    val product: List<TransaksiProductTable> = emptyList(),
)

@Entity(tableName = "kwu_detail_order")
data class DetailTransaksi(
    @PrimaryKey val id: Int,
    val date: String,
    val role: String,
    val buyer: String,
    val seller: String,
    val seller_avatar: String,
    val courrier_name: String,
    val subtotal: Int,
    val courier_fee: Int,
    val total: Int,
    val status: String,
    val destination_sub_district: String,
    val destination_city: String,
    val destination_province: String,
    val destination_address: String,
)

@Entity(tableName = "kwu_review_data")
data class ReviewData(
        @PrimaryKey val id: Int=0, //reviewId+goodyid+dataid
        val goody_review_id:Int = 0,
        val order_id: Int =0,
        val goody_id: Int =0,
        val tipe:String,
        val rating:Int = 0,
        val comment:String = "",
        val data_id:Int, // data == merchant/ user
        val data_name:String,
        val data_avatar:String,
        val date :String = "",
)

@Entity(tableName = "kwu_review_user")
data class ReviewUserTable(
        @PrimaryKey(autoGenerate = true) val id: Int=0,
        val goody_review_id:Int = 0,
        val rating:Int = 0,
        val comment:String = "",
        val user_id:Int,
        val user_name:String,
        val user_avatar:String,
)
@Entity(tableName = "kwu_review_merchant")
data class ReviewMerchantTable(
        @PrimaryKey(autoGenerate = true) val id: Int=0,
        val goody_review_id:Int = 0,
        val rating:Int = 0,
        val comment:String = "",
        val merchant_id:Int,
        val merchant_name:String,
        val merchant_avatar:String,
)

data class DetailTransactionWithProduct(
    @Embedded
    val detailOrder: DetailTransaksi,
    @Relation(parentColumn = "id", entityColumn = "order_id")
    val product: List<TransaksiProductTable> = emptyList(),
    @Relation(parentColumn = "id",
            entity = TransaksiProductTable::class,
            entityColumn = "order_id")
    val product_with_review: List<ProductWithReview> = emptyList() ,
    )
data class ProductWithReview(
        @Embedded
        val product: TransaksiProductTable,
//        @Relation(parentColumn = "goody_review_id", entityColumn = "goody_review_id")
//        val review_user: ReviewUserTable? ,
//        @Relation(parentColumn = "goody_review_id", entityColumn = "goody_review_id")
//        val review_merchant: ReviewMerchantTable? ,
        @Relation(parentColumn = "goody_review_id", entityColumn = "goody_review_id")
        val review_data: List<ReviewData> ,
)


@Entity(tableName = "kwu_tracking_detail")
data class TrackingDetail(
    @PrimaryKey (autoGenerate = true) val id: Int = 0,
    val transaksi_id: Int,
    val code: String,
    val description: String,
    val datetime: String,
)

