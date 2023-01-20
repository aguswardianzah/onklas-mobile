package id.onklas.app.pages.ujian

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.R
import id.onklas.app.databinding.*
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.services.ExamStopService
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.SnapOnScrollListener
import id.onklas.app.utils.hideKeyboard
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat

class TakeUjianPage : Privatepage() {

    private val binding by lazy { TakeUjianPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<UjianViewModel> { component.ujianVmFactory }
    private val id: String by lazy { intent.getStringExtra("id").orEmpty() }
    private val scored: Boolean by lazy { intent.getBooleanExtra("scored", false) }
    private val listSoal by lazy { ArrayList<QuestionAnswered>() }

    private val viewpool by lazy { RecyclerView.RecycledViewPool() }
    private val snapHelper by lazy { PagerSnapHelper() }
    private val choiceItemDecor by lazy { LinearSpaceDecoration() }
    private var currentPage = 1

    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            if (scored) {
                setDisplayShowHomeEnabled(true)
                setDisplayHomeAsUpEnabled(true)

                binding.toolbar.setNavigationOnClickListener { finish() }
            }
        }

        binding.scored = scored
        binding.ujianTitle = intent.getStringExtra("name")

        binding.rvExam.apply {
            val lm =
                object : LinearLayoutManager(this@TakeUjianPage, RecyclerView.HORIZONTAL, false) {
//                    override fun canScrollHorizontally(): Boolean = false
                }
            layoutManager = lm
            adapter = this@TakeUjianPage.adapter
            try {
                snapHelper.attachToRecyclerView(this)
                addOnScrollListener(
                    SnapOnScrollListener(
                        snapHelper,
                        SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL_STATE_IDLE
                    ) { page ->
                        try {
                            with(listSoal[page]) {
                                binding.answered = question.answered
                                binding.isCorrect = question.is_correct
                            }
                            currentPage = page + 1
                            binding.currentPage = page + 1
                            hideKeyboard()
                        } catch (e: Exception) {
                            Timber.e(e)
                        }
                    })
            } catch (e: Exception) {
            }
        }

        binding.btnPrev.setOnClickListener {
            try {
                binding.rvExam.scrollToPosition(currentPage - 2)
            } catch (e: Exception) {
            }
        }

        binding.btnNext.setOnClickListener {
            if (currentPage < listSoal.size) {
                binding.rvExam.scrollToPosition(currentPage)
            }
        }

        binding.pageLabel.setOnClickListener {
            if (!selectQuestionDialog.isShowing)
                selectQuestionDialog.show()
        }

        viewmodel.errorString.observe(this, Observer(this::toast))
        viewmodel.getSoal(id, scored).observe(this, {
            listSoal.clear()
            listSoal.addAll(it)
            listSoal.forEach {
                Timber.e("Soal: ${it.question.id} ${it.question.question}")
                Timber.e("Jawaban: ${it.myAnswer?.answerId}")
                it.answers.forEach {
                    Timber.e("Pilihan: ${it.id} ${it.answer}")
                }
                Timber.e("---------------------------------------")
            }
            binding.totalPage = listSoal.size
            adapter.submitList(it)
            selectQuestionAdapter.submitList(it)
        })
        binding.currentPage = 1

        binding.btnFinish.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.DialogTheme)
                .setTitle("Akhiri Ujian")
                .setMessage("${if (listSoal.any { it.myAnswer == null }) "Perhatian, masih ada soal yang belum terjawab. " else ""} Anda yakin telah mengerjakan semua soal dan akan mengumpulkan jawaban sekarang?")
                .setPositiveButton("Ya") { dialog, _ ->
                    dialog.dismiss()
                    lifecycleScope.launch { endExam() }
                }
                .setNeutralButton("Batal") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        binding.executePendingBindings()

        lifecycleScope.launch {
            Timber.e("set timer to end ujian")
            try {
                if (!scored) {
                    viewmodel.memoryDB.ujian().get(id.toInt())?.let {
                        Timber.e("set timer to end ujian at ${it.date} ${it.endAt}")

                        val ellapsedTime =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("${it.date} ${it.endAt}").time - System.currentTimeMillis()
                        Timber.e("detik tersisa ${ellapsedTime / 1000}")

                        if (ellapsedTime > 0) {
                            object : CountDownTimer(ellapsedTime, 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    binding.timeLeft =
                                        formatTime((millisUntilFinished / 1000).toInt())
                                    binding.executePendingBindings()
                                }

                                override fun onFinish() {
                                    lifecycleScope.launch {
                                        endExam(false)
                                        AlertDialog.Builder(this@TakeUjianPage, R.style.DialogTheme)
                                            .setTitle("Ujian berakhir")
                                            .setMessage("Batas waktu ujian telah selesai, jawaban akan dikumpulkan sesuai dengan yang sudah dikerjakan")
                                            .setPositiveButton("OK") { dialog, _ ->
                                                lifecycleScope.launch {
                                                    dialog.dismiss()
                                                    setResult(RESULT_OK)
                                                    finish()
                                                }
                                            }
                                            .setCancelable(false)
                                            .show()
                                    }
                                }

                                private fun formatTime(secUntilFinish: Int): String {
                                    var minute = secUntilFinish / 60
                                    var hour = 0
                                    if (minute > 59) {
                                        hour = minute / 60
                                        minute %= 60
                                    }
                                    val sec = secUntilFinish % 60
                                    return "${if (hour > 0) "$hour:" else ""}${if (minute > 9) minute else "0$minute"}:${if (sec > 9) sec else "0$sec"}"
                                }
                            }.start().also { timer = it }
                        }
                    } ?: Timber.e("timer, ujian tidak ditemukan")
                }
            } catch (e: Exception) {
                Timber.e("Failed to set timer service")
                Timber.e(e)
            }
        }
    }

    private val selectQuestionDialog by lazy {
        AlertDialog.Builder(this)
            .setView(
                SelectQuestionDialogBinding.inflate(layoutInflater).apply {
                    rvQuestionNumbers.adapter = selectQuestionAdapter
                }.root
            )
            .create()
    }

    private val selectQuestionAdapter by lazy {
        object : ListAdapter<QuestionAnswered, QuestionNumberViewholder>(questionDiffUtil) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): QuestionNumberViewholder = QuestionNumberViewholder(parent)

            override fun onBindViewHolder(holder: QuestionNumberViewholder, position: Int) =
                holder.bind(getItem(position).question)
        }
    }

    inner class QuestionNumberViewholder(
        parent: ViewGroup,
        val binding: QuestionSelectItemBinding = QuestionSelectItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: QuestionTable) {
            binding.text = "${adapterPosition + 1}"
            binding.item = item
            binding.root.setOnClickListener { onClick() }
            binding.textview.setOnClickListener { onClick() }
            binding.image.setOnClickListener { onClick() }
            binding.executePendingBindings()
        }

        fun onClick() {
            selectQuestionDialog.dismiss()
            this@TakeUjianPage.binding.rvExam.scrollToPosition(adapterPosition)
        }
    }

    override fun onBackPressed() {
        if (scored) super.onBackPressed()
    }

    private suspend fun endExam(closeAfter: Boolean = true) {
        val progress =
            ProgressDialog.show(this@TakeUjianPage, "", "mengumpulkan jawaban")
        val submitResult = viewmodel.answerExam(id, intent.getStringExtra("name").orEmpty())
        progress.dismiss()
        if (submitResult && closeAfter) {
            setResult(RESULT_OK)
            finish()
        }
    }

    private val questionDiffUtil by lazy {
        object : DiffUtil.ItemCallback<QuestionAnswered>() {
            override fun areItemsTheSame(
                oldItem: QuestionAnswered,
                newItem: QuestionAnswered
            ): Boolean = oldItem.question.id == newItem.question.id

            override fun areContentsTheSame(
                oldItem: QuestionAnswered,
                newItem: QuestionAnswered
            ): Boolean = oldItem == newItem

            override fun getChangePayload(
                oldItem: QuestionAnswered,
                newItem: QuestionAnswered
            ): Any? = newItem.myAnswer.also { Timber.e("Payload adapter: $it") }
        }
    }

    private val adapter by lazy {
        object : ListAdapter<QuestionAnswered, RecyclerView.ViewHolder>(questionDiffUtil) {
            override fun getItemViewType(position: Int): Int = position

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder =
                if (getItem(viewType).answers.isNotEmpty())
                    QuestionViewholder(parent, getItem(viewType).answers)
                else QuestionEssayViewHolder(parent)

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val item = getItem(position)
                val question = item.question
                if (holder is QuestionViewholder)
                    holder.bind(question)
                else if (holder is QuestionEssayViewHolder)
                    holder.bind(question, item.myAnswer)
            }

            override fun onBindViewHolder(
                holder: RecyclerView.ViewHolder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                if (payloads.isNotEmpty() && payloads.first() is MyAnswerTable && holder is QuestionViewholder)
                    holder.update(payloads.first() as MyAnswerTable)
                else onBindViewHolder(holder, position)
            }
        }
    }

    private inner class QuestionViewholder(
        parent: ViewGroup,
        val choices: List<AnswerTable>,
        val binding: QuestionItemBinding = QuestionItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {
        val adapter: RecyclerView.Adapter<ChoiceViewholder>
        val choiceLabels = ('A'..'Z').toList()

        init {
            adapter = object :
                RecyclerView.Adapter<ChoiceViewholder>() {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): ChoiceViewholder =
                    ChoiceViewholder(parent)

                override fun onBindViewHolder(holder: ChoiceViewholder, position: Int) {
                    holder.bind(
                        choices[position],
                        listSoal[adapterPosition].myAnswer ?: MyAnswerTable(),
                        choiceLabels[position].toString()
                    )
                }

                override fun onBindViewHolder(
                    holder: ChoiceViewholder,
                    position: Int,
                    payloads: MutableList<Any>
                ) {
                    if (payloads.isNotEmpty() && payloads.first() is MyAnswerTable)
                        holder.update(payloads.first() as MyAnswerTable)
                    else
                        onBindViewHolder(holder, position)
                }

                override fun getItemCount(): Int = choices.size
            }

            binding.rvChoice.addItemDecoration(choiceItemDecor)
            binding.rvChoice.setRecycledViewPool(viewpool)
            binding.rvChoice.isNestedScrollingEnabled = false
        }

        fun bind(item: QuestionTable) {
            binding.item = item
            binding.rvChoice.adapter = adapter
            binding.executePendingBindings()
        }

        fun update(myChoice: MyAnswerTable) {
            (0 until adapter.itemCount).forEach {
                adapter.notifyItemChanged(it, myChoice)
            }
        }
    }

    private inner class ChoiceViewholder(
        parent: ViewGroup,
        val binding: ChoiceItemBinding = ChoiceItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.image.clipToOutline = true
        }

        fun bind(choice: AnswerTable, myChoice: MyAnswerTable, choiceLabel: String) {
            binding.choice = choice
            binding.myChoice = myChoice
            binding.choiceLabel = choiceLabel
            binding.scored = scored
            binding.executePendingBindings()

            binding.root.setOnClickListener { answer(choice, myChoice) }
            binding.image.setOnClickListener { answer(choice, myChoice) }
            binding.label.setOnClickListener { answer(choice, myChoice) }
            binding.textAnswer.setOnClickListener { answer(choice, myChoice) }
        }

        fun answer(choice: AnswerTable, myChoice: MyAnswerTable) {
            Timber.e("new answer: $myChoice\nchoice: $choice")
            if (!scored && myChoice.answerId != choice.id) {
                lifecycleScope.launch {
                    viewmodel.persistDB.ujian().setAnswered(choice.qId)
                    viewmodel.persistDB.ujian().insertMyAnswer(
                        MyAnswerTable(
                            choice.qId,
                            choice.id,
                            this@TakeUjianPage.id.toInt()
                        )
                    )
                    this@TakeUjianPage.binding.answered = true
                }
            }
        }

        fun update(myChoice: MyAnswerTable) {
            binding.myChoice = myChoice
            binding.executePendingBindings()
        }
    }

    private inner class QuestionEssayViewHolder(
        parent: ViewGroup,
        val binding: QuestionEssayItemBinding = QuestionEssayItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        private val handler by lazy { Handler(Looper.getMainLooper()) }
        private var updateRunnable: Runnable? = null

        fun bind(item: QuestionTable, myAnswer: MyAnswerTable?) {
            binding.item = item
            binding.myAnswer = myAnswer
            binding.inputAnswer.isEnabled = !scored
            binding.executePendingBindings()

            if (!scored)
                binding.inputAnswer.doAfterTextChanged {
                    it?.toString()?.let {
                        if (it.isNotEmpty()) {
                            updateRunnable?.let { it1 -> handler.removeCallbacks(it1) }
                            handler.postDelayed(Runnable {
                                lifecycleScope.launch {
                                    viewmodel.persistDB.ujian().setAnswered(item.id)
                                    viewmodel.persistDB.ujian().insertMyAnswer(
                                        MyAnswerTable(
                                            item.id,
                                            0,
                                            this@TakeUjianPage.id.toInt(), it
                                        )
                                    )
                                    this@TakeUjianPage.binding.answered = true
                                }
                            }.also { updateRunnable = it }, 300)
                        }
                    }
                }
        }
    }

    private val pendingIntentStopExam by lazy {
        PendingIntent.getService(
            applicationContext,
            0,
            Intent(applicationContext, ExamStopService::class.java).putExtra("id", id.toInt()),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onPause() {
        super.onPause()

        if (!scored) {
            viewmodel.lastTime.timeInMillis = System.currentTimeMillis()
            (getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + viewmodel.pauseThresholds * 1000,
                pendingIntentStopExam
            )
        }
    }

    override fun onResume() {
        super.onResume()

        if (!scored) {
            val used = (System.currentTimeMillis() - viewmodel.lastTime.timeInMillis) / 1000
            Timber.e("pause used: $used")
            if (used > viewmodel.pauseThresholds) {
                lifecycleScope.launch {
                    if (viewmodel.persistDB.ujian().ujianEnded(id.toInt()) == true)
                        finish()
                    else
                        endExam()
                }
            } else {
                viewmodel.pauseThresholds = (viewmodel.pauseThresholds - used).toInt()
            }
            (getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.cancel(pendingIntentStopExam)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.cancel(pendingIntentStopExam)
        timer?.cancel()
    }
}