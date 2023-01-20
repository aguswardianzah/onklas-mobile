package id.onklas.app.pages.sekolah.store

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import androidx.room.withTransaction
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.StringUtil
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class CartViewModel @Inject constructor(
        val api: ApiService,
        val apiWrapper: ApiWrapper,
        val db: MemoryDB,
        val stringUtil: StringUtil
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }

    val isEmptyCart by lazy { MutableLiveData<Boolean>() }
    val isLoadingCart by lazy { MutableLiveData(true) }
    val countSelected by lazy { MutableLiveData(0) }
    val totalPrice by lazy { MutableLiveData(0) }
    val countCart by lazy { MutableLiveData<Int>() }

    init {
        viewModelScope.launch {
            val listSelected = db.cart().getSelected()
            countSelected.postValue(listSelected.size)
            totalPrice.postValue(
                    listSelected.sumBy { it.cart.quantity * (it.product?.price ?: 0) })

            val count = db.cart().countCart()
            countCart.postValue(count)
//            if (count > 0)
            fetchCart()
        }
    }

    fun fetchCart() = viewModelScope.launch {
        try {
            isLoadingCart.postValue(true)
            val data = api.loadCart().data
            db.withTransaction {
                db.cart().clear()
                data.forEach {
                    it.goodies.forEach { good ->
                        val newCart = CartTable(
                                0, it.merchant.id, it.merchant.name, good.goodie.id, good.quantity
                        )
                        val oldCart = db.cart().getCart(it.merchant.id, good.goodie.id)
                        if (oldCart == null)
                            db.cart().insert(newCart)
                        else
                            db.cart().updateQuantity(good.quantity, it.merchant.id, good.goodie.id)

                        db.store().insertProduct(ProductTable(good.goodie))
                    }
                    val oldMerchant = db.cart().getCart(it.merchant.id, 0)
                    if (oldMerchant == null) {
                        db.cart().insert(
                                CartTable(
                                        merchant_id = it.merchant.id,
                                        merchant_name = it.merchant.name,
                                        showItem = false
                                )
                        )
                    } else {
                        db.cart().updateMerchant(it.merchant.id, it.merchant.name)
                    }
                }
            }
            isLoadingCart.postValue(false)

            isEmptyCart.postValue(data.isEmpty())
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.message)
            isEmptyCart.postValue(true)
        } finally {
            isLoadingCart.postValue(false)
        }
    }

    val cartData by lazy {
        db.cart().cartPagingSource().toLiveData(
                50,
                boundaryCallback = PagedListBoundaryCallback({
//                fetchCart()
                })
        )
    }

    fun selectItem(item: CartPaging, selected: Boolean) {
        viewModelScope.launch {
            db.withTransaction {
                if (item.cart.product_id > 0) {
                    db.cart().selectByProduct(item.cart.product_id, selected)
                    val countSelectedMerchant =
                            db.cart().countSelectedByMerchant(item.cart.merchant_id)
                    if ((selected && countSelectedMerchant == db.cart()
                                    .countByMerchant(item.cart.merchant_id) - 1)
                            || (!selected && countSelectedMerchant == 0)
                    ) {
                        db.cart().selectMerchant(item.cart.merchant_id, selected)
                    }
                } else
                    db.cart().selectByMerchant(item.cart.merchant_id, selected)
            }
        }
    }
}