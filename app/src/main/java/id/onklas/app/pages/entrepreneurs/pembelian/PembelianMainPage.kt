package id.onklas.app.pages.entrepreneurs.pembelian

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.*
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.akun.ProfileViewModel
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM
import id.onklas.app.pages.entrepreneurs.MenuItemTable
import id.onklas.app.pages.entrepreneurs.ProductBuy
import id.onklas.app.pages.entrepreneurs.pembelian.purchase.PembelianPage
import id.onklas.app.pages.entrepreneurs.pembelian.review.WaitingReviewBuyer
import id.onklas.app.pages.klaspay.topup.KlaspayTopupPage
import id.onklas.app.pages.sekolah.store.CartPage
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class PembelianMainPage : Privatepage() {
    private val binding by lazy { PembelianMainPageBinding.inflate(layoutInflater) }
    private val profileVm by viewModels<ProfileViewModel> { component.profileVmFactory }

    private val viewmodel by viewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val glide by lazy { GlideApp.with(this) }
    private var firstRun = true

    private val menuItem2 = ArrayList<MenuItemTable>()
    private var adaptermenu = MenuAdapter(menuItem2)


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_cart, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        CartPage.open(this)
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.lifecycleOwner = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Pembelian"
            binding.toolbar.title = "Pembelian"
        }

        adaptermenu = MenuAdapter(menuItem2, menuClickHandler)

        lifecycleScope.launchWhenCreated {
            viewmodel.loadMerchantSummary("Purchase").collectLatest {
                try {
                    binding.summary = it
                    menuItem2.clear()
                    menuItem2.addAll(viewmodel.menu_item2)
                    menuItem2.set(0, MenuItemTable(
                            menuItem2[0].menu_id,
                            menuItem2[0].icon,
                            menuItem2[0].name,
                            "${it.purchase}",
                            menuItem2[0].satuan,
                    ))
                    menuItem2.set(1, MenuItemTable(
                            menuItem2[1].menu_id,
                            menuItem2[1].icon,
                            menuItem2[1].name,
                            "${it.reviewable_order_purchase}",
                            menuItem2[1].satuan,
                    ))

                    adaptermenu.notifyDataSetChanged()
                } catch (e: Exception) {
                }
            }
        }


        viewmodel.loadingShow.observe(this, Observer {
            if (firstRun) {
                if (it) {
                    loading()
                } else {
                    dismissloading()
                    firstRun = false
                }
            }

        })
        lifecycleScope.launchWhenCreated {
            viewmodel.loadMerchantSummary("Purchase").collectLatest {
                try {
                    binding.summary = it
                } catch (e: Exception) {
                }
            }
        }

        viewmodel.klaspayData.observe(this, Observer {
            binding.txtSaldo.text = "Rp " + viewmodel.stringUtil.formatCurrency2(it.balance ?: 0)
        })

        binding.rvPembelianMenu.apply {
            layoutManager = LinearLayoutManager(this@PembelianMainPage, RecyclerView.VERTICAL, false)
            adapter = adaptermenu
        }

        binding.rvProduct.apply {
            layoutManager = LinearLayoutManager(this@PembelianMainPage, RecyclerView.VERTICAL, false)
            adapter = HistoryAdapter()
        }
        binding.txtTopup.setOnClickListener {
            startActivity(
                Intent(this, KlaspayTopupPage::class.java)
            )
        }

        viewmodel.errorString.observe(this, Observer(this::toast))
        lifecycleScope.launchWhenCreated {
            profileVm.getUserData()
        }

        profileVm.userData.observe(this, {
            binding.userData = it
            Timber.e("profileVm userData $it")
        })
        profileVm.nisnNik.observe(this, {
            lifecycleScope.launchWhenCreated {
                profileVm.getUserData()
            }
        })
        profileVm.nisnNik.postValue(if (profileVm.userTable.nisn_nik.isNotEmpty()) profileVm.userTable.nisn_nik else profileVm.userTable.nis_nik)

    }

    override fun onPostResume() {
        super.onPostResume()
        lifecycleScope.launchWhenCreated {
            viewmodel.getWallet()
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
                layoutManager =
                    LinearLayoutManager(this@PembelianMainPage, RecyclerView.VERTICAL, false)
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
                        this,
                        PembelianPage::class.java
                    )
                )
                1 -> {
//                    prettyAlert(false, titleText = "Perhatian", msg = "Fitur sedang dalam pengembangan", abortLabel = "")
                    startActivity(
                        Intent(
                            this,
                            WaitingReviewBuyer::class.java
                        )
                    )
                }
            }
        }
    }
}
