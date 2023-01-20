package id.onklas.app.pages.presensi

import android.app.ProgressDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.R
import id.onklas.app.databinding.PresensiKelasItemBinding
import id.onklas.app.databinding.PresensiKelasPageBinding
import id.onklas.app.databinding.PresensiTeacherKelasItemBinding
import id.onklas.app.di.component
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class PresensiKelasPage : Fragment() {

    private lateinit var binding: PresensiKelasPageBinding
    private val viewmodel by activityViewModels<PresensiViewModel> { component.presensiVmFactory }
    private val timers by lazy { HashMap<Int, CountDownTimer>() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        PresensiKelasPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewmodel

        binding.swipeRefresh.setOnRefreshListener {
            viewmodel.getSchedule()
        }
        viewmodel.loadingKelas.observe(
            viewLifecycleOwner,
            Observer(binding.swipeRefresh::setRefreshing)
        )

        binding.rvKelas.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                includeTop = true,
                includeBottom = true
            )
        )
        binding.rvKelas.adapter = adapter
        viewmodel.listJadwal.observe(viewLifecycleOwner) {
            adapter.submitList(it) {
                it.forEachIndexed { index, item ->
                    if (item.late_at.isNotEmpty() && item.status != "passed") {
                        try {
                            val lateTime =
                                Calendar.getInstance()
                                    .apply { time = viewmodel.fullFormat.parse(item.late_at) }
                            val nowTime = Calendar.getInstance()
                            if (nowTime.timeInMillis < lateTime.timeInMillis) {
                                val timeLeftMillis = lateTime.timeInMillis - nowTime.timeInMillis
                                timers[item.id]?.cancel()
                                timers[item.id] = object : CountDownTimer(timeLeftMillis, 1000) {
                                    override fun onTick(millisUntilFinished: Long) {
                                        item.timeLeft = formatMinute(millisUntilFinished / 1000)
                                        adapter.notifyItemChanged(index, item.timeLeft)
                                    }

                                    override fun onFinish() {
                                        item.status = "late"
                                        adapter.notifyItemChanged(index)
                                    }
                                }.start()
                            } else {
                                item.status = "late"
                                adapter.notifyItemChanged(index)
                            }
                        } catch (e: Exception) {
                            Timber.e(e)
                        }
                    }
                }
            }
            binding.swipeRefresh.isRefreshing = false
        }

        viewmodel.getSchedule()
    }

    private fun formatMinute(secUntilFinish: Long): String {
        val minute = secUntilFinish / 60
        val sec = secUntilFinish % 60
        return "$minute:$sec"
    }

    private val adapter by lazy {
        ScheduleAdapter(viewmodel.pref.getBoolean("is_student"))
    }

    @Suppress("UNCHECKED_CAST")
    private inner class ScheduleAdapter(val isStudent: Boolean = true) :
        ListAdapter<ScheduleTable, RecyclerView.ViewHolder>(
            object : DiffUtil.ItemCallback<ScheduleTable>() {
                override fun areItemsTheSame(
                    oldItem: ScheduleTable,
                    newItem: ScheduleTable
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: ScheduleTable,
                    newItem: ScheduleTable
                ): Boolean = oldItem == newItem
            }
        ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            if (isStudent) Viewholder(parent) else TeacherViewholder(parent)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (isStudent)
                (holder as? Viewholder)?.bind(getItem(position))
            else
                (holder as? TeacherViewholder)?.bind(getItem(position))
        }

        override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int,
            payloads: MutableList<Any>
        ) {
            if (payloads.isNotEmpty() && holder is Viewholder)
                holder.updateTime(payloads.first().toString())
            else
                onBindViewHolder(holder, position)
        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: PresensiKelasItemBinding = PresensiKelasItemBinding.inflate(
            LayoutInflater.from(
                parent.context
            ), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScheduleTable) {
            binding.item = item
            binding.timeLeft = item.timeLeft
            binding.executePendingBindings()
            binding.btnAttend.setOnClickListener {
                PresensiMasukKelasPage(item.id) { viewmodel.getSchedule() }.show(
                    childFragmentManager,
                    ""
                )
            }
            binding.btnLeave.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext(), R.style.DialogTheme)
                    .setTitle("Konfirmasi")
                    .setMessage("Anda yakin akan meninggalkan kelas?")
                    .setPositiveButton("Keluar") { dialog, _ ->
                        lifecycleScope.launch {
                            dialog.dismiss()
                            val progress = ProgressDialog.show(requireContext(), "", "keluar kelas")
                            val res = viewmodel.leaveClass(item.id)
                            if (res)
                                viewmodel.getSchedule()
                            progress.dismiss()
                        }
                    }
                    .setNeutralButton("Batal") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }

        fun updateTime(timeLeft: String) {
            binding.timeLeft = timeLeft
        }
    }

    private inner class TeacherViewholder(
        parent: ViewGroup,
        val binding: PresensiTeacherKelasItemBinding = PresensiTeacherKelasItemBinding.inflate(
            LayoutInflater.from(
                parent.context
            ), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScheduleTable) {
            binding.item = item
            binding.executePendingBindings()
            binding.detail.setOnClickListener {
                PresensiDetailPage(item.id, item.date).show(
                    childFragmentManager,
                    ""
                )
            }
            binding.btnAttend.setOnClickListener {
                StartSchedulePage(item.id) { viewmodel.getSchedule() }.show(
                    childFragmentManager,
                    ""
                )
            }
            binding.btnEnd.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext(), R.style.DialogTheme)
                    .setTitle("Konfirmasi")
                    .setMessage("Akhiri kelas sekarang?")
                    .setPositiveButton("Akhiri") { dialog, _ ->
                        lifecycleScope.launch {
                            dialog.dismiss()
                            val progress =
                                ProgressDialog.show(requireContext(), "", "mengakhiri kelas")
                            val res = viewmodel.endClass(item.id)
                            if (res)
                                viewmodel.getSchedule()
                            progress.dismiss()
                        }
                    }
                    .setNeutralButton("Batal") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }
    }
}