package id.onklas.app.pages.entrepreneurs.myMerchant.mystore

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.sekolah.store.ProductTable
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import id.onklas.app.utils.StringUtil
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ProductViewModel @Inject constructor(
    val api: ApiService,
    val db: MemoryDB,
    val stringUtil: StringUtil,
    val pref: PreferenceClass
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }

    val isEmptyProduct by lazy { MutableLiveData<Boolean>() }
    val isLoadingProduct by lazy { MutableLiveData(false) }

    private val myMerchantId = pref.getInt("merchantId")
    private val pageSize = 20
    fun myProducts() =
        db.store().getProductByMerchant(myMerchantId).toLiveData(
            pageSize,
            boundaryCallback = PagedListBoundaryCallback(
                { fetchMyProducts() },
                {
                    viewModelScope.launch {
                        val count = db.store().countProductByMerchant(myMerchantId)
                        if (count >= pageSize)
                            fetchMyProducts(count)
                    }
                },
            )
        )

    private var lastStart = -1
    private var hasMore = true
    fun fetchMyProducts(start: Int = 0) {
        if (lastStart == start || isLoadingProduct.value == true || !hasMore)
            return

        lastStart = start
        viewModelScope.launch {
            try {
                isLoadingProduct.postValue(true)
                val data = api.productMerchantUser(
                    pageSize,
                    start
                ).data

                db.store().insertProduct(data.map { ProductTable(it, myMerchantId) })
                hasMore = data.size >= pageSize
            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
            } finally {
                isLoadingProduct.postValue(false)
            }
        }
    }

    fun refresh() {
        lastStart = -1
        isLoadingProduct.postValue(false)
        hasMore = true
        fetchMyProducts()
    }
}