package id.onklas.app.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.room.withTransaction
import com.squareup.moshi.Moshi
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.chat.ChatListPage
import id.onklas.app.pages.chat.ChatPage
import id.onklas.app.pages.chat.ChatResponse
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.services.DirectReplyChat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotifUtil @Inject constructor(
    val context: Context,
    val db: MemoryDB,
    val moshi: Moshi,
    val pref: PreferenceClass,
    val stringUtil: StringUtil,
    val api: ApiService
) {

    private val myId by lazy { pref.getString("klaspay_id") }

    private val userTableAdapter by lazy { moshi.adapter(UserTable::class.java) }
    val userTable: UserTable by lazy {
        try {
            userTableAdapter.fromJson(pref.getString("user"))
                ?: UserTable(pref.getInt("user_id"))
        } catch (e: Exception) {
            UserTable(pref.getInt("user_id"))
        }
    }

    // build remote input for direct reply
    private val remoteInput by lazy {
        RemoteInput.Builder("reply_message").setLabel("Tulis pesan...").build()
    }

    val chatNotifs by lazy { mutableListOf<Pair<Int, NotificationCompat.Builder>>() }
    val chatNotif by lazy { mutableListOf<Notification>() }
    val notifIds by lazy { mutableListOf<Int>() }

    fun buildGroupChatNotif() {
        GlobalScope.launch {
            Timber.e("start build chat notif")
            var totalUnread = 0

            try {
                db.chat().notifConversations().forEach { conv ->
                    Timber.e("conv: $conv")

                    if (conv.with == myId) return@forEach

                    totalUnread += conv.unread

                    val notifId = stringUtil.numOnly(conv.with)

                    var contact = db.chat().contact(conv.with)
                    if (contact == null) {
                        fetchContactSuspend()
                        contact = db.chat().contact(conv.with)
                    }

                    if (contact == null) return@forEach

                    val personMe =
                        buildNotifPerson("Anda", userTable.user_avatar_image, userTable.uuid)
                    val personWith =
                        buildNotifPerson(contact.name, contact.user_avatar_image, contact.uuid)

                    val chatPendingIntent = buildChatPendingIntent(contact)
                    val replyPendingIntent = buildReplyPendingIntent(conv.with)

                    val notifReplyAction =
                        buildNotifReplyAction(replyPendingIntent ?: chatPendingIntent, remoteInput)

                    val messagingStyle = NotificationCompat.MessagingStyle(personMe)
                        .setGroupConversation(false)
                        .setConversationTitle(conv.name)

                    // get chat with notif flag
                    Timber.e("get chat with notif flag")
                    db.chat().getNotifChat(conv.with).forEach { chat ->
                        Timber.e("chat: $chat")
                        messagingStyle.addMessage(
                            NotificationCompat.MessagingStyle.Message(
                                chat.message,
                                chat.date.time,
//                                if (chat.from == userTable.wallet_id) personMe else personWith
                                if (chat.from == userTable.wallet_id) personMe else personWith
                            )
                        )
                    }

                    if (messagingStyle.messages.isEmpty()) return@forEach

                    val notifBuilder =
                        NotificationCompat.Builder(context, context.getString(R.string.app_name))
                            .setSmallIcon(R.drawable.ic_logo_notif)
                            .setContentTitle(conv.name)
                            .setContentText("${conv.unread} pesan baru")
                            .setStyle(messagingStyle)
                            .setContentIntent(chatPendingIntent)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                            .addAction(notifReplyAction)
                            .setCategory(Notification.CATEGORY_MESSAGE)
                            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                            .setGroup("chat_group")

                    if (notifIds.contains(notifId)) {
                        val index = notifIds.indexOf(notifId)
                        chatNotifs[index] = notifId to notifBuilder
                    } else {
                        Timber.e("ids: $notifIds")
                        notifIds.add(notifId)
                        chatNotif.add(notifBuilder.build())
                        chatNotifs.add(notifId to notifBuilder)
                    }
                }

                NotificationManagerCompat.from(context).run {
                    cancelAll()

                    chatNotif.forEachIndexed { index, notification ->
                        Timber.e("notify index: ${notifIds[index]}")
                        notify(notifIds[index], notification)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun buildChatNotifBiasa(newChat: ChatResponse) {
        GlobalScope.launch {
            try {
                var contact = db.chat().contact(newChat.from)
                if (contact == null) {
                    fetchContactSuspend()
                    contact = db.chat().contact(newChat.from)
                }

                if (contact == null) return@launch

                val notification =
                    NotificationCompat.Builder(context, context.getString(R.string.app_name))
                        .setSmallIcon(R.drawable.ic_logo_notif)
                        .setContentTitle("Pesan baru dari ${contact.name}")
                        .setContentText(newChat.text)
                        .setContentIntent(buildChatPendingIntent(contact))
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        .setCategory(Notification.CATEGORY_MESSAGE)
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .setGroup("chat_group")


                val bitmapRequest =
                    GlideApp.with(context).asBitmap().load(contact.user_avatar_image).circleCrop()
                        .submit(64, 64)
                val bitmap = try {
                    bitmapRequest.get()
                } catch (e: Exception) {
                    null
                }

                if (bitmap != null)
                    notification.setLargeIcon(bitmap)

                NotificationManagerCompat.from(context)
                    .notify(stringUtil.numOnly(newChat.from), notification.build())
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun buildNotifPerson(name: String, image: String, key: String): Person {
        val person = Person.Builder()
            .setName(name)
            .setKey(key)

        val bitmapRequest =
            GlideApp.with(context).asBitmap().load(image).circleCrop().submit(64, 64)
        val bitmap = try {
            bitmapRequest.get()
        } catch (e: Exception) {
            null
        }

        if (bitmap != null)
            person.setIcon(IconCompat.createWithBitmap(bitmap))

        return person.build()
    }

    private val conversationPendingIntent by lazy {
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, ChatListPage::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun buildChatPendingIntent(contact: UserTable): PendingIntent {
        Timber.e("pending contact: $contact")
        val chatIntent = Intent(context, ChatPage::class.java)
            .putExtra("with", contact.wallet_id)
            .putExtra("name", contact.name)
            .putExtra("image", contact.user_avatar_image)

        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(chatIntent)
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } ?: PendingIntent.getActivities(
            context,
            0,
            arrayOf(chatIntent),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun buildReplyPendingIntent(with: String) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // if api level 24+, get service to direct reply from notif
            PendingIntent.getService(
                context,
                1,
                Intent(context, DirectReplyChat::class.java).putExtra("with", with),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            null
        }

    private fun buildNotifReplyAction(
        replyIntent: PendingIntent,
        remoteInput: RemoteInput
    ) =
        NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "Balas",
            replyIntent
        )
            .addRemoteInput(remoteInput)
            .setShowsUserInterface(false)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .build()

    private suspend fun fetchContactSuspend() {
        db.withTransaction {
            try {
                api.getListStudentKlaspay(pref.getString("school_uuid")).data_list.forEach {
                    // get current contact
                    var contact = db.chat().contact(it.wallet_id)

                    if (contact != null) {
                        // update contact data
                        contact.user_avatar_image = it.foto
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