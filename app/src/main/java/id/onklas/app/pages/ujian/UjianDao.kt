package id.onklas.app.pages.ujian

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UjianDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<ExamTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: ExamTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(data: ExamTable)

    @Query("select * from exam where id = :id")
    suspend fun get(id: Int): ExamTable?

    @Query("select id from exam")
    suspend fun getIds(): List<Int>

    @Query("select * from exam where id = :id")
    fun getSync(id: Int): ExamTable?

    @Update
    suspend fun update(data: ExamTable): Int

    @Delete
    suspend fun delete(data: List<ExamTable>): Int

    @Query("delete from exam")
    suspend fun nuke()

    @Query("update exam set downloaded = 1 where id = :id")
    suspend fun setDownloaded(id: Int)

    @Query("update exam_questions set answered = 1 where id = :id")
    suspend fun setAnswered(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(data: QuestionTable): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(data: List<AnswerTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(data: AnswerTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyAnswer(data: MyAnswerTable)

    @Query("select * from exam where date = :date order by time")
    fun listUjian(date: String): DataSource.Factory<Int, ExamTable>

    @Query("select * from exam where status > 2 order by time")
    fun listUjianScoredSync(): DataSource.Factory<Int, ExamTable>

    @Query("select * from exam where status > 2 order by time limit :limit offset :offset")
    fun listUjianScoredSync(limit: Int, offset: Int): MutableList<ExamTable>

    @Query("select * from exam where status > 2 order by time limit :limit offset :offset")
    suspend fun listUjianScored(limit: Int, offset: Int): MutableList<ExamTable>

    @Query("select * from exam where status > 2")
    fun listUjianScored(): Flow<List<ExamTable>>

    @Query("update exam set status = 3, message = 'Menunggu nilai' where id = :id")
    suspend fun endUjian(id: Int): Int

    @Query("update exam set status = 3, message = 'Menunggu nilai' where id = :id")
    fun endUjianSync(id: Int): Int

    @Query("update exam set status = 4 where id = :id")
    suspend fun sendUjian(id: Int): Int

    @Query("update exam set status = 4 where id = :id")
    fun sendUjianSync(id: Int): Int

    @Query("select status > 2 from exam where id = :id")
    suspend fun ujianEnded(id: Int): Boolean?

    @Query("select count(*) from exam where date = :date")
    suspend fun countUjian(date: String): Int

    @Query("select count(*) from exam where scored > 0")
    suspend fun countUjianScored(): Int

    @Transaction
    @Query("select * from exam_questions where testId = :id order by `order`")
    suspend fun getUjianQuestionList(id: Int): List<QuestionAnswered>

    @Transaction
    @Query("select * from exam_questions where testId = :id order by `order`")
    fun getUjianQuestion(id: Int): LiveData<List<QuestionAnswered>>

    @Query("select * from exam_my_answers where testId = :id")
    suspend fun getUjianAnswer(id: Int): List<MyAnswerTable>

    @Query("select * from exam_my_answers where testId = :id")
    suspend fun getUjianAnswerSync(id: Int): List<MyAnswerTable>

    @Query("delete from exam where id = :id")
    suspend fun deleteTest(id: Int)

    @Query("delete from exam_questions where testId = :id")
    suspend fun deleteTestQuestion(id: Int)

    @Query("delete from exam_answers where testId = :id")
    suspend fun deleteTestChoice(id: Int)

    @Query("delete from exam_my_answers where testId = :id")
    suspend fun deleteTestAnswer(id: Int)

    @Transaction
    suspend fun deleteTestCompletely(id: Int) {
        deleteTest(id)
        deleteTestQuestion(id)
        deleteTestChoice(id)
        deleteTestAnswer(id)
    }
}