package id.onklas.app.pages.akm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Html
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.AkmInstructionPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.RomanNumber
import kotlinx.coroutines.flow.collectLatest

class AkmInstructionPage : Privatepage() {

    private val binding by lazy { AkmInstructionPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<AkmViewModel> { component.akmVmFactory }
    private val akmId by lazy { intent.getIntExtra("id", 0) }
    private val actionBack by lazy { intent.getBooleanExtra("action_back", false) }
    private val number by lazy { intent.getIntExtra("number", 0) }
    private val instruction by lazy { intent.getStringExtra("instruction").orEmpty() }
    private val description by lazy { intent.getStringExtra("description").orEmpty() }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.title.text = "${RomanNumber.toRoman(number)}. PERINTAH SOAL"
        binding.instruction.text = Html.fromHtml(instruction)
        binding.desc.text = Html.fromHtml(description)

        binding.btnAction.setOnClickListener {
            if (!actionBack)
                startActivity(Intent(this, AkmQuestionsPage::class.java).putExtras(intent))
            finish()
        }

        lifecycleScope.launchWhenCreated {
            viewmodel.detailAkm(akmId).collectLatest { data ->
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
}