package id.onklas.app.pages.studentcard

import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.pages.login.SekolahItem
import id.onklas.app.pages.login.StudentItem
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.PreferenceClass
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class StudentCardVM @Inject constructor(
    val apiWrapper: ApiWrapper,
    val apiService: ApiService,
    val moshi: Moshi,
    val pref: PreferenceClass,
    val intentUtil: IntentUtil
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }
    val loading by lazy { MutableLiveData(true) }
    val template by lazy { MutableLiveData<TemplateData>() }
    val theme by lazy { MutableLiveData<ThemeData>() }
    val dateFormat by lazy { SimpleDateFormat("dd-MM-yyyy", Locale("id")) }
    val sendDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", Locale("id")) }

    private val studentAdapter by lazy { moshi.adapter(StudentItem::class.java) }
    var student = try {
        studentAdapter.fromJson(pref.getString("student")) ?: StudentItem()
    } catch (e: Exception) {
        StudentItem()
    }

    private val userAdapter by lazy { moshi.adapter(UserTable::class.java) }
    var user = try {
        userAdapter.fromJson(pref.getString("user")) ?: UserTable()
    } catch (e: Exception) {
        UserTable()
    }

    val school by lazy {
        try {
            moshi.adapter(SekolahItem::class.java).fromJson(pref.getString("school"))
        } catch (e: Exception) {
            SekolahItem()
        }
    }
    val schoolAddress by lazy { MutableLiveData<String>() }

    val editName by lazy { MutableLiveData(user.name) }
    val editNis by lazy { MutableLiveData(if (student.nis.isNullOrEmpty()) student.nisn else student.nis) }
    val editCity by lazy { MutableLiveData(student.place_of_birth) }
    val editDate by lazy { MutableLiveData(student.date_of_birth) }
    val sendDate by lazy {
        MutableLiveData(
            try {
                sendDateFormat.format(dateFormat.parse(student.date_of_birth))
            } catch (e: Exception) {
                ""
            }
        )
    }
    val editGender by lazy { MutableLiveData(student.gender) }
    val editAddress by lazy { MutableLiveData(student.user.address) }
    val editImage by lazy { MutableLiveData<String>() }

    init {
        viewModelScope.launch {
            try {
                val templateResp = apiService.idCardTemplate()
                template.postValue(templateResp.data)
                schoolAddress.postValue(templateResp.school.address)
                theme.postValue(themes[templateResp.data.theme])
            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
            } finally {
                loading.postValue(false)
            }

            editImage.postValue(student.user.user_avatar_image)
        }
    }

    suspend fun updateData() = try {
        when {
            editName.value.isNullOrEmpty() -> {
                errorString.postValue("Nama tidak boleh kosong")
                false
            }
            editDate.value.isNullOrEmpty() -> {
                errorString.postValue("Tanggal lahir tidak boleh kosong")
                false
            }
            editCity.value.isNullOrEmpty() -> {
                errorString.postValue("Kota lahir tidak boleh kosong")
                false
            }
            editGender.value.isNullOrEmpty() -> {
                errorString.postValue("Silahkan pilih jenis kelamin terlebih dahulu")
                false
            }
            editAddress.value.isNullOrEmpty() -> {
                errorString.postValue("Alamat tidak boleh kosong")
                false
            }
            else -> {
                val typePlain = "text/plain".toMediaTypeOrNull()
                val data = mapOf(
                    "name" to editName.value.orEmpty().toRequestBody(typePlain),
                    "date_of_birth" to sendDate.value.orEmpty().toRequestBody(typePlain),
                    "place_of_birth" to editCity.value.orEmpty().toRequestBody(typePlain),
                    "gender" to editGender.value.orEmpty().toRequestBody(typePlain),
                    "address" to editAddress.value.orEmpty().toRequestBody(typePlain)
                )

                val fileData = if (editImage.value == student.user.user_avatar_image) null else {
                    val file = File(editImage.value)
                    val fileType =
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
                            ?.toMediaTypeOrNull()

                    MultipartBody.Part.createFormData(
                        "file", file.name, file.asRequestBody(fileType)
                    )
                }

                apiService.updateCard(data, fileData)

                user.name = editName.value.orEmpty()
                pref.putString("user", userAdapter.toJson(user))

                student.name = editName.value.orEmpty()
                student.date_of_birth = editDate.value.orEmpty()
                student.place_of_birth = editCity.value.orEmpty()
                student.gender = editGender.value.orEmpty()
                student.user.address = editAddress.value.orEmpty()
                student.user.user_avatar_image = editImage.value.orEmpty()
                pref.putString("student", studentAdapter.toJson(student))

                true
            }
        }
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)

        false
    }
}