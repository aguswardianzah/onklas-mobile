package id.onklas.app.pages.sekolah.store

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.arasthel.spannedgridlayoutmanager.SpannedGridLayoutManager
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.*
import id.onklas.app.di.component
import id.onklas.app.pages.BaseFragment
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.myMerchant.MyMerchantPage
import id.onklas.app.pages.entrepreneurs.pembelian.PembelianMainPage
import id.onklas.app.utils.GridSpacingItemDecoration
import kotlinx.coroutines.launch
import kotlin.math.max

class StoreProductPage : BaseFragment() {

    private lateinit var binding: StoreProductPageBinding
    private val viewmodel by activityViewModels<StoreVm> { component.storeVmFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }
    private val gridLayoutManager by lazy {
        SpannedGridLayoutManager(
            orientation = SpannedGridLayoutManager.Orientation.HORIZONTAL,
            spans = 2
        )
    }
    private val gridItemDecoration by lazy {
        GridSpacingItemDecoration(
            2,
            resources.getDimensionPixelSize(R.dimen._4sdp),
            true,
            resources.getDimensionPixelSize(R.dimen._8sdp)
        )
    }

//    private val items by lazy {
//        listOf(
//            StoreBannerItem(
//                "https://homepages.cae.wisc.edu/~ece533/images/airplane.png",
//            ),
//            StoreBannerItem(
//                "https://homepages.cae.wisc.edu/~ece533/images/boat.png",
//            ),
//            StoreBannerItem(
//                "https://homepages.cae.wisc.edu/~ece533/images/mountain.png",
//            ),
//            StoreBannerItem(
//                "https://homepages.cae.wisc.edu/~ece533/images/watch.png",
//            )
//        )
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = StoreProductPageBinding.inflate(inflater, container, false)
        .also { binding = it }.root


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.swipeRefresh.isRefreshing = true

        lifecycleScope.launchWhenCreated { viewmodel.loadHomePage() }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch { viewmodel.loadHomePage() }
        }

//        binding.viewpager.adapter = object :
//            FragmentPagerAdapter(requireFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
//            override fun getItem(position: Int): Fragment = items[position]
//            override fun getCount(): Int = items.size
//        }

        binding.actionToko.setOnClickListener {
            if (viewmodel.pref.getBoolean("onklas_pro"))
                startActivity(Intent(requireActivity(), MyMerchantPage::class.java))
            else
                prettyAlert(
                    showImage = true,
                    customImg = R.drawable.img_bukan_pro,
                    titleText = "Uppss, kamu belum bisa berjualan!",
                    msg = "Hubungi sekolah untuk berjualan produkmu dan dapatkan penghasilanmu sendiri",
                    okLabel = "Oke saya akan hubungi sekolah",
                    abortLabel = ""
                )
        }

        binding.icPro.visibility =
            if (viewmodel.pref.getBoolean("onklas_pro")) View.GONE else View.VISIBLE

        binding.actionPembelian.setOnClickListener {
            startActivity(Intent(requireActivity(), PembelianMainPage::class.java))
        }

        // category

        viewmodel.listCategory.observe(viewLifecycleOwner, Observer {
            categoryAdapter.submitList(it.toMutableList())
            categoryAdapter.notifyDataSetChanged()
            binding.swipeRefresh.isRefreshing = false
        })

        binding.inclCategory.rvKategori.apply {
            layoutManager = gridLayoutManager
        }

        binding.inclCategory.rvKategori.adapter = categoryAdapter
        binding.inclCategory.lihatSemua.setOnClickListener {
            CategoryPage.open(activity as AppCompatActivity)
        }

        // popular product
        binding.inclPopularProduct.imgBanner.clipToOutline = true

        binding.inclPopularProduct.lihatSemua2.setOnClickListener {
            SeeAllProduct.open(activity as AppCompatActivity, "Produk Terpopuler", "terpopuler")
        }
        viewmodel.listHomePopular.observe(viewLifecycleOwner, Observer {
            PopularProductAdapter.submitList(it)
            binding.swipeRefresh.isRefreshing = false
        })
        binding.rvProductTerpopuler.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = PopularProductAdapter
        }
        binding.inclPopularProduct.lihatSemua.setOnClickListener {
            SeeAllProduct.open(activity as AppCompatActivity, "Produk Terpopuler", "terpopuler")
        }

        // terbaru
        viewmodel.listHomeNewest.observe(viewLifecycleOwner, Observer {
            try {
                NewestProductAdapter.submitList(it.subList(0, max(it.size, 9)))
                binding.swipeRefresh.isRefreshing = false
            } catch (e: Exception) {
            }
        })

        binding.inclNewestProduct.rvProduct.apply {
            layoutManager = GridLayoutManager(
                requireContext(),
                3,
                GridLayoutManager.VERTICAL,
                false
            )
            adapter = NewestProductAdapter
            addItemDecoration(gridItemDecoration)
        }

        binding.inclNewestProduct.actionReload.setOnClickListener {
            // akal random
            viewmodel.listHomeNewest.observe(viewLifecycleOwner, Observer {
                try {
                    NewestProductAdapter.submitList(it.subList(0, max(9, it.size)).shuffled())
                    binding.swipeRefresh.isRefreshing = false
                } catch (e: Exception) {
                }
            })
        }

        //best seller
        binding.inclBestsellerProduct.lihatSemua.setOnClickListener {
            SeeAllProduct.open(activity as AppCompatActivity, "Produk Terlaris", "terlaris")
        }

        binding.inclBestsellerProduct.rvProdukTerlaris.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            adapter = ProductTerlarisHorizontalAdapter
        }
        viewmodel.listHomeBestSeller.observe(viewLifecycleOwner, Observer {
            BestSellerProdukBottomAdapter.submitList(it)
            ProductTerlarisHorizontalAdapter.submitList(it)
            binding.swipeRefresh.isRefreshing = false
        })
        binding.inclBestsellerProduct.rvProduk.apply {
            layoutManager = GridLayoutManager(
                requireContext(),
                2,
            )
            adapter = BestSellerProdukBottomAdapter
        }

        // best Price
        viewmodel.listHomeBestPrice.observe(viewLifecycleOwner, Observer {
            BestPriceProdukAdapter.submitList(it)
            binding.swipeRefresh.isRefreshing = false
        })
        binding.inclBestPriceProduk.rvProduk.apply {
            layoutManager = GridLayoutManager(
                requireContext(),
                2,
            )
            adapter = BestPriceProdukAdapter
            addItemDecoration(gridItemDecoration)
        }

        binding.inclBestPriceProduk.lihatSemua.setOnClickListener {
            SeeAllProduct.open(activity as AppCompatActivity, "Harga Terbaik", "terlaris")
        }

        // schoool (other)
        val shoolHorizontal = ArrayList<HomeProductItem>()
        viewmodel.listHomeOtherHorizontal.observe(viewLifecycleOwner, Observer {
            shoolHorizontal.addAll(it)
        })

        binding.inclSchoolProduct.rvProdukTerlaris.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            adapter = SchoolProdukAdapter(shoolHorizontal)
        }

        viewmodel.listHomeOtherVertical.observe(viewLifecycleOwner, Observer {
            SchoolVerticalProdukAdapter.submitList(it)
        })

        binding.rvProduk.apply {
            layoutManager = GridLayoutManager(
                requireContext(),
                2,
            )
            adapter = SchoolVerticalProdukAdapter
            addItemDecoration(gridItemDecoration)
        }
        binding.inclSchoolProduct.lihatSemua.setOnClickListener {
            SeeAllProduct.open(activity as AppCompatActivity, "Produk Sekolah ", "sekolah_lain ")
        }

        //rekomendasi
        viewmodel.listRecomendation.observe(viewLifecycleOwner, Observer {
            RekomendationProdukAdapter.submitList(it)
            binding.swipeRefresh.isRefreshing = false
        })

        binding.incleRekomendasi.rvProduk.apply {
            layoutManager = GridLayoutManager(
                requireContext(),
                2,
            )
            adapter = RekomendationProdukAdapter
            addItemDecoration(gridItemDecoration)
        }
    }

    private val categoryAdapter by lazy {
        object : ListAdapter<CategoryItem, CategoryViewholder>(object :
            DiffUtil.ItemCallback<CategoryItem>() {
            override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewholder =
                CategoryViewholder(parent)

            override fun onBindViewHolder(holder: CategoryViewholder, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private inner class CategoryViewholder(
        parent: ViewGroup,
        val binding: StoreCategoryItemBinding = StoreCategoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryItem) {
            binding.icon.clipToOutline = true
            glide.load(item.icon)
                .centerCrop()
                .into(binding.icon)
            binding.label.text = item.name

            binding.root.setOnClickListener {
                CategoryProductPage.open(
                    requireActivity() as Privatepage,
                    0,
                    item.id,
                    item.name
                )
            }
        }
    }


    //    --------------------------

    private inner class SchoolProdukAdapter(
        val listProduct: List<HomeProductItem>
    ) :
        RecyclerView.Adapter<SchoolProdukAdapter.SchoolViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolViewHolder {
            val view: View = if (viewType == 0) {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.store_service_product_item1, parent, false)
            } else {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.store_product_item2, parent, false)
            }
            return SchoolViewHolder(view)
        }

        override fun getItemCount(): Int = listProduct.size

        override fun onBindViewHolder(holder: SchoolViewHolder, position: Int) =
            holder.bind(position)

        override fun getItemViewType(position: Int): Int {
            return position
        }

        private inner class SchoolViewHolder(
            itemView: View,
        ) :
            RecyclerView.ViewHolder(itemView) {
            fun bind(position: Int) {
                itemView.layoutParams.height = (viewmodel.width * 0.8).toInt()

                if (position > 0) {
                    itemView.layoutParams.width = (viewmodel.width * 0.35).toInt()
//                    itemView.product_img.layoutParams.height = (viewmodel.width * 0.45).toInt()
//                    itemView.product_img.clipToOutline = true
//                    glide.load(listProduct[position].image)
//                        .centerCrop()
//                        .into(itemView.product_img)
//                    itemView.product_name.text = listProduct[position].name
//                    itemView.lbl_terjual.text =
//                        "Stock ${listProduct[position].stock}"
                    itemView.setOnClickListener {
                        DetailProduk.open(
                            activity as Privatepage,
                            listProduct[position].homeProductId
                        )
                    }
                } else {
                    itemView.layoutParams.width = (viewmodel.width * 0.65).toInt()
//                    itemView.img_banenr.clipToOutline = true
//                    itemView.img_banenr.setImageResource(R.drawable.img_banner_produk_sekolah)
//                    itemView.lihat_semua.setOnClickListener {
//                        SeeAllProduct.open(
//                            activity as AppCompatActivity,
//                            "Sekolah",
//                            "sekolah_lain"
//                        )
//                    }
                }
            }
        }
    }


    private val SchoolVerticalProdukAdapter by lazy {
        object : ListAdapter<HomeProductItem, SchoolVerticalViewholder>(object :
            DiffUtil.ItemCallback<HomeProductItem>() {
            override fun areItemsTheSame(
                oldItem: HomeProductItem,
                newItem: HomeProductItem
            ): Boolean =
                oldItem.homeProductId == newItem.homeProductId

            override fun areContentsTheSame(
                oldItem: HomeProductItem,
                newItem: HomeProductItem
            ): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): SchoolVerticalViewholder =
                SchoolVerticalViewholder(parent)

            override fun onBindViewHolder(holder: SchoolVerticalViewholder, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private inner class SchoolVerticalViewholder(
        parent: ViewGroup,
        val binding: StoreProductItem2FullHomeProductBinding = StoreProductItem2FullHomeProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeProductItem) {
            binding.productImg.clipToOutline = true

            binding.item = item
            binding.viewmodel = viewmodel
            binding.stringUtil = viewmodel.stringUtil

            binding.root.setOnClickListener {
                DetailProduk.open(activity as Privatepage, item.homeProductId)
            }

        }
    }


    //------------------------- terpopuler
    private val PopularProductAdapter by lazy {
        object : ListAdapter<HomeProductItem, PopularProductViewholder>(object :
            DiffUtil.ItemCallback<HomeProductItem>() {
            override fun areItemsTheSame(
                oldItem: HomeProductItem,
                newItem: HomeProductItem
            ): Boolean =
                oldItem.homeProductId == newItem.homeProductId

            override fun areContentsTheSame(
                oldItem: HomeProductItem,
                newItem: HomeProductItem
            ): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): PopularProductViewholder =
                PopularProductViewholder(parent)

            override fun onBindViewHolder(holder: PopularProductViewholder, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private inner class PopularProductViewholder(
        parent: ViewGroup,
        val binding: StoreProductItem1Binding = StoreProductItem1Binding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeProductItem) {

            binding.productImg.clipToOutline = true
            glide.load(viewmodel.product[position].product_img)
                .centerCrop()
                .into(binding.productImg)
            binding.productName.text = viewmodel.product[position].product_name
            binding.lblTerjual.text = "Terjual  ${viewmodel.product[position].product_sold}"

            binding.productImg.layoutParams.width = (viewmodel.width * 0.37).toInt()
            binding.productImg.layoutParams.height = (viewmodel.width * 0.23).toInt()


//            binding.icon.clipToOutline = true

            binding.item = item
            binding.viewholder = viewmodel
            binding.stringUtil = viewmodel.stringUtil

            binding.mainLayout.setOnClickListener {
                DetailProduk.open(activity as Privatepage, item.homeProductId)
            }

        }
    }


    //------------------------- terbaru
    private val NewestProductAdapter by lazy {
        object : ListAdapter<HomeProductItem, NewestProductViewholder>(object :
            DiffUtil.ItemCallback<HomeProductItem>() {
            override fun areItemsTheSame(
                oldItem: HomeProductItem,
                newItem: HomeProductItem
            ): Boolean =
                oldItem.homeProductId == newItem.homeProductId

            override fun areContentsTheSame(
                oldItem: HomeProductItem,
                newItem: HomeProductItem
            ): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): NewestProductViewholder =
                NewestProductViewholder(parent)

            override fun onBindViewHolder(holder: NewestProductViewholder, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private inner class NewestProductViewholder(
        parent: ViewGroup,
        val binding: StoreProductItem4Binding = StoreProductItem4Binding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeProductItem) {
            binding.icon.clipToOutline = true

            binding.item = item
            binding.viewholder = viewmodel
            binding.stringUtil = viewmodel.stringUtil

            binding.mainLayout.setOnClickListener {
                DetailProduk.open(activity as Privatepage, item.homeProductId)
            }

        }
    }


    //// -------------------- Produk Terlaris

    private val ProductTerlarisHorizontalAdapter by lazy {
        object : ListAdapter<HomeProductItem, Viewholder>(object :
            DiffUtil.ItemCallback<HomeProductItem>() {
            override fun areItemsTheSame(
                oldItem: HomeProductItem,
                newItem: HomeProductItem
            ): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(
                oldItem: HomeProductItem,
                newItem: HomeProductItem
            ): Boolean =
                oldItem == newItem
        }) {
            override fun getItemViewType(position: Int): Int = getItem(position)?.let {
                if (position == 0) 101 else 102
            } ?: 102

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
//                if (viewType == 101) {
//                    ProductTerlarisTopBannerVH(parent)
//                } else {
                ProdukTerlarisTopVH(parent)
//                }


            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(getItem(position))

        }
    }

    private abstract class Viewholder(view: View) : RecyclerView.ViewHolder(view) {

        abstract fun bind(item: HomeProductItem)
    }

    private inner class ProductTerlarisTopBannerVH(
        parent: ViewGroup,
        val binding: StoreProdukTerlarisItemBannerBinding = StoreProdukTerlarisItemBannerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : Viewholder(binding.root) {

        override fun bind(item: HomeProductItem) {
            binding.img1.clipToOutline = true
            binding.img2.clipToOutline = true
            binding.img3.clipToOutline = true

            glide.load("https://picsum.photos/200/300")
                .centerCrop()
                .into(binding.img1)

            glide.load("https://picsum.photos/200/300")
                .centerCrop()
                .into(binding.img2)

            glide.load("https://picsum.photos/200/300")
                .centerCrop()
                .into(binding.img3)

        }

    }


    private inner class ProdukTerlarisTopVH(
        parent: ViewGroup,
        val binding: StoreProdukTerlarisItemBinding = StoreProdukTerlarisItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : Viewholder(binding.root) {

        override fun bind(item: HomeProductItem) {
            binding.productImg.clipToOutline = true
            itemView.layoutParams.width = (viewmodel.width * 0.4).toInt()
            itemView.layoutParams.height = (viewmodel.width * 0.6).toInt()
//            itemView.product_img.layoutParams.height = (viewmodel.width * 0.34).toInt()
            binding.item = item
            binding.stringUtils = viewmodel.stringUtil

//            glide.load("https://picsum.photos/200/300")
//                .centerCrop()
//                .into(binding.productImg)

//            binding.item = item
//            binding.txtTotal.text = "Rp " + viewmodel.stringUtil.formatCurrency2(item.goody_price)

            binding.root.setOnClickListener {
                DetailProduk.open(requireActivity() as Privatepage, item.homeProductId)
            }
        }

    }


    // produk terlaris bawah

    private val BestSellerProdukBottomAdapter by lazy {
        object : ListAdapter<HomeProductItem, BestSellerProductBottomViewholder>(object :
            DiffUtil.ItemCallback<HomeProductItem>() {
            override fun areItemsTheSame(
                oldItem: HomeProductItem,
                newItem: HomeProductItem
            ): Boolean =
                oldItem.homeProductId == newItem.homeProductId

            override fun areContentsTheSame(
                oldItem: HomeProductItem,
                newItem: HomeProductItem
            ): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): BestSellerProductBottomViewholder =
                BestSellerProductBottomViewholder(parent)

            override fun onBindViewHolder(
                holder: BestSellerProductBottomViewholder,
                position: Int
            ) =
                holder.bind(getItem(position))
        }
    }

    private inner class BestSellerProductBottomViewholder(
        parent: ViewGroup,
        val binding: StoreProductItem2FullHomeProductBinding = StoreProductItem2FullHomeProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeProductItem) {
            binding.productImg.clipToOutline = true

            binding.item = item
            binding.viewmodel = viewmodel
            binding.stringUtil = viewmodel.stringUtil

            binding.mainLayout.setOnClickListener {
                DetailProduk.open(activity as Privatepage, item.homeProductId)
            }

        }
    }


    // best Price

    private val BestPriceProdukAdapter by lazy {
        object : ListAdapter<HomeProductItem, BestPriceProductViewholder>(object :
            DiffUtil.ItemCallback<HomeProductItem>() {
            override fun areItemsTheSame(
                oldItem: HomeProductItem,
                newItem: HomeProductItem
            ): Boolean =
                oldItem.homeProductId == newItem.homeProductId

            override fun areContentsTheSame(
                oldItem: HomeProductItem,
                newItem: HomeProductItem
            ): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): BestPriceProductViewholder =
                BestPriceProductViewholder(parent)

            override fun onBindViewHolder(holder: BestPriceProductViewholder, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private inner class BestPriceProductViewholder(
        parent: ViewGroup,
        val binding: StoreProductItem2FullHomeProductBinding = StoreProductItem2FullHomeProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeProductItem) {
            binding.productImg.clipToOutline = true

            binding.item = item
            binding.viewmodel = viewmodel
            binding.stringUtil = viewmodel.stringUtil

            binding.mainLayout.setOnClickListener {
                DetailProduk.open(activity as Privatepage, item.homeProductId)
            }

        }
    }


    // rekomendation


    private val RekomendationProdukAdapter by lazy {
        object : ListAdapter<HomeProductItem, RekomendationProductViewholder>(object :
            DiffUtil.ItemCallback<HomeProductItem>() {
            override fun areItemsTheSame(
                oldItem: HomeProductItem,
                newItem: HomeProductItem
            ): Boolean =
                oldItem.homeProductId == newItem.homeProductId

            override fun areContentsTheSame(
                oldItem: HomeProductItem,
                newItem: HomeProductItem
            ): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RekomendationProductViewholder =
                RekomendationProductViewholder(parent)

            override fun onBindViewHolder(holder: RekomendationProductViewholder, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private inner class RekomendationProductViewholder(
        parent: ViewGroup,
        val binding: StoreProductItem2FullHomeProductBinding = StoreProductItem2FullHomeProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeProductItem) {
            binding.productImg.clipToOutline = true

            binding.item = item
            binding.viewmodel = viewmodel
            binding.stringUtil = viewmodel.stringUtil

            binding.mainLayout.setOnClickListener {
                DetailProduk.open(activity as Privatepage, item.homeProductId)
            }

        }
    }


}