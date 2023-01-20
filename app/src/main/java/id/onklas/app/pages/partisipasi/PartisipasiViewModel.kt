package id.onklas.app.pages.partisipasi

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import androidx.room.withTransaction
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.login.StudentItem
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import id.onklas.app.utils.StringUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PartisipasiViewModel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val stringUtil: StringUtil,
    val api: ApiService,
    val db: MemoryDB
) : ViewModel() {

    val balance by lazy { MutableLiveData<Int>() }
    var isPageOnGoing = true
    var klaspayId = ""

    val student: StudentItem by lazy {
        try {
            moshi.adapter(StudentItem::class.java).fromJson(pref.getString("student"))
                ?: StudentItem()
        } catch (e: Exception) {
            StudentItem()
        }
    }

    val dateFormat by lazy { SimpleDateFormat("dd MMMM yyyy", Locale("id")) }

    val emptyItem by lazy { MutableLiveData(false) }
    val loadingItem by lazy { MutableLiveData(true) }
    val calendar: Calendar by lazy { Calendar.getInstance() }

    private val pagingSize = 20

    private var refreshJob: Job? = null
    fun refresh() {
        refreshJob?.cancel()

        refreshJob = viewModelScope.launch {
            emptyItem.postValue(false)
            loadingItem.postValue(true)

//                initItems()
//                db.partisipasi().insert(api.listDanaPartisipasi().data.list_bill)

            pagingSource.value = if (isPageOnGoing) db.partisipasi()
                .pagingOngoing() else db.partisipasi()
                .pagingFinished()
        }
    }

    val errorString by lazy { MutableLiveData<String>() }

    private val respDateFormat by lazy {
        SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.ENGLISH
        )
    }

    fun fetchData() {
        viewModelScope.launch {
            try {
                loadingItem.postValue(true)
                db.partisipasi().insert(api.listDanaPartisipasi().data.list_bill.map {
                    PartisipasiItem(it).apply {
                        deadline = try {
                            respDateFormat.parse(it.expired_date) ?: Date()
                        } catch (e: Exception) {
                            Date()
                        }
                    }
                })
            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
            } finally {
                loadingItem.postValue(false)
            }
        }
    }

    private val pagingSource by lazy {
        MutableStateFlow(db.partisipasi().pagingFinished())
    }

    val listItem by lazy {
        pagingSource.map {
            it.toLiveData(
                pageSize = pagingSize,
                boundaryCallback = PagedListBoundaryCallback<PartisipasiItem>(
                    {
//                        viewModelScope.launch {
//                            emptyItem.postValue(
//                                if (isPageOnGoing)
//                                    db.partisipasi().countOngoing(calendar.timeInMillis) == 0
//                                else
//                                    db.partisipasi().countFinished(calendar.timeInMillis) == 0
//                            )
//                        }
                    }
                )
            )
        }
    }

//    private val channels by lazy {
//        listOf(
//            "MANDIRIVA" to "Mandiri",
//            "BRIVA" to "BRI",
//            "BNIVA" to "BNI",
//            "KLASPAY" to "Klaspay"
//        )
//    }

//    private suspend fun initItems() {
//        db.withTransaction {
//            db.partisipasi().nuke()
//            (0..Random.nextInt(1, 10)).forEach { i ->
//                val amount = Random.nextInt(1, 10) * 10000
//                val partisipasiId = db.partisipasi().insert(
//                    PartisipasiItem(
//                        0,
//                        "Nama Kegiatan $i",
//                        amount,
//                        Random.nextInt(0, amount),
//                        true,
//                        !isPageOnGoing,
//                        Calendar.getInstance().apply {
//                            add(
//                                Calendar.DAY_OF_YEAR,
//                                i * (if (!isPageOnGoing) 1 else -1)
//                            )
//                        }.time
//                    )
//                ).toInt()
//
//                val numPayment = Random.nextInt(1, 10)
//                repeat((0..numPayment).count()) {
//                    val channel = channels[Random.nextInt(0, channels.size - 1)]
//                    db.partisipasi().insert(
//                        PartisipasiPayment(
//                            0,
//                            partisipasiId,
//                            amount / numPayment,
//                            channel.first,
//                            channel.second,
//                            Calendar.getInstance().apply {
//                                add(Calendar.DAY_OF_YEAR, i * -1)
//                            }.time
//                        )
//                    )
//                }
//            }
//        }
//    }

    suspend fun payment(id: String) = db.partisipasi().payment(id)

    val loadingDetail by lazy { MutableLiveData(true) }
    fun detail(id: String): Flow<PartisipasiWithPayment> {
        fetchDetail(id)
        return db.partisipasi().getSingle(id).flatMapConcat {
            flow {
                emit(
                    PartisipasiWithPayment(it, db.partisipasi().payments(id))
                )
            }
        }
    }

    fun fetchDetail(id: String) {
        viewModelScope.launch {
            try {
                loadingDetail.postValue(true)
                val resp = api.listPaymentDanaPartisipasi(id)
                db.withTransaction {
                    db.partisipasi().insert(PartisipasiItem(resp.data).apply {
                        deadline = try {
                            respDateFormat.parse(resp.data.expired_date) ?: Date()
                        } catch (e: Exception) {
                            Date()
                        }
                    })
                    db.partisipasi().insertPayments(resp.data.list_child.map {
                        PartisipasiPayment(resp.data.bill_id, it).apply {
                            date = try {
                                respDateFormat.parse(it.created_date) ?: Date()
                            } catch (e: Exception) {
                                Date()
                            }
                        }
                    })
                }
            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
            } finally {
                loadingDetail.postValue(false)
            }
        }
    }

    val loadingPayBill by lazy { MutableLiveData(false) }
    val successPayBill by lazy { MutableLiveData<Boolean>() }
    fun payBill(id: String, channel: String, nominal: Int, pin: String) {
        viewModelScope.launch {
            try {
                loadingPayBill.postValue(true)
                val res = api.payBill(
                    mapOf(
                        "bill_id" to id,
                        "channel" to channel,
                        "nominal" to nominal,
                        "pin" to pin
                    )
                )

                successPayBill.postValue(true)
            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
                successPayBill.postValue(false)
            } finally {
                loadingPayBill.postValue(false)
            }
        }
    }
}