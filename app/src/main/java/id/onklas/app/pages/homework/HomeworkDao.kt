package id.onklas.app.pages.homework

import androidx.paging.DataSource
import androidx.room.*

@Dao
interface HomeworkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassRoom(data: List<ClassRoomTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassRoom(data: ClassRoomTable)

    @Query("select * from classroom order by name")
    suspend fun getListClassroom(): List<ClassRoomTable>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(data: HomeworkTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeworkLink(data: List<HomeworkLink>)

    @Delete
    suspend fun deleteHomework(data: HomeworkTable)

    @Query("delete from homework where id = :id")
    suspend fun deleteHomework(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeworkCollected(data: List<HomeworkCollected>)

    @Transaction
    @Query("select * from homework where type = 0 order by id desc")
    fun getHomeworkBelum(): DataSource.Factory<Int, HomeworkItemTable>

    @Query("select count(*) from homework where type = 0 order by id desc")
    suspend fun countHomeworkBelum(): Int

    @Transaction
    @Query("select * from homework where type = 1 order by id desc")
    fun getHomeworkSudah(): DataSource.Factory<Int, HomeworkItemTable>

    @Query("select count(*) from homework where type = 1 order by id desc")
    suspend fun countHomeworkSudah(): Int

    @Transaction
    @Query("select * from homework where type = 2 order by id desc")
    fun getHomeworkNilai(): DataSource.Factory<Int, HomeworkItemTable>

    @Query("select count(*) from homework where type = 2 order by id desc")
    suspend fun countHomeworkNilai(): Int

    @Transaction
    @Query("select * from homework where teacherId = :id order by id desc")
    fun getHomeworkTeacher(id: Int): DataSource.Factory<Int, HomeworkItemTable>

    @Transaction
    @Query("select * from homework where teacherId = :id and subjectId = :subjectId order by id desc")
    fun getHomeworkTeacher(id: Int, subjectId: Int): DataSource.Factory<Int, HomeworkItemTable>

    @Transaction
    @Query("select * from homework where teacherId = :id and classRoomId = :classId order by id desc")
    fun getHomeworkTeacherClass(id: Int, classId: Int): DataSource.Factory<Int, HomeworkItemTable>

    @Transaction
    @Query("select * from homework where teacherId = :id and subjectId = :subjectId and classRoomId = :classId order by id desc")
    fun getHomeworkTeacherSubjectClass(
        id: Int,
        subjectId: Int,
        classId: Int
    ): DataSource.Factory<Int, HomeworkItemTable>

    @Query("select count(*) from homework where teacherId = :id")
    suspend fun countHomeworkTeacher(id: Int): Int

    @Query("select * from homework_collected order by id desc")
    fun getHomeworkCollected(): DataSource.Factory<Int, HomeworkCollected>

    @Query("select count(*) from homework")
    suspend fun countHomeworkCollected(): Int

    @Transaction
    @Query("select * from homework where id = :id")
    suspend fun getDetailHomework(id: Int): HomeworkItemTable?
}