package id.onklas.app.pages.homework

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.HomeworkSudahItemBinding
import id.onklas.app.databinding.HomeworkSudahPageBinding
import id.onklas.app.di.component
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch

class HomeworkSudahPage : Fragment() {

    private lateinit var binding: HomeworkSudahPageBinding
    private val viewmodel by activityViewModels<HomeWorkViewModel> { component.homeworkVmFactory }
    private var firstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        HomeworkSudahPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rvSudah.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                includeTop = true,
                includeBottom = true
            )
        )
        binding.rvSudah.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                firstLoad = true
                viewmodel.prefSudah = -1
                viewmodel.fetchHomeworkSudah()
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewmodel.listSudah.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager =
                        (binding.rvSudah.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvSudah.scrollToPosition(position)
                    }
                } else {
                    binding.rvSudah.scrollToPosition(0)
                    firstLoad = false
                }
            }
        })

        lifecycleScope.launchWhenCreated {
            viewmodel.fetchHomeworkSudah()
        }
    }

    private val adapter by lazy {
        object : HomeworkAdapter<Viewholder>() {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun bindItemViewholder(holder: Viewholder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }
        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: HomeworkSudahItemBinding = HomeworkSudahItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeworkItemTable) {
            binding.item = item
            binding.executePendingBindings()
            binding.root.setOnClickListener {
                startActivity(
                    Intent(requireActivity(), HomeworkDetailPage::class.java)
                        .putExtra("id", item.homework.id)
                        .putExtra("isFinished", true)
                )
            }
        }
    }
}