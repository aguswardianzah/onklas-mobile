package id.onklas.app.pages.entrepreneurs.myMerchant.mystore

import android.os.Bundle
import id.onklas.app.databinding.MystorePageBinding
import id.onklas.app.pages.Privatepage

class Mystore : Privatepage() {

    private val binding by lazy { MystorePageBinding.inflate(layoutInflater) }
//    private val glide by lazy { GlideApp.with(this) }
//    private val viewmodel by viewModels<EntrepreneursVM> { component.entrepreneursFactory }
//    private val navController by lazy { findNavController(R.id.page_container) }
//
//    private val colorPrimary by lazy {
//
//        ResourcesCompat.getColor(
//            resources,
//            R.color.colorPrimary,
//            null
//        )
//    }
//    private val colorGray by lazy {
//        ResourcesCompat.getColor(
//            resources,
//            R.color.gray,
//            null
//        )
//    }
//    private val colorWhite by lazy {
//        ResourcesCompat.getColor(
//            resources,
//            R.color.white,
//            null
//        )
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Toko ku"
            binding.toolbar.title = "Toko ku"
        }

//        binding.btnProduct.setOnClickListener {
//            navController.popBackStack()
//            navController.navigate(R.id.action_global_entrepreneurs_mystore_product)
//
//            binding.btnProduct.apply { setTextColor(colorPrimary) }
//            binding.productLine.apply { setBackgroundColor(colorPrimary) }
//
//            binding.btnServices.apply { setTextColor(colorGray) }
//            binding.servicesLine.apply { setBackgroundColor(colorGray) }
//        }

//        binding.btnProduct.setOnClickListener {
//            navController.popBackStack()
//            navController.navigate(R.id.action_global_entrepreneurs_mystore_product)
//
//            binding.btnProduct.apply { setTextColor(colorPrimary) }
//            binding.productLine.apply { setBackgroundColor(colorPrimary) }
//
//            binding.btnServices.apply { setTextColor(colorGray) }
//            binding.servicesLine.apply { setBackgroundColor(colorGray) }
//        }
//
//        binding.btnServices.setOnClickListener {
//            navController.popBackStack()
//            navController.navigate(R.id.action_global_entrepreneurs_mystore_services)
//
//            binding.btnProduct.apply { setTextColor(colorGray) }
//            binding.productLine.apply { setBackgroundColor(colorGray) }
//
//            binding.btnServices.apply { setTextColor(colorPrimary) }
//            binding.servicesLine.apply { setBackgroundColor(colorPrimary) }
//        }


    }
}