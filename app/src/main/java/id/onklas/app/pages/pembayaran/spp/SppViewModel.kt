package id.onklas.app.pages.pembayaran.spp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.OnKlasDbUtil
import id.onklas.app.pages.login.StudentItem
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import id.onklas.app.utils.StringUtil
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class SppViewModel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val db: MemoryDB,
    val dbUtil: OnKlasDbUtil,
    val diffUtil: SppTableDiffUtil,
    val stringUtil: StringUtil,
    val apiService: ApiService,
    val apiWrapper: ApiWrapper
) : ViewModel() {

    val student: StudentItem by lazy {
        try {
            moshi.adapter(StudentItem::class.java).fromJson(pref.getString("student"))
                ?: StudentItem()
        } catch (e: Exception) {
            StudentItem()
        }
    }

    val errorString by lazy { MutableLiveData<String>() }
    private val pageSize = 10

    val listTagihanEmpty by lazy { MutableLiveData(false) }
    val listTagihan by lazy {
        db.spp().getTagihanSpp()
            .toLiveData(pageSize, boundaryCallback = PagedListBoundaryCallback(
                {
                    viewModelScope.launch {
                        db.spp().deleteTagihanSpp()
//                        db.spp().deleteSpp()
                        fetchSpp(1)
                    }
                },
                { viewModelScope.launch { fetchSpp(pageTagihan) } }
            ))
    }
    val checkedItems by lazy { MutableLiveData<HashMap<Int, SppTable>>(HashMap()) }
    val checkedIds by lazy { MutableLiveData<MutableList<Int>>(mutableListOf()) }

    val listPaidEmpty by lazy { MutableLiveData(false) }
    val listPaid by lazy {
        db.spp().getPaidSpp()
            .toLiveData(pageSize, boundaryCallback = PagedListBoundaryCallback(
                {
                    viewModelScope.launch {
                        db.spp().deletePaidSpp()
//                        db.spp().deleteSpp()
                        fetchSpp(1, true)
                    }
                },
                { viewModelScope.launch { fetchSpp(pagePaid, true) } }
            ))
    }

    var pageTagihan = 0
    var pagePaid = 0
    private var hasNextTagihan = true
    private var hasNextPaid = true
    suspend fun fetchSpp(start: Int = 1, isPaid: Boolean = false) {
        if ((isPaid && pagePaid == start) || (!isPaid && pageTagihan == start))
            return

        if (isPaid) pagePaid = start else pageTagihan = start
        try {

            if (start == 1)
                if (isPaid) db.spp().deletePaidSpp()
                else db.spp().deleteTagihanSpp()

//            if (isPaid) {
            val response =
                apiService.sppInvoice()
//                if (isPaid) apiService.paidInvoice(
//                    start,
//                    pageSize
//                ) else apiService.unpaidInvoice(
//                    start,
//                    pageSize
//                )

            if (response.data.transaction_invoice.isNotEmpty()) {
                val dbProcess = dbUtil.processKlaspayTagihanSpp(response)
//                val dbProcess = dbUtil.processSppResponse(response)
//                if (isPaid) {
//                    hasNextPaid = dbProcess
//                    if (dbProcess) pagePaid++
//                } else {
//                    hasNextTagihan = dbProcess
//                    if (dbProcess) pageTagihan++
//                }

                if (isPaid) listPaidEmpty.postValue(db.spp().countPaidSpp() == 0)
                else listTagihanEmpty.postValue(db.spp().countTagihanSpp() == 0)

                hasNextTagihan = false
            } else if (start == 1) {
                errorString.postValue("Data tidak ditemukan")
                if (isPaid) listPaidEmpty.postValue(true)
                else listTagihanEmpty.postValue(true)
            }
//            else {
//                val response = apiService.klaspayTagihanSpp()
//
//                if (response.data.transaction_invoice.isNotEmpty()) {
//                    dbUtil.processKlaspayTagihanSpp(response)
//                } else
//                    errorString.postValue("Data tidak ditemukan")
//
//                hasNextTagihan = false
//            }
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.localizedMessage)
        }
    }

    val listPayment by lazy {
        db.spp().getPaymentSpp().toLiveData(pageSize, boundaryCallback = PagedListBoundaryCallback(
            { viewModelScope.launch { fetchPaymentSpp() } },
            { viewModelScope.launch { if (hasNextPayment) fetchPaymentSpp(pagePayment) } }
        ))
    }

    var pagePayment = 0
    private var hasNextPayment = true
    suspend fun fetchPaymentSpp(page: Int = 1) {
        if (pagePayment == page) return

        pagePayment = page
        try {
            val response = apiService.processInvoice(page, pageSize)

            if (response.data.isNotEmpty()) {
                hasNextPayment = dbUtil.processSppPaymentListRespose(response)
                if (hasNextPayment) pagePayment++
            } else {
                db.spp().deletePaymentSpp()
                db.spp().deleteInvoiceSpp()
                errorString.postValue("Data tidak ditemukan")
            }
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.localizedMessage)
        }
    }

    val balance by lazy { MutableLiveData<Int>() }
    val srcDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH) }
    val paidDateFormat by lazy { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id")) }
}