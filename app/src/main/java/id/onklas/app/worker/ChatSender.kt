package id.onklas.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.onklas.app.di.component
import id.onklas.app.pages.chat.ChatResponse
import id.onklas.app.socket.SocketEvents

class ChatSender(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val pref by lazy { component.preference }
    private val moshi by lazy { component.moshi }
    private val db by lazy { component.memoryDB }
    private val socket by lazy { component.socketClass }
    private val myId by lazy { pref.getString("klaspay_id") }
    private val chatAdapter by lazy { moshi.adapter(ChatResponse::class.java) }

    override suspend fun doWork(): Result {
        db.chat().getUnsentChats(myId).forEach {
            socket.emitData(
                SocketEvents.EVENT_MESSAGE, chatAdapter.toJson(
                    ChatResponse("send", it.id, it.message, it.from, it.to)
                )
            )
        }

        return Result.success()
    }
}