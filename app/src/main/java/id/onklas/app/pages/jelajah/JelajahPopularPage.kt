package id.onklas.app.pages.jelajah

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arasthel.spannedgridlayoutmanager.SpanSize
import com.arasthel.spannedgridlayoutmanager.SpannedGridLayoutManager
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.JelajahPopularItemBinding
import id.onklas.app.databinding.JelajahRvPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.comment.CommentPage
import id.onklas.app.pages.sekolah.PostAdapterCallback
import id.onklas.app.pages.sekolah.SosmedViewModel
import id.onklas.app.pages.sekolah.adapter.PostAdapter2
import id.onklas.app.pages.sekolah.sosmed.FeedItem
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class JelajahPopularPage : Fragment() {

    private lateinit var binding: JelajahRvPageBinding
    private val viewmodel by activityViewModels<JelajahViewModel> { component.jelajahVmFactory }
    private val sosmedVm by viewModels<SosmedViewModel> { component.sosmedVmFactory }
    private val glideRequests by lazy { GlideApp.with(requireActivity()) }
    private val rvMargin by lazy { resources.getDimensionPixelSize(R.dimen._12sdp) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        JelajahRvPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.rvJelajah.apply {
            layoutManager = gridLayoutManager
            adapter = gridAdapter
            doOnLayout { viewmodel.rvWidth = it.width }
        }

        viewmodel.search.observe(viewLifecycleOwner, { search ->
            Timber.e("populer - search value: $search")
            if (search.isNotEmpty()) {
                binding.rvJelajah.apply {
                    layoutManager = linearLayoutManager
                    adapter = postAdapter
                    addItemDecoration(linearSpace)
                    layoutParams = layoutParams.run {
                        (this as? ConstraintLayout.LayoutParams)?.apply {
                            setMargins(rvMargin, 0, rvMargin, 0)
                        }
                    }
                }

                viewmodel.listPopular(search).observe(viewLifecycleOwner, {
                    postAdapter.submitList(it)
                })

                lifecycleScope.launch {
                    viewmodel.hasMorePost = true
                    viewmodel.isRequestPost = false
                    viewmodel.fetchPopular(0, search)
                }
            } else {
                binding.rvJelajah.apply {
                    layoutManager = gridLayoutManager
                    adapter = gridAdapter
                    removeItemDecoration(linearSpace)
                    layoutParams = layoutParams.run {
                        (this as? ConstraintLayout.LayoutParams)?.apply {
                            setMargins(0, 0, 0, 0)
                        }
                    }
                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)

                            lifecycleScope.launchWhenCreated {
                                if (listPost.size >= viewmodel.pageSize && gridLayoutManager.lastVisiblePosition > listPost.size - 6 && !requestingData) {
                                    requestingData = true
                                    val newData = viewmodel.requestPopular(listPost.size, search)
                                        .filterNot { listPost.contains(it) }
                                    listPost.addAll(newData)
                                    gridAdapter.notifyItemRangeInserted(
                                        listPost.size - newData.size,
                                        listPost.size
                                    )
                                    requestingData = newData.size < viewmodel.pageSize
                                }
                            }
                        }
                    })
                }

                lifecycleScope.launch {
                    if (listPost.isNotEmpty())
                        listPost.clear()

                    requestingData = true
                    val newData = viewmodel.requestPopular(listPost.size, search)
                    listPost.addAll(newData)
                    gridAdapter.notifyItemRangeInserted(
                        listPost.size - newData.size,
                        listPost.size
                    )
                    requestingData = newData.size < viewmodel.pageSize
                }
            }
        })

        viewmodel.loadingPopular.observe(viewLifecycleOwner, Observer(binding::setLoading))
    }

    enum class MainGridPos { LEFT, RIGHT }

    private var mainGridPos = MainGridPos.RIGHT
    private var nextMainPos = 0
    private val gridLayoutManager by lazy {
        SpannedGridLayoutManager(
            orientation = SpannedGridLayoutManager.Orientation.VERTICAL,
            spans = 3
        ).apply {
            itemOrderIsStable = true
            spanSizeLookup = SpannedGridLayoutManager.SpanSizeLookup { position ->
                var size = 1
                if (position == nextMainPos) {
                    size = 2
                } else if (position % 9 == 0) {
                    if (mainGridPos == MainGridPos.LEFT) {
                        mainGridPos = MainGridPos.RIGHT
                        size = 2
                    } else {
                        mainGridPos = MainGridPos.LEFT
                        nextMainPos = position + 1
                    }
                }
                SpanSize(size, size)
            }
        }
    }

    private var requestingData = false
    private val listPost by lazy { ArrayList<FeedItem>() }
    private val gridAdapter by lazy {
        object : RecyclerView.Adapter<Viewholder>(
        ) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(listPost[position])

            override fun getItemCount(): Int = listPost.size
        }
    }

    inner class Viewholder(
        parent: ViewGroup,
        val binding: JelajahPopularItemBinding = JelajahPopularItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FeedItem) {
            glideRequests.load(
                if (item.feed_type == "ebook") item.feed_thumbnail_image
                else item.file.feed_files_path
            )
                .centerCrop()
                .thumbnail(0.1f)
                .into(binding.image)

            binding.image.setOnClickListener {
                CommentPage.open(requireActivity(), item.row_id)
            }
        }
    }

    private val adapterCallback by lazy {
        object : PostAdapterCallback.PostCallback(requireActivity()) {
            override fun onClickLike(item: FeedTimeline, liked: Boolean) {
                GlobalScope.launch {
                    sosmedVm.likePost(item, liked)
                }
            }

            override fun onClickDownloadEbook(item: FeedTimeline) {
                item.files.firstOrNull()?.let {
                    viewmodel.intentUtil.openPdf(
                        requireActivity(),
                        it.path,
                        item.feed.feed_body
                    )
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
                        ProgressDialog.show(
                            requireContext(),
                            "",
                            "menghapus post"
                        )
                    sosmedVm.deleteFeed(item.feed.feed_id)
                    progress.dismiss()
                }
            }
        }
    }

    private val postAdapter by lazy {
        PostAdapter2(
            adapterCallback,
            glideRequests,
            viewmodel.stringUtil,
            viewmodel.pref.getInt("user_id")
        )
    }

    private val linearLayoutManager by lazy {
        LinearLayoutManager(
            requireActivity(),
            RecyclerView.VERTICAL,
            false
        )
    }
    private val linearSpace by lazy {
        LinearSpaceDecoration(
            space = resources.getDimensionPixelSize(
                R.dimen._8sdp
            ), includeTop = true, includeBottom = true
        )
    }
}