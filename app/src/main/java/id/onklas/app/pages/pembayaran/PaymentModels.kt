package id.onklas.app.pages.pembayaran

import androidx.annotation.Keep
import androidx.room.*
import com.squareup.moshi.JsonClass
import java.util.*

@Keep
@JsonClass(generateAdapter = true)
data class PaymentType(val id: String = "", val name: String = "", val payment_code: String = "")

@Keep
@JsonClass(generateAdapter = true)
data class PaymentTypeResponse(val data: List<PaymentType> = emptyList())

@Keep
@JsonClass(generateAdapter = true)
data class CheckoutResponse(
    val rc: String = "",
    val rd: String = "",
    val request_time: String = "",
    val data: CheckoutResponseData = CheckoutResponseData()
)

@Keep
@JsonClass(generateAdapter = true)
data class CheckoutResponseData(
    val payment_code: String = "",
    val payment_method: String = "",
    val payment_method_code: String = "",
    val expired: String = "",
    val total_amount: String = "",
    val transaction_type: String = "",
    val spi_status_url: String = ""
)

@Entity(
    tableName = "payment_invoice",
    indices = [Index("trx_id", unique = true)]
)
data class PaymentInvoice(
    @PrimaryKey val trx_id: String = "",
    val type: String = "",
    val created_at: Date? = null,
    val created_at_label: String = "",
    val expired_at: Date? = null,
    val expired_at_label: String = "",
    var status: String = "",
    val channel: String = "",
    val channel_label: String = "",
    val channel_icon: String = "",
    val payment_code: String = "",
    val payment_code_type: String = "",
    val amount: Int = 0,
    val fee_admin: Int = 0,
    val fee_service: Int = 0,
    val fee_other: Int = 0,
    val total: Int = 0,
    val cancelable: Boolean = false,
    val note: String = "",
    var is_paid: Boolean = false,
    val paid_at: Date? = null,
    val paid_at_label: String = ""
)

data class PaymentInvoiceFromDetail(
    val trx_id: String = "",
    val type: String = "",
    val created_at: Date? = null,
    val created_at_label: String = "",
    var status: String = "",
    val channel: String = "",
    val channel_label: String = "",
    val payment_code: String = "",
    val payment_code_type: String = "",
    val amount: Int = 0,
    val fee_admin: Int = 0,
    val fee_service: Int = 0,
    val fee_other: Int = 0,
    val total: Int = 0,
    val cancelable: Boolean = false,
    val note: String = "",
    var is_paid: Boolean = true,
    val paid_at: Date? = null,
    val paid_at_label: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class GuidanceResponse(
    val rc: String = "",
    val rd: String = "",
    val data: GuidanceData = GuidanceData()
)

@Keep
@JsonClass(generateAdapter = true)
data class GuidanceData(
    val channel: String = "",
    val guide: List<Map<String, List<String>>> = emptyList()
)

@Entity(tableName = "pay_guide_channel", indices = [Index("channel")])
data class PaymentGuideType(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val channel: String = "",
    val payment_type: String = "",
    val updated_at: Long = 0
)

@Entity(tableName = "pay_guide_item", indices = [Index("typeId")])
data class PaymentGuideItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val typeId: Int = 0,
    val order: Int = 0,
    val guide: String = ""
)

data class PaymentGuideChannelItems(
    @Embedded val typePayment: PaymentGuideType,
    @Relation(parentColumn = "id", entityColumn = "typeId")
    val itemPayments: List<PaymentGuideItem> = emptyList()
)

data class GuideItem(
    val id: Int = 0,
    val type: String = "",
    val order: String = "",
    val guide: String = "",
    val parent: Int = 0,
    val childs: List<GuideItem> = emptyList(),
    var showChild: Boolean = false
)