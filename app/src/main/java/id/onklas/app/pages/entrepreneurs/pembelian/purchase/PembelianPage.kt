package id.onklas.app.pages.entrepreneurs.pembelian.purchase

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.PembelianPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM
import id.onklas.app.pages.entrepreneurs.PembelianVM
import kotlinx.coroutines.launch
import java.util.*

class PembelianPage : Privatepage() {

    private val binding by lazy { PembelianPageBinding.inflate(layoutInflater) }
    private val viewmodelMain by viewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val viewmodelPembelian by viewModels<PembelianVM> { component.entrepreneursPembelianFactory }
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
            R.color.Black3,
            null
        )
    }
    private val colorWhite by lazy { Color.WHITE }

    private var position1 = 0

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_calender, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val calendar = Calendar.getInstance()
        DatePickerDialog.newInstance { view, year, monthOfYear, dayOfMonth ->
            view.dismiss()
            TimePickerDialog.newInstance({ view, hourOfDay, minute, second ->
                view.dismiss()
                calendar.set(year, monthOfYear, dayOfMonth, hourOfDay, minute, second)

            }, true)
        }
            .apply {
                accentColor = colorPrimary
                maxDate = calendar
                show(supportFragmentManager, "date_picker")
            }.setOnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                calendar[year, monthOfYear, dayOfMonth, 0] = 0
                val formattedDate: String = viewmodelMain.dateFormat.format(calendar.time)
                viewmodelPembelian.dateFilterLiveData.postValue(formattedDate)
            }

        return super.onOptionsItemSelected(item)
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

        binding.lifecycleOwner = this
        binding.viewModel = viewmodelPembelian

        binding.actionDalamProses.setOnClickListener {
            viewmodelPembelian.dateFilterLiveData.postValue("")
            navController.popBackStack()
            //default
            navController.navigate(R.id.action_global_pembelian_processedProduct)
            position1 = 0

            //top
            binding.actionDalamProses.apply {
                setTextColor(colorWhite)
                setBackgroundResource(R.drawable.fill_blue_radius6)
            }

            binding.actionSelesai.apply {
                setTextColor(colorGray)
                setBackgroundResource(R.drawable.border_gray_radius6)
            }

            //top2
            binding.btnJasa.apply { setTextColor(colorGray) }
            binding.jasaLine.apply { setBackgroundColor(colorGray) }

            binding.btnProduk.apply { setTextColor(colorPrimary) }
            binding.produkLine.apply { setBackgroundColor(colorPrimary) }
        }

        binding.actionSelesai.setOnClickListener {
            viewmodelPembelian.dateFilterLiveData.postValue("")
            navController.popBackStack()
            //defautl
            navController.navigate(R.id.action_global_pembelian_doneProduct)
            position1 = 1

            //top
            binding.actionSelesai.apply {
                setTextColor(colorWhite)
                setBackgroundResource(R.drawable.fill_blue_radius6)
            }

            binding.actionDalamProses.apply {
                setTextColor(colorGray)
                setBackgroundResource(R.drawable.border_gray_radius6)
            }

            //top2
            binding.btnJasa.apply { setTextColor(colorGray) }
            binding.jasaLine.apply { setBackgroundColor(colorGray) }

            binding.btnProduk.apply { setTextColor(colorPrimary) }
            binding.produkLine.apply { setBackgroundColor(colorPrimary) }
        }

        binding.btnJasa.setOnClickListener {
            navController.popBackStack()
            if (position1 == 0) {
                //proccessed
                navController.navigate(R.id.action_global_pembelian_processedService)
            } else {
                //done
                navController.navigate(R.id.action_global_pembelian_doneService)
            }

            binding.btnJasa.apply { setTextColor(colorPrimary) }
            binding.jasaLine.apply { setBackgroundColor(colorPrimary) }

            binding.btnProduk.apply { setTextColor(colorGray) }
            binding.produkLine.apply { setBackgroundColor(colorGray) }
            lifecycleScope.launch {
                val countDon = viewmodelPembelian.db.kwu().countOutcomingDone()
                viewmodelPembelian.countDone.postValue(countDon)
            }
        }

        binding.btnProduk.setOnClickListener {
            navController.popBackStack()
            if (position1 == 0) {
                //proccessed
                navController.navigate(R.id.action_global_pembelian_processedProduct)
            } else {
                //done
                navController.navigate(R.id.action_global_pembelian_processedService)
            }

            binding.btnJasa.apply { setTextColor(colorGray) }
            binding.jasaLine.apply { setBackgroundColor(colorGray) }

            binding.btnProduk.apply { setTextColor(colorPrimary) }
            binding.produkLine.apply { setBackgroundColor(colorPrimary) }

            lifecycleScope.launch {
                val countPro = viewmodelPembelian.db.kwu().countOutcomingProcessed()
                viewmodelPembelian.countProcessed.postValue(countPro)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 310 && resultCode == Activity.RESULT_OK) {
            refreshDataList("done")
            refreshDataList("processed")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun refreshDataList(page: String) {
        lifecycleScope.launch {
            viewmodelPembelian.fetchListSellerOrder(
                0,
                page,
                "",
                "buyer"
            )
        }
        viewmodelPembelian.listOrder(page, "buyer", "")
    }
}