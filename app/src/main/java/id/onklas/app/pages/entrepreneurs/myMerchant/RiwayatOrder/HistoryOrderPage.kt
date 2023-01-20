package id.onklas.app.pages.entrepreneurs.myMerchant.RiwayatOrder

import android.app.Activity
import android.content.Intent
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
import id.onklas.app.databinding.HistoryOrderPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM
import id.onklas.app.pages.entrepreneurs.OrderVM
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*


class HistoryOrderPage : Privatepage() {

    private val binding by lazy { HistoryOrderPageBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodel by viewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val viewmodelOrder by viewModels<OrderVM> { component.entrepreneursOrderFactory }
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
    private val colorWhite by lazy {
        ResourcesCompat.getColor(
            resources,
            R.color.white,
            null
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_calender, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Timber.e("income page -----------")
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
                val formattedDate: String = viewmodel.dateFormat.format(calendar.time)
                viewmodelOrder.dateFilterLiveData.postValue("$formattedDate")
            }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            binding.toolbar.inflateMenu(R.menu.menu_calender)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Riwayat Orderan"
            binding.toolbar.title = "Riwayat Orderan"
        }
        binding.btnJasa.setOnClickListener {
            navController.popBackStack()
            navController.navigate(R.id.historyService)

            binding.btnJasa.apply { setTextColor(colorPrimary) }
            binding.jasaLine.apply { setBackgroundColor(colorPrimary) }

            binding.btnProduk.apply { setTextColor(colorGray) }
            binding.produkLine.apply { setBackgroundColor(colorGray) }
        }

        binding.btnProduk.setOnClickListener {
            navController.popBackStack()
            navController.navigate(R.id.historyProduct)

            binding.btnJasa.apply { setTextColor(colorGray) }
            binding.jasaLine.apply { setBackgroundColor(colorGray) }

            binding.btnProduk.apply { setTextColor(colorPrimary) }
            binding.produkLine.apply { setBackgroundColor(colorPrimary) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode === 101) {
            if (resultCode === Activity.RESULT_OK) {
                refreshDataList("")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun refreshDataList(page:String=""){
        viewmodelOrder.isEmptyOrder.postValue(false)
        lifecycleScope.launch { viewmodelOrder.fetchListSellerOrder(0, "completed", "seller", "") }
        viewmodelOrder.listOrder("completed", "seller", "")
    }


}