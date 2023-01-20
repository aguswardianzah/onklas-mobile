package id.onklas.app.pages.entrepreneurs.myMerchant.addProduct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.*
import id.onklas.app.databinding.AddProductCategoryItem2Binding
import id.onklas.app.databinding.AddProductCategoryListPageBinding
import id.onklas.app.databinding.AddProductSubCategoryItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.sekolah.store.CategorySubTable
import id.onklas.app.pages.sekolah.store.CategoryTable
import id.onklas.app.utils.PagingAdapter

class AddProductCategoryListPage : Privatepage() {

    private val binding by lazy { AddProductCategoryListPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<AddProductViewModel> { component.addProductVmFactory }
    private var currentParentId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.lifecycleOwner = this
        binding.showChild = false

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        viewModel.isLoading.observe(this) {
            if (it)
                loading(msg = "menampilkan data")
            else
                dismissloading()
        }

        binding.rvParent.addItemDecoration(DividerItemDecoration(this, OrientationHelper.VERTICAL))
        binding.rvParent.adapter = mainAdapter

        viewModel.mainCategory.observe(this, mainAdapter::submitList)

        binding.actionBack.setOnClickListener { switch(false) }
        binding.selectedParent.setOnClickListener { switch(false) }

//        binding.rvParent.apply {
//            adapter = categoryParent()
//        }

//        binding.btnAction.setOnClickListener {
//            binding.mainLayout.visibility = View.VISIBLE
//            binding.childLayout.visibility = View.GONE
//        }

        binding.executePendingBindings()
    }

    private fun switch(showChild: Boolean, parentId: Int = 0, parentName: String = "") {
        binding.showChild = showChild

        if (showChild) {
            currentParentId = parentId
            binding.parentName = parentName
            binding.rvParent.adapter = childAdapter
            viewModel.currentChildCategory?.removeObserver(childListener)
            viewModel.setCurrentCategory(parentId)
            viewModel.currentChildCategory?.observe(this, childListener)
        } else {
            binding.rvParent.adapter = mainAdapter
        }
    }

    override fun onBackPressed() {
        if (binding.selectedParent.isShown)
            switch(false)
        else
            super.onBackPressed()
    }

    private val childListener by lazy {
        Observer<PagedList<CategorySubTable>> {
            childAdapter.submitList(it)
        }
    }

    private val mainAdapter by lazy {
        object : PagingAdapter<CategoryTable, MainCategoryVH>(object :
            DiffUtil.ItemCallback<CategoryTable>() {
            override fun areItemsTheSame(oldItem: CategoryTable, newItem: CategoryTable): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: CategoryTable,
                newItem: CategoryTable
            ): Boolean = oldItem == newItem
        }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): MainCategoryVH =
                MainCategoryVH(parent)

            override fun bindItemViewholder(holder: MainCategoryVH, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: MainCategoryVH,
                position: Int,
                payloads: MutableList<Any>
            ) = bindItemViewholder(holder, position)
        }
    }

    private inner class MainCategoryVH(
        parent: ViewGroup,
        val binding: AddProductCategoryItem2Binding = AddProductCategoryItem2Binding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryTable) {
            binding.item = item

            binding.root.setOnClickListener {
                switch(true, item.id, item.name)
            }

            binding.executePendingBindings()
        }
    }

    private val childAdapter by lazy {
        object : PagingAdapter<CategorySubTable, ChildCategoryVH>(object :
            DiffUtil.ItemCallback<CategorySubTable>() {
            override fun areItemsTheSame(
                oldItem: CategorySubTable,
                newItem: CategorySubTable
            ): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: CategorySubTable,
                newItem: CategorySubTable
            ): Boolean = oldItem == newItem
        }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): ChildCategoryVH =
                ChildCategoryVH(parent)

            override fun bindItemViewholder(holder: ChildCategoryVH, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: ChildCategoryVH,
                position: Int,
                payloads: MutableList<Any>
            ) = bindItemViewholder(holder, position)
        }
    }

    private inner class ChildCategoryVH(
        parent: ViewGroup,
        val binding: AddProductSubCategoryItemBinding = AddProductSubCategoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategorySubTable) {
            binding.item = item

            binding.root.setOnClickListener {
                setResult(
                    RESULT_OK,
                    intent.putExtra("categoryId", item.id)
                        .putExtra("categoryName", item.name)
                        .putExtra("parentId", currentParentId)
                )
                finish()
            }

            binding.executePendingBindings()
        }
    }

//    private inner class categoryParent(
//        val listProduct: List<CategoryParent> = viewModel.categoryParent
//    ) :
//        RecyclerView.Adapter<categoryParent.categoryVH>() {
//
//        private inner class categoryVH(
//            parent: ViewGroup,
//            val bindingitem: AddProductCategoryItemBinding = AddProductCategoryItemBinding.inflate(
//                LayoutInflater.from(
//                    parent.context
//                ), parent, false
//            )
//        ) :
//            RecyclerView.ViewHolder(bindingitem.root) {
//            fun bind(item: CategoryParent) {
//                bindingitem.txtCategory.text = item.category_name
//
//
//                bindingitem.root.setOnClickListener {
//                    binding.mainLayout.visibility = View.GONE
//                    binding.childLayout.visibility = View.VISIBLE
//                    binding.txtCategoryParent.text = item.category_name
//
//                    binding.rvChild.apply {
//                        layoutManager = LinearLayoutManager(
//                            this@AddProductCategoryListPage,
//                            RecyclerView.VERTICAL,
//                            false
//                        )
//                        adapter = categoryChild(item.CategoryChild)
//                    }
//                }
//            }
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): categoryVH =
//            categoryVH(parent)
//
//        override fun getItemCount(): Int = listProduct.size
//
//        override fun onBindViewHolder(holder: categoryVH, position: Int) =
//            holder.bind(listProduct[position])
//
//
//    }
//
//    private inner class categoryChild(
//        val listProduct: List<CategoryChild>
//    ) :
//        RecyclerView.Adapter<categoryChild.categoryVH>() {
//
//        private inner class categoryVH(
//            parent: ViewGroup,
//            val bindingitem: AddProductCategoryItemBinding = AddProductCategoryItemBinding.inflate(
//                LayoutInflater.from(
//                    parent.context
//                ), parent, false
//            )
//        ) :
//            RecyclerView.ViewHolder(bindingitem.root) {
//            fun bind(item: CategoryChild) {
//                bindingitem.btnAction.visibility = View.GONE
//                bindingitem.txtCategory.text = item.category_name
//
//                bindingitem.root.setOnClickListener {
//                    onBackPressed()
//                }
//            }
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): categoryVH =
//            categoryVH(parent)
//
//        override fun getItemCount(): Int = listProduct.size
//
//        override fun onBindViewHolder(holder: categoryVH, position: Int) =
//            holder.bind(listProduct[position])
//
//
//    }
}