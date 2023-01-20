package id.onklas.app.pages.ppob.internet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.ppob.PpobTransaction
import id.onklas.app.pages.ppob.air.PdamItem
import id.onklas.app.utils.StringUtil
import timber.log.Timber
import javax.inject.Inject

class InternetViewModel @Inject constructor(
    val stringUtil: StringUtil,
    val apiService: ApiService,
    val db: MemoryDB
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }

    val inputJenis by lazy { MutableLiveData<PdamItem>() }
    val inputNomor by lazy { MutableLiveData<String>() }

    val loadingListInternet by lazy { MutableLiveData(true) }
    val listInternet by lazy { MutableLiveData<List<PdamItem>>() }

    suspend fun loadJenis() = try {
        loadingListInternet.postValue(true)
        listInternet.postValue(apiService.listInternet().data)
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
    } finally {
        loadingListInternet.postValue(false)
    }

    suspend fun inqInternet(pin: String?) = try {
        val res = apiService.ppobInquiry(
            mapOf(
                "ppob_type" to "internet",
                "idpel1" to inputNomor.value,
                "product_id" to inputJenis.value?.product_id,
                "pin" to pin
            )
        )

        val internet = res.data.transaction_detail.internet
        db.ppob().insert(
            PpobTransaction.formInqResponseDetail(internet.ref1, res.data.transaction_detail)
        )
        internet.ref1
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
        ""
    }
}