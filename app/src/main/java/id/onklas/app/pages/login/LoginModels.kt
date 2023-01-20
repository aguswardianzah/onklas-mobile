package id.onklas.app.pages.login

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString
import id.onklas.app.pages.homework.ClassRoomTable
import id.onklas.app.pages.sekolah.sosmed.FeedUser
import id.onklas.app.pages.theory.TeacherItem

@Keep
@JsonClass(generateAdapter = true)
data class SekolahResponse(val data: List<SekolahItem> = emptyList())

@Keep
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "school",
    indices = [Index(name = "school_rowidx", value = ["id"], unique = true)]
)
data class SekolahItem(
    @PrimaryKey val id: Int = 0,
    val uuid: String = "",
    val name: String = "",
    val image: String = "",
    val address: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class PembelajaranSekolahItem(
    val row_id: Int = 0,
    val uuid: String = "",
    val id: String = "",
    val name: String = "",
    val image: String = ""
)


@Keep
@JsonClass(generateAdapter = true)
data class KepalaSekolah(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val nis_nik: String? = "",
    val phone: String = "",
    val has_sales_id: Boolean = false
)

@Keep
@JsonClass(generateAdapter = true)
data class UserResponse(
    val data: UserResponseData = UserResponseData(),
    val errors: UserResponseError? = null,
    val error: String = "",
    val rule_label: String = "",
    val rule: Role = Role(),
    val is_klaspay_activated: Boolean = false,
    val is_email_verified: Boolean = false,
    val is_verified: Boolean = false
)

@Keep
@JsonClass(generateAdapter = true)
data class Role(
    val is_student: Boolean = false,
    val is_teacher: Boolean = false,
    val is_librarian: Boolean = false
)

@Keep
@JsonClass(generateAdapter = true)
data class UserResponseData(
    val id: Int = 0,
    val uuid: String = "",
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val email: String = "",
    @NullToEmptyString val nisn_nik: String = "",
    @NullToEmptyString val nis_nik: String = "",
    @NullToEmptyString val phone: String = "",
    @NullToEmptyString val place_of_birth: String = "",
    @NullToEmptyString val date_of_birth: String = "",
    @NullToEmptyString val gender: String = "",
    @NullToEmptyString var user_avatar_image: String = "",
    @NullToEmptyString val religion: String = "",
    @NullToEmptyString val blood_type: String = "",
    @NullToEmptyString var address: String = "",
    @NullToEmptyString var sub_district_id: String = "",
    @NullToEmptyString var sub_district: String = "",
    @NullToEmptyString var city_id: String = "",
    @NullToEmptyString var city: String = "",
    @NullToEmptyString var province_id: String = "",
    @NullToEmptyString var province: String = "",
    @NullToEmptyString val user_username: String = "",
    val student: StudentItem? = null,
    val teacher: TeacherItem? = null,
    val school: SekolahItem = SekolahItem(),
    val roles: List<UserRole> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class TeacherUser(
    val id: Int = 0,
    val name: String = "",
    val nip: String = "",
    val address: String = "",
    val user: TeacherUserItem = TeacherUserItem()
)

@Keep
@JsonClass(generateAdapter = true)
data class TeacherUserItem(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val nis_nik: String = "",
    val phone: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class UserResponseError(val credential: List<String> = emptyList())

@Keep
@JsonClass(generateAdapter = true)
data class StudentItem(
    val id: Int = 0,
    val nisn: String = "",
    val nis: String = "",
    var name: String = "",
    @NullToEmptyString var place_of_birth: String = "",
    @NullToEmptyString var date_of_birth: String = "",
    @NullToEmptyString var gender: String = "",
    @NullToEmptyString val religion: String = "",
    @NullToEmptyString val blood_type: String = "",
    @NullToEmptyString val height: String = "",
    @NullToEmptyString val weight: String = "",
    @NullToEmptyString val uniform_size: String = "",
    @NullToEmptyString val sport_shirt_size: String = "",
    @NullToEmptyString val previous_school: String = "",
    @NullToEmptyString val previous_class: String = "",
    @NullToEmptyString val class_of: String = "",
    @NullToEmptyString val `class`: String = "",
    @NullToEmptyString val grade_class: String = "",
    val student_class: StudentClass = StudentClass(),
    val user: UserResponseData = UserResponseData()
)

@Keep
@JsonClass(generateAdapter = true)
data class StudentClass(
    val id: Int = 0,
    val class_room: ClassRoomTable = ClassRoomTable(),
    val name:String = "",
    val grade : Int = 0,
)


@Keep
@JsonClass(generateAdapter = true)
data class UserRole(
    val id: Int = 0,
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val guard_name: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class LoginResponse(
    val data: UserResponseData = UserResponseData(),
    val meta: MetaItem = MetaItem(),
    val credential: LoginCredential = LoginCredential(),
    val error: String = "",
    val is_verified: Boolean = false,
    val rule_label: String = "",
    val rule: Role = Role(),
    val is_klaspay_activated: Boolean = false,
    val is_email_verified: Boolean = false,
    val product_school: SchoolProduct = SchoolProduct()
)

@Keep
@JsonClass(generateAdapter = true)
data class SchoolProduct(
    val onklas_lite: Boolean = true,
    val onklas_pro: Boolean = false,
    val klastime: Boolean? = false
)

@Keep
@JsonClass(generateAdapter = true)
data class Product(
    val data: UserResponseData = UserResponseData(),
    val meta: MetaItem = MetaItem(),
)

@Keep
@JsonClass(generateAdapter = true)
data class LoginRespData(
    val id: Int = 0,
    @NullToEmptyString val uuid: String = "",
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val email: String = "",
    @NullToEmptyString val nisn_nik: String = "",
    @NullToEmptyString val phone: String = "",
    val school: SekolahItem = SekolahItem(),
    val role: List<UserRole> = emptyList(),
    val student: StudentItem? = null,
    val teacher: TeacherItem? = null,
    @NullToEmptyString val parent: String = "",
    @NullToEmptyString val user_avatar_image: String = "",
    @NullToEmptyString val user_username: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class MetaItem(val token: String = "")

@Keep
@JsonClass(generateAdapter = true)
data class LoginCredential(val using_default_password: String = "Y")

@Keep
@JsonClass(generateAdapter = true)
data class SessionResponse(val data: List<SessionData> = emptyList())

@Keep
@JsonClass(generateAdapter = true)
@Entity(tableName = "sessions", indices = [Index("id", unique = true)])
data class SessionData(
    @PrimaryKey val id: Int = 0,
    @NullToEmptyString val name: String = "",
    @NullToEmptyString val last_used_at: String = ""
)
