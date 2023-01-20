package id.onklas.app.pages.theoryteacher

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.TheoryteacherPageBinding
import id.onklas.app.pages.Privatepage

class TheoryTeacherPage : Privatepage() {

    private val binding by lazy { TheoryteacherPageBinding.inflate(layoutInflater) }
    private val navController by lazy { findNavController(R.id.page_container) }
    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.btnMateri.setOnClickListener {
            navController.navigate(R.id.action_global_materiTeacherpage)
            binding.btnMateri.setStrokeColorResource(R.color.colorPrimary)
            binding.btnMateri.setTextColor(colorPrimary)
            binding.btnMapel.setStrokeColorResource(R.color.gray)
            binding.btnMapel.setTextColor(colorGray)
        }
        binding.btnMapel.setOnClickListener {
            navController.navigate(R.id.action_global_mapelTeacherPage)
            binding.btnMateri.setStrokeColorResource(R.color.gray)
            binding.btnMateri.setTextColor(colorGray)
            binding.btnMapel.setStrokeColorResource(R.color.colorPrimary)
            binding.btnMapel.setTextColor(colorPrimary)
        }

        binding.btnAdd.setOnClickListener {
            startActivity(
                Intent(this, UploadMateriPage::class.java)
            )
        }
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }
}