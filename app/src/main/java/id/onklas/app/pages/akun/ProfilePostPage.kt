package id.onklas.app.pages.akun

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.ProfilePostPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.sekolah.PostAdapterCallback
import id.onklas.app.pages.sekolah.SosmedViewModel
import id.onklas.app.pages.sekolah.adapter.PostAdapter2
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProfilePostPage : Fragment() {

    private lateinit var binding: ProfilePostPageBinding

    private val profileVm by activityViewModels<ProfileViewModel> { component.profileVmFactory }
    private val sekolahVm by activityViewModels<SosmedViewModel> { component.sosmedVmFactory }
    private val glideRequests by lazy { GlideApp.with(requireActivity()) }
    private val adapter by lazy {
        PostAdapter2(
            postAdapterCallback,
            glideRequests,
            profileVm.stringUtil,
            0,
            profileVm.pref.getInt("user_id")
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ProfilePostPageBinding.inflate(inflater, container, false).also { binding = it }.root

    private var firstLoad = true
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rvPost.adapter = adapter
        binding.rvPost.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(
                    R.dimen._8sdp
                ), includeBottom = true, includeTop = true
            )
        )

        profileVm.userData.observe(viewLifecycleOwner, {
            firstLoad = true
            profileVm.listPost(it.data.id)
                .observe(viewLifecycleOwner, {
                    adapter.submitList(it) {
                        if (!firstLoad) {
                            val layoutManager = (binding.rvPost.layoutManager as LinearLayoutManager)
                            val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                            if (position != RecyclerView.NO_POSITION) {
                                binding.rvPost.scrollToPosition(position)
                            }
                        } else {
                            binding.rvPost.scrollToPosition(0)
                            firstLoad = false
                        }
                    }
                })

            lifecycleScope.launch {
                profileVm.loadListPost(0)
            }
        })
    }

    private val postAdapterCallback by lazy {
        object : PostAdapterCallback.PostCallback(requireActivity()) {
            override fun onClickProfile(item: FeedTimeline) {}

            override fun onClickLike(item: FeedTimeline, liked: Boolean) {
                GlobalScope.launch {
                    sekolahVm.likePost(item, liked)
                }
            }

            override fun onClickOption(item: FeedTimeline) {
                MaterialAlertDialogBuilder(requireContext(), R.style.DialogTheme)
                    .setItems(arrayOf("Hapus Post")) { dialog, which ->
                        dialog.dismiss()
                        MaterialAlertDialogBuilder(requireContext(), R.style.DialogTheme)
                            .setMessage("Anda yakin akan menghapus post?")
                            .setPositiveButton("Hapus") { dialog1, _ ->
                                dialog1.dismiss()
                                lifecycleScope.launch {
                                    val progress =
                                        ProgressDialog.show(requireContext(), "", "menghapus post")
                                    sekolahVm.deleteFeed(item.feed.feed_id)
                                    progress.dismiss()
                                }
                            }
                            .setNeutralButton("Batal") { dialog1, _ -> dialog1.dismiss() }
                            .show()
                    }
                    .show()
            }

            override fun onClickMention(username: String) {
                ProfilePage.open(requireActivity(), username = username)
            }
        }
    }
}