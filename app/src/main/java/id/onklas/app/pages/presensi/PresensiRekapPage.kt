package id.onklas.app.pages.presensi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import id.onklas.app.databinding.PresensiRekapItemBinding
import id.onklas.app.databinding.PresensiRekapPageBinding
import id.onklas.app.di.component
import timber.log.Timber
import java.util.*

class PresensiRekapPage : Fragment() {

    private val viewmodel by activityViewModels<PresensiViewModel> { component.presensiVmFactory }
    private lateinit var binding: PresensiRekapPageBinding

//    init {
//        lifecycleScope.launchWhenCreated {
//            if (viewmodel.isStudent)
//                viewmodel.apiService.studentAnnualAttendance(
//                    Calendar.getInstance().get(Calendar.YEAR)
//                )
//            else viewmodel.apiService.teacherAnnualAttendance(
//                Calendar.getInstance().get(Calendar.YEAR)
//            )
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        PresensiRekapPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener {
            viewmodel.getRekapAbsensi()
        }

        binding.rvKelas.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                OrientationHelper.VERTICAL
            )
        )
        binding.rvKelas.adapter = adapter
        viewmodel.listRekapAbsensi.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            binding.swipeRefresh.isRefreshing = false
        })

        viewmodel.getRekapAbsensi()
    }

    private val adapter by lazy {
        object : ListAdapter<RekapAbsensiTable, Viewholder>(
            object : DiffUtil.ItemCallback<RekapAbsensiTable>() {
                override fun areItemsTheSame(
                    oldItem: RekapAbsensiTable,
                    newItem: RekapAbsensiTable
                ): Boolean = oldItem.date == newItem.date

                override fun areContentsTheSame(
                    oldItem: RekapAbsensiTable,
                    newItem: RekapAbsensiTable
                ): Boolean = oldItem == newItem
            }
        ) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private class Viewholder(
        parent: ViewGroup,
        val binding: PresensiRekapItemBinding = PresensiRekapItemBinding.inflate(
            LayoutInflater.from(
                parent.context
            ), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RekapAbsensiTable) {
            binding.item = item
            binding.executePendingBindings()
        }
    }
}