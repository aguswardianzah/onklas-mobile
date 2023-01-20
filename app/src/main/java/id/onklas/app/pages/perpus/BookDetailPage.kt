package id.onklas.app.pages.perpus

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.R
import id.onklas.app.databinding.BookDetailPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import timber.log.Timber

class BookDetailPage : Privatepage() {

    private val binding by lazy { BookDetailPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<PerpusViewModel> { component.perpusVmFactory }
    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private val navController by lazy { findNavController(R.id.page_container) }
    private val buttons by lazy { arrayOf(binding.btnDesc, binding.btnDetail) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        if (intent.hasExtra("id") && intent.getIntExtra("id", 0) > 0) {
            binding.image.clipToOutline = true
            viewmodel.bookDetail.observe(this@BookDetailPage, {
                binding.item = it
                binding.executePendingBindings()
                if (it.cover.isNotEmpty()) {
                    Glide.with(binding.image.context)
                        .load(thumbnail(it.cover, binding.image.width))
                        .placeholder(R.drawable.img_loading_book_cover)
                        .error(R.drawable.img_error_book_cover)
                        .thumbnail(0.1f)
                        .fitCenter()
                        .into(binding.image)
                } else {
                    binding.image.setImageResource(R.drawable.img_default_book_cover)
                }
            })

            lifecycleScope.launchWhenCreated {
                val progress =
                    ProgressDialog.show(this@BookDetailPage, "", "menampilkan detail buku")

                val id = intent.getIntExtra("id", 0)
                val res = viewmodel.getDetail(id)
                val stock = viewmodel.getStock(id)

                binding.available = stock.first
                binding.stock = stock.second

                progress.dismiss()
                if (!res)
                    close()
                else {
                    binding.btnDetail.setOnClickListener {
                        navController.navigate(R.id.action_global_bookInfoPage)
                        setButton(binding.btnDetail)
                    }

                    binding.btnDesc.setOnClickListener {
                        navController.navigate(R.id.action_global_bookDescPage)
                        setButton(binding.btnDesc)
                    }
                }
            }
        } else
            close()
    }

    fun thumbnail(url: String, width: Int) = url.let {
        var newUrl = it
        newUrl = newUrl.replace("assets.onklas.id/", "thumbnail.onklas.id/")
        newUrl = newUrl.plus(if (!newUrl.contains('?')) "?" else "&")
        newUrl = newUrl.plus("width=${width}")
        Timber.e("load thumbnail: $newUrl")
        newUrl
    }

    private fun close() {
        MaterialAlertDialogBuilder(this, R.style.DialogTheme)
            .setTitle("Buku tidak ditemukan")
            .setMessage(
                viewmodel.errorString.value ?: "Tidak terdapat data buku sesuai pencarian Anda"
            )
            .setCancelable(false)
            .setPositiveButton("Tutup") { dialog, which ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun setButton(btn: View) {
        buttons.forEach {
            if (btn == it) {
                it.setStrokeColorResource(R.color.colorPrimary)
                it.setTextColor(colorPrimary)
            } else {
                it.setStrokeColorResource(R.color.gray)
                it.setTextColor(colorGray)
            }
        }
    }
}