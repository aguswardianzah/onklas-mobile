package id.onklas.app.pages.homework

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.HomeworkPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import kotlinx.coroutines.launch

class HomeWorkPage : Privatepage() {

    private val binding by lazy { HomeworkPageBinding.inflate(layoutInflater) }
    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private val navController by lazy { findNavController(R.id.page_container) }
    private val buttons by lazy { arrayOf(binding.btnBelum, binding.btnSudah, binding.btnNilai) }

    private val viewmodel by viewModels<HomeWorkViewModel> { component.homeworkVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { supportFinishAfterTransition() }
        }

        toggleFilter(false)

        binding.btnBelum.setOnClickListener {
            navController.navigate(R.id.action_global_homeworkBelumPage)
//            toggleFilter(true)
            setButton(binding.btnBelum)
        }

        binding.btnSudah.setOnClickListener {
            navController.navigate(R.id.action_global_homeworkSudahPage)
//            toggleFilter(true)
            setButton(binding.btnSudah)
        }

        binding.btnNilai.setOnClickListener {
            navController.navigate(R.id.action_global_homeworkNilaiPage)
//            toggleFilter(false)
            setButton(binding.btnNilai)
        }

        binding.icFilter.setOnClickListener {
            HomeworkFilterPage().show(supportFragmentManager, "")
        }

        lifecycleScope.launch {
            viewmodel.fetchHomeworkTodo()
        }
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
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

    private fun toggleFilter(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        binding.bgFilter.visibility = visibility
        binding.icFilter.visibility = visibility
        binding.labelFilter.visibility = visibility
        binding.spFilter.visibility = visibility
    }
}