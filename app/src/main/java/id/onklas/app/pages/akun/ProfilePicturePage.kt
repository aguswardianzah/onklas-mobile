package id.onklas.app.pages.akun

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yalantis.ucrop.UCrop
import droidninja.filepicker.FilePickerConst
import id.onklas.app.R
import id.onklas.app.databinding.ProfilePicturePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.IntentUtil
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class ProfilePicturePage : Privatepage() {

    private val binding by lazy { ProfilePicturePageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<ProfilePictureViewmodel> { component.profilePicVmFactory }
    private var hasChange = false
    private val editable by lazy { intent.getBooleanExtra("editable", false) }
    private val extraPath by lazy { intent.getStringExtra("path") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        }

        binding.imagePath =
            if (extraPath.isNullOrEmpty()) viewmodel.userTable.user_avatar_image else extraPath
    }

    override fun onBackPressed() {
        if (hasChange && editable) {
            MaterialAlertDialogBuilder(this, R.style.DialogTheme)
                .setMessage("Simpan perubahan?")
                .setPositiveButton("Ya") { dialog, which ->
                    dialog.dismiss()
                    saveProfilePicture()
                }
                .setNegativeButton("tidak") { dialog, which ->
                    super.onBackPressed()
                }.show()
        } else
            supportFinishAfterTransition()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile_picture, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menu_edit)?.isVisible = editable
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_edit) {
            MaterialAlertDialogBuilder(this, R.style.DialogTheme)
                .setTitle("Ambil gambar dari")
                .setItems(arrayOf("Kamera", "Galeri")) { dialog, which ->
                    Timber.e("klik opsi: $which")
                    if (which == 0)
                        viewmodel.intentUtil.openCamera(this)
                    else
                        viewmodel.intentUtil.openGalleryPhoto(this, "Pilih foto profil")
                    dialog.dismiss()
                }
                .show()
        } else if (item.itemId == R.id.menu_save) {
            if (hasChange)
                saveProfilePicture()
        }
        return true
    }

    private fun saveProfilePicture() {
        lifecycleScope.launch {
            loading(msg = "menyimpan foto profil")
            val res = viewmodel.uploadAvatar()
            dismissloading()
            if (res) {
                setResult(Activity.RESULT_OK, Intent().setData(Uri.parse(viewmodel.path.value)))
                supportFinishAfterTransition()
            } else {
                toast("gagal menyimpan gambar")
            }
            hasChange = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IntentUtil.RC_GALLERY_PHOTO) {
            data?.data?.let { imageEditor(it) }

//            val uri =
//                data?.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
//            if (!uri.isNullOrEmpty()) {
//                imageEditor(uri.first())
//            }
        } else if (requestCode == IntentUtil.RC_CAMERA && resultCode == RESULT_OK) {
            imageEditor(Uri.fromFile(File(viewmodel.intentUtil.currentPhotoPath)))
//            data?.getStringExtra("fileUri")?.let {
//                imageEditor(Uri.parse(it))
//            }
        } else if (requestCode == UCrop.REQUEST_CROP && data != null) {
            UCrop.getOutput(data)?.let { uri ->
                binding.imagePath = uri
                viewmodel.path.postValue(uri.toString())
                hasChange = true
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private val colorTextBlack by lazy { ContextCompat.getColor(this, R.color.textBlack) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private fun imageEditor(uri: Uri) {
        UCrop.of(uri, Uri.fromFile(File(cacheDir, "${System.currentTimeMillis()}.jpg")))
            .withOptions(UCrop.Options().apply {
                setCompressionFormat(Bitmap.CompressFormat.JPEG)
                setCompressionQuality(80)
                setShowCropGrid(false)
                withMaxResultSize(640, 640)
                withAspectRatio(1f, 1f)
                setDimmedLayerColor(Color.parseColor("#99FFFFFF"))
                setRootViewBackgroundColor(Color.WHITE)
                setShowCropFrame(false)
                setStatusBarColor(Color.WHITE)
                setToolbarColor(Color.WHITE)
                setActiveControlsWidgetColor(colorPrimary)
                setToolbarWidgetColor(colorTextBlack)
                setToolbarTitle("Atur Gambar")
                setCircleDimmedLayer(true)
            })
            .start(this)
    }
}