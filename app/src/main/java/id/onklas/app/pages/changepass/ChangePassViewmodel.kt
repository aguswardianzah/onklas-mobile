package id.onklas.app.pages.changepass

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.onklas.app.utils.ApiWrapper
import timber.log.Timber
import javax.inject.Inject

class ChangePassViewmodel @Inject constructor(
    private val apiWrapper: ApiWrapper
) : ViewModel() {

    val oldPass by lazy { MutableLiveData<String>() }
    val newPass by lazy { MutableLiveData<String>() }
    val confirmPass by lazy { MutableLiveData<String>() }
    val allowChange by lazy { MutableLiveData<Boolean>(false) }
    val errorString by lazy { MutableLiveData<String>() }

    suspend fun changePass(next: (success: Boolean) -> Unit) {
        try {
            apiWrapper.changePassword(oldPass.value.orEmpty(), newPass.value.orEmpty())
            next.invoke(true)
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.localizedMessage)
            next.invoke(false)
        }
    }
}