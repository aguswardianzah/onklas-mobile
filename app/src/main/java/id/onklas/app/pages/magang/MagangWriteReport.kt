package id.onklas.app.pages.magang

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import id.onklas.app.R
import id.onklas.app.databinding.MagangWriteReportBinding
import id.onklas.app.di.component
import id.onklas.app.pages.PageDialogFragment

class MagangWriteReport(
    val scheduleId: Int,
    val onDismiss: () -> Unit = {},
    val editable: Boolean = false,
    val content: String = ""
) : PageDialogFragment() {

    private lateinit var binding: MagangWriteReportBinding
    private val viewModel by activityViewModels<MagangViewModel> { component.magangVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        MagangWriteReportBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { dismiss() }

        if (!editable)
            binding.toolbar.menu.clear()

        if (!editable) {
            binding.input.hint = ""
            binding.input.isEnabled = editable
        }

        binding.input.setText(content)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.toolbar.menu.findItem(R.id.menu_save)?.apply {
            isEnabled = true
            setOnMenuItemClickListener {
                alert(
                    title = "Presensi Keluar",
                    msg = "Simpan laporan dan presensi keluar masuk sekarang?",
                    okLabel = "Konfirmasi",
                    okClick = {
                        lifecycleScope.launchWhenCreated {
                            loading()
                            viewModel.sendReport(scheduleId, binding.input.text.toString())
                            dismissloading()

                            toast("Laporan tersimpan")
                            onDismiss.invoke()
                            dismiss()
                        }
                    }
                )

                true
            }
        }
    }
}