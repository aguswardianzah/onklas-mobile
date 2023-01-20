package id.onklas.app.pages.createpost

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.size.AspectRatio
import com.otaliastudios.cameraview.size.SizeSelector
import com.otaliastudios.cameraview.size.SizeSelectors
import id.onklas.app.R
import id.onklas.app.databinding.CameraPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class CameraPage : Privatepage() {

    private val binding by lazy { CameraPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<CameraViewModel> { component.cameraVmFactory }
    private val isVideo by lazy { MutableLiveData<Boolean>(false) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private val file by lazy {
        File(
            viewmodel.fileUtils.getExternalDir(this),
            "Capture"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.BLACK

        setContentView(binding.root)

        binding.camera.setLifecycleOwner(this)
        binding.counter.visibility = View.GONE

        binding.camera.setPictureSize(
            SizeSelectors.or()
        )

        binding.camera.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                result.toBitmap (760, 760) {
                    it?.let { it ->
                        lifecycleScope.launch {
                            loading(msg = "memproses gambar ...")
                            if (!file.exists())
                                file.createNewFile()

                            val bitmap = viewmodel.fileUtils.cropToSquare(it)
                            if (viewmodel.fileUtils.bitmapToFile(bitmap, file)) {
                                binding.camera.close()
                            } else {
                                toast("gagal memproses gambar")
                            }
                            dismissloading()
                            setResult(
                                RESULT_OK,
                                Intent().putExtra("fileUri", file.toUri().toString())
                            )
                            finish()
                        }
                    }
                }
            }

            override fun onVideoRecordingStart() {
                countDownTimer.start()
                binding.counter.visibility = View.VISIBLE
            }

            override fun onVideoRecordingEnd() {
                countDownTimer.cancel()
                binding.counter.visibility = View.GONE
            }

            override fun onVideoTaken(result: VideoResult) {
                binding.camera.close()
            }
        })

        isVideo.observe(this, Observer {
            if (it) {
                binding.camera.mode = Mode.VIDEO
                binding.video.setTextColor(colorPrimary)
                binding.picture.setTextColor(Color.WHITE)
            } else {
                binding.camera.mode = Mode.PICTURE
                binding.video.setTextColor(Color.WHITE)
                binding.picture.setTextColor(colorPrimary)
            }
        })

        binding.menuClose.setOnClickListener { finish() }
        binding.menuFlash.setOnClickListener {
            binding.camera.flash = if (binding.camera.flash == Flash.OFF) Flash.ON else Flash.OFF
        }
        binding.menuRotate.setOnClickListener {
            Timber.e("rotate camera")
            binding.camera.toggleFacing()
        }

        binding.video.setOnClickListener { isVideo.postValue(true) }
        binding.picture.setOnClickListener { isVideo.postValue(false) }

        binding.capture.setOnClickListener {
            if (isVideo.value == true) {
                if (!file.exists())
                    file.createNewFile()
                binding.camera.takeVideo(file)
            } else {
                binding.camera.takePicture()
            }
        }
    }

    private val countDownTimer by lazy {
        object : CountDownTimer(60 * 1000, 1000) {
            override fun onFinish() {
                binding.camera.stopVideo()
            }

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                binding.counter.text = "0:${60 - (millisUntilFinished / 1000)}"
            }
        }
    }
}