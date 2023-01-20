package id.onklas.app.worker

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.moshi.Types
import id.onklas.app.App
import id.onklas.app.R
import id.onklas.app.di.component
import id.onklas.app.pages.chat.*
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.socket.SocketEvents
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class ChatIncomingHandler(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val socketClass by lazy { component.socketClass }
    private val moshi by lazy { component.moshi }
    private val chatAdapter by lazy { moshi.adapter(ChatResponse::class.java) }
    private val stringUtil by lazy { component.stringUtil }
    private val db by lazy { component.memoryDB }
    private val pref by lazy { component.preference }
    private val api by lazy { component.apiService }
    private val notifUtil by lazy { component.notifUtil }
    private val myId by lazy { pref.getString("klaspay_id") }

    override suspend fun doWork(): Result {
        Timber.e("start incoming chat handler with params: $inputData")
        val resp = inputData.getString("resp") ?: return Result.success()
        var isConvOpen = false

        val newChat = try {
            chatAdapter.fromJson(resp)
        } catch (e: Exception) {
            Timber.e(e)
            null
        } ?: return Result.success()

        db.withTransaction {
            // check new chat ack
            when (newChat.cmd) {
                /* receive chat ack
                1 --> chat sent
                2 --> chat delivered
                3 --> chat read
                 */
                "ack" -> {
                    // try to get old chat with new chat id
                    val oldChat = db.chat().singleChat(newChat.id)
                    val newStatus = when (newChat.ack) {
                        1 -> ChatStatus.CHAT_STATUS_SENT
                        2 -> ChatStatus.CHAT_STATUS_RECEIVED
                        3 -> ChatStatus.CHAT_STATUS_READ
                        else -> ChatStatus.CHAT_STATUS_NEW
                    }

                    if (oldChat != null && oldChat.status < newStatus) {
                        // update status of old chat
                        db.chat().insert(oldChat.copy(status = newStatus))
                    } else {
                    }
                }

                // new chat
                else -> {
                    // check if conversation exist
                    // try to get conversation
                    var conv = db.chat().conversation(newChat.from)
                    Timber.tag("SOCKET").e("conversation: $conv")

                    // try to get last conversation's chat
                    val lastChat = db.chat().getLastChat(newChat.from)
                    Timber.tag("SOCKET").e("last chat: $lastChat")

                    if (conv == null) {
                        // no conversation found, create new one
                        var user = db.chat().contact(newChat.from)

                        if (user == null) {
                            fetchContactSuspend()
                            user = db.chat().contact(newChat.from)
                        }

                        Timber.tag("SOCKET").e("contact: $user")
                        conv = ConversationItem(
                            newChat.from,
                            user?.name ?: newChat.from,
                            user?.user_avatar_image.orEmpty(),
                            newChat.id,
                            1,
                            type = if (newChat.from.contains('M', true)) "kwu" else ""
                        )

                        // insert greeting
//                        db.chat().insert(
//                            ChatItem(
//                                id = stringUtil.md5("$myId-${newChat.from}-${System.currentTimeMillis()}"),
//                                with = newChat.from,
//                                message = "Anda terhubung dengan ${user?.name ?: newChat.from}, silahkan mulai percakapan",
//                                type = ChatType.CHAT_TYPE_INFO,
//                                date = Date(System.currentTimeMillis())
//                            ).also { Timber.tag("SOCKET").e("greet: $it") }
//                        )
                    } else {
                        // conversation found, update conversation data
                        conv.last_chat_id = newChat.id
                        conv.last_update = Date()
                        conv.show_notif = !isConvOpen

                        isConvOpen = conv.is_open

                        // inc conversation unread if conversation not open
                        if (!isConvOpen)
                            conv.unread = conv.unread + 1
                    }

                    // update conversation
                    db.chat().insert(conv)
                    Timber.tag("SOCKET").e("newConv: $conv")

                    // check if needed to insert date item
                    val lastDay = Calendar.getInstance().apply {
                        timeInMillis = lastChat?.date?.time ?: 0L
                    }
                    val current = Calendar.getInstance()
                    if (
                        lastChat == null || lastChat.type == ChatType.CHAT_TYPE_INFO ||
                        (current.get(Calendar.YEAR) >= lastDay.get(Calendar.YEAR) &&
                                current.get(Calendar.DAY_OF_YEAR) > lastDay.get(
                            Calendar.DAY_OF_YEAR
                        ))
                    ) {
                        db.chat().insert(
                            ChatItem(
                                id = stringUtil.md5("$myId-${newChat.from}-${System.currentTimeMillis()}"),
                                with = newChat.from,
                                type = ChatType.CHAT_TYPE_DATE,
                                date = Date(System.currentTimeMillis() + 1)
                            ).also { Timber.tag("SOCKET").e("chatDate: $it") }
                        )
                    }

                    // check if new chat is first of message
                    val isFirst = lastChat == null
                            || lastChat.type != ChatType.CHAT_TYPE_MESSAGE
                            || (lastChat.type == ChatType.CHAT_TYPE_MESSAGE && lastChat.from != newChat.from)

                    db.chat().clearNotif(newChat.from)

                    val notifId = stringUtil.numOnly(newChat.from)
                    if (notifUtil.notifIds.contains(notifId)) {
                        val index = notifUtil.notifIds.indexOf(notifId)
                        notifUtil.notifIds.removeAt(index)
                        notifUtil.chatNotif.removeAt(index)
                        notifUtil.chatNotifs.removeAt(index)
                    }

                    // update/insert chat
                    // try to get old chat with new chat id
                    val oldChat = db.chat().singleChat(newChat.id)
                    if (oldChat == null) {
                        db.chat().insert(
                            ChatItem(
                                newChat.id,
                                with = newChat.from,
                                from = newChat.from,
                                to = newChat.to,
                                message = newChat.text,
                                first = isFirst,
                                status = when (newChat.ack) {
                                    1 -> ChatStatus.CHAT_STATUS_SENT
                                    2 -> ChatStatus.CHAT_STATUS_RECEIVED
                                    3 -> ChatStatus.CHAT_STATUS_READ
                                    else -> ChatStatus.CHAT_STATUS_NEW
                                },
                                processed = isConvOpen,
                                date = Date(System.currentTimeMillis() + 2)
                            )
                                .apply {
                                    try {
                                        val mapAdapter = moshi.adapter<Map<String, Any>>(
                                            Types.newParameterizedType(
                                                Map::class.java,
                                                String::class.java,
                                                Any::class.java
                                            )
                                        )

                                        val payload =
                                            mapAdapter.fromJson(newChat.text) ?: return@apply

                                        if (payload.containsKey("productId") && payload["productId"] != null) {
                                            product_id = payload["productId"] as Int
                                            type = ChatType.CHAT_TYPE_PRODUCT
                                        }

                                        if (payload.containsKey("productName") && payload["productName"] != null)
                                            product_name = payload["productName"] as String

                                        if (payload.containsKey("productImage") && payload["productImage"] != null)
                                            product_image = payload["productImage"] as String

                                        if (payload.containsKey("productPrice") && payload["productPrice"] != null)
                                            product_price = payload["productPrice"] as Int
                                    } catch (_: Exception) {
                                    }
                                }
                                .also { Timber.tag("SOCKET").e("new message: $it") }
                        )
                    } else {
                        Timber.tag("SOCKET")
                            .e("new message not inserted because there is already message with the same id")
                    }

                    // play notif sound
//                    try {
//                        MediaPlayer.create(applicationContext, R.raw.pristine).start()
//                    } catch (e: Exception) {
//                        Timber.e(e)
//                    }

                    if (socketClass.connected()) {
                        socketClass.emitData(
                            SocketEvents.EVENT_MESSAGE,
                            chatAdapter.toJson(newChat.apply {
                                cmd = "ack"
                                ack =
                                    if (isConvOpen) 3 // conversation is open --> send read ack
                                    else 2 // conversation is close --> send delivered ack
                            })
                        ) {
                            if (isConvOpen)
                                GlobalScope.launch { db.chat().setProcessed(newChat.id) }
                        }

                        if (isConvOpen) {
                            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
                                Intent("${applicationContext.packageName}.newChat")
                            )
                        } else if ((applicationContext as? App)?.currentAct !is ChatListPage) {
                            notifUtil.buildGroupChatNotif()
                        } else {
                        }
//            return Result.success()
                    } else {
                        Timber.e("socket disconected, show ugly notif")
                        socketClass.initSocket()
                        socketClass.connect()
                        notifUtil.buildChatNotifBiasa(newChat)
//            return Result.retry()
                    }
                }
            }
        }

        return Result.success()
    }

    private suspend fun fetchContactSuspend() {
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
}