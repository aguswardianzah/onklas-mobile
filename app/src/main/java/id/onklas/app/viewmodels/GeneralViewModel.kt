package id.onklas.app.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.PreferenceClass
import timber.log.Timber
import javax.inject.Inject

class GeneralViewModel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val intentUtil: IntentUtil,
    val apiService: ApiService
) : ViewModel() {

    suspend fun checkVersion(): CheckVersion? = try {
        val resp = apiService.checkVersion().data
        moshi.adapter(CheckVersion::class.java).fromJson(resp.setting_globals_value)
    } catch (e: Exception) {
        Timber.e(e)
        null
    }
}

@JsonClass(generateAdapter = true)
@Keep
data class CheckVersionResponse(
    val data: CheckVersionRespData = CheckVersionRespData()
)

@JsonClass(generateAdapter = true)
@Keep
data class CheckVersionRespData(
    val setting_globals_lable: String = "Android Version Apps",
    val setting_globals_name: String = "android-version",
    val setting_globals_value: String = ""
)


@JsonClass(generateAdapter = true)
@Keep
data class CheckVersionData(
    val setting_globals_lable: String = "Android Version Apps",
    val setting_globals_name: String = "android-version",
    val setting_globals_value: CheckVersion = CheckVersion()
)

@JsonClass(generateAdapter = true)
@Keep
data class CheckVersion(val version: String = "1.0.0")
