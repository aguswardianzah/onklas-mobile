package id.onklas.app.pages.sekolah.store

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.login.StudentItem
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CheckoutAddressViewModel @Inject constructor(
    val apiService: ApiService,
    val db: MemoryDB,
    val moshi: Moshi,
    val pref: PreferenceClass
) : ViewModel() {

    private val pageSize = 20
    val errorString by lazy { MutableLiveData<String>() }
    val loadingData by lazy { MutableLiveData<Boolean>() }

    private val studentAdapter by lazy { moshi.adapter(StudentItem::class.java) }
    var student = try {
        studentAdapter.fromJson(pref.getString("student")) ?: StudentItem()
    } catch (e: Exception) {
        StudentItem()
    }

    val selectedProvince by lazy { MutableLiveData<Province>() }
    val selectedCity by lazy { MutableLiveData<City>() }
    val selectedDistrict by lazy { MutableLiveData<District>() }
    val inputAddress by lazy { MutableLiveData<String>() }

    init {
        inputAddress.postValue(student.user.address)

        val provinceId = student.user.province_id
        val cityId = student.user.city_id

        selectedProvince.postValue(Province(provinceId, student.user.province))
        selectedCity.postValue(City(cityId, student.user.city, provinceId))
        selectedDistrict.postValue(
            District(
                student.user.sub_district_id,
                student.user.sub_district,
                cityId,
                provinceId
            )
        )
    }

    val listProvince by lazy {
        db.checkoutAddress().provincePaging()
            .toLiveData(pageSize, boundaryCallback = PagedListBoundaryCallback({
                viewModelScope.launch {
                    fetchProvince()
                }
            }))
    }

    private suspend fun fetchProvince() = try {
        loadingData.postValue(true)
        db.checkoutAddress().insertProvince(apiService.listProvinces().data)
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
    } finally {
        loadingData.postValue(false)
    }

    fun listCity(provinceId: String) = db.checkoutAddress().cityPaging(provinceId)
        .toLiveData(pageSize, boundaryCallback = PagedListBoundaryCallback({
            viewModelScope.launch {
                fetchCity(provinceId)
            }
        }, {
            viewModelScope.launch {
                val count = db.checkoutAddress().countCity(provinceId)

                if (count > pageSize && hasNextCity)
                    fetchCity(provinceId, count / pageSize + 1)
            }
        }))

    private var hasNextCity = true
    private var isFetchingCity = false
    private var lastPageCity = 0
    private suspend fun fetchCity(provinceId: String, page: Int = 1) {
        if (isFetchingCity || lastPageCity == page)
            return

        isFetchingCity = true
        lastPageCity = page

        try {
            loadingData.postValue(true)
            val data = apiService.listCities(provinceId, pageSize, page).data
            db.checkoutAddress().insertCity(data)

            hasNextCity = data.size >= pageSize
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.message)
        } finally {
            isFetchingCity = false
            loadingData.postValue(false)
        }
    }

    fun listDistrict(cityId: String) = db.checkoutAddress().districtPaging(cityId)
        .toLiveData(pageSize, boundaryCallback = PagedListBoundaryCallback({
            viewModelScope.launch {
                fetchDistrict(cityId)
            }
        }, {
            viewModelScope.launch {
                val count = db.checkoutAddress().countDistrict(cityId)

                if (count > pageSize && hasNextDistrict)
                    fetchDistrict(cityId, count / pageSize + 1)
            }
        }))

    private var hasNextDistrict = true
    private var isFetchingDistrict = false
    private var lastPageDistrict = 0
    private suspend fun fetchDistrict(cityId: String, page: Int = 1) {
        if (isFetchingDistrict || lastPageDistrict == page)
            return

        isFetchingDistrict = true
        lastPageCity = page

        try {
            loadingData.postValue(true)
            val data = apiService.listDistrict(cityId, pageSize, page).data
            db.checkoutAddress().insertDistrict(data)

            hasNextDistrict = data.size >= pageSize
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.message)
        } finally {
            isFetchingDistrict = false
            loadingData.postValue(false)
        }
    }

    suspend fun saveAddress() = try {
        apiService.setUserAddress(
            mapOf(
                "province_id" to selectedProvince.value?.id,
                "city_id" to selectedCity.value?.id,
                "sub_district_id" to selectedDistrict.value?.id,
                "address" to inputAddress.value
            )
        )

        student.user.address = inputAddress.value.orEmpty()
        student.user.province_id = selectedProvince.value?.id.orEmpty()
        student.user.province = selectedProvince.value?.name.orEmpty()
        student.user.city_id = selectedCity.value?.id.orEmpty()
        student.user.city = selectedCity.value?.name.orEmpty()
        student.user.sub_district_id = selectedDistrict.value?.id.orEmpty()
        student.user.sub_district = selectedDistrict.value?.name.orEmpty()
        pref.putString("student", studentAdapter.toJson(student))

        true
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
        false
    }
}