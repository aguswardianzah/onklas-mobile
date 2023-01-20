package id.onklas.app.pages.ppob.bayar

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString
import java.io.Serializable

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayBayarResponse(
    val rc: String? = "",
    val rd: String? = "",
    val data: KlaspayBayarData = KlaspayBayarData(),
) : Serializable

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayBayarData(
    @NullToEmptyString val transaction_id: String = "",
    @NullToEmptyString val payment_code: String = "",
    @NullToEmptyString val payment_method: String = "",
    val fee_admin: Int = 0,
    val fee_service: Int = 0,
    val fee_other: Int = 0,
    val total_amount: Int = 0,
    @NullToEmptyString val spi_status_url: String = "",
    @NullToEmptyString val payment_method_code: String = "",
    @NullToEmptyString val guidance_code: String = "",
    @NullToEmptyString var expired_date: String = "",
//    val guidance: Map<String, List<String>> = emptyMap(),
    val guidance: List<KlaspayBayarGuidance> = emptyList(),
    ) : Serializable

//@JsonClass(generateAdapter = true)
//@Keep
//data class KlaspayBayarDataMap(
//        val payment_code: String? = "",
//        val spi_status_url: String? = "",
//        val total_amount: Int? = 0,
//        val fee_admin: Int? = 0,
//        val payment_method: String? = "",
//        val payment_method_code: String? = "",
//        var expired_date: String? = "",
//        val guidance: Map<String, List<String>> = emptyMap(),
////        val guidance: List<KlaspayBayarGuidance> = emptyList(),
//) : Serializable

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayBayarGuidance(
    @Json(name = "ATM") val virtualAccount: List<String> = emptyList(),
    @Json(name = "MBANGKING") val emoney: List<String> = emptyList(),
    @Json(name = "IBANKING") val creditCard: List<String> = emptyList(),
    @Json(name = "EDC") val clickPay: List<String> = emptyList(),
    @Json(name = "Kantor") val modernStore: List<String> = emptyList(),
    @Json(name = "ATM Lain") val bankTransfer: List<String> = emptyList(),
    @Json(name = "Direct") val direct: List<String> = emptyList(),
) : Serializable

data class KlaspayGuidanceData(
    val guidance: Map<String, List<TypeGuidanceItem>> = emptyMap()
)

data class TypeGuidanceItem(
    val paymentMethod: String = "",
    val parentName: String = "",
    val items: List<String> = emptyList(),
    var showChild: Boolean = false
)