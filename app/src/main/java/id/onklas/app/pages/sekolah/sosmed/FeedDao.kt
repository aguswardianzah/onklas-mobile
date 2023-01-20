package id.onklas.app.pages.sekolah.sosmed

import androidx.paging.DataSource
import androidx.room.*

@Dao
interface FeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<FeedTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: FeedTable): Long

    @Query("update feed set isLike = 1, count_likes = count_likes + 1 where feed_id = :feedId")
    suspend fun likeFeed(feedId: Int)

    @Query("update feed set isLike = 0, count_likes = count_likes - 1 where feed_id = :feedId")
    suspend fun unlikeFeed(feedId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(data: FeedFileTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(data: List<UserTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(data: UserTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(data: List<FeedCommentTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(data: FeedCommentTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(data: FeedUserCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(data: List<FeedUserCrossRef>)

    @Delete
    suspend fun deleteLike(data: FeedUserCrossRef)

    @Query("delete from feed where feed_id = :feedId")
    suspend fun deletePost(feedId: Int)

    @Query("select count(*) from feed where feed_type = :type")
    suspend fun countFeed(type: String = "text"): Int

    @Query("select count(*) from feed_comment where feed_id = :feedId")
    suspend fun countFeed(feedId: Int): Int

    @Query("select count(*) from user where name like :search or username like :search")
    suspend fun countUser(search: String): Int

    @Query("delete from feed where feed_type = :type")
    suspend fun deleteFeed(type: String = "text")

    @Transaction
    @Query("select * from feed where feed_type = :type order by feed_id desc")
    fun getFeed(type: String = "text"): DataSource.Factory<Int, FeedTimeline>

    @Transaction
    @Query("select * from feed where feed_body like :search collate nocase order by feed_id desc")
    fun exploreFeed(search: String = ""): DataSource.Factory<Int, FeedTimeline>

    @Query("select count(*) from feed where feed_body like :search collate nocase")
    suspend fun countExploreFeed(search: String = ""): Int

    @Transaction
    @Query("select * from feed where user_id_owner = :userId and feed_type = :type order by created_at desc")
    fun getFeedByUser(
        userId: Int,
        type: String = "text"
    ): DataSource.Factory<Int, FeedTimeline>

    @Query("select count(*) from feed  where user_id_owner = :userId and feed_type = :type")
    suspend fun countFeedByUser(
        userId: Int,
        type: String = "text"
    ): Int

    @Transaction
    @Query("select * from feed where feed_id = :feed_id")
    suspend fun getFeed(feed_id: Int): FeedTimeline

    @Transaction
    @Query("select * from feed_file where type like 'image%'")
    fun getFeedMedia(): DataSource.Factory<Int, FeedMedia>

    @Transaction
    @Query("select * from feed_comment where feed_id = :feed_id order by cid")
    fun getFeedComment(feed_id: Int): DataSource.Factory<Int, FeedCommentWithUser>

    @Transaction
    @Query("select * from feed_like where feed_id = :feed_id")
    fun getFeedListLike(feed_id: Int): DataSource.Factory<Int, FeedListLike>

    @Query("select count(*) from feed_like where feed_id = :feed_id")
    suspend fun countFeedListLike(feed_id: Int): Int

    @Query("select * from user where (name like :search COLLATE NOCASE or username like :search COLLATE NOCASE) and id > 0")
    fun getUser(search: String = ""): DataSource.Factory<Int, UserTable>

    @Query("select * from user where username like :search COLLATE NOCASE or name like :search COLLATE NOCASE")
    suspend fun searchUsername(search: String = ""): List<UserTable>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHashtag(data: List<HashtagTable>)

    @Query("select * from hashtag where name like :search COLLATE NOCASE order by total desc")
    fun getHashtag(search: String = ""): DataSource.Factory<Int, HashtagTable>

    @Query("select count(*) from hashtag where name like :search COLLATE NOCASE")
    suspend fun countHashtag(search: String = ""): Int

    @Query("update feed set count_comments = count_comments + 1 where feed_id = :feedId")
    suspend fun incComment(feedId: Int)

    @Query("delete from feed where user_id_owner = :userId and feed_type = :type")
    suspend fun nukeFeedByUserId(userId: Int, type: String = "text")

    @Query("delete from feed where feed_type = :type")
    suspend fun nukeFeed(type: String = "text")
}