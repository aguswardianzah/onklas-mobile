package id.onklas.app.pages.akm

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import androidx.room.withTransaction
import androidx.work.WorkInfo
import androidx.work.WorkManager
import id.onklas.app.BuildConfig
import id.onklas.app.R
import id.onklas.app.databinding.*
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.comment.ImageViewPage
import id.onklas.app.utils.SnapOnScrollListener
import id.onklas.app.utils.hideKeyboard
import id.onklas.app.utils.showKeyboard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*

class AkmQuestionsPage : Privatepage() {

    private val binding by lazy { AkmQuestionsPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<AkmViewModel> { component.akmVmFactory }
    private val examTitle by lazy { intent.getStringExtra("title") ?: "Kerjakan Ujian" }
    private val akmId by lazy { intent.getIntExtra("id", 0) }
    private val instructionId by lazy { intent.getIntExtra("instruction_id", 0) }

    private val snapHelper by lazy { PagerSnapHelper() }
    private var questionScrollHandler: RecyclerView.OnScrollListener? = null

    private var allowFinish = false

    init {
        lifecycleScope.launchWhenCreated {
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

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.BUILD_TYPE != "debug")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

//            binding.toolbar.title = examTitle
            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.cardInstruction.setOnClickListener {
            startActivity(
                Intent(this, AkmInstructionPage::class.java)
                    .putExtras(intent)
                    .putExtra("action_back", true)
            )
        }

        binding.rvQuestions.apply {
            adapter = questionAdapter
            snapHelper.attachToRecyclerView(this)
            setHasFixedSize(true)
        }

        binding.btnNext.setOnClickListener {
            try {
                var currentPos =
                    (binding.rvQuestions.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                if (currentPos < 0)
                    currentPos = 0
                binding.rvQuestions.smoothScrollToPosition(currentPos + 1)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        lifecycleScope.launchWhenCreated {
            loading(msg = "menampilkan soal")
            viewmodel.instQuestionAnswer(instructionId).collectLatest {
                dismissloading()

                allowFinish = it.qa.all { it.question.answered }
                invalidateOptionsMenu()

                binding.pageLabel.setOnClickListener { v ->
                    QuestionSelectDialog(
                        this@AkmQuestionsPage,
                        it.qa.map { it.question }) {
                        binding.rvQuestions.smoothScrollToPosition(it)
                    }.show()
                }

                binding.btnNext.visibility = if (it.qa.size == 1) View.GONE else View.VISIBLE

                var currentPos =
                    (binding.rvQuestions.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                binding.pageLabel.text = "${currentPos + 1} / ${it.instruction.num_question}"
                if (currentPos < 0)
                    currentPos = 0

                setTag(it.qa[currentPos].question.answered)

                questionScrollHandler?.let { it1 -> binding.rvQuestions.removeOnScrollListener(it1) }
                SnapOnScrollListener(snapHelper) { pos ->
                    binding.pageLabel.post {
                        binding.pageLabel.text = "${pos + 1} / ${it.instruction.num_question}"
                    }
                    setTag(it.qa[pos].question.answered)
//                    if (it.qa[pos].question.type == ANSWER_ESSAY)
//                        showKeyboard()
//                    else
//                        hideKeyboard()

                    binding.btnNext.visibility =
                        if (pos < it.qa.size - 1) View.VISIBLE else View.GONE
                }.also { scrollListener ->
                    questionScrollHandler = scrollListener
                    binding.rvQuestions.addOnScrollListener(scrollListener)
                }

                questionAdapter.submitList(it.qa)
//                Timber.e(it.toString())
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTag(answered: Boolean) {
        binding.tag.post {
            binding.tag.apply {
                if (answered) {
                    text = "Soal terjawab"
                    background =
                        ResourcesCompat.getDrawable(resources, R.drawable.tag_primary, null)
                } else {
                    text = "Belum terjawab"
                    background =
                        ResourcesCompat.getDrawable(resources, R.drawable.tag_gray, null)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_ujian, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menu_page_ujian)?.isVisible = allowFinish
        menu?.findItem(R.id.menu_page_ujian)?.isEnabled = allowFinish
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    private val questionAdapter by lazy {
        QuestionAdapter(this::answerEssay, this::answerChoice, this::answerPair) {
            startActivity(
                Intent(this, ImageViewPage::class.java)
                    .putExtra("title", "Gambar Ujian")
                    .putExtra("downloadable", false)
                    .setData(Uri.fromFile(File(it)))
            )
        }
    }

    private fun answerEssay(answer: String, item: QuestionAnswers) {
        lifecycleScope.launch {
            viewmodel.db.akm()
                .insertQuestion(
                    item.question.copy(
                        answer_essay = answer,
//                        answered = answer.isNotEmpty()
                        answered = true
                    )
                )
            viewmodel.db.akm().setInstAnswered(instructionId)
        }
    }

    private fun answerChoice(item: AkmAnswerTable) {
        lifecycleScope.launch {
            viewmodel.db.withTransaction {
                viewmodel.db.akm().setAnswered(item.question_id)
                viewmodel.db.akm().setInstAnswered(instructionId)

                val multipleCorrects = arrayOf(
                    AkmAnswerType.ANSWER_MULTIPLE_CHOICE_MULTIPLE_CORRECT,
                    AkmAnswerType.ANSWER_MULTIPLE_CHOICE_MULTIPLE_CORRECT_TABLE,
                    AkmAnswerType.ANSWER_MULTIPLE_CHOICE_TRUE_FALSE
                )
                if (!multipleCorrects.contains(
                        viewmodel.db.akm().getQuestionType(item.question_id)
                    )
                )
                    viewmodel.db.akm().unselectAnswers(item.question_id)

                viewmodel.db.akm().insertAnswer(item)
            }
        }
    }

    private fun answerPair(answers: List<AkmAnswerTable>) {
        lifecycleScope.launch {
            viewmodel.db.withTransaction {
                viewmodel.db.akm().setAnswered(answers.first().question_id)
                viewmodel.db.akm().setInstAnswered(instructionId)

                viewmodel.db.akm().insertAnswers(answers)
            }
        }
    }
}