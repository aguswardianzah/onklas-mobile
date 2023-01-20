package id.onklas.app.pages.theory

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.R
import id.onklas.app.databinding.MateriDetailPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import java.io.File
import java.io.FileOutputStream

class MateriDetailPage : Privatepage() {

    private val binding by lazy { MateriDetailPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<TheoryViewModel> { component.theoryVmFactory }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
            binding.toolbar.title = intent.getStringExtra("title") ?: "Detail Materi"
            title = intent.getStringExtra("title") ?: "Detail Materi"
        }

        val paramMateriId = if (intent.hasExtra("id")) intent.getIntExtra("id", 0) else 0
        val paramSubjectId = if (intent.hasExtra("subId")) intent.getIntExtra("subId", 0) else 0
        if (paramSubjectId > 0 && paramMateriId > 0) {
            loading(msg = "menampilkan detail materi")
            viewmodel.detailMateri(paramMateriId, paramSubjectId).observe(this, { materiWithLink ->
                val item = materiWithLink.materi
                dismissloading()
                val uri = Uri.parse(item.file_path.replace("\\", ""))

                if (item.description.isEmpty()) {
                    binding.descLabel.visibility = View.GONE
                    binding.desc.visibility = View.GONE
                } else {
                    binding.descLabel.visibility = View.VISIBLE
                    binding.desc.visibility = View.VISIBLE
                    binding.desc.text = item.description
                }

                binding.materiLayout.root.isVisible =
                    item.file_name.isNotEmpty() && item.file_path.isNotEmpty()
                val size = if (item.file_size.isEmpty()) 0L else item.file_size.toLong()
                binding.materiLayout.name.text =
                    "${item.file_name} | ${viewmodel.fileUtils.getStringSizeLengthFile(size)}"

                binding.materiLayout.btnLihat.setOnClickListener {
                    lifecycleScope.launchWhenCreated {
                        try {
                            loading(title = "menampilkan data")
                            val download = component.apiService.download(uri.toString())
                            val file = File(
                                filesDir,
                                uri.lastPathSegment ?: item.name
                            ).also { if (!it.exists()) it.createNewFile() }

                            FileOutputStream(file).apply {
                                write(download.bytes())
                                flush()
                                close()
                            }

                            viewmodel.intentUtil.openFile(
                                this@MateriDetailPage,
                                file,
                                item.name
                            )
                        } catch (e: Exception) {
                            toast(e.message)
                        } finally {
                            dismissloading()
                        }
                    }
//                    viewmodel.intentUtil.openPdf(this@MateriDetailPage, uri.toString(), item.name)
                }

//                binding.materiLayout.btnDownload.visibility = View.GONE
                binding.materiLayout.btnDownload.setOnClickListener {
                    viewmodel.intentUtil.downloadFile(
                        this@MateriDetailPage,
                        uri,
                        item.file_name,
                        "materi"
                    )
                }

                if (materiWithLink.link.isNotEmpty()) {
                    binding.linkPreview.visibility = View.VISIBLE
                    lifecycleScope.launchWhenCreated {
                        binding.linkPreview.loadUrl(
                            viewmodel.apiService,
                            materiWithLink.link.first().link
                        )
                    }
                } else
                    binding.linkPreview.visibility = View.GONE

            })
        } else MaterialAlertDialogBuilder(this, R.style.DialogTheme)
            .setTitle("Materi Tidak tersedia")
            .setMessage("Halaman yang kamu cari sudah tidak tersedia")
            .setCancelable(false)
            .setPositiveButton("Tutup") { dialog, which ->
                dialog.dismiss()
                supportFinishAfterTransition()
            }
            .show()
    }
}