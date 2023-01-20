package id.onklas.app.pages.presensi

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.AttendSchedulePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.PageDialogFragment
import kotlinx.coroutines.launch

class AttendSchedulePage(val scheduleId: Int, val onDismiss: () -> Unit) : PageDialogFragment() {

    private val viewmodel by activityViewModels<PresensiViewModel> { component.presensiVmFactory }
    private lateinit var binding: AttendSchedulePageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        AttendSchedulePageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { dismiss() }

        binding.inPassword.doAfterTextChanged {
            binding.btnAttend.isEnabled = it.toString().isNotEmpty()
        }
        binding.btnAttend.setOnClickListener {
            lifecycleScope.launch {
//                val progress = ProgressDialog.show(requireContext(), "", "proses masuk kelas")
//                val res = viewmodel.attendClass(
//                    scheduleId,
//                    binding.inPassword.text.toString()
//                )
//                progress.dismiss()
//                if (res) {
//                    onDismiss.invoke()
//                    dismiss()
//                }
            }
        }
    }
}