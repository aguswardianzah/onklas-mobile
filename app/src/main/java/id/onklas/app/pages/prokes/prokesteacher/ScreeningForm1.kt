package id.onklas.app.pages.prokes.prokesteacher

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.ScreeningForm1Binding
import id.onklas.app.databinding.ScreeningForm2Binding
import id.onklas.app.di.component
import id.onklas.app.pages.BasePage
import id.onklas.app.pages.prokes.ListChoice
import id.onklas.app.pages.prokes.ProkesViewmodel
import id.onklas.app.pages.prokes.prokesstudent.formulir.ChoiceAdapter
import id.onklas.app.pages.prokes.prokesstudent.formulir.SubchoiceAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class ScreeningForm1 : Fragment() {
    private lateinit var binding: ScreeningForm1Binding
    private val viewmodel by activityViewModels<ProkesViewmodel> { component.prokesVmFactory }

    val mainData by lazy { MutableLiveData<List<ListChoice>>() }
    val subchoiceData by lazy { MutableLiveData<List<ListChoice>>() }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return  ScreeningForm1Binding.inflate(inflater,container,false).also { binding = it }.root
    }

    override fun onResume() {
        super.onResume()

        binding.rvSubchoice.visibility = View.GONE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        lifecycleScope.launch {
            viewmodel.loadChoice("history_of_illness").collectLatest {
                try {
                    var mainList = ArrayList<ListChoice>()
                    it.forEach {
                        mainList.add(ListChoice(it.id, it.text, false,it.key ))
                    }
                    mainData.postValue(mainList)
                } catch (e: Exception) { }
            }
        }
        lifecycleScope.launch {
            viewmodel.loadChoice("feel_indication").collectLatest {
                try {
                    var subChoice = ArrayList<ListChoice>()
                    it.forEach {
                        subChoice.add(ListChoice(it.id, it.text, false,it.key ))
                    }
                    subchoiceData.postValue(subChoice)
                } catch (e: Exception) { }
            }
        }


        mainData.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it?.toMutableList())
        })
        subchoiceData.observe(viewLifecycleOwner, Observer {
            adapter2.submitList(it?.toMutableList())
        })

        binding.rvChoice.apply {
            adapter = this@ScreeningForm1.adapter
        }

        binding.rvSubchoice.apply {
            adapter = this@ScreeningForm1.adapter2
        }

        viewmodel.errorString.observe(viewLifecycleOwner, Observer {
            (requireActivity() as BasePage).toast(it) })


        mainData.observe(viewLifecycleOwner, Observer {
            try {
                val selected = mainData.value?.first { it.isChosen }?.copy()
                binding.showSub = (it?.indexOf(selected) == it?.lastIndex)

                val subitem = ArrayList<ListChoice>()
                subitem.addAll(subchoiceData.value?.toMutableList()!!)
                for (i in subitem?.indices!!) {
                    if (subitem[i].isChosen) {
                        val newItem = subitem[i].copy()
                        val newIschosen = !subitem[i].isChosen
                        subitem[i] = newItem.apply { isChosen = newIschosen }
                    }
                }
                subchoiceData.postValue(subitem)

                if (it?.indexOf(selected) == it?.lastIndex) {
                    //jika di posisi terakhir btn confirm otomatis disable
                    binding.btnConfirm.isEnabled = !(it?.indexOf(selected) == it?.lastIndex)
                } else {
                    binding.btnConfirm.isEnabled = mainData.value?.find { it.isChosen } != null
                }
            } catch (e: Exception) {
            }

        })

        subchoiceData.observe(viewLifecycleOwner, Observer {
            try {
                if(mainData.value?.indexOf(mainData.value?.first { it.isChosen }?.copy()) == mainData.value?.lastIndex){
                    binding.btnConfirm.isEnabled = (it.count { it.isChosen } > 0)
                }

            } catch (e: Exception) {
            }
        })

        binding.btnConfirm.setOnClickListener {
            mainData.value?.first{ it.isChosen }?.choiceText?: ""
            viewmodel.ScreeningMain.postValue(mainData.value?.filter { it.isChosen })
            subchoiceData.value?.filter { it.isChosen }.let { itListChoice1 -> viewmodel.ScreeningChoiceSub.postValue(itListChoice1) }
            findNavController().navigate(R.id.action_global_screening_2)
        }

        viewmodel.sendProkseResponse.observe(viewLifecycleOwner, Observer {
            (requireActivity() as BasePage).finish()
        })
        viewmodel.errorString.observe(viewLifecycleOwner, Observer {
            (requireActivity() as BasePage).toast(it)
        })




    }

    private val adapter by lazy {
        ChoiceAdapter { item, position ->
            mainData.value?.let { currentList ->
                val checklist = ArrayList(currentList)
                try {
                    val active = checklist.first { it.isChosen }.copy()
                    val activeIndex = checklist.indexOf(active)
                    checklist[activeIndex] = active.apply { isChosen = !active.isChosen }
                } catch (e: Exception) {
                }

                val newItem = checklist.first { it.id == item.id }.copy()
                val newIsShow = !newItem.isChosen
                val index = checklist.indexOf(newItem)

                try {
                    if (index > -1) {
                        checklist[index] = newItem.apply { isChosen = newIsShow }
                        mainData.postValue(checklist)
                    }
                } catch (e: Exception) {
                }


            }
        }
    }

    private val adapter2 by lazy {
        SubchoiceAdapter { item, position ->
            subchoiceData.value?.let { currentList ->
                val checklist = ArrayList(currentList)
                var activeCount = 0
                try {
                    activeCount = checklist.count { it.isChosen }
                } catch (e: Exception) {
                }

//                if (activeCount < 2) {
                    val newItem = checklist.first { it.id == item.id }.copy()
                    val newIsShow = !newItem.isChosen
                    val index = checklist.indexOf(newItem)
                    try {
                        if (index > -1) {
                            checklist[index] = newItem.apply { isChosen = newIsShow }
                        }
                    } catch (e: Exception) {
                    }
                subchoiceData.postValue(checklist)


            }
        }
    }

}