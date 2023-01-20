package id.onklas.app.pages.perpus

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PerpusDao {

    // banner start
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(data: List<PerpusBanner>)

    @Query("select * from perpus_banner order by id")
    fun getBanner(): DataSource.Factory<Int, PerpusBanner>

    @Query("select * from perpus_banner order by id")
    suspend fun listBannerAll(): List<PerpusBanner>

    // banner end

    // start book
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(data: List<BookTable>)

    @Query("select * from book where type = 'newest'")
    suspend fun getNewest(): List<BookTable>

    @Query("select * from book where type = 'best'")
    suspend fun getBest(): List<BookTable>

    @Query("select * from book where title like :search")
    suspend fun searchBook(search: String): List<BookTable>

    @Query("select * from book where id = :id")
    suspend fun getBook(id: Int): List<BookTable>
    // end book

    // start rent
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRent(data: List<BookRentTable>)

    @Query("select * from book_rent where status < 2 order by return_at")
    fun getRentOngoing(): DataSource.Factory<Int, BookRentTable>

    @Query("select count(*) from book_rent where status < 2 order by return_at")
    suspend fun countRentOngoing(): Int

    @Query("select * from book_rent where status > 1 order by return_at")
    fun getRentFinish(): DataSource.Factory<Int, BookRentTable>

    @Query("select count(*) from book_rent where status > 1 order by return_at")
    suspend fun countRentFinish(): Int
    // end rent
}