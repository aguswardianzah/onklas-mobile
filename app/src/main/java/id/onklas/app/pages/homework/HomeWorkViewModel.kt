package id.onklas.app.pages.homework

import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.OnKlasDbUtil
import id.onklas.app.pages.homework.teacher.CreateHomeworkBindConverter
import id.onklas.app.pages.theory.MapelTable
import id.onklas.app.pages.theory.MapelTeacherCrossRef
import id.onklas.app.pages.theory.TeacherItem
import id.onklas.app.pages.theoryteacher.UploadMateriBindConverter
import id.onklas.app.utils.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class HomeWorkViewModel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val db: MemoryDB,
    val dbUtil: OnKlasDbUtil,
    val fileUtils: FileUtils,
    val intentUtil: IntentUtil,
    val apiService: ApiService,
    val apiWrapper: ApiWrapper
) : ViewModel() {

    private val pageSize = 10
    val errorString by lazy { MutableLiveData<String>() }
    val teacher by lazy {
        try {
            moshi.adapter(TeacherItem::class.java).fromJson(pref.getString("teacher"))
        } catch (e: Exception) {
            null
        }
    }

    val filterTime by lazy { MutableLiveData<String>("Semua") }
    val filterMapel by lazy { MutableLiveData<ArrayList<MapelTable>>(ArrayList()) }
    val classId by lazy { MutableLiveData<Int>() }
    val mapel by lazy { MutableLiveData<Int>() }
    val uploadLink by lazy { MutableLiveData<String>() }

    val listMapel by lazy { MutableLiveData<List<MapelTable>>() }
    val listClass by lazy { MutableLiveData<List<ClassRoomTable>>() }
    fun initMapelAndClass() {
        viewModelScope.launch {
            val listClassRoom = mutableListOf(ClassRoomTable(name = "Semua")).apply {
                addAll(
                    db.homework().getListClassroom().let {
                        (if (it.isEmpty()) fetchClassRoom() else it).sortedBy { it.name }
                    }
                )
            }
            CreateHomeworkBindConverter.listClass.addAll(listClassRoom)
            listClass.postValue(listClassRoom)

            val listMapelUpdate = mutableListOf(MapelTable(name = "Semua")).apply {
                addAll(
                    db.theory().getListMapel().let {
                        (if (it.isEmpty()) fetchMapel() else it).sortedBy { it.name }
                    }
                )
            }
            UploadMateriBindConverter.listSubject.addAll(listMapelUpdate)
            listMapel.postValue(listMapelUpdate)
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

    val listBelum by lazy {
        db.homework().getHomeworkBelum().toLiveData(
            pageSize, boundaryCallback = PagedListBoundaryCallback(
                { viewModelScope.launch { fetchHomeworkTodo() } },
                {
                    viewModelScope.launch {
                        val count = db.homework().countHomeworkBelum()
                        if (count >= pageSize && hasNextBelum) fetchHomeworkTodo(count)
                    }
                }
            )
        )
    }

    private var hasNextBelum = true
    var prefBelum = -1
    suspend fun fetchHomeworkTodo(start: Int = 0) {
        if (prefBelum == start)
            return

        prefBelum = start
        try {
            val response = apiService.studentTaskTodo(pageSize, start)
            hasNextBelum = dbUtil.processHomeworkRespones(response)
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.localizedMessage)
        }
    }

    val listSudah by lazy {
        db.homework().getHomeworkSudah().toLiveData(
            pageSize, boundaryCallback = PagedListBoundaryCallback(
                { viewModelScope.launch { fetchHomeworkSudah() } },
                {
                    viewModelScope.launch {
                        val count = db.homework().countHomeworkSudah()
                        if (count >= pageSize && hasNextSudah) fetchHomeworkSudah(count)
                    }
                }
            )
        )
    }

    private var hasNextSudah = true
    var prefSudah = -1
    suspend fun fetchHomeworkSudah(start: Int = 0) {
        if (prefSudah == start)
            return

        prefSudah = start
        try {
            val response = apiService.studentTaskDone(pageSize, start)
            hasNextSudah = dbUtil.processHomeworkRespones(response, 1)
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.localizedMessage)
        }
    }

    val listNilai by lazy {
        db.homework().getHomeworkNilai().toLiveData(
            pageSize, boundaryCallback = PagedListBoundaryCallback(
                { viewModelScope.launch { fetchHomeworkNilai() } },
                {
                    viewModelScope.launch {
                        val count = db.homework().countHomeworkNilai()
                        if (count >= pageSize && hasNextNilai) fetchHomeworkNilai(count)
                    }
                }
            )
        )
    }

    private var hasNextNilai = true
    var prefNilai = -1
    suspend fun fetchHomeworkNilai(start: Int = 0) {
        if (prefNilai == start)
            return

        prefNilai = start
        try {
            val response = apiService.studentTaskScored(pageSize, start)
            hasNextNilai = dbUtil.processHomeworkRespones(response, 2)
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.localizedMessage)
        }
    }

    fun listTeacher() = (
            if ((mapel.value == null || mapel.value == 0) && (classId.value == null || classId.value == 0))
                db.homework().getHomeworkTeacher(teacher?.id ?: 0)
            else if (mapel.value != null && (classId.value == null || classId.value == 0))
                db.homework().getHomeworkTeacher(teacher?.id ?: 0, mapel.value ?: 0)
            else if ((mapel.value == null || mapel.value == 0) && classId.value != null)
                db.homework().getHomeworkTeacherClass(teacher?.id ?: 0, classId.value ?: 0)
            else
                db.homework().getHomeworkTeacherSubjectClass(
                    teacher?.id ?: 0,
                    mapel.value ?: 0,
                    classId.value ?: 0
                )
            ).toLiveData(
            pageSize, boundaryCallback = PagedListBoundaryCallback(
                { viewModelScope.launch { fetchHomeworkTeacher() } },
                {
                    viewModelScope.launch {
                        val count = db.homework().countHomeworkTeacher(teacher?.id ?: 0)
                        if (count >= pageSize && hasNextTeacher) fetchHomeworkTeacher(count)
                    }
                }
            )
        )

    private var hasNextTeacher = true
    var prefTeacher = -1
    suspend fun fetchHomeworkTeacher(start: Int = 0) {
        if (prefTeacher == start)
            return

        prefTeacher = start
        try {
            val response = apiService.teacherAssignment(
                mutableMapOf(
                    "take" to pageSize,
                    "skip" to start,
                    "filter[0][0]" to "teacher_id",
                    "filter[0][1]" to "=",
                    "filter[0][2]" to teacher?.id
                ).apply {
                    classId.value?.let {
                        putAll(
                            mapOf(
                                "filter[1]" to "and",
                                "filter[2][0]" to "school_classes_id",
                                "filter[2][1]" to "=",
                                "filter[2][2]" to it
                            )
                        )
                    }

                    mapel.value?.let {
                        putAll(
                            mapOf(
                                "filter[3]" to "and",
                                "filter[4][0]" to "school_subject_id",
                                "filter[4][1]" to "=",
                                "filter[4][2]" to it
                            )
                        )
                    }
                }
            )
            hasNextTeacher = dbUtil.processHomeworkRespones(response)
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.localizedMessage)
        }
    }

    val listDikumpulkan by lazy {
        db.homework().getHomeworkCollected().toLiveData(
            pageSize, boundaryCallback = PagedListBoundaryCallback(
                { viewModelScope.launch { fetchDikumpulkan() } },
                {
                    viewModelScope.launch {
                        val count = db.homework().countHomeworkCollected()
                        if (count >= pageSize && hasNextDikumpulkan) fetchDikumpulkan(count)
                    }
                }
            )
        )
    }

    private var hasNextDikumpulkan = true
    var prefDikumpulkan = -1
    suspend fun fetchDikumpulkan(start: Int = 0) {
        if (prefDikumpulkan == start)
            return

        prefDikumpulkan = start
        try {
            val response = apiService.assignmentCollected(pageSize, start)
            db.homework().insertHomeworkCollected(response.data)
            hasNextDikumpulkan = true
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.localizedMessage)
        }
    }

    suspend fun getAssigmentDetail(id: Int): AssignmentResponse? = try {
        apiService.assignmentDetail(id)
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        null
    }

    suspend fun setAssignmentScore(colledtedId: Int, assignmentId: Int, score: Int): Boolean = try {
        apiWrapper.scoreAssignment(colledtedId, assignmentId, score)
        true
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        false
    }

    suspend fun getDetailHomework(id: Int): HomeworkItemTable? = try {
        db.homework().getDetailHomework(id)
    } catch (e: Exception) {
        Timber.e(e)
        null
    }

    suspend fun uploadHomework(id: Int): Boolean = try {
        val textMediaType = "text/plain".toMediaTypeOrNull()
        apiService.answerHomework(
            id,
            mapOf(
                "checked" to "1".toRequestBody(textMediaType),
                "uploaded" to (if (tugasFile.value.isNullOrEmpty() && uploadLink.value.isNullOrEmpty()) "0" else "1")
                    .toRequestBody(textMediaType),
                "link" to uploadLink.value.orEmpty().toRequestBody(textMediaType)
            ),
            tugasFile.value?.let {
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
        )

        db.homework().getDetailHomework(id)?.let {
            db.homework().deleteHomework(it.homework)
        }
        true
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        false
    }

    val tugasFile by lazy { MutableLiveData<String>() }

    suspend fun deleteHomework(id: Int) = try {
        apiService.deleteAssignment(id)
        db.homework().deleteHomework(id)
        true
    } catch (e: Exception) {
        errorString.postValue(e.message)
        false
    }
}