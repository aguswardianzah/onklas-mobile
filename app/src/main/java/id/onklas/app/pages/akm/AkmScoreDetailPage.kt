package id.onklas.app.pages.akm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.AkmDetailBebanSoalItemBinding
import id.onklas.app.databinding.AkmScoreDetailPageBinding
import id.onklas.app.databinding.DiagramScoreLabelBinding
import id.onklas.app.databinding.ScoreItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.pembayaran.RowAdapter
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

class AkmScoreDetailPage : Privatepage() {

    private val binding by lazy { AkmScoreDetailPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<AkmViewModel> { component.akmVmFactory }
    private val akmId by lazy { intent.getIntExtra("id", 0) }
    private val isSchoolScope by lazy { intent.getBooleanExtra("isSchoolScope", false) }
    private val isTryout by lazy { intent.getBooleanExtra("isTryout", false) }
    private val diagramColors by lazy {
        arrayOf(
            ResourcesCompat.getColor(resources, R.color.magenta, null),
            ResourcesCompat.getColor(resources, R.color.colorPrimary, null),
            ResourcesCompat.getColor(resources, R.color.gold, null)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.title =
                if (isTryout) "Hasil Quiz" else if (isSchoolScope) "Hasil Ujian" else "Hasil AKM"
            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.rvScore.addItemDecoration(
            LinearSpaceDecoration(
                OrientationHelper.HORIZONTAL,
                resources.getDimensionPixelOffset(R.dimen._16sdp)
            )
        )

        binding.rvInfo.addItemDecoration(DividerItemDecoration(this, OrientationHelper.VERTICAL))
        binding.rvTypes.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._4sdp)
            )
        )

        binding.bebanLabel.text = if (isSchoolScope) "Jumlah soal: " else "Beban soal:"

        try {
            lifecycleScope.launchWhenCreated {
                viewmodel.detailAkm(akmId).collectLatest { data ->
                    loading(msg = "menampilkan data")
                    Timber.e("detail data: $data")
                    binding.akmName.text = data.schedule.name
                    binding.akmType.text = data.schedule.type

                    if (data.exams.isNotEmpty()) {
                        Timber.e("list exam: ${data.exams.map { "${it.type} -- ${it.num_question} -- ${it.score}" }}")
                        if (!data.schedule.exam_template.equals("AKM", true)) {
                            binding.scoreUjian.text = data.exams.first().score.toString()
                            binding.diagramLayout.visibility = View.GONE
                        } else {
                            binding.scoreUjian.visibility = View.GONE
                            val scores = FloatArray(data.exams.size)
                            var maxScore = 0
                            data.exams.forEachIndexed { index, examsTable ->
                                addDiagramLabel(
                                    examsTable.type.replace("Literasi Teks", "LT", true),
                                    diagramColors[index % diagramColors.size]
                                )
                                scores[index] = ((examsTable.score + 20).toFloat())
                                if (examsTable.score > maxScore)
                                    maxScore = examsTable.score
                            }
                            binding.diagram.setScores((maxScore + 20).toFloat(), scores)
                            binding.rvScore.adapter = ScoreAdapter(data.exams.map { it.score })
                        }

                        binding.rvTypes.adapter =
                            BebanAdapter(data.exams.filter { it.num_question > 0 })
                    } else {
                        binding.scoreUjian.visibility = View.GONE
                        binding.diagramLayout.visibility = View.GONE
                    }

                    binding.rvInfo.adapter = RowAdapter(
                        arrayListOf(
                            "Peserta" to viewmodel.student.name,
                            "NIS" to if (viewmodel.student.nis.isNotEmpty()) viewmodel.student.nis else viewmodel.student.nisn,
                            "Kelas" to viewmodel.student.student_class.class_room.name,
                            "Sekolah" to viewmodel.school.name,
                        ).apply {
                            if (!isSchoolScope)
                                add(3, "Level" to data.schedule.level.toString())
                        }
                    )

                    binding.downloadProgress.visibility = View.GONE
                    binding.progressLabel.visibility = View.GONE
                    binding.btnAction.visibility = View.GONE

//                    if (!data.schedule.exam_template.equals("AKM", true))
//                        viewmodel.api.reviewAkm(akmId)
//                    else
//                        viewmodel.api.reviewTryout(akmId)

                    dismissloading()
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            dismissloading()
            prettyAlert(
                showImage = true,
                isSuccess = false,
                titleText = "Terjadi kesalahan",
                msg = "Gagal menampilkan data",
                okClick = {
                    finish()
                },
                abortLabel = ""
            )
        }
    }

    private fun addDiagramLabel(label: String, color: Int) {
        binding.layoutMainActivityCircular1.addView(
            DiagramScoreLabelBinding.inflate(layoutInflater).root.apply {
                text = if (label.isNotEmpty()) label else "Nilai"
                setTextColor(color)
            }
        )
    }

    private val scoreBackgrounds by lazy {
        arrayOf(
            R.drawable.ring_magenta,
            R.drawable.ring_primary_bold,
            R.drawable.ring_gold
        )
    }

    private inner class ScoreAdapter(val data: List<Int>) :
        RecyclerView.Adapter<ScoreVH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreVH = ScoreVH(parent)

        override fun onBindViewHolder(holder: ScoreVH, position: Int) {
            holder.binding.textView.apply {
                text = data[position].toString()
                setTextColor(diagramColors[position % diagramColors.size])
                setBackgroundResource(scoreBackgrounds[position])
            }
        }

        override fun getItemCount(): Int = data.size
    }

    private inner class ScoreVH(
        parent: ViewGroup,
        val binding: ScoreItemBinding = ScoreItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

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