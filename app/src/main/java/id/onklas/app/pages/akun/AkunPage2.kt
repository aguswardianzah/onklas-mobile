package id.onklas.app.pages.akun

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import id.onklas.app.R
import id.onklas.app.databinding.AkunPage2Binding
import id.onklas.app.di.component
import id.onklas.app.pages.home.HomePage

class AkunPage2 : Fragment() {

    private lateinit var binding: AkunPage2Binding
    private val navController by lazy {
        Navigation.findNavController(
            requireActivity(),
            R.id.nav_controller
        )
    }
    private val profileVm by activityViewModels<ProfileViewModel> { component.profileVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = AkunPage2Binding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = this

        profileVm.myProfilePicture.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) binding.imgUrl = it
        })

        profileVm.myUsername.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) binding.myUsername = it
        })

        profileVm.userData.observe(viewLifecycleOwner, {
            binding.userData = it
        })

        profileVm.nisnNik.observe(viewLifecycleOwner, {
            lifecycleScope.launchWhenCreated {
                profileVm.getUserData()
            }
        })

        profileVm.nisnNik.postValue(if (profileVm.userTable.nisn_nik.isNotEmpty()) profileVm.userTable.nisn_nik else profileVm.userTable.nis_nik)

        binding.toolbar.inflateMenu(R.menu.menu_option)
        binding.toolbar.menu.findItem(R.id.m_option).setOnMenuItemClickListener {
            (requireActivity() as? HomePage)?.let {
                it.binding.drawerLayout.openDrawer(it.binding.drawer.root)
                true
            } ?: false
        }

        binding.img.setOnClickListener {
            startActivityForResult(
                Intent(requireActivity(), ProfilePicturePage::class.java)
                    .putExtra(
                        "editable",
                        true
                    )
                    .putExtra("path", profileVm.userTable.user_avatar_image.orEmpty()),
                321,
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(),
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
            binding.btnPairing.setImageResource(R.drawable.ic_pairing_inactive)
        }

        binding.btnMedia.setOnClickListener {
            navController.navigate(R.id.action_global_profileMediaPage)
            binding.btnPost.setImageResource(R.drawable.ic_create_post_inactive)
            binding.btnMedia.setImageResource(R.drawable.ic_galery_active)
            binding.btnEbook.setImageResource(R.drawable.ic_ebook_inactive)
            binding.btnPairing.setImageResource(R.drawable.ic_pairing_inactive)
        }

        binding.btnEbook.setOnClickListener {
            navController.navigate(R.id.action_global_profileEbookPage)
            binding.btnPost.setImageResource(R.drawable.ic_create_post_inactive)
            binding.btnMedia.setImageResource(R.drawable.ic_galery_inactive)
            binding.btnEbook.setImageResource(R.drawable.ic_ebook_active)
            binding.btnPairing.setImageResource(R.drawable.ic_pairing_inactive)
        }

        binding.btnPairing.setOnClickListener {
            navController.navigate(R.id.action_global_pairing)
            binding.btnPost.setImageResource(R.drawable.ic_create_post_inactive)
            binding.btnMedia.setImageResource(R.drawable.ic_galery_inactive)
            binding.btnEbook.setImageResource(R.drawable.ic_ebook_inactive)
            binding.btnPairing.setImageResource(R.drawable.ic_pairing_active)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 321 && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                profileVm.myProfilePicture.postValue(it.toString())
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}