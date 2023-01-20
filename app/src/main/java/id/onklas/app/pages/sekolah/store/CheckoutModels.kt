package id.onklas.app.pages.sekolah.store

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString

@Keep
@JsonClass(generateAdapter = true)
data class ProvinceResponse(
    val data: List<Province> = emptyList()
)

@Entity(tableName = "province")
@Keep
@JsonClass(generateAdapter = true)
data class Province(
    @NullToEmptyString @PrimaryKey val id: String = "",
    @NullToEmptyString val name: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Keep
@JsonClass(generateAdapter = true)
data class CityResponse(
    val data: List<City> = emptyList()
)

@Entity(tableName = "city")
@Keep
@JsonClass(generateAdapter = true)
data class City(
    @NullToEmptyString @PrimaryKey val id: String = "",
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val province_id: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Keep
@JsonClass(generateAdapter = true)
data class DistrictResponse(
    val data: List<District> = emptyList()
)

@Entity(tableName = "district")
@Keep
@JsonClass(generateAdapter = true)
data class District(
    @NullToEmptyString @PrimaryKey val id: String = "",
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val city_id: String = "",
    @NullToEmptyString val province_id: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Keep
@JsonClass(generateAdapter = true)
data class AddressResponse(
    val data: AddressData = AddressData()
)

@Keep
@JsonClass(generateAdapter = true)
data class AddressData(
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val address: String = "",
    val province: Province = Province(),
    val city: City = City(),
    val sub_district: District? = District()
)