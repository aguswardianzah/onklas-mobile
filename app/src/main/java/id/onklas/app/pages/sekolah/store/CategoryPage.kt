package id.onklas.app.pages.sekolah.store

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.CategoryPageBinding
import id.onklas.app.databinding.StoreCategoryChildItemBinding
import id.onklas.app.databinding.StoreCategoryItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.PagingAdapter
import timber.log.Timber

class CategoryPage : Privatepage() {

    companion object {
        fun open(activity: AppCompatActivity) {
            activity.startActivity(
                Intent(activity, CategoryPage::class.java)
            )
        }
    }

    private val binding by lazy { CategoryPageBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodel by viewModels<StoreVm> { component.storeVmFactory }

    private var firstLoad = true
    private var selected_id = 0 // subcategory id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        viewmodel.LoadingShow.observe(this, Observer {
            binding.swipeRefresh.isRefreshing = it
        })

        binding.rvMain.apply {
            layoutManager = LinearLayoutManager(this@CategoryPage, RecyclerView.VERTICAL, false)
            adapter = CategoryAdapter
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Kategori"
            binding.toolbar.title = "Kategori"
        }

        binding.rvChild.apply {
            layoutManager = LinearLayoutManager(this@CategoryPage, RecyclerView.VERTICAL, false)
            adapter = CategorySubAdapter
        }

        binding.swipeRefresh.isEnabled = false
        binding.swipeRefresh.setOnRefreshListener {
            initDataCategorySub(selected_id)
        }

        initDataCategory(1) // default id 1 karena id 1 ada di atas
    }

    private var currLiveData: LiveData<PagedList<CategoryTable>>? = null
    private fun initDataCategory(id_selected: Int) {
        firstLoad = true
        binding.swipeRefresh.isRefreshing = true
        viewmodel.lastProduct = -1
        viewmodel.hasNextProduct = true
        currLiveData?.removeObservers(this)
        currLiveData = viewmodel.listCategory(id_selected).apply {
            observe(this@CategoryPage, listObserver)
        }
    }

    private val listObserver by lazy {
        Observer<PagedList<CategoryTable>> {
            Timber.e("loaded data: ${it.size}")

            viewmodel.masterListCategory.addAll(it)
            CategoryAdapter.submitList(viewmodel.masterListCategory)
            CategoryAdapter.notifyDataSetChanged()

        }
    }

    private var currSubLiveData: LiveData<PagedList<CategorySubTable>>? = null
    private fun initDataCategorySub(parent_id: Int) {
        firstLoad = true
        binding.swipeRefresh.isRefreshing = true
        viewmodel.lastProduct = -1
        viewmodel.hasNextProduct = true
        currSubLiveData?.removeObservers(this)
        currSubLiveData = viewmodel.listCategorySub(parent_id).apply {
            observe(this@CategoryPage, listSubObserver)
        }

    }

    private val listSubObserver by lazy {
        Observer<PagedList<CategorySubTable>> {
            Timber.e("loaded data: ${it.size}")
            CategorySubAdapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager = (binding.rvChild.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvChild.scrollToPosition(position)
                    }
                } else {
                    binding.rvChild.scrollToPosition(0)
                    firstLoad = false
                }

            }
        }
    }

    private val CategoryAdapter by lazy {
        object :
            ListAdapter<CategoryTable, Viewholder>(object : DiffUtil.ItemCallback<CategoryTable>() {
                override fun areItemsTheSame(
                    oldItem: CategoryTable,
                    newItem: CategoryTable
                ): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: CategoryTable,
                    newItem: CategoryTable
                ): Boolean =
                    oldItem == newItem
            }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)


            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(getItem(position), position)

        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: StoreCategoryItemBinding = StoreCategoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryTable, position: Int) {
            binding.root.layoutParams.width = (viewmodel.screenWidth / 3.5).toInt()
            binding.root.layoutParams.height = (viewmodel.screenWidth / 3.5).toInt()

            if (!item.is_selected) {
                binding.mainLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        this@CategoryPage,
                        R.color.bg_gray
                    )
                )
                binding.label.setTextColor(ContextCompat.getColor(this@CategoryPage, R.color.gray))
            } else {
                Timber.e("category id-------- ${item.id}")
                initDataCategorySub(item.id)
                selected_id = item.id

                binding.mainLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        this@CategoryPage,
                        R.color.white
                    )
                )
                binding.label.setTextColor(
                    ContextCompat.getColor(
                        this@CategoryPage,
                        R.color.colorPrimary
                    )
                )
            }

            binding.icon.clipToOutline = true
            glide.load(item.icon)
                .centerCrop()
                .into(binding.icon)
            binding.label.text = item.name

            binding.root.setOnClickListener {
                //reset data list
                if (viewmodel.masterListCategory.find { it.is_selected } != null) {
                    val Item = viewmodel.masterListCategory.find { it.is_selected }
                    val posisiItem = viewmodel.masterListCategory.indexOf(Item)
                    val olddata = viewmodel.masterListCategory[posisiItem]
                    viewmodel.masterListCategory.set(
                        posisiItem, CategoryTable(
                            olddata.id,
                            olddata.name,
                            olddata.icon,
                            false,
                        )
                    )
                    CategoryAdapter?.notifyItemChanged(posisiItem)
                }

                viewmodel.masterListCategory.set(
                    position, CategoryTable(
                        item.id,
                        item.name,
                        item.icon,
                        true,
                    )
                )
                CategoryAdapter?.notifyItemChanged(position)
            }
        }
    }

    private val CategorySubAdapter by lazy {
        object : PagingAdapter<CategorySubTable, SubViewholder>(object :
            DiffUtil.ItemCallback<CategorySubTable>() {
            override fun areItemsTheSame(
                oldItem: CategorySubTable,
                newItem: CategorySubTable
            ): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: CategorySubTable,
                newItem: CategorySubTable
            ): Boolean =
                oldItem == newItem
        }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): SubViewholder =
                SubViewholder(parent)


            override fun bindItemViewholder(holder: SubViewholder, position: Int) {
                getItem(position)?.let { holder.bind(it, position) }
            }

            override fun bindItemViewholder(
                holder: SubViewholder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                getItem(position)?.let { holder.bind(it, position) }
            }

        }
    }

    private inner class SubViewholder(
        parent: ViewGroup,
        val binding: StoreCategoryChildItemBinding = StoreCategoryChildItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategorySubTable, position: Int) {
            binding.text.text = item.name

            binding.root.setOnClickListener {
                CategoryProductPage.open(
                    this@CategoryPage,
                    item.sub_id,
                    item.parent_id,
                    item.name
                )
            }
        }

    }
}
