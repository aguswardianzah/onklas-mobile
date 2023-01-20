package id.onklas.app.pages.prokes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import id.onklas.app.R
import id.onklas.app.databinding.ProkesPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BasePage
import id.onklas.app.pages.Privatepage
import kotlinx.coroutines.launch
import timber.log.Timber

class ProkesPage : BasePage() {
    private val binding by lazy { ProkesPageBinding.inflate(layoutInflater) }

    private val viewmodel by viewModels<ProkesViewmodel> { component.prokesVmFactory }
    private val prokesNav by lazy {
        Navigation.findNavController(
            this,
            R.id.page_container
        )
    }
    private val colorPrimary by lazy {
        ContextCompat.getColor(this, R.color.colorPrimary)
    }
    private val colorGray by lazy {
        ContextCompat.getColor(this, R.color.gray)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Protokol Kesehatan"
            binding.toolbar.title = "Protokol Kesehatan"
        }

        binding.menuFormulir.setOnClickListener {
//            sekolahNav.popBackStack()
            binding.menuInfo.setTextColor(colorGray)
            binding.menuInfo.setBackgroundResource(R.drawable.border_form_login_gray)

            prokesNav.navigate(R.id.action_global_prokes_form)
            binding.menuFormulir.setTextColor(colorPrimary)
            binding.menuFormulir.setBackgroundResource(R.drawable.border_form_login_primary)
        }

        binding.menuInfo.setOnClickListener {
//            sekolahNav.popBackStack()
            binding.menuFormulir.setTextColor(colorGray)
            binding.menuFormulir.setBackgroundResource(R.drawable.border_form_login_gray)

            prokesNav.navigate(R.id.prokesinfo)
            binding.menuInfo.setTextColor(colorPrimary)
            binding.menuInfo.setBackgroundResource(R.drawable.border_form_login_primary)

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 223 && resultCode == AppCompatActivity.RESULT_OK) {
            lifecycleScope.launch {
                viewmodel.cekStudentReport()
                viewmodel.loadCekVaksin()
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        finish()
    }
}