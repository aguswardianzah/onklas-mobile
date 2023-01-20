package id.onklas.app.pages.magang

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.MagangReportItemBinding
import id.onklas.app.databinding.MagangReportPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BaseFragment
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch

class MagangReportPage : BaseFragment() {

    private lateinit var binding: MagangReportPageBinding
    private val viewModel by activityViewModels<MagangViewModel> { component.magangVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        MagangReportPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        lifecycleScope.launch {
            viewModel.fetchReport()
        }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewModel.fetchReport()
            }
        }

        viewModel.loadingReport.observe(viewLifecycleOwner, binding.swipeRefresh::setRefreshing)

        binding.rvMagangReport.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                includeTop = true,
                includeBottom = true,
                customEdge = resources.getDimensionPixelSize(R.dimen._16sdp)
            )
        )
        binding.rvMagangReport.adapter = adapter

        viewModel.listReport.observe(viewLifecycleOwner, adapter::submitList)
    }

    private val adapter by lazy { MagangAdapter() }

    private inner class MagangAdapter :
        PagedListAdapter<MagangReportEntity, Viewholder>(object :
            DiffUtil.ItemCallback<MagangReportEntity>() {
            override fun areItemsTheSame(
                oldItem: MagangReportEntity,
                newItem: MagangReportEntity
            ): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: MagangReportEntity,
                newItem: MagangReportEntity
            ): Boolean =
                oldItem == newItem
        }) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
            Viewholder(parent)

        override fun onBindViewHolder(holder: Viewholder, position: Int) {
            getItem(position)?.let { holder.bind(it) }
        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: MagangReportItemBinding = MagangReportItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MagangReportEntity) {
            binding.item = item
            binding.viewMore.setOnClickListener {
                MagangWriteReport(content = item.daily_report, scheduleId = item.id).show(
                    childFragmentManager,
                    ""
                )
            }
            binding.executePendingBindings()
        }
    }
}