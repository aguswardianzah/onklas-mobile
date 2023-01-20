package id.onklas.app.pages.entrepreneurs.myMerchant.addProduct

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import id.onklas.app.databinding.ProductDescriptionPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM

class ProductDescriptionPage : Privatepage() {

    private val binding by lazy { ProductDescriptionPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val desc by lazy { intent.getStringExtra("desc").orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.inputDesc.setText(desc)

        binding.inputDesc.doAfterTextChanged { text ->
            binding.btnSave.isEnabled = !text.isNullOrBlank()
        }

        binding.btnSave.setOnClickListener {
            setResult(RESULT_OK, intent.putExtra("desc", binding.inputDesc.text.toString()))
            finish()
        }
    }
}