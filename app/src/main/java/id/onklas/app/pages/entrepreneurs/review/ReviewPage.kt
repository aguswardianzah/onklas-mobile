package id.onklas.app.pages.entrepreneurs.review

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.MystorePageBinding
import id.onklas.app.databinding.ReviewPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM

class ReviewPage : Privatepage() {

    private val binding by lazy { ReviewPageBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodel by viewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val navController by lazy { findNavController(R.id.page_container) }
    private val colorPrimary by lazy {
        ResourcesCompat.getColor(
                resources,
                R.color.colorPrimary,
                null
        )
    }
    private val colorGray by lazy {
        ResourcesCompat.getColor(
                resources,
                R.color.gray,
                null
        )
    }
    private val colorWhite by lazy {
        ResourcesCompat.getColor(
                resources,
                R.color.white,
                null
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Menunggu Review"
            binding.toolbar.title = "Menunggu Review"
        }

        binding.btnProduct.setOnClickListener {
            navController.popBackStack()
            navController.navigate(R.id.action_global_entrepreneurs_review_product)

            binding.btnProduct.apply { setTextColor(colorPrimary) }
            binding.productLine.apply { setBackgroundColor(colorPrimary) }

            binding.btnServices.apply { setTextColor(colorGray) }
            binding.servicesLine.apply { setBackgroundColor(colorGray) }
        }

        binding.btnServices.setOnClickListener {
            navController.popBackStack()
            navController.navigate(R.id.action_global_entrepreneurs_review_service)

            binding.btnProduct.apply { setTextColor(colorGray) }
            binding.productLine.apply { setBackgroundColor(colorGray) }

            binding.btnServices.apply { setTextColor(colorPrimary) }
            binding.servicesLine.apply { setBackgroundColor(colorPrimary) }
        }


    }
}