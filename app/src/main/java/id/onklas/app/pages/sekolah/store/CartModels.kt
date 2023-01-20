package id.onklas.app.pages.sekolah.store

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString
import java.util.*

@JsonClass(generateAdapter = true)
@Keep
data class CartResponse(val data: List<CartResponseData> = emptyList())

@JsonClass(generateAdapter = true)
@Keep
data class CartResponseData(
    val merchant: CartMerchant = CartMerchant(),
    val goodies: List<CartGoodies> = emptyList()
)

@JsonClass(generateAdapter = true)
@Keep
data class CartMerchant(
    val id: Int = 0,
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val avatar: String = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class CartGoodies(
    val goodie: CartItem = CartItem(),
    val quantity: Int = 0
)

@JsonClass(generateAdapter = true)
@Keep
data class CartItem(
    val id: Int = 0,
    @NullToEmptyString val name: String = "",
    val price: Int = 0,
    val stock: Int = 0,
    val image: CartItemImage? = CartItemImage()
)

@JsonClass(generateAdapter = true)
@Keep
data class CartItemImage(val image: String = "")


@Entity(tableName = "cart")
data class CartTable(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val merchant_id: Int = 0,
    val merchant_name: String = "",
    val product_id: Int = 0,
    val quantity: Int = 0,
    var selected: Boolean = false,
    var showItem: Boolean = true,
    val created: Date = Date()
)

data class CartPaging(
    @Embedded val cart: CartTable,
    @Relation(
        entity = ProductTable::class,
        parentColumn = "product_id",
        entityColumn = "product_id"
    )
    val product: ProductTable? = null
)