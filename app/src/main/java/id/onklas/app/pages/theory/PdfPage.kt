package id.onklas.app.pages.theory

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter
import id.onklas.app.R
import id.onklas.app.databinding.PdfPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class PdfPage : Privatepage() {

    private val binding by lazy { PdfPageBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val titleText = intent.getStringExtra("title") ?: "Baca PDF"
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
            binding.toolbar.title = titleText
            title = titleText
        }

        intent.getStringExtra("file_path")?.let {
            var file = File(it)
            if (file.exists()) {
                binding.progress.visibility = View.GONE
                binding.vpPdf.adapter = PDFPagerAdapter(this, file.path)
                initPageController()
                return@let
            }

            val uri = Uri.parse(it)
            file = File(filesDir, "${uri.lastPathSegment ?: titleText}.pdf")
            Timber.e("file path: ${file.absolutePath}")

            if (file.exists()) {
                binding.progress.visibility = View.GONE
                binding.vpPdf.adapter = PDFPagerAdapter(this, file.path)
                initPageController()
            } else {
                val errorMsg =
                    "Gangguan koneksi ketika membuka file, silahkan ulangi beberapa saat lagi"
                try {
                    lifecycleScope.launchWhenCreated {
                        toast("Sedang membuka file")
                        val download = component.apiService.download(it)
                        FileOutputStream(file).apply {
                            write(download.bytes())
                            flush()
                            close()
                        }

                        binding.progress.visibility = View.GONE
                        binding.vpPdf.adapter =
                            PDFPagerAdapter(this@PdfPage, file.absolutePath)
                        initPageController()
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                    alert(
                        msg = errorMsg,
                        okClick = this::finish
                    )
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initPageController() {
        val pageSize = binding.vpPdf.adapter.count
        binding.pageLabel.text = "1/$pageSize"
        hideController()

        binding.pageLabel.setOnClickListener {
            AlertDialog.Builder(this@PdfPage, R.style.DialogTheme)
                .setTitle("Pindah halaman")
                .setItems((1..pageSize).map { "$it/$pageSize" }
                    .toTypedArray()) { dialog, which ->
                    dialog.dismiss()
                    binding.vpPdf.currentItem = which - 1
                }
                .show()
        }

        binding.btnUp.setOnClickListener {
            if (binding.vpPdf.currentItem > 0)
                binding.vpPdf.currentItem = binding.vpPdf.currentItem - 1
        }

        binding.btnDown.setOnClickListener {
            if (binding.vpPdf.currentItem < pageSize - 1)
                binding.vpPdf.currentItem = binding.vpPdf.currentItem + 1
        }

        binding.vpPdf.setOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                binding.pageLabel.text = "${position + 1}/$pageSize"
                showController()
                hideController()
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    private var hideJob: Job? = null
    private fun hideController() {
        hideJob?.cancel()
        hideJob = lifecycleScope.launch {
            delay(2000)
            binding.pageController.visibility = View.GONE
        }
    }

    private fun showController() {
        binding.pageController.visibility = View.VISIBLE
    }
}