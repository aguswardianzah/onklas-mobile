package id.onklas.app.pages.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import androidx.paging.toLiveData
import androidx.room.withTransaction
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.login.SekolahItem
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.socket.SocketClass
import id.onklas.app.socket.SocketEvents
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import id.onklas.app.utils.StringUtil
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    val moshi: Moshi,
    val api: ApiService,
    val apiWrapper: ApiWrapper,
    val db: MemoryDB,
    val pref: PreferenceClass,
    val stringUtil: StringUtil,
    val socketClass: SocketClass
) : ViewModel() {

    init {
        if (!socketClass.connected()) {
            socketClass.initSocket()
            socketClass.connect()
        }
    }

    val myId by lazy { pref.getString("klaspay_id") }
    private val pageSize = 50

    val school by lazy {
        try {
            moshi.adapter(SekolahItem::class.java).fromJson(pref.getString("school"))
        } catch (e: Exception) {
            SekolahItem()
        }
    }

    fun contactList(search: String) =
        db.chat()
            .getContact(
                "%$search%",
                myId.also { Timber.e("exclude id: $it") }
            )
            .toLiveData(
                pageSize,
                boundaryCallback = PagedListBoundaryCallback()
            )

    suspend fun fetchContactSuspend() {
        db.withTransaction {
            try {
                api.getListStudentKlaspay(pref.getString("school_uuid")).data_list.forEach {
                    // get current contact
                    var contact = db.chat().contact(it.wallet_id)

                    if (contact != null) {
                        // update contact data
                        contact.wallet_id = it.wallet_id
                        contact.class_name = it.kelas
                        contact.majors = it.jurusan
                    } else {
                        // when contact not found, create contact from it
                        contact = UserTable(it)
                    }

                    // save updated contact
                    db.feed().insertUser(contact)

                    // save conversation
                    db.chat().conversation(it.wallet_id)?.let { conv ->
                        db.chat().insert(
                            conv.copy(
                                name = contact.name,
                                img_profile = contact.user_avatar_image
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun fetchContact() {
        viewModelScope.launch { fetchContactSuspend() }
    }

    fun closeAllChat() {
        viewModelScope.launch {
            db.chat().closeConversation()
        }
    }

    val emptyConversation by lazy { MutableLiveData<Boolean>() }
    fun conversation(type: String = "") = db.chat().conversationPaging(myId, type).toLiveData(
        pageSize,
        boundaryCallback = PagedListBoundaryCallback({
            emptyConversation.postValue(true)
        })
    )

    suspend fun setConversation(with: String, name: String, image: String) {
        val oldConv = db.chat().conversation(with)

        if (oldConv != null) {
            db.chat().insert(
                oldConv.copy(
                    name = name,
                    img_profile = image,
                    is_open = true,
                    unread = 0,
                    show_notif = false
                )
            )
        } else {
            db.chat().insert(
                ConversationItem(
                    with,
                    name,
                    image,
                    is_open = true,
                    type = if (with.contains('M', true)) "kwu" else ""
                )
            )
        }

        db.chat().clearNotif(with)

        // send ack read to unprocessed chats
        Timber.e("get unprocessed chats")
        db.chat().getUnprocessedChat(with, myId).forEach { item ->
            socketClass.emitData(
                SocketEvents.EVENT_MESSAGE,
                chatAdapter.toJson(
                    ChatResponse(item).apply {
                        cmd = "ack"
                        ack = 3
                    }
                )
            ) {
                viewModelScope.launch { db.chat().setProcessed(item.id) }
            }
        }
    }

    fun currentConv(with: String) = db.chat().conversationListen(with)

    suspend fun closeConv() {
        db.chat().getUnprocessedProductChat(with, myId).run {
            if (isEmpty())
                db.chat().closeConversation()
            else {
                forEach { db.chat().deleteChat(it.id) }
                if (db.chat().countChat(with) == 0)
                    db.chat().deleteConversation(with)
            }
        }
    }

    lateinit var with: String
    lateinit var name: String
    fun chat(with: String) = db.chat().getChats(with)
        .toLiveData(pageSize, boundaryCallback = object : PagedList.BoundaryCallback<ChatItem>() {
            override fun onZeroItemsLoaded() {
                super.onZeroItemsLoaded()
//                viewModelScope.launch {
//                    db.chat().insert(
//                        ChatItem(
//                            id = stringUtil.md5("$myId-$with-${System.currentTimeMillis()}"),
//                            with = with,
//                            message = "Anda terhubung dengan $name, silahkan mulai percakapan",
//                            type = ChatType.CHAT_TYPE_INFO
//                        )
//                    )
//                }
            }
        })

    val dateFormat by lazy { SimpleDateFormat("dd MMMM yyyy", Locale("id")) }
    val timeFormat by lazy { SimpleDateFormat("HH:mm", Locale("id")) }

    private suspend fun initNewChat(lastChat: ChatItem?): String {
        val lastDay = Calendar.getInstance().apply {
            timeInMillis = lastChat?.date?.time ?: 0L
        }
        val current = Calendar.getInstance()
        if (
            lastChat == null || lastChat.type == ChatType.CHAT_TYPE_INFO ||
            (current.get(Calendar.YEAR) >= lastDay.get(Calendar.YEAR) &&
                    current.get(Calendar.DAY_OF_YEAR) > lastDay.get(Calendar.DAY_OF_YEAR))
        ) {
            db.chat().insert(
                ChatItem(
                    id = stringUtil.md5("$myId-$with-${System.currentTimeMillis()}"),
                    with = this@ChatViewModel.with,
                    type = ChatType.CHAT_TYPE_DATE,
                    date = Date(System.currentTimeMillis() + 1)
                )
            )
        }

        return stringUtil.md5("$myId-$with-${System.currentTimeMillis()}")
    }

    private val chatAdapter by lazy { moshi.adapter(ChatResponse::class.java) }
    private val anyAdapter by lazy { moshi.adapter(Any::class.java) }
    fun sendChat(msg: String) {
        viewModelScope.launch {
            db.withTransaction {
                val lastChat = db.chat().getLastChat(with)
                val chatId = initNewChat(lastChat)

                db.chat().insert(
                    ChatItem(
                        id = chatId,
                        with = this@ChatViewModel.with,
                        to = this@ChatViewModel.with,
                        from = myId,
                        message = msg,
                        first = lastChat?.type != ChatType.CHAT_TYPE_MESSAGE || lastChat.type != ChatType.CHAT_TYPE_PRODUCT || lastChat.from != myId,
                        date = Date(System.currentTimeMillis() + 2),
                        show_notif = false
                    )
                )

                db.chat().updateLastChat(with, chatId)

                if (lastChat?.type == ChatType.CHAT_TYPE_PRODUCT) {
                    val payload = anyAdapter.toJson(
                        mapOf(
                            "productId" to lastChat.product_id,
                            "productName" to lastChat.product_name,
                            "productImage" to lastChat.product_image,
                            "productPrice" to lastChat.product_price
                        )
                    )
                    socketClass.emitData(
                        SocketEvents.EVENT_MESSAGE,
                        chatAdapter.toJson(
                            ChatResponse(
                                "send",
                                lastChat.id,
                                payload,
                                myId,
                                with,
//                                type = "marketplace",
                                payload = payload
                            )
                        )
                    ) {
                        viewModelScope.launch {
                            db.chat().insert(lastChat.copy(processed = true))
                        }
                    }
                }

                socketClass.emitData(
                    SocketEvents.EVENT_MESSAGE,
                    chatAdapter.toJson(
                        ChatResponse("send", chatId, msg, myId, with)
                    )
                )
            }
        }
    }

    fun insertProductChat(
        productId: Int,
        productName: String,
        productImage: String,
        productPrice: Int
    ) {
        viewModelScope.launch {
            db.withTransaction {
                val lastChat = db.chat().getLastChat(with)
                val chatId = initNewChat(lastChat)

                db.chat().insert(
                    ChatItem(
                        id = chatId,
                        with = this@ChatViewModel.with,
                        to = this@ChatViewModel.with,
                        from = myId,
                        first = lastChat?.type != ChatType.CHAT_TYPE_MESSAGE || lastChat.type != ChatType.CHAT_TYPE_PRODUCT || lastChat.from != myId,
                        date = Date(System.currentTimeMillis() + 2),
                        show_notif = false,
                        type = ChatType.CHAT_TYPE_PRODUCT,
                        product_id = productId,
                        product_name = productName,
                        product_image = productImage,
                        product_price = productPrice
                    )
                )

                db.chat().updateLastChat(with, chatId)
            }
        }
    }
}