package id.onklas.app.pages.login

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.theory.TeacherItem
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    val context: Context,
    val moshi: Moshi,
    val pref: PreferenceClass,
    val intentUtil: IntentUtil,
    val db: MemoryDB,
    private val apiWrapper: ApiWrapper,
    private val apiService: ApiService
) : ViewModel() {

    val nisn by lazy { MutableLiveData<String>("") }
    val student by lazy { MutableLiveData<StudentItem>() }
    val teacher by lazy { MutableLiveData<TeacherItem>() }
    val school by lazy { MutableLiveData<SekolahItem>() }
    val user by lazy { MutableLiveData<UserResponseData>() }
    val password by lazy { MutableLiveData<String>("") }
    val allowProcesslogin by lazy { MutableLiveData<Boolean>(false) }
    val allowLogin by lazy { MutableLiveData<Boolean>(false) }
    val role_label by lazy { MutableLiveData<String>() }
    var uuid: String = ""

    private val studentAdapter by lazy { moshi.adapter(StudentItem::class.java) }
    private val teacherAdapter by lazy { moshi.adapter(TeacherItem::class.java) }

    private val pageSize = 20
    fun listSekolah(search: String = "") = db.login().getListSchool("%$search%")
        .toLiveData(
            pageSize, boundaryCallback = PagedListBoundaryCallback(
                { viewModelScope.launch { fetchSchool(0, search) } },
                {
                    viewModelScope.launch {
                        val count = db.login().countSchool("%$search%")
                        Timber.e("count school: $count -- hasNext = $hasNextSchool")
//                        if (count >= pageSize && hasNextSchool)
                        if (hasNextSchool)
                            fetchSchool(count, search)
                    }
                }
            )
        )

    var lastSchool = -1
    var hasNextSchool = true
    private suspend fun fetchSchool(start: Int = 0, search: String = "") {
        if (start == lastSchool)
            return

        lastSchool = start
        hasNextSchool = try {
            apiService.listSekolah(pageSize, lastSchool, search)
                .run {
                    db.login().insertSchool(data)
                    data.size >= pageSize
                }
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    suspend fun getUserData(): Pair<Boolean, String> = try {
        val userResponse = apiWrapper.checkAccount(nisn.value, school.value?.uuid)

        user.postValue(userResponse.data)
        role_label.postValue(userResponse.rule_label)

        if (userResponse.rule.is_student) {
            student.postValue(userResponse.data.student)
        } else if (userResponse.rule.is_teacher) {
            teacher.postValue(userResponse.data.teacher)
        }

        uuid = userResponse.data.uuid
        pref.putInt("user_id", userResponse.data.id)
        true to ""
    } catch (e: Exception) {
        Timber.e(e)
        false to e.message.toString()
    }

    suspend fun login(): Pair<Boolean, String> = try {
        val loginResponse = apiWrapper.login(uuid, password.value, school.value?.id ?: 0)
//        pref.putString("password", password.value.orEmpty())

        pref.putInt("user_id", loginResponse.data.id)
        Firebase.analytics.setUserId(loginResponse.data.id.toString())
        pref.putString("user_uuid", uuid)

        if (loginResponse.rule.is_student) {
            pref.putBoolean("is_student", true)
            pref.putString("student", studentAdapter.toJson(loginResponse.data.student))
            pref.putInt("class_id", loginResponse.data.student?.student_class?.class_room?.id ?: 0)
        } else if (loginResponse.rule.is_teacher) {
            pref.putBoolean("is_teacher", true)
            pref.putString("teacher", teacherAdapter.toJson(loginResponse.data.teacher))
        }

        pref.putString("roles", loginResponse.rule_label)

        pref.putString(
            "school",
            moshi.adapter(SekolahItem::class.java).toJson(loginResponse.data.school)
        )
        pref.putString("school_uuid", school.value?.uuid.orEmpty())
        pref.putString("user_token", loginResponse.meta.token)
        pref.putBoolean("logged_in", true)
        pref.putBoolean(
            "default_pass",
            loginResponse.credential.using_default_password == "Y"
        )

        pref.putBoolean("is_verified", loginResponse.is_verified)
        pref.putBoolean("is_email_verified", loginResponse.is_email_verified)
        pref.putBoolean("klaspayActive", loginResponse.is_klaspay_activated)

        pref.putBoolean("onklas_lite", loginResponse.product_school.onklas_lite)
        pref.putBoolean("onklas_pro", loginResponse.product_school.onklas_pro)
        pref.putBoolean("klastime", loginResponse.product_school.klastime ?: false)

        apiWrapper.updateFcm()
        true to ""
    } catch (e: Exception) {
        Timber.e(e)
        false to e.message.toString()
    }
}