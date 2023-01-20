package id.onklas.app.models

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

object DefaultRemoteConfig {

    val minVersion by lazy {
        mapOf(
            "minVersion" to 1,
            "needUpdate" to true
        )
    }
}

@Keep
@JsonClass(generateAdapter = true)
data class MinVersionClass(val minVersion: Int = 1, val needUpdate: Boolean = true)