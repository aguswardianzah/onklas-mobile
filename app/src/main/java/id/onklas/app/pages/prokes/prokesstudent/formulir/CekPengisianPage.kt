package id.onklas.app.pages.prokes.prokesstudent.formulir

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexboxLayout
import id.onklas.app.R
import id.onklas.app.databinding.CekPengisianPageBinding
import id.onklas.app.databinding.FormulirPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BasePage
import id.onklas.app.pages.prokes.*
import id.onklas.app.pages.prokes.prokesteacher.CekScreeningPage
import kotlinx.coroutines.launch
import timber.log.Timber

class CekPengisianPage : BasePage() {

    companion object {
        fun open(
            activity: Activity,
            onScreening: Boolean = false,
            WayOfTravelText: String = "",
            WayOfTravelKey: String = "",
            transportChoiceText: List<String> = emptyList(),
            transportChoiceKey: List<String> = emptyList()
        ) {
            var Transporttext = ArrayList<String>()
            var Transportkey = ArrayList<String>()
            if (transportChoiceText.isNotEmpty()) {
                Transporttext.addAll(transportChoiceText)
                Transportkey.addAll(transportChoiceKey)
            }
            activity.startActivityForResult(
                Intent(activity, CekPengisianPage::class.java)
                    .putExtra(
                        "onScreening",
                        onScreening
                    ) //  onscreening true atau lihat detail  false
                    .putStringArrayListExtra("transportKey", Transportkey)
                    .putStringArrayListExtra("transportText", Transporttext)
                    .putExtra("wayofTravelKey", WayOfTravelKey)
                    .putExtra("wayofTravelText", WayOfTravelText), 224
            )
        }
    }

    private val binding by lazy { CekPengisianPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<ProkesViewmodel> { component.prokesVmFactory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.onScreening = intent.getBooleanExtra("onScreening", false)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        lifecycleScope.launch {
            if (intent.getBooleanExtra("onScreening", false)) {

                val WayOftrafelText = intent.getStringExtra("wayofTravelText").toString()
                val WayOftrafelKey = intent.getStringExtra("wayofTravelKey").toString()
                val TransportText = intent.getStringArrayListExtra("transportText")
                val TransportKey = intent.getStringArrayListExtra("transportKey")

                val ListTransport = ArrayList<GlobalsValueInnerItem>()

                for (i in 0 until (TransportText?.size ?: 0)) {
                    ListTransport.add(
                        GlobalsValueInnerItem(
                            TransportKey!!.get(i).toString(),
                            TransportText!!.get(i).toString(),
                        )
                    )
                }
                viewmodel.cekStudentReport.postValue(
                    ResponseCheckReport(
                        data = CheckReportItem(
                            check = Check(
                                student = cekStudentItem(
                                    way_of_travel = GlobalsValueInnerItem(
                                        WayOftrafelKey,
                                        WayOftrafelText
                                    ),
                                    public_transportion_choice = ListTransport
                                ),
                                staff = null
                            ),
                        )
                    )
                )
                viewmodel.LoadingShow.postValue(false)
            } else {
                viewmodel.cekStudentReport()
            }
        }

        viewmodel.LoadingShow.observe(this, Observer {
            Timber.e("loading show $it")
            if (it) {
                loading()
            } else {
                dismissloading()
            }
        })

        viewmodel.errorString.observe(this, Observer {
            toast(it)
        })


        viewmodel.cekStudentReport.observe(this, Observer {
            binding.item = it
            it.data?.check?.student?.public_transportion_choice?.map { it.text }
                ?.let { it1 -> setTextViewInFlexbox(it1) }
        })

        binding.btnConfirm.setOnClickListener {
            alertSelectNew(
                "Proses Formulir",
                "Anda setuju untuk melanjutkan proses dan mengirimkan laporan formulir ini?\n",
                "Proses Sekarang",
                {
                    lifecycleScope.launch {
                        intent.getStringArrayListExtra("transportKey")?.let { itListChoice1 ->
                            viewmodel.sendProkesStudent(
                                intent.getStringExtra("wayofTravelKey").toString(),
                                itListChoice1
                            )
                        }
                    }
                },
                "Cek Ulang"
            )
        }

        viewmodel.sendProkseResponse.observe(this, Observer {
            prettyAlert(
                showImage = true, isSuccess = true,
                titleText = "Pengisian Formulir Berhasil",
                msg = "Formulir rekam jejak perjalananmu berhasil dikirim, isi kembali keesokan harinya",
                okLabel = "Ok",
                okClick = {
                    val returnIntent = Intent()
                    setResult(RESULT_OK, returnIntent)
                    finish()
                },
                abortLabel = ""
            )
        })

    }

    private fun setTextViewInFlexbox(feelIndication: List<String>) {
        binding.flexboxlayout.removeAllViews()
        for (i in feelIndication.indices) {
            val txtFeelInication = TextView(this)
            txtFeelInication.text = feelIndication[i]
            txtFeelInication.textSize = 12f
            txtFeelInication.gravity = Gravity.CENTER
            txtFeelInication.setPadding(
                resources.getDimension(R.dimen._10sdp).toInt(),
                resources.getDimension(R.dimen._8sdp).toInt(),
                resources.getDimension(R.dimen._10sdp).toInt(),
                resources.getDimension(R.dimen._8sdp).toInt()
            )
            txtFeelInication.background = ResourcesCompat.getDrawable(resources, R.drawable.border_white_radius6, null)
            txtFeelInication.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.e4e4)
            txtFeelInication.setTextColor(ResourcesCompat.getColor(resources, android.R.color.black, null))

            val lpRight = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            txtFeelInication.layoutParams = lpRight
            val lp = txtFeelInication.layoutParams as FlexboxLayout.LayoutParams
            lp.setMargins(10, 10, 10, 10)
            txtFeelInication.layoutParams = lp
            binding.flexboxlayout.addView(txtFeelInication)
        }
    }
}