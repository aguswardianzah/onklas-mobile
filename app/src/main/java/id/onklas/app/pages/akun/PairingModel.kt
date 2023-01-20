package id.onklas.app.pages.akun

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import id.onklas.app.pages.login.StudentItem
import id.onklas.app.pages.login.UserResponseData


@Keep
@JsonClass(generateAdapter = true)
data class acceptPairingResponse(
    val data: pairingItem ? = null
)
@Keep
@JsonClass(generateAdapter = true)
data class listPairingResponse(
    val data: List<pairingItem> = emptyList()
)
@Keep
@JsonClass(generateAdapter = true)
data class pairingItem(
    val id: Int = 0,
    val status : String = "",
    val user: UserResponseData? = null,
    val student: StudentItem? = null,
    val student_uuid : String= "",
    val otp : String = ""
)

@Keep
@JsonClass(generateAdapter = true)
@Entity(tableName = "pairingTable")
data class PairingTable(
    @PrimaryKey val id : Int = 0,
    val status : String = "",
    val id_student:Int = 0,
    val nisn: String = "",
    val nis: String = "",
    val name:String = "",
    val student_class_name:String = "",
    val student_class_id:Int = 0,
    val student_class_grade:Int = 0,
    val uuid : String ="",
    val user_username:String,
    val email:String,
    val nis_nik:String,
    val user_avatar_image:String,
    val otp :String,
){
    companion object{
        fun fromPairingitem(item: pairingItem) = PairingTable(
            item.id,
            item.status,
            item.student!!.id,
            item.student.nisn,
            item.student.nis,
            item.student.name,
            item.student.student_class.name,
            item.student.student_class.id,
            item.student.student_class.grade,
            item.student.user.uuid,
            item.student.user.user_username,
            item.student.user.email,
            item.student.user.nis_nik,
            item.student.user.user_avatar_image,
            item.otp
        )
    }
}


