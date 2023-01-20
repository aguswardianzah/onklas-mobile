package id.onklas.app.pages.sekolah.store

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.onklas.app.di.modules.NullToEmptyString

@Entity(tableName = "store_category")
data class CategoryTable(
    @PrimaryKey val id: Int = 0,
    val name: String = "",
    val icon: String = "",
    val is_selected: Boolean,
) {
    constructor(item: CategoryItem) : this(item.id, item.name, item.icon, item.is_selected)
}

@Entity(tableName = "store_categorySub")
data class CategorySubTable(
    @PrimaryKey val id: Int = 0,
    val parent_id: Int = 0,
    val sub_id: Int = 0,
    val name: String = "",
    val icon: String = "",
) {
    constructor(parentId: Int, item: CategorySubItem) : this(
        item.id,
        parentId,
        item.id,
        item.name,
        ""
    )
}


//
//@Entity(tableName = "store_cart")
//data class CartTable(
//        @PrimaryKey(autoGenerate = true) val id: Int = 0,
//        val merchat_id: Int = 0,
//)
//
//@Entity(tableName = "store_merchant")
//data class MerchantTable(
//        @PrimaryKey(autoGenerate = true) val id: Int = 0,
//        val name : String,
//        val avatar : String,
//)
//
//@Entity(tableName = "store_goodies")
//data class GoodiesTable(
//        @PrimaryKey val id: Int = 0,
//        val merchat_id: Int = 0,
//        val goodie_id:Int = 0,
//        val qty : Int,
//)
//
//@Entity(tableName = "store_goodie")
//data class GoodieTable(
//        @PrimaryKey val id: Int = 0,
//        val merchat_id: Int = 0,
//        val name: String = "",
//        val desc: String = "",
//        val price: Int = 0,
//        val stock : Int = 0,
//        val weight : Int = 0,
//        val qty : Int = 0,
//        val img : String = ""
//)

object ProductPublishStatus {
    const val ACCEPTED = "ACCEPTED"
    const val REJECTED = "REJECTED"
    const val CHECKED = "CHECKED"
    const val DISABLED = "DISABLED"
}

@Entity(tableName = "store_product")
data class ProductTable(
    @PrimaryKey val id: String = "",// produt id + name
    val product_id: Int = 0,
    val name: String = "",
    val description: String = "",
    val productFilter: String = "",
    val productPosition: String = "", // card_homepage , filter_homepage
    val price: Int = 0,
    var rating: String = "",
    val product_sold: Int = 0,
    var stock: Int = 0,
    var image: String = "",
    var category_id: Int = 0,
    var sub_category_id: Int = 0,
    val merchant_id: Int = 0,
    var publish_status: String = ProductPublishStatus.CHECKED,
    var published: Boolean = false
) {
    constructor(item: CartItem) : this(
        "${item.id}-${item.name}",
        item.id,
        item.name,
        price = item.price,
        stock = item.stock,
        image = item.image?.image.orEmpty()
    )

    constructor(item: MerchantProductItem, merchantId: Int) : this(
        "${item.id}",
        item.id,
        item.name,
        item.description,
        price = item.price,
        stock = item.stock,
        image = item.image,
        rating = item.rating,
        product_sold = item.sold,
        merchant_id = merchantId,
        publish_status = item.status,
        published = item.published == 1,
        productPosition = "mystore"
    )
}

@Entity(tableName = "store_merchant_product")
data class ProductMerchantTable(
    @PrimaryKey val id: String = "", // merchantId + productId
    val merchant_id: Int = 0,
    val product_id: Int = 0,
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val stock: Int = 0,
    val image: String = "",
    val sold: Int = 0,
    var rating: String = "",
)
