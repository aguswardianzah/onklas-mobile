package id.onklas.app.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import id.onklas.app.pages.akm.*
import id.onklas.app.pages.akun.PairingDao
import id.onklas.app.pages.akun.PairingTable
import id.onklas.app.pages.announcement.AnnouncementDao
import id.onklas.app.pages.announcement.AnnouncementTable
import id.onklas.app.pages.chat.ChatDao
import id.onklas.app.pages.chat.ChatItem
import id.onklas.app.pages.chat.ConversationItem
import id.onklas.app.pages.entrepreneurs.*
import id.onklas.app.pages.homework.*
import id.onklas.app.pages.jelajah.ExploreFeedTable
import id.onklas.app.pages.jelajah.JelajahDao
import id.onklas.app.pages.login.LoginDao
import id.onklas.app.pages.login.SekolahItem
import id.onklas.app.pages.login.SessionData
import id.onklas.app.pages.magang.MagangCompany
import id.onklas.app.pages.magang.MagangDao
import id.onklas.app.pages.magang.MagangReportEntity
import id.onklas.app.pages.magang.MagangSchedule
import id.onklas.app.pages.partisipasi.PartisipasiDao
import id.onklas.app.pages.partisipasi.PartisipasiItem
import id.onklas.app.pages.partisipasi.PartisipasiPayment
import id.onklas.app.pages.pembayaran.PaymentDao
import id.onklas.app.pages.pembayaran.PaymentGuideItem
import id.onklas.app.pages.pembayaran.PaymentGuideType
import id.onklas.app.pages.pembayaran.PaymentInvoice
import id.onklas.app.pages.pembayaran.spp.SppDao
import id.onklas.app.pages.pembayaran.spp.SppProcess
import id.onklas.app.pages.pembayaran.spp.SppProcessCrossRef
import id.onklas.app.pages.pembayaran.spp.SppTable
import id.onklas.app.pages.perpus.BookRentTable
import id.onklas.app.pages.perpus.BookTable
import id.onklas.app.pages.perpus.PerpusBanner
import id.onklas.app.pages.perpus.PerpusDao
import id.onklas.app.pages.ppob.PpobDao
import id.onklas.app.pages.ppob.PpobTransaction
import id.onklas.app.pages.presensi.*
import id.onklas.app.pages.prokes.ChoiceTable
import id.onklas.app.pages.prokes.ListClass
import id.onklas.app.pages.prokes.ListStudentItem
import id.onklas.app.pages.prokes.ProkesDao
import id.onklas.app.pages.sekolah.sosmed.*
import id.onklas.app.pages.sekolah.store.*
import id.onklas.app.pages.theory.*
import id.onklas.app.pages.tryout.TryOutDao
import id.onklas.app.pages.ujian.*
import id.onklas.app.socket.SocketDao
import id.onklas.app.socket.SocketQueueItem

@Database(
    version = 142,
    exportSchema = false,
    entities = [
        SekolahItem::class,
        UserTable::class,
        TeacherTable::class,
        ClassRoomTable::class,

        SessionData::class,

        FeedTable::class,
        FeedFileTable::class,
        FeedCommentTable::class,
        FeedUserCrossRef::class,
        ExploreFeedTable::class,
        HashtagTable::class,

        AnnouncementTable::class,

        MapelTable::class,
        MapelTeacherCrossRef::class,

        MateriTable::class,
        MateriLinkTable::class,

        GradeTable::class,
        MajorItem::class,

        SppTable::class,
        SppProcess::class,
        SppProcessCrossRef::class,

        HomeworkTable::class,
        HomeworkLink::class,
        HomeworkCollected::class,

        ScheduleTable::class,
        ScheduleDetailTable::class,
        ScheduleAttendanceTable::class,
        AbsensiTable::class,
        RekapAbsensiTable::class,

        ExamTable::class,
        QuestionTable::class,
        AnswerTable::class,
        MyAnswerTable::class,

        PerpusBanner::class,
        BookTable::class,
        BookRentTable::class,

        PaymentInvoice::class,
        PaymentGuideType::class,
        PaymentGuideItem::class,
        PpobTransaction::class,

        AkmTable::class,
        AkmExamsTable::class,
        AkmInstructionTable::class,
        AkmQuestionTable::class,
        AkmAnswerTable::class,

        PartisipasiItem::class,
        PartisipasiPayment::class,

        ConversationItem::class,
        ChatItem::class,

        SocketQueueItem::class,

        Province::class,
        City::class,
        District::class,

        // store entities
        CategoryTable::class,
        CategorySubTable::class,

        ProductTable::class,
        ProductMerchantTable::class,

        //KWU
        TransaksiTable::class,
        TransaksiProductTable::class,
        DetailTransaksi::class,
        MerchantTable::class,
        MerchantSummaryTable::class,
        TrackingDetail::class,

        CartTable::class,

        ReviewUserTable::class,
        ReviewMerchantTable::class,
        ReviewData::class,

        ListClass::class,
        ListStudentItem::class,
        PairingTable::class,

        ChoiceTable::class,

        MagangCompany::class,
        MagangSchedule::class,
        MagangReportEntity::class
    ]
)
@TypeConverters(DbConverter::class)
abstract class MemoryDB : RoomDatabase() {

    abstract fun login(): LoginDao
    abstract fun feed(): FeedDao
    abstract fun announcement(): AnnouncementDao
    abstract fun theory(): TheoryDao
    abstract fun spp(): SppDao
    abstract fun homework(): HomeworkDao
    abstract fun schedule(): PresensiDao
    abstract fun perpus(): PerpusDao
    abstract fun ujian(): UjianDao
    abstract fun explore(): JelajahDao
    abstract fun payment(): PaymentDao
    abstract fun ppob(): PpobDao
    abstract fun akm(): AkmDao
    abstract fun partisipasi(): PartisipasiDao
    abstract fun chat(): ChatDao
    abstract fun socket(): SocketDao

    abstract fun store(): StoreDao
    abstract fun kwu(): EnterepreneurDao

    abstract fun cart(): CartDao
    abstract fun checkoutAddress(): CheckoutAddressDao

    abstract fun prokes(): ProkesDao
    abstract fun pairing(): PairingDao

    abstract fun tryout(): TryOutDao

    abstract fun magang(): MagangDao
}
