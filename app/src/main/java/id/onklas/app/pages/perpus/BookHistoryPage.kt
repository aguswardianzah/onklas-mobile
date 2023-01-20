package id.onklas.app.pages.perpus

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.BookHistoryPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class BookHistoryPage : Privatepage() {

    private val binding by lazy { BookHistoryPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<PerpusViewModel> { component.perpusVmFactory }
    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private val navController by lazy { findNavController(R.id.page_container) }
    private val buttons by lazy { arrayOf(binding.btnDesc, binding.btnDetail) }

    init {
        lifecycleScope.launchWhenCreated {
            viewmodel.getHistory()
            viewmodel.getListRent()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.viewmodel = viewmodel
        binding.lifecycleOwner = this

        binding.btnDetail.setOnClickListener {
            navController.navigate(R.id.action_global_listArchivedPage)
            setButton(binding.btnDetail)
        }

        binding.btnDesc.setOnClickListener {
            navController.navigate(R.id.action_global_listRentPage)
            setButton(binding.btnDesc)
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
        finish()
    }
}