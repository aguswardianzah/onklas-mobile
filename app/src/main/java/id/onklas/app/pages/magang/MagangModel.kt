package id.onklas.app.pages.magang

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString

@Keep
@JsonClass(generateAdapter = true)
data class MagangResponse(
    val data: List<MagangData> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class MagangData(
    val id: Int,
    val company: MagangCompany? = null,
    val schedule: MagangScheduleResp? = null,
    val attendance: MagangAttendance? = null
)

@Keep
@JsonClass(generateAdapter = true)
data class MagangScheduleResp(
    val date: String? = "",
    val start_time: String? = "",
    val end_time: String? = "",
)

@Keep
@JsonClass(generateAdapter = true)
data class MagangAttendance(
    val status: String? = "",
    val can_attend: Boolean? = false,
    val can_leave: Boolean? = false,
)

@Keep
@JsonClass(generateAdapter = true)
data class MagangReportResp(
    val data: List<MagangReportData> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class MagangReportData(
    val id: Int = 0,

    @NullToEmptyString
    val check_in_at: String = "",

    @NullToEmptyString
    val check_in_status: String = "",

    @NullToEmptyString
    val check_out_at: String = "",

    @NullToEmptyString
    val check_out_status: String = "",

    @NullToEmptyString
    val daily_report: String = "",

    val location: MagangReportLoc = MagangReportLoc(),
    val company: MagangReportCompany = MagangReportCompany()
)

@Keep
@JsonClass(generateAdapter = true)
data class MagangReportLoc(
    val latitide: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)

@Keep
@JsonClass(generateAdapter = true)
data class MagangReportCompany(
    @NullToEmptyString
    val name: String = "",
    @NullToEmptyString
    val address: String = "",
    @NullToEmptyString
    val latitude: String = "",
    @NullToEmptyString
    val longitude: String = "",
)
