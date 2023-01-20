package id.onklas.app.pages.sekolah

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import id.onklas.app.R
import id.onklas.app.databinding.SekolahPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.chat.ChatListPage
import id.onklas.app.pages.createpost.CreatePostPage
import id.onklas.app.pages.createpost.UploadEbookPage
import id.onklas.app.pages.home.HomePage
import id.onklas.app.pages.jelajah.JelajahPage
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.pages.sekolah.store.CartPage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class SekolahPage : Fragment() {

    private lateinit var binding: SekolahPageBinding
    private val viewmodel by activityViewModels<SosmedViewModel> { component.sosmedVmFactory }
    private val sekolahNav by lazy {
        Navigation.findNavController(
            requireActivity(),
            R.id.page_container
        )
    }
    private val colorPrimary by lazy {
        ContextCompat.getColor(requireContext(), R.color.colorPrimary)
    }
    private val colorGray by lazy {
        ContextCompat.getColor(requireContext(), R.color.gray)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = SekolahPageBinding.inflate(inflater, container, false).also { binding = it }.root

//    override fun onResume() {
//        super.onResume()
//
//        try {
//            if (viewmodel.pref.getBoolean("klaspayActive") && binding.toolbar.menu.findItem(R.id.menu_chat) == null) {
//                binding.toolbar.inflateMenu(R.menu.menu_profile)
//                val actionView = binding.toolbar.menu.findItem(R.id.menu_chat).actionView
//                actionView.setOnClickListener {
//                    startActivity(Intent(requireActivity(), ChatListPage::class.java))
//                }
//
//                binding.toolbar.setOnMenuItemClickListener {
//                    if (it.itemId == R.id.menu_cart) {
//                        CartPage.open(requireActivity())
//                        true
//                    } else {
//                        startActivity(Intent(requireActivity(), ChatListPage::class.java))
//                        true
//                    }
//                }
//
//                val actionCart = binding.toolbar.menu.findItem(R.id.menu_cart).actionView
//                actionCart.setOnClickListener {
//                    CartPage.open(requireActivity())
//                }
//
//                binding.btnFilter.setOnClickListener {
//                    val intent = Intent("OnklasBroadcast")
//                    intent.putExtra("openFilter", "openFilter")
//
//                    LocalBroadcastManager.getInstance(requireContext())
//                        .sendBroadcast(intent) //local broadcast
//                }
//
//                viewmodel.countUnreadChat.observe(viewLifecycleOwner) {
//                    try {
//                        if (it == null) return@observe
//
//                        Timber.e("chat unread: $it")
//                        val badge = actionView.findViewById<View>(R.id.badge)
//                        badge.visibility = if (it > 0) View.VISIBLE else View.GONE
//                    } catch (e: Exception) {
//                        Timber.e(e)
//                    }
//                }
//            }
//        } catch (e: Exception) {
//        }
//    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewmodel

        binding.fabCreatePost.setOnClickListener {
            startActivityForResult(
                Intent(requireContext(), CreatePostPage::class.java), 123
            )
            binding.fabCreate.collapseImmediately()
        }

        binding.fabUploadEbook.setOnClickListener {
            startActivityForResult(
                Intent(requireActivity(), UploadEbookPage::class.java), 1234
            )
            binding.fabCreate.collapseImmediately()
        }

        binding.menuPost.setOnClickListener {
//            sekolahNav.popBackStack()
            binding.menuEbook.setTextColor(colorGray)
            binding.menuEbook.setBackgroundResource(R.drawable.border_form_login_gray)

//            binding.menuToko.setTextColor(colorGray)
//            binding.menuToko.setBackgroundResource(R.drawable.border_form_login_gray)

            sekolahNav.navigate(R.id.action_global_sosmedPage)
            binding.menuPost.setTextColor(colorPrimary)
            binding.menuPost.setBackgroundResource(R.drawable.border_form_login_primary)

            binding.fabCreate.visibility = View.VISIBLE
            binding.btnFilter.visibility = View.GONE
        }

        binding.menuEbook.setOnClickListener {
//            sekolahNav.popBackStack()
            binding.menuPost.setTextColor(colorGray)
            binding.menuPost.setBackgroundResource(R.drawable.border_form_login_gray)

//            binding.menuToko.setTextColor(colorGray)
//            binding.menuToko.setBackgroundResource(R.drawable.border_form_login_gray)

            sekolahNav.navigate(R.id.action_global_ebookPage)
            binding.menuEbook.setTextColor(colorPrimary)
            binding.menuEbook.setBackgroundResource(R.drawable.border_form_login_primary)

            binding.fabCreate.visibility = View.VISIBLE
            binding.btnFilter.visibility = View.GONE
        }

//        binding.menuToko.setOnClickListener {
////            sekolahNav.popBackStack()
//            binding.menuPost.setTextColor(colorGray)
//            binding.menuPost.setBackgroundResource(R.drawable.border_form_login_gray)
//
//            binding.menuEbook.setTextColor(colorGray)
//            binding.menuEbook.setBackgroundResource(R.drawable.border_form_login_gray)
//
//
//            sekolahNav.navigate(R.id.action_global_storePage)
//            binding.menuToko.setTextColor(colorPrimary)
//            binding.menuToko.setBackgroundResource(R.drawable.border_form_login_primary)
//
//            binding.fabCreate.visibility = View.GONE
//            binding.btnFilter.visibility = View.VISIBLE
//        }

        binding.menuProfil.setOnClickListener {
            startActivity(Intent(requireActivity(), JelajahPage::class.java))
        }

        binding.executePendingBindings()
    }

    private val homePage by lazy { (requireActivity() as? HomePage) }
    fun onScroll(scrollY: Int) {
        val visible = scrollY <= 0
//        binding.appbar.setExpanded(visible, true)
        homePage?.setBottomNavVisibility(visible)
    }

    val postAdapterCallback by lazy {
        object : PostAdapterCallback.PostCallback(requireActivity()) {
            override fun onClickLike(item: FeedTimeline, liked: Boolean) {
//                GlobalScope.launch {
                    viewmodel.likePost(item, liked)
//                }
            }

            override fun onClickDownloadEbook(item: FeedTimeline) {
                item.files.firstOrNull()?.let {
                    viewmodel.intentUtil.openPdf(requireActivity(), it.path, item.feed.feed_body)
                } ?: run {
                    Toast.makeText(
                        requireContext(),
                        "ebook tidak tersedia, mohon ulangi beberapa saat lagi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onDeleteFeed(item: FeedTimeline) {
                lifecycleScope.launch {
                    val progress =
                        ProgressDialog.show(requireContext(), "", "menghapus post")
                    viewmodel.deleteFeed(item.feed.feed_id)
                    progress.dismiss()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.e("activity result --> requestCode: $requestCode -- resultCode: $resultCode")
        if (resultCode == Activity.RESULT_OK && requestCode == 123) {
            sekolahNav.navigate(R.id.action_global_sosmedPage)
        } else if (resultCode == Activity.RESULT_OK && requestCode == 1234) {
            sekolahNav.navigate(R.id.action_global_ebookPage)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}