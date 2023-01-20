package id.onklas.app.pages.tryout

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.work.*
import id.onklas.app.R
import id.onklas.app.databinding.TryOutPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.akm.AkmStatus
import id.onklas.app.worker.AkmUploader
import java.util.concurrent.TimeUnit

class TryOutPage : Privatepage() {

    private val binding by lazy { TryOutPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<TryOutViewModel> { component.tryoutVmFactory }
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
    private val colorWhite by lazy {
        ResourcesCompat.getColor(
            resources,
            R.color.white,
            null
        )
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.title = "Quiz"
            binding.toolbar.setNavigationOnClickListener { finish() }
        }
        viewmodel.errorString.observe(this, Observer(this::toast))

        binding.btnJadwal.setOnClickListener {
            navController.navigate(R.id.action_global_tryoutschedulepage)
            binding.btnJadwal.apply {
                backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
                setTextColor(colorWhite)
            }

            binding.btnNilai.apply {
                backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
                setStrokeColorResource(R.color.gray)
                setTextColor(colorGray)
            }
        }


        binding.btnNilai.setOnClickListener {
            navController.navigate(R.id.action_global_tryoutscorepage)
            binding.btnNilai.apply {
                backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
                setTextColor(colorWhite)
            }

            binding.btnJadwal.apply {
                backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
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
            viewmodel.loadTryout()
            viewmodel.loadTryoutScored()

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