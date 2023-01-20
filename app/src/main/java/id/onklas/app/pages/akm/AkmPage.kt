package id.onklas.app.pages.akm

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.work.*
import id.onklas.app.R
import id.onklas.app.databinding.AkmPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.worker.AkmUploader
import java.util.concurrent.TimeUnit

class AkmPage : Privatepage() {

    private val binding by lazy { AkmPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<AkmViewModel> { component.akmVmFactory }
    private val isSchoolScope by lazy { intent.getBooleanExtra("isSchoolScope", false) }
    private val navController by lazy { findNavController(R.id.page_container) }
    private val colorPrimary by lazy {
        ResourcesCompat.getColor(
            resources,
            R.color.colorPrimary,
            null
        )
    }
    private val colorGray by lazy {
        ResourcesCompat.getColor(
            resources,
            R.color.gray,
            null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.title = if (isSchoolScope) "Ujian" else "AKM"
            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        viewmodel.errorString.observe(this, Observer(this::toast))

        binding.btnIkuti.text = if (isSchoolScope) "Jadwal" else "Ikuti AKM"

        binding.btnIkuti.setOnClickListener {
            navController.navigate(R.id.action_global_akmListPage)
            binding.btnIkuti.apply {
                setStrokeColorResource(R.color.colorPrimary)
                setTextColor(colorPrimary)
            }

            binding.btnNilai.apply {
                setStrokeColorResource(R.color.gray)
                setTextColor(colorGray)
            }
        }

        binding.btnNilai.setOnClickListener {
            navController.navigate(R.id.action_global_akmScorePage)
            binding.btnNilai.apply {
                setStrokeColorResource(R.color.colorPrimary)
                setTextColor(colorPrimary)
            }

            binding.btnIkuti.apply {
                setStrokeColorResource(R.color.gray)
                setTextColor(colorGray)
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    fun openScoreList() {
        binding.btnNilai.performClick()
    }

    init {
        lifecycleScope.launchWhenCreated {
            viewmodel.isSchoolScope = isSchoolScope
            if (isSchoolScope) {
                viewmodel.loadUjianSchool()
                viewmodel.loadUjianSchoolScored()
            } else {
                viewmodel.loadAkm()
                viewmodel.loadAkmScored()
            }

            // try to post jawaban ujian
            viewmodel.db.akm().getUnfinishedUjian().forEach { akmTable ->
                if (akmTable.status < AkmStatus.AKM_STATUS_UPLOADED) {
                    // insert worker to upload jawaban
                    val timeLeft = akmTable.date_end.time - System.currentTimeMillis()

                    val workRequest = OneTimeWorkRequestBuilder<AkmUploader>()
                        .setInputData(
                            workDataOf("id" to akmTable.id)
                        )
                        .setInitialDelay(timeLeft, TimeUnit.MILLISECONDS)
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .setBackoffCriteria(
                            BackoffPolicy.LINEAR,
                            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS
                        )
                        .addTag("akm_uploader")
                        .build()

                    WorkManager.getInstance(applicationContext)
                        .enqueueUniqueWork(
                            "akm_uploader_${akmTable.id}",
                            ExistingWorkPolicy.REPLACE,
                            workRequest
                        )
                }
            }
        }
    }
}