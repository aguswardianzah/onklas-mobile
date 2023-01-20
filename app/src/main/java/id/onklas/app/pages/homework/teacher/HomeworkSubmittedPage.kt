package id.onklas.app.pages.homework.teacher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import id.onklas.app.R
import id.onklas.app.databinding.AssignmentItemBinding
import id.onklas.app.databinding.HomeworkSubmittedPagedBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.homework.Assignment
import id.onklas.app.pages.homework.HomeWorkViewModel
import id.onklas.app.pages.homework.HomeworkDetailPage
import id.onklas.app.pages.pembayaran.RowAdapter
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.NoFilterArrayAdapter
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class HomeworkSubmittedPage : Privatepage() {

    private val binding by lazy { HomeworkSubmittedPagedBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<HomeWorkViewModel> { component.homeworkVmFactory }
    private var masterList = ArrayList<Assignment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        viewmodel.errorString.observe(this, Observer(this::toast))

        binding.layoutDetail.rvDetail.addItemDecoration(
            DividerItemDecoration(this, OrientationHelper.VERTICAL)
        )
        binding.rvAssignment.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                includeTop = true,
                includeBottom = true,
                customEdge = resources.getDimensionPixelSize(R.dimen._12sdp)
            )
        )
        binding.btnDetail.setOnClickListener {
            Timber.e("cuki")
            if (binding.layoutDetail.mainLayout.isShown) {
                binding.layoutDetail.mainLayout.visibility = View.GONE
                binding.carret.rotation = 90f
            } else {
                binding.layoutDetail.mainLayout.visibility = View.VISIBLE
                binding.carret.rotation = 270f
            }
        }

        fetchData(intent.getIntExtra("id", 0))
    }

    private fun fetchData(id: Int) {
        lifecycleScope.launchWhenCreated {
            loading(msg = "sedang menampilkan data")
            val response = viewmodel.getAssigmentDetail(id)
            dismissloading()

            response?.let {
                masterList.clear()
                masterList.addAll(it.data.assignments)

                binding.layoutDetail.rvDetail.adapter = RowAdapter(
                    arrayListOf(
                        "Terkumpul" to "${it.data.count_assignment_collected}/${it.data.count_assignment_collected_all}",
                        "Kelas" to it.data.`class`.name,
                        "Mata Pelajaran" to it.data.subject.name,
                        "Tugas Diupload" to it.data.upload_at_label,
                        "Tugas Berakhir" to it.data.end_at_label
                    )
                )
                binding.layoutDetail.btnLihat.setOnClickListener {
                    startActivity(
                        Intent(this@HomeworkSubmittedPage, HomeworkDetailPage::class.java)
                            .putExtra("id", id)
                    )
                }

                binding.rvAssignment.adapter = adapter
                adapter.submitList(masterList)

                binding.inputClass.setAdapter(
                    NoFilterArrayAdapter(
                        this@HomeworkSubmittedPage,
                        listOf("Semua", "Belum Dinilai", "Sudah Dinilai")
                    )
                )
                binding.inputClass.doAfterTextChanged {
                    adapter.submitList(
                        when (binding.inputClass.text.toString()) {
                            "Belum Dinilai" -> masterList.filter { it.scored == 0 }
                            "Sudah Dinilai" -> masterList.filter { it.scored == 1 }
                            else -> masterList
                        }
                    )
                }
            } ?: alert(msg = "Halaman tidak tersedia", okClick = { finish() })
        }
    }

    private val adapter by lazy {
        object : ListAdapter<Assignment, Viewholder>(
            object : DiffUtil.ItemCallback<Assignment>() {
                override fun areItemsTheSame(oldItem: Assignment, newItem: Assignment): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: Assignment, newItem: Assignment): Boolean =
                    oldItem == newItem
            }
        ) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: AssignmentItemBinding = AssignmentItemBinding.inflate(
            LayoutInflater.from(
                parent.context
            ), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Assignment) {
            binding.item = item
            binding.executePendingBindings()
            binding.lihat.setOnClickListener {
//                lifecycleScope.launchWhenCreated {
//                    loading(title = "menampilkan data")
//                    val download = component.apiService.download(item.file_path)
//                    val file = File(
//                        filesDir,
//                        item.file_name
//                    ).also { if (!it.exists()) it.createNewFile() }
//
//                    FileOutputStream(file).apply {
//                        write(download.bytes())
//                        flush()
//                        close()
//                    }
//
//                    viewmodel.intentUtil.openFile(
//                        this@HomeworkSubmittedPage,
//                        file,
//                        item.file_name
//                    )
//                    dismissloading()
//                }
//                viewmodel.intentUtil.openPdf(
//                    this@HomeworkSubmittedPage,
//                    item.file_path,
//                    item.file_name
//                )
            }
            binding.nilai.setOnClickListener {
                HomeworkScoringPage(intent.getIntExtra("id", 0), item) {
                    fetchData(intent.getIntExtra("id", 0))
                }.show(supportFragmentManager, "")
            }
        }
    }
}