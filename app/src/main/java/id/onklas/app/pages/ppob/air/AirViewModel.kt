package id.onklas.app.pages.ppob.air

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.ppob.PpobTransaction
import id.onklas.app.utils.StringUtil
import timber.log.Timber
import javax.inject.Inject

class AirViewModel @Inject constructor(
    val apiService: ApiService,
    val stringUtil: StringUtil,
    val db: MemoryDB
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }

    val inputNomor by lazy { MutableLiveData("") }
    val inputWilayah by lazy { MutableLiveData<PdamItem>() }

    val titleBar by lazy { MutableLiveData("") }

    val masterListPdam = ArrayList<PdamItem>()
    val listPdam by lazy { MutableLiveData<List<PdamItem>>() }
    val loadingPdam by lazy { MutableLiveData(true) }
    val searchPdam by lazy { MutableLiveData<String>() }

    suspend fun loadPdam() = try {
        loadingPdam.postValue(true)
        masterListPdam.clear()
        masterListPdam.addAll(apiService.listPdam().data.also { listPdam.postValue(it) })
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
    } finally {
        loadingPdam.postValue(false)
    }

    fun searchPdam(search: String) =
        listPdam.postValue(masterListPdam.filter { it.name.contains(search, true) })

//    private val periodeSrc by lazy { SimpleDateFormat("yyyyMM", Locale.ENGLISH) }
//    private val periodeDest by lazy { SimpleDateFormat("MMM yyyy", Locale("id")) }

    suspend fun inqPdam(pin: String?) = try {
        val res = apiService.ppobInquiry(
            mapOf(
                "ppob_type" to "pdam",
                "idpel1" to inputNomor.value,
                "product_id" to inputWilayah.value?.product_id,
                "pin" to pin
            )
        )

        val pdam = res.data.transaction_detail.pdam
        db.ppob().insert(
            PpobTransaction.formInqResponseDetail(pdam.ref1, res.data.transaction_detail)
        )

        pdam.ref1
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
        ""
    }
}