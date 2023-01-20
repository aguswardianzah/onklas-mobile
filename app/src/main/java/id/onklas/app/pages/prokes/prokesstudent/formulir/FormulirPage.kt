package id.onklas.app.pages.prokes.prokesstudent.formulir

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.FormulirPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BasePage
import id.onklas.app.pages.prokes.ListChoice
import id.onklas.app.pages.prokes.ProkesViewmodel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class FormulirPage : BasePage() {
    companion object {
        fun open(activity: Activity) {
            activity.startActivityForResult(
                    Intent(activity, FormulirPage::class.java), 223
            )
        }
    }

    private val binding by lazy { FormulirPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<ProkesViewmodel> { component.prokesVmFactory }

    val mainData by lazy { MutableLiveData<List<ListChoice>>() }
    val subchoiceData by lazy { MutableLiveData<List<ListChoice>>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
        }


        binding.rvChoice.apply {
            adapter = this@FormulirPage.adapter
        }

        binding.rvSubchoice.apply {
            adapter = this@FormulirPage.adapter2
        }

        lifecycleScope.launch {
            viewmodel.loadFormProkesStudent()
        }

        lifecycleScope.launch {
            viewmodel.loadChoice("way_of_travel").collectLatest {
                try {
                    var mainList = ArrayList<ListChoice>()
                    it.forEach {
                        mainList.add(ListChoice(it.id, it.text, false, it.key))
                    }
                    mainData.postValue(mainList)
                } catch (e: Exception) {
                }
            }
        }
        lifecycleScope.launch {
            viewmodel.loadChoice("public_transportion_choice").collectLatest {
                try {
                    var subChoice = ArrayList<ListChoice>()
                    it.forEach {
                        subChoice.add(ListChoice(it.id, it.text, false, it.key))
                    }
                    subchoiceData.postValue(subChoice)
                } catch (e: Exception) {
                }
            }
        }

        mainData.observe(this, Observer {
            adapter.submitList(it?.toMutableList())
        })
        subchoiceData.observe(this, Observer {
            adapter2.submitList(it?.toMutableList())
        })

        viewmodel.errorString.observe(this, Observer { toast(it) })


        mainData.observe(this, Observer {
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

        subchoiceData.observe(this, Observer {
            try {
                if (mainData.value?.indexOf(mainData.value?.first { it.isChosen }?.copy()) == mainData.value?.lastIndex) {
                    binding.btnConfirm.isEnabled = (it.count { it.isChosen } > 0)
                }

            } catch (e: Exception) {
            }
        })

        binding.btnConfirm.setOnClickListener {
            Timber.e("way of travel text ${mainData.value?.first { it.isChosen }?.choiceText ?: ""}")
            subchoiceData.value?.filter { it.isChosen }?.map { it.choiceText }?.let { it1 ->
                CekPengisianPage.open(
                        this,
                        true,
                        mainData.value?.first { it.isChosen }?.choiceText ?: "",
                        mainData.value?.first { it.isChosen }?.key ?: "",
                        it1,
                        subchoiceData.value?.filter { it.isChosen }?.map { it.key }!!
                )
            }
        }

        viewmodel.sendProkseResponse.observe(this, Observer {
            finish()
        })
        viewmodel.errorString.observe(this, Observer {
            toast(it)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.e("request code $requestCode")
        if (requestCode == 224 && resultCode == RESULT_OK) {
            val returnIntent = Intent()
            returnIntent.putExtra("code",223)
            setResult(RESULT_OK, returnIntent)
            finish()
        } else
            super.onActivityResult(requestCode, resultCode, data)
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

                if (activeCount < 2) {
                    val newItem = checklist.first { it.id == item.id }.copy()
                    val newIsShow = !newItem.isChosen
                    val index = checklist.indexOf(newItem)
                    try {
                        if (index > -1) {
                            checklist[index] = newItem.apply { isChosen = newIsShow }
                        }
                    } catch (e: Exception) {
                    }
                } else {
                    toast("Kendaraan umum max 2")

                    val newItem = checklist.first { it.id == item.id }.copy()
                    val index = checklist.indexOf(newItem)
                    try {
                        if (index > -1) {
                            checklist[index] = newItem.apply { isChosen = false }
                        }
                    } catch (e: Exception) {
                    }
                }
                subchoiceData.postValue(checklist)


            }
        }
    }


}