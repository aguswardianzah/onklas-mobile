package id.onklas.app.pages.prokes.prokesteacher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexboxLayout
import id.onklas.app.R
import id.onklas.app.databinding.CekScreeningPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BasePage
import id.onklas.app.pages.prokes.*
import kotlinx.coroutines.launch
import timber.log.Timber

class CekScreeningPage : BasePage() {
    companion object {
        fun open(
            activity: Activity,
            studentName: String,
            StudentId: Int,
            onScreening: Boolean = false,
            suhu: String = "",
            HistoryOfIllnesKey: String = "",
            HistoryOfIllnesText: String = "",
            FeelIndicationKey: List<String> = emptyList(),
            FeelIndicationText: List<String> = emptyList()
        ) {
            var Feeltext = ArrayList<String>()
            var Feelkey = ArrayList<String>()
            if (FeelIndicationText.isNotEmpty()) {
                Feeltext.addAll(FeelIndicationText)
                Feelkey.addAll(FeelIndicationKey)
            }
            Timber.e("feel text $Feeltext")
            Timber.e("feel text key  $Feelkey")
            activity.startActivityForResult(
                Intent(activity, CekScreeningPage::class.java)
                    .putExtra("student_name", studentName)
                    .putExtra("student_id", StudentId)
                    .putExtra(
                        "onScreening",
                        onScreening
                    ) //  onscreening true atau lihat detail  false
                    .putExtra("suhu", suhu)
                    .putExtra("historyOfIllnesText", HistoryOfIllnesText)
                    .putExtra("historyOfIllnesKey", HistoryOfIllnesKey)
                    .putStringArrayListExtra("feelIndicationText", Feeltext)
                    .putStringArrayListExtra("feelIndicationKey", Feelkey), 214
            )
        }
    }

    private val binding by lazy { CekScreeningPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<ProkesViewmodel> { component.prokesVmFactory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        var titleTop = ""
        if (intent.getStringExtra("student_name")?.isNotEmpty()!!) {
            titleTop = intent.getStringExtra("student_name").toString()
        } else {
            titleTop = "Cek Pengisian"
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = titleTop
            binding.toolbar.title = titleTop
            binding.toolbar.setNavigationOnClickListener {
                finish()
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

        binding.onScreening = intent.getBooleanExtra("onScreening", false)
        if (intent.getBooleanExtra("onScreening", false)) {
            //insert manual ke vm dari data yang telah di inputkan
            try {
                val suhu = intent.getStringExtra("suhu")
                val HistoryOfIllnesText = intent.getStringExtra("historyOfIllnesText")
                val HistoryOfIllnesKey = intent.getStringExtra("historyOfIllnesKey")
                val FeelIndicationText = intent.getStringArrayListExtra("feelIndicationText")
                val FeelIndicationKey = intent.getStringArrayListExtra("feelIndicationKey")

                Timber.e("HistoryOfIllnesText $HistoryOfIllnesText")
                Timber.e("HistoryOfIllnesKey $HistoryOfIllnesKey")
                Timber.e("FeelIndicationText $FeelIndicationText")
                Timber.e("FeelIndicationKey  $FeelIndicationKey")

                val ListFeelIndication = ArrayList<GlobalsValueInnerItem>()
                for (i in 0 until FeelIndicationKey!!.size) {
                    ListFeelIndication.add(
                        GlobalsValueInnerItem(
                            FeelIndicationKey.get(i).toString(),
                            FeelIndicationText!!.get(i).toString(),
                        )
                    )
                }
                viewmodel.loadHistoryReportStudent.postValue(
                    ResponseCheckReport(
                        data = CheckReportItem(
                            check = Check(
                                student = cekStudentItem(
                                    temperature = suhu.toString(),
                                    history_of_illness = GlobalsValueInnerItem(
                                        HistoryOfIllnesKey.toString(),
                                        HistoryOfIllnesText.toString()
                                    ),
                                    feel_indication = ListFeelIndication
                                ),
                                staff = null
                            ),
                        )
                    )
                )
                viewmodel.LoadingShow.postValue(false)
            } catch (e: Exception) {
            }
        } else {
            lifecycleScope.launch {
                viewmodel.loadHistoryReportStudent(intent.getIntExtra("student_id", 0))
            }
        }

        viewmodel.loadHistoryReportStudent.observe(this, Observer {
            binding.item = it
            setTextViewInFlexbox(it.data!!.check!!.student!!.feel_indication.map { it.text })
        })

        binding.btnConfirm.setOnClickListener {
            val suhu = intent.getStringExtra("suhu")!!.replace(",", ".")
            alertSelectNew(
                "Proses Formulir",
                "Anda setuju untuk melanjutkan proses dan mengirimkan laporan formulir ini?\n",
                "Proses Sekarang",
                {
                    lifecycleScope.launch {
                        viewmodel.sendScreening(
                            intent.getStringExtra("historyOfIllnesKey").toString(),
                            intent.getStringArrayListExtra("feelIndicationKey")!!,
                            suhu,
                            intent.getIntExtra("student_id", 0)
                        )
                    }
                },
                "Cek Ulang"
            )
        }
        viewmodel.sendScreeningResponse.observe(this, {
//            val suhu = intent.getStringExtra("suhu")!!.replace(",", ".")
            if (it.indication_of_covid) {
                prettyAlert(
                    showImage = true, isSuccess = true, customImg = R.drawable.ic_indikasi_covid,
                    titleText = "Terindikasi Covid 19",
                    msg = "Jika suhu tubuh pelajar tinggi, sarankan pelajar merawat diri dan lakukan isolasi mandiri",
                    okLabel = "Ok, sarankan mengisolasi mandiri",
                    okClick = {
                        val returnIntent = Intent()
                        setResult(RESULT_OK, returnIntent)
                        finish()
                    },
                    abortLabel = ""
                )
            } else {
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
            }
        })
    }


    private fun setTextViewInFlexbox(HistoryOfIllnes: List<String>) {
        binding.flexboxlayout.removeAllViews()
        for (i in HistoryOfIllnes.indices) {
            val txtFeelInication = TextView(this)
            txtFeelInication.text = HistoryOfIllnes[i]
            txtFeelInication.textSize = 12f
            txtFeelInication.gravity = Gravity.CENTER
            txtFeelInication.setPadding(
                resources.getDimension(R.dimen._10sdp).toInt(),
                resources.getDimension(R.dimen._8sdp).toInt(),
                resources.getDimension(R.dimen._10sdp).toInt(),
                resources.getDimension(R.dimen._8sdp).toInt()
            )
            txtFeelInication.background = resources.getDrawable(R.drawable.border_white_radius6)
            txtFeelInication.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.e4e4)
            txtFeelInication.setTextColor(resources.getColor(android.R.color.black))

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