package id.onklas.app.pages.resetpass

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.onklas.app.utils.ApiWrapper
import timber.log.Timber
import javax.inject.Inject

class ResetPassViewmodel @Inject constructor(
    val apiWrapper: ApiWrapper
) : ViewModel() {

    val email by lazy { MutableLiveData("") }
    val errorString by lazy { MutableLiveData("") }

    suspend fun resetPass(): Boolean = try {
        apiWrapper.resetPass(email.value)
        true
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        false
    }
}