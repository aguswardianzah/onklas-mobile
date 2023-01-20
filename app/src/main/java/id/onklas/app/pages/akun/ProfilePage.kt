package id.onklas.app.pages.akun

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.material.appbar.AppBarLayout
import id.onklas.app.R
import id.onklas.app.databinding.ProfilePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.sekolah.MyImageZoom

class ProfilePage : Privatepage() {

    private val binding by lazy { ProfilePageBinding.inflate(layoutInflater) }
    private val navController by lazy { findNavController(R.id.nav_controller) }
    private val profileVm by viewModels<ProfileViewModel> { component.profileVmFactory }

    companion object {

        fun open(
            activity: Activity,
            nisn_nik: String = "",
            user_id: Int = 0,
            username: String = ""
        ) {
            activity.startActivity(
                Intent(activity, ProfilePage::class.java)
                    .putExtra("nisn_nik", nisn_nik)
                    .putExtra("username", username)
                    .putExtra("user_id", user_id)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        profileVm.userData.observe(this, {
            binding.userData = it
            binding.swipeRefresh.isRefreshing = false
        })

        profileVm.nisnNik.observe(this, {
            lifecycleScope.launchWhenCreated {
                val loading = ProgressDialog.show(this@ProfilePage, "", "menampilkan profil")
                profileVm.getUserData()
                loading.dismiss()
            }
        })

//        profileVm.userId.observe(this, { lifecycleScope.launchWhenCreated { profileVm.getUser() } })
        profileVm.username.observe(this, {
            lifecycleScope.launchWhenCreated {
                loading(msg = "menampilkan profil")
                profileVm.getUserByUsername()
                dismissloading()
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launchWhenCreated {
                profileVm.getUserData()
            }
        }

        profileVm.nisnNik.postValue(
            if (intent.hasExtra("nisn_nik") && !intent.getStringExtra("nisn_nik").isNullOrEmpty())
                intent.getStringExtra("nisn_nik")
            else
                if (profileVm.userTable.nisn_nik.isNotEmpty()) profileVm.userTable.nisn_nik else profileVm.userTable.nis_nik
        )

//        profileVm.userId.postValue(
//            if (intent.hasExtra("user_id") && intent.getIntExtra("user_id", 0) > 0)
//                intent.getIntExtra("user_id", 0)
//            else
//                null
//        )

        profileVm.username.postValue(
            if (intent.hasExtra("username") && !intent.getStringExtra("username").isNullOrEmpty())
                intent.getStringExtra("username")?.replace("@", "")
            else
                ""
        )

        binding.img.setOnClickListener {
            startActivityForResult(
                Intent(this, ProfilePicturePage::class.java)
                    .putExtra(
                        "editable",
                        binding.userData?.data?.user_avatar_image == profileVm.userTable.user_avatar_image
                    )
                    .putExtra("path", binding.userData?.data?.user_avatar_image.orEmpty()),
                321,
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    binding.img,
                    "profile"
                ).toBundle()
            )
        }

        binding.btnPost.setOnClickListener {
            navController.navigate(R.id.action_global_profilePostPage)
            binding.btnPost.setImageResource(R.drawable.ic_create_post)
            binding.btnMedia.setImageResource(R.drawable.ic_galery_inactive)
            binding.btnEbook.setImageResource(R.drawable.ic_ebook_inactive)
        }

        binding.btnMedia.setOnClickListener {
            navController.navigate(R.id.action_global_profileMediaPage)
            binding.btnPost.setImageResource(R.drawable.ic_create_post_inactive)
            binding.btnMedia.setImageResource(R.drawable.ic_galery_active)
            binding.btnEbook.setImageResource(R.drawable.ic_ebook_inactive)
        }

        binding.btnEbook.setOnClickListener {
            navController.navigate(R.id.action_global_profileEbookPage)
            binding.btnPost.setImageResource(R.drawable.ic_create_post_inactive)
            binding.btnMedia.setImageResource(R.drawable.ic_galery_inactive)
            binding.btnEbook.setImageResource(R.drawable.ic_ebook_active)
        }

        binding.appbar.addOnOffsetChangedListener(appBarCollapseListener)
    }

    override fun onBackPressed() {
        finish()
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_profile, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
////        startActivity(
////            Intent(this, ChatPage::class.java)
////                .putExtra("to", profileVm.user)
////        )
//        return true
//    }

    private val zoomHelper: MyImageZoom by lazy { MyImageZoom(this, binding.root) }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return zoomHelper.onDispatchTouchEvent(ev) || super.dispatchTouchEvent(ev)
    }

    enum class State { EXPANDED, COLLAPSED, IDLE }

    private val appBarCollapseListener: AppBarLayout.OnOffsetChangedListener by lazy {
        object : AppBarLayout.OnOffsetChangedListener {
            var mCurrentState = State.IDLE
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, i: Int) {
                if (i == 0) {
                    if (mCurrentState != State.EXPANDED) {
                        binding.swipeRefresh.isEnabled = true
                    }
                    mCurrentState = State.EXPANDED
                } else if (Math.abs(i) >= appBarLayout?.totalScrollRange ?: 0) {
                    if (mCurrentState != State.COLLAPSED) {
                        binding.swipeRefresh.isEnabled = false
                    }
                    mCurrentState = State.COLLAPSED
                } else {
                    mCurrentState = State.IDLE
                }
            }
        }
    }
}