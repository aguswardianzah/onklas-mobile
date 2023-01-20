package id.onklas.app.pages.akun

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.PreferenceClass
import timber.log.Timber
import javax.inject.Inject

class SettingAkunViewmodel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val intentUtil: IntentUtil,
    private val apiWrapper: ApiWrapper,
    private val apiService: ApiService
) : ViewModel() {

    private val userAdapter by lazy { moshi.adapter(UserTable::class.java) }
    val userTable: UserTable by lazy {
        try {
            userAdapter.fromJson(pref.getString("user")) ?: UserTable(pref.getInt("user_id"))
        } catch (e: Exception) {
            UserTable(pref.getInt("user_id"))
        }
    }

    val errorString by lazy { MutableLiveData<String>() }

    val allowChange by lazy { MutableLiveData<Boolean>(false) }
    val username by lazy { MutableLiveData(userTable.username) }
    val name by lazy { MutableLiveData(userTable.name) }
    val email by lazy { MutableLiveData(userTable.email) }
    val phone by lazy { MutableLiveData(userTable.phone) }
    val isVerified by lazy { MutableLiveData(pref.getBoolean("is_verified")) }

    val isEmailVerified by lazy { MutableLiveData(pref.getBoolean("is_email_verified")) }
    val isVerifying by lazy { MutableLiveData(pref.getBoolean("is_email_verifying", false)) }

    val editEmail by lazy { MutableLiveData(false) }
    val editPhone by lazy { MutableLiveData(false) }

    val hasChange by lazy { MutableLiveData(false) }

    fun nameChanged() = name.value != userTable.name
    fun usernameChanged() = username.value != userTable.username
    fun emailChanged() = email.value != userTable.email
    fun phoneChanged() = phone.value != userTable.phone
    fun hasChange() = nameChanged() || usernameChanged() || emailChanged() || phoneChanged()

    suspend fun updateProfile(next: (success: Boolean) -> Unit) {
        try {
            apiWrapper.updateAccount(
                phone.value.orEmpty(),
                email.value.orEmpty(),
                username.value.orEmpty()
            )
            userTable.username = username.value.orEmpty()
            userTable.name = name.value.orEmpty()
            userTable.phone = phone.value.orEmpty()
            userTable.email = email.value.orEmpty()
            pref.putString("user", userAdapter.toJson(userTable))

            next.invoke(true)
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.localizedMessage)
            next.invoke(false)
        }
    }

    suspend fun updateProfile() = try {
        apiWrapper.updateAccount(
            phone.value.orEmpty(),
            email.value.orEmpty(),
            username.value.orEmpty()
        )
        userTable.username = username.value.orEmpty()
        userTable.name = name.value.orEmpty()
        userTable.phone = phone.value.orEmpty()
        userTable.email = email.value.orEmpty()
        pref.putString("user", userAdapter.toJson(userTable))
        true
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        false
    }

    suspend fun sendEmail() = try {
        apiService.sendEmailVerification()

        isVerifying.value = true
        pref.putBoolean("is_email_verifying", true)

        true
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        false
    }
}