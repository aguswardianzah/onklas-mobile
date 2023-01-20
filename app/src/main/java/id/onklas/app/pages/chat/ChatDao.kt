package id.onklas.app.pages.chat

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import id.onklas.app.pages.sekolah.sosmed.UserTable

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ConversationItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ChatItem)

    @Query("select sum(unread) from conversation where type = :type")
    fun countAllUnread(type: String = ""): LiveData<Int>

    @Transaction
    @Query("select * from conversation where last_chat_id != '' and `with` != :myId and type = :type order by last_update desc")
//    @Query("select * from conversation order by last_update desc")
    fun conversationPaging(
        myId: String,
        type: String = ""
    ): DataSource.Factory<Int, ConversationWithLastMessage>

    @Query("select * from conversation where `with` = :with limit 1")
    suspend fun conversation(with: String): ConversationItem?

    @Query("select * from conversation where unread > 0 order by last_update desc")
    suspend fun unreadConversations(): List<ConversationItem>

    @Query("select * from conversation where show_notif = 1 order by last_update desc")
    suspend fun notifConversations(): List<ConversationItem>

    @Query("update conversation set unread = unread + 1 where `with` = :with")
    suspend fun incUnreadConversation(with: String)

    @Query("update conversation set last_chat_id = :lastChatId where `with` = :with")
    suspend fun updateLastChat(with: String, lastChatId: String)

    @Query("select is_open from conversation where `with` = :with limit 1")
    suspend fun isConversationOpen(with: String): Boolean

    @Query("select count(*) from conversation where `with` = :with")
    suspend fun countConversation(with: String): Int

    @Query("select * from conversation where `with` = :with limit 1")
    fun conversationListen(with: String): LiveData<ConversationItem>

    @Query("update conversation set presence = :presence where `with` = :with")
    suspend fun updatePresence(with: String, presence: String)

    @Query("update conversation set is_open = 0")
    suspend fun closeConversation()

    @Query("delete from conversation where `with` = :with")
    suspend fun deleteConversation(with: String)

    @Query("update conversation set is_open = 0 where `with` = :with")
    suspend fun closeConversation(with: String)

    @Query("update chat set show_notif = 0 where `with` = :with and show_notif = 1")
    suspend fun clearNotif(with: String)

    @Query("select * from user where wallet_id = :walletId limit 1")
    suspend fun contact(walletId: String): UserTable?

    @Query("select * from user where wallet_id != '' and wallet_id != :exclude and (name like :search COLLATE NOCASE or majors like :search COLLATE NOCASE or class_name like :search COLLATE NOCASE or nis_nik like :search COLLATE NOCASE or nisn_nik like :search COLLATE NOCASE) order by name COLLATE NOCASE ASC, majors COLLATE NOCASE ASC, class_name COLLATE NOCASE ASC")
    fun getContact(search: String, exclude: String): DataSource.Factory<Int, UserTable>

    @Query("select count(*) from user where nisn_nik = :nisn or nis_nik = :nisn")
    suspend fun countContact(nisn: String): Int

    @Query("update user set wallet_id = :wallet_id, class_name = :class_name, majors = :majors where nisn_nik = :nisn or nis_nik = :nisn")
    suspend fun updateContact(nisn: String, wallet_id: String, class_name: String, majors: String)

    @Query("select count(*) from chat where `with` = :with and type > 2")
    suspend fun countChat(with: String): Int

    @Query("select * from chat where `with` = :with order by date desc limit 1")
    suspend fun getLastChat(with: String): ChatItem?

    @Query("select * from chat where `with` = :with and is_deleted = 0 order by date desc")
    fun getChats(with: String): DataSource.Factory<Int, ChatItem>

    @Query("select * from chat where id = :id limit 1")
    suspend fun singleChat(id: String): ChatItem?

    @Query("update chat set processed = 1 where id = :id")
    suspend fun setProcessed(id: String)

    @Query("select * from chat where `with` = :with and processed = 0 and `from` = :myId and type = 3")
    suspend fun getUnprocessedProductChat(with: String, myId: String): List<ChatItem>

    @Query("select * from chat where `with` = :with and processed = 0 and `from` != :myId and type = 0")
    suspend fun getUnprocessedChat(with: String, myId: String): List<ChatItem>

    @Query("select * from chat where `with` = :with and show_notif = 1 and type = 0 order by date")
    suspend fun getNotifChat(with: String): List<ChatItem>

    @Query("select * from chat where `from` = :myId and status = 0")
    suspend fun getUnsentChats(myId: String): List<ChatItem>

    @Query("delete from chat where id = :id")
    suspend fun deleteChat(id: String)
}