package id.onklas.app.di

import android.app.Activity
import android.app.Service
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.work.CoroutineWorker
import androidx.work.Worker
import com.squareup.moshi.Moshi
import dagger.BindsInstance
import dagger.Component
import id.onklas.app.App
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.PersistentDB
import id.onklas.app.di.modules.ApiModules
import id.onklas.app.di.modules.DbModules
import id.onklas.app.di.modules.MoshiModule
import id.onklas.app.di.modules.PreferenceModule
import id.onklas.app.pages.akm.AkmViewModel
import id.onklas.app.pages.akun.DeviceViewModel
import id.onklas.app.pages.akun.ProfilePictureViewmodel
import id.onklas.app.pages.akun.ProfileViewModel
import id.onklas.app.pages.akun.SettingAkunViewmodel
import id.onklas.app.pages.akun.PairingViewModel
import id.onklas.app.pages.announcement.AnnouncementViewmodel
import id.onklas.app.pages.changepass.ChangePassViewmodel
import id.onklas.app.pages.chat.ChatViewModel
import id.onklas.app.pages.comment.CommentViewModel
import id.onklas.app.pages.createpost.CameraViewModel
import id.onklas.app.pages.createpost.CreatePostViewmodel
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM
import id.onklas.app.pages.entrepreneurs.OrderVM
import id.onklas.app.pages.entrepreneurs.PembelianVM
import id.onklas.app.pages.entrepreneurs.myMerchant.addProduct.AddProductViewModel
import id.onklas.app.pages.entrepreneurs.myMerchant.mystore.ProductViewModel
import id.onklas.app.pages.homework.HomeWorkViewModel
import id.onklas.app.pages.homework.teacher.CreateHomeworkVm
import id.onklas.app.pages.jelajah.JelajahViewModel
import id.onklas.app.pages.klaspay.aktivasi.KlaspayAktivasiViewmodel
import id.onklas.app.pages.klaspay.riwayat.KlaspayRiwayatViewModel
import id.onklas.app.pages.klaspay.tagihan.KlaspayTagihanViewModel
import id.onklas.app.pages.klaspay.topup.KlaspayTopupViewModel
import id.onklas.app.pages.listlike.ListLikeViewModel
import id.onklas.app.pages.login.LoginViewModel
import id.onklas.app.pages.magang.MagangViewModel
import id.onklas.app.pages.partisipasi.PartisipasiViewModel
import id.onklas.app.pages.pembayaran.ConfirmPinViewModel
import id.onklas.app.pages.pembayaran.PaymentTypeViewmodel
import id.onklas.app.pages.pembayaran.PaymentViewModel
import id.onklas.app.pages.pembayaran.spp.SppViewModel
import id.onklas.app.pages.pembelajaran.PembelajaranVm
import id.onklas.app.pages.perpus.PerpusViewModel
import id.onklas.app.pages.ppob.PpobViewModel
import id.onklas.app.pages.ppob.air.AirViewModel
import id.onklas.app.pages.ppob.bayar.KlaspayBayarViewModel
import id.onklas.app.pages.ppob.bpjs.BpjsViewModel
import id.onklas.app.pages.ppob.game.GameViewModel
import id.onklas.app.pages.ppob.internet.InternetViewModel
import id.onklas.app.pages.ppob.listrik.ListrikViewModel
import id.onklas.app.pages.ppob.pulsa.PulsaViewModel
import id.onklas.app.pages.presensi.PresensiViewModel
import id.onklas.app.pages.prokes.ProkesViewmodel
import id.onklas.app.pages.resetpass.ResetPassViewmodel
import id.onklas.app.pages.sekolah.store.StoreVm
import id.onklas.app.pages.sekolah.SosmedViewModel
import id.onklas.app.pages.sekolah.store.CartViewModel
import id.onklas.app.pages.sekolah.store.CheckoutAddressViewModel
import id.onklas.app.pages.sekolah.store.CheckoutStoreViewModel
import id.onklas.app.pages.studentcard.StudentCardVM
import id.onklas.app.pages.theory.TheoryViewModel
import id.onklas.app.pages.theoryteacher.UploadMateriViewmodel
import id.onklas.app.pages.tryout.TryOutViewModel
import id.onklas.app.pages.ujian.UjianViewModel
import id.onklas.app.socket.SocketClass
import id.onklas.app.utils.*
import id.onklas.app.viewmodels.GeneralViewModel
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        MoshiModule::class,
        PreferenceModule::class,
        DbModules::class,
        ApiModules::class,
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance appContext: Context,
            @BindsInstance appInstance: App
        ): AppComponent
    }

    val loginVmFactory: ViewModelFactory<LoginViewModel>
    val generalVmFactory: ViewModelFactory<GeneralViewModel>
    val sosmedVmFactory: ViewModelFactory<SosmedViewModel>
    val storeVmFactory: ViewModelFactory<StoreVm>
    val entrepreneursFactory: ViewModelFactory<EntrepreneursVM>
    val entrepreneursOrderFactory: ViewModelFactory<OrderVM>
    val entrepreneursPembelianFactory: ViewModelFactory<PembelianVM>
    val commentVmFactory: ViewModelFactory<CommentViewModel>
    val createPostVmFactory: ViewModelFactory<CreatePostViewmodel>
    val cameraVmFactory: ViewModelFactory<CameraViewModel>
    val changePassVmFactory: ViewModelFactory<ChangePassViewmodel>
    val studentCardVmFactory: ViewModelFactory<StudentCardVM>
    val listLikeVmFactory: ViewModelFactory<ListLikeViewModel>
    val settingAkunVmFactory: ViewModelFactory<SettingAkunViewmodel>
    val profileVmFactory: ViewModelFactory<ProfileViewModel>
    val profilePicVmFactory: ViewModelFactory<ProfilePictureViewmodel>
    val pembelajaranVmFactory: ViewModelFactory<PembelajaranVm>
    val announcementVmFactory: ViewModelFactory<AnnouncementViewmodel>
    val theoryVmFactory: ViewModelFactory<TheoryViewModel>
    val uploadMateriVmFactory: ViewModelFactory<UploadMateriViewmodel>
    val homeworkVmFactory: ViewModelFactory<HomeWorkViewModel>
    val createHomeworkVmFactory: ViewModelFactory<CreateHomeworkVm>
    val sppVmFactory: ViewModelFactory<SppViewModel>
    val paymentVmFactory: ViewModelFactory<PaymentViewModel>
    val presensiVmFactory: ViewModelFactory<PresensiViewModel>
    val perpusVmFactory: ViewModelFactory<PerpusViewModel>
    val ujianVmFactory: ViewModelFactory<UjianViewModel>
    val resetPassVmFactory: ViewModelFactory<ResetPassViewmodel>
    val jelajahVmFactory: ViewModelFactory<JelajahViewModel>
    val deviceViewModelFactory: ViewModelFactory<DeviceViewModel>
    val klaspayAktivasiVmFactory: ViewModelFactory<KlaspayAktivasiViewmodel>
    val KlaspayTopupVmFactory: ViewModelFactory<KlaspayTopupViewModel>
    val KlaspayTagihanVmFactory: ViewModelFactory<KlaspayTagihanViewModel>
    val KlaspayRiwayatVmFactory: ViewModelFactory<KlaspayRiwayatViewModel>
    val klaspayBayarVmFactory: ViewModelFactory<KlaspayBayarViewModel>
    val klaspayConfirmPinVmFactory: ViewModelFactory<ConfirmPinViewModel>
    val airVmFactory: ViewModelFactory<AirViewModel>
    val listrikVmFactory: ViewModelFactory<ListrikViewModel>
    val pulsaVmFactory: ViewModelFactory<PulsaViewModel>
    val paymentTypeVmFactory: ViewModelFactory<PaymentTypeViewmodel>
    val ppobVmFactory: ViewModelFactory<PpobViewModel>
    val internetVmFactory: ViewModelFactory<InternetViewModel>
    val bpjsVmFactory: ViewModelFactory<BpjsViewModel>
    val gameVmFactory: ViewModelFactory<GameViewModel>
    val partisipasiVmFactory: ViewModelFactory<PartisipasiViewModel>
    val cartVmFactory: ViewModelFactory<CartViewModel>

    val addProductVmFactory: ViewModelFactory<AddProductViewModel>
    val productVmFactory: ViewModelFactory<ProductViewModel>
    val checkoutStoreVmFactory: ViewModelFactory<CheckoutStoreViewModel>
    val checkoutAddresVmFactory: ViewModelFactory<CheckoutAddressViewModel>

    val akmVmFactory: ViewModelFactory<AkmViewModel>
    val chatVmFactory: ViewModelFactory<ChatViewModel>
    val prokesVmFactory: ViewModelFactory<ProkesViewmodel>
    val pairingVmFactory: ViewModelFactory<PairingViewModel>

    val tryoutVmFactory: ViewModelFactory<TryOutViewModel>

    val magangVmFactory: ViewModelFactory<MagangViewModel>

    val preference: PreferenceClass
    val moshi: Moshi
    val memoryDB: MemoryDB
    val persistentDB: PersistentDB
    val intentUtil: IntentUtil
    val utils: Utils
    val stringUtil: StringUtil
    val fileUtils: FileUtils
    val apiWrapper: ApiWrapper
    val apiService: ApiService

    val socketClass: SocketClass
    val notifUtil: NotifUtil
}

val Activity.component get() = (application as App).appComponent
val Fragment.component get() = (requireActivity().application as App).appComponent
val Service.component get() = (application as App).appComponent
val CoroutineWorker.component get() = (applicationContext as App).appComponent
val Worker.component get() = (applicationContext as App).appComponent