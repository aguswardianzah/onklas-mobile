package id.onklas.app.pages.presensi

import androidx.room.*

@Dao
interface PresensiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<ScheduleTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: ScheduleDetailTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(data: List<ScheduleAttendanceTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbsensi(data: List<AbsensiTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRekap(data: List<RekapAbsensiTable>)

    @Query("select * from schedule where name_of_day = :date order by time_plot_start_at")
    suspend fun getJadwal(date: String): List<ScheduleTable>

    @Query("select * from schedule where id = :scheduleId limit 1")
    suspend fun getJadwal(scheduleId: Int): ScheduleTable?

    @Transaction
    @Query("select * from schedule_detail where subject_schedule_id = :schedule_id limit 1")
    suspend fun getDetailSchedule(schedule_id: Int): ScheduleDetailAttendance?

    @Query("select * from absensi where month = :month and year = :year order by date")
    suspend fun getAbsensi(month: Int, year: Int): List<AbsensiTable>

    @Query("select * from absensi where attend_at != '' or leave_at != '' order by date desc limit 1")
    suspend fun getLastAbsensi(): AbsensiTable?

    @Query("select * from rekap_absensi where year = :year order by `order`")
    suspend fun getRekapAbsensi(year: Int): List<RekapAbsensiTable>

    @Query("delete from schedule where date = :date")
    suspend fun deleteScheduleByDate(date: String)
}