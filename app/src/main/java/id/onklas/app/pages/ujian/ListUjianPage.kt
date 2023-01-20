package id.onklas.app.pages.ujian

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.ListUjianPageBinding
import id.onklas.app.databinding.UjianItemBinding
import id.onklas.app.di.component
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import timber.log.Timber

class ListUjianPage : Fragment() {

    private lateinit var binding: ListUjianPageBinding
    private val viewmodel by activityViewModels<UjianViewModel> { component.ujianVmFactory }
    private lateinit var listData: LiveData<PagedList<ExamTable>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ListUjianPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewmodel

        binding.rvUjian.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                includeTop = true,
                includeBottom = true
            )
        )
        binding.rvUjian.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { refresh() }

        viewmodel.loadingList.observe(
            viewLifecycleOwner,
            Observer(binding.swipeRefresh::setRefreshing)
        )
        viewmodel.calKelas.observe(viewLifecycleOwner, { refresh() })

        lifecycleScope.launchWhenCreated {
            viewmodel.persistDB.ujian().listUjianScored().debounce(100).collect {
                if (it.isNotEmpty())
                    refresh()
            }
        }
    }

    private var firstLoad = true
    private val listObserver by lazy {
        Observer<PagedList<ExamTable>> {
            adapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager =
                        (binding.rvUjian.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvUjian.scrollToPosition(position)
                    }
                } else {
                    binding.rvUjian.scrollToPosition(0)
                    firstLoad = false
                }
            }
            viewmodel.loadingList.postValue(false)
        }
    }

    private fun refresh() {
        lifecycleScope.launch {
            try {
                if (this@ListUjianPage::listData.isInitialized)
                    listData.removeObserver(listObserver)
                listData = viewmodel.listUjianStudent()
                listData.observe(viewLifecycleOwner, listObserver)

                firstLoad = true

                viewmodel.loadingList.postValue(true)
//                viewmodel.memoryDB.ujian().nuke()
                viewmodel.hasNextUjian = true
                viewmodel.ujianStart = -1
                viewmodel.fetchUjian()
            } catch (e: Exception) {
                Timber.e(e)
                viewmodel.loadingList.postValue(false)
            }
        }
    }

    private val adapter by lazy {
        object : PagingAdapter<ExamTable, Viewholder>(
            object : DiffUtil.ItemCallback<ExamTable>() {
                override fun areItemsTheSame(oldItem: ExamTable, newItem: ExamTable): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: ExamTable, newItem: ExamTable): Boolean =
                    oldItem == newItem
            }
        ) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun bindItemViewholder(holder: Viewholder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: Viewholder,
                position: Int,
                payloads: MutableList<Any>
            ) = bindItemViewholder(holder, position)
        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: UjianItemBinding = UjianItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExamTable) {
            binding.item = item
            binding.executePendingBindings()
            binding.btnAttend.setOnClickListener {
                startActivityForResult(
                    Intent(requireActivity(), PrepareUjianPage::class.java)
                        .putExtra("id", item.id.toString())
                        .putExtra("name", item.mapelName),
                    4783
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.e("activity result list ujian")
        refresh()
        super.onActivityResult(requestCode, resultCode, data)
    }
}