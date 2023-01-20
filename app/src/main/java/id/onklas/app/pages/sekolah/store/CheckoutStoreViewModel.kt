package id.onklas.app.pages.sekolah.store

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.login.StudentItem
import id.onklas.app.utils.PreferenceClass
import id.onklas.app.utils.StringUtil
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CheckoutStoreViewModel @Inject constructor(
    val pref: PreferenceClass,
    val db: MemoryDB,
    val stringUtil: StringUtil,
    val apiService: ApiService,
    val moshi: Moshi
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }
    val isLoading by lazy { MutableLiveData(true) }

    val klaspayBalance by lazy { MutableLiveData<Int>() }
    val subtotal by lazy { MutableLiveData<Int>() }
    val total by lazy { MutableLiveData<Int>() }
    val countItem by lazy { MutableLiveData<Int>() }
    val listItems by lazy { MutableLiveData<List<CartPaging>>() }
    val selectedShip by lazy { MutableLiveData<ListShipData>() }

    val address by lazy { MutableLiveData<AddressData>() }

    val shipItems by lazy { mutableListOf<ListShipData>() }

    val shipItemLive by lazy { MutableLiveData<List<ListShipData>>() }

    private val studentAdapter by lazy { moshi.adapter(StudentItem::class.java) }
    var student = try {
        studentAdapter.fromJson(pref.getString("student")) ?: StudentItem()
    } catch (e: Exception) {
        StudentItem()
    }

    init {
        viewModelScope.launch {
            val selectedCart = db.cart().getSelected()
            val items = selectedCart.filter { it.product != null }
            listItems.postValue(selectedCart)
            countItem.postValue(items.size)
            val tmpSubtotal = items.sumBy { it.product?.price ?: 0 }
            subtotal.postValue(tmpSubtotal)

            val userAddress = fetchUserAddress()?.data
            if (userAddress != null) {
                address.postValue(userAddress)
                student.user.address = userAddress.address
                student.user.province_id = userAddress.province.id
                student.user.province = userAddress.province.name
                student.user.city_id = userAddress.city.id
                student.user.city = userAddress.province.name
                student.user.sub_district_id = userAddress.sub_district?.id.orEmpty()
                student.user.sub_district = userAddress.sub_district?.name.orEmpty()
                pref.putString("student", studentAdapter.toJson(student))

                fetchShipments()
            }

            isLoading.postValue(false)
        }
    }

    suspend fun fetchShipments() = try {
        val selectedCart = db.cart().getSelected()
        val items = selectedCart.filter { it.product != null }
        val tmpSubtotal = items.sumBy { it.product?.price ?: 0 }

        shipItems.clear()
        shipItems.addAll(fetchShipping(items.map { it.product?.product_id!! }))
        val selectedShipItem = shipItems.minByOrNull { it.cost }
        selectedShip.postValue(selectedShipItem)

        val tmpShipItem = mutableListOf<ListShipData>()
        if (shipItems.isNotEmpty()) {
            shipItems.distinctBy { it.courier_name }.forEach { parent ->
                val selected =
                    parent == selectedShipItem || shipItems.filter { it.courier_name == parent.courier_name }
                        .any { it == selectedShipItem }
                tmpShipItem.add(parent.copy(isParent = true, selected = selected))
                shipItems.filter { it.courier_name == parent.courier_name }.forEach {
                    tmpShipItem.add(it.copy(selected = it == selectedShipItem))
                }
            }
        }
        shipItemLive.postValue(tmpShipItem)

        total.postValue(tmpSubtotal + (selectedShipItem?.cost ?: 0))
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
    }

    suspend fun fetchUserAddress() = try {
        apiService.getUserAddress()
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
        null
    }

    suspend fun fetchShipping(products: List<Int>) = try {
        apiService.getShipping(products).data
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
        emptyList()
    }

    suspend fun buyProduct(pin: String) = try {
        apiService.buyProduct(
            mutableMapOf<String, Any?>(
                "shipping_fee_id" to selectedShip.value?.id,
                "pin" to pin
            ).apply {
                listItems.value?.filter { it.product != null }?.forEach {
                    put("product[]", it.product?.product_id)
                }
            }
        )
        true
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
        false
    }
}