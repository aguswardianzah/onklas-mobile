package id.onklas.app.pages.prokes

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.ProkesFormBinding
import id.onklas.app.databinding.ProkesFormOrtuLayBinding
import id.onklas.app.databinding.ProkesFormStudentLayBinding
import id.onklas.app.databinding.ProkesFormTeacherLayBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BasePage
import id.onklas.app.pages.prokes.prokesstudent.formulir.CekPengisianPage
import id.onklas.app.pages.prokes.prokesstudent.formulir.FormulirPage
import id.onklas.app.pages.prokes.prokesteacher.ScreeningClass
import kotlinx.coroutines.launch
import timber.log.Timber

class ProkesForm : Fragment() {
    private lateinit var binding: ProkesFormBinding
    private lateinit var prokesStudent: ProkesFormStudentLayBinding
    private lateinit var prokesTeacher: ProkesFormTeacherLayBinding
    private lateinit var prokesOrtu: ProkesFormOrtuLayBinding
    private val viewmodel by activityViewModels<ProkesViewmodel> { component.prokesVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ProkesFormStudentLayBinding.inflate(inflater, container, false)
            .also { prokesStudent = it }.root
        ProkesFormTeacherLayBinding.inflate(inflater, container, false)
            .also { prokesTeacher = it }.root
        return ProkesFormBinding.inflate(inflater, container, false).also { binding = it }.root
    }

//    override fun onResume() {
//        lifecycleScope.launch {
//            if(viewmodel.pref.getBoolean("is_student")){
//                viewmodel.cekStudentReport()
//                viewmodel.loadCekVaksin()
//                viewmodel.loadCekVaksinTeacher()
//            }else{
//                viewmodel.loadCekVaksinTeacher()
//                viewmodel.loadScreenigStudent()
//            }
//        }
//        super.onResume()
//    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val formTitle =
            Html.fromHtml("Lindungi <strong>KELUARGA</strong>, Lindungi <strong>SEKOLAH</strong>")
        if (viewmodel.pref.getBoolean("is_student")) {
            binding.formContainer.addView(prokesStudent.root)
            prokesStudent.formTitle.text = formTitle
        } else {
            binding.formContainer.addView(prokesTeacher.root)
            prokesTeacher.formTitle.text = formTitle
        }
        lifecycleScope.launch {
            if (viewmodel.cekStudentReport.value == null) {
                if (viewmodel.pref.getBoolean("is_student")) {
                    viewmodel.cekStudentReport()
                    viewmodel.loadCekVaksin()
                } else {
                    viewmodel.loadCekVaksinTeacher()
                    viewmodel.loadScreenigStudent()
                }
            }
        }

        // prokes student
        prokesStudent.lifecycleOwner = viewLifecycleOwner
        prokesTeacher.lifecycleOwner = viewLifecycleOwner

        prokesStudent.viewholder = viewmodel
        prokesTeacher.viewholder = viewmodel
        viewmodel.cekStudentReport.observe(viewLifecycleOwner, Observer {
            prokesStudent.report = it
            prokesTeacher.report = it
        })

        viewmodel.cekVaksinasi.observe(viewLifecycleOwner, Observer {
            prokesStudent.vaccinated = it.vaccinated
            prokesTeacher.vaccinated = it.vaccinated
        })

        viewmodel.saveVaksinasi.observe(viewLifecycleOwner, Observer {
            prokesStudent.vaccinated = it.data?.check?.vaccinated
            (requireActivity() as BasePage).toast("Status berhasil diperbarui")
            viewmodel.saveVaksinasi.removeObserver { it }
        })

        val swipeRefresh = if (viewmodel.pref.getBoolean("is_student")) {
            prokesStudent.swipeRefresh
        } else {
            prokesTeacher.swipeRefresh
        }

        viewmodel.LoadingShow.observe(viewLifecycleOwner, Observer {
            Timber.e("loading show $it")
            if (it) {
                (requireActivity() as BasePage).loading()
            } else {
                swipeRefresh.isRefreshing = it
                (requireActivity() as BasePage).dismissloading()
            }
        })

        val btnForm = if (viewmodel.pref.getBoolean("is_student")) {
            prokesStudent.btnForm
        } else {
            prokesTeacher.btnForm
        }

        btnForm.setOnClickListener {
            if (viewmodel.pref.getBoolean("is_student")) {
                if (viewmodel.cekStudentReport.value?.message.isNullOrEmpty()) {
                    FormulirPage.open(requireActivity())
                } else {
                    startActivity(Intent(requireContext(), CekPengisianPage::class.java))
                }
            } else {
                startActivity(Intent(requireContext(), ScreeningClass::class.java))
            }
        }

        swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                if (viewmodel.pref.getBoolean("is_student")) {
                    viewmodel.cekStudentReport()
                    viewmodel.loadCekVaksin()
                } else {
                    viewmodel.loadCekVaksinTeacher()
                    viewmodel.loadScreenigStudent()
                }
            }
        }

        val vSwitch = if (viewmodel.pref.getBoolean("is_student")) {
            prokesStudent.vSwitch
        } else {
            prokesTeacher.vSwitch
        }

        vSwitch.setOnClickListener {
            (requireActivity() as BasePage).alertSelectNew(
                "Perubahan Data Vaksin",
                "Anda setuju untuk konfirmasi dan menyimpan perubahan data tentang info vaksinasi?",
                "Konfirmasi",
                {
                    lifecycleScope.launch {
                        if (viewmodel.pref.getBoolean("is_student")) {
                            viewmodel.saveVaksinasi(!prokesStudent.swButton.isChecked)
                            viewmodel.loadCekVaksin()
                        } else {
                            viewmodel.saveVaksinasiTeacher(!prokesTeacher.swButton.isChecked)
                            viewmodel.loadCekVaksinTeacher()
                        }
                    }
                },
                "Tidak jadi"
            )
        }
        // end prokes student


        // prokes teacher
        viewmodel.ScreeningStudent.observe(viewLifecycleOwner, Observer {
            prokesTeacher.screeningCheck = it
        })
    }
}