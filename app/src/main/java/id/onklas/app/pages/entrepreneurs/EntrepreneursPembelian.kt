package id.onklas.app.pages.entrepreneurs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.databinding.EntrepreneursLastbuyItemBinding
import id.onklas.app.databinding.EntrepreneursLastbuyItemMainBinding
import id.onklas.app.databinding.EntrepreneursPembelianPageBinding
import id.onklas.app.databinding.EntrepreneursPenjualanMenuItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.entrepreneurs.pembelian.purchase.PembelianPage
import id.onklas.app.pages.entrepreneurs.review.ReviewPage
import id.onklas.app.pages.klaspay.topup.KlaspayTopupPage
import kotlinx.coroutines.flow.collectLatest

class EntrepreneursPembelian : Fragment() {

    private lateinit var binding: EntrepreneursPembelianPageBinding
    private val viewmodel by activityViewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = EntrepreneursPembelianPageBinding.inflate(inflater, container, false)
            .also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner



        viewmodel.klaspayData.observe(viewLifecycleOwner, Observer {
            binding.txtSaldo.text = "Rp " + viewmodel.stringUtil.formatCurrency2(it.balance ?: 0)
        })

        binding.rvPembelianMenu.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = MenuAdapter(onClick = menuClickHandler)
        }

        binding.rvProduct.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = HistoryAdapter()
        }
        binding.txtTopup.setOnClickListener {
            startActivity(
                    Intent(requireActivity(), KlaspayTopupPage::class.java)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchWhenCreated {
            viewmodel.getWallet()
        }
        lifecycleScope.launchWhenCreated {
            viewmodel.loadMerchantUser().collectLatest {
                try {
                    binding.txtJumlahReviewer.text = it.amount_of_review.toString() + " review"
                    binding.txtRating.text = it.sales_rating.toString()
                } catch (E: Exception) {
                }
            }
        }
    }

    private inner class MenuAdapter(
            val listProduct: List<MenuItemTable> = viewmodel.menu_item2,
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
                binding.icon.setImageDrawable(getResources().getDrawable(item.icon));

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


    private inner class HistoryAdapter(
            val listProduct: List<Any> = emptyList()
    ) :
            RecyclerView.Adapter<HistoryVH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryVH =
                HistoryVH(parent)

        override fun getItemCount(): Int = listProduct.size

        override fun onBindViewHolder(holder: HistoryVH, position: Int) =
                holder.bind(position)
    }

    private inner class HistoryVH(
            parent: ViewGroup,
            val binding: EntrepreneursLastbuyItemMainBinding = EntrepreneursLastbuyItemMainBinding.inflate(
                    LayoutInflater.from(
                            parent.context
                    ), parent, false
            )
    ) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.lblDate.text = viewmodel.buyhistory[position].date
            var list = ArrayList<ProductBuy>()
            list.clear()
            list.addAll(viewmodel.buyhistory[position].product)

            binding.rvProduct.apply {
                layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
                adapter = ProdukAdapter(list)
            }
        }
    }

    private inner class ProdukAdapter(
            val listProduct: List<ProductBuy>
    ) :
            RecyclerView.Adapter<ProdukVH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukVH =
                ProdukVH(parent)

        override fun getItemCount(): Int = listProduct.size

        override fun onBindViewHolder(holder: ProdukVH, position: Int) =
                holder.bind(listProduct[position])
    }

    private inner class ProdukVH(
            parent: ViewGroup,
            val binding: EntrepreneursLastbuyItemBinding = EntrepreneursLastbuyItemBinding.inflate(
                    LayoutInflater.from(
                            parent.context
                    ), parent, false
            )
    ) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(productData: ProductBuy) {
            binding.productImg.clipToOutline = true
            glide.load(productData.product_img)
                    .centerCrop()
                    .into(binding.productImg)
            binding.productName.text = productData.product_name
            binding.lblJumlahProduk.text = "${productData.product_total} Produk"
            binding.lblTotalBelanja.text = "Total Belanja : ${productData.product_total_price}"

        }
    }

    private val menuClickHandler by lazy {
        { pos: Int, view: View ->
            when (pos) {
                0 -> startActivity(
                        Intent(
                                requireContext(),
                                PembelianPage::class.java
                        )
                )
                1 -> startActivity(
                        Intent(
                                requireContext(),
                                ReviewPage::class.java
                        )
                )

            }
        }
    }
}