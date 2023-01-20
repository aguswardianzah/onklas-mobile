package id.onklas.app.pages.homework.teacher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.HomeworkTeacherListPageBinding
import id.onklas.app.databinding.HomeworkTeacherNilaiItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.homework.HomeWorkViewModel
import id.onklas.app.pages.homework.HomeworkCollected
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch

class HomeworkTeacherNilaiPage : Fragment() {

    private lateinit var binding: HomeworkTeacherListPageBinding
    private val viewmodel by activityViewModels<HomeWorkViewModel> { component.homeworkVmFactory }
    private var firstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = HomeworkTeacherListPageBinding.inflate(inflater, container, false)
        .also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rvHomework.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                includeTop = true,
                includeBottom = true
            )
        )
        binding.rvHomework.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                firstLoad = true
                viewmodel.prefDikumpulkan = -1
                viewmodel.fetchDikumpulkan()
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewmodel.listDikumpulkan.observe(viewLifecycleOwner, {
            adapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager =
                        (binding.rvHomework.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvHomework.scrollToPosition(position)
                    }
                } else {
                    binding.rvHomework.scrollToPosition(0)
                    firstLoad = false
                }
            }
        })

        lifecycleScope.launchWhenCreated {
            viewmodel.fetchDikumpulkan()
        }
    }

    private val adapter by lazy {
        object : PagingAdapter<HomeworkCollected, Viewholder>(
            object : DiffUtil.ItemCallback<HomeworkCollected>() {
                override fun areItemsTheSame(
                    oldItem: HomeworkCollected,
                    newItem: HomeworkCollected
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: HomeworkCollected,
                    newItem: HomeworkCollected
                ): Boolean = oldItem == newItem
            }
        ) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun bindItemViewholder(holder: Viewholder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: Viewholder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                getItem(position)?.let { holder.bind(it) }
            }
        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: HomeworkTeacherNilaiItemBinding = HomeworkTeacherNilaiItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeworkCollected) {
            binding.item = item
            binding.executePendingBindings()
            binding.root.setOnClickListener {
                startActivity(
                    Intent(requireActivity(), HomeworkSubmittedPage::class.java)
                        .putExtra("id", item.id)
                )
            }
        }
    }
}