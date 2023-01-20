package id.onklas.app.pages.pembayaran

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.klaspay.aktivasi.KlaspayWalletData
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.PreferenceClass
import id.onklas.app.utils.StringUtil
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PaymentViewModel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val apiService: ApiService,
    val apiWrapper: ApiWrapper,
    val stringUtil: StringUtil,
    val db: MemoryDB,
    val intentUtil: IntentUtil
) : ViewModel() {

    val selectedPaymentType by lazy { MutableLiveData<PaymentType>() }
    val errorString by lazy { MutableLiveData<String>() }
    val klaspayData by lazy { MutableLiveData<KlaspayWalletData>() }
    val isKlaspayActive by lazy { MutableLiveData(pref.getBoolean("klaspayActive")) }

    val dateFormat by lazy { SimpleDateFormat("dd MMM yyyy", Locale("id")) }
    val timeFormat by lazy { SimpleDateFormat("HH:mm:ss", Locale("id")) }

    suspend fun fetchPaymentType(): List<PaymentType> = try {
        apiService.paymentServices()
            .data
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        emptyList()
    }

    suspend fun getWallet() {
        try {
            val res = apiService.klaspayWallet()
            if (res.rc == "00") {
                pref.putBoolean("klaspayActive", true)
                isKlaspayActive.postValue(true)
                klaspayData.postValue(res.data)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    suspend fun cancelInvoice(trxId: String, pin: String?) = try {
        apiService.klaspayCancelInvoice(mapOf("transaction_id" to trxId, "pin" to pin))
        db.payment().delete(trxId)
        true
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        false
    }

    val listGuidance by lazy { MutableLiveData<List<GuideItem>>() }

    suspend fun getGuidance(channel: String) = try {
        // check last update guidance, update if last_update more than 1 day
        db.payment().lastUpdate(channel)?.let {
            if (System.currentTimeMillis() - it > 1000 * 60 * 60 * 24)
                fetchPayGuidance(channel)
        } ?: fetchPayGuidance(channel)

        val listDb = db.payment().paymentGuide(channel)
        listGuidance.postValue(
            listDb.mapIndexed { index, item ->
                GuideItem(
                    item.typePayment.id,
                    item.typePayment.payment_type,
                    childs = item.itemPayments.mapIndexed { i, it ->
                        GuideItem(
                            listDb.size * (index + 1) + (i + 1),
                            order = it.order.toString(),
                            guide = it.guide,
                            parent = item.typePayment.id
                        )
                    }
                )
            }
        )
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
    }

    suspend fun fetchPayGuidance(channel: String) = try {
        apiService.payGuide(channel).data.guide.forEach {
            if (it.keys.isNotEmpty()) {
                it.entries.forEach {
                    val typeId = db.payment().insertGuideType(
                        PaymentGuideType(0, channel, it.key, System.currentTimeMillis())
                    ).toInt()

                    db.payment().insertGuideItem(it.value.mapIndexed { index, item ->
                        PaymentGuideItem(0, typeId, index + 1, item)
                    })
                }
            }
        }
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
    }

    suspend fun getDetailTransaction(
        trxId: String,
        onSuccess: (PaymentInvoice) -> Unit,
        onFail: suspend () -> Unit
    ) {
        db.payment().getSingle(trxId)?.let {
            Timber.e("payment detail: $it")
            if (it.total > 0 && it.amount > 0)
                onSuccess.invoke(it)
            else onFail.invoke()
        } ?: onFail.invoke()
    }

    suspend fun fetchDetailTransaction(
        trxId: String,
        onSuccess: (PaymentInvoice) -> Unit,
        onFail: suspend () -> Unit
    ) {
        try {
            val data = apiService.klaspayHistoryDetail(trxId).data.transaction_detail
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id"))
            val srcFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            val createdAt = try {
                srcFormat.parse(data.created_at)
            } catch (e: Exception) {
                null
            }
            val createdAtLabel = createdAt?.let { dateFormat.format(it) }.orEmpty()

            val paidAt = try {
                srcFormat.parse(data.paid_date)
            } catch (e: Exception) {
                null
            }
            val paidAtLabel = paidAt?.let { dateFormat.format(it) }.orEmpty()

            if (db.payment().count(trxId) > 0)
                db.payment().update(
                    PaymentInvoiceFromDetail(
                        data.transaction_id,
                        data.type,
                        createdAt,
                        createdAtLabel,
                        data.transaction_status,
                        data.payment_method,
                        data.payment_method_code,
                        data.payment_code,
                        if (data.payment_code.equals("alfamart", true))
                            "Tagihan Pembelian"
                        else "Virtual Account",
                        data.amount ?: 0,
                        data.fee_admin ?: 0,
                        data.fee_service ?: 0,
                        data.fee_other ?: 0,
                        data.total_amount ?: 0,
                        false,
                        data.note,
                        true, paidAt, paidAtLabel
                    )
                )
            else
                db.payment().insert(
                    PaymentInvoice(
                        data.transaction_id,
                        data.type,
                        createdAt, createdAtLabel,
                        null, "",
                        data.transaction_status,
                        data.payment_method,
                        data.payment_method_code,
                        data.payment_code,
                        if (data.payment_code.equals("alfamart", true))
                            "Tagihan Pembelian"
                        else "Virtual Account",
                        "",
                        data.amount ?: 0,
                        data.fee_admin ?: 0,
                        data.fee_service ?: 0,
                        data.fee_other ?: 0,
                        data.total_amount ?: 0,
                        false,
                        data.note,
                        true, paidAt, paidAtLabel
                    )
                )

            getDetailTransaction(trxId, onSuccess, onFail)
        } catch (e: Exception) {
            Timber.e(e)
            onFail.invoke()
        }
    }
}