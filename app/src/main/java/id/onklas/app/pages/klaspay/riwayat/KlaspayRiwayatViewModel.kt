package id.onklas.app.pages.klaspay.riwayat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.onklas.app.api.ApiService
import id.onklas.app.pages.ppob.InqResponseDetail
import id.onklas.app.utils.NumberUtil
import id.onklas.app.utils.StringUtil
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class KlaspayRiwayatViewModel @Inject constructor(
    val apiService: ApiService,
    val numberUtil: NumberUtil,
    val stringUtil: StringUtil
) : ViewModel() {

    val titleBar by lazy { MutableLiveData("") }
    val errorString by lazy { MutableLiveData("") }
    val historyDetail by lazy { MutableLiveData<InqResponseDetail>() }
    val historyList by lazy { MutableLiveData<List<TransactionHistory>>(emptyList()) }
    val loadingList by lazy { MutableLiveData(false) }
    val listEmpty by lazy { MutableLiveData(false) }

    //    val srcFormat by lazy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH) }
    val srcFormat by lazy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH) }
    val dstFormat by lazy { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id")) }

    suspend fun transactionHistory() = try {
        loadingList.value = true
        apiService.klaspayHistory()
            .data
            .transaction_history
            .onEach {
//                it.transaction_id = it.transaction_id
                it.priceLabel = stringUtil.formatCurrency2(it.price)
                it.created_at = try { dstFormat.format(srcFormat.parse(it.created_at)!!) } catch (e: Exception) { "" }
            }
            .also {
                historyList.value = it
                listEmpty.value = it.isEmpty()
            }
    } catch (e: Exception) {
        Timber.e(e)
        errorString.value = e.localizedMessage
        loadingList.value = false
    }

    val loadingDetail by lazy { MutableLiveData(false) }
    suspend fun transactionHistoryDetail(id: String?) = try {
        loadingDetail.value = true
        apiService.klaspayHistoryDetail(id)
            .data
            .transaction_detail
            .also {
                it.paid_date = try { dstFormat.format(srcFormat.parse(it.paid_date)!!) } catch (e: Exception) { "" }
                historyDetail.postValue(it)
            }
    } catch (e: Exception) {
        Timber.e(e)
        errorString.value = e.localizedMessage
        loadingDetail.value = false
    }
}