package id.onklas.app.pages.akm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import androidx.work.WorkManager
import id.onklas.app.R
import id.onklas.app.databinding.AkmDetailBebanSoalItemBinding
import id.onklas.app.databinding.AkmDetailPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.pembayaran.RowAdapter
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class AkmDetailPage : Privatepage() {

    private val binding by lazy { AkmDetailPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<AkmViewModel> { component.akmVmFactory }
    private val akmId by lazy { intent.getIntExtra("id", 0) }
    private val isSchoolScope by lazy { intent.getBooleanExtra("isSchoolScope", false) }
    private val dateFormat by lazy { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id")) }
    private var numQuestions = 0
    private val examType by lazy { intent.getIntExtra("examType", ExamType.SCHOOL) }

    init {
        lifecycleScope.launchWhenCreated {
            viewmodel.isSchoolScope = isSchoolScope
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.title = if(examType == ExamType.TRYOUT) "Try Out" else if (isSchoolScope) "Detail Ujian" else "Tes AKM"
            title = if(examType == ExamType.TRYOUT) "Try Out" else if (isSchoolScope) "Detail Ujian" else "Tes AKM"
            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.rvInfo.addItemDecoration(DividerItemDecoration(this, OrientationHelper.VERTICAL))
        binding.rvTypes.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._4sdp)
            )
        )

        binding.bebanLabel.text = if(examType == ExamType.TRYOUT) "Jumlah Soal" else if (isSchoolScope) "Jumlah soal: " else "Beban soal:"
        binding.btnAction.text = if(examType == ExamType.TRYOUT) "Mulai Try Out" else if (isSchoolScope) "Mulai Ujian" else "Mulai AKM"

        viewmodel.loadingDetail.observe(this) {
            if (it)
                loading(msg = "menampilkan detail ujian")
            else dismissloading()
        }

        lifecycleScope.launchWhenCreated {
            viewmodel.fetchDetailAkm(akmId, examType)
            viewmodel.detailAkm(akmId).collectLatest { data ->
//                Timber.e("detail data: $data")
                binding.akmName.text = data.schedule.name
                binding.akmType.text = data.schedule.type
                binding.info.text =
                    "${if(examType == ExamType.TRYOUT) "Try Out" else if (isSchoolScope) "Ujian" else "AKM"} berakhir: ${dateFormat.format(data.schedule.date_end)}"

                binding.rvInfo.adapter = RowAdapter(
                    arrayListOf(
                        "Peserta" to viewmodel.student.name,
                        "NIS" to if (viewmodel.student.nis.isNotEmpty()) viewmodel.student.nis else viewmodel.student.nisn,
                        "Kelas" to viewmodel.student.student_class.class_room.name,
                        "Level" to data.schedule.level.toString(),
                        "Sekolah" to viewmodel.school.name,
                    )
                )

                binding.rvTypes.adapter = BebanAdapter(data.exams.filter { it.num_question > 0 })

                binding.downloadProgress.visibility = View.GONE
                binding.progressLabel.visibility = View.GONE
                binding.btnAction.visibility = View.GONE

                when (data.schedule.status) {
//                    in arrayOf(AKM_STATUS_NEW, AKM_STATUS_FINISHED) -> {
                    AkmStatus.AKM_STATUS_NEW -> {
                        binding.btnAction.apply {
                            WorkManager.getInstance(applicationContext)
                                .cancelUniqueWork("akm_uploader_$akmId")

                            visibility = View.VISIBLE
                            text = "Download Soal"
                            setOnClickListener {
                                visibility = View.GONE
                                binding.downloadProgress.visibility = View.VISIBLE
                                binding.progressLabel.visibility = View.VISIBLE
                                binding.progressLabel.text =
                                    "mendownload soal (${data.schedule.download_progress}/$numQuestions)"

                                val workId = viewmodel.downloadSoal(akmId)

                                WorkManager.getInstance(applicationContext)
                                    .getWorkInfoByIdLiveData(workId)
                                    .observe(this@AkmDetailPage) { workInfo: WorkInfo? ->
                                        if (workInfo != null && workInfo.state == WorkInfo.State.FAILED) {
                                            Timber.e("work failed with output: ${workInfo.outputData}")
                                            val message = workInfo.outputData.getString("message")
                                            toast("Terjadi kesalahan pada proses download soal${if (message.isNullOrEmpty()) ", silahkan ulangi download soal" else message}")
                                            binding.btnAction.visibility = View.VISIBLE
                                            binding.downloadProgress.visibility = View.GONE
                                            binding.progressLabel.visibility = View.GONE
                                        }
                                    }
                            }
                        }
                    }

                    AkmStatus.AKM_STATUS_DOWNLOADING -> {
                        numQuestions = data.exams.sumBy { it.num_question }

                        binding.downloadProgress.visibility = View.VISIBLE
                        binding.downloadProgress.max = numQuestions
                        binding.downloadProgress.progress = data.schedule.download_progress

                        binding.progressLabel.visibility = View.VISIBLE
                        binding.progressLabel.text =
                            "mendownload soal (${data.schedule.download_progress}/$numQuestions)"
                    }

//                    in arrayOf(AKM_STATUS_DOWNLOADED, AKM_STATUS_FINISHED) -> {
                    AkmStatus.AKM_STATUS_DOWNLOADED -> {
                        if (Date() > data.schedule.date_start) {
                            binding.btnAction.apply {
                                visibility = View.VISIBLE
                                text = if(examType == ExamType.TRYOUT) "Mulai Try Out" else if (isSchoolScope) "Mulai Ujian" else "Mulai AKM"
                                setOnClickListener {
                                    // set timer to end exam based on schedule
                                    startActivityForResult(
                                        Intent(
                                            this@AkmDetailPage,
                                            AkmTakeResumePage::class.java
                                        )
                                            .putExtra("id", akmId)
                                            .putExtra("isSchoolScope", isSchoolScope)
                                            .putExtra("examType", examType)
                                        , 129
                                    )
                                }
                            }
                        } else {
                            binding.progressLabel.visibility = View.VISIBLE
                            binding.progressLabel.text = "Ujian belum dimulai"
                        }
                    }

                    in arrayOf(AkmStatus.AKM_STATUS_FINISHED, AkmStatus.AKM_STATUS_UPLOADED) -> {
                        binding.progressLabel.visibility = View.VISIBLE
                        binding.progressLabel.text = "Ujian sudah selesai"
                    }

                    AkmStatus.AKM_STATUS_SCORED -> {
                        binding.btnAction.apply {
                            visibility = View.VISIBLE
                            text = "Lihat Nilai"
                            setOnClickListener {
                                startActivityForResult(
                                    Intent(
                                        this@AkmDetailPage,
                                        AkmScoreDetailPage::class.java
                                    ).putExtra("id", akmId), 219
                                )
                            }

                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 129 && resultCode == RESULT_OK) {
            if (data?.hasExtra("finished") == true && data.getBooleanExtra("finished", false)) {
                setResult(RESULT_OK, Intent().putExtra("showScore", true))
                finish()
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private inner class BebanAdapter(val data: List<AkmExamsTable>) :
        RecyclerView.Adapter<BebanVh>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BebanVh = BebanVh(parent)

        override fun onBindViewHolder(holder: BebanVh, position: Int) = holder.bind(data[position])

        override fun getItemCount(): Int = data.size
    }

    private inner class BebanVh(
        parent: ViewGroup,
        val binding: AkmDetailBebanSoalItemBinding = AkmDetailBebanSoalItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AkmExamsTable) {
            binding.item = item
            binding.executePendingBindings()
        }
    }
}