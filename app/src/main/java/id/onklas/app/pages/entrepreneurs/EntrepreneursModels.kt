package id.onklas.app.pages.entrepreneurs

import androidx.annotation.Keep
import androidx.annotation.Nullable
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString
import id.onklas.app.pages.sekolah.store.MerchantItem
import org.jetbrains.annotations.NotNull
import javax.microedition.khronos.opengles.GL10

@JsonClass(generateAdapter = true)
@Keep
data class MenuItemTable(
        @NullToEmptyString var menu_id: String = "",
        @NullToEmptyString var icon: Int,
        @NullToEmptyString var name: String = "",
        @NullToEmptyString var data: String = "", // cth: 1 produk, 10000-> untuk uang, 12/2 -> untuk produk
        @NullToEmptyString var satuan: String = "",//jika produk diisi "" ,  jika uang di'isi ->currency<- , jika jumlah di isi ""
)


@JsonClass(generateAdapter = true)
@Keep
data class Product(
        @NullToEmptyString var product_id: String = "",
        @NullToEmptyString var product_name: String = "",
        @NullToEmptyString var product_img: String = "",
        @NullToEmptyString var product_rating: String = "",
        @NullToEmptyString var product_sold: String = "",
        @NullToEmptyString var product_price: String = "",
)


@JsonClass(generateAdapter = true)
@Keep
data class HistoryBuy(
        @NullToEmptyString var id: String = "",
        @NullToEmptyString var date: String = "",
        @NullToEmptyString var product: List<ProductBuy>
)

@JsonClass(generateAdapter = true)
@Keep
data class ProductBuy(
        @NullToEmptyString var product_id: String = "",
        @NullToEmptyString var product_name: String = "",
        @NullToEmptyString var product_img: String = "",
        @NullToEmptyString var product_total: Int = 0,
        @NullToEmptyString var product_total_price: Double = 0.0,
)

@JsonClass(generateAdapter = true)
@Keep
data class MyProduct(
        @NullToEmptyString var product_id: String = "",
        @NullToEmptyString var product_name: String = "",
        @NullToEmptyString var product_img: String = "",
        @NullToEmptyString var product_stock: Int = 0,
        var product_price: Int = 0,
        @NullToEmptyString var product_status: Int, // non aktif = 0 ,ditayangkan = 1, menunggu verifikasi = 2, ditolak = 3
)


data class ProductAddImg(
        @PrimaryKey(autoGenerate = true) var imgId: Int,
        var path: String = "",
        var size: String = "",
        var type: String = "",
        var name: String = "",
        var width: Int = 0,
        var height: Int = 0,
)


@JsonClass(generateAdapter = true)
@Keep
data class CategoryParent(
        @NullToEmptyString var category_parent_id: Int = 0,
        @NullToEmptyString var category_icon: String = "",
        @NullToEmptyString var category_name: String = "",
        @NullToEmptyString var CategoryChild: List<CategoryChild>,
        @NullToEmptyString var order: Int = 0,
        @NullToEmptyString var is_selected: String,

        )

@JsonClass(generateAdapter = true)
@Keep
data class CategoryChild(
        @NullToEmptyString var category_parent_id: Int = 0,
        @NullToEmptyString var category_id: Int = 0,
        @NullToEmptyString var category_icon: String = "",
        @NullToEmptyString var category_name: String = "",
        @NullToEmptyString var order: Int = 0
)

data class MarketingLocation(
        val id: Int = 0,
        val name: String = "",
        val Required: Boolean, //wajib atau tidak
        val selected: Boolean = false
)

@JsonClass(generateAdapter = true)
@Keep
data class AcceptRejectResponse(
        val data: AcceptRejectItem,
)

@JsonClass(generateAdapter = true)
@Keep
data class AcceptRejectItem(
        val id: Int,
        val transaction_date: String,
        val buyer_name: String,
        val buyer_school: String,
        val buyer_type: String,
        val goodies: List<TransaksiGoodies>,
        val origin_province: String,
        val destination_sub_district: String,
        val destination_city: String,
        val destination_province: String,
        val destination_address: String,
        val goodies_count: Int,
        val sub_total: Int,
        val courier_name: String,
        val courier_fee: Int,
        val total: Int,
        @NullToEmptyString val rejected_reason: String,
        val status: String,
)

@JsonClass(generateAdapter = true)
@Keep
data class AcceptCancleBuyerResponse(
        val data: AcceptCancleBuyerItem,
)

@JsonClass(generateAdapter = true)
@Keep
data class AcceptCancleBuyerItem(
        val id: Int,
        val user_id: Int,
        val enterpreneur_merchant_id: Int,
        val total: Int,
        val status: String,
)


@JsonClass(generateAdapter = true)
@Keep
data class TransaksiResponse(
        val data: List<TransaksiItem>,
)

//@JsonClass(generateAdapter = true)
//@Keep
//data class DetailReviewResponse (
//        val data: TransaksiItem,
//)

@JsonClass(generateAdapter = true)
@Keep
data class TransaksiItem(
        val id: Int,
        val buyer_name: String,
        val status: String,
        val date: String,
        val goodies: List<TransaksiGoodies>,
        val goodies_count: Int,
        val sub_total: Int,
        val courier_name: String,
        val courier_fee: Int,
        val total: Int,
        val merchant: MerchantItem?,
        @NullToEmptyString val destination_address: String = "",
)

@JsonClass(generateAdapter = true)
@Keep
data class MerchantItem(
        val id: Int,
        val buyer_name: String,
        val status: String,
        val date: String,
        val goodies: List<TransaksiGoodies>,
        val goodies_count: Int,
        val sub_total: Int,
        val courier_name: String,
        val courier_fee: Int,
        val total: Int,
        @NullToEmptyString val destination_address: String = "",
)

@JsonClass(generateAdapter = true)
@Keep
data class TransaksiGoodies(
        val id:Int = 0,
        val goody_id: Int,
        val goody_image: String,
        val goody_name: String,
        val goody_price: Int,
        val goody_quantity: Int,
        val rating:Int? = 0,
        @NullToEmptyString val comment:String = "",
        val review_user:ReviewDataItem?,
        val review_merchant:ReviewDataItem?,
)

@JsonClass(generateAdapter = true)
@Keep
data class ReviewDataItem(
        val id:Int = 0,
        val user: User,
        val rating :Int,
        val comment:String,
        val date:String,
)

@JsonClass(generateAdapter = true)
@Keep
data class User(
        val id:Int=0,
        val uuid:String="",
        val name: String="",
        val user_username: String="",
        val email: String="",
        val nis_nik: Int=0,
        val phone:Long? = 0,
        val user_avatar_image:String = "",
        val avatar:String = "",
)


// detail transaksi

@JsonClass(generateAdapter = true)
@Keep
data class DetailTransaksiResponse(
        val data: DetailTransaksiItem,
)

@JsonClass(generateAdapter = true)
@Keep
data class DetailTransaksiItem(
        val id: Int,
        @NullToEmptyString val transaction_date: String = "",
        @NullToEmptyString val date: String = "",//buyer
        @NullToEmptyString val buyer_name: String = "",
        @NullToEmptyString val buyer_school: String = "",
        @NullToEmptyString val buyer_type: String = "",
        val goodies: List<TransaksiGoodies>,
        @NullToEmptyString val origin_sub_district: String= "",
        @NullToEmptyString val origin_city: String= "",
        @NullToEmptyString val origin_province: String= "",
        @NullToEmptyString val destination_sub_district: String= "",
        @NullToEmptyString val destination_city: String= "",
        @NullToEmptyString val destination_province: String= "",
        @NullToEmptyString val destination_address: String= "",
        val goodies_count: Int = 0,
        val sub_total: Int = 0,
        @NullToEmptyString val courier_name: String = "",
        val courier_fee: Int = 0,
        @NullToEmptyString val shipping_name: String = "",//buyer
        val shipping_fee: Int = 0,//buyer
        val total: Int,
        val status: String,
        val merchant: MerchantItem?,
)


// buyer detail transakai
@JsonClass(generateAdapter = true)
@Keep
data class DetailTransaksiBuyerResponse(
        val data: DetailTransaksiBuyerItem,
)

@JsonClass(generateAdapter = true)
@Keep
data class DetailTransaksiBuyerItem(
        val id: Int,
        val status: String,
        val date: String,
        @NullToEmptyString val shipping_name: String= "",
        val shipping_fee: Int,
        @NullToEmptyString val origin_sub_district: String = "",
        @NullToEmptyString val origin_city: String = "",
        @NullToEmptyString val origin_province: String = "",
        @NullToEmptyString val destination_sub_district: String= "",
        @NullToEmptyString val destination_city: String= "",
        @NullToEmptyString val destination_province: String= "",
        @NullToEmptyString val destination_address: String= "",
        val sub_total: Int,
        val total: Int,
        val merchant: MerchantItem?,
        val goodies: List<TransaksiGoodies>
)

//tracking
@JsonClass(generateAdapter = true)
@Keep
data class TrackingDetailResponse(
        val data: List<TrackingDetailItem>,
)

@JsonClass(generateAdapter = true)
@Keep
data class TrackingDetailItem(
        val code: String,
        val description: String,
        val datetime: String,
)

@JsonClass(generateAdapter = true)
@Keep
data class InputResiResponse(
        val error: String,
        val message: String,
        val waybill: String,
        val courier: String,
)



