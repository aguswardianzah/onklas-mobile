package id.onklas.app.pages.ppob.air

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString

@Keep
@JsonClass(generateAdapter = true)
data class PdamResponse(
    @NullToEmptyString val rc: String = "",
    @NullToEmptyString val rd: String = "",
    val data: List<PdamItem> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class PdamItem(
    @NullToEmptyString val product_id: String = "",
    @NullToEmptyString val name: String = "",
    val price: Int? = 0,
    val admin: Int? = 0
)