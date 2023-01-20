package id.onklas.app.pages.ujian

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import id.onklas.app.R
import id.onklas.app.databinding.UjianDetailPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BasePage
import id.onklas.app.pages.pembayaran.RowAdapter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class UjianDetailPage : BasePage() {

    private val binding by lazy { UjianDetailPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<UjianViewModel> { component.ujianVmFactory }
    private val id by lazy { intent.getStringExtra("id").orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        viewmodel.errorString.observe(this, Observer(this::toast))

        lifecycleScope.launchWhenCreated {
            viewmodel.memoryDB.ujian().get(id.toInt())?.let {
                binding.item = it

                binding.rvInfo.adapter = RowAdapter(
                    arrayListOf(
                        "Name" to viewmodel.student.name,
                        "Kelas" to viewmodel.student.student_class.class_room.name,
                        "Tanggal" to it.dateLabel,
                        "Waktu" to it.time,
                        "Sekolah" to viewmodel.school.name
                    )
                )
                binding.rvInfo.addItemDecoration(
                    DividerItemDecoration(this@UjianDetailPage, OrientationHelper.VERTICAL)
                )

                binding.btnJawaban.setOnClickListener { _ ->
                    lifecycleScope.launchWhenCreated {
                        val endTime =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("${it.date} ${it.endAt}").time
                        if (System.currentTimeMillis() > endTime) {
                            binding.btnJawaban.isEnabled = false
                            loading(msg = "mohon tunggu")
                            val res = viewmodel.detailUjian(id)
                            dismissloading()
                            binding.btnJawaban.isEnabled = true
                            if (res)
                                startActivity(
                                    Intent(
                                        this@UjianDetailPage,
                                        TakeUjianPage::class.java
                                    ).putExtra("id", id).putExtra("scored", true)
                                )
                        } else {
                            toast("Jawaban dapat dilihat setelah jadwal ujian berakhir")
                        }
                    }
                }

                val logFileName = "${viewmodel.pref.getInt("user_id")}_${it.date}.txt"
                val logFile = File(filesDir, logFileName)
                if (logFile.exists()) {
                    binding.btnReport.visibility = View.VISIBLE
                    binding.btnReport.setOnClickListener { _ ->
                        binding.btnReport.isEnabled = false

                        MaterialAlertDialogBuilder(this@UjianDetailPage, R.style.DialogTheme)
                            .setTitle("Laporkan Nilai")
                            .setMessage("Jika terdapat jawaban atau nilai yang tidak sesuai dengan pekerjaan Anda, segera laporkan nilai ujian Anda agar kami dapat membantu memperbaiki")
                            .setPositiveButton("Laporkan") { dialog, _ ->
                                dialog.dismiss()
                                loading(msg = "Mohon tunggu")
                                Firebase.storage.reference.child("logs/$logFileName")
                                    .putFile(Uri.fromFile(logFile)).addOnCompleteListener {
                                        dismissloading()
                                        if (it.isSuccessful)
                                            toast("Laporan sudah dikirimkan, silahkan tunggu perbaruan nilai dari kami")
                                        else {
                                            toast("Gagal mengirimkan laporan")
                                            binding.btnReport.isEnabled = true
                                        }
                                    }
                            }
                            .setNegativeButton("Batal") { dialog, _ ->
                                binding.btnReport.isEnabled = true
                                dialog.dismiss()
                            }
                            .show()
                    }
                } else
                    binding.btnReport.visibility = View.GONE

                binding.executePendingBindings()
            } ?: run {
                toast("halaman tidak valid")
            }
        }
    }
}