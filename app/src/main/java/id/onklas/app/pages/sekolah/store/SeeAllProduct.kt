package id.onklas.app.pages.sekolah.store

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.SeeAllProductBinding
import id.onklas.app.databinding.StoreProductItem2FullBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.GridSpacingItemDecoration
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import timber.log.Timber

class SeeAllProduct : Privatepage() {

    companion object {
        fun open(activity: AppCompatActivity, titlePage: String, filterCard: String) {
            activity.startActivity(
                    Intent(activity, SeeAllProduct::class.java)
                            .putExtra("titlePage", titlePage)
                            .putExtra("filterCard", filterCard)
            )
        }
    }

    private val binding by lazy { SeeAllProductBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodel by viewModels<StoreVm> { component.storeVmFactory }
    private var titlePage = ""
    private var filter = ""
    private var firstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        titlePage = intent.getStringExtra("titlePage").toString()
        filter = intent.getStringExtra("filterCard").toString()

        initData(filter)

        binding.swipeRefresh.apply {
            setOnRefreshListener {
                initData(filter)
            }
        }


        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = titlePage
            binding.toolbar.title = titlePage
        }

        binding.rvProduct.apply {
            layoutManager = GridLayoutManager(
                    context,
                    2,
            )
            adapter = GoodieDetailCardAdapter
            addItemDecoration(
                    GridSpacingItemDecoration(
                            2,
                            resources.getDimensionPixelSize(R.dimen._4sdp),
                            true,
                            resources.getDimensionPixelSize(R.dimen._8sdp)
                    )
            )
        }

        viewmodel.LoadingShow.observe(this, Observer {
            binding.swipeRefresh.isRefreshing = it
        })


    }

    private var currLiveData: LiveData<PagedList<ProductTable>>? = null
    private fun initData(filter: String = "") {
        firstLoad = true
        binding.swipeRefresh.isRefreshing = true
        viewmodel.lastProduct = -1
        viewmodel.hasNextProduct = true
        currLiveData?.removeObservers(this)
        currLiveData = viewmodel.listgoodies(filter, "card_homepage").apply {
            observe(this@SeeAllProduct, listObserver)
        }

    }

    private val listObserver by lazy {
        Observer<PagedList<ProductTable>> {
            Timber.e("loaded data: ${it.size}")
            GoodieDetailCardAdapter.submitList(it) {
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

    private val GoodieDetailCardAdapter by lazy {
        object : PagingAdapter<ProductTable, GoodieDetailCardViewHolder>(object : DiffUtil.ItemCallback<ProductTable>() {
            override fun areItemsTheSame(oldItem: ProductTable, newItem: ProductTable): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ProductTable, newItem: ProductTable): Boolean =
                    oldItem == newItem
        }) {

            override fun createItemViewholder(parent: ViewGroup, viewType: Int): GoodieDetailCardViewHolder =
                    GoodieDetailCardViewHolder(parent)


            override fun bindItemViewholder(holder: GoodieDetailCardViewHolder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(holder: GoodieDetailCardViewHolder, position: Int, payloads: MutableList<Any>) {
                getItem(position)?.let { holder.bind(it) }
            }

        }
    }

    private inner class GoodieDetailCardViewHolder(
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
            binding.stringUtil = viewmodel.stringUtil
            binding.viewmodel = viewmodel

            binding.root.setOnClickListener {
                DetailProduk.open(this@SeeAllProduct, item.product_id)
            }
        }
    }
}