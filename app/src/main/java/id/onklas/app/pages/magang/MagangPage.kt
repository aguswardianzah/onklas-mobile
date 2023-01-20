package id.onklas.app.pages.magang

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.MagangPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BasePage
import id.onklas.app.pages.Privatepage
import kotlinx.coroutines.launch

class MagangPage : Privatepage() {

    private val binding by lazy { MagangPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MagangViewModel> {
        component.magangVmFactory
    }
    private val buttons by lazy { arrayOf(binding.btnSchedule, binding.btnReport) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private val navController by lazy { findNavController(R.id.page_container) }

    init {
        lifecycleScope.launchWhenCreated {
            viewModel.fetchSchedule()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { supportFinishAfterTransition() }
        }

        binding.btnSchedule.setOnClickListener {
            navController.navigate(R.id.action_global_magangSchedulePage)
            setButton(binding.btnSchedule)
        }

        binding.btnReport.setOnClickListener {
            navController.navigate(R.id.action_global_magangReportPage)
            setButton(binding.btnReport)
        }

        viewModel.stringError.observe(this, this::toast)
    }

    private fun setButton(btn: View) {
        buttons.forEach {
            if (btn == it) {
                it.setStrokeColorResource(R.color.colorPrimary)
                it.setTextColor(colorPrimary)
            } else {
                it.setStrokeColorResource(R.color.gray)
                it.setTextColor(colorGray)
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }
}