package id.onklas.app.pages.prokes.prokesteacher

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.ScreeningForm2Binding
import id.onklas.app.di.component
import id.onklas.app.pages.BasePage
import id.onklas.app.pages.prokes.ProkesViewmodel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat


class ScreeningForm2 : Fragment() {
    private lateinit var binding: ScreeningForm2Binding
    private val viewmodel by activityViewModels<ProkesViewmodel> { component.prokesVmFactory }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return ScreeningForm2Binding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_global_screening_1)
        }

        binding.txtSuhu.doAfterTextChanged {
            val text  = binding.txtSuhu.text.toString()
            viewmodel.suhu.postValue(text)
//            if (text!=""){
//                val filter = text.toString().replace(",","")
////                Timber.e("filter $filter")
//                val formatter: NumberFormat = DecimalFormat("##,#0")
//                formatter.roundingMode = RoundingMode.CEILING
//                val formated = formatter.format(filter.toString().toInt())
////                Timber.e("formated $formated")
//                if(formated!=text){
//                    binding.txtSuhu.setText(formated)
//                    binding.txtSuhu.setSelection(formated.length)
//                }
//            }
        }

        viewmodel.suhu.observe(viewLifecycleOwner, Observer {
            try {
                    binding.btnConfirm.isEnabled = (!(it.toDouble()>50.0) && !(it.toDouble()<30.0))
            } catch (e: Exception) {
            }
        })

        binding.btnConfirm.setOnClickListener {
            Timber.e("sub choice ${ viewmodel.ScreeningChoiceSub.value?.filter { it.isChosen }?.map { it.choiceText }!!}")
            Timber.e("sub choice key  ${ viewmodel.ScreeningChoiceSub.value?.filter { it.isChosen }?.map { it.key }!!}")
            viewmodel.ScreeningChoiceSub.value?.filter { it.isChosen }?.map { it.key }?.let { it1 ->
                CekScreeningPage.open(
                        requireActivity(),
                        "",
                        viewmodel.SelectedStudentId.value?:0,
                        true,
                        viewmodel.suhu.value!!,
                        viewmodel.ScreeningMain.value?.first{it.isChosen}?.key ?:"",
                        viewmodel.ScreeningMain.value?.first{it.isChosen}?.choiceText ?:"",
                        it1,
                        viewmodel.ScreeningChoiceSub.value?.filter { it.isChosen }?.map { it.choiceText }!!,
                        )
            }
        }
    }


}