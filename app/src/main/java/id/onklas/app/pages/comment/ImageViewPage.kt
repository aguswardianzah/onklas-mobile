package id.onklas.app.pages.comment

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import id.onklas.app.R
import id.onklas.app.databinding.ImageViewPageBinding
import id.onklas.app.pages.Privatepage

class ImageViewPage : Privatepage() {

    private val binding by lazy { ImageViewPageBinding.inflate(layoutInflater) }
    private val mTitle by lazy { intent.getStringExtra("title").orEmpty() }
    private val allowDownload by lazy { intent.getBooleanExtra("downloadable", true) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            if (mTitle.isNotEmpty())
                binding.toolbar.title = mTitle

            binding.toolbar.setNavigationOnClickListener { supportFinishAfterTransition() }
        }

        intent.data?.let {
            binding.imagePath = it
        } ?: run {
            MaterialAlertDialogBuilder(this, R.style.DialogTheme)
                .setTitle("Halaman Tidak tersedia")
                .setMessage("Gambar yang kamu cari sedang tidak tersedia, mohon ulangi beberapa saat lagi")
                .setCancelable(false)
                .setPositiveButton("Tutup") { dialog, which ->
                    dialog.dismiss()
                    finish()
                }
                .show()
        }
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_image_view, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menu_save)?.isVisible = allowDownload
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT <= 29)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        Dexter.withContext(this)
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) {
                        toast("proses download gambar akan dimulai sesaat lagi")
                        (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?)?.enqueue(
                            DownloadManager.Request(intent.data).apply {
                                setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    (intent.data?.lastPathSegment
                                        ?: "onklas-post-${System.currentTimeMillis()}.jpg")
                                );
                                allowScanningByMediaScanner()
                                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            }
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .check()
        return true
    }
}