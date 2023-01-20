package id.onklas.app.pages.presensi

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import id.onklas.app.databinding.PresensiDetailPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.PageDialogFragment
import id.onklas.app.pages.pembayaran.RowAdapter
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class PresensiDetailPage(val scheduleId: Int, val date: String) : PageDialogFragment() {

    private lateinit var binding: PresensiDetailPageBinding
    private val viewmodel by activityViewModels<PresensiViewModel> { component.presensiVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        PresensiDetailPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { dismiss() }

        binding.rvInfo.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                OrientationHelper.VERTICAL
            )
        )

        binding.rvAttendance.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                OrientationHelper.VERTICAL
            )
        )

        binding.swipeRefresh.setOnRefreshListener(this::init)
        binding.swipeRefresh.isRefreshing = true
        init()
    }

    private fun init() {
        lifecycleScope.launch {
            viewmodel.detailClass(scheduleId, date).observe(viewLifecycleOwner, observer)
        }
    }

    private val observer by lazy {
        Observer<ScheduleDetailAttendance> { item ->
            try {
                binding.swipeRefresh.isRefreshing = false
                binding.item = item
                binding.rvInfo.adapter = RowAdapter(
                    arrayListOf(
                        "Pengajar" to item.detail.teacher_name,
                        "Kelas" to item.detail.class_name + " " + item.detail.school_major_name,
                        "Sekolah" to item.detail.school_name,
                        "Jadwal" to item.detail.plot_start_at + " - " + item.detail.plot_end_at,
                        "Waktu Mulai" to item.detail.attend_at,
                        "Waktu Selesai" to item.detail.leave_at,
                        "Password" to item.detail.class_password,
                        "Kehadiran" to "${item.attendances.count { it.is_present }}/${item.detail.total_student}"
                    )
                )

                if (item.attendances.isNotEmpty()) {
                    binding.attendanceLabel.visibility = View.VISIBLE
                    binding.attendancePresent.visibility = View.VISIBLE
                    binding.attendanceAbsent.visibility = View.VISIBLE
                    binding.attendanceLate.visibility = View.VISIBLE
                    binding.attendanceTotal.visibility = View.VISIBLE

                    binding.attendancePresent.text =
                        "Hadir: ${item.attendances.count { it.is_present }}"
                    binding.attendanceAbsent.text =
                        "Absen: ${item.attendances.count { !it.is_present }}"
                    binding.attendanceLate.text =
                        "Terlambat: ${item.attendances.count { it.is_late }}"
                    binding.attendanceTotal.text = "Total: ${item.attendances.size}"

                    binding.rvAttendance.adapter = RowAdapter(
                        item.attendances.sortedWith { o1, o2 ->
                            "${o1?.is_present}_${o1?.is_late}_${o1?.student_name?.lowercase()}".compareTo(
                                "${o2.is_present}_${o2.is_late}_${o2.student_name.lowercase()}"
                            )
                        }.map {
                            "${it.student_name}\n${it.nisn}" to if (it.is_late) "Terlambat" else if (!it.is_present) "Tidak Hadir" else ""
                        } as ArrayList<Pair<String, String>>
                    )
                } else {
                    binding.attendanceLabel.visibility = View.GONE
                    binding.attendancePresent.visibility = View.GONE
                    binding.attendanceAbsent.visibility = View.GONE
                    binding.attendanceLate.visibility = View.GONE
                    binding.attendanceTotal.visibility = View.GONE
                }

                binding.executePendingBindings()
            } catch (e: Exception) {
            }
        }
    }
}