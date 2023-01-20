package id.onklas.app.pages.partisipasi

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@Entity(tableName = "partisipasi")
data class PartisipasiItem(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val final_amount: Int = 0,
    val current_amount: Int = 0,
    val is_active: Boolean = false,
    val is_expired: Boolean = false,
    val finished: Boolean = false,
    var deadline: Date = Date()
) {
    constructor(item: PartisipasiRespItem) : this(
        item.bill_id,
        item.bill_name,
        item.target_nominal,
        item.paid_nominal,
        item.is_active,
        item.is_expired,
        true
    )
}

@Entity(tableName = "partisipasi_payment")
data class PartisipasiPayment(
    @PrimaryKey val id: String = "",
    val partisipasi_id: String = "",
    val amount: Int = 0,
    val channel_code: String = "",
    val channel_name: String = "",
    var date: Date = Date()
) {
    constructor(partisipasiId: String, item: PartisipasiRespPaymentItem) : this(
        item.transaction_id,
        partisipasiId,
        item.nominal,
        item.chanel,
        item.chanel
    )
}

data class PartisipasiWithPayment(
    val item: PartisipasiItem,
    val payment: List<PartisipasiPayment>
)

@Keep
@JsonClass(generateAdapter = true)
data class ListPartisipasiResponse(
    val data: ListPartisipasiData = ListPartisipasiData()
)

@Keep
@JsonClass(generateAdapter = true)
data class ListPartisipasiDetailResponse(
    val data: PartisipasiRespItem = PartisipasiRespItem()
)

@Keep
@JsonClass(generateAdapter = true)
data class ListPartisipasiData(
    val list_bill: List<PartisipasiRespItem> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class PartisipasiRespItem(
    val bill_id: String = "",
    val bill_name: String = "",
    val target_nominal: Int = 0,
    val paid_nominal: Int = 0,
    val is_active: Boolean = false,
    val is_expired: Boolean = false,
    val expired_date: String = "",
    val list_child: List<PartisipasiRespPaymentItem> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class PartisipasiRespPaymentItem(
    val transaction_id: String = "",
    val chanel: String = "",
    val nominal: Int = 0,
    val created_date: String = ""
)