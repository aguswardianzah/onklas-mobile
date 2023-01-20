package id.onklas.app.pages.announcement

import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.R
import id.onklas.app.databinding.AnnouncementDetailPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class AnnouncementDetailPage : Privatepage() {

    private val binding by lazy { AnnouncementDetailPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<AnnouncementViewmodel> { component.announcementVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { supportFinishAfterTransition() }
        }

        viewmodel.getDetail(intent.getIntExtra("id", 0)).observe(this, {
            it?.let {
                binding.imageUrl = it.image
                binding.toolbar.title = it.title
                binding.collapsingToolbar.title = it.title
                title = it.title
                binding.time.text = it.created_label
                binding.content.isNestedScrollingEnabled = false
                binding.content.loadData(it.body, "text/html; charset=utf-8", "UTF-8")
                binding.content.isVerticalScrollBarEnabled = false
                binding.content.isHorizontalScrollBarEnabled = false
            } ?: run {
                MaterialAlertDialogBuilder(this, R.style.DialogTheme)
                    .setTitle("Pengumuman Tidak tersedia")
                    .setMessage("Halaman yang kamu cari sudah tidak tersedia")
                    .setCancelable(false)
                    .setPositiveButton("Tutup") { dialog, which ->
                        dialog.dismiss()
                        finish()
                    }
                    .show()
            }
        })
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }
}