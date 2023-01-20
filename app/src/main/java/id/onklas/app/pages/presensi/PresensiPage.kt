package id.onklas.app.pages.presensi

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.PresensiPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class PresensiPage : Privatepage() {

    private val binding by lazy { PresensiPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<PresensiViewModel> { component.presensiVmFactory }
    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private val navController by lazy { findNavController(R.id.page_container) }
    private val buttons by lazy { arrayOf(binding.btnKelas, binding.btnAbsensi, binding.btnRekap) }
    private val isStudent by lazy { viewmodel.pref.getBoolean("is_student") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { supportFinishAfterTransition() }
        }

        viewmodel.stringError.observe(this, Observer(this::toast))

        binding.btnKelas.setOnClickListener {
            navController.navigate(R.id.action_global_presensiKelasPage)
            setButton(it)
        }

        binding.isStudent = isStudent
//        if (isStudent) {
//            binding.btnAbsensi.visibility = View.GONE
//            (binding.btnKelas.layoutParams as ConstraintLayout.LayoutParams).matchConstraintPercentWidth = 0.48f
//            (binding.btnRekap.layoutParams as ConstraintLayout.LayoutParams).matchConstraintPercentWidth = 0.48f
//        } else {
//            binding.btnAbsensi.visibility = View.VISIBLE
//            (binding.btnKelas.layoutParams as ConstraintLayout.LayoutParams).matchConstraintPercentWidth = 0.32f
//            (binding.btnRekap.layoutParams as ConstraintLayout.LayoutParams).matchConstraintPercentWidth = 0.32f
//        }

        binding.btnAbsensi.setOnClickListener {
            navController.navigate(R.id.action_global_presensiAbsenPage)
            setButton(it)
        }

        binding.btnRekap.setOnClickListener {
            navController.navigate(R.id.action_global_presensiRekapPage)
            setButton(it)
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

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }
}