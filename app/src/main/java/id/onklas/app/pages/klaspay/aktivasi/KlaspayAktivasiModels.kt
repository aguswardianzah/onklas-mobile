package id.onklas.app.pages.klaspay.aktivasi

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayCheckResponse(val data: KlaspayCheckData = KlaspayCheckData())

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayCheckData(
    val email: Boolean = false,
    val password: Boolean = false
)

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayActivateResponse(
    val rc: String? = "",
    val rd: String? = "",
    val data: Boolean? = false
)

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayWalletResponse(
    @NullToEmptyString val rc: String = "",
    @NullToEmptyString val rd: String = "",
    val data: KlaspayWalletData = KlaspayWalletData()
)

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayWalletData(
    @NullToEmptyString val wallet_id: String = "",
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val email: String = "",
    val user_type: Int? = 0,
    val school_id: Int? = 0,
    val balance: Int? = 0,
    @NullToEmptyString val last_update: String = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class ResetPinResponse(
    @NullToEmptyString val rc: String = "",
    @NullToEmptyString val rd: String = "",
    @NullToEmptyString val reset_token: String = "",
    val email_verified: Boolean = false,
    val mail_verified: Boolean = false
)