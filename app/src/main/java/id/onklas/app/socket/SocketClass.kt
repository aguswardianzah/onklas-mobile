package id.onklas.app.socket

import android.content.Context
import androidx.work.*
import com.squareup.moshi.Moshi
import id.onklas.app.BuildConfig
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.chat.*
import id.onklas.app.utils.NotifUtil
import id.onklas.app.utils.PreferenceClass
import id.onklas.app.utils.StringUtil
import id.onklas.app.worker.ChatIncomingHandler
import id.onklas.app.worker.ChatSender
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketClass @Inject constructor(
    val context: Context,
    val pref: PreferenceClass,
    val moshi: Moshi,
    val db: MemoryDB,
    val stringUtil: StringUtil,
    val notifUtil: NotifUtil
) {

    private var socket: Socket? = null
    private val myId by lazy { pref.getString("klaspay_id") }
    private val chatAdapter by lazy { moshi.adapter(ChatResponse::class.java) }
    private val presenceAdapter by lazy { moshi.adapter(PresenceItem::class.java) }

    fun initSocket() {
        if (socket != null || pref.getString("user_token").isEmpty()) {
            return
        }
//        socket?.off()
//        socket?.disconnect()
//        socket = null
//        isConnecting = false

        socket = try {
            IO.socket(
                BuildConfig.SOCKET_URL,
                IO.Options.builder()
                    .setAuth(
                        mapOf(
                            "token" to pref.getString("user_token")
                                .also { Timber.tag("SOCKET").e("init socket with id $it") })
                    )
                    .setReconnection(true)
                    .setPath("/api/messenger/socket.io")
                    .setTransports(arrayOf(WebSocket.NAME))
                    .build()
            ).apply {

                on(Socket.EVENT_CONNECT) {
                    Timber.tag("SOCKET").e("socket connected with id: ${id()}")

                    // try to resend unprocessed chats
                    GlobalScope.launch {
                        db.chat().getUnsentChats(myId).forEach {
                            emitData(
                                SocketEvents.EVENT_MESSAGE, chatAdapter.toJson(
                                    ChatResponse("send", it.id, it.message, it.from, it.to)
                                )
                            )
                        }

                        // try to resend queued data
                        db.socket().getUnprocessedQueue().forEach { queue ->
                            emitData(queue.event, queue.data) {
                                GlobalScope.launch { db.socket().setProcessedQueue(queue.id) }
                            }
                        }
                    }

                    // register periodic worker to resend chat for each minutes
                    WorkManager.getInstance(context)
                        .enqueueUniquePeriodicWork(
                            "chat_sender",
                            ExistingPeriodicWorkPolicy.KEEP,
                            PeriodicWorkRequestBuilder<ChatSender>(1, TimeUnit.MINUTES).build()
                        )
                }

                on(Socket.EVENT_CONNECT_ERROR) {
                    Timber.tag("SOCKET").e("connect socket error")
                    Timber.tag("SOCKET").e(it.firstOrNull()?.toString())
                }

                on(Socket.EVENT_DISCONNECT) {
                    isConnecting = false
                }

                on(SocketEvents.EVENT_PRESENCE) {
                    val resp = it.firstOrNull() ?: return@on
                    Timber.tag("SOCKET").e("[${SocketEvents.EVENT_PRESENCE}] receive: $resp")

                    presenceAdapter.fromJson(resp.toString())?.let { presenceItem ->
                        GlobalScope.launch {
                            db.chat().updatePresence(presenceItem.id, presenceItem.type)
                        }
                    } ?: Timber.tag("SOCKET").e("failed to parse presence json")
                }

                on(SocketEvents.EVENT_MESSAGE) {
                    val resp = it.firstOrNull() ?: return@on
                    Timber.tag("SOCKET")
                        .e("[${SocketEvents.EVENT_MESSAGE}] receive: $resp")

                    chatAdapter.fromJson(resp.toString())?.let { newChat ->
                        val workRequest = OneTimeWorkRequestBuilder<ChatIncomingHandler>()
                            .setInputData(
                                workDataOf("resp" to resp.toString())
                            )
                            .setConstraints(
                                Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build()
                            )
                            .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS
                            )
                            .addTag("chat_incoming_handler")
                            .build()

                        WorkManager.getInstance(context)
                            .enqueueUniqueWork(
                                "chat_incoming_handler_${newChat.cmd}_${newChat.id}",
                                ExistingWorkPolicy.REPLACE,
                                workRequest
                            )
                    } ?: Timber.tag("SOCKET").e("failed to parse msg json")
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    fun emitData(event: String, data: String, onAck: Ack? = null) {
        if (socket == null || !connected()) {
            Timber.tag("SOCKET")
                .e("fail to emit, socket is null --> init and connect socket, add emitData to queue")
            GlobalScope.launch {
                Timber.tag("SOCKET").e("[$event] insert emit queue: $data")
                db.socket().insert(SocketQueueItem(event = event, data = data))
                initSocket()
                connect()
            }
            return
        }

        Timber.tag("SOCKET").e("[$event] emitting: $data")
        socket?.emit(
            event,
            arrayOf(data)
        ) {
//            Timber.tag("SOCKET").e("[$event] $data -- report: ${it.firstOrNull()}")
            onAck?.call(it)
        }
    }

    private var isConnecting = false
    fun connect() {
        if (!connected() && !isConnecting && socket != null) {
            Timber.e("try connecting to socket")
            isConnecting = true
            socket?.connect()
        } else {
            Timber.e("is connecting to socket")
        }
    }

    fun connected() = socket?.connected() == true

    fun disconnect() {
        if (connected())
            socket?.disconnect()
    }

    fun off() = socket?.off()
}

object SocketEvents {
    const val EVENT_PRESENCE = "Presence"
    const val EVENT_MESSAGE = "Msg"
}