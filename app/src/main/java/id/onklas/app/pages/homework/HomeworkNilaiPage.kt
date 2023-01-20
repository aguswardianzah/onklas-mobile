package id.onklas.app.pages.homework

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
import id.onklas.app.databinding.HomeworkNilaiItemBinding
import id.onklas.app.databinding.HomeworkNilaiPageBinding
import id.onklas.app.di.component
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch

class HomeworkNilaiPage : Fragment() {

    private lateinit var binding: HomeworkNilaiPageBinding
    private val viewmodel by activityViewModels<HomeWorkViewModel> { component.homeworkVmFactory }
    private var firstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = HomeworkNilaiPageBinding.inflate(layoutInflater, container, false)
        .also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rvNilai.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                includeTop = true,
                includeBottom = true
            )
        )
        binding.rvNilai.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                firstLoad = true
                viewmodel.prefNilai = -1
                viewmodel.fetchHomeworkNilai()
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewmodel.listNilai.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager =
                        (binding.rvNilai.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvNilai.scrollToPosition(position)
                    }
                } else {
                    binding.rvNilai.scrollToPosition(0)
                    firstLoad = false
                }
            }
        })

        lifecycleScope.launchWhenCreated {
            viewmodel.fetchHomeworkNilai()
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
        val binding: HomeworkNilaiItemBinding = HomeworkNilaiItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeworkItemTable) {
            binding.item = item
            binding.executePendingBindings()
            binding.root.setOnClickListener {
                HomeworkNilaiDetailPage(item).show(childFragmentManager, "")
            }
        }
    }
}