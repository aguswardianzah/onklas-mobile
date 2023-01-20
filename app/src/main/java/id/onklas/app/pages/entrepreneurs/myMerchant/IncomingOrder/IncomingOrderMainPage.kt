package id.onklas.app.pages.entrepreneurs.myMerchant.IncomingOrder

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.EntrepreneursCostomDialogBinding
import id.onklas.app.databinding.IncomingOrderMainPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM
import id.onklas.app.pages.entrepreneurs.OrderVM
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class IncomingOrderMainPage : Privatepage() {

    private val binding by lazy { IncomingOrderMainPageBinding.inflate(layoutInflater) }
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
    private val bordergGray by lazy {
        ResourcesCompat.getDrawable(
            resources, R.drawable.border_gray_radius6, null
        )
    }
    private var position1 = 0
    private var position2 = 0


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
                val formattedDate: String = viewmodel.dateFormat.format(calendar.time)
                Timber.e("date filter----- $formattedDate")
                viewmodelOrder.dateFilterLiveData.postValue("$formattedDate")
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
            title = "Orderan Masuk"
            binding.toolbar.title = "Orderan Masuk "
        }

        binding.lifecycleOwner = this
        binding.viewModel = viewmodelOrder

        viewmodelOrder.countProcess.observe(this, Observer {
            Timber.e("count proses $it")
        })

        viewmodelOrder.errorString.observe(this, Observer(this::toast))

        binding.actionIncomingOrder.setOnClickListener {
            viewmodelOrder.dateFilterLiveData.postValue("")
            navController.popBackStack()
            //default
            navController.navigate(R.id.action_global_incoming_product)
            position1 = 0

            //top
            binding.actionIncomingOrder.apply {
                setTextColor(colorWhite)
                setBackgroundResource(R.drawable.fill_blue_radius6)
            }

            binding.actionProcessed.apply {
                setTextColor(colorGray)
                setBackgroundResource(R.drawable.border_gray_radius6)
            }

            //top2
            binding.btnJasa.apply { setTextColor(colorGray) }
            binding.jasaLine.apply { setBackgroundColor(colorGray) }

            binding.btnProduk.apply { setTextColor(colorPrimary) }
            binding.produkLine.apply { setBackgroundColor(colorPrimary) }

        }

        binding.actionProcessed.setOnClickListener {
            viewmodelOrder.dateFilterLiveData.postValue("")
            navController.popBackStack()
            //defautl
            navController.navigate(R.id.action_global_processed_product)
            position1 = 1

            //top
            binding.actionProcessed.apply {
                setTextColor(colorWhite)
                setBackgroundResource(R.drawable.fill_blue_radius6)
            }

            binding.actionIncomingOrder.apply {
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
                //incoming
                navController.navigate(R.id.action_global_incoming_service)
            } else {
                //processed
                navController.navigate(R.id.action_global_processed_service)
            }


            binding.btnJasa.apply { setTextColor(colorPrimary) }
            binding.jasaLine.apply { setBackgroundColor(colorPrimary) }

            binding.btnProduk.apply { setTextColor(colorGray) }
            binding.produkLine.apply { setBackgroundColor(colorGray) }
        }

        binding.btnProduk.setOnClickListener {
            navController.popBackStack()
            if (position1 == 0) {
                //incoming
                navController.navigate(R.id.action_global_incoming_product)
            } else {
                //processed
                navController.navigate(R.id.action_global_processed_product)
            }

            binding.btnJasa.apply { setTextColor(colorGray) }
            binding.jasaLine.apply { setBackgroundColor(colorGray) }

            binding.btnProduk.apply { setTextColor(colorPrimary) }
            binding.produkLine.apply { setBackgroundColor(colorPrimary) }
        }

        lifecycleScope.launchWhenCreated {
            try {
                val countInc = viewmodelOrder.db.kwu().countIncomingIn()
                viewmodelOrder.countIncoming.postValue(countInc)

                val countPro = viewmodelOrder.db.kwu().countIncomingProcessed()
                viewmodelOrder.countProcess.postValue(countPro)
            } catch (E: Exception) {
            }
        }



        viewmodelOrder.incActResponse.removeObservers(this)
        viewmodelOrder.incActResponse.observe(this, Observer {
            Timber.e("in action status ${it.data.status}")
            if (it.data.status == "APPROVED") {// diterima penjual
                button1Dialog(
                    "approve",
                    "Orderan Sudah Diterima",
                    "Orderan dari ${viewmodelOrder.buyerName.value} sudah berhasil diterima",
                    "Oke, Terimakasih",
                )
            } else if (it.data.status == "DECLINED") { //  ditolak penjual
                button1Dialog(
                    "decline",
                    "Orderan Sudah Ditolak",
                    "Orderan dari ${viewmodelOrder.buyerName.value} sudah berhasil ditolak",
                    "Oke, Terimakasih",
                )
            }
        })

        viewmodelOrder.inpResiResponse.removeObservers(this)
        viewmodelOrder.inpResiResponse.observe(this, Observer {
            Timber.e("inp resi response  $it")
            if (it.error.isEmpty()) {
                button1Dialog(
                    "success",
                    "Konfirmasi Pengiriman",
                    "Kami akan memberitahukan kepada pembeli bahwa sedang dalam pengiriman",
                    "Oke, Terimakasih",
                )
            }
        })


        viewmodelOrder.moveToProcessed.observe(this, Observer {
            viewmodelOrder.dateFilterLiveData.postValue("")
            navController.popBackStack()
            //defautl
            navController.navigate(R.id.action_global_processed_product)
            position1 = 1

            //top
            binding.actionProcessed.apply {
                setTextColor(colorWhite)
                setBackgroundResource(R.drawable.fill_blue_radius6)
            }

            binding.actionIncomingOrder.apply {
                setTextColor(colorGray)
                setBackgroundResource(R.drawable.border_gray_radius6)
            }

            //top2
            binding.btnJasa.apply { setTextColor(colorGray) }
            binding.jasaLine.apply { setBackgroundColor(colorGray) }

            binding.btnProduk.apply { setTextColor(colorPrimary) }
            binding.produkLine.apply { setBackgroundColor(colorPrimary) }

            viewmodelOrder.moveToProcessed.removeObserver { this }
        })

    }

    private fun button1Dialog(
        status: String,
        title: String,
        desc: String,
        btnDone: String,
    ) {
        val bindingDialog = EntrepreneursCostomDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(bindingDialog.root)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

        if (status == "decline") {
            bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_danger) }
        } else {
            bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_pay_success) }
        }


        bindingDialog.txtTitle.text = title
        bindingDialog.txtDesc.text = desc

        refreshDataList("incoming")
        refreshDataList("processed")

        bindingDialog.button1.root.visibility = View.VISIBLE
        bindingDialog.button1.btnDone.text = btnDone
        bindingDialog.button1.btnDone.setOnClickListener {
            viewmodelOrder.incActResponseBoolean.postValue(false)
            if (status == "approve") {
                viewmodelOrder.moveToProcessed.postValue(true)
            }
            dialog.dismiss()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode === 310) {
            if (resultCode === Activity.RESULT_OK) {
                refreshDataList("incoming")
                refreshDataList("processed")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun refreshDataList(page:String){
        lifecycleScope.launch { viewmodelOrder.fetchListSellerOrder(0, page, "seller", "") }
        viewmodelOrder.listOrder(page, "seller", "")
    }


}