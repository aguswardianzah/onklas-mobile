package id.onklas.app.pages.entrepreneurs.myMerchant.income

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.IncomeProductItemBinding
import id.onklas.app.databinding.IncomeProductPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.entrepreneurs.OrderVM
import id.onklas.app.pages.entrepreneurs.TransactionWithProduct
import id.onklas.app.pages.entrepreneurs.OrderDetailPage
import id.onklas.app.pages.entrepreneurs.myMerchant.addProduct.AddProductPage
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber


class IncomeProductPage : Fragment() {

    private lateinit var binding: IncomeProductPageBinding
    private val viewModel by activityViewModels<OrderVM> { component.entrepreneursOrderFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }

    private var firstLoad = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = IncomeProductPageBinding.inflate(inflater, container, false).also { binding = it }.root


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.kosongLay.imgEmp.apply { setImageDrawable(resources.getDrawable(R.drawable.img_emp_income)) }
        binding.kosongLay.txtTitle.text = "Belum Ada Pendapatan"
        binding.kosongLay.txtSubtitle.text = "Upload Produk dan mulai berjualan ke sesama sekolah atau luar sekolah"
        binding.kosongLay.actionBtn.text = "Upload Produk"
        binding.kosongLay.actionBtn.setOnClickListener {
            startActivity(Intent(context, AddProductPage::class.java))
        }

        binding.rvProduct.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = TransaksiAdapter
        }

        initData()

        viewModel.LoadingShow.observe(viewLifecycleOwner, Observer {
            binding.swipeRefresh.isRefreshing = it
        })

        binding.swipeRefresh.setOnRefreshListener {
            initData()
        }
        lifecycleScope.launchWhenCreated {
            viewModel.db.kwu().getMerchantSummary("MyMerchant",viewModel.pref.getInt("merchantId")).collectLatest {
                try {
                    Timber.e("it.incoming_amount ${it.incoming_amount}")
                    binding.txtTotalPendapatan.text = "Total Seluruh Pendapatan : Rp " + viewModel.stringUtil.formatCurrency2(it.incoming_amount)
                } catch (e: Exception) {
                }
            }
        }
        viewModel.dateFilterLiveData.observe(viewLifecycleOwner, Observer {
            initData(it)
        })
    }

    private fun initData(date:String=""){
        viewModel.isEmptyOrder.postValue(false)
        lifecycleScope.launch { viewModel.fetchListSellerOrder(0, "incoming", "seller", date) }
        initIncomingTransaksi(date)
    }

    private var currLiveData: LiveData<PagedList<TransactionWithProduct>>? = null
    private fun initIncomingTransaksi(dateFilter:String) {
        firstLoad = true
        viewModel.lastProduct = -1
        viewModel.hasNextProduct = true
        currLiveData?.removeObserver(listObserver)
        currLiveData = viewModel.listIncome("complete", "seller", "ACCEPTED",dateFilter).apply {
            observe(viewLifecycleOwner, listObserver)
        }

    }

    private val listObserver by lazy {
        Observer<PagedList<TransactionWithProduct>> {
            viewModel.isEmptyOrder.postValue(it.isEmpty())
            viewModel.LoadingShow.postValue(it.isEmpty())
            Timber.e("loaded data: ${it.size}")
            TransaksiAdapter.submitList(it) {
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

    private val TransaksiAdapter by lazy {
        object : PagingAdapter<TransactionWithProduct, TransaksiDateViewholder>(object : DiffUtil.ItemCallback<TransactionWithProduct>() {
            override fun areItemsTheSame(oldItem: TransactionWithProduct, newItem: TransactionWithProduct): Boolean =
                    oldItem == newItem

            override fun areContentsTheSame(oldItem: TransactionWithProduct, newItem: TransactionWithProduct): Boolean =
                    oldItem == newItem
        }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): TransaksiDateViewholder =
                    TransaksiDateViewholder(parent)


            override fun bindItemViewholder(holder: TransaksiDateViewholder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(holder: TransaksiDateViewholder, position: Int, payloads: MutableList<Any>) {
                getItem(position)?.let { holder.bind(it) }
            }


        }
    }

    private inner class TransaksiDateViewholder(
            parent: ViewGroup,
            val binding: IncomeProductItemBinding = IncomeProductItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionWithProduct) {
            binding.item = item.order
            binding.stringUtil = viewModel.stringUtil
            binding.dateUtil = viewModel.dateUtil
            binding.frontName = item.order.buyer_name

            binding.txtCountProduct.visibility = View.GONE


            binding.actionDetail.setOnClickListener {
                OrderDetailPage.open(
                        requireActivity() as AppCompatActivity, item.order.id, "Detail Order", "seller", true
                )
            }

        }
    }

}