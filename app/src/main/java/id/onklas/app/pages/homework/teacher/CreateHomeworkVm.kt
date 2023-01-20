package id.onklas.app.pages.homework.teacher

import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.OnKlasDbUtil
import id.onklas.app.pages.homework.AssignmentDay
import id.onklas.app.pages.homework.AssignmentSchedule
import id.onklas.app.pages.homework.ClassRoomTable
import id.onklas.app.pages.homework.HomeworkLinkSource
import id.onklas.app.pages.login.SekolahItem
import id.onklas.app.pages.theory.MapelTable
import id.onklas.app.pages.theory.MapelTeacherCrossRef
import id.onklas.app.pages.theory.TeacherItem
import id.onklas.app.pages.theoryteacher.UploadMateriBindConverter
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.FileUtils
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

class CreateHomeworkVm @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val db: MemoryDB,
    val dbUtil: OnKlasDbUtil,
    val fileUtils: FileUtils,
    val intentUtil: IntentUtil,
    val apiWrapper: ApiWrapper,
    val apiService: ApiService
) : ViewModel() {

    val teacher by lazy {
        try {
            moshi.adapter(TeacherItem::class.java).fromJson(pref.getString("teacher"))
        } catch (e: Exception) {
            null
        }
    }
    val school: SekolahItem by lazy {
        try {
            moshi.adapter(SekolahItem::class.java).fromJson(pref.getString("school"))
                ?: SekolahItem()
        } catch (e: Exception) {
            SekolahItem()
        }
    }
    val errorString by lazy { MutableLiveData<String>() }
    val isLoading by lazy { MutableLiveData<Boolean>() }
    val urlApi by lazy { pref.getString("url_api") }
    val anyAdapter by lazy { moshi.adapter(Any::class.java) }
    val dateFormat by lazy { SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale("id")) }

    val listMapel by lazy { MutableLiveData<List<MapelTable>>() }
    val listClass by lazy { MutableLiveData<List<ClassRoomTable>>() }
    val listDay by lazy { MutableLiveData<List<AssignmentDay>>() }
    val listSchedule by lazy { MutableLiveData<List<AssignmentSchedule>>() }

    val mapel by lazy { MutableLiveData<Int>() }
    val classId by lazy { MutableLiveData<Int>() }
    val classDay by lazy { MutableLiveData<String>() }
    val scheduleId by lazy { MutableLiveData<Int>() }
    val expired by lazy { MutableLiveData<String>(dateFormat.format(Date(System.currentTimeMillis()))) }
    val name by lazy { MutableLiveData<String>() }
    val desc by lazy { MutableLiveData<String>() }
    val file by lazy { MutableLiveData<String>() }
    val uploadLink by lazy { MutableLiveData<String>() }
    val allowCreate by lazy { MutableLiveData<Boolean>(false) }
    val uploadFile by lazy { MutableLiveData<Boolean>(false) }
    val needDownload by lazy { MutableLiveData<Boolean>(false) }
    val needUpload by lazy { MutableLiveData<Boolean>(false) }

    val fileInfo by lazy { MutableLiveData<String>() }

    var homeworkId: Int = 0

    fun setHomework(id: Int) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)
                db.homework().getDetailHomework(id)?.let {
                    homeworkId = id
                    mapel.postValue(it.mapel?.id?.toInt())
                    classId.postValue(it.homework.classRoomId)
                    expired.postValue(it.homework.end_at_label)
                    name.postValue(it.homework.title)
                    desc.postValue(it.homework.description)
                    classDay.postValue(it.homework.scheduleDay)
                    scheduleId.postValue(it.homework.scheduleId)

                    val link =
                        it.links.firstOrNull { it.src == HomeworkLinkSource.SRC_TEACHER }?.link
                    uploadLink.postValue(link)
                    if (it.homework.file_name.isNotEmpty())
                        fileInfo.postValue("${it.homework.file_name} (ukuran: ${it.homework.file_size})")

                    uploadFile.postValue(link != null || it.homework.file_name.isNotEmpty())
                    needDownload.postValue(it.homework.downloded == 1)
                    needUpload.postValue(it.homework.uploaded == 1)
                }
            } catch (e: Exception) {
                errorString.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    suspend fun fetchClassRoom(): List<ClassRoomTable> = try {
        apiService.assignmentClass()
            .data
            .also { db.homework().insertClassRoom(it) }
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        emptyList()
    }

    private suspend fun fetchMapel(): List<MapelTable> = try {
        apiService.teacherSubjectTeach()
            .data.let {
                it.map {
                    db.theory().insertMapelTeacherCrossRef(
                        MapelTeacherCrossRef(
                            it.id.toLong(),
                            teacher?.id?.toLong() ?: 0
                        )
                    )
                    MapelTable(
                        it.id.toLong(),
                        it.name,
                        it.icon_image,
                        it.message_label,
                        it.teacher.id,
                        it.teacher.user.id
                    )
                }.also {
                    db.theory().insertMapel(it)
                }
            }
    } catch (e: Exception) {
        Timber.e(e)
        emptyList()
    }

    fun fetchSchedule() {
        viewModelScope.launch {
            try {
                val classIid = classId.value ?: return@launch
                val day = classDay.value ?: return@launch

                isLoading.postValue(true)
                val data = apiService.assignmentScheduleClass(classIid, day).data
                listSchedule.postValue(data)
                UploadMateriBindConverter.listSchedule = data

                if (scheduleId.value != null)
                    scheduleId.postValue(scheduleId.value)
            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    suspend fun create(): Boolean = try {
        val textMediaType = "text/plain".toMediaTypeOrNull()

        val dataPart = mapOf(
            "title" to name.value.orEmpty().toRequestBody(textMediaType),
            "description" to desc.value.orEmpty().toRequestBody(textMediaType),
            "teacher_id" to teacher?.id?.toString().orEmpty().toRequestBody(textMediaType),
            "school_id" to school.id.toString().toRequestBody(textMediaType),
            "school_subject_id" to mapel.value?.toString().orEmpty()
                .toRequestBody(textMediaType),
            "school_classes_id" to classId.value?.toString().orEmpty()
                .toRequestBody(textMediaType),
            "school_subject_schedules_id" to scheduleId.value.toString()
                .toRequestBody(textMediaType),
            "grade" to CreateHomeworkBindConverter.classId_to_grade(classId.value)?.toString()
                .orEmpty().toRequestBody(textMediaType),
            "end_at" to expired.value.orEmpty().toRequestBody(textMediaType),
            "checked" to "1".toRequestBody(textMediaType),
            "downloded" to (if (needDownload.value == true) "1" else "0").toRequestBody(
                textMediaType
            ),
            "uploaded" to (if (needUpload.value == true) "1" else "0").toRequestBody(
                textMediaType
            ),
            "link" to uploadLink.value.orEmpty().toRequestBody(textMediaType)
        )
        val filePart = file.value?.let {
            val file = File(it)
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody(
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
                        ?.toMediaTypeOrNull()
                )
            )
        }

        if (homeworkId == 0)
            apiService.createAssignment(dataPart, filePart)
        else
            apiService.updateAssignment(homeworkId, dataPart, filePart)

        true
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        false
    }

    init {
        viewModelScope.launch {
            val listClassRoom = db.homework().getListClassroom().let {
                (if (it.isEmpty()) fetchClassRoom() else it).sortedBy { it.name }
            }
            CreateHomeworkBindConverter.listClass.addAll(listClassRoom)
            listClass.postValue(listClassRoom)

            val listMapelUpdate = db.theory().getListMapel().let {
                (if (it.isEmpty()) fetchMapel() else it).sortedBy { it.name }
            }
            UploadMateriBindConverter.listSubject.addAll(listMapelUpdate)
            listMapel.postValue(listMapelUpdate)

            listDay.postValue(apiService.assignmentDay().data)
        }
    }
}