package id.onklas.app.pages.sekolah.store

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.CategoryProductPageBinding
import id.onklas.app.databinding.StoreProductItem2FullBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch
import timber.log.Timber

class CategoryProductPage : Privatepage() {

    companion object {
        fun open(activity: Privatepage, categorySubId: Int, categoryId: Int, title: String) {
            activity.startActivity(
                    Intent(activity, CategoryProductPage::class.java)
                            .putExtra("categoryId", categoryId)
                            .putExtra("categorySubId", categorySubId)
                            .putExtra("title", title)
            )
        }
    }

    private val binding by lazy { CategoryProductPageBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodel by viewModels<StoreVm> { component.storeVmFactory }

    private var categoryId = 0
    private var categorySubId = 0
    private var firstLoad = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        categoryId = intent.getIntExtra("categoryId", 0)
        categorySubId = intent.getIntExtra("categorySubId", 0)


        Timber.e("category id $categoryId")
        Timber.e("category sub id $categorySubId")

        binding.empLay.imgEmp.apply { setImageDrawable(resources.getDrawable(R.drawable.img_emp_riwayat_order)) }
        binding.empLay.txtTitle.text ="Pencarian Tidak Ditemukan"
        binding.empLay.txtSubtitle.text = "Koreksi kata pencarian atau ubah menggunakan kata yang lainnya"
        binding.empLay.actionBtn.visibility = View.INVISIBLE

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = intent.getStringExtra("title")
            binding.toolbar.title = intent.getStringExtra("title")
        }
        lifecycleScope.launch {
            viewmodel.fetchProduct(0, "", "categoryProduct", categoryId = categoryId, categorySubId = categorySubId)
        }
        initData(categoryId = categoryId, categorySubId = categorySubId)

        viewmodel.LoadingShow.observe(this, Observer {
            binding.swipeRefresh.isRefreshing = it
        })

        binding.rvProduct.apply {
            adapter = ResultAdapter
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(
                    LinearSpaceDecoration(
                            space = resources.getDimensionPixelSize(
                                    R.dimen._8sdp
                            ),
                            includeTop = true, includeBottom = true
                    )
            )
        }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewmodel.fetchProduct(0, "", "categoryProduct", categoryId = categoryId, categorySubId = categorySubId)
            }
            initData(categoryId = categoryId, categorySubId = categorySubId)
        }

    }

    private var currLiveData: LiveData<PagedList<ProductTable>>? = null
    private fun initData(filter: String = "", categoryId: Int, categorySubId: Int) {
        firstLoad = true
        binding.swipeRefresh.isRefreshing = true
        viewmodel.lastProduct = -1
        viewmodel.hasNextProduct = true
        currLiveData?.removeObservers(this)
        currLiveData = viewmodel.listCategoryResult(filter, "categoryProduct", categoryId, categorySubId).apply {
            observe(this@CategoryProductPage, listObserver)
        }

    }

    private val listObserver by lazy {
        Observer<PagedList<ProductTable>> {
            Timber.e("loaded data: ${it.size}")
            binding.isEmptyData  = it.isEmpty()
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
            val binding: StoreProductItem2FullBinding = StoreProductItem2FullBinding.inflate(
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

            Timber.e("product itabel $item")
            binding.root.setOnClickListener {
                DetailProduk.open(this@CategoryProductPage, item.product_id)
            }
        }
    }
}