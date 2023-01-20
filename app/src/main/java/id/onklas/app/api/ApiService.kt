package id.onklas.app.api

import id.onklas.app.di.modules.LogFile
import id.onklas.app.di.modules.Timeout
import id.onklas.app.pages.akm.AkmDownloadResponse
import id.onklas.app.pages.akm.DetailAkmResponse
import id.onklas.app.pages.akm.ListAkmResponse
import id.onklas.app.pages.akun.acceptPairingResponse
import id.onklas.app.pages.akun.listPairingResponse
import id.onklas.app.pages.announcement.AnnouncementResponse
import id.onklas.app.pages.chat.ContactResponse
import id.onklas.app.pages.entrepreneurs.*
import id.onklas.app.pages.homework.*
import id.onklas.app.pages.klaspay.aktivasi.KlaspayCheckResponse
import id.onklas.app.pages.klaspay.aktivasi.KlaspayWalletResponse
import id.onklas.app.pages.klaspay.aktivasi.ResetPinResponse
import id.onklas.app.pages.klaspay.riwayat.KlaspayHistoryResponse
import id.onklas.app.pages.klaspay.tagihan.KlaspayInvoiceResponse
import id.onklas.app.pages.klaspay.tagihan.KlaspayTagihanSppResult
import id.onklas.app.pages.klaspay.topup.KlaspayToolbarResponse
import id.onklas.app.pages.klaspay.topup.KlaspayTopupInqResponse
import id.onklas.app.pages.login.LoginResponse
import id.onklas.app.pages.login.SekolahResponse
import id.onklas.app.pages.login.SessionResponse
import id.onklas.app.pages.login.UserResponse
import id.onklas.app.pages.magang.MagangReportResp
import id.onklas.app.pages.magang.MagangResponse
import id.onklas.app.pages.partisipasi.ListPartisipasiDetailResponse
import id.onklas.app.pages.partisipasi.ListPartisipasiResponse
import id.onklas.app.pages.pembayaran.CheckoutResponse
import id.onklas.app.pages.pembayaran.GuidanceResponse
import id.onklas.app.pages.pembayaran.PaymentTypeResponse
import id.onklas.app.pages.pembayaran.spp.SppProcessListResponse
import id.onklas.app.pages.pembayaran.spp.SppResponse
import id.onklas.app.pages.perpus.BookRentResponse
import id.onklas.app.pages.perpus.BookResponse
import id.onklas.app.pages.perpus.BookStockRespones
import id.onklas.app.pages.perpus.PerpusBannerResponse
import id.onklas.app.pages.ppob.InqResponse
import id.onklas.app.pages.ppob.air.PdamResponse
import id.onklas.app.pages.ppob.bayar.KlaspayBayarResponse
import id.onklas.app.pages.ppob.listrik.ListPlnResponse
import id.onklas.app.pages.ppob.pulsa.ListGameResponse
import id.onklas.app.pages.ppob.pulsa.ListProviderResponse
import id.onklas.app.pages.presensi.*
import id.onklas.app.pages.prokes.*
import id.onklas.app.pages.sekolah.sosmed.*
import id.onklas.app.pages.sekolah.store.*
import id.onklas.app.pages.studentcard.TemplateResponse
import id.onklas.app.pages.theory.*
import id.onklas.app.pages.tryout.ListTryoutResponse
import id.onklas.app.pages.ujian.DownloadSoalResponse
import id.onklas.app.pages.ujian.TestDetailResponse
import id.onklas.app.pages.ujian.TestStudentResponse
import id.onklas.app.viewmodels.CheckVersionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiService {

    @Headers("Accept: */*")
    @GET
    suspend fun download(@Url url: String): ResponseBody

    @GET
    suspend fun downloadString(@Url url: String): Any

    @GET("mobile/app/config/check-android-version")
    suspend fun checkVersion(): CheckVersionResponse

    @GET("mobile/app/authentication/schools")
    suspend fun listSekolah(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("name") search: String = ""
    ): SekolahResponse

    @POST("mobile/app/authentication/check-account")
    suspend fun checkAccount(@Body data: Any): UserResponse

    @POST("mobile/app/authentication/login-account")
    suspend fun login(@Body data: Any): LoginResponse

    @GET("mobile/gmail/verify/{google_token}")
    suspend fun verifyEmail(@Path("google_token") google_token: String): Any

    @DELETE("logout")
    suspend fun logout()

    @DELETE("sessions/others")
    suspend fun logoutOthers()

    @DELETE("sessions/{id}")
    suspend fun logoutDevice(@Path("id") id: Int)

    @GET("sessions")
    suspend fun listSessions(
        @Query("limit") limit: Int = 10,
        @Query("take") take: Int = 10,
        @Query("skip") skip: Int = 0
    ): SessionResponse

    @POST("sosmed/setting/setup-user-fcm")
    suspend fun updateFcm(@Body data: Any): Any

    @POST("sosmed/setting/setup-user-fcm")
    fun updateFcmAsync(@Body data: Any): Call<String>

    @GET("mobile/app/accounts/user/{userId}")
    suspend fun getUser(@Path("userId") userId: Int): GetUserResp

    @GET("mobile/app/accounts/get-username/{username}")
    suspend fun getUsername(@Path("username") username: String?): FeedUsernameResponse

    @GET("mobile/app/accounts/user/{userId}/feed-post")
    suspend fun getUserPost(
        @Path("userId") userId: Int?,
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): FeedResponse

    @GET("mobile/app/accounts/user/{userId}/feed-image")
    suspend fun getUserImage(
        @Path("userId") userId: Int?,
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): FeedResponse

    @GET("mobile/app/accounts/user/{userId}/feed-ebook")
    suspend fun getUserEbook(
        @Path("userId") userId: Int?,
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): FeedResponse

    @Multipart
    @POST("mobile/app/accounts/user/{userUuid}/change-avatar")
    suspend fun uploadProfilePicture(
        @Path("userUuid") userUuid: String,
        @Part file: MultipartBody.Part?
    ): Any

    @PUT("mobile/app/accounts/user/{userUuid}/change-profile")
    suspend fun updateAccount(@Path("userUuid") userUuid: String, @Body data: Any): Any

    @GET("mobile/email/verify")
    suspend fun sendEmailVerification(): Any

    @PUT("mobile/app/accounts/user/{userUuid}/change-password")
    suspend fun changePassword(@Path("userUuid") userUuid: String, @Body data: Any): Any

    @POST("mobile/app/authentication/reset-password")
    suspend fun resetPass(@Body data: Any): Any

    @GET("mobile/app/accounts/search-username")
    suspend fun searchUsername(@Query("params") search: String): FeedUserResponse

    @Multipart
    @POST("mobile/app/schools/feed-posts")
    suspend fun createPost(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: MultipartBody.Part?
    ): Any

    @Multipart
    @POST("mobile/app/schools/feed-ebooks")
    suspend fun createEbook(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: List<MultipartBody.Part>?
    ): Any

    @GET("mobile/app/schools/feed-posts")
    suspend fun feeds(@Query("take") take: Int, @Query("skip") skip: Int): FeedResponse

    @GET("mobile/app/schools/feed-ebooks")
    suspend fun ebooks(@Query("take") take: Int, @Query("skip") skip: Int): FeedResponse

    @GET("mobile/app/schools/feeds/{feedId}")
    suspend fun feedDetail(@Path("feedId") feedId: Int): FeedSingleResponse

    @GET("mobile/app/schools/feeds/{feedId}/comment")
    suspend fun feedComment(
        @Path("feedId") feedId: Int,
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): FeedCommentResponse

    @GET("mobile/app/schools/feeds/{feedId}/like")
    suspend fun feedLike(
        @Path("feedId") feedId: Int,
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): FeedLikeResponse

    @POST("mobile/app/schools/feed-posts/send-like")
    suspend fun likeFeed(@Body data: Any): Any

    @DELETE("mobile/app/schools/feed-posts/{feedId}/unlike")
    suspend fun unlikeFeed(@Path("feedId") feedId: Int): Any

    @POST("mobile/app/schools/feed-posts/{feedId}/comment")
    suspend fun commentFeed(@Path("feedId") feedId: Int, @Body data: Any): Any

    @DELETE("mobile/app/schools/feeds/{feedId}")
    suspend fun deleteFeed(@Path("feedId") feedId: Int): Any

    @GET("mobile/app/schools/explore/feed")
    suspend fun exploreFeed(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("params") params: String = ""
    ): FeedResponse

    @GET("mobile/app/schools/explore/user")
    suspend fun exploreUser(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("params") params: String = ""
    ): FeedUserResponse

    @GET("mobile/app/schools/explore/hastag")
    suspend fun exploreHashtag(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("params") params: String = ""
    ): HashtagResponse

    @GET("mobile/app/learning/announcements")
    suspend fun announcement(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): AnnouncementResponse

    @GET("mobile/app/learning/theories/students/subjects")
    suspend fun studentSubject(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): MapelResponse

    @GET("mobile/app/learning/theories/students/subjects/{subjectId}/theories")
    suspend fun studentTheoryBySubject(
        @Path("subjectId") subjectId: Int,
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): MateriResponse

    @GET("mobile/app/learning/theories/students/subjects/{subjectId}/theories/{theoryId}")
    suspend fun studentTheoryDetail(
        @Path("subjectId") subjectId: Int, @Path("theoryId") theoryId: Int
    ): DetailMateriResponse

    @GET("mobile/teacher/school-class-room")
    suspend fun teacherClassRoom(): ClassRoomResponse

    @GET("mobile/app/learning/theories/teachers/subjects")
    suspend fun teacherSubject(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): MapelResponse

    @GET("mobile/app/learning/theories/teachers/subjects")
    suspend fun teacherSubjectTeach(): MapelResponse

    @GET("mobile/teacher/school-subject")
    suspend fun teacherSubjectTeach2(): MapelResponse

    @GET("mobile/app/learning/theories/teachers/school-majors")
    suspend fun teacherMajor(@Query("take") take: Int = 1000): MajorResponse

    @GET("mobile/app/learning/theories/teachers/theories")
    suspend fun teacherTheory(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("school_subject") school_subject: Int? = null,
        @Query("school_class") school_class: Int? = null
    ): MateriResponse

    @GET("mobile/app/learning/theories/teachers/subjects/{subjectId}/theories")
    suspend fun teacherTheoryBySubject(
        @Path("subjectId") subjectId: Int,
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): MateriResponse

    @GET("mobile/app/learning/theories/teachers/subjects/{subjectId}/theories/{theoryId}")
    suspend fun teacherTheoryDetail(
        @Path("subjectId") subjectId: Int, @Path("theoryId") theoryId: Int
    ): DetailMateriResponse

    @Multipart
    @POST("mobile/app/learning/theories/teachers/theories")
    suspend fun createTheory(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: MultipartBody.Part?
    ): Any

    @Multipart
    @POST("mobile/app/learning/theories/teachers/theories/update/{id}")
    suspend fun updateTheory(
        @Path("id") id: Int,
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: MultipartBody.Part?
    ): Any

    @DELETE("mobile/app/learning/theories/teachers/theories/{id}")
    suspend fun deleteTheory(@Path("id") id: Long): Any

    @GET("mobile/teacher/school-grade")
    suspend fun gradeTeach(): GradeResponse

    @GET("mobile/app/learning/assignment/students/backlog")
    suspend fun studentTaskTodo(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): HomeworkResponse

    @GET("mobile/app/learning/assignment/students/done")
    suspend fun studentTaskDone(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): HomeworkResponse

    @GET("mobile/app/learning/assignment/students/scored")
    suspend fun studentTaskScored(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): HomeworkResponse

    @Multipart
    @POST("mobile/app/learning/assignment/students/{id}/collect")
    suspend fun answerHomework(
        @Path("id") id: Int,
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: MultipartBody.Part?
    ): Any

    @GET("mobile/app/learning/assignment/teachers/class")
    suspend fun assignmentClass(): ClassRoomResponse

    @GET("mobile/app/learning/assignment/teachers/schedule-day")
    suspend fun assignmentDay(): AssignmentDayResp

    @GET("mobile/app/learning/assignment/teachers/schedule")
    suspend fun assignmentScheduleClass(
        @Query("school_class_id") classId: Int,
        @Query("day") day: String
    ): AssignmentScheduleResp

    @Multipart
    @POST("mobile/app/learning/assignment/teachers/create")
    suspend fun createAssignment(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: MultipartBody.Part?
    ): Any

    @Multipart
    @POST("mobile/app/learning/assignment/teachers/update/{id}")
    suspend fun updateAssignment(
        @Path("id") id: Int,
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: MultipartBody.Part?
    ): Any

    @DELETE("mobile/app/learning/assignment/teachers/delete/{id}")
    suspend fun deleteAssignment(@Path("id") id: Int): Any

    @GET("mobile/app/learning/assignment/teachers/backlog")
    suspend fun teacherAssignment(
        @QueryMap params: Map<String, @JvmSuppressWildcards Any?>?
    ): HomeworkResponse

    @GET("mobile/app/learning/assignment/teachers/scored")
    suspend fun assignmentCollected(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): HomeworkCollectedResponse

    @POST("mobile/app/learning/assignment/teachers/scored/{colledtedId}/student-assignment/{assignmentId}")
    suspend fun scoreAssignment(
        @Path("colledtedId") colledtedId: Int,
        @Path("assignmentId") assignmentId: Int,
        @Body data: Any
    ): Any

    @GET("mobile/app/learning/assignment/teachers/scored/{id}")
    suspend fun assignmentDetail(@Path("id") id: Int): AssignmentResponse

    @GET("mobile/attendance/schedule")
    suspend fun schedule(@Query("date") date: String): PresensiListResponse

    @GET("mobile/attendance/data/{month}/{year}")
    suspend fun attendance(
        @Path("month") month: Int?,
        @Path("year") year: Int?
    ): AbsensiResponse

    @GET("mobile/attendance/summary/{year}")
    suspend fun attendanceSummary(
        @Path("year") year: Int?
    ): RekapAbsensiResponse

    @GET("mobile/attendance/schedule/{id}")
    suspend fun attendanceSchedule(
        @Path("id") id: Int,
        @Query("date") date: String
    ): ScheduleDetailResponse

    @POST("mobile/attendance/start")
    suspend fun startClass(@Body data: Any): Any

    @POST("mobile/attendance/attend")
    suspend fun attendClass(@Body data: Any): Any

    @POST("mobile/attendance/end")
    suspend fun endClass(@Body data: Any): Any

    @POST("mobile/attendance/leave")
    suspend fun leaveClass(@Body data: Any): Any

    @GET("mobile/attendance/student/by-month")
    suspend fun studentMonthlyAttendance(
        @Query("year") year: Int?,
        @Query("month") month: Int?
    ): AbsensiResponse

    @GET("mobile/attendance/student/by-year")
    suspend fun studentAnnualAttendance(@Query("year") year: Int): RekapAbsensiResponse

    @GET("mobile/attendance/staff/by-month")
    suspend fun teacherMonthlyAttendance(
        @Query("year") year: Int?,
        @Query("month") month: Int?
    ): AbsensiResponse

    @GET("mobile/attendance/staff/by-year")
    suspend fun teacherAnnualAttendance(@Query("year") year: Int): RekapAbsensiResponse

    @GET("mobile/attendance/staff/check")
    suspend fun teacherCheckPresensi(): CheckAbsenResponse

    @POST("mobile/attendance/staff/check-in")
    suspend fun teacherCheckIn(@Body data: Any): Any

    @POST("mobile/attendance/staff/check-out")
    suspend fun teacherCheckOut(@Body data: Any): Any

    @GET("mobile/attendance/student/check")
    suspend fun studentCheckPresensi(): CheckAbsenResponse

    @POST("mobile/attendance/student/check-in")
    suspend fun studentCheckIn(@Body data: Any): Any

    @POST("mobile/attendance/student/check-out")
    suspend fun studentCheckOut(@Body data: Any): Any

    @GET("mobile/attendance/{calender}/class-recap/{class}")
    suspend fun recapAbsensi(
        @Path("calender") calender: String,
        @Path("class") classId: Int
    ): PresensiClassRekapResponse

    @GET("mobile/app/learning/examinations/students/exams-list")
    suspend fun listExam(
        @Query("date") date: String,
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): TestStudentResponse

    @GET("mobile/app/learning/examinations/students/exams-scored")
    suspend fun examScored(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): TestStudentResponse

    @LogFile
    @PUT("mobile/app/learning/examinations/students/exams/{id}/download")
    suspend fun downloadExam(
        @Path("id") id: String, @Body data: Any
    ): DownloadSoalResponse

    @GET("mobile/app/learning/examinations/students/exams/{id}/detail")
    suspend fun detailExam(
        @Path("id") id: String,
        @Query("is_show_correct") showCorrect: Int = 1
    ): TestDetailResponse

    @PUT("mobile/app/learning/examinations/students/exams/{id}/start")
    suspend fun startExam(
        @Path("id") id: String, @Body data: Any
    ): Any

    @LogFile
    @POST("mobile/app/learning/examinations/students/exams/{id}/answer")
    suspend fun answerExam(
        @Path("id") id: String, @Body data: Any
    ): Any

    @LogFile
    @PUT("mobile/app/learning/examinations/students/exams/{id}/stop")
    suspend fun endExam(@Path("id") id: String): Any

    @PUT("mobile/app/learning/examinations/students/exams/{id}/stop")
    fun endExamAsync(@Path("id") id: String): Call<Any>

    @GET("transaction/school-invoice/unpaid")
    suspend fun unpaidInvoice(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): SppResponse

    @GET("transaction/school-invoice/paid")
    suspend fun paidInvoice(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): SppResponse

    @GET("transaction/school-invoice/process")
    suspend fun processInvoice(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): SppProcessListResponse

    @Timeout(5, TimeUnit.MINUTES)
    @POST("transaction/school-invoice/pay")
    suspend fun payInvoice(@Body data: Any): CheckoutResponse

    @GET("transaction/payment-service")
    suspend fun paymentServices(): PaymentTypeResponse

    @GET("mobile/app/learning/pustaka/students/homepage-banner")
    suspend fun perpusBanner(): PerpusBannerResponse

    @GET("mobile/app/learning/pustaka/students/homepage-book-newest")
    suspend fun bookNewest(): BookResponse

    @GET("mobile/app/learning/pustaka/students/homepage-book-famous")
    suspend fun bookBest(): BookResponse

    @GET("mobile/app/learning/pustaka/students/search-books")
    suspend fun searchBook(@Query("name") name: String): BookResponse

    @GET("mobile/app/learning/pustaka/students/books/{id}/available")
    suspend fun bookStock(@Path("id") id: Int): BookStockRespones

    @GET("mobile/app/learning/pustaka/students/rent-archives")
    suspend fun rentHistory(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): BookRentResponse

    @GET("mobile/app/learning/pustaka/students/rents")
    suspend fun rentOngoing(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): BookRentResponse

    @GET("mobile/app/accounts/user/{userId}/get-layout-policy")
    suspend fun policy(@Path("userId") userId: Int): PolicyResponse

    @GET("mobile/app/accounts/user/{userId}/get-layout-about")
    suspend fun about(@Path("userId") userId: Int): PolicyResponse

    // klaspay

    @POST("mobile/app/klaspay/check")
    suspend fun klaspayCheck(): KlaspayCheckResponse

    @POST("mobile/app/klaspay/activate")
    suspend fun klaspayActivate(@Body data: Any)

    //    @Headers("Authorization: Bearer 1927|2BLVfERX5XoDhs0n01UFzFqFGCWpPAtmyf8uq7rFlSYnAExdJwJ4y2hOH8jIxX2nKONhelpGlu03R7Cl")
    @GET("klaspay/wallet")
    suspend fun klaspayWallet(): KlaspayWalletResponse

    @GET("klaspay/biller/getToolbar")
    suspend fun klaspayToolbar(): KlaspayToolbarResponse

    @POST("klaspay/reset-pin")
    suspend fun resetPinKlaspay(@Body data: Any): ResetPinResponse

    @POST("klaspay/reset-pin/setpin")
    suspend fun setPinKlaspay(@Body data: Any): ResetPinResponse

    @POST("klaspay/transaction/topup_inq")
    suspend fun klaspayTopupInq(@Body data: Any): KlaspayTopupInqResponse

//    @POST("klaspay/transaction/topup_trx")
//    suspend fun klaspayTopupTrx(@Body data: Any): KlaspayTopupTrxResponse

    @POST("klaspay/transaction/topup_trx")
    suspend fun klaspayTopupTrx(@Body data: Any): KlaspayBayarResponse

    @GET("klaspay/transaction/invoice")
    suspend fun klaspayInvoice(): KlaspayInvoiceResponse

    @POST("klaspay/transaction/spp_cancel_invoice")
    suspend fun klaspayCancelInvoice(@Body data: Any): Any

    @POST("klaspay/transaction/transaction_spp")
    suspend fun klaspaySppInq(@Body data: Any): Any

    @GET("klaspay/transaction/history")
    suspend fun klaspayHistory(): KlaspayHistoryResponse

    @GET("klaspay/transaction/history/id/{id}")
    suspend fun klaspayHistoryDetail(@Path("id") id: String?): InqResponse

    @GET("klaspay/biller/getToolbarSpp")
    suspend fun sppPaymentChannels(): KlaspayToolbarResponse

    @GET("klaspay/transaction/history_school")
    suspend fun sppInvoice(): KlaspayTagihanSppResult

    @POST("klaspay/transaction/spp_trx")
    suspend fun paySpp(@Body data: Any): Any

    @POST("klaspay/transaction/spp_trx")
    suspend fun paySppChannel(@Body data: Any): KlaspayBayarResponse

    @GET("klaspay/transaction/guide/channel/{channelCode}")
    suspend fun payGuide(@Path("channelCode") channelCode: String): GuidanceResponse

    @POST("klaspay/transaction/bill_trx")
    suspend fun payBill(@Body data: Any): Any

    @GET("klaspay/product/list/pulsa/{phone}")
    suspend fun providerPulsa(@Path("phone") phone: String): ListProviderResponse

    @GET("klaspay/product/list/pulsa_pasca/{phone}")
    suspend fun providerPulsaPasca(@Path("phone") phone: String): ListProviderResponse

    @POST("klaspay/transaction/ppob_trx")
    suspend fun buyPulsa(@Body data: Any): InqResponse

    @GET("klaspay/product/list/pln")
    suspend fun listTokenListrik(): ListPlnResponse

    @GET("klaspay/product/list/pdam")
    suspend fun listPdam(): PdamResponse

    @GET("klaspay/product/list/internet")
    suspend fun listInternet(): PdamResponse

    @GET("klaspay/product/list/bpjs")
    suspend fun listBpjs(): PdamResponse

    @GET("klaspay/product/list/game")
    suspend fun listGame(): ListGameResponse

    @POST("klaspay/transaction/ppob_inq")
    suspend fun ppobInquiry(@Body data: Any): InqResponse

    @POST("klaspay/transaction/ppob_inq_pay")
    suspend fun ppobPay(@Body data: Any): InqResponse

    @GET("mobile/app/learning/akm/schedules")
    suspend fun listAkm(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): ListAkmResponse

    @GET("mobile/app/learning/akm/scored")
    suspend fun listScoreAkm(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): ListAkmResponse

    @GET("mobile/app/learning/akm/schedules/{id}")
    suspend fun detailAkm(@Path("id") id: Int): DetailAkmResponse

    @GET("mobile/app/learning/akm/exam-schedules/{id}")
    suspend fun detailUjianSekolah(@Path("id") id: Int): DetailAkmResponse

    @GET("mobile/app/learning/akm/student/exam/{id}/review")
    suspend fun reviewAkm(@Path("id") id: Int): Any

    @GET("mobile/app/learning/try-out/scored/{id}")
    suspend fun reviewTryout(@Path("id") id: Int): Any

    @GET("mobile/app/learning/akm/student/exam/{id}/download")
    suspend fun downloadSoalAkm(@Path("id") id: Int): AkmDownloadResponse

    @GET("mobile/app/learning/akm/student/exam-school/{id}/download")
    suspend fun downloadSoalUjianSchool(@Path("id") id: Int): AkmDownloadResponse

    @GET("mobile/app/learning/try-out/student/training/{id}/download")
    suspend fun downloadSoalUjianTryout(@Path("id") id: Int): AkmDownloadResponse

    @LogFile
    @POST("mobile/app/learning/akm/student/exam/{akm_id}/answer/{student_id}")
    suspend fun uploadJawabanAkm(
        @Path("akm_id") akm_id: Int,
        @Path("student_id") student_id: Int?,
        @Body data: Any
    ): Any

    @LogFile
    @POST("mobile/app/learning/akm/student/exam-school/{akm_id}/answer/{student_id}")
    suspend fun uploadJawabanUjianSchool(
        @Path("akm_id") akm_id: Int,
        @Path("student_id") student_id: Int?,
        @Body data: Any
    ): Any

    @LogFile
    @POST("mobile/app/learning/try-out/student/training/{akm_id}/answer/{student_id}")
    suspend fun uploadJawabanTryOut(
        @Path("akm_id") akm_id: Int,
        @Path("student_id") student_id: Int?,
        @Body data: Any
    ): Any

    @GET("mobile/app/learning/akm/exam-schedules")
    suspend fun listUjian(
        @Query("date") date: String,
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): ListAkmResponse

    @GET("mobile/app/learning/try-out/schedules")
    suspend fun listTryOutSchedule(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): ListTryoutResponse

    @GET("mobile/app/learning/try-out/scored")
    suspend fun listTryOutScored(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): ListTryoutResponse

    @GET("mobile/app/learning/akm/exam-schedules-scored")
    suspend fun listUjianScored(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): ListAkmResponse

    // home store start

    @GET("mobile/enterpreneur/homepage")
    suspend fun loadHomepage(): HomepageResponse

    @GET("mobile/enterpreneur/category")
    suspend fun loadCategory(): CategoryResponse

    @GET("mobile/enterpreneur/category/{categoryId}/detail")
    suspend fun loadCategorySub(
        @Path("categoryId") categoryId: Int,
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): CategorySubResponse

//    @GET("mobile/enterpreneur/goodies/categories/{categoryId}/subcategories/{categorySubId}?take=10&skip=0&sub_category_id=9")

    @GET("mobile/enterpreneur/goodies/categories/{categoryId}{categorySubId}")
    suspend fun loadCategoryProduct(
        @Path("categoryId") categoryId: Int,
        @Path("categorySubId") categorySubId: String,
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): CategoryProductResponse


    //Goodie Card Detail
    // Endpoint untuk menampilkan detail card pada homepage
    //    sekolah_lain|terpopuler|terbaru|terlaris

    @GET("mobile/enterpreneur/card")
    suspend fun loadGoodieCartDetail(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("filter") filter: String
    ): GoodieCardDetailResponse

    @GET("mobile/enterpreneur/goodies/{goodieId}/detail")
    suspend fun loadGoodieDetail(
        @Path("goodieId") goodieId: Int
    ): GoodieDetailResponse

    @GET("mobile/enterpreneur/goodies/{goodieId}/review")
    suspend fun loadGoodieDetailReview(
        @Path("goodieId") goodieId: Int
    ): DetailReviewResponse

    @GET("mobile/enterpreneur/goodies/{goodieId}/listreview")
    suspend fun loadGoodieReview(
        @Path("goodieId") goodieId: Int,
        @Query("star") star: String // all|1|2|3|4|5
    ): ListReviewResponse


    //filter goodie store
    @GET("mobile/enterpreneur/filter/count")
    suspend fun LoadCountProductFilter(
        @Query("filter") filter: String
    ): CountProductFilterResponse

    @GET("mobile/enterpreneur/filter/list")
    suspend fun LoadResultFilterGoodies(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("filter") filter: String
    ): ResultGooidesFilterResponse

    @GET("mobile/enterpreneur/search-merchants")
    suspend fun suggestMerchant(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("keyword") keyword: String
    ): SuggestMerchantResponse

    @GET("mobile/enterpreneur/search-goodies-suggestion")
    suspend fun suggestProduct(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("keyword") keyword: String
    ): SuggestProductResponse


    @GET("mobile/enterpreneur/search")
    suspend fun searchResult(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("keyword") keyword: String
    ): SearchResultResponse


    // merchant goodie
    @GET("mobile/enterpreneur/merchants/{sellerId}")
    suspend fun loadMerchantGoodie(
        @Path("sellerId") sellerId: Int
    ): MerchantResponse

    @GET("mobile/enterpreneur/merchants/{sellerId}/goodies-all")
    suspend fun productMerchantGoodie(
        @Path("sellerId") sellerId: Int,
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): MerchantProductResponse

    @GET("mobile/enterpreneur/merchants/{sellerId}/goodies-best-seller")
    suspend fun productBestSellerMerchantGoodie(
        @Path("sellerId") sellerId: Int,
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): MerchantProductResponse

    @GET("mobile/enterpreneur/merchants/{sellerId}/summary")
    suspend fun summaryMerchantGoodie(
        @Path("sellerId") sellerId: Int
    ): MerchantSummary

    // mercahnt user
    @GET("mobile/enterpreneur/merchants/account/profile")
    suspend fun loadMerchantUser(): MerchantResponse

    @POST("mobile/enterpreneur/merchants")
    suspend fun createMerchantUser(
        @Query("name") name: String
    ): MerchantResponse

    @PUT("mobile/enterpreneur/merchants/account/profiles")
    suspend fun editMerchantUser(
        @Query("name") name: String
    ): MerchantResponse

    @Multipart
    @POST("mobile/enterpreneur/merchants/account/profiles/image")
    suspend fun editImgMerchantUser(
        @Part file: MultipartBody.Part?
    ): MerchantResponse


    @GET("mobile/enterpreneur/merchants/account/profile/goodies-all")
    suspend fun productMerchantUser(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): MerchantProductResponse

    @GET("mobile/enterpreneur/merchants/account/profile/goodies-best-seller")
    suspend fun productBestSellerMerchantUser(
        @Query("take") take: Int,
        @Query("skip") skip: Int
    ): MerchantProductResponse

    @GET("mobile/enterpreneur/merchants/account/profile/summary")
    suspend fun summaryMerchant(): MerchantSummary

    @GET("mobile/enterpreneur/merchants/account/profile/summary-purchase")
    suspend fun summaryMerchantPembelian(): MerchantPembelianSummary


    @GET("mobile/enterpreneur/merchants/account/transactions/incoming")
    suspend fun IncomingOrder(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("date") date: String
    ): TransaksiResponse


    // buyer
    @GET("mobile/enterpreneur/merchants/account/transactions/processed")
    suspend fun ProcessedTransaksi(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("date") date: String
    ): TransaksiResponse

    @GET("mobile/enterpreneur/merchants/account/transactions/completed")
    suspend fun CompletedTransaksi(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("date") date: String
    ): TransaksiResponse

    //    @GET("mobile/enterpreneur/reviews/transactions/{TransaksiId}/details/{TransaksiSubId}") //  transaksi sub id bukan goody id
//    suspend fun DetailReview(
//            @Part("TransaksiId") TransaksiId:Int,
//            @Part("TransaksiSubId") TransaksiSubId:Int,
//    ): DetailReviewResponse
    @GET("mobile/enterpreneur/merchants/account/reviews")
    suspend fun ReviewTransaksiSeller(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("date") date: String
    ): TransaksiResponse

    @GET("mobile/enterpreneur/reviews")
    suspend fun ReviewTransaksiBuyer(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("date") date: String
    ): TransaksiResponse


    // incoming transaction action
    // My Merchant
    @POST("mobile/enterpreneur/merchants/account/transactions/{TransaksiId}/accept")
    suspend fun acceptTransaction(
        @Path("TransaksiId") TransaksiId: Int
    ): AcceptRejectResponse

    @POST("mobile/enterpreneur/merchants/account/transactions/{TransaksiId}/reject")
    suspend fun rejectTransaction(
        @Path("TransaksiId") TransaksiId: Int
    ): AcceptRejectResponse


    // Buyer

    @GET("https://dev.api.onklas.id/api/mobile/enterpreneur/transactions/{TransaksiId}/accept")
    suspend fun acceptTransaksiBuyer(
        @Path("TransaksiId") TransaksiId: Int
    ): AcceptCancleBuyerResponse

    @GET("mobile/enterpreneur/transactions/{TransaksiId}/cancel")
    suspend fun cancleTransactionBUyer(
        @Path("TransaksiId") TransaksiId: Int
    ): AcceptCancleBuyerResponse


    //histori transaksi pembelian

    @GET("mobile/enterpreneur/transactions/purchases-done")
    suspend fun ListPurchasesDone(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("date") date: String
    ): TransaksiResponse

    @GET("mobile/enterpreneur/transactions/purchases-processed")
    suspend fun ListPurchasesProcess(
        @Query("take") take: Int,
        @Query("skip") skip: Int,
        @Query("date") date: String
    ): TransaksiResponse


//    @POST("mobile/enterpreneur/transactions/awb/{TransaksiId}")
//    suspend fun rejectTransaction(
//            @Path("TransaksiId") TransaksiId: Int,
//    ): TransaksiResponse


    @GET("mobile/enterpreneur/merchants/account/transactions/{transaksiId}")
    suspend fun detailTransaksi(
        @Path("transaksiId") transaksiId: Int
    ): DetailTransaksiResponse

    @GET("mobile/enterpreneur/reviews/transactions/{transaksiId}")
    suspend fun detailReviewBuyerTransaksi(
        @Path("transaksiId") transaksiId: Int
    ): DetailTransaksiResponse

    @GET("mobile/enterpreneur/merchants/account/reviews/transactions/{transaksiId}")
    suspend fun detailReviewSellerTransaksi(
        @Path("transaksiId") transaksiId: Int
    ): DetailTransaksiResponse

    @GET("mobile/enterpreneur/transactions/purchases/{transaksiId}")
    suspend fun detailTransaksiPembelian(
        @Path("transaksiId") transaksiId: Int
    ): DetailTransaksiResponse

    @POST("mobile/enterpreneur/reviews/{goodyReviewId}")
    suspend fun postReviewBuyer(
        @Path("goodyReviewId") goodyReviewId: Int,
        @Query("rating") rating: Int,
        @Query("comment") comment: String
    ): Any

    @POST("mobile/enterpreneur/merchants/account/reviews/{goodyReviewId}")
    suspend fun postReviewSeller(
        @Path("goodyReviewId") goodyReviewId: Int,
        @Query("rating") rating: Int,
        @Query("comment") comment: String
    ): Any


    @GET("mobile/enterpreneur/trackings/{transaksiId}")
    suspend fun trackingDetail(
        @Path("transaksiId") transaksiId: Int
    ): TrackingDetailResponse

    @GET("mobile/enterpreneur/transactions/awb/{transaksiId}")
    suspend fun inputResi(
        @Path("transaksiId") transaksiId: Int,
        @Query("waybill") waybill: String,
        @Query("courier") courier: String
    ): InputResiResponse


    @GET("mobile/enterpreneur/merchants/account/goodies/{goodieId}")
    suspend fun viewGood(@Path("goodieId") goodieId: Int): MyProductResponse

    @Multipart
    @POST("mobile/enterpreneur/merchants/account/goodies/create")
    suspend fun createGood(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part files: List<MultipartBody.Part>?
    ): CreateProductResponse

    @Multipart
    @POST("mobile/enterpreneur/merchants/account/goodies/create/{goodieId}/image")
    suspend fun addImageGood(@Path("goodieId") goodieId: Int, @Part file: MultipartBody.Part): Any

    @Multipart
    @POST("mobile/enterpreneur/merchants/account/goodies/update/{goodieId}/image/{imageId}")
    suspend fun updateImageGood(
        @Path("goodieId") goodieId: Int,
        @Path("imageId") imageId: Int,
        @Part file: MultipartBody.Part
    ): Any

    @PUT("mobile/enterpreneur/merchants/account/goodies/publish/{goodieId}")
    suspend fun publishGood(@Path("goodieId") goodieId: Int, @Body data: Any): Any

    @DELETE("mobile/enterpreneur/merchants/account/goodies/delete/{goodieId}/image/{imageId}")
    suspend fun deleteImageGood(@Path("goodieId") goodieId: Int, @Path("imageId") imageId: Int): Any

    @PUT("mobile/enterpreneur/merchants/account/goodies/update/{goodieId}")
    suspend fun updateGood(
        @Path("goodieId") goodieId: Int,
        @Body data: Any
    ): UpadateProductData

    @DELETE("mobile/enterpreneur/merchants/account/goodies/delete/{goodieId}")
    suspend fun deleteGood(@Path("goodieId") goodieId: Int)

    //cart

    @GET("mobile/enterpreneur/carts")
    suspend fun loadCart(): CartResponse

    @POST("mobile/enterpreneur/carts")
    suspend fun addToCart(@Body data: Any): Any

    @PUT("mobile/enterpreneur/carts/goodies/{goods}")
    suspend fun updateCart(@Path("goods") productId: Int, @Body data: Any): Any

    @DELETE("mobile/enterpreneur/carts/goodies/{goods}")
    suspend fun deleteCart(@Path("goods") productId: Int): Any

    @GET("klaspay/bill/list")
    suspend fun listDanaPartisipasi(): ListPartisipasiResponse

    @GET("klaspay/bill/history/id/{id}")
    suspend fun listPaymentDanaPartisipasi(@Path("id") id: String): ListPartisipasiDetailResponse

    @GET("dana-partisipasi/school/{schoolId}/student")
    suspend fun getListStudentKlaspay(@Path("schoolId") schoolId: String): ContactResponse

    @GET("mobile/enterpreneur/checkouts/shipping-fee-list")
    suspend fun getShipping(@Query("product[]") products: List<Int>): ListShipResponse

    @POST("mobile/enterpreneur/checkouts/transaction-create")
    suspend fun buyProduct(@Body data: Any): Any

    @GET("mobile/app/accounts/student-card-template")
    suspend fun idCardTemplate(): TemplateResponse

    @Multipart
    @POST("mobile/app/accounts/user/student-card")
    suspend fun updateCard(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: MultipartBody.Part?
    ): Any

    @GET("klaspay/merchant/id/{merchantId}")
    suspend fun merchantKlaspay(@Path("merchantId") merchantId: Int): MerchantKlaspayInfoResponse

    @GET("mobile/enterpreneur/location/province")
    suspend fun listProvinces(): ProvinceResponse

    @GET("mobile/enterpreneur/location/city")
    suspend fun listCities(
        @Query("province_id") provinceId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): CityResponse

    @GET("mobile/enterpreneur/location/district")
    suspend fun listDistrict(
        @Query("city_id") cityId: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int
    ): DistrictResponse

    @GET("mobile/app/accounts/user/shipping-address")
    suspend fun getUserAddress(): AddressResponse

    @POST("mobile/app/accounts/user/shipping-address")
    suspend fun setUserAddress(@Body data: Any): Any

    //student prokes

    @GET("mobile/app/learning/health-protocols/student-form-early-detection")
    suspend fun getformProkesStudent(): ResponseEarlyDetectionProkes

    @POST("mobile/app/learning/health-protocols/save-report")
    suspend fun sendProksesStudent(@Body data: Any): ResponseCheckReport

    @GET("mobile/app/learning/health-protocols/check-report")
    suspend fun cekProkesStudent(): ResponseCheckReport

    @GET("mobile/app/learning/health-protocols/check-vaccinated")
    suspend fun cekVaksinasi(): ResponseCheckReport

    @POST("mobile/app/learning/health-protocols/save-vaccinated")
    suspend fun saveVaksinasi(@Body data: Any): ResponseCheckReport

    // teacher prokes

    @GET("mobile/app/learning/health-protocols/check-vaccinated-teacher")
    suspend fun cekVaksinasiTeacher(): ResponseCheckReport

    @POST("mobile/app/learning/health-protocols/save-vaccinated-teacher")
    suspend fun saveVaksinasiTeacher(@Body data: Any): ResponseCheckReport

    @GET("mobile/app/learning/health-protocols/list-school-class-teacher")
    suspend fun listClass(): ListClassResponse

    @GET("mobile/app/learning/health-protocols/list-school-class-student/{idClass}")
    suspend fun listStudent(@Path("idClass") idClass: Int): ListStudentResponse

    @GET("mobile/app/learning/health-protocols/teacher-form-early-detection")
    suspend fun teacherFormEarlyDetetion(): ResponseEarlyDetectionProkes

    @POST("mobile/app/learning/health-protocols/save-history-report/{studentId}")
    suspend fun saveScreening(@Path("studentId") studentId: Int, @Body data: Any): SaveScreeningResp

    @GET("mobile/app/learning/health-protocols/check-screening-student")
    suspend fun screeningCheck(): ScreeningCheckResponse

    @GET("mobile/app/learning/health-protocols/check-history-report/{studentId}")
    suspend fun historyReportStudent(
        @Path("studentId") studentId: Int
    ): ResponseCheckReport

    // pairing

    @GET("mobile/app/accounts/parent/pair-lists")
    suspend fun pairingList(): listPairingResponse

    @POST("mobile/app/accounts/parent/accept-pair/{id}")
    suspend fun acceptPairing(@Path("id") id: Int): acceptPairingResponse

    // magang endpoint
    @GET("mobile/internship/schedule")
    suspend fun magangSchedule(): MagangResponse

    @POST("mobile/internship/attend")
    suspend fun attendMagang(@Body data: Any)

    @POST("mobile/internship/leave")
    suspend fun reportMagang(@Body data: Any)

    @GET("mobile/internship/attendance")
    suspend fun magangReport(): MagangReportResp
}
