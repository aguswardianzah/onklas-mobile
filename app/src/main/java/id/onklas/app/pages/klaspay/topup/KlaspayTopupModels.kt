package id.onklas.app.pages.klaspay.topup

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString
import java.io.Serializable

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayToolbarResponse(val data: KlaspayToolbarData = KlaspayToolbarData())

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayToolbarData(
    val products: Map<String, List<VirtualAccountItem>> = emptyMap()
)

data class TypeTopupItem(
    val nameId: String = "",
    val img: Any? = 0,
    val name: String = "",
    val info: String = "",
    val items: List<TypeTopupItem> = emptyList(),
    var payment_code: String = "",
    val parentName: String = "",
    var showChild: Boolean = false,
    var balance: String = "",
    var needTopup: Boolean = false
)

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayToolbarProduct(
    @Json(name = "virtual account") val virtualAccount: List<VirtualAccountItem> = emptyList(),
    @Json(name = "e-money") val emoney: List<VirtualAccountItem> = emptyList(),
    @Json(name = "credit card") val creditCard: List<VirtualAccountItem> = emptyList(),
    @Json(name = "clickpay") val clickPay: List<VirtualAccountItem> = emptyList(),
    @Json(name = "modern store") val modernStore: List<VirtualAccountItem> = emptyList(),
    @Json(name = "bank transfer") val bankTransfer: List<VirtualAccountItem> = emptyList(),
    @Json(name = "qris") val qris: List<VirtualAccountItem> = emptyList()
)

@JsonClass(generateAdapter = true)
@Keep
data class VirtualAccountItem(
    @NullToEmptyString val payment_code: String = "",
    @NullToEmptyString val payment_name: String = "",
    @NullToEmptyString val payment_description: String = "",
    @NullToEmptyString val payment_logo: String = "",
    @NullToEmptyString val payment_url: String = "",
    @NullToEmptyString val payment_url_v2: String = "",
    val is_direct: Boolean? = false,
)

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayTopupInqResponse(val data: KlaspayTopupInqData = KlaspayTopupInqData())

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayTopupInqData(
    val transaction_id_inquiry: String? = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayTopupTrxResponse(val data: KlaspayTopupTrxData = KlaspayTopupTrxData()) :
    Serializable

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayTopupTrxData(
    val payment_code: String? = "",
    val spi_status_url: String? = "",
    val total_amount: Long? = 0,
    val fee_admin: Int = 0,
    val fee_service: Int = 0,
    val fee_other: Int = 0,
    val payment_method: String? = "",
    val payment_method_code: String? = "",
) : Serializable