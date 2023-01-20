package id.onklas.app.pages.home

import android.content.Intent
import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.messaging.FirebaseMessaging
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.HomePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.AboutPage
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.TermsPage
import id.onklas.app.pages.akm.AkmPage
import id.onklas.app.pages.akun.DevicesPage
import id.onklas.app.pages.akun.ProfileViewModel
import id.onklas.app.pages.akun.SettingAkunPage
import id.onklas.app.pages.akun.SettingContactPage
import id.onklas.app.pages.changepass.ChangePassPage
import id.onklas.app.pages.chat.ChatViewModel
import id.onklas.app.pages.home.dialogs.HomeDialog
import id.onklas.app.pages.login.Loginpage
import id.onklas.app.pages.sekolah.MyImageZoom
import id.onklas.app.pages.sekolah.SosmedViewModel
import id.onklas.app.pages.studentcard.StudentCardPage
import kotlinx.coroutines.launch
import timber.log.Timber

class HomePage : Privatepage() {

    val binding: HomePageBinding by lazy { HomePageBinding.inflate(layoutInflater) }
    private val navController by lazy { findNavController(R.id.form_container) }
    private val viewmodel by viewModels<SosmedViewModel> { component.sosmedVmFactory }
    private val profileVm by viewModels<ProfileViewModel> { component.profileVmFactory }
//    private val chatVm by viewModels<ChatViewModel> { component.chatVmFactory }

    private val gSignInClient by lazy {
        GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build()
        )
    }

    init {
        lifecycleScope.launchWhenCreated {
            // get klaspay wallet data
            try {
                viewmodel.apiService.klaspayWallet().data.let {
                    // init socket with klaspay wallet id
                    viewmodel.pref.putString("klaspay_id", it.wallet_id)
//                    FirebaseMessaging.getInstance()
//                        .subscribeToTopic("/topics/messenger/${it.wallet_id}")
//
//                    chatVm.fetchContactSuspend()
//                    viewmodel.db.chat().closeConversation()
//                    if (!viewmodel.socketClass.connected()) {
//                        viewmodel.socketClass.initSocket()
//                        viewmodel.socketClass.connect()
//                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = window.decorView
        var defaultFlags = view.systemUiVisibility
//        var sosmedFlags = view.systemUiVisibility
        var akunFlags = view.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            sosmedFlags = sosmedFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

            defaultFlags =
                defaultFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

            akunFlags = akunFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
//            akunFlags = akunFlags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            akunFlags = akunFlags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

            view.systemUiVisibility = defaultFlags
        } else {
            window.statusBarColor =
                ContextCompat.getColor(binding.root.context, R.color.colorPrimary)

            view.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }

//        if (BuildConfig.BUILD_TYPE != "release") {
//            window.statusBarColor =
//                Color.parseColor(if (BuildConfig.BUILD_TYPE == "debug") "#53C0F1" else "#F49C4D")
//        }

        setContentView(binding.root)

        FirebaseMessaging.getInstance().apply {
            subscribeToTopic("onklas-notification-user-${viewmodel.pref.getString("user_uuid")}")
            subscribeToTopic("onklas-notification-school-${viewmodel.pref.getString("school_uuid")}")
            subscribeToTopic("Klaspay-US-${viewmodel.pref.getInt("user_id")}")
            subscribeToTopic("attendance-${viewmodel.pref.getInt("class_id")}")
            subscribeToTopic("loggedin")

            unsubscribeFromTopic("unlogged")
        }

        binding.drawer.viewmodel = viewmodel
        binding.drawer.lifecycleOwner = this

        binding.menuBot.setOnNavigationItemSelectedListener {
            navController.popBackStack()
            navController.navigate(
                when (it.itemId) {
//                    R.id.menu_jelajah -> R.id.action_global_jelajahPage
                    R.id.menu_pembelajaran -> R.id.action_global_pembelajaranPage
                    R.id.menu_pembayaran -> R.id.action_global_pembayaranPage
                    R.id.menu_akun -> R.id.action_global_accountPage
                    R.id.menu_toko -> R.id.action_global_storePage
                    else -> R.id.action_global_sekolahPage
                }
            )

            if (it.itemId == R.id.menu_pembelajaran || it.itemId == R.id.menu_pembayaran) {
//                if (it.itemId == R.id.menu_akun || it.itemId == R.id.menu_pembelajaran || it.itemId == R.id.menu_pembayaran) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    view.systemUiVisibility = akunFlags
//                    window.statusBarColor =
//                        ContextCompat.getColor(binding.root.context, android.R.color.transparent)
                } else {
                    view.systemUiVisibility = defaultFlags
//                    window.statusBarColor =
//                        ContextCompat.getColor(binding.root.context, R.color.colorPrimary)
                }
                window.statusBarColor = Color.parseColor("#00A2E9")
//                    ContextCompat.getColor(binding.root.context, R.color.colorPrimary)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.systemUiVisibility = defaultFlags
                window.statusBarColor =
                    ContextCompat.getColor(binding.root.context, android.R.color.transparent)
            }

//            if (BuildConfig.BUILD_TYPE != "release") {
//                window.statusBarColor =
//                    Color.parseColor(if (BuildConfig.BUILD_TYPE == "debug") "#53C0F1" else "#F49C4D")
//            }

            true
        }

        binding.drawer.btnUserSetting.setOnClickListener {
            startActivityForResult(
                Intent(this, SettingAkunPage::class.java), 320
            )
        }

        binding.drawer.btnContact.setOnClickListener {
            startActivity(
                Intent(this, SettingContactPage::class.java)
            )
        }

        binding.drawer.btnChangePass.setOnClickListener {
            startActivity(
                Intent(this, ChangePassPage::class.java)
            )
        }

        binding.drawer.btnStudentCard.visibility =
            if (viewmodel.pref.getBoolean("is_student")) View.VISIBLE else View.GONE

        binding.drawer.btnStudentCard.setOnClickListener {
            startActivity(
                Intent(this, StudentCardPage::class.java)
            )
        }

        binding.drawer.btnTerms.setOnClickListener {
            startActivity(
                Intent(this, TermsPage::class.java)
            )
        }
        binding.drawer.btnAbout.setOnClickListener {
            startActivity(
                Intent(this, AboutPage::class.java)
            )
        }
        binding.drawer.btnDevices.setOnClickListener {
            startActivity(
                Intent(this, DevicesPage::class.java)
            )
        }
        binding.drawer.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.DialogTheme)
                .setTitle("Logout")
                .setMessage("Anda yakin akan keluar dari aplikasi?")
                .setPositiveButton("Logout") { dialog, _ ->
                    lifecycleScope.launch {
                        dialog.dismiss()
                        if (viewmodel.db.akm().hasUnfinishedUjian()) {
                            alert(
                                msg = "Masih terdapat ujian yang belum diselesaikan, silahkan kumpulkan ujian terlebih dahulu agar nilai ujian terproses",
                                okLabel = "OK"
                            ) {
                                startActivity(
                                    Intent(
                                        this@HomePage,
                                        AkmPage::class.java
                                    ).putExtra("isSchoolScope", true)
                                )
                            }
                        } else {
                            loading(msg = "Keluar dari aplikasi")
                            gSignInClient.signOut()
                            viewmodel.intentUtil.logOut()
                            dismissloading()
                            startActivity(Intent(this@HomePage, Loginpage::class.java))
                            finishAffinity()
                        }
                    }
                }
                .setNeutralButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.drawerLayout.setScrimColor(Color.TRANSPARENT)
        binding.drawerLayout.addDrawerListener(drawerToggle)
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding.drawer.root.layoutParams = binding.drawer.root.layoutParams.apply {
            width = screenWidth / 10 * 7
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            profileVm.myProfilePicture.observe(this, {
                GlideApp.with(this).load(it)
                    .override(resources.getDimensionPixelSize(R.dimen._24sdp))
                    .thumbnail(0.1f)
                    .circleCrop()
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            try {
                                binding.menuBot.menu.findItem(R.id.menu_akun).apply {
                                    icon = resource
                                    iconTintBlendMode = BlendMode.DST
                                }
                            } catch (e: Exception) {
                                Timber.e(e)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })

            })
        }

        viewmodel.errorString.observe(this, Observer(this::toast))
    }

    private fun verifyEmail(google_token: String, email: String) {
        lifecycleScope.launchWhenCreated {
            try {
                loading(msg = "memverifikasi email")
                profileVm.apiService.verifyEmail(google_token)
                profileVm.pref.putBoolean("is_email_verified", true)
                profileVm.pref.putBoolean("is_email_verifying", false)

                profileVm.userTable.email = email
                profileVm.pref.putString(
                    "user",
                    profileVm.userTableAdapter.toJson(profileVm.userTable)
                )
            } catch (e: Exception) {
                gSignInClient.signOut()
                toast(e.message)
                Timber.e(e)
            } finally {
                dismissloading()
            }
        }
    }

    private val googleSignInResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK)
                lifecycleScope.launchWhenCreated {
                    try {
                        loading(msg = "memverifikasi email")
                        val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                        val token = task.result.idToken
                        val email = task.result.email
                        if (token != null && email != null) {
                            verifyEmail(token, email)
                            (supportFragmentManager.findFragmentByTag("email_dialog") as? DialogFragment)?.dismiss()
                        } else {
                            toast("gagal memverifikasi email google, silahkan coba beberapa saat lagi")
                            gSignInClient.signOut()
                        }
//                        Timber.e("token: ${task.result.idToken}")
//                        Timber.e("display name: ${task.result.displayName}")
//                        Timber.e("email: ${task.result.email}")
                    } catch (e: Exception) {
                        Timber.e(e)
                    } finally {
                        dismissloading()
                    }
                }
        }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launchWhenCreated {
            try {
//                viewmodel.db.chat().closeConversation()
                viewmodel.checkUser()

                // popup to verify email
                if (!viewmodel.pref.getBoolean("is_email_verified")) {
                    val account = GoogleSignIn.getLastSignedInAccount(this@HomePage)

                    if (account != null) {
                        // try to verify email with signed google account
                        val token = account.idToken
                        val email = account.email
                        if (token != null && email != null) {
                            verifyEmail(token, email)
                        } else {
                            // token is null, try to sign out account first
                            gSignInClient.signOut().addOnSuccessListener {
                                // open google sign in intent when sign out success
                                googleSignInResult.launch(Intent(gSignInClient.signInIntent))
                            }
                        }
                    } else {
                        // open google sign in intent
                        googleSignInResult.launch(Intent(gSignInClient.signInIntent))
                    }

                    // show verification email dialog
                    HomeDialog.showEmailSettingDialog(supportFragmentManager) {
                        if (viewmodel.pref.getBoolean("default_pass"))
                            HomeDialog.showChangePasswordDialog(
                                this@HomePage,
                                supportFragmentManager,
                                layoutInflater
                            )
                    }
                } else if (viewmodel.pref.getBoolean("is_email_verifying"))
                    HomeDialog.showEmailConfirmDialog(supportFragmentManager) {
                        if (viewmodel.pref.getBoolean("default_pass"))
                            HomeDialog.showChangePasswordDialog(
                                this@HomePage,
                                supportFragmentManager,
                                layoutInflater
                            )
                    }
                else if (viewmodel.pref.getBoolean("default_pass"))
                    HomeDialog.showChangePasswordDialog(
                        this@HomePage,
                        supportFragmentManager,
                        layoutInflater
                    )
                else if (viewmodel.user.value?.username.isNullOrEmpty())
                    HomeDialog.showSetUsernameDialog(
                        this@HomePage,
                        supportFragmentManager,
                        layoutInflater
                    )
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun setBottomNavVisibility(visible: Boolean = true) {
        binding.menuBot.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END))
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        else
            finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 320 && resultCode == RESULT_OK) {
            profileVm.myUsername.postValue(data?.getStringExtra("username"))
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private val zoomHelper: MyImageZoom by lazy { MyImageZoom(this, binding.root) }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return zoomHelper.onDispatchTouchEvent(ev) || super.dispatchTouchEvent(ev)
    }

    private val drawerToggle: DrawerLayout.DrawerListener by lazy {
        object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(p0: Int) {}
            override fun onDrawerClosed(p0: View) {}
            override fun onDrawerOpened(p0: View) {}

            override fun onDrawerSlide(v: View, offset: Float) {
                binding.content.translationX = -v.width * offset
            }
        }
    }

    private val screenWidth: Int by lazy {
        if (viewmodel.pref.getInt("screen_x") > 0) {
            viewmodel.pref.getInt("screen_x")
        } else {
            val size = Point()
            windowManager.defaultDisplay.getSize(size)
            viewmodel.pref.putInt("screen_x", size.x)
            viewmodel.pref.putInt("screen_y", size.y)
            size.x
        }
    }
}