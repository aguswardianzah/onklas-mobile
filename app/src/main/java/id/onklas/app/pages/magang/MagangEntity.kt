package id.onklas.app.pages.magang

import androidx.annotation.Keep
import androidx.room.*
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString
import id.onklas.app.pages.sekolah.store.ProductTable

@Keep
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "magang_company",
    indices = [Index(name = "magang_company_idx", unique = true, value = ["id"])]
)
data class MagangCompany(
    @PrimaryKey
    val id: Int = 0,
    val name: String = "",
    val address: String? = "",
    val latitude: String? = null,
    val longitude: String? = null
)

@Entity(
    tableName = "magang_schedule",
    indices = [Index(name = "magang_schedule_idx", unique = true, value = ["id"])]
)
data class MagangSchedule(
    @PrimaryKey
    val id: Int = 0,
    val company_id: Int? = null,
    val date: String = "",
    val date_formatted: String = "",
    val start_time: String? = "",
    val end_time: String? = "",
    val att_status: String? = "",
    val can_attend: Boolean = false,
    val can_leave: Boolean = false,
)

data class MagangScheduleCompany(
    @Embedded val schedule: MagangSchedule,
    @Relation(entity = MagangCompany::class, parentColumn = "company_id", entityColumn = "id")
    val company: MagangCompany?
)

@Entity(tableName = "magang_report")
data class MagangReportEntity(
    @PrimaryKey
    val id: Int = 0,
    val check_in_at: String = "",
    val check_in_status: String = "",
    val check_out_at: String = "",
    val check_out_status: String = "",
    val daily_report: String = "",
    val location_lat: String = "",
    val location_lon: String = "",
    val company_name: String = "",
    val company_address: String = "",
    val company_lat: String = "",
    val company_lon: String = ""
)