package id.onklas.app.pages.entrepreneurs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.utils.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class OrderVM @Inject constructor(
        val pref: PreferenceClass,
        val apiService: ApiService,
        val numberUtil: NumberUtil,
        val stringUtil: StringUtil,
        val dateUtil: DateUtil,
        val db: MemoryDB,
) : ViewModel() {
    private val pageSize = 50
    var lastProduct = -1
    var hasNextProduct = true
    val LoadingShow by lazy { MutableLiveData(true) }
    val isEmptyOrder by lazy { MutableLiveData<Boolean>() }
    val errorString by lazy { MutableLiveData<String>() }

    val dateFilterLiveData by lazy { MutableLiveData<String>() }

    val incActResponse by lazy { MutableLiveData<AcceptRejectResponse>() }
    val incActResponseBoolean by lazy { MutableLiveData<Boolean>() }
    val buyerName by lazy { MutableLiveData<String>("") } // untuk accept tolak

    val moveToProcessed by lazy { MutableLiveData<Boolean>() }

    val inpResiResponse by lazy { MutableLiveData<InputResiResponse>() }

    val test by lazy { MutableLiveData<String>("") }


    init {
        viewModelScope.launch {
            try {
                val countInc = db.kwu().countIncomingIn()
                countIncoming.postValue(countInc)

                val countPro = db.kwu().countIncomingProcessed()
                countProcess.postValue(countPro)
            } catch (E: Exception) {
            }
        }

    }

    val countIncoming by lazy { MutableLiveData<Int>(0) }
    val countProcess by lazy { MutableLiveData<Int>(0) }

    fun listOrder(page: String, role: String,dateFilter:String) = db.kwu().getListSellerOrder(page, role,dateFilter)
            .toLiveData(
                    pageSize, boundaryCallback = PagedListBoundaryCallback(
                    { viewModelScope.launch {
                        fetchListSellerOrder(0, page, role,dateFilter) }
                    },
                    {
                        viewModelScope.launch {
                            Timber.e("date filter view model $dateFilter")
                            val count = db.kwu().countListSellerOrder(page, role,dateFilter)
                            countIncoming.postValue(db.kwu().countIncomingIn())
                            countProcess.postValue(db.kwu().countIncomingProcessed())
                            Timber.e("count product: $count -- hasNext = $hasNextProduct")
                            if (hasNextProduct && count>=pageSize)
                                fetchListSellerOrder(count, page, role,dateFilter)
                        }
                    }
            ))

    fun listIncome(page: String, role: String, status: String,dateFilter: String) = db.kwu().getIncomeSeller(page, role, status,dateFilter)
            .toLiveData(
                    pageSize, boundaryCallback = PagedListBoundaryCallback(
                    { viewModelScope.launch { fetchListSellerOrder(0, page, role,dateFilter) } },
                    {
                        viewModelScope.launch {
                            val count = db.kwu().countIncomeSeller(page, role, status,dateFilter)
                            Timber.e("count product: $count -- hasNext = $hasNextProduct")
                            if (hasNextProduct)
                                fetchListSellerOrder(count, page, role,dateFilter)
                        }
                    }
            ))


     suspend fun fetchListSellerOrder(
            start: Int = 0,
            page: String,
            role: String,
            dateFilter: String
    ) {
        if (start == lastProduct) {
            return
        }
         LoadingShow.postValue(true)
         lastProduct = start
         hasNextProduct = try {
            if (page == "incoming") {
                apiService.IncomingOrder(pageSize, start,dateFilter)
                        .run {
                            if(start == 0){
                                db.kwu().delete(page,role)
                                isEmptyOrder.postValue(data.isEmpty())
                            }else{
                                isEmptyOrder.postValue(false)
                            }
                            data.forEach {
                                val main = it
                                db.kwu().insertOrder(
                                        TransaksiTable(
                                                main.id,
                                                main.buyer_name,
                                                main.status,
                                                page,
                                                role,
                                                main.date,
                                                dateUtil.getDateTime(dateUtil.formatDate(main.date)),
                                                dateUtil.getDateTime3(dateUtil.formatDate(main.date)),
                                                main.goodies_count,
                                                main.sub_total,
                                                main.courier_name,
                                                main.courier_fee,
                                                main.total,
                                            main.destination_address,
                                            main.merchant?.name?:"",
                                            main.merchant?.avatar?:"",

                                        )
                                )
                                it.goodies.forEach {
                                    val sub = it
                                    db.kwu().insertOrderProduct(
                                            TransaksiProductTable(
                                                    "${main.id} ${sub.goody_name}".hashCode(),
                                                    sub.goody_id,
                                                    main.id,
                                                    sub.goody_image,
                                                    sub.goody_name,
                                                    sub.goody_price,
                                                    sub.goody_quantity,
                                            )
                                    )
                                }
                            }
                            data.size >= pageSize
                        }

            }
            else if (page == "processed") {
                apiService.ProcessedTransaksi(pageSize, start,dateFilter)
                        .run {
                            if(start == 0){
                                db.kwu().delete(page,role)
                                isEmptyOrder.postValue(data.isEmpty())
                            }else{
                                isEmptyOrder.postValue(false)
                            }
                            data.forEach {
                                val main = it
                                db.kwu().insertOrder(
                                        TransaksiTable(
                                                main.id,
                                                main.buyer_name,
                                                main.status,
                                                page,
                                                role,
                                                main.date,
                                                dateUtil.getDateTime(dateUtil.formatDate(main.date)),
                                                dateUtil.getDateTime3(dateUtil.formatDate(main.date)),
                                                main.goodies_count,
                                                main.sub_total,
                                                main.courier_name,
                                                main.courier_fee,
                                                main.total,
                                            main.destination_address,
                                            main.merchant?.name?:"",
                                            main.merchant?.avatar?:"",
                                        )
                                )
                                it.goodies.forEach {
                                    val sub = it
                                    db.kwu().insertOrderProduct(
                                            TransaksiProductTable(
                                                    "${main.id} ${sub.goody_name}".hashCode(),
                                                    sub.goody_id,
                                                    main.id,
                                                    sub.goody_image,
                                                    sub.goody_name,
                                                    sub.goody_price,
                                                    sub.goody_quantity,
                                            )
                                    )
                                }
                            }
                            data.size >= pageSize
                        }
            }
            else if (page == "completed") { // completed
                apiService.CompletedTransaksi(pageSize, start,dateFilter)
                        .run {
                            if(start == 0){
                                db.kwu().delete(page,role)
                                isEmptyOrder.postValue(data.isEmpty())
                            }else{
                                isEmptyOrder.postValue(false)
                            }
                            data.forEach {
                                val main = it
                                db.kwu().insertOrder(
                                        TransaksiTable(
                                                main.id,
                                                main.buyer_name,
                                                main.status,
                                                page,
                                                role,
                                                main.date,
                                                dateUtil.getDateTime(dateUtil.formatDate(main.date)),
                                                dateUtil.getDateTime3(dateUtil.formatDate(main.date)),
                                                main.goodies_count,
                                                main.sub_total,
                                                main.courier_name,
                                                main.courier_fee,
                                                main.total,
                                            main.destination_address,
                                            main.merchant?.name?:"",
                                            main.merchant?.avatar?:"",
                                        )
                                )
                                it.goodies.forEach {
                                    val sub = it
                                    db.kwu().insertOrderProduct(
                                            TransaksiProductTable(
                                                    "${main.id} ${sub.goody_name}".hashCode(),
                                                    sub.goody_id,
                                                    main.id,
                                                    sub.goody_image,
                                                    sub.goody_name,
                                                    sub.goody_price,
                                                    sub.goody_quantity,
                                            )
                                    )
                                }
                            }
                            data.size >= pageSize
                        }
            }
            else { // review
                apiService.ReviewTransaksiSeller(pageSize, start,dateFilter)
                        .run {
                            if(start == 0){
                                db.kwu().delete(page,role)
                                isEmptyOrder.postValue(data.isEmpty())
                            }else{
                                isEmptyOrder.postValue(false)
                            }
                            data.forEach {
                                val main = it
                                db.kwu().insertOrder(
                                        TransaksiTable(
                                                main.id,
                                                main.buyer_name,
                                                main.status,
                                                page,
                                                role,
                                                main.date,
                                                dateUtil.getDateTime(dateUtil.formatDate(main.date)),
                                                dateUtil.getDateTime3(dateUtil.formatDate(main.date)),
                                                main.goodies_count,
                                                main.sub_total,
                                                main.courier_name,
                                                main.courier_fee,
                                                main.total,
                                                main.destination_address,
                                            main.merchant?.name?:"",
                                            main.merchant?.avatar?:"",
                                        )
                                )
                                it.goodies.forEach {
                                    val sub = it
                                    db.kwu().insertOrderProduct(
                                            TransaksiProductTable(
                                                    "${main.id} ${sub.goody_name}".hashCode(),
                                                    sub.goody_id,
                                                    main.id,
                                                    sub.goody_image,
                                                    sub.goody_name,
                                                    sub.goody_price,
                                                    sub.goody_quantity,
                                            )
                                    )
                                }
                            }
                            data.size >= pageSize
                        }
            }

        } catch (e: Exception) {
            LoadingShow.postValue(false)
            Timber.e(e)
            false
        } finally {
            LoadingShow.postValue(false)
        }
    }


    // accept transaksi
    suspend fun acceptTransaksi(transaksiId: Int) = try {
        LoadingShow.postValue(true)
        incActResponse.postValue(apiService.acceptTransaction(transaksiId))
        incActResponseBoolean.postValue(true)
    } catch (e: Exception) {
        incActResponseBoolean.postValue(false)
        LoadingShow.postValue(false)
        Timber.e(e)
        errorString.postValue(e.message)
    }

    // cancle transaksi
    suspend fun rejectTransaksi(transaksiId: Int) = try {
        LoadingShow.postValue(true)
        incActResponse.postValue(apiService.rejectTransaction(transaksiId))
        incActResponseBoolean.postValue(true)
    } catch (e: Exception) {
        incActResponseBoolean.postValue(false)
        LoadingShow.postValue(false)
        Timber.e(e)
        errorString.postValue(e.message)
    }







    // input resi
    suspend fun inputResi(transaksiId: Int,waybill:String, courier:String) = try {
        LoadingShow.postValue(true)
        inpResiResponse.postValue(apiService.inputResi(transaksiId,waybill,courier))
        incActResponseBoolean.postValue(true)
    } catch (e: Exception) {
        incActResponseBoolean.postValue(false)
        LoadingShow.postValue(false)
        Timber.e(e)
        errorString.postValue(e.message)
    }

    // post review
    val BuyerAnyResponse by lazy { MutableLiveData<Any>() }
    suspend fun PostReview(goodyReviewId: Int,rating:Int,comment:String) = try {
        LoadingShow.postValue(true)
        BuyerAnyResponse.postValue(apiService.postReviewSeller(goodyReviewId,rating,comment))
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        Timber.e(e)
        errorString.postValue(e.message)
    }





}