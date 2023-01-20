package id.onklas.app.utils

import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.PersistentDB
import id.onklas.app.pages.sekolah.sosmed.FeedCommentTable
import id.onklas.app.pages.sekolah.sosmed.FeedUserCrossRef
import id.onklas.app.pages.sekolah.sosmed.UserTable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class ApiWrapper @Inject constructor(
    val pref: PreferenceClass,
    val memoryDB: MemoryDB,
    val persistentDB: PersistentDB,
    val api: ApiService,
    val moshi: Moshi
) {
    val errorString by lazy { MutableLiveData<String>() }
    private val userAdapter: JsonAdapter<UserTable> by lazy { moshi.adapter(UserTable::class.java) }
    private var userTable: UserTable? = try {
        userAdapter.fromJson(pref.getString("user"))
    } catch (e: Exception) {
        null
    }

    suspend fun checkAccount(nisn: String?, schoolId: String?) = api.checkAccount(
        mapOf("nisn_nik" to nisn, "school_id" to schoolId)
    ).also {
        if (it.data.id == pref.getInt("user_id")) {
            userTable?.apply {
                name = it.data.name
                email = it.data.email
                username = it.data.user_username
                user_avatar_image = it.data.user_avatar_image
                pref.putString("user", userAdapter.toJson(this))
            }
        }
    }

    suspend fun login(uuid: String, password: String?, schoolId: Int) = api.login(
        mapOf("uuid" to uuid, "password" to password)
    ).also { loginResponse ->
        userTable = UserTable(
            loginResponse.data.id,
            loginResponse.data.uuid,
            loginResponse.data.name,
            loginResponse.data.email,
            loginResponse.data.nisn_nik,
            loginResponse.data.nis_nik,
            loginResponse.data.phone,
            loginResponse.data.user_avatar_image,
            loginResponse.data.user_username,
            schoolId
        )
        pref.putString("user", userAdapter.toJson(userTable))
    }

    suspend fun searchUsername(search: String) = api.searchUsername(search).also {
        memoryDB.feed().insertUser(it.data.map { UserTable(it) })
    }

    suspend fun uploadProfilePicture(file: File) = api.uploadProfilePicture(
        pref.getString("user_uuid"), MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        )
    ).also {
        userTable?.apply {
            user_avatar_image = file.path
            pref.putString("user", userAdapter.toJson(this))
        }
    }

    suspend fun updateAccount(phone: String, email: String, username: String) = api.updateAccount(
        pref.getString("user_uuid"), mapOf(
            "phone" to phone, "email" to email, "username" to username
        )
    ).also {
        userTable?.apply {
            this.phone = phone
            this.email = email
            this.username = username
            pref.putString("user", userAdapter.toJson(this))
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String) = api.changePassword(
        pref.getString("user_uuid"), mapOf(
            "old_password" to oldPassword, "new_password" to newPassword
        )
    ).also {
        pref.putBoolean("default_pass", false)
//        pref.putString("password", newPassword)
    }

    suspend fun resetPass(email: String?) = api.resetPass(
        mapOf(
            "user_id" to pref.getInt("user_id"),
            "email" to email
        )
    )

    suspend fun createPost(feedBody: String, file: File?) = api.createPost(
        mapOf("feed_body" to feedBody.toRequestBody("text/plain".toMediaTypeOrNull())),
        file?.let {
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody(
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
                        ?.toMediaTypeOrNull()
                )
            )
        }
    )

    suspend fun createEbook(feedTitle: String, feedAuthor: String, cover: File, file: File) =
        api.createEbook(
            mapOf(
                "feed_title" to feedTitle.toRequestBody("text/plain".toMediaTypeOrNull()),
                "feed_author" to feedAuthor.toRequestBody("text/plain".toMediaTypeOrNull())
            ),
            listOf(
                MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    file.asRequestBody(
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
                            ?.toMediaTypeOrNull()
                    )
                ),
                MultipartBody.Part.createFormData(
                    "cover",
                    cover.name,
                    cover.asRequestBody(
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(cover.extension)
                            ?.toMediaTypeOrNull()
                    )
                )
            )
        )

    suspend fun getFeedComment(feedId: Int, take: Int, skip: Int) =
        api.feedComment(feedId, take, skip)
            .also {
                memoryDB.feed().insertComment(it.data.map {
                    FeedCommentTable(
                        it.row_id,
                        feedId,
                        it.feed_comments_body,
                        it.user.id,
                        it.created_at_label
                    )
                })
            }

    suspend fun getFeedLike(feedId: Int, take: Int, skip: Int) =
        api.feedLike(feedId, take, skip).also {
            memoryDB.feed().insertUser(it.data.map { UserTable(it.user) })
            memoryDB.feed().insertLike(it.data.map {
                FeedUserCrossRef(
                    feedId,
                    it.user.id,
                    it.created_at_label
                )
            })
        }

    suspend fun likeFeed(feedId: Int) = api.likeFeed(
        mapOf(
            "user_id" to pref.getInt("user_id"),
            "feed_id" to feedId
        )
    ).also {
//        memoryDB.feed().insertLike(FeedUserCrossRef(feedId, pref.getInt("user_id")))
//        memoryDB.feed().likeFeed(feedId)
    }

    suspend fun unlikeFeed(feedId: Int) = api.unlikeFeed(feedId).also {
//        memoryDB.feed().deleteLike(FeedUserCrossRef(feedId, pref.getInt("user_id")))
//        memoryDB.feed().unlikeFeed(feedId)
    }

    suspend fun commentFeed(feedId: Int, comment: String?) = api.commentFeed(
        feedId, mapOf("feed_comments_body" to comment)
    )

    suspend fun deleteFeed(feedId: Int) = api.deleteFeed(feedId)
        .also { memoryDB.feed().deletePost(feedId) }

    suspend fun createTheory(
        name: String,
        desc: String,
        schoolSubjectId: Int,
        link: String,
        file: File?,
        grade: Int? = null,
        classId: Int? = null,
        major: Int? = null
    ) =
        api.createTheory(
            mutableMapOf(
                "name" to name.toRequestBody("text/plain".toMediaTypeOrNull()),
                "description" to desc.toRequestBody("text/plain".toMediaTypeOrNull()),
                "link" to link.toRequestBody("text/plain".toMediaTypeOrNull()),
                "school_subject_id" to schoolSubjectId.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
            ).apply {
                grade?.let {
                    put(
                        "grade",
                        it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    )
                }
                classId?.let {
                    put(
                        "school_classes_id",
                        it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    )
                }
                major?.let {
                    put(
                        "school_major_id",
                        it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    )
                }
            },
            file?.let {
                MultipartBody.Part.createFormData(
                    "file",
                    it.name,
                    it.asRequestBody(
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension)
                            ?.toMediaTypeOrNull()
                    )
                )
            }
        )

//    suspend fun teacherAssignment(
//        take: Int,
//        skip: Int,
//        teacherId: Int,
//        classId: Int? = null,
//        mapelId: Int? = null
//    ) = api.teacherAssignment(
//        hashMapOf(
//            "take" to take,
//            "skip" to skip,
//            "filter[0][0]" to "teacher_id",
//            "filter[0][1]" to "=",
//            "filter[0][2]" to teacherId
//        ).apply {
//            classId?.let {
//                putAll(
//                    mapOf(
//                        "filter[1]" to "and",
//                        "filter[2][0]" to "school_classes_id",
//                        "filter[2][1]" to "=",
//                        "filter[2][2]" to it
//                    )
//                )
//            }
//
//            mapelId?.let {
//                putAll(
//                    mapOf(
//                        "filter[3]" to "and",
//                        "filter[4][0]" to "school_subject_id",
//                        "filter[4][1]" to "=",
//                        "filter[4][2]" to it
//                    )
//                )
//            }
//        }
//    )

    suspend fun scoreAssignment(colledtedId: Int, assignmentId: Int, score: Int) =
        api.scoreAssignment(
            colledtedId, assignmentId, mapOf("score" to score)
        )

    suspend fun startClass(
        scheduleId: Int, pass: String, late: Int,
        lat: Double,
        long: Double
    ) = api.startClass(
        mapOf(
            "school_subject_schedule_id" to scheduleId,
//            "password" to pass,
//            "late_limit" to late,
            "lat" to lat,
            "lng" to long,
        )
    )

    suspend fun attendClass(scheduleId: Int, lat: Double, long: Double) = api.attendClass(
        mapOf(
            "school_subject_schedule_id" to scheduleId,
            "lat" to lat,
            "lng" to long,
//            "password" to pass
        )
    )

    suspend fun endClass(scheduleId: Int) = api.endClass(
        mapOf("school_subject_schedule_id" to scheduleId)
    )

    suspend fun leaveClass(scheduleId: Int) =
        api.leaveClass(mapOf("school_subject_schedule_id" to scheduleId))

    suspend fun downloadExam(id: String, isShowCorrect: Boolean = false) =
        api.downloadExam(id, mapOf("is_show_correct" to isShowCorrect))

    suspend fun startExam(id: String, password: String) =
        api.startExam(id, mapOf("password" to password))

    suspend fun answerExam(id: String) = api.answerExam(
        id, mapOf("answer" to persistentDB.ujian().getUjianAnswer(id.toInt())
            .map {
                mutableMapOf<String, Any>(
                    "exam_question_id" to it.qId
                ).apply {
                    if (it.answerId > 0)
                        put("exam_answer_choice_id", it.answerId)
                    else
                        put("essay_answer", it.answerEssay)
                }
            })
    )

    suspend fun payInvoice(paymentCode: String?, ids: Array<String>) = api.payInvoice(
        mapOf(
            "payment_method" to paymentCode,
            "school_fee_invoice" to ids.map { mapOf("id" to it) }
        )
    )

    suspend fun paySpp(transaction_id: String, pin: String?, channel: String?) = api.paySpp(
        mapOf(
            "transaction_id" to transaction_id,
            "pin" to pin,
            "channel" to channel
        )
    )

    suspend fun paySppChannel(transaction_id: String, pin: String?, channel: String? = "") =
        api.paySppChannel(
            mapOf(
                "transaction_id" to transaction_id,
                "pin" to pin,
                "channel" to channel
            )
        )

    suspend fun updateFcm() = try {
        api.updateFcm(
            mapOf(
                "uuid" to pref.getString("user_uuid"),
                "user_fcm_token" to pref.getString("token")
            )
        )
    } catch (e: Exception) {
        Timber.e(e)
    }

    fun updateFcmAsync() =
        api.updateFcmAsync(
            mapOf(
                "uuid" to pref.getString("user_uuid"),
                "user_fcm_token" to pref.getString("token")
            )
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {}
            override fun onFailure(call: Call<String>, t: Throwable) {}
        })

    val addToCartResponse by lazy { MutableLiveData<Boolean>() }
    suspend fun addToCart(productId: Int, quantity: Int) = try {
        api.addToCart(mapOf("enterpreneur_goodies_id" to productId, "quantity" to quantity))
        addToCartResponse.postValue(true)
    } catch (e: Exception) {
        Timber.e(e)
        addToCartResponse.postValue(false)
        errorString.postValue(e.message)
    }

    suspend fun updateCart(productId: Int, quantity: Int) =
        api.updateCart(productId, mapOf("quantity" to quantity))
}