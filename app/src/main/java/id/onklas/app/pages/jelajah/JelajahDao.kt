package id.onklas.app.pages.jelajah

import androidx.paging.DataSource
import androidx.room.*

@Dao
interface JelajahDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<ExploreFeedTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: ExploreFeedTable): Long

    @Transaction
    @Query("select * from explore_feed where feed_body like :search collate NOCASE order by feed_id desc")
    fun getFeed(search: String): DataSource.Factory<Int, FeedExplore>

    @Query("select count(*) from explore_feed where feed_body like :search collate NOCASE")
    suspend fun countFeed(search: String = ""): Int
}