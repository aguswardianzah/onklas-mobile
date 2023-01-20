package id.onklas.app.pages.entrepreneurs

import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import androidx.room.withTransaction
import com.squareup.moshi.Moshi
import id.onklas.app.R
import id.onklas.app.api.ApiException
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.klaspay.aktivasi.KlaspayWalletData
import id.onklas.app.pages.sekolah.store.MerchantItem
import id.onklas.app.pages.sekolah.store.MerchantResponse
import id.onklas.app.pages.sekolah.store.MerchantSummaryItem
import id.onklas.app.pages.sekolah.store.ProductMerchantTable
import id.onklas.app.utils.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class EntrepreneursVM @Inject constructor(
    val pref: PreferenceClass,
    val apiService: ApiService,
    val numberUtil: NumberUtil,
    val stringUtil: StringUtil,
    val moshi: Moshi,
    val dateUtil: DateUtil,
    val intentUtil: IntentUtil,
    val apiWrapper: ApiWrapper,
    val db: MemoryDB,
) : ViewModel() {

    val titleBar by lazy { MutableLiveData("") }
    val errorString by lazy { MutableLiveData("") }
    val errorProduct by lazy { MutableLiveData(false) }
    val dateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", Locale("id")) }

    //        val errorJasa by lazy { MutableLiveData(true) }
    val errorJasa = true
    val dialogEntrepreneurs by lazy { MutableLiveData(pref.getBoolean("dialogEntrepreneurs")) }

    //        val IncomingOrderPostition by lazy {  MutableLiveData<String>() }
    var IncomingOrderPostition = ""
    //    position
    //    0  incoming order (product)
    //    1  incoming order (service)
    //    2  onprocess (product)
    //    3  onprocess (service)

    val Merchant by lazy { MutableLiveData<MerchantItem>() }

    private val pageSize = 20
    var lastProduct = -1
    var hasNextProduct = true
    val loadingShow by lazy { MutableLiveData(true) }
    val merchantSummaryItem by lazy { MutableLiveData<MerchantSummaryItem>() }

    val menu_item by lazy {
        listOf(
            MenuItemTable("1", R.drawable.ic_mystore, "Toko ku", "0 Produk", ""),
            MenuItemTable("2", R.drawable.ic_income, "Total pendapatan", "0", "currency"),
            MenuItemTable("3", R.drawable.ic_incoming_orders, "Orderan Masuk", "0 ", ""),
            MenuItemTable("4", R.drawable.ic_review, "Lihat Review", "0", ""),
            MenuItemTable("5", R.drawable.ic_order_history, "Riwayat Orderan", "0", ""),
        )
    }

    val menu_item2 by lazy {
        listOf(
            MenuItemTable("1", R.drawable.ic_purchase, "Pembelian", "0", ""),
            MenuItemTable("2", R.drawable.ic_waiting_review, "Menunggu Review", "0", ""),
        )
    }

    val product by lazy {
        listOf(
            Product(
                "00",
                "Cetaphill Gentle Skin Cleanser 59 ML",
                "https://picsum.photos/200/200",
                "5",
                "122",
                "400",
            ),
            Product(
                "",
                "Sarung Tangan Jempol Untuk Main PUBG  ",
                "https://picsum.photos/200/200",
                "1",
                "100",
                "200",
            ),
            Product(
                "",
                "Lem Kain Fashion Glue Kerajinan Tangan Fab...",
                "https://picsum.photos/200/200",
                "5",
                "122",
                "400",
            ),
            Product(
                "",
                "Ring Rotan Kerajinan Tangan",
                "https://picsum.photos/200/200",
                "5",
                "122",
                "400",
            ),
            Product(
                "",
                "COSRX Low PH Goog Morning Gel Cleanser",
                "https://picsum.photos/200/200",
                "5",
                "122",
                "400",
            ),
            Product(
                "",
                "Celana Pendek Pria Jumbo Polos Kaos ",
                "https://picsum.photos/200/200",
                "5",
                "122",
                "400",
            ),
        )
    }

    val productBuy by lazy {
        listOf(
            ProductBuy(
                "",
                "Celana Pendek Pria Jumbo Polos Kaos ",
                "https://picsum.photos/200/200",
                100,
                4599.0,
            ),
            ProductBuy(
                "",
                "COSRX Low PH Goog Morning Gel Cleanser",
                "https://picsum.photos/200/200",
                3,
                47349.0,
            ),
        )
    }

    val buyhistory by lazy {
        listOf(
            HistoryBuy(
                "",
                "4 sept 2021",
                productBuy,
            ),
            HistoryBuy(
                "",
                "3 sept 2021",
                productBuy,
            ),
            HistoryBuy(
                "",
                "2 sept 2021",
                productBuy,
            ),
        )

    }

    val MyProduct by lazy {
        listOf(
            MyProduct(
                "",
                "Parfum Botol 150ml Original untuk Wanita",
                "https://picsum.photos/200/200",
                20,
                50000,
                1,
            ),
            MyProduct(
                "",
                "Paket Epoxy Resin Kit Wimpy DIY Craft KIT",
                "https://picsum.photos/200/200",
                10000,
                50000,
                0,
            ),
            MyProduct(
                "",
                "Ring Rotan Kerajinan Tangan",
                "https://picsum.photos/200/200",
                0,
                250000,
                1,
            ),
            MyProduct(
                "",
                "Tas Wanita - Pouch Lips Warna Kuning Tersedia 4 Varian Warna",
                "https://picsum.photos/200/200",
                0,
                50000,
                2,
            ),
            MyProduct(
                "",
                "Topi Mini Santa Claus Kerajinan Tangan",
                "https://picsum.photos/200/200",
                0,
                50000,
                3,
            ),
        )

    }

    val img by lazy {
        listOf(
            ProductAddImg(
                0,
                "R.drawable.img_add_product",
                "",
                "",
                "",
                0,
                0,
            ),
            ProductAddImg(
                0,
                "R.drawable.img_add_product",
                "",
                "",
                "",
                0,
                0,
            ),
            ProductAddImg(
                0,
                "R.drawable.img_add_product",
                "",
                "",
                "",
                0,
                0,
            ),
            ProductAddImg(
                0,
                "R.drawable.img_add_product",
                "",
                "",
                "",
                0,
                0,
            ),
            ProductAddImg(
                0,
                "R.drawable.img_add_product",
                "",
                "",
                "",
                0,
                0,
            ),
            ProductAddImg(
                0,
                "R.drawable.img_add_product",
                "",
                "",
                "",
                0,
                0,
            ),
        )
    }

    val makanan by lazy {
        listOf(
            CategoryChild(
                category_name = "bahan pokok",
            ), CategoryChild(
                category_name = "permen",
            ), CategoryChild(
                category_name = "makanan ringan ",
            ), CategoryChild(
                category_name = "makanan instan",
            )
        )
    }

    val minuman by lazy {
        listOf(
            CategoryChild(category_name = "marimas"),
            CategoryChild(category_name = "kopi"),
            CategoryChild(category_name = "josua"),
            CategoryChild(category_name = "segar sari"),
        )
    }

    val book by lazy {
        listOf(
            CategoryChild(category_name = "b indo"),
            CategoryChild(category_name = "matematika"),
            CategoryChild(category_name = "b jawa"),
            CategoryChild(category_name = "primbon"),
        )
    }

    val categoryParent by lazy {
        listOf(
            CategoryParent(
                category_name = "makanan",
                CategoryChild = makanan,
                category_icon = "https://picsum.photos/200/200",
                is_selected = "true"
            ),
            CategoryParent(
                category_name = "minuman",
                CategoryChild = minuman,
                category_icon = "https://picsum.photos/200/200",
                is_selected = "false"
            ),
            CategoryParent(
                category_name = "book",
                CategoryChild = book,
                category_icon = "https://picsum.photos/200/200",
                is_selected = "false"
            ),
            CategoryParent(
                category_name = "makanan",
                CategoryChild = makanan,
                category_icon = "https://picsum.photos/200/200",
                is_selected = "true"
            ),
            CategoryParent(
                category_name = "minuman",
                CategoryChild = minuman,
                category_icon = "https://picsum.photos/200/200",
                is_selected = "false"
            ),
            CategoryParent(
                category_name = "book",
                CategoryChild = book,
                category_icon = "https://picsum.photos/200/200",
                is_selected = "false"
            ),

            )
    }

    val marketingLoactionDummy by lazy {
        listOf(
            MarketingLocation(
                1,
                "SMA Onklas 1",
                true,
                true,
            ),
            MarketingLocation(
                2,
                "SMA Onklas 2",
                false,
                false,
            ),
            MarketingLocation(
                3,
                "SMA Onklas 3",
                false,
                false,
            ),
            MarketingLocation(
                4,
                "SMA Onklas 4",
                false,
                false,
            ),

            )
    }

    val dummyList by lazy { MutableLiveData<List<String>>() }

    suspend fun loadDummyList() = try {
        dummyList.postValue(
            listOf(
                "1",
                "1",
                "1",
                "1",
                "1",
            )
        )
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
    }

    suspend fun loadMerchantUser(): Flow<MerchantTable> {
        var count = 0
        viewModelScope.launch {
            db.withTransaction {
                loadingShow.postValue(false)
                fetchMerchant()
                count = db.kwu().countMerchant(pref.getString("merchantId"))
            }.apply {
                if (count == 0) {
                    fetchMerchant()
                }
            }
        }
        return db.kwu().getMerchant()
    }

    val editMerchantResponse by lazy { MutableLiveData<MerchantResponse>() }
    suspend fun editMerchantUser(name: String) {
        try {
            loadingShow.postValue(true)
            editMerchantResponse.postValue(apiService.editMerchantUser(name))
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.message)
        } finally {
            loadingShow.postValue(false)
        }
    }

    val createMerchantResponse by lazy { MutableLiveData<MerchantResponse>() }
    suspend fun createMerchantUser(name: String) {
        try {
            Timber.e("name $name")
            loadingShow.postValue(true)
            createMerchantResponse.postValue(apiService.createMerchantUser(name))
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.message)
        } finally {
            loadingShow.postValue(false)
        }
    }

    suspend fun editImgMerchantUser(filePath: String) {
        try {
            val file = File(filePath)
            val fileType =
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
                    ?.toMediaTypeOrNull()

            editMerchantResponse.postValue(
                apiService.editImgMerchantUser(
                    MultipartBody.Part.createFormData(
                        "file", file.name, file.asRequestBody(fileType)
                    )
                )
            )
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.message)
        } finally {
            loadingShow.postValue(false)
        }
    }

    fun fetchMerchant() {
        viewModelScope.launch {
            Timber.e("load --------- ")
            loadingShow.postValue(true)
            try {
                apiService.loadMerchantUser()
                    .run {
                        pref.putBoolean("has_store", true)
                        pref.putString("merchantId", data.id.toString())
                        db.kwu().deleteMerchant()
                        db.kwu().insetMerchant(
                            MerchantTable(
                                data.id,
                                data.name,
                                data.avatar,
                                data.owner?.name ?: "",
                                data.owner?.roles?.get(0)?.id ?: 0,
                                data.owner?.roles?.get(0)?.name ?: "",
                                data.owner?.school?.id ?: 0,
                                data.owner?.school?.name ?: "",
                                data.owner?.ownerClass?.id ?: 0,
                                data.owner?.ownerClass?.name ?: "",
                                data.owner?.ownerClass?.major?.id ?: 0,
                                data.owner?.ownerClass?.major?.name ?: "",
                                data.salesRating,
                                data.amountOfReviewer,
                                data.successTransactions,
                            ),
                        )
                    }
            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
                (e as? ApiException)?.let {
                    if (it.responseCode ?: 0 > 0) {
                        errorString.postValue(e.message)
                        Timber.e("response code load merchant : ${e.responseCode}")
                        if (e.responseCode == 400) {
                            pref.putBoolean("has_store", false)
                        }
                    }
                }
            } finally {
                loadingShow.postValue(false)
            }
        }
    }

    fun listProductMerchant(merchant_id: Int, merchantTabs: String) =
        db.store().getProductMerchant(merchant_id)
            .toLiveData(
                pageSize, boundaryCallback = PagedListBoundaryCallback(
                    {
                        viewModelScope.launch {
                            fetchproductMerchant(
                                0,
                                merchant_id,
                                merchantTabs
                            )
                        }
                    },
                    {
                        viewModelScope.launch {
                            val count = db.store().countProductMerchant(merchant_id.toString())
                            Timber.e("count product: $count -- hasNext = $hasNextProduct")
                            if (hasNextProduct)
                                fetchproductMerchant(count, merchant_id, merchantTabs)
                        }

                    }
                ))

    private suspend fun fetchproductMerchant(
        start: Int = 0,
        merchant_id: Int,
        merchantTabs: String
    ) {
        //merchnat tabs  -> best seller dan all

        if (start == lastProduct) {
            loadingShow.postValue(false)
            return
        }

        lastProduct = start
        if (merchantTabs == "bestSeller") {
            hasNextProduct = try {
                apiService.productBestSellerMerchantUser(pageSize, 0)
                    .run {
                        data.forEach {
                            db.store().insertProductMerchant(
                                ProductMerchantTable(
                                    "${merchant_id}, ${it.name}",
                                    merchant_id,
                                    it.id,
                                    it.name,
                                    it.description,
                                    it.price,
                                    it.stock,
                                    it.image,
                                    it.sold,
                                    it.rating,
                                ),
                            )
                        }
                        loadingShow.postValue(data.isEmpty())
                        data.size >= pageSize
                    }
            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
                false
            } finally {
                loadingShow.postValue(false)
            }
        } else {
            hasNextProduct = try {
                apiService.productMerchantUser(pageSize, 0)
                    .run {
                        data.forEach {
                            db.store().insertProductMerchant(
                                ProductMerchantTable(
                                    "${merchant_id}, ${it.name}",
                                    merchant_id,
                                    it.id,
                                    it.name,
                                    it.description,
                                    it.price,
                                    it.stock,
                                    it.image,
                                    it.sold,
                                    it.rating,
                                ),
                            )
                        }
                        loadingShow.postValue(data.isEmpty())
                        data.size >= pageSize
                    }
            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
                false
            } finally {
                loadingShow.postValue(false)
            }
        }
    }

    fun loadMerchantSummary(SummaryCode: String): Flow<MerchantSummaryTable> {
        loadingShow.postValue(false)
        fetchMerchantSummary(SummaryCode)
        return db.kwu().getMerchantSummary(SummaryCode, pref.getInt("merchantId"))
    }

    fun fetchMerchantSummary(SummaryCode: String) {
        viewModelScope.launch {
            Timber.e("load --------- ")
            loadingShow.postValue(true)
            if (SummaryCode == "MyMerchant") {
                try {
                    apiService.summaryMerchant()
                        .run {
                            loadingShow.postValue(false)
                            db.kwu().insetMerchantSummary(
                                MerchantSummaryTable(
                                    summaryCode = "MyMerchant",
                                    merchantId = pref.getInt("merchantId"),
                                    product = data.product,
                                    incoming_order = data.incoming_order,
                                    incoming_amount = data.incoming_amount,
                                    review = data.review,
                                    history_order = data.history_order,
                                )

                            )
                        }
                } catch (e: Exception) {
                    Timber.e(e)
                    errorString.postValue(e.message)
                } finally {
                    loadingShow.postValue(false)
                }
            } else {
                // Purchase
                try {
                    apiService.summaryMerchantPembelian()
                        .run {
                            loadingShow.postValue(false)
                            db.kwu().insetMerchantSummary(
                                MerchantSummaryTable(
                                    summaryCode = "Purchase",
                                    merchantId = pref.getInt("merchantId"),
                                    purchase = data.purchase,
                                    reviewable_order_purchase = data.reviewable_order,
                                )

                            )
                        }
                } catch (e: Exception) {
                    Timber.e(e)
                    errorString.postValue(e.message)
                } finally {
                    loadingShow.postValue(false)
                }
            }
        }
    }

    val klaspayData by lazy { MutableLiveData<KlaspayWalletData>() }
    suspend fun getWallet() {
        try {
            val res = apiService.klaspayWallet()
            if (res.rc == "00") {
                pref.putBoolean("klaspayActive", true)
                klaspayData.postValue(res.data)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun loadDetail(orderId: Int, role: String): Flow<DetailTransactionWithProduct> {
        viewModelScope.launch {
            fetchDetailTransaksi(orderId, role)
            loadingShow.postValue(false)
        }
        return db.kwu().getDetailTransaksi(orderId)
    }

    fun loadReview(
        orderId: Int,
        goodyReviewId: Int,
        goodyId: Int,
        role: String,
        tipe: String
    ): Flow<ReviewData> {
        return db.kwu().getReviewData(orderId, goodyReviewId, goodyId, tipe)
    }

    fun fetchDetailTransaksi(
        orderId: Int,
        role: String
    ) {
        // role  -> seller, sellerReview ,   buyer
        viewModelScope.launch {
            Timber.e("load Detail Transaksi --------- $role ")
            loadingShow.postValue(true)
            if (role == "seller") {
                try {
                    apiService.detailTransaksi(orderId)
                        .run {
                            loadingShow.postValue(false)
                            db.kwu().insertDetailTransaksi(
                                DetailTransaksi(
                                    data.id,
                                    data.transaction_date,
                                    role,
                                    data.buyer_name,
                                    "",
                                    "",
                                    data.courier_name,
                                    data.sub_total,
                                    data.courier_fee,
                                    data.total,
                                    data.status,
                                    data.destination_sub_district,
                                    data.destination_city,
                                    data.destination_province,
                                    data.destination_address,
                                ),
                            )

                            data.goodies.forEach {
                                val sub = it
                                db.kwu().insertOrderProduct(
                                    TransaksiProductTable(
                                        "${data.id} ${it.goody_name}".hashCode(),
                                        sub.goody_id,
                                        data.id,
                                        sub.goody_image,
                                        sub.goody_name,
                                        sub.goody_price,
                                        sub.goody_quantity,

                                        )
                                )
                            }
                        }
                } catch (e: Exception) {
                    Timber.e(e)
                    errorString.postValue(e.message)
                } finally {
                    loadingShow.postValue(false)
                }
            } else if (role == "sellerReview") {
                try {
                    apiService.detailReviewSellerTransaksi(orderId)
                        .run {
                            loadingShow.postValue(false)
                            db.kwu().insertDetailTransaksi(
                                DetailTransaksi(
                                    data.id,
                                    data.transaction_date,
                                    role,
                                    data.buyer_name,
                                    "",
                                    "",
                                    data.courier_name,
                                    data.sub_total,
                                    data.courier_fee,
                                    data.total,
                                    data.status,
                                    data.destination_sub_district,
                                    data.destination_city,
                                    data.destination_province,
                                    data.destination_address,
                                ),
                            )

                            data.goodies.forEach {
                                val sub = it
                                db.kwu().insertOrderProduct(
                                    TransaksiProductTable(
                                        "${data.id} ${it.goody_name}".hashCode(),
                                        sub.goody_id,
                                        data.id,
                                        sub.goody_image,
                                        sub.goody_name,
                                        sub.goody_price,
                                        sub.goody_quantity,
                                        sub.id,
                                    )
                                )
                                val reviewUser = sub.review_user
                                val reviewUserId = reviewUser?.id ?: 0
                                db.kwu().insertReviewData(
                                    ReviewData(
                                        id = "$sub.id + ${sub.goody_id} + $reviewUserId + $role".hashCode(),
                                        goody_review_id = sub.id,
                                        rating = reviewUser?.rating ?: 0,
                                        comment = reviewUser?.comment ?: "",
                                        data_id = reviewUser?.id ?: 0,
                                        data_name = reviewUser?.user?.name ?: "",
                                        data_avatar = reviewUser?.user?.avatar ?: "",
                                        tipe = "buyer",
                                        date = reviewUser?.date ?: "",
                                        order_id = data.id,
                                        goody_id = sub.goody_id
                                    )
                                )
                                val reviewMerchant = sub.review_merchant
                                val reviewMerchantId = reviewMerchant?.id ?: 0
                                db.kwu().insertReviewData(
                                    ReviewData(
                                        id = "$sub.id + ${sub.goody_id} + $reviewMerchantId + $role".hashCode(),
                                        goody_review_id = sub.id,
                                        rating = reviewMerchant?.rating ?: 0,
                                        comment = reviewMerchant?.comment ?: "",
                                        data_id = reviewMerchant?.id ?: 0,
                                        data_name = reviewMerchant?.user?.name ?: "",
                                        data_avatar = reviewMerchant?.user?.avatar
                                            ?: "",
                                        tipe = "merchant",
                                        date = reviewMerchant?.date ?: "",
                                        order_id = data.id,
                                        goody_id = sub.goody_id
                                    )
                                )
                            }
                        }
                } catch (e: Exception) {
                    Timber.e(e)
                    errorString.postValue(e.message)
                } finally {
                    loadingShow.postValue(false)
                }
            } else if (role == "buyer") { // buyer
                try {
                    apiService.detailTransaksiPembelian(orderId)
                        .run {
                            loadingShow.postValue(false)
                            db.kwu().insertDetailTransaksi(
                                DetailTransaksi(
                                    data.id,
                                    data.date,
                                    role,
                                    "",
                                    data.merchant?.name ?: "",
                                    data.merchant?.avatar ?: "",
                                    data.shipping_name,
                                    data.sub_total,
                                    data.shipping_fee,
                                    data.total,
                                    data.status,
                                    data.destination_sub_district,
                                    data.destination_city,
                                    data.destination_province,
                                    data.destination_address,
                                ),
                            )

                            data.goodies.forEach {
                                val sub = it
                                db.kwu().insertOrderProduct(
                                    TransaksiProductTable(
                                        "${data.id} ${it.goody_name}".hashCode(),
                                        sub.goody_id,
                                        data.id,
                                        sub.goody_image,
                                        sub.goody_name,
                                        sub.goody_price,
                                        sub.goody_quantity,
                                    )
                                )
                            }
                        }
                } catch (e: Exception) {
                    Timber.e(e)
                    errorString.postValue(e.message)
                } finally {
                    loadingShow.postValue(false)
                }
            } else if (role == "buyerReview") { // buyer
                try {
                    apiService.detailReviewBuyerTransaksi(orderId)
                        .run {
                            loadingShow.postValue(false)
                            db.kwu().insertDetailTransaksi(
                                DetailTransaksi(
                                    data.id,
                                    data.date,
                                    role,
                                    data.buyer_name,
                                    data.merchant?.name ?: "",
                                    data.merchant?.avatar ?: "",
                                    data.courier_name,
                                    data.sub_total,
                                    data.courier_fee,
                                    data.total,
                                    data.status,
                                    data.destination_sub_district,
                                    data.destination_city,
                                    data.destination_province,
                                    data.destination_address,
                                ),
                            )
                            data.goodies.forEach {
                                val sub = it
                                db.kwu().insertOrderProduct(
                                    TransaksiProductTable(
                                        "${data.id} ${it.goody_name}".hashCode(),
                                        sub.goody_id,
                                        data.id,
                                        sub.goody_image,
                                        sub.goody_name,
                                        sub.goody_price,
                                        sub.goody_quantity,
                                        sub.id,
                                    )
                                )
                                val reviewUser = sub.review_user
                                val reviewUserId = reviewUser?.id ?: 0
                                db.kwu().insertReviewData(
                                    ReviewData(
                                        id = "$sub.id + ${sub.goody_id} + $reviewUserId + $role".hashCode(),
                                        goody_review_id = sub.id,
                                        rating = reviewUser?.rating ?: 0,
                                        comment = reviewUser?.comment ?: "",
                                        data_id = reviewUser?.id ?: 0,
                                        data_name = reviewUser?.user?.name ?: "",
                                        data_avatar = reviewUser?.user?.avatar ?: "",
                                        tipe = "buyer",
                                        date = reviewUser?.date ?: "",
                                        order_id = data.id,
                                        goody_id = sub.goody_id
                                    )
                                )
                                val reviewMerchant = sub.review_merchant
                                val reviewMerchantId = reviewMerchant?.id ?: 0
                                db.kwu().insertReviewData(
                                    ReviewData(
                                        id = "$sub.id + ${sub.goody_id} + $reviewMerchantId + $role".hashCode(),
                                        goody_review_id = sub.id,
                                        rating = reviewMerchant?.rating ?: 0,
                                        comment = reviewMerchant?.comment ?: "",
                                        data_id = reviewMerchant?.id ?: 0,
                                        data_name = reviewMerchant?.user?.name ?: "",
                                        data_avatar = reviewMerchant?.user?.avatar
                                            ?: "",
                                        tipe = "merchant",
                                        date = reviewMerchant?.date ?: "",
                                        order_id = data.id,
                                        goody_id = sub.goody_id
                                    )
                                )
                            }
                        }
                } catch (e: Exception) {
                    Timber.e(e)
                    errorString.postValue(e.message)
                } finally {
                    loadingShow.postValue(false)
                }
            }
        }
    }

    fun loadTrackingDetail(transaksiId: Int): Flow<List<TrackingDetail>> {
        viewModelScope.launch {
            fetchTrackingDetail(transaksiId)
            loadingShow.postValue(false)
        }
        return db.kwu().getTrackingDetail(transaksiId)
    }

    fun fetchTrackingDetail(transaksiId: Int) {
        // merchnat tabs  -> best seller dan all
        viewModelScope.launch {
            loadingShow.postValue(true)
            try {
                apiService.trackingDetail(transaksiId)
                    .run {
                        loadingShow.postValue(false)
                        db.kwu().DeleteTrackingDetail(transaksiId)
                        data.forEach {
                            db.kwu().InsetTrackingDetail(
                                TrackingDetail(
                                    0,
                                    transaksiId,
                                    it.code,
                                    it.description,
                                    it.datetime,
                                ),
                            )
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
            } finally {
                loadingShow.postValue(false)
            }
        }
    }
}