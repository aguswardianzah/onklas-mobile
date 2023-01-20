package id.onklas.app.pages.akm

import androidx.paging.DataSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AkmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: AkmTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<AkmExamsTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(data: AkmExamsTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(data: AkmQuestionTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(data: AkmAnswerTable): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(data: List<AkmAnswerTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstruction(data: AkmInstructionTable)

    @Query("select * from akm where status < 3 and is_active = 1 and exam_type = :exampType  order by id desc")
    fun listAkm(exampType:Int = ExamType.SCHOOL): DataSource.Factory<Int, AkmTable>

    @Query("select * from akm where status < 3 and is_school_scope = 1 and date_label = :date and exam_type = :exampType order by date_start asc")
    fun listUjianSchool(date: String,exampType:Int = ExamType.SCHOOL): DataSource.Factory<Int, AkmTable>

    @Query("select * from akm where status < 3 and is_school_scope = 1 and exam_type = :exampType order by date_start asc")
    fun listUjianTryout(exampType:Int= ExamType.SCHOOL): DataSource.Factory<Int, AkmTable>

    @Query("select count(*) from akm where status < 3 and is_school_scope = 1 and exam_type = :exampType order by date_end desc")
    suspend fun countUjianSchool(exampType:Int = ExamType.SCHOOL): Int

    @Query("select * from akm where status > 2 and is_school_scope = 1 and exam_type = :exampType order by date_end desc")
    fun listUjianSchoolScored(exampType:Int = ExamType.SCHOOL): DataSource.Factory<Int, AkmTable>

    @Query("select count(*) from akm where status > 2 and is_school_scope = 1 and exam_type = :exampType order by date_end desc")
    suspend fun countUjianSchoolScored(exampType:Int = ExamType.SCHOOL): Int

    @Query("update akm set status = 3 where id = :akmId")
    suspend fun finishAkm(akmId: Int)

    @Query("select count(*) > 0 from akm where status < 3 and student_exam_id > 0")
    suspend fun hasUnfinishedUjian(): Boolean

    @Query("select * from akm where status < 3 and student_exam_id > 0")
    suspend fun getUnfinishedUjian(): List<AkmTable>

    @Query("select count(*) from akm where status < 3 and is_active = 1 and exam_type = :exampType order by date_end desc")
    suspend fun countList(exampType:Int = ExamType.SCHOOL): Int

    @Query("select * from akm where status > 2 and is_active = 1 and exam_type = :exampType  order by id desc")
    fun listNilaiAkm(exampType:Int = ExamType.SCHOOL): DataSource.Factory<Int, AkmTable>

    @Query("select count(*) from akm where status > 2 and is_active = 1 and exam_type = :exampType order by date_end desc")
    suspend fun countScoreList(exampType:Int = ExamType.SCHOOL): Int

    @Query("select id, status, student_exam_id from akm")
    suspend fun getIdAndStatus(): List<AkmIdStatus>

    @Query("update akm set status = :status where id = :akmId")
    suspend fun updateStatusAkm(akmId: Int, status: Int)

    @Transaction
    @Query("select * from akm where id = :id")
    fun get(id: Int): Flow<AkmSchedule>

    @Query("select type from akm_exams where id = :id limit 1")
    suspend fun getExamType(id: Int): String?

    @Transaction
    @Query("select * from akm_exams where schedule_id = :akmId")
    fun getExamInstruction(akmId: Int): Flow<List<AkmExamInstruction>>

    @Transaction
    @Query("select * from akm_exams where schedule_id = :akmId")
    suspend fun getExamInstructionAsync(akmId: Int): List<AkmExamInstruction>

    @Query("update akm_exams set finished = ((select count(*) from akm_instruction where exam_id = akm_exams.id) = (select count(*) from akm_instruction where exam_id = akm_exams.id and finished = 1))")
    suspend fun setExamStatus()

    @Query("select student_exam_id from akm where id = :akmId")
    suspend fun getStudentExamId(akmId: Int): Int?

    @Transaction
    @Query("select * from akm where id = :id")
    suspend fun getSingle(id: Int): AkmSchedule?

    @Transaction
    @Query("select * from akm_instruction where id = :instId")
    fun instQuestion(instId: Int): Flow<InstuctionQuestion>

    @Transaction
    @Query("select * from akm_instruction where id = :instId")
    suspend fun getInstQuestion(instId: Int): InstuctionQuestion

    @Query("select exam_id from akm_instruction where id = :instId limit 1")
    suspend fun getExamIdByInstId(instId: Int): Int?

    @Query("update akm_instruction set answered = (select count(*) from akm_question where instruction_id = :instId and answered = 1) where id = :instId")
    suspend fun setInstAnswered(instId: Int)

    @Transaction
    @Query("select * from akm_question where id = :questionId")
    fun questionAnswer(questionId: Int): Flow<QuestionAnswers>

    @Query("select type from akm_question where id = :id limit 1")
    suspend fun getQuestionType(id: Int): Int?

    @Query("update akm_question set answered = 1 where id = :questionId")
    suspend fun setAnswered(questionId: Int)

    @Query("select * from akm_answer where question_id = :questionId order by sequence")
    suspend fun getAnswers(questionId: Int): List<AkmAnswerTable>

    @Query("select * from akm_answer where question_id = :questionId and selected = 1")
    suspend fun getSelectedAnswers(questionId: Int): List<AkmAnswerTable>

    @Query("update akm_answer set selected = 0 where question_id = :questionId")
    suspend fun unselectAnswers(questionId: Int)

    @Query("update akm_exams set show_child = 0 where schedule_id = :akmId")
    suspend fun collapseExam(akmId: Int)

    @Query("delete from akm where id = :id")
    suspend fun delete(id: Int)

    @Query("delete from akm")
    suspend fun nuke()
}