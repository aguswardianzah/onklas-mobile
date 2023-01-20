package id.onklas.app.pages.homework.teacher

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import id.onklas.app.databinding.HomeworkScoringPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.PageDialogFragment
import id.onklas.app.pages.homework.Assignment
import id.onklas.app.pages.homework.HomeWorkViewModel
import id.onklas.app.pages.pembayaran.RowAdapter
import java.io.File
import java.io.FileOutputStream

class HomeworkScoringPage(val collectId: Int, val item: Assignment, val onDismiss: () -> Unit) :
    PageDialogFragment() {

    private lateinit var binding: HomeworkScoringPageBinding
    private val viewmodel by activityViewModels<HomeWorkViewModel> { component.homeworkVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        HomeworkScoringPageBinding.inflate(inflater, container, false).also { binding = it }.root

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { dismiss() }

        binding.item = item

        if (item.file_path.isNotEmpty()) {
            val uri = Uri.parse(item.file_path.replace("\\", ""))
            val size =
                if (item.file_size.isEmpty()) 0L else item.file_size.toLong()
            binding.answerLayout.name.text =
                "${item.file_name} | ${
                    viewmodel.fileUtils.getStringSizeLengthFile(
                        size
                    )
                }"
            binding.answerLayout.btnLihat.text = "baca jawaban"
            binding.answerLayout.btnLihat.setOnClickListener {
                lifecycleScope.launchWhenCreated {
                    try {
                        loading(title = "menampilkan data")
                        val download = component.apiService.download(uri.toString())
                        val file = File(
                            requireContext().filesDir,
                            uri.lastPathSegment ?: "Jawaban ${item.student.name} $collectId"
                        ).also { if (!it.exists()) it.createNewFile() }

                        FileOutputStream(file).apply {
                            write(download.bytes())
                            flush()
                            close()
                        }

                        viewmodel.intentUtil.openFile(
                            requireActivity(),
                            file,
                            "Jawaban ${item.student.name}"
                        )
                    } catch (e: Exception) {
                        toast(e.message)
                    } finally {
                        dismissloading()
                    }
                }
            }
            binding.answerLayout.btnDownload.setOnClickListener {
                viewmodel.intentUtil.downloadFile(
                    requireActivity(),
                    uri,
                    item.file_name,
                    "jawaban"
                )
            }
        }

        if (item.uri_student != null && item.uri_student.link.isNotEmpty()) {
            lifecycleScope.launchWhenCreated {
                binding.uploadLinkPreview.loadUrl(
                    viewmodel.apiService, item.uri_student.link.first()
                )
            }
        }

        binding.rvInfo.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                OrientationHelper.VERTICAL
            )
        )
        binding.rvInfo.adapter = RowAdapter(
            arrayListOf(
                "Peserta" to item.student.name,
                "NIS" to item.student.nisn,
                "Kelas" to item.student.`class`,
                "Sekolah" to item.subject_assignment.school.name
            )
        )

        binding.score.filters = arrayOf(object : InputFilter {
            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                try {
                    val input =
                        (dest!!.subSequence(0, dstart).toString() + source + dest.subSequence(
                            dend,
                            dest.length
                        )).toInt()
                    if (input in 0..100) return null
                } catch (nfe: NumberFormatException) {
                }
                return ""
            }
        })
        binding.score.doAfterTextChanged {
            binding.btnSave.isEnabled = binding.score.text.isNotEmpty()
        }

        binding.btnSave.setOnClickListener {
            lifecycleScope.launchWhenCreated {
                val progress = ProgressDialog.show(requireContext(), "", "sedang mengirimkan nilai")
                val success = viewmodel.setAssignmentScore(
                    collectId,
                    item.id,
                    binding.score.text.toString().toInt()
                )
                progress.dismiss()
                if (success) {
                    onDismiss.invoke()
                    dismiss()
                }
            }
        }
        binding.executePendingBindings()

        viewmodel.errorString.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        })
    }
}