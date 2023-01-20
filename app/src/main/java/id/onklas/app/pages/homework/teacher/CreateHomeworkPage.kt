package id.onklas.app.pages.homework.teacher

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.models.ErrorModel
import com.linkdev.filepicker.models.FileData
import com.linkdev.filepicker.models.MimeType
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import id.onklas.app.R
import id.onklas.app.databinding.CreateHomeworkPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.homework.HomeWorkViewModel
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.NoFilterArrayAdapter
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*

class CreateHomeworkPage : Privatepage() {

    private val binding by lazy { CreateHomeworkPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<CreateHomeworkVm> { component.createHomeworkVmFactory }
    private val homeworkVm by viewModels<HomeWorkViewModel> { component.homeworkVmFactory }
    private val homeworkId by lazy { intent.getIntExtra("homeworkId", 0) }
    private var editable: Boolean = true
    private val colorPrimary by lazy {
        ResourcesCompat.getColor(
            resources,
            R.color.colorPrimary,
            null
        )
    }
    private var pickFilesFactory: IPickFilesFactory? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_option, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.m_option)?.isVisible = !editable
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        AlertDialog.Builder(this, R.style.DialogTheme)
            .setItems(arrayOf("Edit Tugas", "Hapus Tugas")) { dialog, which ->
                dialog.dismiss()
                if (which == 0) {
                    editable = true
                    invalidateOptionsMenu()
                    binding.toolbar.title = "Edit Tugas"
                    binding.editable = editable
                    binding.executePendingBindings()

                    binding.materiUploadLayout.btnFile.isVisible = editable
                    binding.materiUploadLayout.executePendingBindings()
                } else
                    MaterialAlertDialogBuilder(this, R.style.DialogTheme)
                        .setMessage("Anda yakin akan menghapus tugas?")
                        .setPositiveButton("Hapus") { dialog1, _ ->
                            dialog1.dismiss()
                            lifecycleScope.launch {
                                loading(msg = "menghapus tugas")
                                homeworkVm.deleteHomework(homeworkId)
                                dismissloading()
                                finish()
                            }
                        }
                        .setNeutralButton("Batal") { dialog1, _ -> dialog1.dismiss() }
                        .show()
            }
            .show()

        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editable = intent.getBooleanExtra("editable", true)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            if (homeworkId > 0)
                binding.toolbar.title = if (editable) "Edit Tugas" else "Detail Tugas"
            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.lifecycleOwner = this
        binding.editable = editable

        viewmodel.isLoading.observe(this) {
            if (it)
                loading(msg = "mohon tunggu")
            else
                dismissloading()
        }

        viewmodel.apply {
            binding.viewmodel = this
//            listMapel.observe(this@CreateHomeworkPage, {
//                binding.inputMapel.setAdapter(
//                    NoFilterArrayAdapter(
//                        this@CreateHomeworkPage,
//                        R.layout.text_item,
//                        it.map { it.name })
//                )
//            })

            listSchedule.observe(this@CreateHomeworkPage) {
                binding.inputMapel.setAdapter(
                    NoFilterArrayAdapter(
                        this@CreateHomeworkPage,
                        it.map {
                            "[${it.time_plot.start_at} - ${it.time_plot.end_at}] ${it.subject.name}"
                        })
                )
            }

            listClass.observe(this@CreateHomeworkPage, {
                binding.inputKelas.setAdapter(
                    NoFilterArrayAdapter(
                        this@CreateHomeworkPage,
                        it.map { it.name })
                )
                binding.inputKelas.setText(it.firstOrNull()?.name, false)
            })

            listDay.observe(this@CreateHomeworkPage, {
                binding.inputDay.setAdapter(
                    NoFilterArrayAdapter(
                        this@CreateHomeworkPage,
                        it.map { it.key })
                )
            })

            classId.observe(this@CreateHomeworkPage) {
                viewmodel.fetchSchedule()
                binding.inputDay.isEnabled = editable
                binding.inputDayLayout.setBackgroundColor(Color.TRANSPARENT)
            }

            classDay.observe(this@CreateHomeworkPage) {
                viewmodel.fetchSchedule()
                binding.inputMapel.isEnabled = editable
                binding.inputMapelLayout.setBackgroundColor(Color.TRANSPARENT)
            }

            scheduleId.observe(this@CreateHomeworkPage) { sId ->
                viewmodel.listSchedule.value?.firstOrNull { it.id == sId }?.let {
                    viewmodel.mapel.postValue(it.subject.id)
                }
            }

            allowCreate.observe(this@CreateHomeworkPage, Observer(binding.btnPost::setEnabled))

            name.observe(this@CreateHomeworkPage, allowPostObserver)
            name.observe(this@CreateHomeworkPage, Observer {
                binding.titleCounter.text = "${if (it.isNullOrEmpty()) "0" else it.length}/500"
            })

//            mapel.observe(this@CreateHomeworkPage, allowPostObserver)
//            classId.observe(this@CreateHomeworkPage, allowPostObserver)
            file.observe(this@CreateHomeworkPage, allowPostObserver)
            file.observe(this@CreateHomeworkPage, {
                binding.checkStudentDownload.visibility =
                    if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
            })
            uploadLink.observe(this@CreateHomeworkPage, {
                binding.checkStudentDownload.visibility =
                    if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
            })

            uploadFile.observe(this@CreateHomeworkPage, allowPostObserver)
            uploadFile.observe(this@CreateHomeworkPage, {
                val visibility = if (it) View.VISIBLE else View.GONE
                binding.fileInfo.visibility = visibility
                binding.materiUploadLayout.root.visibility = visibility

                binding.linkLabel.visibility = visibility
                binding.inputLink.visibility = visibility
                binding.linkDivider.visibility = visibility
                binding.linkPreview.visibility = visibility
            })

            errorString.observe(this@CreateHomeworkPage, Observer(this@CreateHomeworkPage::toast))
        }

        binding.inputDuedate.setOnClickListener {

            val calendar = Calendar.getInstance()
            DatePickerDialog.newInstance { view, year, monthOfYear, dayOfMonth ->
                view.dismiss()
                TimePickerDialog.newInstance({ view, hourOfDay, minute, second ->
                    view.dismiss()
                    calendar.set(year, monthOfYear, dayOfMonth, hourOfDay, minute, second)

                    val date = viewmodel.dateFormat.format(calendar.time)
                    Timber.e("data: $date")

                    binding.inputDuedate.setText(date)
                    viewmodel.expired.postValue(date)
                }, true).apply {
                    accentColor = colorPrimary
                    title = "Batas pengumpulan"
                    show(supportFragmentManager, "time_picker")
                }
            }
                .apply {
                    accentColor = colorPrimary
                    minDate = calendar
                    title = "Batas pengumpulan"
                    show(supportFragmentManager, "date_picker")
                }

//            MaterialDatePicker.Builder.datePicker()
//                .setTheme(R.style.AppCalendar)
//                .setTitleText("Batas pengumpulan tugas")
//                .setSelection(System.currentTimeMillis())
//                .setCalendarConstraints(
//                    CalendarConstraints.Builder()
//                        .setStart(System.currentTimeMillis())
//                        .build()
//                )
//                .build()
//                .apply {
//                    addOnPositiveButtonClickListener {
//                        val calendar = Calendar.getInstance().apply { timeInMillis = it }
//
//                        MaterialTimePicker.Builder()
//                            .setTimeFormat(TimeFormat.CLOCK_24H)
//                            .setHour(0)
//                            .setMinute(0)
//                            .setTitleText("Batas pengumpulan tugas")
//                            .build()
//                            .apply {
//                                show(supportFragmentManager, "time_picker")
//                                addOnPositiveButtonClickListener {
//                                    calendar.set(Calendar.HOUR, hour)
//                                    calendar.set(Calendar.MINUTE, minute)
//
//                                    val date = viewmodel.dateFormat.format(calendar.time)
//                                    Timber.e("data: $date")
//
//                                    binding.inputDuedate.setText(date)
//                                    viewmodel.expired.postValue(date)
//                                }
//                            }
//                    }
//                }
//                .show(supportFragmentManager, "date_picker")
        }

        binding.materiUploadLayout.btnFile.isVisible = editable
        binding.materiUploadLayout.btnFile.setOnClickListener {
            pickFilesFactory = viewmodel.intentUtil.openFilePicker2(
                this,
                "Pilih File Tugas",
                arrayListOf(MimeType.ALL_FILES)
            )
        }

        var oldLink = ""
        var timer: Timer? = null
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
                                if (success) {
                                    viewmodel.allowCreate.postValue(
                                        !viewmodel.name.value.isNullOrEmpty() &&
                                                viewmodel.mapel.value ?: 0 > 0 &&
                                                viewmodel.classId.value ?: 0 > 0 &&
                                                !viewmodel.classDay.value.isNullOrEmpty() &&
                                                viewmodel.scheduleId.value ?: 0 > 0
                                    )
                                } else {
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
                loading(msg = "sedang membuat tugas")
                val res = viewmodel.create()
                dismissloading()
                if (res) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }

        if (homeworkId > 0) {
            viewmodel.fileInfo.observe(this) {
                binding.materiUploadLayout.uploadFileInfo.text = it
            }
            viewmodel.setHomework(homeworkId)
        }

        binding.executePendingBindings()
    }

    private val allowPostObserver by lazy {
        Observer { it: Any ->
            viewmodel.allowCreate.postValue(
                !viewmodel.name.value.isNullOrEmpty() &&
                        viewmodel.mapel.value ?: 0 > 0 &&
                        viewmodel.classId.value ?: 0 > 0 &&
                        !viewmodel.classDay.value.isNullOrEmpty() &&
                        viewmodel.scheduleId.value ?: 0 > 0
            )
            if (viewmodel.uploadFile.value == true)
                viewmodel.allowCreate.postValue(!viewmodel.file.value.isNullOrEmpty() || !viewmodel.uploadLink.value.isNullOrEmpty())
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
                        viewmodel.file.postValue(it)
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
                    override fun onFilePicked(fileData: ArrayList<FileData>) {
                        fileData.firstOrNull()?.let {
                            viewmodel.file.postValue(it.filePath)
                            binding.materiUploadLayout.icPdf.visibility = View.VISIBLE
                            binding.materiUploadLayout.uploadFileInfo.text =
                                "${it.file?.name} (ukuran: ${
                                    viewmodel.fileUtils.getStringSizeLengthFile(
                                        it.file?.length() ?: 0
                                    )
                                })"
                        }
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