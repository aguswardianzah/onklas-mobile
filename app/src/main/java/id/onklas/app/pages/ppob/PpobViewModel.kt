package id.onklas.app.pages.ppob

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.StringUtil
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PpobViewModel @Inject constructor(
    val apiService: ApiService,
    val stringUtil: StringUtil,
    val db: MemoryDB,
    val intentUtil: IntentUtil
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }
    val klaspayBalance by lazy { MutableLiveData<Int>() }

    val srcFormat by lazy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH) }

    suspend fun processPay(item: PpobTransaction, pin: String?) = try {
        when (item.productType) {
            PpobPulsa -> when (item.productSubType) {
                in arrayOf(PulsaPrabayar, PulsaPaket, PulsaSms) -> buy("pulsa", item, pin)
                PulsaPascabayar -> payInvoice(item, pin)
                else -> ""
            }
            PpobGame -> buy("game", item, pin)
            in arrayOf(PpobListrik, PpobAir, PpobInternet) -> payInvoice(item, pin)
            else -> ""
        }
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
        ""
    }

    suspend fun buy(type: String, item: PpobTransaction, pin: String?) = try {
        val detail = apiService.buyPulsa(
            mapOf(
                "ppob_type" to type,
                "idpel1" to item.custId,
                "product_id" to item.productId,
                "pin" to pin
            )
        ).data.transaction_detail

        db.ppob().insert(item.apply {
            trxId = if (detail.pulsa.ref1.isNotEmpty()) detail.pulsa.ref1 else detail.game.ref1
            status = detail.transaction_status
            detail.amount?.let { amount = it }
            detail.fee_admin?.let { feeAdmin = it }
            detail.fee_service?.let { feeService = it }
            detail.fee_other?.let { feeOther = it }
            detail.total_amount?.let { totalAmount = it }
            detail.cashback?.let { cashback = it }
            info = detail.note
            try {
                srcFormat.parse(detail.paid_date)?.let { paidAt = it.time }
            } catch (e: Exception) {
            }
        })

        item.trxId
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
        ""
    }

    suspend fun payInvoice(item: PpobTransaction, pin: String?) = try {
        val detail = apiService.ppobPay(
            mutableMapOf(
                "transaction_id" to item.trxId,
                "pin" to pin
            ).apply {
                if (item.productSubType == ListrikToken)
                    this.put("nominal", item.trxRef1)
            }
        ).data.transaction_detail

        db.ppob().insert(item.apply {
            status = detail.transaction_status
            detail.amount?.let { amount = it }
            detail.fee_admin?.let { feeAdmin = it }
            detail.fee_service?.let { feeService = it }
            detail.fee_other?.let { feeOther = it }
            detail.total_amount?.let { totalAmount = it }
            detail.cashback?.let { cashback = it }
            info = detail.note
            try {
                srcFormat.parse(detail.paid_date)?.let { paidAt = it.time }
            } catch (e: Exception) {
            }

            if (productSubType == ListrikToken)
                custRef1 = detail.pln.ref3
            else if (productType == PpobAir)
                custRef2 = "${detail.pdam.standawal} - ${detail.pdam.standakhir}"
        })

        item.trxId
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
        ""
    }

    suspend fun fetchDetail(trxId: String) = try {
        val data = apiService.klaspayHistoryDetail(trxId).data.transaction_detail
        PpobTransaction.formInqResponseDetail(trxId, data)
    } catch (e: Exception) {
        null
    }
}