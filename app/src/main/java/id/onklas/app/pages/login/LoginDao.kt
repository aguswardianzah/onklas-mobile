package id.onklas.app.pages.login

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LoginDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchool(data: List<SekolahItem>)

    @Query("select * from school where name like :search order by id desc")
    fun getListSchool(search: String): DataSource.Factory<Int, SekolahItem>

    @Query("select count(*) from school where name like :search")
    suspend fun countSchool(search: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(data: List<SessionData>)

    @Query("select * from sessions")
    fun getSessions(): DataSource.Factory<Int, SessionData>

    @Query("select count(*) from sessions")
    suspend fun countSession(): Int

    @Query("delete from sessions")
    suspend fun nukeSession()

    @Query("delete from sessions where id = :id")
    suspend fun deleteSession(id: Int)
}