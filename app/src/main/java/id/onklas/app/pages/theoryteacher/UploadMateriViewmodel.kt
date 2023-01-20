package id.onklas.app.pages.theoryteacher

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.homework.ClassRoomTable
import id.onklas.app.pages.homework.teacher.CreateHomeworkBindConverter
import id.onklas.app.pages.theory.*
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
import javax.inject.Inject

class UploadMateriViewmodel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val db: MemoryDB,
    val fileUtils: FileUtils,
    val intentUtil: IntentUtil,
    val apiService: ApiService,
    val apiWrapper: ApiWrapper
) : ViewModel() {
    val errorString by lazy { MutableLiveData<String>() }
    val allowUpload by lazy { MutableLiveData<Boolean>(false) }
    val materiTitle by lazy { MutableLiveData<String>("") }
    val materiDesc by lazy { MutableLiveData<String>("") }
    val materiLink by lazy { MutableLiveData<String>("") }
    val subjectId by lazy { MutableLiveData<Int>() }
    val grade by lazy { MutableLiveData<Int>() }
    val classId by lazy { MutableLiveData<Int>() }
    val majorId by lazy { MutableLiveData<Int>() }
    val materiFile by lazy { MutableLiveData<String>() }

    val fileInfo by lazy { MutableLiveData<String>() }

    val showSelected by lazy { MutableLiveData("grade") }
    var linkAttached = false

    val listSubject by lazy { MutableLiveData<List<MapelTable>>() }
    val listGrade by lazy { MutableLiveData<List<GradeTable>>() }
    val listClass by lazy { MutableLiveData<List<ClassRoomTable>>() }
    val listMajor by lazy { MutableLiveData<List<MajorItem>>() }

    val teacher by lazy {
        try {
            moshi.adapter(TeacherItem::class.java).fromJson(pref.getString("teacher"))
        } catch (e: Exception) {
            null
        }
    }

    private var materiId = 0
    val isLoading by lazy { MutableLiveData<Boolean>() }

    fun setMateri(id: Int) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)
                materiId = id
                db.theory().getDetailMateri(id)?.let {
                    materiTitle.postValue(it.materi.name)
                    materiDesc.postValue(it.materi.description)
                    materiLink.postValue(it.link.firstOrNull()?.link)
                    subjectId.postValue(it.materi.subject_id)
                    grade.postValue(it.materi.grade)
                    classId.postValue(it.materi.class_id)
                    majorId.postValue(it.materi.major_id)
                    fileInfo.postValue("${it.materi.file_name} (ukuran: ${it.materi.file_size})")
                    showSelected.postValue(
                        when {
                            it.materi.major_id > 0 -> "major"
                            it.materi.class_id > 0 -> "class"
                            else -> "grade"
                        }
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    suspend fun fetchClassRoom(): List<ClassRoomTable> = try {
        apiService.teacherClassRoom()
            .data
            .also { db.homework().insertClassRoom(it) }
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        emptyList()
    }

    suspend fun fetchMajor(): List<MajorItem> = try {
        apiService.teacherMajor()
            .data
            .also { db.theory().insertMajors(it) }
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        emptyList()
    }

    private suspend fun fetchGrade(): List<GradeTable> = try {
        apiService.gradeTeach().data.also { db.theory().insertGrade(it) }
    } catch (e: Exception) {
        Timber.e(e)
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

    suspend fun createMateri(): Boolean = try {
        val file = materiFile.value?.let { File(Uri.parse(it)?.path.orEmpty()) }

        if (file?.length() ?: 0 > 1024 * 1024 * 12) {
            errorString.postValue("ukuran file melebihi batas maksimal (12Mb)")
            false
        } else {
            val textType = "text/plain".toMediaTypeOrNull()
            val dataPart = mutableMapOf(
                "name" to materiTitle.value.orEmpty().toRequestBody(textType),
                "description" to materiDesc.value.orEmpty().toRequestBody(textType),
                "link" to materiLink.value.orEmpty().toRequestBody(textType),
                "school_subject_id" to (subjectId.value ?: 0).toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
            ).apply {
                when (showSelected.value) {
                    "class" -> put(
                        "school_classes_id",
                        classId.value.toString().toRequestBody(textType)
                    )
                    "major" -> put(
                        "school_major_id",
                        majorId.value.toString().toRequestBody(textType)
                    )
                    "grade" -> put("grade", grade.value.toString().toRequestBody(textType))
                }
            }
            val filePart = file?.let {
                MultipartBody.Part.createFormData(
                    "file",
                    it.name,
                    it.asRequestBody(
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension)
                            ?.toMediaTypeOrNull()
                    )
                )
            }

            if (materiId == 0)
                apiService.createTheory(dataPart, filePart)
            else apiService.updateTheory(materiId, dataPart, filePart)

            true
        }
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage ?: "Gagal membuat materi")
        false
    }

    init {
        viewModelScope.launch {
            UploadMateriBindConverter.listSubject.clear()
            UploadMateriBindConverter.listSubject.addAll(
//                db.theory().getListTeacherMapel(teacher?.id?.toInt() ?: 0).let {
//                    if (it.isEmpty())
                fetchMapel().sortedBy { it.name }
//                    else it.map { it.mapel }
//                }
            )
            listSubject.postValue(UploadMateriBindConverter.listSubject)
            if (subjectId.value != null)
                subjectId.postValue(subjectId.value)

            UploadMateriBindConverter.listGrade.clear()
            UploadMateriBindConverter.listGrade.addAll(db.theory().getListGrade().let {
                (if (it.isEmpty()) fetchGrade()
                else it).sortedBy { it.name }
            })
            listGrade.postValue(UploadMateriBindConverter.listGrade)
            if (grade.value != null) {
                grade.postValue(grade.value)
            }

            val listClassRoom = db.homework().getListClassroom().let {
                (if (it.isEmpty()) fetchClassRoom() else it).sortedBy { it.name }
            }
            UploadMateriBindConverter.listClass.clear()
            UploadMateriBindConverter.listClass.addAll(listClassRoom)
            listClass.postValue(listClassRoom)
            if (classId.value != null)
                classId.postValue(classId.value)

            UploadMateriBindConverter.listMajor.clear()
            UploadMateriBindConverter.listMajor.addAll(db.theory().getListMajor().let {
                (if (it.isEmpty()) fetchMajor() else it).sortedBy { it.name }
            })
            listMajor.postValue(UploadMateriBindConverter.listMajor)
            if (majorId.value != null)
                majorId.postValue(majorId.value)
        }
    }
}