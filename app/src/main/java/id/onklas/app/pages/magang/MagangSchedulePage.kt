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
import id.onklas.app.databinding.MagangItemBinding
import id.onklas.app.databinding.MagangSchedulePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BaseFragment
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch

class MagangSchedulePage : BaseFragment() {

    private lateinit var binding: MagangSchedulePageBinding
    private val viewModel by activityViewModels<MagangViewModel> { component.magangVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        MagangSchedulePageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        lifecycleScope.launch {
            viewModel.fetchSchedule()
        }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewModel.fetchSchedule()
            }
        }

        viewModel.loadingSchedule.observe(viewLifecycleOwner, binding.swipeRefresh::setRefreshing)

        binding.rvMagangSchedule.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                includeTop = true,
                includeBottom = true,
                customEdge = resources.getDimensionPixelSize(R.dimen._16sdp)
            )
        )
        binding.rvMagangSchedule.adapter = adapter

        viewModel.listJadwal.observe(viewLifecycleOwner, adapter::submitList)
    }

    private val adapter by lazy { MagangAdapter() }

    private inner class MagangAdapter :
        PagedListAdapter<MagangScheduleCompany, Viewholder>(object :
            DiffUtil.ItemCallback<MagangScheduleCompany>() {
            override fun areItemsTheSame(
                oldItem: MagangScheduleCompany,
                newItem: MagangScheduleCompany
            ): Boolean =
                oldItem.schedule.id == newItem.schedule.id

            override fun areContentsTheSame(
                oldItem: MagangScheduleCompany,
                newItem: MagangScheduleCompany
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
        val binding: MagangItemBinding = MagangItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MagangScheduleCompany) {
            binding.item = item

            binding.btnAttend.setOnClickListener {
                MagangAttendPage(item.schedule.id) {
                    lifecycleScope.launch {
                        viewModel.fetchSchedule()
                        viewModel.fetchReport()
                    }
                }.show(childFragmentManager, "")
            }

            binding.btnReport.setOnClickListener {
                MagangWriteReport(
                    scheduleId = item.schedule.id,
                    editable = true,
                    onDismiss = {
                        lifecycleScope.launch {
                            viewModel.fetchSchedule()
                            viewModel.fetchReport()
                        }
                    }
                ).show(childFragmentManager, "")
            }

            binding.executePendingBindings()
        }
    }
}