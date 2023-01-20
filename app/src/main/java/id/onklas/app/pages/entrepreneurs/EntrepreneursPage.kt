package id.onklas.app.pages.entrepreneurs

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.EditMerchantDialogBinding
import id.onklas.app.databinding.EntrepreneursDialogBinding
import id.onklas.app.databinding.EntrepreneursPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.sekolah.store.CartPage
import kotlinx.coroutines.flow.collectLatest

class EntrepreneursPage : Privatepage() {

    private val binding by lazy { EntrepreneursPageBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodel by viewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val navController by lazy { findNavController(R.id.page_container) }

    private val colorPrimary by lazy {
        ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
    }
    private val colorGray by lazy {
        ResourcesCompat.getColor(resources, R.color.Black3, null)
    }
    private val colorWhite by lazy {
        ResourcesCompat.getColor(resources, R.color.white, null)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_cart,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        CartPage.open(this)
        return super.onOptionsItemSelected(item)
    }

    private var firstRun = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        lifecycleScope.launchWhenCreated {
            viewmodel.loadMerchantSummary("MyMerchant").collectLatest {
                binding.summary =it
            }
        }
        viewmodel.loadingShow.observe(this, Observer {
            if (firstRun) {
                if (it) {
                    loading()
                } else {
                    dismissloading()
                    firstRun = false
                }
            }

        })
        viewmodel.errorString.observe(this, Observer(this::toast))
        viewmodel.pref.putBoolean("dialogEntrepreneurs",true)
        if (viewmodel.pref.getBoolean("dialogEntrepreneurs")) {
            viewmodel.pref.putBoolean("dialogEntrepreneurs",false)
            val bindingDialog = EntrepreneursDialogBinding.inflate(layoutInflater)
            val dialog = AlertDialog.Builder(this)
                    .setView(bindingDialog.root)
                    .show()
                    .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }


            bindingDialog.btnAction.setOnClickListener {
                dialog.dismiss()
            }
            bindingDialog.btnLater.setOnClickListener {
                dialog.dismiss()
            }
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Kewirausahaan"
            binding.toolbar.title = "Kewirausahaan"
        }

        binding.btnPembelian.setOnClickListener {
            navController.popBackStack()
            navController.navigate(R.id.action_global_entrepreneurs_pembelian)

            binding.btnPembelian.apply { setTextColor(colorPrimary) }
            binding.pembelianLine.apply { setBackgroundColor(colorPrimary) }

            binding.btnPenjualan.apply { setTextColor(colorGray) }
            binding.penjualanLine.apply { setBackgroundColor(colorGray) }
        }

        binding.btnPenjualan.setOnClickListener {
            navController.popBackStack()
            navController.navigate(R.id.action_global_entrepreneurs_penjualan)

            binding.btnPembelian.apply { setTextColor(colorGray) }
            binding.pembelianLine.apply { setBackgroundColor(colorGray) }

            binding.btnPenjualan.apply { setTextColor(colorPrimary) }
            binding.penjualanLine.apply { setBackgroundColor(colorPrimary) }
        }

        binding.actionEdit.setOnClickListener {
            editMerchantDialog()
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        lifecycleScope.launchWhenCreated {
            viewmodel.loadMerchantUser().collectLatest {
                try {
                    binding.item = it
                } catch (e: Exception) {
                }
            }
        }

    }


    private fun editMerchantDialog() {
        val bindingDialog = EditMerchantDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
                .setView(bindingDialog.root)
                .show()
                .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }


        bindingDialog.inName.hint = binding.username.text.toString()
        bindingDialog.actionKonfirmasi.setOnClickListener {
            toast("test konfirmasi ")
        }
        bindingDialog.actionBatal.setOnClickListener {
            dialog.dismiss()
        }

    }
}