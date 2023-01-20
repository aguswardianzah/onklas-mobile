package id.onklas.app.pages.klaspay.aktivasi

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import id.onklas.app.R
import id.onklas.app.databinding.KlaspayAktivasiPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class KlaspayAktivasiPage : Privatepage() {

    private val binding by lazy { KlaspayAktivasiPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<KlaspayAktivasiViewmodel> { component.klaspayAktivasiVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val view = window.decorView
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            view.systemUiVisibility = flags
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }

        setContentView(binding.root)

        viewModel.pageStep.postValue(1)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

//        viewModel.pageStep.observe(this, {
//            Timber.d("pageStep: $it")
//            binding.toolbar.title = "Step $it / 3"
//        })

        viewModel.errorString.observe(this, {
            if (it.isNotEmpty()) toast(it)
        })

        binding.imageBack.setOnClickListener { onBackPressed() }
    }
}