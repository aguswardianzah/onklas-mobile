package id.onklas.app.pages.ppob.listrik

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString

@Keep
@JsonClass(generateAdapter = true)
data class ListPlnResponse(
    @NullToEmptyString val rc: String = "",
    @NullToEmptyString val rd: String = "",
    val data: List<ListPlnData> = emptyList(),
)

@Keep
@JsonClass(generateAdapter = true)
data class ListPlnData(
    @NullToEmptyString val product_id: String = "",
    @NullToEmptyString val name: String = "",
    val price: Int? = 0,
    val admin: Int? = 0,
    val product: Map<String, List<PlnItem>> = emptyMap()
)

@Keep
@JsonClass(generateAdapter = true)
data class PlnItem(
    @NullToEmptyString val product_id: String = "",
    @NullToEmptyString val nominal: String = ""
)