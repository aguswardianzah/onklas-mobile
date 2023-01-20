package id.onklas.app.pages.ppob.bpjs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.ppob.PpobTransaction
import id.onklas.app.utils.StringUtil
import timber.log.Timber
import javax.inject.Inject

class BpjsViewModel @Inject constructor(
    val stringUtil: StringUtil,
    val apiService: ApiService,
    val db: MemoryDB
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }

    val inputPeriode by lazy { MutableLiveData<Pair<Int, String>>() }
    val inputNomor by lazy { MutableLiveData<String>() }

    suspend fun inqBpjs(pin: String?) = try {
        val res = apiService.ppobInquiry(
            mapOf(
                "ppob_type" to "bpjs",
                "idpel1" to inputNomor.value,
                "product_id" to "ASRBPJSKSH",
                "periode" to inputPeriode.value?.first,
                "pin" to pin
            )
        )

        val bpjs = res.data.transaction_detail.bpjs
        db.ppob().insert(
            PpobTransaction.formInqResponseDetail(bpjs.ref1, res.data.transaction_detail)
        )
        bpjs.ref1
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
        ""
    }
}