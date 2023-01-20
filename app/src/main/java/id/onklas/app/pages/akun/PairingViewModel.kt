package id.onklas.app.pages.akun

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class PairingViewModel @Inject constructor(
        val moshi: Moshi,
        val pref: PreferenceClass,
        val intentUtil: IntentUtil,
        val apiWrapper: ApiWrapper,
        val apiService: ApiService,
        val db:MemoryDB
) : ViewModel() {
        val LoadingShow by lazy { MutableLiveData(true) }
        val errorString by lazy { MutableLiveData<String>() }

        private val pageSize = 20
        var lastProduct = -1
        var hasNextProduct = true

        fun listPairing() = db.pairing().getListPairing()
                .toLiveData(
                        pageSize, boundaryCallback = PagedListBoundaryCallback(
                                { viewModelScope.launch { fetchListPairing(0) } },
                                {
                                        viewModelScope.launch {
                                                val count = db.pairing().countlistPairing()
                                                Timber.e("count pairing: $count -- hasNext = $hasNextProduct")
                                                if (hasNextProduct)
                                                        fetchListPairing(count)
                                        }
                                }
                        ))

        suspend fun fetchListPairing(start: Int = 0) {
                if (start == lastProduct) {
                        LoadingShow.postValue(false)
                        return
                }
                lastProduct = start
                hasNextProduct = try {
                        apiService.pairingList()
                                .data
                                .map { PairingTable.fromPairingitem(it) }
                                .also {
                                        db.pairing().delete()
                                        db.pairing().insert(it) }
                        true
                } catch (e: Exception) {
                        Timber.e(e)
                        false
                } finally {
                        LoadingShow.postValue(false)
                }
        }

        val acceptPairingResponse by lazy { MutableLiveData<pairingItem>() }
        suspend fun acceptPairing(id:Int) = try {
                LoadingShow.postValue(true)
                acceptPairingResponse.postValue(apiService.acceptPairing(id).data)
        } catch (e: Exception) {
                LoadingShow.postValue(false)
                errorString.postValue(e.message)
        } finally {
                LoadingShow.postValue(false)
        }




}