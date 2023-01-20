package id.onklas.app.pages.klaspay.aktivasi

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiException
import id.onklas.app.api.ApiService
import id.onklas.app.pages.login.StudentItem
import id.onklas.app.utils.PreferenceClass
import timber.log.Timber
import javax.inject.Inject

class KlaspayAktivasiViewmodel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    private val apiService: ApiService
) : ViewModel() {

    val password: MutableLiveData<String> = MutableLiveData("")
    val pin: MutableLiveData<String> = MutableLiveData("")
    val pinConfirm: MutableLiveData<String> = MutableLiveData("")
    val pageStep by lazy { MutableLiveData<Int>(1) }
    val errorString by lazy { MutableLiveData("") }
    val errorPassword by lazy { MutableLiveData("") }

    init {
        Timber.e("pref_student: ${pref.getString("student")}")
        Timber.e("pref_user_token: ${pref.getString("user_token")}")
        Timber.e("pageStep: $pageStep")
    }

    val student: StudentItem by lazy {
        try {
            moshi.adapter(StudentItem::class.java).fromJson(pref.getString("student"))
                ?: StudentItem()
        } catch (e: Exception) {
            StudentItem()
        }
    }

    suspend fun klaspayCheck(
        next: (success: Boolean, errorTypes: Array<String>) -> Unit
    ) = try {
        apiService.klaspayCheck().also {
            next.invoke(true, emptyArray())
        }
    } catch (e: Exception) {
        val errorTypes = (e as? ApiException)?.errorTypes ?: emptyArray()
        if (errorTypes.contains("email"))
            apiService.sendEmailVerification()

        next.invoke(false, errorTypes)
//        errorString.postValue(e.localizedMessage)
        errorPassword.postValue(e.localizedMessage)
        Timber.e(e)
    }

    //    private val klaspayActivate by lazy { moshi.adapter(KlaspayActivateResponse::class.java) }
    suspend fun klaspayActivate(password: String?, pin: String?) =
        try {
            apiService.klaspayActivate(mapOf("password" to password, "pin" to pin))
            pref.putBoolean("klaspayActive", true)
            true
//                pref.putString("user_klaspay", klaspayActivate.toJson(it))
        } catch (e: Exception) {
            errorString.postValue(e.localizedMessage)
            Timber.e(e)
            false
        }
}