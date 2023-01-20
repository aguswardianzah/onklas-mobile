package id.onklas.app.pages.homework

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import id.onklas.app.R
import id.onklas.app.databinding.FilterCheckboxItemBinding
import id.onklas.app.databinding.FilterRadioItemBinding
import id.onklas.app.databinding.HomeworkFilterPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.theory.MapelTable
import id.onklas.app.pages.PageDialogFragment
import timber.log.Timber

class HomeworkFilterPage : PageDialogFragment() {

    private lateinit var binding: HomeworkFilterPageBinding
    private val viewmodel by activityViewModels<HomeWorkViewModel> { component.homeworkVmFactory }
    private var listMapel = ArrayList<Pair<MapelTable, Boolean>>()
    private val listTime by lazy {
        arrayOf(
            "Semua" to true, "Belum dikerjakan" to false, "Terlewati" to false
        )
    }
    private var lastSelectedTime = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        HomeworkFilterPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.inflateMenu(R.menu.menu_filter_tugas)
        binding.toolbar.setOnMenuItemClickListener {
            listTime[0] = "Semua" to true
            listTime[1] = "Belum dikerjakan" to false
            listTime[2] = "Terlewati" to false
            timeAdapter.notifyDataSetChanged()

            listMapel.forEachIndexed { index, pair ->
                listMapel[index] = pair.first to false
            }
            mapelAdapter.notifyDataSetChanged()

            true
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { dismiss() }

        binding.rvTime.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                OrientationHelper.VERTICAL
            )
        )
        binding.rvTime.adapter = timeAdapter

        binding.rvMapel.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                OrientationHelper.VERTICAL
            )
        )
        binding.rvMapel.adapter = mapelAdapter

        lifecycleScope.launchWhenCreated {
            listMapel.addAll(viewmodel.db.theory().getListMapel().map { it to false })
            mapelAdapter.notifyDataSetChanged()
        }

        viewmodel.filterTime.observe(viewLifecycleOwner, Observer { selected ->

            val selectedId = listTime.indexOfFirst { it.first == selected }
            if (selectedId > -1) {
                listTime[selectedId] = selected to true
                timeAdapter.notifyItemChanged(selectedId, true)

                listTime[lastSelectedTime] = listTime[lastSelectedTime].first to false
                timeAdapter.notifyItemChanged(selectedId, false)

                lastSelectedTime = selectedId
            }
        })

        viewmodel.filterMapel.observe(viewLifecycleOwner, Observer { filter ->
            listMapel.forEachIndexed { index, pair ->
                val selected = filter.contains(pair.first)
                if (pair.second != selected) {
                    listMapel[index] = pair.first to selected
                    mapelAdapter.notifyItemChanged(index, selected)
                }
            }
        })
    }

    private val timeAdapter by lazy {
        object : RecyclerView.Adapter<RadioViewholder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioViewholder =
                RadioViewholder(parent)

            override fun getItemCount(): Int = listTime.size

            override fun onBindViewHolder(holder: RadioViewholder, position: Int) = holder.bind(
                listTime[position]
            )

            override fun onBindViewHolder(
                holder: RadioViewholder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                if (payloads.isNotEmpty()) holder.update(payloads.first() as Boolean)
                else onBindViewHolder(holder, position)
            }
        }
    }

    private val mapelAdapter by lazy {
        object : RecyclerView.Adapter<CheckboxViewholder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckboxViewholder =
                CheckboxViewholder(parent)

            override fun getItemCount(): Int = listMapel.size

            override fun onBindViewHolder(holder: CheckboxViewholder, position: Int) {
                holder.bind(listMapel[position])
            }

            override fun onBindViewHolder(
                holder: CheckboxViewholder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                if (payloads.isNotEmpty()) holder.update(payloads.first() as Boolean)
                else onBindViewHolder(holder, position)
            }
        }
    }

    private inner class RadioViewholder(
        parent: ViewGroup,
        val binding: FilterRadioItemBinding = FilterRadioItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<String, Boolean>) {
            binding.radio.text = item.first
            binding.radio.setOnCheckedChangeListener { buttonView, isChecked ->
                Timber.e("check item ${item.first} --> $isChecked")
                if (isChecked)
                    viewmodel.filterTime.postValue(item.first)
            }
        }

        fun update(checked: Boolean) {
            binding.radio.isChecked = checked
        }
    }

    private inner class CheckboxViewholder(
        parent: ViewGroup,
        val binding: FilterCheckboxItemBinding = FilterCheckboxItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pair: Pair<MapelTable, Boolean>) {
            binding.checkbox.text = pair.first.name
            binding.checkbox.isChecked = pair.second
            binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                val filter = viewmodel.filterMapel.value ?: ArrayList()
                if (isChecked)
                    filter.add(pair.first)
                else
                    filter.remove(pair.first)
                viewmodel.filterMapel.postValue(filter)
            }
        }

        fun update(checked: Boolean) {
            binding.checkbox.isChecked = checked
        }
    }
}