package id.onklas.app.pages.klaspay.tagihan

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString
import id.onklas.app.pages.ppob.bayar.KlaspayBayarData
import java.io.Serializable

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayInvoiceResponse(
    val rc: String? = "",
    val rd: String? = "",
    val data: KlaspayInvoiceData = KlaspayInvoiceData()
)

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayInvoiceData(
    val transaction_invoice: List<KlaspayTransactionInvoiceItem> = emptyList()
)

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayTransactionInvoiceItem(
    @NullToEmptyString var transaction_id: String = "",
    @NullToEmptyString var note: String = "",
    val price: Int = 0,
    @NullToEmptyString val type: String = "",
    @NullToEmptyString val transaction_status: String = "",
    @NullToEmptyString var created_date: String = "",
    @NullToEmptyString var priceLabel: String = "",
    val cancelable: Boolean = false,
    val is_paid: Boolean = false,
//    val details: KlaspayTransactionInvoiceDetail? = KlaspayTransactionInvoiceDetail()
    val details: KlaspayBayarData = KlaspayBayarData()
) : Serializable

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayTransactionInvoiceDetail(
    val payment_code: String? = "",
    val payment_method: String? = "",
    val fee_admin: Int = 0,
    val fee_service: Int = 0,
    val fee_other: Int = 0,
    val total_amount: Long? = 0,
    val spi_status_url: String? = ""
) : Serializable

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayTagihanSppResult(
    val rc: String = "",
    val rd: String = "",
    val data: KlaspayTagihanSppData = KlaspayTagihanSppData()
)

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayTagihanSppData(val transaction_invoice: List<KlaspayTagihanSpp> = emptyList())

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayTagihanSpp(
    @NullToEmptyString val transaction_id: String = "",
    val price: Int = 0,
    @NullToEmptyString val type: String = "",
    @NullToEmptyString val transaction_status: String = "",
    @NullToEmptyString val created_date: String = "",
    @NullToEmptyString val invoice: String = "",
    @NullToEmptyString val invoice_type: String = "",
    @NullToEmptyString val note: String = "",
    val is_paid: Boolean = false,
    @NullToEmptyString val paid_date: String = "",
    val is_inquiry_channel: Boolean = false
)