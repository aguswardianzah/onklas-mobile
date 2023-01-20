package id.onklas.app.pages.announcement

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.AnnouncementItemBinding
import id.onklas.app.databinding.AnnouncementPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch

class AnnouncementPage : Privatepage() {

    private val binding by lazy { AnnouncementPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<AnnouncementViewmodel> { component.announcementVmFactory }
    private var firstLoad = true

    init {
        lifecycleScope.launchWhenCreated {
            viewmodel.fetchData()
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

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                firstLoad = true
                viewmodel.fetchData(0)
                binding.swipeRefresh.isRefreshing = false
            }
        }

        binding.rvAnnouncement.apply {
            adapter = pageAdapter
            addItemDecoration(
                LinearSpaceDecoration(
                    space = resources.getDimensionPixelSize(R.dimen._12sdp),
                    includeTop = true,
                    includeBottom = true
                )
            )
        }

        viewmodel.listAnnouncement.observe(this, Observer {
            pageAdapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager =
                        (binding.rvAnnouncement.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvAnnouncement.scrollToPosition(position)
                    }
                } else {
                    binding.rvAnnouncement.scrollToPosition(0)
                    firstLoad = false
                }
            }
            binding.emptyLabel.visibility = if (it.isNullOrEmpty()) View.VISIBLE else View.GONE
        })
    }

    private val pageAdapter by lazy {
        object : PagingAdapter<AnnouncementTable, Viewholder>(object :
            DiffUtil.ItemCallback<AnnouncementTable>() {
            override fun areItemsTheSame(
                oldItem: AnnouncementTable,
                newItem: AnnouncementTable
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: AnnouncementTable,
                newItem: AnnouncementTable
            ): Boolean = oldItem == newItem
        }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun bindItemViewholder(holder: Viewholder, position: Int) {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }

            override fun bindItemViewholder(
                holder: Viewholder,
                position: Int,
                payloads: MutableList<Any>
            ) = bindItemViewholder(holder, position)
        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: AnnouncementItemBinding = AnnouncementItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.clipToOutline = true
        }

        fun bind(item: AnnouncementTable) {
            binding.item = item
            binding.image.setOnClickListener { openDetail(item.id.toInt()) }
            binding.readMore.setOnClickListener { openDetail(item.id.toInt()) }
            binding.executePendingBindings()
        }

        private fun openDetail(id: Int) {
            startActivity(
                Intent(
                    this@AnnouncementPage,
                    AnnouncementDetailPage::class.java
                ).putExtra("id", id),
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this@AnnouncementPage,
                    binding.image,
                    "announcement"
                ).toBundle()
            )
        }
    }
}