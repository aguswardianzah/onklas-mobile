 package id.onklas.app.pages.pembayaran.spp

import androidx.annotation.Keep
import androidx.room.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString

@Keep
@JsonClass(generateAdapter = true)
@Entity(tableName = "spp", indices = [Index("id", unique = true)])
data class SppTable(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @Json(name = "month") val name: String = "",
    val issued_at: String = "",
    val school_id: Int = 0,
    val product_school_id: Int = 0,
    val student_id: Int = 0,
    val discount_amount: Double = 0.0,
    val total_fee: Double = 0.0,
    @NullToEmptyString var paid_at: String = "",
    var date: Long = 0,
    val transaction_id: String = "",
    val is_inquiry_channel: Boolean = false
)

@Keep
@JsonClass(generateAdapter = true)
data class ResponseMeta(
    val current_page: Int = 0,
    @NullToEmptyString val per_page: String = "",
    val last_page: Int = 0,
    val from: Int = 0,
    val to: Int = 0,
    @NullToEmptyString val path: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class ResponseLinks(
    @NullToEmptyString val first: String = "",
    @NullToEmptyString val last: String = "",
    @NullToEmptyString val prev: String = "",
    @NullToEmptyString val next: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class SppResponse(
    val data: List<SppTable> = emptyList(),
    val links: ResponseLinks = ResponseLinks()
//    val meta: ResponseMeta = ResponseMeta()
)

@Keep
@JsonClass(generateAdapter = true)
data class SppProcessItem(
    val id: Int = 0,
    @NullToEmptyString @Json(name = "transaction_type") val jenis: String = "",
    @NullToEmptyString @Json(name = "reff_id") val reffId: String = "",
    @NullToEmptyString @Json(name = "payment_method") val paymentMethod: String = "",
    @NullToEmptyString val payment_code: String = "",
    @NullToEmptyString val payment_status_url: String = "",
    @NullToEmptyString @Json(name = "expired_at") val expiredAt: String = "",
    @NullToEmptyString @Json(name = "paid_at") val paidAt: String = "",
    @Json(name = "is_paid") val isPaid: Boolean = false,
    @Json(name = "is_expired") val isExpired: Boolean = false,
    val total: Int = 0,
    val school_fee_invoice: List<SppTable> = emptyList()
)

@Entity(
    tableName = "spp_payment",
    indices = [Index("id", unique = true)]
)
data class SppProcess(
    @PrimaryKey val id: Int = 0,
    val jenis: String = "",
    val reffId: String = "",
    val paymentMethod: String = "",
    val paymentCode: String = "",
    val paymentUrl: String = "",
    val expiredAt: String = "",
    val paidAt: String = "",
    val isPaid: Boolean = false,
    val isExpired: Boolean = false,
    val total: Int = 0
)

@Entity(
    tableName = "spp_payment_cross_ref",
    primaryKeys = ["invoice_id", "payment_id"],
    indices = [
        Index(name = "spp_process_inv_idx", value = ["invoice_id"], unique = false),
        Index(name = "spp_process_pay_idx", value = ["payment_id"], unique = true)
    ]
)
data class SppProcessCrossRef(val invoice_id: Int = 0, val payment_id: Int = 0)

data class SppInvoicePayment(
    @Embedded val crossRef: SppProcessCrossRef,
    @Relation(parentColumn = "payment_id", entityColumn = "id")
    val payment: SppProcess,
    @Relation(parentColumn = "invoice_id", entityColumn = "id")
    val invoice: List<SppTable> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class SppProcessListResponse(
    val data: List<SppProcessItem> = emptyList(),
    val links: ResponseLinks = ResponseLinks()
)