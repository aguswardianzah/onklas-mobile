package id.onklas.app.pages.login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.ListSekolahItemBinding
import id.onklas.app.databinding.ListSekolahPageBinding
import id.onklas.app.di.component
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.pages.PageDialogFragment
import id.onklas.app.utils.PagingAdapter
import timber.log.Timber

class ListSekolahPage(private val onItemClick: (SekolahItem) -> Unit) : PageDialogFragment() {

    private val viewmodel by activityViewModels<LoginViewModel> { component.loginVmFactory }
    private lateinit var binding: ListSekolahPageBinding

    private var firstLoad = true
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private val runSearch by lazy { Runnable { initData(binding.inSearch.text.toString()) } }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ListSekolahPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { dismiss() }
        binding.rvSekolah.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                includeTop = true,
                includeBottom = true
            )
        )
        binding.rvSekolah.adapter = adapter
        binding.inSearch.doAfterTextChanged {
            handler.removeCallbacks(runSearch)
            handler.postDelayed(runSearch, 500)
        }
        initData()
    }

    private var currLiveData: LiveData<PagedList<SekolahItem>>? = null
    private fun initData(search: String = "") {
        firstLoad = true
        viewmodel.lastSchool = -1
        viewmodel.hasNextSchool = true
        binding.progress.visibility = View.VISIBLE
        binding.progressLabel.visibility = View.VISIBLE
        currLiveData?.removeObservers(viewLifecycleOwner)
        currLiveData = viewmodel.listSekolah(search).apply {
            observe(viewLifecycleOwner, listObserver)
        }
//        lifecycleScope.launchWhenCreated {
//            viewmodel.lastSchool = -1
//            viewmodel.fetchSchool()
//        }
    }

    private val listObserver by lazy {
        Observer<PagedList<SekolahItem>> {
            Timber.e("loaded data: ${it.size}")
            adapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager = (binding.rvSekolah.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvSekolah.scrollToPosition(position)
                    }
                } else {
                    binding.rvSekolah.scrollToPosition(0)
                    firstLoad = false
                }
                binding.progress.visibility = View.GONE
                binding.progressLabel.visibility = View.GONE
            }
        }
    }

    private val adapter by lazy {
        object : PagingAdapter<SekolahItem, SekolahViewholder>(object :
            DiffUtil.ItemCallback<SekolahItem>() {
            override fun areItemsTheSame(
                oldItem: SekolahItem,
                newItem: SekolahItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: SekolahItem,
                newItem: SekolahItem
            ): Boolean = oldItem == newItem
        }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): SekolahViewholder =
                SekolahViewholder(parent)

            override fun bindItemViewholder(holder: SekolahViewholder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: SekolahViewholder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                getItem(position)?.let { holder.bind(it) }
            }
        }
    }

    inner class SekolahViewholder(
        parent: ViewGroup,
        private val binding: ListSekolahItemBinding = ListSekolahItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SekolahItem) {
            binding.text.text = item.name
            binding.root.setOnClickListener {
                onItemClick.invoke(item)
                dismiss()
            }
        }
    }
}