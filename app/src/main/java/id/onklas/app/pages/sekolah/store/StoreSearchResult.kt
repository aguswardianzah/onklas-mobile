package id.onklas.app.pages.sekolah.store

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.StoreProductItem2FullBinding
import id.onklas.app.databinding.StoreSearchResultBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.PagingAdapter
import timber.log.Timber


class StoreSearchResult : Fragment() {
    private lateinit var binding: StoreSearchResultBinding
    private val viewmodel by activityViewModels<StoreVm> { component.storeVmFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }

    private var filter = ""

    private var firstLoad = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = StoreSearchResultBinding.inflate(inflater, container, false)
            .also { binding = it }.root


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        filter = arguments?.getString("selectedFilter").toString()
        Timber.e("filter selected ------------- $filter")

        initData(filter)


        binding.swipeRefresh.apply {
            setOnRefreshListener {
                initData(filter)
            }
        }

        binding.rvProduct.apply {
            layoutManager = GridLayoutManager(
                    context,
                    2,
            )
            adapter = ResultAdapter
        }

        binding.searchLay.setOnClickListener {
            startActivity(Intent(requireContext(), StoreSearchPage::class.java))
        }

        binding.selectSearch.setOnClickListener {
            bottomsheetSelectSearch(requireContext(), layoutInflater)
        }

        viewmodel.LoadingShow.observe(requireActivity(), Observer {
            Timber.e("loding shoe $it")
            binding.swipeRefresh.isRefreshing = it
        })

    }

    private var currLiveData: LiveData<PagedList<ProductTable>>? = null
    private fun initData(filter: String = "") {
        firstLoad = true
        binding.swipeRefresh.isRefreshing = true
        viewmodel.lastProduct = -1
        viewmodel.hasNextProduct = true
        currLiveData?.removeObservers(viewLifecycleOwner)
        currLiveData = viewmodel.listgoodies(filter, "filter_homepage").apply {
            observe(viewLifecycleOwner, listObserver)
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

    private fun bottomsheetSelectSearch(context: Context, layoutInflater: LayoutInflater) {
        val dialog = BottomSheetDialog(context)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = layoutInflater.inflate(R.layout.bottom_sheet_store_search_choice, null)
//        view.btn_jasa.setOnClickListener {
//            binding.selectSearch.text = "Jasa"
//            dialog.dismiss()
//        }
//        view.btn_produk.setOnClickListener {
//            binding.selectSearch.text = "Produk"
//            dialog.dismiss()
//        }
//
//        view.btn_exit.setOnClickListener {
//            dialog.dismiss()
//        }

        dialog.setContentView(view)
        dialog.show()
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

            binding.root.setOnClickListener {
                DetailProduk.open(requireActivity() as Privatepage, item.product_id)
            }
        }
    }
}