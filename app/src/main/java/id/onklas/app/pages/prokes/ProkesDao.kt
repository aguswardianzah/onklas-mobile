package id.onklas.app.pages.prokes

import androidx.paging.DataSource
import androidx.room.*
import id.onklas.app.pages.entrepreneurs.TrackingDetail
import kotlinx.coroutines.flow.Flow


@Dao
interface ProkesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(data: ListClass)

    @Query("select * from list_class ")
    fun getClassProkes(): DataSource.Factory<Int, ListClass>


    @Query("select count(*) from list_class")
    suspend fun countClass(): Int



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(data: ListStudentItem)

    @Query("select * from list_student_item where classId = :classId and name LIKE '%' || :search || '%' ")
    fun getStudent(search:String,classId:Int): DataSource.Factory<Int, ListStudentItem>


    @Query("select count(*) from list_student_item where classId = :classId and name LIKE '%' || :search || '%' ")
    suspend fun countStudent(search:String,classId:Int): Int




    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChoice(data: ChoiceTable)

    @Query("select * from choice_table where mainKey = :MainKey order by :Order")
    fun getChoice(MainKey:String,Order:String): Flow<List<ChoiceTable>>

    @Query("select count(*) from choice_table where mainKey = :MainKey")
    suspend fun countChoice(MainKey:String): Int

    @Query("Delete  from choice_table")
    suspend fun deleteChoice()



}