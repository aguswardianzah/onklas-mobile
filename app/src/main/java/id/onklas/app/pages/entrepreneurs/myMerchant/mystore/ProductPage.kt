package id.onklas.app.pages.entrepreneurs.myMerchant.mystore

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.MystoreProductItemBinding
import id.onklas.app.databinding.ProductPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BaseFragment
import id.onklas.app.pages.entrepreneurs.myMerchant.addProduct.AddProductPage
import id.onklas.app.pages.sekolah.store.ProductTable
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch

class ProductPage : BaseFragment() {

    private lateinit var binding: ProductPageBinding

    //    private val viewmodel by activityViewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val viewModel by activityViewModels<ProductViewModel> { component.productVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ProductPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.isLoadingProduct.observe(viewLifecycleOwner, binding.swipeRefresh::setRefreshing)

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        binding.rvProduct.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                customEdge = resources.getDimensionPixelSize(R.dimen._16sdp),
                includeBottom = true,
                includeTop = true
            )
        )
        binding.rvProduct.adapter = adapter

        viewModel.myProducts().observe(viewLifecycleOwner, Observer(adapter::submitList))

        binding.btnAddProduct.setOnClickListener {
            startActivity(Intent(requireContext(), AddProductPage::class.java))
        }

        binding.executePendingBindings()
    }

    private val adapter by lazy {
        object :
            PagingAdapter<ProductTable, ProductVH>(object : DiffUtil.ItemCallback<ProductTable>() {
                override fun areItemsTheSame(
                    oldItem: ProductTable,
                    newItem: ProductTable
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: ProductTable,
                    newItem: ProductTable
                ): Boolean = oldItem == newItem

                override fun getChangePayload(oldItem: ProductTable, newItem: ProductTable): Any =
                    newItem
            }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): ProductVH =
                ProductVH(parent)

            override fun bindItemViewholder(holder: ProductVH, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: ProductVH,
                position: Int,
                payloads: MutableList<Any>
            ) {
                if (payloads.isNotEmpty())
                    holder.bind(payloads.first() as ProductTable)
                else
                    bindItemViewholder(holder, position)
            }
        }
    }

    private inner class ProductVH(
        parent: ViewGroup,
        val binding: MystoreProductItemBinding = MystoreProductItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.productImg.clipToOutline = true
        }

        fun bind(item: ProductTable) {
            binding.txtEdit.setOnClickListener {
                alertSelect("Atur", arrayOf("Edit", "Hapus")) {
                    if (it == 0) {
                        startActivityForResult(
                            Intent(
                                requireActivity(),
                                AddProductPage::class.java
                            ).putExtra("productId", item.product_id), 320
                        )
                    } else {
                        prettyAlert(
                            true,
                            false,
                            titleText = "Konfirmasi Hapus",
                            msg = "Hapus produk ini?",
                            okLabel = "Hapus"
                        )
                    }
                }
            }
            binding.switchPublish.setOnCheckedChangeListener { buttonView, isChecked ->
                lifecycleScope.launch {
                    loading(msg = "memproses permintaan")
                    try {
                        viewModel.api.publishGood(item.product_id, mapOf("published" to isChecked))
                    } catch (e: Exception) {
                    }
                    dismissloading()
                }
            }



            binding.productImg.setOnClickListener {
                // open product detail
            }

            update(item)
        }

        fun update(item: ProductTable) {
            binding.item = item
            binding.stringUtil = viewModel.stringUtil
            binding.executePendingBindings()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 320 && resultCode == AppCompatActivity.RESULT_OK) {
            viewModel.refresh()
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }


    //    private inner class myProductAdapter(
//        val listProduct: List<MyProduct> = viewmodel.MyProduct,
//        val onClick: (Int, View) -> Unit = { _, _ -> }
//    ) :
//        RecyclerView.Adapter<myProductAdapter.myProductVh>() {
//
//        private inner class myProductVh(
//            parent: ViewGroup,
//            val binding: MystoreProductItemBinding = MystoreProductItemBinding.inflate(
//                LayoutInflater.from(
//                    parent.context
//                ), parent, false
//            )
//        ) :
//            RecyclerView.ViewHolder(binding.root) {
//            fun bind(item: MyProduct) {
////                binding.item = item
//                binding.productImg.clipToOutline = true
//                // non aktif = 0 ,ditayangkan = 1, menunggu verifikasi = 2, ditolak = 3
//                if (item.product_status == 0) {
//                    binding.txtStatus.text = "Nonaktif"
//                    binding.txtStatus.setTextColor(
//                        ContextCompat.getColor(
//                            requireContext(),
//                            R.color.textBlack
//                        )
//                    );
//                } else if (item.product_status == 1) {
//                    binding.txtStatus.text = "Ditayangkan"
//                    binding.txtStatus.setTextColor(
//                        ContextCompat.getColor(
//                            requireContext(),
//                            R.color.green
//                        )
//                    );
//                } else if (item.product_status == 2) {
//                    binding.txtStatus.text = "Menunggu Verifikasi"
//                    binding.txtStatus.setTextColor(
//                        ContextCompat.getColor(
//                            requireContext(),
//                            R.color.gold
//                        )
//                    );
//                } else {
//                    binding.txtStatus.text = "Ditolak"
//                    binding.txtStatus.setTextColor(
//                        ContextCompat.getColor(
//                            requireContext(),
//                            R.color.red
//                        )
//                    );
//                }
//                if (item.product_stock > 0) {
//                    binding.icDanger.visibility = View.GONE
//                } else {
//                    binding.icDanger.visibility = View.VISIBLE
//                }
//                binding.txtPrice.text =
//                    "Rp. ${viewmodel.stringUtil.formatCurrency2(item.product_price)}"
//
////                binding.root.setOnClickListener {
////                    onClick.invoke(
////                        adapterPosition,
////                        binding.name
////                    )
////                }
//            }
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myProductVh =
//            myProductVh(parent)
//
//        override fun getItemCount(): Int = listProduct.size
//
//        override fun onBindViewHolder(holder: myProductVh, position: Int) =
//            holder.bind(listProduct[position])
//    }
}