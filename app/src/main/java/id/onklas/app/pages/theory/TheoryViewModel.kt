package id.onklas.app.pages.theory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.OnKlasDbUtil
import id.onklas.app.pages.homework.ClassRoomTable
import id.onklas.app.utils.FileUtils
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class TheoryViewModel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val db: MemoryDB,
    val dbUtil: OnKlasDbUtil,
    val fileUtils: FileUtils,
    val intentUtil: IntentUtil,
    val apiService: ApiService
) : ViewModel() {

    val teacher by lazy {
        try {
            moshi.adapter(TeacherItem::class.java).fromJson(pref.getString("teacher"))
        } catch (e: Exception) {
            null
        }
    }
    val teacherId by lazy { teacher?.id ?: 0 }
    val isTeacher by lazy { pref.getBoolean("is_teacher") }

    val errorString by lazy { MutableLiveData<String>() }

    val listMapel by lazy {
        db.theory().getMapel().toLiveData(
            pageSize,
            boundaryCallback = PagedListBoundaryCallback(
                { viewModelScope.launch { fetchMapel() } },
                {
                    viewModelScope.launch {
                        val count = db.theory().countMapel()
                        if (hasNextMapel && count >= pageSize) fetchMapel(count)
                    }
                })
        )
    }

    fun listMateri(id: Int) =
        db.theory().getMateri(id).toLiveData(
            pageSize,
            boundaryCallback = PagedListBoundaryCallback(
                { viewModelScope.launch { fetchMateriBySubject(id) } },
                {
                    viewModelScope.launch {
                        val count = db.theory().countMateri(id)
                        if (hasNextMateri && count >= pageSize) fetchMateriBySubject(id, count)
                    }
                }
            )
        )

    val listTeacherMateri by lazy {
        db.theory().getTeachersMateri(teacherId)
            .toLiveData(pageSize, boundaryCallback = PagedListBoundaryCallback(
                { viewModelScope.launch { fetchMateriTeacher() } },
                {
                    viewModelScope.launch {
                        val count = db.theory().countTeachersMateri(teacherId)
                        if (hasNextTeacherMateri && count >= pageSize) fetchMateriTeacher(count)
                    }
                }
            ))
    }

    suspend fun listTeacherMateri(
        subjectId: Int? = null,
        classId: Int? = null
    ): LiveData<PagedList<MateriMapelTeacher>> {
        val (datasource, count) = if (subjectId != null && classId != null)
            db.theory()
                .getTeachersMateriSubjectClass(teacherId, subjectId, classId) to db.theory()
                .countTeachersMateriSubjectClass(teacherId, subjectId, classId)
        else if (subjectId != null && classId == null)
            db.theory().getTeachersMateriSubject(teacherId, subjectId) to db.theory()
                .countTeachersMateriSubject(teacherId, subjectId)
        else if (subjectId == null && classId != null)
            db.theory().getTeachersMateriClass(teacherId, classId) to db.theory()
                .countTeachersMateriClass(teacherId, classId)
        else db.theory().getTeachersMateri(teacherId) to db.theory()
            .countTeachersMateri(teacherId)

        return datasource.toLiveData(pageSize, boundaryCallback = PagedListBoundaryCallback(
            {
                viewModelScope.launch {
                    fetchMateriTeacher(subjectId = subjectId, classId = classId)
                }
            }, {
                viewModelScope.launch {
                    if (hasNextTeacherMateri && count >= pageSize)
                        fetchMateriTeacher(count, subjectId, classId)
                }
            })
        )
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

    suspend fun fetchMapel2(): List<MapelTable> = try {
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

    fun detailMateri(id: Int, subjectId: Int): LiveData<MateriWithLink> {
        val res = MutableLiveData<MateriWithLink>()
        viewModelScope.launch {
            res.postValue(db.theory().getDetailMateri(id) ?: fetchDetailMateri(subjectId, id))
        }
        return res
    }

    private val pageSize = 20
    private var prevStart = -1
    private var hasNextMapel = true
    suspend fun fetchMapel(start: Int = 0) {
        if (prevStart == start)
            return

        prevStart = start
        try {
            val mapelResponse = if (isTeacher) apiService.teacherSubject(
                pageSize,
                start
            ) else apiService.studentSubject(pageSize, start)
            hasNextMapel = dbUtil.processMapelResponse(mapelResponse)
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.localizedMessage)
        }
    }

    private var prevStartMateri = -1
    private var hasNextMateri = true
    suspend fun fetchMateriBySubject(subjectId: Int, start: Int = 0) {
        if (prevStartMateri == start)
            return

        prevStartMateri = start
        try {
            val materiResponse = if (isTeacher)
                apiService.teacherTheoryBySubject(subjectId, pageSize, start)
            else
                apiService.studentTheoryBySubject(subjectId, pageSize, start)

            if (start == 0)
                db.theory().deleteMateriBySubject(subjectId)

            hasNextMateri = dbUtil.processMateriResponse(materiResponse, subjectId)
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.localizedMessage)
        }
    }

    var prevStartTeacherMateri = -1
    var hasNextTeacherMateri = true
    suspend fun fetchMateriTeacher(start: Int = 0, subjectId: Int? = null, classId: Int? = null) {
        if (prevStartTeacherMateri == start)
            return

        prevStartTeacherMateri = start
        try {
            val materiResponse = apiService.teacherTheory(pageSize, start, subjectId, classId)
            if (start == 0)
                db.theory().clearMateri()

            hasNextMateri = dbUtil.processMateriResponse(materiResponse)
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.localizedMessage)
        }
    }

    var prefIds = ""
    suspend fun fetchDetailMateri(subjectId: Int, theoryId: Int): MateriWithLink? = try {
        if (prefIds == "${subjectId}_${theoryId}")
            null
        else {
            (if (isTeacher)
                apiService.teacherTheoryDetail(subjectId, theoryId)
            else
                apiService.studentTheoryDetail(subjectId, theoryId)
                    ).data
                .let {
                    val link = if (it.uri != null) it.uri.link.map { link ->
                        MateriLinkTable(
                            0,
                            it.id.toLong(),
                            link
                        )
                    }.also { db.theory().insertMateriLink(it) } else emptyList()
                    val materi =
                        MateriTable.fromMateriItem(it).also { db.theory().insertMateri(it) }
                    MateriWithLink(materi, link)
                }
        }
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        null
    }

    suspend fun deleteMateri(id: Long) = try {
        apiService.deleteTheory(id)
        true
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        false
    }
}