package id.onklas.app.pages.akun

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DeviceViewModel @Inject constructor(
    val pref: PreferenceClass,
    val memoryDB: MemoryDB,
    val apiService: ApiService
) : ViewModel() {

    val pageSize = 10

    val errorString by lazy { MutableLiveData<String>() }
    val loading by lazy { MutableLiveData(false) }
    val listDevice by lazy {
        memoryDB.login().getSessions().toLiveData(
            pageSize,
            boundaryCallback = PagedListBoundaryCallback(
                {
                    viewModelScope.launch { fetchSession() }
                },
                {
                    viewModelScope.launch {
                        val count = memoryDB.login().countSession()
                        if (hasNext && count >= pageSize)
                            fetchSession(count)
                    }
                })
        )
    }

    private var lastStart = -1
    private var hasNext = true
    suspend fun fetchSession(start: Int = 0) {
        if (!hasNext || lastStart == start)
            return

        loading.postValue(true)
        lastStart = start
        hasNext = try {
            val resp = apiService.listSessions(pageSize, pageSize, start)
            memoryDB.login().insertSession(resp.data)
            true
        } catch (e: Exception) {
            errorString.postValue(e.localizedMessage)
            Timber.e(e)
            false
        } finally {
            loading.postValue(false)
        }
    }

    suspend fun refresh() {
        memoryDB.login().nukeSession()
        lastStart = -1
        hasNext = true
        fetchSession()
    }

    suspend fun deleteSession(id: Int) = try {
        apiService.logoutDevice(id)
        memoryDB.login().deleteSession(id)
        true
    } catch (e: Exception) {
        errorString.postValue(e.localizedMessage)
        Timber.e(e)
        false
    }

    suspend fun deleteAllSession() = try {
        apiService.logoutOthers()
        refresh()
        true
    } catch (e: Exception) {
        errorString.postValue(e.localizedMessage)
        Timber.e(e)
        false
    }
}