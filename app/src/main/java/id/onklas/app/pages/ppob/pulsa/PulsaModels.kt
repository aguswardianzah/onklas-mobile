package id.onklas.app.pages.ppob.pulsa

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString

@JsonClass(generateAdapter = true)
@Keep
data class ListProviderResponse(
    @NullToEmptyString val rc: String = "",
    @NullToEmptyString val rd: String = "",
    val data: ProviderData = ProviderData()
)

@JsonClass(generateAdapter = true)
@Keep
data class ProviderData(
    @NullToEmptyString val provider: String = "",
    val product: Map<String, List<PulsaItem>> = emptyMap()
)

@JsonClass(generateAdapter = true)
@Keep
data class ListGameResponse(
    @NullToEmptyString val rc: String = "",
    @NullToEmptyString val rd: String = "",
    val data: Map<String, List<PulsaItem>> = emptyMap()
)

@JsonClass(generateAdapter = true)
@Keep
data class PulsaItem(
    @NullToEmptyString val product_id: String = "",
    @NullToEmptyString val name: String = "",
    val price_end: Int = 0,
    val admin: Int = 0,
    val cashback: Int = 0,
    @NullToEmptyString val provider: String = "",
    @NullToEmptyString val provider_text: String = "",
    @NullToEmptyString val type: String = ""
)