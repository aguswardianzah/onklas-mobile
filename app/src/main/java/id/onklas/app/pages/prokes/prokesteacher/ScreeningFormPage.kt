package id.onklas.app.pages.prokes.prokesteacher

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.ScreeningClassBinding
import id.onklas.app.databinding.ScreeningFormBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BasePage
import id.onklas.app.pages.prokes.ProkesViewmodel
import kotlinx.coroutines.launch
import timber.log.Timber

class ScreeningFormPage : BasePage() {

    companion object {
        fun open(activity: Activity, studentName: String, StudentId:Int, avatar:String,nis:String,jurusan:String) {
            activity.startActivityForResult(
                    Intent(activity, ScreeningFormPage::class.java)
                            .putExtra("student_name", studentName)
                            .putExtra("student_id", StudentId)
                            .putExtra("avatar", avatar)
                            .putExtra("nis", nis)
                            .putExtra("jurusan", jurusan),
                    213
            )
        }
    }


    private val binding by lazy { ScreeningFormBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<ProkesViewmodel> { component.prokesVmFactory }

    private var StudentName = ""
    private var StudentId = 0
    private var Avatar = ""
    private val isShowSet by lazy { MutableLiveData<Boolean>(false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        StudentName = intent.getStringExtra("student_name").toString()
        StudentId = intent.getIntExtra("student_id",0)
        Avatar = intent.getStringExtra("avatar").toString()

        viewmodel.SelectedStudentId.postValue(StudentId)

        binding.name = StudentName
        binding.avatarUrl = Avatar
        isShowSet.observe(this, Observer {
            binding.isShow = it
        })


        binding.nis  = intent.getStringExtra("nis").toString()
        binding.jurusan= intent.getStringExtra("jurusan").toString()

        binding.btnShow.setOnClickListener {
            isShowSet.postValue(!isShowSet.value!!)
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Lakukan pemeriksaan"
            binding.toolbar.title = "Lakukan pemeriksaan"
        }

//        viewmodel.sendScreeningResponse.observe(this, Observer {
//            val returnIntent = Intent()
//            setResult(RESULT_OK, returnIntent)
//            finish()
//        })


        viewmodel.LoadingShow.observe(this, Observer {
            Timber.e("loading show $it")
            if (it) {
                loading()
            } else {
                dismissloading()
            }
        })

        viewmodel.errorString.observe(this, Observer {
            toast(it)
        })



    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 214 && resultCode == RESULT_OK) {
            val returnIntent = Intent()
            setResult(RESULT_OK, returnIntent)
            finish()
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }


}