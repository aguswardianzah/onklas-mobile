package id.onklas.app.pages.sekolah.ebook

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
import id.onklas.app.databinding.EbookPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.sekolah.SekolahPage
import id.onklas.app.pages.sekolah.SosmedViewModel
import id.onklas.app.pages.sekolah.adapter.PostEbookAdapter
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch

class EbookPage : Fragment() {

    private lateinit var binding: EbookPageBinding
    private val viewmodel by activityViewModels<SosmedViewModel> { component.sosmedVmFactory }
    private val sekolahPage by lazy { (requireParentFragment().parentFragment as SekolahPage) }
    private val glide by lazy { GlideApp.with(requireActivity()) }
    private val adapter by lazy {
        PostEbookAdapter(
            sekolahPage.postAdapterCallback,
            glide,
            viewmodel.stringUtil,
            viewmodel.pref.getInt("user_id")
        )
    }
    private var firstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = EbookPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewmodel = viewmodel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.rvEbook.doOnLayout {
            viewmodel.ebookRvWidth = binding.rvEbook.width
        }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                firstLoad = true
                viewmodel.prevEbook = -1
                viewmodel.nextEbookAvailable = true
                viewmodel.refreshTimeline("ebook")
            }
        }
        viewmodel.loadingEbook.observe(viewLifecycleOwner, Observer(binding.swipeRefresh::setRefreshing))

        binding.rvEbook.adapter = adapter
        binding.rvEbook.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(
                    R.dimen._8sdp
                ), includeTop = true, includeBottom = true
            )
        )
//        binding.rvEbook.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                sekolahPage.onScroll(dy)
//            }
//        })

        viewmodel.listEbook.observe(viewLifecycleOwner, {
            adapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager = (binding.rvEbook.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvEbook.scrollToPosition(position)
                    }
                } else {
                    binding.rvEbook.scrollToPosition(0)
                    firstLoad = false
                }
            }
        })
    }

    init {
        lifecycleScope.launchWhenCreated {
            viewmodel.refreshTimeline("ebook")
        }
    }
}