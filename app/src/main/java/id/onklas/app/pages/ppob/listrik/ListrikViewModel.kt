package id.onklas.app.pages.ppob.listrik

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.ppob.PpobTransaction
import id.onklas.app.utils.StringUtil
import timber.log.Timber
import javax.inject.Inject

class ListrikViewModel @Inject constructor(
    val stringUtil: StringUtil,
    val apiService: ApiService,
    val db: MemoryDB
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }

    val inputMeter by lazy { MutableLiveData<String>() }
    val isTagihan by lazy { MutableLiveData(false) }

    val listPlnItem by lazy { MutableLiveData<List<PlnItem>>() }
    val productPasc by lazy { MutableLiveData<String>() }
    val productPra by lazy { MutableLiveData<String>() }
    val selectedNominal by lazy { MutableLiveData<String>() }

//    private val periodeSrc by lazy { SimpleDateFormat("yyyyMM", Locale.ENGLISH) }
//    private val periodeDest by lazy { SimpleDateFormat("MMM yyyy", Locale("id")) }

    suspend fun inqPln(pin: String?) = try {
        val isTagihan = isTagihan.value == true
        val res = apiService.ppobInquiry(
            mutableMapOf(
                "ppob_type" to "pln",
                "idpel1" to inputMeter.value,
                "product_id" to if (isTagihan) productPasc.value else productPra.value,
                "pin" to pin
            ).apply {
                if (!isTagihan)
                    set("nominal", selectedNominal.value)
            }
        )

        val pln = res.data.transaction_detail.pln
        db.ppob().insert(
            PpobTransaction.formInqResponseDetail(pln.ref1, res.data.transaction_detail)
        )

        pln.ref1
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
        ""
    }

    suspend fun loadListPln() = try {
        apiService.listTokenListrik().data.forEach {
            if (it.name.contains("prabayar", true)) {
                productPra.postValue(it.product_id)
                listPlnItem.postValue(it.product.values.firstOrNull())
            } else {
                productPasc.postValue(it.product_id)
            }
        }
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
    }
}