package id.onklas.app.pages.studentcard

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.yalantis.ucrop.UCrop
import id.onklas.app.R
import id.onklas.app.databinding.BottomSheetPentunjukStudentCardBinding
import id.onklas.app.databinding.StudentCardPageBinding
import id.onklas.app.databinding.UploadFotoPelajarDialogBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.IntentUtil
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*

class StudentCardPage : Privatepage() {

    private val binding by lazy { StudentCardPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<StudentCardVM> { component.studentCardVmFactory }
    private val colorPrimary by lazy {

        ResourcesCompat.getColor(
            resources,
            R.color.colorPrimary,
            null
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_setting_akun, menu)
        return super.onCreateOptionsMenu(menu)
    }

    var isEdit = false
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.apply {
            findItem(R.id.menu_edit).isVisible = !isEdit
            findItem(R.id.menu_save).apply {
                isVisible = isEdit
                isEnabled = true
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_save) {
            lifecycleScope.launch {
                loading(msg = "Sedang mengupdate data")
                val success = viewmodel.updateData()
                dismissloading()

                if (success)
                    prettyAlert(
                        true,
                        titleText = "Berhasil mengupdate data",
                        okLabel = "Tutup",
                        okClick = { finish() },
                        abortLabel = ""
                    )
            }
        } else if (item.itemId == R.id.menu_edit) {
            isEdit = true
            binding.isEdit = isEdit
            invalidateOptionsMenu()
            binding.executePendingBindings()
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.lifecycleOwner = this
        binding.vm = viewmodel
        binding.isEdit = isEdit

        binding.lblBaca.setOnClickListener {
            bottomsheetSelectSearch(this, layoutInflater)
        }

        binding.inDate.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val calendar = Calendar.getInstance()
                DatePickerDialog.newInstance { view, year, monthOfYear, dayOfMonth ->
                    view.dismiss()
                    calendar[year, monthOfYear, dayOfMonth, 0] = 0
                    viewmodel.editDate.postValue(viewmodel.dateFormat.format(calendar.time))
                    viewmodel.sendDate.postValue(viewmodel.sendDateFormat.format(calendar.time))
                    binding.inDate.clearFocus()
                }
                    .apply {
                        accentColor = colorPrimary
                        minDate = Calendar.getInstance().apply {
                            set(Calendar.YEAR, 1970)
                        }
                        show(supportFragmentManager, "date_picker")
                    }
            }
        }

        binding.imgStudentAdd.clipToOutline = true
        binding.actionAddImg.setOnClickListener { uplodFotoDialog() }
        binding.radioLk.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                viewmodel.editGender.postValue("laki-laki")
            else
                viewmodel.editGender.postValue("perempuan")
        }
        binding.radioPr.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked)
                viewmodel.editGender.postValue("laki-laki")
            else
                viewmodel.editGender.postValue("perempuan")
        }

        viewmodel.errorString.observe(this, Observer(this::toast))
        viewmodel.loading.observe(this) {
            if (it) loading(msg = "memproses data")
            else dismissloading()
        }

        binding.executePendingBindings()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IntentUtil.RC_CAMERA && resultCode == RESULT_OK) {
            imageEditor(Uri.fromFile(File(viewmodel.intentUtil.currentPhotoPath)))
        } else if (requestCode == IntentUtil.RC_GALLERY_PHOTO && resultCode == RESULT_OK) {
            data?.data?.let { imageEditor(it) }
        } else if (requestCode == UCrop.REQUEST_CROP && data != null) {
            UCrop.getOutput(data)?.let { uri ->
                viewmodel.editImage.postValue(uri.path.toString())
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private val colorTextBlack by lazy { ContextCompat.getColor(this, R.color.textBlack) }
    private fun imageEditor(uri: Uri) {
        Timber.e("uri: $uri")
        UCrop.of(uri, Uri.fromFile(File(cacheDir, "${System.currentTimeMillis()}.jpg")))
            .withOptions(UCrop.Options().apply {
                setCompressionFormat(Bitmap.CompressFormat.JPEG)
                setCompressionQuality(80)
                setShowCropGrid(false)
                withMaxResultSize(720, 960)
                withAspectRatio(3f, 4f)
                setDimmedLayerColor(Color.parseColor("#99FFFFFF"))
                setRootViewBackgroundColor(Color.WHITE)
                setShowCropFrame(false)
                setStatusBarColor(Color.WHITE)
                setToolbarColor(Color.WHITE)
                setActiveControlsWidgetColor(colorPrimary)
                setToolbarWidgetColor(colorTextBlack)
                setToolbarTitle("Atur Gambar")
            })
            .start(this)
    }

    private fun uplodFotoDialog() {
        val dialogBinding = UploadFotoPelajarDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

        dialog.setCancelable(true)

        dialogBinding.apply {
            actionCamera.setOnClickListener {
                viewmodel.intentUtil.openCamera(this@StudentCardPage)
                dialog.dismiss()
            }

            actionGalery.setOnClickListener {
                viewmodel.intentUtil.openGalleryPhoto(
                    this@StudentCardPage,
                    "Pilh Gambar"
                )
                dialog.dismiss()
            }
        }
    }

    private fun hapusFotoDialog() {
        val view = layoutInflater.inflate(R.layout.hapus_foto_pelajar_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

        dialog.setCancelable(true)
    }

    private fun bottomsheetSelectSearch(context: Context, layoutInflater: LayoutInflater) {
        val dialogBinding = BottomSheetPentunjukStudentCardBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(context)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.toolbar.setNavigationOnClickListener { dialog.dismiss() }
        dialogBinding.txtPetunjuk.text = HtmlCompat.fromHtml(
            viewmodel.template.value?.instruction.orEmpty(),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        dialog.setContentView(dialogBinding.root)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val parentLayout =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        parentLayout?.let { it ->
            val behaviour = BottomSheetBehavior.from(it)
            setupFullHeight(it)
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.show()
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }
}