package id.onklas.app.pages.homework.teacher

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.HomeworkTeacherPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.homework.HomeWorkViewModel
import id.onklas.app.utils.NoFilterArrayAdapter

class HomeworkTeacherPage : Privatepage() {

    private val binding by lazy { HomeworkTeacherPageBinding.inflate(layoutInflater) }
    private val homeworkViewmodel by viewModels<HomeWorkViewModel> { component.homeworkVmFactory }
    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private val navController by lazy { findNavController(R.id.page_container) }
    private val buttons by lazy { arrayOf(binding.btnBelum, binding.btnSudah) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { supportFinishAfterTransition() }
        }

        binding.fabCreate.setOnClickListener {
            addResult.launch(Intent(this, CreateHomeworkPage::class.java))
        }

        binding.viewmodel = homeworkViewmodel
        binding.lifecycleOwner = this

        homeworkViewmodel.apply {
            initMapelAndClass()

            listMapel.observe(this@HomeworkTeacherPage, {
                binding.inputMapel.setAdapter(
                    NoFilterArrayAdapter(
                        this@HomeworkTeacherPage,
                        it.map { it.name })
                )
                binding.inputMapel.setText(it.firstOrNull()?.name, false)
            })

            listClass.observe(this@HomeworkTeacherPage, {
                binding.inputClass.setAdapter(
                    NoFilterArrayAdapter(
                        this@HomeworkTeacherPage,
                        it.map { it.name })
                )
                binding.inputClass.setText(it.firstOrNull()?.name, false)
            })

            errorString.observe(this@HomeworkTeacherPage, Observer(this@HomeworkTeacherPage::toast))
        }

        binding.btnBelum.setOnClickListener {
            navController.navigate(R.id.action_global_homeworkTeacherListPage)
            setButton(binding.btnBelum)
            toggleFilter(true)
        }

        binding.btnSudah.setOnClickListener {
            navController.navigate(R.id.action_global_homeworkTeacherNilaiPage)
            setButton(binding.btnSudah)
            toggleFilter(false)
        }
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
        binding.classLabel.visibility = visibility
        binding.inputClassLayout.visibility = visibility
        binding.mapelLabel.visibility = visibility
        binding.inputMapelLayout.visibility = visibility
    }

    override fun onBackPressed() {
        finish()
    }

    private val addResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK)
                lifecycleScope.launchWhenCreated {
                    homeworkViewmodel.fetchHomeworkTodo()
                }
        }
}