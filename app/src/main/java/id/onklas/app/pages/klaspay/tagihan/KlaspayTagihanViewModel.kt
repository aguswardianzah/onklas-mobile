package id.onklas.app.pages.klaspay.tagihan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.pembayaran.PaymentInvoice
import id.onklas.app.utils.NumberUtil
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.StringUtil
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class KlaspayTagihanViewModel @Inject constructor(
    val apiService: ApiService,
    val numberUtil: NumberUtil,
    val stringUtil: StringUtil,
    val db: MemoryDB
) : ViewModel() {

    val errorString by lazy { MutableLiveData("") }
    val isLoading by lazy { MutableLiveData(false) }
    val invoiceList by lazy { MutableLiveData<List<KlaspayTransactionInvoiceItem>>() }
    val listEmpty by lazy { MutableLiveData(false) }
    val invoiceItem by lazy { MutableLiveData<KlaspayTransactionInvoiceItem>() }

    val srcFormat by lazy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH) }
    val dstFormat by lazy { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id")) }

    private val pageSize = 20

    val isEmptyUnpaid by lazy { MutableLiveData(false) }
    var hasMoreUnpaid = true
    val loadingList by lazy { MutableLiveData(false) }
    val unpaidList by lazy {
        db.payment().listInvoice()
            .toLiveData(
                pageSize,
                boundaryCallback = PagedListBoundaryCallback(
                    {
                        viewModelScope.launch { fetchInvoice() }
                    }
                )
            )
    }

    val isEmptyPaid by lazy { MutableLiveData(false) }
    var hasMorePaid = true
    val loadingPaid by lazy { MutableLiveData(false) }
    val paidList by lazy {
        db.payment().paidInvoice()
            .toLiveData(
                pageSize,
                boundaryCallback = PagedListBoundaryCallback(
                    {
                        viewModelScope.launch { fetchInvoice(true) }
                    }
                )
            )
    }

    suspend fun fetchInvoice(paid: Boolean = false) {
//        Timber.e("paid: $paid -- loading: ${loadingList.value} -- hasMoreUnpaid: $hasMoreUnpaid")
        if (!paid && (loadingList.value == true || !hasMoreUnpaid)) return
        if (paid && (loadingPaid.value == true || !hasMorePaid)) return

        if (!paid) loadingList.postValue(true)
        else loadingPaid.postValue(true)

        try {
            db.payment().insert(
                apiService.klaspayInvoice().data.transaction_invoice.map {
                    val createdDate: Date? = try {
                        srcFormat.parse(it.created_date)
                    } catch (e: Exception) {
                        null
                    }
                    val createdDateLabel: String = createdDate?.let { dstFormat.format(it) } ?: ""

                    val expiredDate: Date? = try {
                        srcFormat.parse(it.details.expired_date)
                    } catch (e: Exception) {
                        null
                    }
                    val expiredDateLabel: String = expiredDate?.let { dstFormat.format(it) } ?: ""

                    PaymentInvoice(
                        it.transaction_id,
                        it.type,
                        createdDate,
                        createdDateLabel,
                        expiredDate,
                        expiredDateLabel,
                        it.transaction_status,
                        it.details.guidance_code,
                        it.details.payment_method,
                        db.payment().channelIcon(it.details.guidance_code).orEmpty(),
                        it.details.payment_code,
                        if (it.details.guidance_code.equals("alfamart", true))
                            "Tagihan Pembelian"
                        else "Virtual Account",
                        it.price,
                        it.details.fee_admin,
                        it.details.fee_service,
                        it.details.fee_other,
                        it.details.total_amount,
                        it.cancelable,
                        it.note,
                        it.is_paid
                    )
                }
            )
        } catch (e: Exception) {
            Timber.e(e)
            errorString.value = e.message
        } finally {
            if (!paid) {
                val count = db.payment().countInvoice()
                hasMoreUnpaid = count > 0
                isEmptyUnpaid.postValue(count == 0)
                loadingList.postValue(false)
            } else {
                val count = db.payment().countPaid()
                hasMorePaid = count > 0
                isEmptyPaid.postValue(count == 0)
                loadingPaid.postValue(false)
            }
        }
    }

    suspend fun cancelInvoice(trxId: String, pin: String?) = try {
        apiService.klaspayCancelInvoice(mapOf("transaction_id" to trxId, "pin" to pin))
        db.payment().delete(trxId)
        true
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
        false
    }

    suspend fun klaspayInvoice() = try {
        isLoading.value = true
        apiService.klaspayInvoice()
            .data
            .transaction_invoice
            .onEach {
                it.transaction_id = "ID: ${it.transaction_id}"
                it.priceLabel = stringUtil.formatCurrency2(it.price ?: 0)
                it.created_date = try { dstFormat.format(srcFormat.parse(it.created_date)) } catch (e: Exception) { "" }
            }
            .also {
                invoiceList.value = it
                listEmpty.value = it.isEmpty()
            }
    } catch (e: Exception) {
        Timber.e(e)
        errorString.value = e.localizedMessage
        isLoading.value = false
    }
}