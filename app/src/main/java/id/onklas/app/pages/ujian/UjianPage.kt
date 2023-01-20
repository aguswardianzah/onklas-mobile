package id.onklas.app.pages.ujian

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.UjianPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class UjianPage : Privatepage() {

    private val binding by lazy { UjianPageBinding.inflate(layoutInflater) }
    private val navController by lazy { findNavController(R.id.page_container) }
    private val buttons by lazy { arrayOf(binding.btnIkuti, binding.btnNilai) }
    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private val viewmodel by viewModels<UjianViewModel> { component.ujianVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.btnIkuti.setOnClickListener {
            setButton(it)
            navController.navigate(R.id.action_global_listUjianPage)
        }

        binding.btnNilai.setOnClickListener {
            setButton(it)
            navController.navigate(R.id.action_global_nilaiujianPage)
        }

        viewmodel.errorString.observe(this, Observer(this::toast))

        lifecycleScope.launchWhenCreated { viewmodel.sendUjian() }
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

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_calender, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        startActivity(Intent(this, SppHistoryPage::class.java))
//        return super.onOptionsItemSelected(item)
//    }
}