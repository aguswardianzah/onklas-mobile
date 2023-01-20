package id.onklas.app.pages.entrepreneurs

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
import id.onklas.app.databinding.EntrepreneursPenjualanMenuItemBinding
import id.onklas.app.databinding.EntrepreneursPenjualanPageBinding
import id.onklas.app.databinding.MerchantProductItem1Binding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.myMerchant.IncomingOrder.IncomingOrderMainPage
import id.onklas.app.pages.entrepreneurs.myMerchant.RiwayatOrder.HistoryOrderPage
import id.onklas.app.pages.entrepreneurs.myMerchant.addProduct.AddProductPage
import id.onklas.app.pages.entrepreneurs.myMerchant.income.IncomePage
import id.onklas.app.pages.entrepreneurs.myMerchant.mystore.Mystore
import id.onklas.app.pages.entrepreneurs.review.ReviewPage
import id.onklas.app.pages.sekolah.store.DetailProduk
import id.onklas.app.pages.sekolah.store.ProductMerchantTable
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

class EntrepreneursPenjualan : Fragment() {

    private lateinit var binding: EntrepreneursPenjualanPageBinding
    private val viewmodel by activityViewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }

    private val menuItem = ArrayList<MenuItemTable>()

    private var firstLoad = true

    private var adaptermenu = MenuAdapter(menuItem)

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = EntrepreneursPenjualanPageBinding.inflate(inflater, container, false).also { binding = it }.root


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

        adaptermenu = MenuAdapter(menuItem, menuClickHandler)


        lifecycleScope.launchWhenCreated {
            viewmodel.loadMerchantSummary("MyMerchant").collectLatest {
                try {
                    menuItem.clear()
                    menuItem.addAll(viewmodel.menu_item)
                    menuItem.set(0, MenuItemTable(
                            menuItem[0].menu_id,
                            menuItem[0].icon,
                            menuItem[0].name,
                            "${it.product} Produk",
                            menuItem[0].satuan,
                    ))
                    menuItem.set(1, MenuItemTable(
                            menuItem[1].menu_id,
                            menuItem[1].icon,
                            menuItem[1].name,
                            "Rp " + viewmodel.stringUtil.formatCurrency2(it.incoming_amount),
                            menuItem[1].satuan,
                    ))
                    menuItem.set(2, MenuItemTable(
                            menuItem[2].menu_id,
                            menuItem[2].icon,
                            menuItem[2].name,
                            "${it.incoming_order}",
                            menuItem[2].satuan,
                    ))
                    menuItem.set(3, MenuItemTable(
                            menuItem[3].menu_id,
                            menuItem[3].icon,
                            menuItem[3].name,
                            "${it.review} ",
                            menuItem[3].satuan,
                    ))
                    menuItem.set(4, MenuItemTable(
                            menuItem[4].menu_id,
                            menuItem[4].icon,
                            menuItem[4].name,
                            "${it.history_order} ",
                            menuItem[4].satuan,
                    ))
                    adaptermenu.notifyDataSetChanged()
                } catch (e: Exception) {
                }
            }
        }

        binding.rvPenjualanMenu.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = adaptermenu
        }

        binding.rvProduct.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = ProductAdapter
        }
        binding.btnAddProduct.setOnClickListener {
            startActivity(Intent(requireContext(), AddProductPage::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchWhenCreated {
            viewmodel.loadMerchantUser().collectLatest {
                try {
                    binding.txtJumlahReviewer.text = it.amount_of_review.toString() + " review"
                    binding.txtRating.text = it.sales_rating.toString()
                    binding.txtTotalTerjual.text = it.success_transaction.toString()
                    initData(it.id)
                } catch (e: Exception) {
                }

            }
        }
    }

    private inner class MenuAdapter(
            val listProduct: List<MenuItemTable>,
            val onClick: (Int, View) -> Unit = { _, _ -> }
    ) :
            RecyclerView.Adapter<MenuAdapter.MenuVH>() {

        private inner class MenuVH(
                parent: ViewGroup,
                val binding: EntrepreneursPenjualanMenuItemBinding = EntrepreneursPenjualanMenuItemBinding.inflate(
                        LayoutInflater.from(
                                parent.context
                        ), parent, false
                )
        ) :
                RecyclerView.ViewHolder(binding.root) {
            fun bind(item: MenuItemTable) {
                binding.icon.setImageResource(item.icon);
                binding.name.text = item.name
                binding.data.text = item.data
                binding.root.setOnClickListener {
                    onClick.invoke(
                            adapterPosition,
                            binding.name
                    )
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuVH =
                MenuVH(parent)

        override fun getItemCount(): Int = listProduct.size

        override fun onBindViewHolder(holder: MenuVH, position: Int) =
                holder.bind(listProduct[position])
    }


    private var currLiveData: LiveData<PagedList<ProductMerchantTable>>? = null
    private fun initData(merchant_id: Int) {
        firstLoad = true
        viewmodel.lastProduct = -1
        viewmodel.hasNextProduct = true
        currLiveData?.removeObservers(this)
        currLiveData = viewmodel.listProductMerchant(merchant_id, "all").apply {
            observe(viewLifecycleOwner, listObserver)
        }

    }

    private val listObserver by lazy {
        Observer<PagedList<ProductMerchantTable>> {
            Timber.e("loaded data: ${it.size}")
            ProductAdapter.submitList(it) {
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

    private val ProductAdapter by lazy {
        object : PagingAdapter<ProductMerchantTable, ProdukVH>(object : DiffUtil.ItemCallback<ProductMerchantTable>() {
            override fun areItemsTheSame(oldItem: ProductMerchantTable, newItem: ProductMerchantTable): Boolean =
                    oldItem == newItem

            override fun areContentsTheSame(oldItem: ProductMerchantTable, newItem: ProductMerchantTable): Boolean =
                    oldItem == newItem
        }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): ProdukVH =
                    ProdukVH(parent)


            override fun bindItemViewholder(holder: ProdukVH, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(holder: ProdukVH, position: Int, payloads: MutableList<Any>) {
                getItem(position)?.let { holder.bind(it) }
            }

        }
    }

    private inner class ProdukVH(
            parent: ViewGroup,
            val binding: MerchantProductItem1Binding = MerchantProductItem1Binding.inflate(
                    LayoutInflater.from(
                            parent.context
                    ), parent, false
            )
    ) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductMerchantTable) {
            binding.productImg.clipToOutline = true
            binding.item = item

            binding.lblHarga.text = "Rp. " + viewmodel.stringUtil.formatCurrency2(item.price)

            binding.root.setOnClickListener {
                DetailProduk.open(requireContext() as Privatepage, item.product_id)
            }
        }
    }

    private val menuClickHandler by lazy {
        { pos: Int, view: View ->
            when (pos) {
                0 -> startActivity(
                        Intent(
                                requireContext(),
                                Mystore::class.java
                        )
                )
                1 -> startActivity(
                        Intent(
                                requireContext(),
                                IncomePage::class.java
                        )
                )
                2 -> startActivity(
                        Intent(
                                requireContext(),
                                IncomingOrderMainPage::class.java
                        )
                )
                3 -> startActivity(
                        Intent(
                                requireContext(),
                                ReviewPage::class.java
                        )
                )
                4 -> startActivity(
                        Intent(
                                requireContext(),
                                HistoryOrderPage::class.java
                        )
                )
            }
        }
    }

}