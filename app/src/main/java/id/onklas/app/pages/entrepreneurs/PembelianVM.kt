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

class PembelianVM @Inject constructor(
    val pref: PreferenceClass,
    val apiService: ApiService,
    val numberUtil: NumberUtil,
    val stringUtil: StringUtil,
    val dateUtil: DateUtil,
    val db: MemoryDB,
) : ViewModel() {


    private val pageSize = 20
    var lastProduct = -1
    var hasNextProduct = true
    val LoadingShow by lazy { MutableLiveData(true) }
    val isEmptyOrder by lazy { MutableLiveData<Boolean>() }
    val errorString by lazy { MutableLiveData<String>() }


    val dateFilterLiveData by lazy { MutableLiveData<String>("") }

    val incActResponse by lazy { MutableLiveData<TransaksiResponse>() }


    init {
        viewModelScope.launch {
            try {
                val countPro = db.kwu().countOutcomingProcessed()
                countProcessed.postValue(countPro)

                val countDon = db.kwu().countOutcomingDone()
                countDone.postValue(countDon)
            } catch (E: Exception) {
            }
        }

    }

    val countProcessed by lazy { MutableLiveData<Int>(0) }
    val countDone by lazy { MutableLiveData<Int>(0) }

    fun listOrder(page: String, role: String, dateFilter: String) =
        db.kwu().getListSellerOrder(page, role, dateFilter)
            .toLiveData(
                pageSize, boundaryCallback = PagedListBoundaryCallback(
                    { viewModelScope.launch { fetchListSellerOrder(0, page, dateFilter, role) } },
                    {
                        viewModelScope.launch {
                            val count = db.kwu().countListSellerOrder(page, role, dateFilter)
                            countProcessed.postValue(db.kwu().countOutcomingProcessed())
                            countDone.postValue(db.kwu().countOutcomingDone())
                            Timber.e("count product: $count -- hasNext = $hasNextProduct")
                            if (hasNextProduct && count >= pageSize)
                                fetchListSellerOrder(count, page, dateFilter, role)
                        }
                    }
                ))


    suspend fun fetchListSellerOrder(
        start: Int = 0,
        page: String,
        dateFilter: String,
        role: String
    ) {

        if (start == lastProduct) {
            LoadingShow.postValue(false)
            return
        }

        lastProduct = start
        hasNextProduct = try {
            Timber.e("role --------- $role")
            if (page == "processed") {
                apiService.ListPurchasesProcess(pageSize, start, dateFilter)
                    .run {
                        if (start == 0) {
                            db.kwu().delete(page, role)
                            isEmptyOrder.postValue(data.isEmpty())
                        } else {
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
                                    "buyer",
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
                        isEmptyOrder.postValue(data.isEmpty())
                        data.size >= pageSize
                    }

            }
            else if (page == "done") { //done
                apiService.ListPurchasesDone(pageSize, start, dateFilter)
                    .run {
                        if (start == 0) {
                            db.kwu().delete(page, role)
                            isEmptyOrder.postValue(data.isEmpty())
                        } else {
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
                                    "buyer",
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
                        isEmptyOrder.postValue(data.isEmpty())
                        data.size >= pageSize
                    }
            }
            else { //  review
                apiService.ReviewTransaksiBuyer(pageSize, start, dateFilter)
                    .run {
                        if (start == 0) {
                            db.kwu().delete(page, role)
                            isEmptyOrder.postValue(data.isEmpty())
                        } else {
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
                                        sub.id,
                                    )
                                )
                            }

                        }
                        data.size >= pageSize
                    }
            }


        } catch (e: Exception) {
            LoadingShow.postValue(false)
            isEmptyOrder.postValue(true)
            Timber.e(e)
            false
        } finally {
            LoadingShow.postValue(false)
        }
    }


    // accept transaksi Buyer
    val BuyerActResponse by lazy { MutableLiveData<AcceptCancleBuyerResponse>() }
    suspend fun acceptTransaksiBuyer(transaksiId: Int) = try {
        LoadingShow.postValue(true)
        BuyerActResponse.postValue(apiService.acceptTransaksiBuyer(transaksiId))
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        Timber.e(e)
        errorString.postValue(e.message)
    }finally {
        LoadingShow.postValue(false)
    }

    // cancle transaksi Buyer

    suspend fun CancleTransaksiBuyer(transaksiId: Int) = try {
        LoadingShow.postValue(true)
        BuyerActResponse.postValue(apiService.cancleTransactionBUyer(transaksiId))
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        Timber.e(e)
        errorString.postValue(e.message)
    }finally {
        LoadingShow.postValue(false)
    }


    // post review
    val BuyerAnyResponse by lazy { MutableLiveData<Any>() }
    suspend fun PostReview(goodyReviewId: Int,rating:Int,comment:String) = try {
        LoadingShow.postValue(true)
        BuyerAnyResponse.postValue(apiService.postReviewBuyer(goodyReviewId,rating,comment))
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        Timber.e(e)
        errorString.postValue(e.message)
    }

}