package id.onklas.app.pages.resetpass

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import id.onklas.app.databinding.ResetPassPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class ResetPassPage : Privatepage() {

    private val binding by lazy { ResetPassPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<ResetPassViewmodel> { component.resetPassVmFactory }

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
    }
}