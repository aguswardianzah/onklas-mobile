package id.onklas.app.pages.theory

import androidx.paging.DataSource
import androidx.room.*
import id.onklas.app.pages.homework.ClassRoomTable

@Dao
interface TheoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapel(mapel: MapelTable): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapel(mapel: List<MapelTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapelTeacherCrossRef(data: MapelTeacherCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMajors(data: List<MajorItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMateri(materi: MateriTable): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMateriLink(materi: List<MateriLinkTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherTable): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(data: List<GradeTable>)

    @Transaction
    @Query("select * from mapel order by name")
    fun getMapel(): DataSource.Factory<Int, MapelTeacher>

    @Transaction
    @Query("select * from mapel_teacher where teacherId = :teacherId order by mapelId")
    fun getTeacherMapel(teacherId: Int): DataSource.Factory<Int, MapelTeacherCrossRef>

    @Transaction
    @Query("select * from mapel_teacher where teacherId = :teacherId order by mapelId")
    suspend fun getListTeacherMapel(teacherId: Int): List<MapelWithTeacher>

    @Query("select * from mapel order by name")
    suspend fun getListMapel(): List<MapelTable>

    @Query("select count(*) from mapel")
    suspend fun countMapel(): Int

    @Query("select * from major order by name")
    suspend fun getListMajor(): List<MajorItem>

    @Query("select count(*) from mapel_teacher where teacherId = :teacherId")
    suspend fun countMapelTeacher(teacherId: Int): Int

    @Transaction
    @Query("select * from materi where subject_id = :subject_id order by id desc")
    fun getMateri(subject_id: Int): DataSource.Factory<Int, MateriMapelTeacher>

    @Query("delete from materi where subject_id = :subjectId")
    suspend fun deleteMateriBySubject(subjectId: Int)

    @Transaction
    @Query("select * from materi where teacher_id = :teacherId order by id desc")
    fun getTeachersMateri(teacherId: Int): DataSource.Factory<Int, MateriMapelTeacher>

    @Transaction
    @Query("select * from materi where teacher_id = :teacherId and subject_id = :subjectId order by id desc")
    fun getTeachersMateriSubject(
        teacherId: Int,
        subjectId: Int
    ): DataSource.Factory<Int, MateriMapelTeacher>

    @Transaction
    @Query("select * from materi where teacher_id = :teacherId and class_id = :classId order by id desc")
    fun getTeachersMateriClass(
        teacherId: Int,
        classId: Int
    ): DataSource.Factory<Int, MateriMapelTeacher>

    @Transaction
    @Query("select * from materi where teacher_id = :teacherId and subject_id = :subjectId and class_id = :classId order by id desc")
    fun getTeachersMateriSubjectClass(
        teacherId: Int,
        subjectId: Int,
        classId: Int
    ): DataSource.Factory<Int, MateriMapelTeacher>

    @Query("select count(*) from materi where subject_id = :subject_id")
    suspend fun countMateri(subject_id: Int): Int

    @Query("select count(*) from materi where teacher_id = :teacherId")
    suspend fun countTeachersMateri(teacherId: Int): Int

    @Query("select count(*) from materi where teacher_id = :teacherId and subject_id = :subjectId")
    suspend fun countTeachersMateriSubject(teacherId: Int, subjectId: Int): Int

    @Query("select count(*) from materi where teacher_id = :teacherId and class_id = :classId")
    suspend fun countTeachersMateriClass(teacherId: Int, classId: Int): Int

    @Query("select count(*) from materi where teacher_id = :teacherId and subject_id = :subjectId and class_id = :classId")
    suspend fun countTeachersMateriSubjectClass(teacherId: Int, subjectId: Int, classId: Int): Int

    @Transaction
    @Query("select * from materi where id = :id")
    suspend fun getDetailMateri(id: Int): MateriWithLink?

    @Query("select * from grade order by id")
    suspend fun getListGrade(): List<GradeTable>

    @Delete
    suspend fun deleteMateri(item: MateriTable)

    @Query("delete from materi")
    suspend fun clearMateri()
}