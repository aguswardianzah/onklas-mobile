package id.onklas.app.pages.magang

import androidx.paging.DataSource
import androidx.room.*

@Dao
interface MagangDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(data: List<MagangSchedule>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(data: MagangSchedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(data: MagangCompany)

    @Transaction
    @Query("select * from magang_schedule order by date desc")
    fun schedulePagingSource(): DataSource.Factory<Int, MagangScheduleCompany>

    @Transaction
    @Query("select * from magang_schedule where id = :id")
    suspend fun getSchedule(id: Int): MagangScheduleCompany

    @Query("delete from magang_schedule")
    suspend fun deleteSchedule()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(data: List<MagangReportEntity>)

    @Query("select * from magang_report order by id")
    fun reportPagingSource(): DataSource.Factory<Int, MagangReportEntity>

    @Query("delete from magang_report")
    suspend fun deleteReport()
}