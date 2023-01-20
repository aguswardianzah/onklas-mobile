package id.onklas.app.pages.homework

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.models.ErrorModel
import com.linkdev.filepicker.models.FileData
import com.linkdev.filepicker.models.MimeType
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import id.onklas.app.R
import id.onklas.app.databinding.HomeworkDetailPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.IntentUtil
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

class HomeworkDetailPage : Privatepage() {

    private val binding by lazy { HomeworkDetailPageBinding.inflate(layoutInflater) }
    private lateinit var item: HomeworkItemTable
    private val viewmodel by viewModels<HomeWorkViewModel> { component.homeworkVmFactory }
    private val instruction by lazy { ArrayList<Pair<String, String>>() }
    private val instDownload by lazy { "download" to "Silahkan baca/download soal terlebih dahulu" }
    private val instUpload by lazy { "upload" to "Upload file jawaban atau isikan link jawaban untuk menyelesaikan tugas" }
    private var pickFilesFactory: IPickFilesFactory? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { supportFinishAfterTransition() }
        }

        binding.viewmodel = viewmodel

        lifecycleScope.launchWhenResumed {
            loading(msg = "menampilkan detail tugas")
            if (intent.hasExtra("id") && intent.getIntExtra("id", 0) > 0) {
                val id = intent.getIntExtra("id", 0)
                val isFinished = intent.getBooleanExtra("isFinished", false)
                viewmodel.getDetailHomework(id)?.let {
                    val item = it
                    val isStudent = viewmodel.pref.getBoolean("is_student")
                    binding.isStudent = isStudent
                    binding.item = item

                    if (item.links.any { it.src == HomeworkLinkSource.SRC_TEACHER }) {
                        val link =
                            item.links.first { it.src == HomeworkLinkSource.SRC_TEACHER }.link
                        binding.newLinkPreview.visibility = View.VISIBLE

                        binding.newLinkPreview.loadUrl(
                            viewmodel.apiService,
                            link
                        ) { success, meta ->
                            Timber.e("load new link preview: $success -- meta: $meta")
                        }
                        binding.newLinkPreview.onClick {
                            Timber.e("link clicked, try to remove inst download")
                            if (instruction.contains(instDownload))
                                instruction.remove(instDownload)
                            Timber.e("inst: $instruction")
                        }
                    } else
                        binding.newLinkPreview.visibility = View.GONE

                    if (item.links.any { it.src == HomeworkLinkSource.SRC_STUDENT }) {
                        binding.uploadLinkPreview.loadUrl(
                            viewmodel.apiService,
                            item.links.first { it.src == HomeworkLinkSource.SRC_STUDENT }.link
                        )
                    }

                    val allowUpload =
                        item.homework.uploaded == 1
//                                && !item.homework.is_overdue
                                && !isFinished
                                && isStudent
                    binding.allowUpload = allowUpload
                    binding.isFinished = isFinished

                    if (isFinished) {
                        binding.info = "Kamu sudah mengerjakan tugas"
                        binding.infoColor =
                            ContextCompat.getColor(this@HomeworkDetailPage, R.color.colorPrimary)
                    } else if (item.homework.is_overdue) {
                        binding.info = "Waktu pengumpulan tugas terlambat"
                        binding.infoColor =
                            ContextCompat.getColor(this@HomeworkDetailPage, R.color.red)
                    }

                    // set detail soal
                    if (item.homework.file_path.isNotEmpty()) {
                        val uri = Uri.parse(item.homework.file_path.replace("\\", ""))
                        val size =
                            if (item.homework.file_size.isEmpty()) 0L else item.homework.file_size.toLong()
                        binding.materiDetailLayout.name.text =
                            "${item.homework.file_name} | ${
                                viewmodel.fileUtils.getStringSizeLengthFile(
                                    size
                                )
                            }"
                        binding.materiDetailLayout.btnLihat.text = "baca tugas"
                        binding.materiDetailLayout.btnLihat.setOnClickListener {
                            lifecycleScope.launchWhenCreated {
                                try {
                                    loading(title = "menampilkan data")
                                    val download = component.apiService.download(uri.toString())
                                    val file = File(
                                        filesDir,
                                        uri.lastPathSegment ?: item.homework.title
                                    ).also { if (!it.exists()) it.createNewFile() }

                                    FileOutputStream(file).apply {
                                        write(download.bytes())
                                        flush()
                                        close()
                                    }

                                    viewmodel.intentUtil.openFile(
                                        this@HomeworkDetailPage,
                                        file,
                                        item.homework.title
                                    )
                                    instruction.remove(instDownload)
                                } catch (e: Exception) {
                                    toast(e.message)
                                } finally {
                                    dismissloading()
                                }
                            }
                        }
                        binding.materiDetailLayout.btnDownload.setOnClickListener {
                            viewmodel.intentUtil.downloadFile(
                                this@HomeworkDetailPage,
                                uri,
                                item.homework.file_name,
                                "soal"
                            )
                            instruction.remove(instDownload)
                        }
                    }

                    // set jawaban layout
                    if (item.homework.file_student_path.isNotEmpty()) {
                        val uri = Uri.parse(item.homework.file_student_path.replace("\\", ""))
                        val size =
                            if (item.homework.file_student_size.isEmpty()) 0L else item.homework.file_student_size.toLong()
                        binding.answerLayout.name.text =
                            "${item.homework.file_student_name} | ${
                                viewmodel.fileUtils.getStringSizeLengthFile(
                                    size
                                )
                            }"
                        binding.answerLayout.btnLihat.text = "baca jawaban"
                        binding.answerLayout.btnLihat.setOnClickListener {
                            lifecycleScope.launchWhenCreated {
                                try {
                                    loading(title = "menampilkan data")
                                    val download = component.apiService.download(uri.toString())
                                    val file = File(
                                        filesDir,
                                        uri.lastPathSegment ?: item.homework.title
                                    ).also { if (!it.exists()) it.createNewFile() }

                                    FileOutputStream(file).apply {
                                        write(download.bytes())
                                        flush()
                                        close()
                                    }

                                    viewmodel.intentUtil.openFile(
                                        this@HomeworkDetailPage,
                                        file,
                                        item.homework.title
                                    )
                                    instruction.remove(instDownload)
                                } catch (e: Exception) {
                                    toast(e.message)
                                } finally {
                                    dismissloading()
                                }
                            }
                        }
                        binding.answerLayout.btnDownload.setOnClickListener {
                            viewmodel.intentUtil.downloadFile(
                                this@HomeworkDetailPage,
                                uri,
                                item.homework.file_student_name,
                                "jawaban"
                            )
                        }
                    }

                    // set upload layout
                    if (allowUpload) {
                        binding.materiUploadLayout.btnFile.setOnClickListener {
                            pickFilesFactory = viewmodel.intentUtil.openFilePicker2(
                                this@HomeworkDetailPage,
                                "Pilih File Materi",
                                arrayListOf(MimeType.ALL_FILES)
                            )
//                            viewmodel.intentUtil.openFilePicker(
//                                this@HomeworkDetailPage,
//                                "Upload File Tugas",
//                                listOf(
//                                    Triple("Pdf", arrayOf("pdf"), R.drawable.ic_pdf),
//                                    Triple(
//                                        "Gambar",
//                                        arrayOf("png", "jpg", "jpeg"),
//                                        R.drawable.image_placeholder
//                                    )
//                                )
//                            )
                        }

                        viewmodel.tugasFile.observe(this@HomeworkDetailPage, {
                            if (!it.isNullOrEmpty())
                                instruction.remove(instUpload)
                        })

                        var oldLink = ""
                        var timer: Timer? = null
                        binding.inputLink.doAfterTextChanged {
                            timer?.cancel()
                            timer = Timer()

                            timer?.schedule(object : TimerTask() {
                                override fun run() {
                                    if (binding.inputLink.text.toString() != oldLink) {
                                        oldLink = binding.inputLink.text.toString()
                                        Handler(Looper.getMainLooper()).post {
                                            binding.loadingLink.visibility = View.VISIBLE
                                            binding.loadingLinkLabel.visibility = View.VISIBLE
                                        }

                                        lifecycleScope.launchWhenCreated {
                                            binding.uploadLinkPreview.loadUrl(
                                                viewmodel.apiService,
                                                binding.inputLink.text.toString()
                                            ) { success, meta ->
                                                if (success) {
                                                    Timber.e("preview link success, try to remove instupload")
                                                    instruction.remove(instUpload)
                                                    Timber.e("inst: $instruction")

                                                    Handler(Looper.getMainLooper()).post {
                                                        binding.loadingLink.visibility = View.GONE
                                                        binding.loadingLinkLabel.visibility =
                                                            View.GONE
                                                    }
                                                } else {
                                                    Handler(Looper.getMainLooper()).post {
                                                        binding.loadingLink.visibility = View.GONE
                                                        binding.loadingLinkLabel.visibility =
                                                            View.GONE
                                                        toast("gagal menampilkan preview url")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }, 1000)
                        }

                        binding.inputLink.visibility = View.VISIBLE
                        binding.linkDivider.visibility = View.VISIBLE
                        binding.linkLabel.visibility = View.VISIBLE
                    } else {
                        binding.inputLink.visibility = View.GONE
                        binding.linkDivider.visibility = View.GONE
                        binding.linkLabel.visibility = View.GONE
                    }

                    // set button finish
                    if (isStudent) {
                        // set allow finish homework
                        if (item.homework.downloded > 0) {
                            instruction.add(instDownload)
                        }
                        if (item.homework.uploaded > 0) {
                            instruction.add(instUpload)
                        }

                        binding.btnUpload.setOnClickListener {
                            lifecycleScope.launch {
                                if (instruction.isEmpty()) {
                                    loading("sedang mengumpulkan tugas")
                                    val res = viewmodel.uploadHomework(item.homework.id)
                                    dismissloading()
                                    if (res)
                                        finish()
                                } else {
                                    toast(instruction.joinToString(". ") { it.second })
                                }
                            }
                        }
                    }
                    binding.executePendingBindings()
                }
            } else {
                alert(msg = "Halaman tidak tersedia", okClick = {
                    finish()
                })
            }
            dismissloading()
        }

        viewmodel.errorString.observe(this, Observer(this::toast))
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            IntentUtil.RC_PDF_PICKER -> {
                val uri =
                    data?.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_DOCS)
                if (!uri.isNullOrEmpty()) {
                    ContentUriUtils.getFilePath(this, uri.first())?.let {
                        viewmodel.tugasFile.postValue(it)
                        val file = File(it)
                        binding.materiUploadLayout.icPdf.visibility = View.VISIBLE
                        binding.materiUploadLayout.uploadFileInfo.text =
                            "${file.name} (ukuran: ${
                                viewmodel.fileUtils.getStringSizeLengthFile(
                                    file.length()
                                )
                            })"
                    }
                }
            }
            IntentUtil.RC_FILE_PICKER -> {
                pickFilesFactory?.handleActivityResult(requestCode, resultCode, data, object :
                    PickFilesStatusCallback {
                    override fun onFilePicked(fileData: java.util.ArrayList<FileData>) {
                        viewmodel.tugasFile.postValue(fileData[0].filePath)

                        val mimeType = fileData[0].mimeType.orEmpty()
                        if (mimeType.contains("pdf", true)) {
                            binding.materiUploadLayout.icPdf.visibility = View.VISIBLE
                            binding.materiUploadLayout.icPdf.setImageResource(R.drawable.ic_pdf)
                        } else if (mimeType.contains("image", true)) {
                            binding.materiUploadLayout.icPdf.visibility = View.VISIBLE
                            fileData[0].filePath?.let {
                                try {
                                    binding.materiUploadLayout.icPdf.setImageURI(
                                        Uri.fromFile(File(it))
                                    )
                                } catch (e: Exception) {
                                    Timber.e(e)
                                }
                            }
                        }

                        binding.materiUploadLayout.uploadFileInfo.text =
                            "${fileData[0].fileName} (ukuran: ${
                                viewmodel.fileUtils.getStringSizeLengthFile(
                                    fileData[0].fileSize!!.toLong()
                                )
                            })"
                    }

                    override fun onPickFileCanceled() {
                    }

                    override fun onPickFileError(errorModel: ErrorModel) =
                        toast(getString(errorModel.errorMessage))
                })

            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}