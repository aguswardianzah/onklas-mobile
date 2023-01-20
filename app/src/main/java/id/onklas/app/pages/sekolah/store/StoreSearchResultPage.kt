package id.onklas.app.pages.sekolah.store

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.StoreSearchProductItemBinding
import id.onklas.app.databinding.StoreSearchResultPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch
import timber.log.Timber

class StoreSearchResultPage : Privatepage(), TabLayout.OnTabSelectedListener {

    companion object {
        fun open(activity: Privatepage, keyword: String) {
            activity.startActivity(
                Intent(activity, StoreSearchResultPage::class.java)
                    .putExtra("keyword", keyword)
            )
        }
    }

    private val binding by lazy { StoreSearchResultPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<StoreVm> { component.storeVmFactory }
    private var keyword = ""

    private var firstLoad = true

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_product_filter, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menu_filter_enable)?.isVisible = viewmodel.isFiltered.value!!
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        keyword = intent.getStringExtra("keyword").toString()



        binding.inName.text = keyword
        binding.inName.setOnClickListener { finish() }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
        }


        lifecycleScope.launch {
            lifecycleScope.launch {
                viewmodel.fetchProduct(0, keyword, "searchKey")
            }
            initData(keyword)
        }

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Terkait"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Terbaru"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Terlaris"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Harga"))
        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL)


        binding.tabLayout.setOnTabSelectedListener(this)


        viewmodel.LoadingShow.observe(this, Observer {
            binding.swipeRefresh.isRefreshing = it
        })

        binding.rvProduct.apply {
            adapter = ResultAdapter
            layoutManager = GridLayoutManager(context, 2)
        }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewmodel.fetchProduct(0, keyword, "searchKey")
            }
            initData(keyword)
        }

    }

    private var currLiveData: LiveData<PagedList<ProductTable>>? = null
    private fun initData(filter: String = "") {
        firstLoad = true
        binding.swipeRefresh.isRefreshing = true
        viewmodel.lastProduct = -1
        viewmodel.hasNextProduct = true
        currLiveData?.removeObservers(this)

        currLiveData = viewmodel.listSearchResult(filter, "searchKey").apply {
            observe(this@StoreSearchResultPage, listObserver)
        }

    }

    private val listObserver by lazy {
        Observer<PagedList<ProductTable>> {
            Timber.e("loaded data: ${it.size}")
            ResultAdapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager = (binding.rvProduct.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvProduct.scrollToPosition(position)
                    }
                } else {
                    binding.rvProduct.scrollToPosition(0)
                    firstLoad = false
                }

            }
        }
    }

    private val ResultAdapter by lazy {
        object : PagingAdapter<ProductTable, ResultViewHolder>(object :
            DiffUtil.ItemCallback<ProductTable>() {
            override fun areItemsTheSame(oldItem: ProductTable, newItem: ProductTable): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ProductTable, newItem: ProductTable): Boolean =
                oldItem == newItem
        }) {
            override fun bindItemViewholder(
                holder: ResultViewHolder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun createItemViewholder(parent: ViewGroup, viewType: Int): ResultViewHolder =
                ResultViewHolder(parent)


            override fun bindItemViewholder(holder: ResultViewHolder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

        }
    }

    private inner class ResultViewHolder(
        parent: ViewGroup,
        val binding: StoreSearchProductItemBinding = StoreSearchProductItemBinding.inflate(
            LayoutInflater.from(
                parent.context
            ), parent, false
        )
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductTable) {
            binding.productImg.clipToOutline = true
            binding.item = item
            binding.viewmodel = viewmodel
            binding.stringUtil = viewmodel.stringUtil

            binding.root.setOnClickListener {
                DetailProduk.open(this@StoreSearchResultPage, item.product_id)
            }
        }
    }


    override fun onTabSelected(tab: TabLayout.Tab?) {
        Timber.e("on tab selected $tab")
        Timber.e("tab position  ${tab?.position}")
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        Timber.e("on tab unselected $tab")
        Timber.e("tab position  ${tab?.position}")
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        Timber.e("on tab reselected $tab")
        Timber.e("tab position  ${tab?.position}")
    }


}