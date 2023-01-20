package id.onklas.app.pages.prokes

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString
import id.onklas.app.di.modules.ObjectToList
import id.onklas.app.pages.homework.MajorTable

data class TipsMain(
    val id: Int = 0,
    val imgId: Int = 0,
    val tipsitem: List<TipsItem>,
    val note: String,
    val title: String = "",
    var isShowChild: Boolean = false
)

data class TipsItem(
    val iconId: Int = 0,
    val tipsTextt: String = ""
)


data class ListInfoCovid(
    @PrimaryKey val id: Int = 0,
    val parentId: Int = 0,
    val img: Int = 0,
    val text: String = "",
    val infoCode: Int = 0, // 1 banner, 2 mainTitle, 3 child
    var isShowChild: Boolean = false,
    val items: List<ListInfoCovid> = emptyList()
)

data class ListMenuForm(
    @PrimaryKey val id: Int = 0,
    val icon: Int,
    val title: String,
    val subtitle: String,
    val info: String,
    val date: String = "-",
    val time: String = "-",
    val viewType: Int = 0, //1 formulir, 2 vaksin dan persetujuan ortu,
    val status: Boolean = false
)

data class ListChoice(
    @PrimaryKey val id: Int = 0,
    val choiceText: String = "",
    var isChosen: Boolean = false,
    var key: String = "",
    val subChoice: List<ListChoice> = emptyList()
)


@Keep
@JsonClass(generateAdapter = true)
data class ResponseEarlyDetectionProkes(
    val message: String,
    val data: FormEarlyDetectionProkes? = null
)

@Keep
@JsonClass(generateAdapter = true)
data class FormEarlyDetectionProkes(
    val setting_globals_lable: String,
    val setting_globals_name: String,
    val setting_globals_value: GlobalsValueItem
)

@Keep
@JsonClass(generateAdapter = true)
data class GlobalsValueItem(
    val feel_indication: List<GlobalsValueInnerItem?>? = emptyList(),
    val history_of_illness: List<GlobalsValueInnerItem?>? = emptyList(),
    val way_of_travel: List<GlobalsValueInnerItem?>? = emptyList(),
    val public_transportion_choice: List<GlobalsValueInnerItem?>? = emptyList(),
)

@Keep
@JsonClass(generateAdapter = true)
data class GlobalsValueInnerItem(
    @NullToEmptyString val key: String,
    @NullToEmptyString val text: String,
)


@Keep
@JsonClass(generateAdapter = true)
data class ChoiceResponse(
    val way_of_travel: WayOfTravel? = null,
    val public_transportion_choice: PublicTransportationChoice? = null
)


@Keep
@JsonClass(generateAdapter = true)
data class WayOfTravel(
    val walk: String = "",
    val ride_join: String = "",
    val private_vehicle: String = "",
    val public_transportation: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class PublicTransportationChoice(
    val nus: String = "",
    val ship: String = "",
    val taxi: String = "",
    val train: String = "",
    val public: String = "",
    val private: String = "",
    val rickshaw: String = ""
)


@Keep
@JsonClass(generateAdapter = true)
data class ResponseCheckReport(
    val error: String = "",
    val message: String = "",
    val data: CheckReportItem? = null
)

@Keep
@JsonClass(generateAdapter = true)
data class CheckReportItem(
    val date_input: String = "",
    val time_input: String = "",
    val target_type: String = "",
    val target_id: Int = 0,
    val check: Check?,
    val updated_at: String = "",
    val created_at: String = "",
    val id: Int = 0,
    val vaccinated: Boolean = false
)

@Keep
@JsonClass(generateAdapter = true)
data class Check(
    val student: cekStudentItem?,
    @NullToEmptyString val surveyor: String = "",
    val vaccinated: Boolean = false,
    val staff: Staff?
)

@Keep
@JsonClass(generateAdapter = true)
data class Staff(
    val email: String = "",
    val user_id: Int = 0,
    val user_name: String = "",
)

@Keep
@JsonClass(generateAdapter = true)
data class cekStudentItem(
    val way_of_travel: GlobalsValueInnerItem? = null,
    val already_vaccinated: Boolean = false,
    @ObjectToList val public_transportion_choice: List<GlobalsValueInnerItem> = emptyList(),
    val temperature: String = "",
    val feel_indication: List<GlobalsValueInnerItem> = emptyList(),
    val history_of_illness: GlobalsValueInnerItem? = null,
)


@Keep
@JsonClass(generateAdapter = true)
data class ListClassResponse(
    val data: List<ListClassItem>
)


@Keep
@JsonClass(generateAdapter = true)
data class ListClassItem(
    @PrimaryKey val id: Int,
    val name: String,
    val grade: Int,
    val major: MajorTable?,
    val total_student: String,
    val total_student_screening: String,
    val total_student_remaining: String
)

@Entity(tableName = "list_class")
data class ListClass(
    @PrimaryKey val id: Int,
    val name: String,
    val grade: Int,
    val majorId: Int,
    val majorName: String,
    val total_student: String,
    val total_student_screening: String,
    val total_student_remaining: String
)

@Keep
@JsonClass(generateAdapter = true)
data class ListStudentResponse(
    val data: List<ListStudentItem>
)

@Keep
@JsonClass(generateAdapter = true)
@Entity(tableName = "list_student_item")
data class ListStudentItem(
    @PrimaryKey val id: Int,
    val classId: Int = 0, // clasid tambahan untuk db
    val name: String,
    val majorName: String = "", // juruan tambahan untuk db
    @NullToEmptyString val nisn_nik: String = "",
    @NullToEmptyString val nisn: String = "",
    @NullToEmptyString val nis: String = "",
    val user_avatar_image: String,
    val already_screening: Boolean
)

// screning

@Keep
@JsonClass(generateAdapter = true)
data class ScreningResponse(
    val feel_indication: FeelIndication? = null,
    val history_of_illness: HistoryOfIllnes? = null
)


@Keep
@JsonClass(generateAdapter = true)
data class FeelIndication(
    val cold: String = "",
    val fever: String = "",
    val headache: String = "",
    @Json(name = "dry-cough") val dry_cough: String = "",
    @Json(name = "chest-pain") val chest_pain: String = "",
    @Json(name = "lost-sense") val lost_sense: String = "",
    @Json(name = "sore-throat") val sore_throat: String = "",
    @Json(name = "acute-fatigue") val acute_fatigue: String = "",
    @Json(name = "nose-disorder") val nose_disorder: String = "",
    @Json(name = "out-of-breath") val out_of_breath: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class HistoryOfIllnes(
    val feel: String = "",
    val nothing: String? = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class ScreeningCheckResponse(
    val message: String = "",
    val data: ScreeningCheckItem? = null
)

@Keep
@JsonClass(generateAdapter = true)
data class ScreeningCheckItem(
    val total_student: Int = 0,
    val total_data: Int = 0
)

@Entity(tableName = "choice_table")
data class ChoiceTable(
    @PrimaryKey val id: Int = 0,
    val mainKey: String, // way_of_travel,public_transportion_choice,feel_indication,history_of_illness
    val key: String,
    val text: String,
    val isSelected: Boolean = false
)

@Keep
@JsonClass(generateAdapter = true)
data class SaveScreeningResp(val indication_of_covid: Boolean = false)


//@Entity(tableName = "student_report_table")
//data class StudentReportTable (
//        @PrimaryKey val id :Int,
//        val date_input:String,
//        val time_input:String,
//        val emperature:String,
//        val feel_indication:List<String>,
//        val history_of_illnes:String,
//)

//@Entity(tableName = "user_report_table")
//data class UserReportTable (
//        @PrimaryKey val id :Int,
//        val date_input:String,
//        val time_input:String,
//        val way_of_travel:String,
//        val public_transportion_choice:List<String>,
//)