package id.onklas.app.pages.sekolah.store

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString
import id.onklas.app.di.modules.ObjectToList

// Homepage list------------
@Keep
@JsonClass(generateAdapter = true)
data class HomepageResponse(
    val data: HomePageItem
)

@Keep
@JsonClass(generateAdapter = true)
data class HomePageItem(
    val category: List<CategoryItem> = emptyList(),
    val populer: List<HomeProductItem> = emptyList(),
    val newest: List<HomeProductItem> = emptyList(),
    val bestseller: List<HomeProductItem> = emptyList(),
    val bestprice: List<HomeProductItem> = emptyList(),
    val other_horizontal: List<HomeProductItem> = emptyList(),
    val other_vertical: List<HomeProductItem> = emptyList(),
    val recomendation: List<HomeProductItem> = emptyList(),
)

@Keep
@JsonClass(generateAdapter = true)
data class HomeProductItem(
    @Json(name = "id") val homeProductId: Int = 0,
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val description: String = "",
    val price: Int = 0,
    val stock: Int = 0,
    @NullToEmptyString val image: String = "",

    )

@Keep
@JsonClass(generateAdapter = true)
data class HomeProductItem2(
    @Json(name = "id") val homeProductId: Int = 0,
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val description: String = "",
    val price: Int = 0,
    val stock: Int = 0,
    @NullToEmptyString val image: String = "",
    val category: categoryProductItem,
    val sub_category: categoryProductItem
)

@Keep
@JsonClass(generateAdapter = true)
data class categoryProductItem(
    val id: Int,
    val name: String = "",
    val icon: String = "",
    val created_at: String = "",
    val updated_at: String = ""
)


// Homepage list------------


//category ----------------
@Keep
@JsonClass(generateAdapter = true)
data class CategoryResponse(
    val data: List<CategoryItem> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class CategoryItem(
    val id: Int,
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val icon: String = "",
    val is_selected: Boolean = false,
)

@Keep
@JsonClass(generateAdapter = true)
data class CategorySubResponse(
    val data: List<CategorySubItem> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class CategorySubItem(
    val id: Int,
    @NullToEmptyString val name: String = "",
    val data: List<CategoryItem> = emptyList()

)

@Keep
@JsonClass(generateAdapter = true)
data class CategoryProductResponse(
    val data: List<HomeProductItem2> = emptyList()
)

//category ----------------


//filter ------------
@Keep
@JsonClass(generateAdapter = true)
data class CountProductFilterResponse(
    val data: String
)

@Keep
@JsonClass(generateAdapter = true)
data class ResultGooidesFilterResponse(
    val data: List<GoodiesListItem>
)


//@Keep
//@JsonClass(generateAdapter = true)
//data class ResultGooidesFilterItem(
//        val id: Int,
//        val name: String,
//        val price: Int,
//        @NullToEmptyString val rating: String,
//        val product_sold: Int,
//        val main_image: List<ResultGoodiesImg>,
//)
//
//@Keep
//@JsonClass(generateAdapter = true)
//data class ResultGoodiesImg(
//        val id: Int,
//        val sequence: Int,
//        val image_original: String,
//        val image: String,
//)


// list goodie cart detai (homepage)
@Keep
@JsonClass(generateAdapter = true)
data class GoodieCardDetailResponse(
    val data: List<GoodiesListItem> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class GoodiesListItem(
    val id: Int,
    val name: String,
    val price: Int,
    @NullToEmptyString val rating: String = "",
    val product_sold: Int = 0,
    @ObjectToList val main_image: List<ImgItem>,
)

@Keep
@JsonClass(generateAdapter = true)
data class ImgItem(
    @NullToEmptyString val image: String,
)

// sugest


@Keep
@JsonClass(generateAdapter = true)
data class SuggestMerchantResponse(
    val data: List<SuggestMerchantItem>,
)

@Keep
@JsonClass(generateAdapter = true)
data class SuggestMerchantItem(
    val id: Int,
    val name: String,
    val avatar_original: String,
    val avatar: String,
    val school_name: String,
)

@Keep
@JsonClass(generateAdapter = true)
data class SuggestProductResponse(
    val data: List<SuggestProductItem>,
)

@Keep
@JsonClass(generateAdapter = true)
data class SuggestProductItem(
    val id: Int,
    val name: String,

    )

@Keep
@JsonClass(generateAdapter = true)
data class SearchResultResponse(
    val data: List<HomeProductItem>,
)


// goodie detail-----------
@JsonClass(generateAdapter = true)
@Keep
data class GoodieDetailResponse(
    val data: GoodieDetailItem
)

@Keep
@JsonClass(generateAdapter = true)
data class GoodieDetailItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: Int,
    val stock: Int,
    val weight: Int,
    val rating: String = "0",
    val sold: Int,
    val image: List<GoodieDetailImg>,
    val merchant: MerchantDetailGoodie?,
)

@Keep
@JsonClass(generateAdapter = true)
data class MerchantDetailGoodie(
    val id: Int,
    val name: String,
    val avatar: String,
    val owner: OwnerDetailGoodie?,
    val sales_rating: Int,
    val amount_of_reviewer: Int,
    val success_transactions: Int

)

@Keep
@JsonClass(generateAdapter = true)
data class OwnerDetailGoodie(
    val id: Int = 0,
    val name: String = "",
    val roles: List<RoleDetailGoodie?>,
    val school: SchoolDetailGoodie?,
    @Json(name = "class") val Kelas: ClassDetailGoodie?
)

@Keep
@JsonClass(generateAdapter = true)
data class RoleDetailGoodie(
    val id: Int = 0,
    val name: String = "",
)

@Keep
@JsonClass(generateAdapter = true)
data class SchoolDetailGoodie(
    val id: Int = 0,
    val name: String = "",
)

@Keep
@JsonClass(generateAdapter = true)
data class ClassDetailGoodie(
    val id: Int = 0,
    val name: String = "",
    val grade: Int = 0,
    val major: MajorDetailGoodie?
)

@Keep
@JsonClass(generateAdapter = true)
data class MajorDetailGoodie(
    val id: Int = 0,
    val name: String = "",
)


@Keep
@JsonClass(generateAdapter = true)
data class GoodieDetailImg(
    val id: Int,
    val sequence: Int,
    @NullToEmptyString val image_original: String,
    @NullToEmptyString val image: String
)

// review product
@JsonClass(generateAdapter = true)
@Keep
data class DetailReviewResponse(
    val data: DetailReviewItem
)

@JsonClass(generateAdapter = true)
@Keep
data class DetailReviewItem(
    val average_rating: String = "",
    val jumlah_reviewer: Int = 0
)

@JsonClass(generateAdapter = true)
@Keep
data class ListReviewResponse(
    val data: List<ListReviewItem>,
)

@JsonClass(generateAdapter = true)
@Keep
data class ListReviewItem(
    val user: UserReview?,
    val date: String = "",
    val rating: Int = 0,
    val comment: String = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class UserReview(
    val name: String,
    val avatar: String
)

// merchant goodie


@JsonClass(generateAdapter = true)
@Keep
data class MerchantResponse(
    val data: MerchantItem,
)

@JsonClass(generateAdapter = true)
@Keep
data class MerchantItem(
    val id: Int = 0,
    val name: String = "",
    val avatar: String = "",
    val owner: MerchantOwner?,

    @Json(name = "sales_rating")
    val salesRating: Int = 0,

    @Json(name = "amount_of_reviewer")
    val amountOfReviewer: Int = 0,

    @Json(name = "success_transactions")
    val successTransactions: Int = 0
)

@JsonClass(generateAdapter = true)
@Keep
data class MerchantOwner(
    val id: Int = 0,
    val name: String = "",
    val roles: List<MerchantRoles> = emptyList(),
    val school: MerchantSchool?,

    @Json(name = "class")
    val ownerClass: MerchantClassClass?
)

@JsonClass(generateAdapter = true)
@Keep
data class MerchantClassClass(
    val id: Int = 0,
    val name: String = "",
    val grade: Int = 0,
    val major: MerchantSchool?
)

@JsonClass(generateAdapter = true)
@Keep
data class MerchantSchool(
    val id: Int = 0,
    val name: String = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class MerchantRoles(
    val id: Int = 0,
    val name: String = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class MerchantProductResponse(
    val data: List<MerchantProductItem>,
)

@JsonClass(generateAdapter = true)
@Keep
data class MerchantProductItem(
    val id: Int,
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val description: String = "",
    val price: Int,
    val stock: Int,
    @NullToEmptyString val image: String = "",
    val sold: Int,
    @NullToEmptyString val rating: String = "",
    @NullToEmptyString val status: String = "",
    val published: Int = 0
)

@JsonClass(generateAdapter = true)
@Keep
data class MerchantSummary(
    val data: MerchantSummaryItem,
)

@JsonClass(generateAdapter = true)
@Keep
data class MerchantSummaryItem(
    val product: Int,
    val incoming_order: Int,
    val incoming_amount: Int,
    val review: Int,
    val history_order: Int,
)


@JsonClass(generateAdapter = true)
@Keep
data class MerchantPembelianSummary(
    val data: MerchantPembelianSummaryItem,
)

@JsonClass(generateAdapter = true)
@Keep
data class MerchantPembelianSummaryItem(
    val purchase: Int,
    val reviewable_order: Int,
)


//cart------------

//@Keep
//@JsonClass(generateAdapter = true)
//data class CartResponse(
//        val merchant: Merchant,
//        val goodies: List<Goody>
//)
//
//@Keep
//@JsonClass(generateAdapter = true)
//data class Goody(
//        @Json(name = "user_id") val userID: Int,
//        val goodie: Goodie,
//        val quantity: Int
//)
//
//@Keep
//@JsonClass(generateAdapter = true)
//data class Goodie(
//        val id: Int,
//        val name: String,
//        val description: String,
//        val price: Int,
//        val stock: Int,
//        val weight: Int,
//        val image: Image? = null
//)
//
//@Keep
//@JsonClass(generateAdapter = true)
//data class Image(
//        val id: Int,
//        val sequence: Int,
//        @Json(name = "image_original") val imageOriginal: String,
//        val image: String
//)
//
//@Keep
//@JsonClass(generateAdapter = true)
//data class Merchant(
//        val id: Int,
//        val name: String,
//        val avatar: String
//)


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
data class listSection(
    @NullToEmptyString var top_banner: String = "",
    @NullToEmptyString var id: String = "",
    @NullToEmptyString var product: List<Product>,
    @NullToEmptyString var order: Int = 0
)


@JsonClass(generateAdapter = true)
@Keep
data class CategoryParentModel(
    @NullToEmptyString var category_parent_id: Int = 0,
    @NullToEmptyString var category_icon: String = "",
    @NullToEmptyString var category_name: String = "",
    @NullToEmptyString var CategoryChild: List<CategoryChildModel>,
    @NullToEmptyString var order: Int = 0,
    @NullToEmptyString var is_selected: String,

    )

@JsonClass(generateAdapter = true)
@Keep
data class CategoryChildModel(
    @NullToEmptyString var category_parent_id: Int = 0,
    @NullToEmptyString var category_id: Int = 0,
    @NullToEmptyString var category_icon: String = "",
    @NullToEmptyString var category_name: String = "",
    @NullToEmptyString var order: Int = 0
)

@JsonClass(generateAdapter = true)
@Keep
data class StoreFilterModel(
    @NullToEmptyString var id: Int = 0,
    @NullToEmptyString var name: String = "",
    @NullToEmptyString var filter_code: String = "",
    @NullToEmptyString var is_selected: Boolean,
)


@JsonClass(generateAdapter = true)
@Keep
data class DetailProduct(
    @NullToEmptyString var product_id: Int = 0,
    @NullToEmptyString var name: String = "",
    @NullToEmptyString var product_img: List<ProductImg>,
    @NullToEmptyString var price: Double = 0.0,
    @NullToEmptyString var rating: Double = 0.0,
    @NullToEmptyString var product_sold: String = "", //string/ int
    @NullToEmptyString var stock: Int = 0,
    @NullToEmptyString var description: String = "",

    @NullToEmptyString var seller_img: String = "",
    @NullToEmptyString var seller_name: String = "",
    @NullToEmptyString var seller_id: String = "",
    @NullToEmptyString var seller_address: String = "",
)


@JsonClass(generateAdapter = true)
@Keep
data class ProductImg(
    @NullToEmptyString var id: Int = 0,
    @NullToEmptyString var imgUrl: String = "",
    @NullToEmptyString var order: Int = 0,
)

@JsonClass(generateAdapter = true)
@Keep
data class ReviewFilter(
    @NullToEmptyString var id: Int = 0,
    @NullToEmptyString var name: String = "",
    @NullToEmptyString var filter_code: String = "",
    @NullToEmptyString var is_selected: Boolean
)

@JsonClass(generateAdapter = true)
@Keep
data class CreateProductResponse(val data: CreateProductData)

@JsonClass(generateAdapter = true)
@Keep
data class CreateProductData(
    val id: Int = 0,
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val description: String = "",
    @NullToEmptyString val price: String = "",
    @NullToEmptyString val stock: String = "",
    val category: CategoryItem,
    val sub_category: CategorySubItem
)

@JsonClass(generateAdapter = true)
@Keep
data class UpadateProductData(
    val id: Int = 0,
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val description: String = "",
    val price: Int = 0,
    val stock: Int = 0,
    val enterpreneur_category_id: Int = 0,
    val enterpreneur_sub_category_id: Int = 0
)


@JsonClass(generateAdapter = true)
@Keep
data class MyProductResponse(val data: MyProductData = MyProductData())

@JsonClass(generateAdapter = true)
@Keep
data class MyProductData(
    val id: Int = 0,
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val description: String = "",
    @NullToEmptyString val status: String = "",
    val price: Int = 0,
    val stock: Int = 0,
    val published: Int = 0,
    val category: MyProductCategory = MyProductCategory(),
    val sub_category: MyProductCategory = MyProductCategory(),
    val image: List<MyProductImage> = emptyList()
)

@JsonClass(generateAdapter = true)
@Keep
data class MyProductCategory(
    val id: Int = 0,
    @NullToEmptyString val name: String = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class MyProductImage(
    val id: Int = 0,
    val sequence: Int = 0,
    @NullToEmptyString var image: String = ""
) {

    override fun equals(other: Any?): Boolean =
        other is MyProductImage && other.id == id && other.image == image

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + sequence
        result = 31 * result + image.hashCode()
        return result
    }
}

@JsonClass(generateAdapter = true)
@Keep
data class ListShipResponse(val data: List<ListShipData> = emptyList())

@JsonClass(generateAdapter = true)
@Keep
data class ListShipData(
    @NullToEmptyString val id: String = "",
    val courier_id: Int = 0,
    @NullToEmptyString val courier_name: String = "",
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val description: String = "",
    val cost: Int = 0,
    @NullToEmptyString val estimation_day: String = "",
    @NullToEmptyString val note: String = "",
    @NullToEmptyString val information: String = "",
    var isParent: Boolean = false,
    var selected: Boolean = false,
    var isLast: Boolean = false
)

@Keep
@JsonClass(generateAdapter = true)
data class MerchantKlaspayInfoResponse(
    val rc: String = "",
    val rd: String = "",
    val data: MerchantKlaspayInfoData = MerchantKlaspayInfoData()
)

@Keep
@JsonClass(generateAdapter = true)
data class MerchantKlaspayInfoData(
    val wallet_id: String = "",
    val merchant_id: Int = 0,
    val chat_id: String = ""
)