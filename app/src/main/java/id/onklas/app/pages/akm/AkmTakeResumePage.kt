package id.onklas.app.pages.akm

import android.content.Intent
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import androidx.work.*
import id.onklas.app.R
import id.onklas.app.databinding.AkmTakeExamItemBinding
import id.onklas.app.databinding.AkmTakeInstructionItemBinding
import id.onklas.app.databinding.AkmTakeResumePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.RomanNumber
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class AkmTakeResumePage : Privatepage() {

    private val binding by lazy { AkmTakeResumePageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<AkmViewModel> { component.akmVmFactory }
    private val akmId by lazy { intent.getIntExtra("id", 0) }
    private val isSchoolScope by lazy { intent.getBooleanExtra("isSchoolScope", false) }
    private val examType by lazy { intent.getIntExtra("examType", ExamType.SCHOOL) }
    private val dividerDrawable by lazy {
        ShapeDrawable(RectShape()).apply {
            paint.color = ResourcesCompat.getColor(resources, R.color.gray, null)
            setBounds(0, 0, 5000, resources.getDimensionPixelSize(R.dimen._4sdp))
        }
    }

    init {
        lifecycleScope.launchWhenCreated {
            viewmodel.isSchoolScope = isSchoolScope
            Timber.e("listen akm id: $akmId")
            viewmodel.detailAkm(akmId).collectLatest { data ->
                Timber.e("collect akm: $data")
                if (data.schedule.status == AkmStatus.AKM_STATUS_FINISHED) {
                    alert(
                        msg = "Waktu ujian telah berakhir, jawaban akan dikumpulkan",
                        okLabel = "OK",
                        okClick = {
                            setResult(RESULT_OK, Intent().putExtra("finished", true))
                            finish()
                        }
                    )
                } else if (data.schedule.status == AkmStatus.AKM_STATUS_UPLOADED) {
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launchWhenCreated {
            viewmodel.db.akm().getExamInstructionAsync(akmId).forEach {
                if (it.instructions.all { it.answered == it.num_question }) {
                    viewmodel.db.akm().insertExam(it.exam.copy(finished = true))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            (if (examType == ExamType.SCHOOL) "Kerjakan Ujian" else "Kerjakan Tryout").run {
                binding.toolbar.title = this
                title = this
            }

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.rvType.addItemDecoration(
            DividerItemDecoration(
                this,
                OrientationHelper.VERTICAL
            ).apply { setDrawable(dividerDrawable) })
        binding.rvType.adapter = examAdapter

        binding.btnAction.setOnClickListener {
            loading(msg = "Sedang memproses jawaban")
            viewmodel.uploadAnswer(akmId)

            viewmodel.uploadStatus.observe(this) {
                if (it) {
                    dismissloading()
                    alert(
                        "Berhasil",
                        "Jawaban sedang dikumpulkan, silahkan tunggu sampai jawabanmu dinilai",
                        okClick = {
                            setResult(RESULT_OK, Intent().putExtra("finished", true))
                            finish()
                        }
                    )
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewmodel.db.akm().collapseExam(akmId)
            viewmodel.db.akm().getExamInstruction(akmId).collect {
                binding.btnAction.isEnabled = it.all { examInsts -> examInsts.exam.finished }
                examAdapter.submitList(it.toMutableList())
            }
        }
    }

    private val examAdapter by lazy {
        object : ListAdapter<AkmExamInstruction, ExamVh>(object :
            DiffUtil.ItemCallback<AkmExamInstruction>() {
            override fun areItemsTheSame(
                oldItem: AkmExamInstruction,
                newItem: AkmExamInstruction
            ): Boolean = oldItem.exam.id == newItem.exam.id

            override fun areContentsTheSame(
                oldItem: AkmExamInstruction,
                newItem: AkmExamInstruction
            ): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamVh =
                ExamVh(parent)

            override fun onBindViewHolder(holder: ExamVh, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private val instItemDecoration by lazy {
        LinearSpaceDecoration(
            space = resources.getDimensionPixelSize(
                R.dimen._8sdp
            )
        )
    }

    private inner class ExamVh(
        parent: ViewGroup,
        val binding: AkmTakeExamItemBinding = AkmTakeExamItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        val insAdapter by lazy { InstructionAdapter() }

        init {
            binding.rvInstruction.addItemDecoration(instItemDecoration)
            binding.rvInstruction.adapter = insAdapter
        }

        fun bind(item: AkmExamInstruction) {
            binding.item = item.exam.apply { if (isSchoolScope) type = "Ujian" }
            binding.showChild = true
//            binding.showChild = item.exam.show_child
            insAdapter.submitList(item.instructions)

            binding.name.setOnClickListener { select(item.exam) }
            binding.imgSwitch.setOnClickListener { select(item.exam) }
            binding.status.setOnClickListener { select(item.exam) }
            binding.root.setOnClickListener { select(item.exam) }

            binding.executePendingBindings()
        }

        private fun select(exam: AkmExamsTable) {
            lifecycleScope.launch {
                val newItem = exam.copy().apply { show_child = !exam.show_child }
                viewmodel.db.akm().insertExam(newItem)
            }
        }
    }

    private inner class InstructionAdapter :
        ListAdapter<AkmInstructionTable, InstructionVh>(object :
            DiffUtil.ItemCallback<AkmInstructionTable>() {
            override fun areItemsTheSame(
                oldItem: AkmInstructionTable,
                newItem: AkmInstructionTable
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: AkmInstructionTable,
                newItem: AkmInstructionTable
            ): Boolean = oldItem == newItem
        }) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstructionVh =
            InstructionVh(parent)

        override fun onBindViewHolder(holder: InstructionVh, position: Int) =
            holder.bind(getItem(position))
    }

    private inner class InstructionVh(
        parent: ViewGroup,
        val binding: AkmTakeInstructionItemBinding = AkmTakeInstructionItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AkmInstructionTable) {
            binding.number = RomanNumber.toRoman(adapterPosition + 1)
            binding.item = item
            binding.root.setOnClickListener { select(item) }
            binding.executePendingBindings()
        }

        private fun select(item: AkmInstructionTable) {
            lifecycleScope.launch {
                loading(msg = "menampilkan instruksi")
                startActivity(
                    Intent(this@AkmTakeResumePage, AkmInstructionPage::class.java)
                        .putExtra("id", akmId)
                        .putExtra("instruction_id", item.id)
                        .putExtra("title", viewmodel.db.akm().getExamType(item.exam_id))
                        .putExtra("number", adapterPosition + 1)
                        .putExtra("instruction", item.instruction)
                        .putExtra("description", item.description)
                )
                dismissloading()
            }
        }
    }
}