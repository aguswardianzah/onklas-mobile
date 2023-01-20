package id.onklas.app.pages.createpost

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.models.ErrorModel
import com.linkdev.filepicker.models.FileData
import com.linkdev.filepicker.models.MimeType
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import id.onklas.app.R
import id.onklas.app.databinding.UploadEbookPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.IntentUtil
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.ArrayList

class UploadEbookPage : Privatepage() {

    private val binding by lazy { UploadEbookPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<CreatePostViewmodel> { component.createPostVmFactory }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private var pickImgFactory: IPickFilesFactory? = null
    private var pickPdfFactory: IPickFilesFactory? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.viewmodel = viewmodel
        viewmodel.feedContent.observe(this, Observer {
            binding.titleCounter.text = "${it.length}/500"
            if (it.isNotEmpty()) {
                binding.inputDivider.setBackgroundColor(colorPrimary)
                checkAllowUpload()
            } else {
                binding.inputDivider.setBackgroundColor(colorGray)
            }
        })

        viewmodel.feedAuthor.observe(this, Observer {
            binding.authorCounter.text = "${it.length}/50"
            if (it.isNotEmpty()) {
                binding.authorDivider.setBackgroundColor(colorPrimary)
                checkAllowUpload()
            } else {
                binding.authorDivider.setBackgroundColor(colorGray)
            }
        })

        viewmodel.feedThumb.observe(this, Observer {
            if (it.isNotEmpty()) checkAllowUpload()
        })

        viewmodel.feedFile.observe(this, Observer {
            if (it.isNotEmpty()) checkAllowUpload()
        })

        viewmodel.errorString.observe(this, Observer(this::toast))

        binding.btnCover.setOnClickListener {
            pickImgFactory = viewmodel.intentUtil.openFilePicker2(
                this,
                "Pilih Cover Ebook",
                arrayListOf(MimeType.ALL_IMAGES),
                483
            )
//
//            viewmodel.intentUtil.openGalleryPhoto(
//                this,
//                "Cover Ebook"
//            )
        }
        binding.btnFile.setOnClickListener {
            pickPdfFactory = viewmodel.intentUtil.openFilePicker2(
                this,
                "Pilih File Materi",
                arrayListOf(MimeType.PDF),
                832
            )
//            viewmodel.intentUtil.openPdfPicker(
//                this,
//                "File Ebook"
//            )
        }
        binding.btnPost.setOnClickListener {
            if (viewmodel.feedContent.value.isNullOrEmpty())
                MaterialAlertDialogBuilder(this, R.style.DialogTheme)
                    .setMessage("judul tidak boleh kosong")
                    .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                    .show()
            else if (viewmodel.feedAuthor.value.isNullOrEmpty())
                MaterialAlertDialogBuilder(this, R.style.DialogTheme)
                    .setMessage("nama pengarang tidak boleh kosong")
                    .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                    .show()
            else
                lifecycleScope.launch {
                    loading(msg = "mengupload ebook")
                    val uploadRes = viewmodel.createEbook()
                    dismissloading()

                    if (uploadRes)
                        finish()
                }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            483 -> {
                pickImgFactory?.handleActivityResult(requestCode, resultCode, data, object :
                    PickFilesStatusCallback {
                    override fun onFilePicked(fileData: ArrayList<FileData>) {
                        if (fileData.isNotEmpty()) {
                            val file = fileData.first()
                            val uri = file.uri ?: return

                            viewmodel.feedThumb.postValue(file.filePath)
                            binding.coverImage = uri.toString()

                            lifecycleScope.launch {
                                val bitmapfromuri =
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        ImageDecoder.decodeBitmap(
                                            ImageDecoder.createSource(
                                                contentResolver, uri
                                            )
                                        )
                                    } else BitmapFactory.decodeFile(file.filePath)

                                val bitmap = viewmodel.fileUtils.resizeBitmap(
                                    bitmapfromuri,
                                    680,
                                    720
                                )
                                val newCover = File(filesDir, "cover.jpg").apply { createNewFile() }
                                if (viewmodel.fileUtils.bitmapToFile(bitmap, newCover)) {
                                    viewmodel.feedThumb.postValue(Uri.fromFile(newCover).toString())
                                }
                            }
                        }
                    }

                    override fun onPickFileCanceled() {
                    }

                    override fun onPickFileError(errorModel: ErrorModel) =
                        toast(getString(errorModel.errorMessage))
                })
            }
            832 -> {
                pickPdfFactory?.handleActivityResult(requestCode, resultCode, data, object :
                    PickFilesStatusCallback {
                    override fun onFilePicked(fileData: ArrayList<FileData>) {
                        if (fileData.isNotEmpty()) {
                            val file = fileData.first().file ?: return
//                            if (file.length() > 5 * 1024 * 1024) {
//                                toast("Ukuran maksimal file 5Mb")
//                            } else {
                                viewmodel.feedFile.postValue(file.path)
                                binding.icPdf.visibility = View.VISIBLE
                                binding.uploadFileInfo.text =
                                    "${file.name} (ukuran: ${
                                        viewmodel.fileUtils.getStringSizeLengthFile(
                                            file.length()
                                        )
                                    })"
//                            }
                        }
                    }

                    override fun onPickFileCanceled() {
                    }

                    override fun onPickFileError(errorModel: ErrorModel) =
                        toast(getString(errorModel.errorMessage))
                })
            }
            IntentUtil.RC_GALLERY_PHOTO -> {
                val uri =
                    data?.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
                if (!uri.isNullOrEmpty()) {
                    ContentUriUtils.getFilePath(this, uri.first())?.let {
                        Timber.e("file cover: $it")
                        viewmodel.feedThumb.postValue(it)
                        binding.coverImage = uri.first().toString()

                        lifecycleScope.launch {
                            val bitmapfromuri =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    ImageDecoder.decodeBitmap(
                                        ImageDecoder.createSource(
                                            contentResolver, uri.first()
                                        )
                                    )
                                } else BitmapFactory.decodeFile(it)

                            val bitmap = viewmodel.fileUtils.resizeBitmap(
                                bitmapfromuri,
                                680,
                                720
                            )
                            val newCover = File(filesDir, "cover.jpg").apply { createNewFile() }
                            if (viewmodel.fileUtils.bitmapToFile(bitmap, newCover)) {
                                viewmodel.feedThumb.postValue(Uri.fromFile(newCover).toString())
                            }
                        }
                    }
                }
            }
            IntentUtil.RC_PDF_PICKER -> {
                val uri =
                    data?.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_DOCS)
                if (!uri.isNullOrEmpty()) {
                    ContentUriUtils.getFilePath(this, uri.first())?.let {
                        val file = File(it)
                        if (file.length() > 12 * 1024 * 1024) {
                            toast("Ukuran maksimal file 12Mb")
                        } else {
                            viewmodel.feedFile.postValue(it)
                            binding.icPdf.visibility = View.VISIBLE
                            binding.uploadFileInfo.text =
                                "${file.name} (ukuran: ${
                                    viewmodel.fileUtils.getStringSizeLengthFile(
                                        file.length()
                                    )
                                })"
                        }
                    }

                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun checkAllowUpload() {
        binding.btnPost.isEnabled = (
                !viewmodel.feedContent.value.isNullOrEmpty() &&
                        !viewmodel.feedAuthor.value.isNullOrEmpty() &&
                        !viewmodel.feedThumb.value.isNullOrEmpty() &&
                        !viewmodel.feedFile.value.isNullOrEmpty()
                )
    }
}