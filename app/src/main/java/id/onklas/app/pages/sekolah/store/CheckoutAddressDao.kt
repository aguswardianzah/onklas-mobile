package id.onklas.app.pages.sekolah.store

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CheckoutAddressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvince(data: List<Province>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvince(data: Province)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(data: List<City>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(data: City)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDistrict(data: List<District>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDistrict(data: District)

    @Query("select * from province order by name")
    fun provincePaging(): DataSource.Factory<Int, Province>

    @Query("select * from province where id = :id limit 1")
    suspend fun singleProvince(id: String): Province

    @Query("select * from city where province_id = :provinceId order by name")
    fun cityPaging(provinceId: String): DataSource.Factory<Int, City>

    @Query("select * from city where id = :id limit 1")
    suspend fun singleCity(id: String): City

    @Query("select count(*) from city where province_id = :provinceId")
    suspend fun countCity(provinceId: String): Int

    @Query("select * from district where city_id = :cityId order by name")
    fun districtPaging(cityId: String): DataSource.Factory<Int, District>

    @Query("select * from district where id = :id limit 1")
    suspend fun singleDistrict(id: String): District

    @Query("select count(*) from district where city_id = :cityId order by name")
    suspend fun countDistrict(cityId: String): Int
}