package id.onklas.app.pages.ppob.pulsa

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.ppob.PpobTransaction
import id.onklas.app.pages.ppob.PulsaPrabayar
import id.onklas.app.utils.StringUtil
import timber.log.Timber
import javax.inject.Inject

class PulsaViewModel @Inject constructor(
    val apiService: ApiService,
    val stringUtil: StringUtil,
    val db: MemoryDB
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }

    val inputPhone by lazy { MutableLiveData("") }
    val providerName by lazy { MutableLiveData<String>() }
    val pulsaSubType by lazy { MutableLiveData(PulsaPrabayar) }
    val listProduct by lazy { MutableLiveData<List<Pair<String, List<PulsaItem>>>>(emptyList()) }
    val productPasca by lazy { MutableLiveData<PulsaItem>() }

    fun reset() {
        listProduct.postValue(emptyList())
        inputPhone.postValue("")
        providerName.postValue("")
    }

    suspend fun loadProvider() {
        if (inputPhone.value?.isNotEmpty() == true) {
            try {
                val res = apiService.providerPulsa(inputPhone.value!!)
                providerName.postValue(res.data.provider)
                listProduct.postValue(res.data.product.keys.zip(res.data.product.values))

                val pasca = apiService.providerPulsaPasca(inputPhone.value.orEmpty())
                productPasca.postValue(pasca.data.product.values.firstOrNull()?.firstOrNull())
            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
            }
        }
    }

    suspend fun inqPulsa(pin: String) = try {
        val res = apiService.ppobInquiry(
            mapOf(
                "ppob_type" to "pulsa",
                "idpel1" to inputPhone.value,
                "product_id" to productPasca.value?.product_id,
                "pin" to pin
            )
        )

        val pulsa = res.data.transaction_detail.pulsa
        db.ppob().insert(
            PpobTransaction.formInqResponseDetail(pulsa.ref1, res.data.transaction_detail)
        )

        pulsa.ref1
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
        ""
    }
}