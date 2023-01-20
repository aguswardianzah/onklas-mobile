package id.onklas.app.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.onklas.app.di.component
import id.onklas.app.pages.chat.ChatItem
import id.onklas.app.pages.chat.ChatResponse
import id.onklas.app.pages.chat.ChatType
import id.onklas.app.socket.SocketEvents
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class ChatOutgoingHandler(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val socketClass by lazy { component.socketClass }
    private val moshi by lazy { component.moshi }
    private val chatAdapter by lazy { moshi.adapter(ChatResponse::class.java) }
    private val stringUtil by lazy { component.stringUtil }
    private val db by lazy { component.memoryDB }
    private val pref by lazy { component.preference }
    private val notifUtil by lazy { component.notifUtil }
    private val myId by lazy { pref.getString("klaspay_id") }

    override suspend fun doWork(): Result {
        Timber.e("start outgoing chat handler with params: $inputData")

        if (socketClass.connected()) {
            val with = inputData.getString("with") ?: return Result.success()
            val reply = inputData.getString("reply") ?: return Result.success()
            val chatId = inputData.getString("chatId") ?: return Result.success()

            db.withTransaction {
                val lastChat = db.chat().getLastChat(with)

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
                            with = with,
                            type = ChatType.CHAT_TYPE_DATE,
                            date = Date(System.currentTimeMillis() + 1)
                        )
                    )
                }

                db.chat().insert(
                    ChatItem(
                        id = chatId,
                        with = with,
                        to = with,
                        from = myId,
                        message = reply,
                        first = lastChat?.type != ChatType.CHAT_TYPE_MESSAGE || lastChat.from != myId,
                        date = Date(System.currentTimeMillis() + 2)
                    )
                )

                val conv = db.chat().conversation(with)
                if (conv != null) {
                    db.chat().insert(
                        conv.copy(
                            unread = 0,
                            last_update = Date(),
                            last_chat_id = chatId
                        )
                    )
                }

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
                        GlobalScope.launch { db.chat().setProcessed(item.id) }
                    }
                }

                socketClass.emitData(
                    SocketEvents.EVENT_MESSAGE,
                    chatAdapter.toJson(
                        ChatResponse("send", chatId, reply, myId, with)
                    )
                )

                val notif =
                    notifUtil.chatNotifs.firstOrNull { it.first == stringUtil.numOnly(with) }
                if (notif != null) {
                    val messagingStyle =
                        NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(
                            notif.second.build()
                        )

                    if (messagingStyle != null) {
                        messagingStyle.addMessage(
                            NotificationCompat.MessagingStyle.Message(
                                reply,
                                System.currentTimeMillis(),
                                null as Person?
                            )
                        )

                        notif.second.setStyle(messagingStyle)

                        NotificationManagerCompat.from(applicationContext)
                            .notify(notif.first, notif.second.build())
                    } else
                        notifUtil.buildGroupChatNotif()
                } else {
                    notifUtil.buildGroupChatNotif()
                }
            }

            return Result.success()
        } else {
            Timber.e("socket disconected, return retry")
            socketClass.initSocket()
            socketClass.connect()
            return Result.retry()
        }
    }
}