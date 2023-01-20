package id.onklas.app.pages.presensi

import androidx.annotation.Keep
import androidx.room.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString
import id.onklas.app.pages.theory.TeacherItem
import id.onklas.app.pages.theory.TeacherTable

@Keep
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "schedule",
    indices = [Index(name = "schedules_idx", unique = true, value = ["id"])]
)
data class ScheduleTable(
    @PrimaryKey @Json(name = "subject_schedule_id") val id: Int = 0,
    val date: String = "",
    val name_of_day: String = "",
    val school_name: String = "",
    val class_name: String = "",
    val teacher_name: String = "",
    val teacher_nip: String = "",
    val subject_name: String = "",
    @Json(name = "subject_icon_image") val subject_image: String = "",
    val time_plot_start_at: String = "",
    val time_plot_end_at: String = "",
    var status: String = "",
    @NullToEmptyString val attend_at: String = "",
    @NullToEmptyString val leave_at: String = "",
    @NullToEmptyString var late_at: String = "",
    var timeLeft: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class PresensiListResponse(val data: List<ScheduleTable> = emptyList())

@Keep
@JsonClass(generateAdapter = true)
data class PresensiClassRekapResponse(val data: PresensiClassRekapData = PresensiClassRekapData())

@Keep
@JsonClass(generateAdapter = true)
data class PresensiClassRekapData(
    val id: Int = 0,
    @NullToEmptyString val subject: String = "",
    @NullToEmptyString val classroom: String = "",
    @NullToEmptyString val password: String = "",
    @NullToEmptyString val time_plot: String = "",
    @NullToEmptyString val status: String = "",
    @NullToEmptyString val start_time: String = "",
    @NullToEmptyString val end_time: String = "",
    val att_presence: Int? = null,
    val att_not_presence: Int? = null,
    val att_total: Int? = null,
    val teacher: TeacherItem = TeacherItem(),
    val attendances: List<PresensiClassAttendance> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class PresensiClassAttendance(
    @NullToEmptyString val nisn: String = "",
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val nis: String = "",
    @NullToEmptyString val start_time: String = "",
    val is_late: Boolean? = true
)

@Keep
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "schedule_detail",
    indices = [Index(name = "schedule_detail_idx", value = ["id"], unique = true)]
)
data class ScheduleDetailTable(
    @PrimaryKey @Json(name = "attendance_id") val id: Int = 0,
    val subject_schedule_id: Int = 0,
    val class_password: String = "",
    val start_at: String = "",
    val end_at: String = "",
    val late_limit: String = "",
    val teacher_name: String = "",
    val school_name: String = "",
    val school_major_name: String = "",
    val subject_name: String = "",
    var subject_image: String = "",
    val class_name: String = "",
    val grade: String = "",
    val time_plot: String = "",
    val plot_start_at: String = "",
    val plot_end_at: String = "",
    @Json(name = "att_presence") val total_attended_student: String = "",
    val total_student: String = "",
    val attend_at: String = "",
    val leave_at: String = ""
)

@Entity(
    tableName = "schedule_attendance",
    indices = [Index("attendance_id", "nisn", unique = true)]
)
data class ScheduleAttendanceTable(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attendance_id: Int = 0,
    val student_name: String = "",
    val nisn: String = "",
    val is_late: Boolean = false,
    val is_present: Boolean = false
)

data class ScheduleDetailAttendance(
    @Embedded val detail: ScheduleDetailTable,
    @Relation(parentColumn = "id", entityColumn = "attendance_id")
    val attendances: List<ScheduleAttendanceTable> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class ScheduleDetailResponse(val data: ScheduleDetailTable = ScheduleDetailTable())

@Keep
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "absensi",
    indices = [Index(name = "absensi_idx", unique = true, value = ["date"])]
)
data class AbsensiTable(
    @PrimaryKey val date: String = "",
    var month: Int = 0,
    var year: Int = 0,
    @NullToEmptyString var dateLabel: String = "",
    @NullToEmptyString var attend_at: String = "",
    @NullToEmptyString var leave_at: String = "",
    val is_holiday: Boolean = false,
    val attend_is_late: Boolean = false,
    val leave_is_early: Boolean = false
)

@Keep
@JsonClass(generateAdapter = true)
data class AbsensiResponse(val data: List<AbsensiTable> = emptyList())

@Keep
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "rekap_absensi",
    indices = [Index(name = "rekap_absensi_idx", unique = true, value = ["date"])]
)
data class RekapAbsensiTable(
    @PrimaryKey var date: String = "",
    var month: String = "",
    var year: Int = 0,
    var ontime: String = "",
    var late: String = "",
    var order: Int = 0
)

@Keep
@JsonClass(generateAdapter = true)
data class RekapAbsensiResponse(val data: List<RekapAbsensiTable> = emptyList())

@Keep
@JsonClass(generateAdapter = true)
data class CheckAbsenResponse(
    @NullToEmptyString val message: String = "",
    val data: CheckAbsenResponseData = CheckAbsenResponseData()
)

@Keep
@JsonClass(generateAdapter = true)
data class CheckAbsenResponseData(
    val allow_attendance: Boolean = false,
    @NullToEmptyString val type_attendance: String = "",
    @NullToEmptyString val attendanceResponseText: String = "",
    @NullToEmptyString val attendanceResponseTextButton: String = ""
)