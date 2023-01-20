package id.onklas.app.pages.prokes.prokesteacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.ScreeningClassBinding
import id.onklas.app.databinding.ScreeningClassItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BasePage
import id.onklas.app.pages.prokes.ListClass
import id.onklas.app.pages.prokes.ProkesViewmodel
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch
import timber.log.Timber

class ScreeningClass : BasePage() {

    private val binding by lazy { ScreeningClassBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<ProkesViewmodel> { component.prokesVmFactory }
    private var firstLoad = true

    override fun onResume() {
        lifecycleScope.launch {
            viewmodel.fetchClass()
        }
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        lifecycleScope.launch {
            viewmodel.fetchClass()
            initData()
        }

        binding.rvScreening.apply {
            adapter = ClasAdaper
        }

        viewmodel.LoadingShow.observe(this, Observer {
            Timber.e("loading show $it")
            if (it) {
                loading()
            } else {
                dismissloading()
                binding.swipeRefresh.isRefreshing = it
            }
        })

        viewmodel.errorString.observe(this, Observer {
            toast(it)
        })

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Lakukan pemeriksaan"
            binding.toolbar.title = "Lakukan pemeriksaan"
        }

        initData()


        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewmodel.fetchClass()
                initData()
            }
        }
    }

    private var currLiveData: LiveData<PagedList<ListClass>>? = null
    private fun initData() {
        firstLoad = true
        viewmodel.lastProduct = -1
        viewmodel.hasNextProduct = true
        currLiveData?.removeObservers(this)
        currLiveData = viewmodel.ListClassItem().apply {
            observe(this@ScreeningClass, listObserver)
        }

    }

    private val listObserver by lazy {
        Observer<PagedList<ListClass>> {
            Timber.e("loaded data: ${it.size}")
            ClasAdaper.submitList(it) {
                if (!firstLoad) {
                    val layoutManager = (binding.rvScreening.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvScreening.scrollToPosition(position)
                    }
                } else {
                    binding.rvScreening.scrollToPosition(0)
                    firstLoad = false
                }

            }
        }
    }


    private val ClasAdaper by lazy {
        object : PagingAdapter<ListClass, ClassVh>(object :
                DiffUtil.ItemCallback<ListClass>() {
            override fun areItemsTheSame(
                    oldItem: ListClass,
                    newItem: ListClass
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                    oldItem: ListClass,
                    newItem: ListClass
            ): Boolean =
                    oldItem == newItem
        }) {

            override fun createItemViewholder(parent: ViewGroup, viewType: Int): ClassVh =
                    ClassVh(parent)


            override fun bindItemViewholder(holder: ClassVh, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(holder: ClassVh, position: Int, payloads: MutableList<Any>) {
                getItem(position)?.let { holder.bind(it) }
            }
        }
    }

    private inner class ClassVh(
            parent: ViewGroup,
            val binding: ScreeningClassItemBinding = ScreeningClassItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ListClass) {
            binding.item = item
            binding.root.setOnClickListener { select(item) }

            binding.done = (item.total_student_remaining == "0")

        }

        private fun select(item: ListClass) {
            ScreeningStudent.open(
                    this@ScreeningClass,
                    item.name,
                    item.id,
                    item.majorName
            )
        }
    }
}