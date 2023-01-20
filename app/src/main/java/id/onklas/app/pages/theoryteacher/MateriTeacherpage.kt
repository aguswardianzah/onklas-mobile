package id.onklas.app.pages.theoryteacher

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.R
import id.onklas.app.databinding.MateriTeacherPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.homework.ClassRoomTable
import id.onklas.app.pages.homework.HomeworkItemTable
import id.onklas.app.pages.theory.*
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.NoFilterArrayAdapter
import kotlinx.coroutines.launch

class MateriTeacherpage : Fragment() {

    private lateinit var binding: MateriTeacherPageBinding
    private val viewmodel by activityViewModels<TheoryViewModel> { component.theoryVmFactory }
    private var firstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        MateriTeacherPageBinding.inflate(inflater, container, false).also { binding = it }.root

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                firstLoad = true
                viewmodel.prevStartTeacherMateri = -1
                viewmodel.hasNextTeacherMateri = true
                viewmodel.fetchMateriTeacher(0)
                binding.swipeRefresh.isRefreshing = false
            }
        }

        binding.rvMateri.adapter = adapter
        binding.rvMateri.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(
                    R.dimen._8sdp
                ), includeTop = true, includeBottom = true
            )
        )

        lifecycleScope.launchWhenCreated {
            val listClass = mutableListOf(ClassRoomTable(name = "Semua")).apply {
                addAll(
                    viewmodel.db.homework().getListClassroom().let {
                        (if (it.isEmpty()) viewmodel.fetchClassRoom() else it).sortedBy { it.name }
                    }
                )
            }
            binding.inputClass.setAdapter(
                NoFilterArrayAdapter(
                    requireContext(),
                    listClass.map { it.name })
            )
            binding.inputClass.setText("Semua")
            binding.inputClass.setOnItemClickListener { parent, view, position, id ->
                lifecycleScope.launchWhenCreated {
                    viewmodel.hasNextTeacherMateri = true
                    viewmodel.prevStartTeacherMateri = -1
                    viewmodel.listTeacherMateri(
                        null,
                        if (position == 0) null else listClass[position].id
                    )
                        .observe(viewLifecycleOwner, listObserver)
                }
            }

            val listMapel = mutableListOf(MapelTable(name = "Semua")).apply {
                addAll(
                    viewmodel.db.theory().getListMapel().let {
                        (if (it.isEmpty()) viewmodel.fetchMapel2() else it).sortedBy { it.name }
                    }
                )
            }
            binding.inputMapel.setAdapter(
                NoFilterArrayAdapter(
                    requireContext(),
                    listMapel.map { it.name })
            )
            binding.inputMapel.setText("Semua")
            binding.inputMapel.setOnItemClickListener { parent, view, position, id ->
                lifecycleScope.launchWhenCreated {
                    viewmodel.hasNextTeacherMateri = true
                    viewmodel.prevStartTeacherMateri = -1
                    viewmodel.listTeacherMateri(
                        if (position == 0) null else listMapel[position].id.toInt(),
                        null
                    )
                        .observe(viewLifecycleOwner, listObserver)
                }
            }

            viewmodel.listTeacherMateri().observe(viewLifecycleOwner, listObserver)
        }

//        viewmodel.listTeacherMateri.observe(viewLifecycleOwner, {
//            adapter.submitList(it) {
//                if (!firstLoad) {
//                    val layoutManager =
//                        (binding.rvMateri.layoutManager as LinearLayoutManager)
//                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
//                    if (position != RecyclerView.NO_POSITION) {
//                        binding.rvMateri.scrollToPosition(position)
//                    }
//                } else {
//                    binding.rvMateri.scrollToPosition(0)
//                    firstLoad = false
//                }
//            }
//        })
    }

    private val listObserver by lazy {
        Observer<PagedList<MateriMapelTeacher>> {
            adapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager =
                        (binding.rvMateri.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvMateri.scrollToPosition(position)
                    }
                } else {
                    binding.rvMateri.scrollToPosition(0)
                    firstLoad = false
                }
            }
        }
    }

    private val adapter by lazy {
        MateriAdapter(
            { item, view ->
                startActivity(
                    Intent(requireActivity(), MateriDetailPage::class.java)
                        .putExtra("title", item.materi.name)
                        .putExtra("id", item.materi.id.toInt())
                        .putExtra("subId", item.mapel.id.toInt())
                )
            },
            viewmodel.pref.getInt("user_id"),
            { item ->
                AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                    .setItems(arrayOf("Edit Materi", "Hapus Materi")) { dialog, which ->
                        dialog.dismiss()
                        if (which == 0) {
                            editResult.launch(
                                Intent(
                                    requireActivity(),
                                    UploadMateriPage::class.java
                                ).putExtra("materiId", item.materi.id.toInt())
                            )
                        } else
                            MaterialAlertDialogBuilder(requireContext(), R.style.DialogTheme)
                                .setMessage("Anda yakin akan menghapus materi?")
                                .setPositiveButton("Hapus") { dialog1, _ ->
                                    dialog1.dismiss()
//
                                    lifecycleScope.launch {
                                        val progress =
                                            ProgressDialog.show(
                                                requireContext(),
                                                "",
                                                "menghapus materi"
                                            )
                                        val result = viewmodel.deleteMateri(item.materi.id)
                                        progress.dismiss()
                                        if (result) {
                                            viewmodel.db.theory().deleteMateri(item.materi)
                                        }
                                    }
                                }
                                .setNeutralButton("Batal") { dialog1, _ -> dialog1.dismiss() }
                                .show()
                    }
                    .show()
            })
    }

    private val editResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                lifecycleScope.launchWhenCreated {
                    binding.swipeRefresh.isRefreshing = true
                    viewmodel.fetchMateriTeacher()
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
}