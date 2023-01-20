package id.onklas.app.pages.theoryteacher

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
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
import id.onklas.app.databinding.UploadMateriPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.NoFilterArrayAdapter
import id.onklas.app.utils.hideKeyboard
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*

class UploadMateriPage : Privatepage() {

    private val binding by lazy { UploadMateriPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<UploadMateriViewmodel> { component.uploadMateriVmFactory }
    private val materiId by lazy { intent.getIntExtra("materiId", 0) }
    private var pickFilesFactory: IPickFilesFactory? = null
    private val colorGray by lazy { ResourcesCompat.getColor(resources, R.color.ltgray, null) }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { supportFinishAfterTransition() }
        }

        binding.lifecycleOwner = this

        viewmodel.isLoading.observe(this) {
            if (it)
                loading(msg = "mohon tunggu")
            else
                dismissloading()
        }

        viewmodel.apply {
            binding.viewmodel = this
            materiTitle.observe(this@UploadMateriPage) {
                binding.titleCounter.text = "${it.length}/500"
            }
            materiDesc.observe(this@UploadMateriPage) {
                binding.descCounter.text = "${it.length}/5000"
            }
            materiTitle.observe(this@UploadMateriPage, allowPostObserver)
            subjectId.observe(this@UploadMateriPage, allowPostObserver)
            grade.observe(this@UploadMateriPage, allowPostObserver)
            materiFile.observe(this@UploadMateriPage, allowPostObserver)

            listSubject.observe(this@UploadMateriPage, {
                binding.inputMapel.setAdapter(
                    NoFilterArrayAdapter(
                        this@UploadMateriPage,
                        it.map { it.name })
                )
            })

            listGrade.observe(this@UploadMateriPage, {
                binding.inputGrade.setAdapter(
                    NoFilterArrayAdapter(
                        this@UploadMateriPage,
                        it.map { it.name })
                )
            })

            listClass.observe(this@UploadMateriPage, {
                binding.inputKelas.setAdapter(
                    NoFilterArrayAdapter(
                        this@UploadMateriPage,
                        it.map { it.name })
                )
            })


            listMajor.observe(this@UploadMateriPage, {
                binding.inputJurusan.setAdapter(
                    NoFilterArrayAdapter(
                        this@UploadMateriPage,
                        it.map { it.name })
                )
            })

            allowUpload.observe(this@UploadMateriPage, {
                binding.btnPost.isEnabled = it
            })

            errorString.observe(this@UploadMateriPage, {
                if (!it.isNullOrEmpty()) toast(it)
            })
        }

        binding.gradeLabel.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.kelasLabel.isChecked = false
                binding.jurusanLabel.isChecked = false
                viewmodel.showSelected.value = "grade"

                binding.inputKelas.isEnabled = false
                binding.inputJurusan.isEnabled = false
                binding.inputGrade.isEnabled = true

                binding.inputKelasLayout.setBackgroundColor(colorGray)
                binding.inputJurusanLayout.setBackgroundColor(colorGray)
                binding.inputGradeLayout.setBackgroundColor(Color.TRANSPARENT)

                viewmodel.majorId.postValue(0)
                viewmodel.classId.postValue(0)
                binding.executePendingBindings()
            }
        }
        binding.kelasLabel.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.gradeLabel.isChecked = false
                binding.jurusanLabel.isChecked = false
                viewmodel.showSelected.value = "class"

                binding.inputGrade.isEnabled = false
                binding.inputJurusan.isEnabled = false
                binding.inputKelas.isEnabled = true

                binding.inputKelasLayout.setBackgroundColor(Color.TRANSPARENT)
                binding.inputJurusanLayout.setBackgroundColor(colorGray)
                binding.inputGradeLayout.setBackgroundColor(colorGray)

                viewmodel.grade.postValue(0)
                viewmodel.majorId.postValue(0)
                binding.executePendingBindings()
            }
        }
        binding.jurusanLabel.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.kelasLabel.isChecked = false
                binding.gradeLabel.isChecked = false
                viewmodel.showSelected.value = "major"

                binding.inputGrade.isEnabled = false
                binding.inputKelas.isEnabled = false
                binding.inputJurusan.isEnabled = true

                binding.inputKelasLayout.setBackgroundColor(colorGray)
                binding.inputJurusanLayout.setBackgroundColor(Color.TRANSPARENT)
                binding.inputGradeLayout.setBackgroundColor(colorGray)

                viewmodel.grade.postValue(0)
                viewmodel.classId.postValue(0)
                binding.executePendingBindings()
            }
        }

        viewmodel.showSelected.observe(this) {
            when (it) {
                "grade" -> binding.gradeLabel.isChecked = true
                "class" -> binding.kelasLabel.isChecked = true
                "major" -> binding.jurusanLabel.isChecked = true
            }
        }

        binding.inputContent.onFocusChangeListener = focusKeyboardHide

        binding.materiUploadLayout.btnFile.setOnClickListener {
//            viewmodel.intentUtil.openPdfPicker(this, "Pilih File Materi")
            pickFilesFactory = viewmodel.intentUtil.openFilePicker2(
                this,
                "Pilih File Materi",
                arrayListOf(MimeType.ALL_FILES)
            )
        }

        var oldLink = ""
        binding.inputLink.doAfterTextChanged {
            timer?.cancel()
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    if (binding.inputLink.text.toString() != oldLink) {
                        oldLink = binding.inputLink.text.toString()

                        lifecycleScope.launchWhenCreated {
                            binding.linkPreview.loadUrl(
                                viewmodel.apiService,
                                binding.inputLink.text.toString()
                            ) { success, meta ->
                                viewmodel.linkAttached = success
                                viewmodel.allowUpload.postValue(
                                    !viewmodel.materiTitle.value.isNullOrEmpty() &&
                                            viewmodel.subjectId.value ?: 0 > 0 &&
                                            (!viewmodel.materiFile.value.isNullOrEmpty() || !viewmodel.materiLink.value.isNullOrEmpty())
                                )
                                if (!success) {
                                    Handler(Looper.getMainLooper()).post {
                                        toast("gagal menampilkan preview url")
                                    }
                                }
                            }
                        }
                    }
                }
            }, 1000)
        }

        binding.btnPost.setOnClickListener {
            lifecycleScope.launch {
                loading(msg = "sedang mengupload materi")
                val result = viewmodel.createMateri()
                dismissloading()
                if (result) {
                    setResult(RESULT_OK)
                    supportFinishAfterTransition()
                }
            }
        }

        Timber.e("set materi id: $materiId")
        if (materiId > 0) {
            viewmodel.fileInfo.observe(this) {
                binding.materiUploadLayout.uploadFileInfo.text = it
            }
            viewmodel.setMateri(materiId)
        }

        binding.executePendingBindings()
    }

    private var timer: Timer? = null

    private val focusKeyboardHide by lazy {
        View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) hideKeyboard(v)
        }
    }

    private val allowPostObserver by lazy {
        Observer { it: Any ->
            viewmodel.allowUpload.postValue(
                !viewmodel.materiTitle.value.isNullOrEmpty() &&
                        viewmodel.subjectId.value ?: 0 > 0 &&
//                        viewmodel.grade.value ?: 0 > 0 &&
                        (!viewmodel.materiFile.value.isNullOrEmpty() || !viewmodel.materiLink.value.isNullOrEmpty())
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            IntentUtil.RC_PDF_PICKER -> {
                val uri =
                    data?.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_DOCS)
                if (!uri.isNullOrEmpty()) {
                    ContentUriUtils.getFilePath(this, uri.first())?.let {
                        Timber.d("contentUriUtils: $it")
                        viewmodel.materiFile.postValue(it)
                        val file = File(it)
                        binding.materiUploadLayout.icPdf.visibility = View.VISIBLE
                        binding.materiUploadLayout.uploadFileInfo.text =
                            "${file.nameWithoutExtension} (ukuran: ${
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
                    override fun onFilePicked(fileData: ArrayList<FileData>) {
                        viewmodel.materiFile.postValue(fileData[0].filePath)

                        val mimeType = fileData[0].mimeType.orEmpty()
                        if (mimeType.contains("pdf", true)) {
                            binding.materiUploadLayout.icPdf.visibility = View.VISIBLE
                            binding.materiUploadLayout.icPdf.setImageResource(R.drawable.ic_pdf)
                        } else if (mimeType.contains("image", true)) {
                            binding.materiUploadLayout.icPdf.visibility = View.VISIBLE
                            fileData[0].filePath?.let {
                                try {
                                    binding.materiUploadLayout.icPdf.setImageURI(
                                        Uri.fromFile(
                                            File(
                                                it
                                            )
                                        )
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