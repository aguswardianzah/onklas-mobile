package id.onklas.app.pages.akun

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.EbookPartContentBinding
import id.onklas.app.databinding.ProfileEbookPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch

class ProfileEbookPage : Fragment() {

    private lateinit var binding: ProfileEbookPageBinding
    private val viewmodel by activityViewModels<ProfileViewModel> { component.profileVmFactory }
    private val glideRequests by lazy { GlideApp.with(requireActivity()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ProfileEbookPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rvPost.adapter = adapter
        binding.rvPost.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(
                    R.dimen._8sdp
                )
            )
        )

        viewmodel.userData.observe(viewLifecycleOwner, {
            viewmodel.listPost(it.data.id, "ebook")
                .observe(viewLifecycleOwner, Observer(adapter::submitList))

            lifecycleScope.launch {
                viewmodel.loadListEbook(0)
            }
        })
    }

    private val adapter by lazy {
        object : PagingAdapter<FeedTimeline, EbookViewholder>(object :
            DiffUtil.ItemCallback<FeedTimeline>() {
            override fun areItemsTheSame(oldTable: FeedTimeline, newTable: FeedTimeline): Boolean =
                oldTable.feed.feed_id == newTable.feed.feed_id

            override fun areContentsTheSame(oldItem: FeedTimeline, newItem: FeedTimeline): Boolean =
                oldItem.feed.timeString == newItem.feed.timeString &&
                        oldItem.feed.count_comments == newItem.feed.count_comments &&
                        oldItem.feed.count_likes == newItem.feed.count_likes
        }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): EbookViewholder =
                EbookViewholder(parent)

            override fun bindItemViewholder(holder: EbookViewholder, position: Int) {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }

            override fun bindItemViewholder(
                holder: EbookViewholder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }
        }
    }

    inner class EbookViewholder(
        parent: ViewGroup,
        val binding: EbookPartContentBinding = EbookPartContentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: FeedTimeline) {
            binding.item = item
            binding.executePendingBindings()

            val path = item.feed.feed_thumbnail_image
            if (path.isEmpty()) {
                binding.cover.setImageResource(R.drawable.img_default_book_cover)
            } else {
                glideRequests.load(path)
                    .override(
                        resources.getDimensionPixelSize(R.dimen._36sdp),
                        resources.getDimensionPixelSize(R.dimen._72sdp)
                    )
                    .fitCenter()
                    .placeholder(R.drawable.img_loading_book_cover)
                    .error(R.drawable.img_error_book_cover)
                    .into(binding.cover)
            }

            binding.lihat.setOnClickListener {
                item.files.firstOrNull()
                    ?.let { viewmodel.intentUtil.openPdf(requireActivity(), it.path, it.name) }
            }
        }
    }
}