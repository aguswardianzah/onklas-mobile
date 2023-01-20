package id.onklas.app.pages.sekolah.sosmed

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.SosmedPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.sekolah.SekolahPage
import id.onklas.app.pages.sekolah.SosmedViewModel
import id.onklas.app.pages.sekolah.adapter.PostAdapter2
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch
import timber.log.Timber

class SosmedPage : Fragment() {

    private lateinit var binding: SosmedPageBinding
    private val viewmodel by activityViewModels<SosmedViewModel> { component.sosmedVmFactory }
    private val sekolahPage by lazy { (requireParentFragment().parentFragment as SekolahPage) }
    private val glide by lazy { GlideApp.with(requireActivity()) }
    private val adapter by lazy {
        PostAdapter2(
            sekolahPage.postAdapterCallback,
            glide,
            viewmodel.stringUtil,
            viewmodel.postRvWidth,
            viewmodel.pref.getInt("user_id")
        )
    }
    private var firstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = SosmedPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewmodel = viewmodel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.rvPost.doOnLayout {
            viewmodel.postRvWidth = binding.rvPost.width
        }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                firstLoad = true
                viewmodel.prevPost = -1
                viewmodel.nextPostAvailable = true
                viewmodel.refreshTimeline()
            }
        }
        viewmodel.loadingPost.observe(
            viewLifecycleOwner,
            Observer(binding.swipeRefresh::setRefreshing)
        )

        binding.rvPost.adapter = adapter
        binding.rvPost.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(
                    R.dimen._8sdp
                ), includeTop = true, includeBottom = true
            )
        )
//        binding.rvPost.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                sekolahPage.onScroll(dy)
//            }
//        })

        viewmodel.listPost.observe(viewLifecycleOwner, {
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.e("reqCode: $requestCode ---- resultCode: $resultCode")
        super.onActivityResult(requestCode, resultCode, data)
    }

    init {
        lifecycleScope.launchWhenCreated {
            viewmodel.refreshTimeline()
        }
    }
}