package id.onklas.app.pages.partisipasi

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import id.onklas.app.R
import id.onklas.app.databinding.PartisipasiSuccessPageBinding
import id.onklas.app.pages.Privatepage

class PartisipasiSuccessPage : Privatepage() {

    private val binding by lazy { PartisipasiSuccessPageBinding.inflate(layoutInflater) }
    private val id by lazy { intent.getStringExtra("id").orEmpty() }
    private val info by lazy { intent.getStringExtra("product_info").orEmpty() }
    private val dateLabel by lazy { intent.getStringExtra("date_label").orEmpty() }
    private val amountLabel by lazy { intent.getStringExtra("amount_label").orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val view = window.decorView
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            view.systemUiVisibility = flags
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }

        setContentView(binding.root)

        binding.labelType.text = info
        binding.date.text = dateLabel
        binding.textWallet.text = amountLabel

        binding.btnOk.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        binding.btnDetail.setOnClickListener {
            startActivity(Intent(this, PartisipasiHistoryPage::class.java).putExtras(intent))
            finish()
        }
    }
}