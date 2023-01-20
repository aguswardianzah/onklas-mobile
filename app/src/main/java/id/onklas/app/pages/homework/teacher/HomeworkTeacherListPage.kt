package id.onklas.app.pages.homework.teacher

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
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.R
import id.onklas.app.databinding.HomeworkTeacherListItemBinding
import id.onklas.app.databinding.HomeworkTeacherListPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.homework.HomeWorkViewModel
import id.onklas.app.pages.homework.HomeworkAdapter
import id.onklas.app.pages.homework.HomeworkDetailPage
import id.onklas.app.pages.homework.HomeworkItemTable
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch

class HomeworkTeacherListPage : Fragment() {

    private lateinit var binding: HomeworkTeacherListPageBinding
    private val viewmodel by activityViewModels<HomeWorkViewModel> { component.homeworkVmFactory }
    private var firstLoad = true
    private lateinit var list: LiveData<PagedList<HomeworkItemTable>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = HomeworkTeacherListPageBinding.inflate(inflater, container, false)
        .also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rvHomework.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                includeTop = true,
                includeBottom = true
            )
        )
        binding.rvHomework.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener(this::refresh)
        viewmodel.classId.observe(viewLifecycleOwner, { refresh() })
        viewmodel.mapel.observe(viewLifecycleOwner, { refresh() })

        refresh()
    }

    private val listObserver by lazy {
        Observer<PagedList<HomeworkItemTable>> {
            adapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager =
                        (binding.rvHomework.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvHomework.scrollToPosition(position)
                    }
                } else {
                    binding.rvHomework.scrollToPosition(0)
                    firstLoad = false
                }
            }
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun refresh() {
        lifecycleScope.launch {
            if (this@HomeworkTeacherListPage::list.isInitialized)
                list.removeObserver(listObserver)
            list = viewmodel.listTeacher()
            list.observe(viewLifecycleOwner, listObserver)
            binding.swipeRefresh.isRefreshing = true
            firstLoad = true
            viewmodel.prefTeacher = -1
            viewmodel.fetchHomeworkTeacher()
        }
    }

    private val adapter by lazy {
        object : HomeworkAdapter<Viewholder>() {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun bindItemViewholder(holder: Viewholder, position: Int) {
                getItem(position)?.let {
                    holder.bind(
                        it,
                        viewmodel.pref.getInt("user_id") == it.teacher?.sosmed_user_id
                    )
                }
            }
        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: HomeworkTeacherListItemBinding = HomeworkTeacherListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeworkItemTable, isMine: Boolean = false) {
            binding.item = item
            binding.isMine = isMine
            binding.executePendingBindings()
            binding.root.setOnClickListener {
                editResult.launch(
                    Intent(
                        requireActivity(),
                        CreateHomeworkPage::class.java
                    ).putExtra("homeworkId", item.homework.id)
                        .putExtra("editable", false)
                )
//                startActivity(
//                    Intent(requireActivity(), HomeworkDetailPage::class.java)
//                        .putExtra("id", item.homework.id)
//                )
            }

            binding.option.setOnClickListener {
                AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                    .setItems(arrayOf("Edit Tugas", "Hapus Tugas")) { dialog, which ->
                        dialog.dismiss()
                        if (which == 0)
                            editResult.launch(
                                Intent(
                                    requireActivity(),
                                    CreateHomeworkPage::class.java
                                ).putExtra("homeworkId", item.homework.id)
                            )
//                            startActivity(
//                                Intent(
//                                    requireActivity(),
//                                    CreateHomeworkPage::class.java
//                                ).putExtra("homeworkId", item.homework.id)
//                            )
                        else
                            MaterialAlertDialogBuilder(requireContext(), R.style.DialogTheme)
                                .setMessage("Anda yakin akan menghapus tugas?")
                                .setPositiveButton("Hapus") { dialog1, _ ->
                                    dialog1.dismiss()
                                    lifecycleScope.launch {
                                        val progress =
                                            ProgressDialog.show(
                                                requireContext(),
                                                "",
                                                "menghapus tugas"
                                            )
                                        viewmodel.deleteHomework(item.homework.id)
                                        progress.dismiss()
                                    }
                                }
                                .setNeutralButton("Batal") { dialog1, _ -> dialog1.dismiss() }
                                .show()
                    }
                    .show()
            }
        }
    }

    private val editResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                lifecycleScope.launchWhenCreated {
                    binding.swipeRefresh.isRefreshing = true
                    refresh()
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
}