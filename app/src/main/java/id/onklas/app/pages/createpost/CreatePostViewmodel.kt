package id.onklas.app.pages.createpost

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.login.SekolahItem
import id.onklas.app.pages.sekolah.sosmed.FeedFileTable
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.FileUtils
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.PreferenceClass
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class CreatePostViewmodel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val fileUtils: FileUtils,
    val intentUtil: IntentUtil,
    val db: MemoryDB,
    val api: ApiWrapper
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }

    val user by lazy {
        MutableLiveData<UserTable>(
            try {
                moshi.adapter(UserTable::class.java).fromJson(pref.getString("user"))
                    ?: UserTable(pref.getInt("user_id"))
            } catch (e: Exception) {
                UserTable(pref.getInt("user_id"))
            }
        )
    }

    val school by lazy {
        try {
            moshi.adapter(SekolahItem::class.java).fromJson(pref.getString("school"))
        } catch (e: Exception) {
            null
        }
    }
    val listMedia by lazy { MutableLiveData<ArrayList<FeedFileTable>>() }
    val allowPost by lazy { MutableLiveData<Boolean>(false) }
    val urlApi by lazy { pref.getString("url_api") }

    val feedContent by lazy { MutableLiveData<String>("") }
    val feedAuthor by lazy { MutableLiveData<String>("") }
    val feedThumb by lazy { MutableLiveData<String>() }
    val feedFile by lazy { MutableLiveData<String>() }
    val allowUpload by lazy { MutableLiveData<Boolean>(false) }

    suspend fun createPost(): Boolean =
        try {
            val file =
                listMedia.value?.firstOrNull()?.let { File(Uri.parse(it.path)?.path.orEmpty()) }
            api.createPost(feedContent.value.orEmpty(), file)
            true
        } catch (e: Exception) {
            errorString.postValue(e.localizedMessage)
            Timber.e(e)
            false
        }

    suspend fun createEbook(): Boolean =
        try {
            api.createEbook(
                feedContent.value.orEmpty(),
                feedAuthor.value.orEmpty(),
                File(Uri.parse(feedThumb.value)?.path.orEmpty()),
                File(Uri.parse(feedFile.value)?.path.orEmpty())
            )
            true
        } catch (e: Exception) {
            errorString.postValue(e.localizedMessage)
            Timber.e(e)
            false
        }

    val listUser by lazy { MutableLiveData<List<UserTable>>() }

    var searchJob: Job? = null
    suspend fun searchUsername(search: String) {
        searchJob?.cancel()
        try {
            searchJob = viewModelScope.launch {
                listUser.postValue(db.feed().searchUsername("%$search%"))
                delay(1000)
                api.searchUsername(search).data
                listUser.postValue(db.feed().searchUsername("%$search%"))
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}