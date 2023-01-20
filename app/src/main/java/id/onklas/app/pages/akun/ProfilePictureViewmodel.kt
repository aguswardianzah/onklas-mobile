package id.onklas.app.pages.akun

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.squareup.moshi.Moshi
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.PreferenceClass
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class ProfilePictureViewmodel @Inject constructor(
    val moshi: Moshi,
    val pref: PreferenceClass,
    val intentUtil: IntentUtil,
    val apiWrapper: ApiWrapper
) : ViewModel() {

    val userAdapter by lazy { moshi.adapter(UserTable::class.java) }
    val userTable: UserTable by lazy {
        try {
            userAdapter.fromJson(pref.getString("user")) ?: UserTable(pref.getInt("user_id"))
        } catch (e: Exception) {
            UserTable(pref.getInt("user_id"))
        }
    }

    val path by lazy { MutableLiveData<String>(userTable.user_avatar_image) }

    suspend fun uploadAvatar(): Boolean = try {
        apiWrapper.uploadProfilePicture(File(Uri.parse(path.value)?.path.orEmpty()))
        true
    } catch (e: Exception) {
        Timber.e(e)
        false
    }
}