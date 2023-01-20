package id.onklas.app.pages.entrepreneurs.myMerchant

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yalantis.ucrop.UCrop
import id.onklas.app.R
import id.onklas.app.databinding.*
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM
import id.onklas.app.pages.entrepreneurs.MenuItemTable
import id.onklas.app.pages.entrepreneurs.myMerchant.IncomingOrder.IncomingOrderMainPage
import id.onklas.app.pages.entrepreneurs.myMerchant.Review.WaitingReviewPage
import id.onklas.app.pages.entrepreneurs.myMerchant.RiwayatOrder.HistoryOrderPage
import id.onklas.app.pages.entrepreneurs.myMerchant.addProduct.AddProductPage
import id.onklas.app.pages.entrepreneurs.myMerchant.income.IncomePage
import id.onklas.app.pages.entrepreneurs.myMerchant.mystore.Mystore
import id.onklas.app.pages.sekolah.store.DetailProduk
import id.onklas.app.pages.sekolah.store.ProductMerchantTable
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.PagingAdapter
import id.onklas.app.utils.hideKeyboard
import id.onklas.app.utils.showKeyboard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

class MyMerchantPage : Privatepage() {

    private val binding by lazy { MyMerchantPageBinding.inflate(layoutInflater) }

    private val viewmodel by viewModels<EntrepreneursVM> { component.entrepreneursFactory }

    private val menuItem = ArrayList<MenuItemTable>()

    private var firstLoad = true
    private var firstRun = true

    private var adaptermenu = MenuAdapter(menuItem)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_pro, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menu_calender)?.isVisible = !viewmodel.pref.getBoolean("onklas_pro")
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.empPenjualan.imgEmp.setBackgroundResource(R.drawable.ic_emp_penjualan)
        binding.empPenjualan.txtTitle.text = "Belum Ada Penjualan"
        binding.empPenjualan.txtSubtitle.text = "Yuk tambah produkmu dan mulai berjualan"

        adaptermenu = MenuAdapter(menuItem, menuClickHandler)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        viewmodel.loadingShow.observe(this, {
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
            viewmodel.loadMerchantSummary("MyMerchant").collectLatest {
                try {
                    binding.summary = it
                    menuItem.clear()
                    menuItem.addAll(viewmodel.menu_item)
                    menuItem.set(
                        0, MenuItemTable(
                            menuItem[0].menu_id,
                            menuItem[0].icon,
                            menuItem[0].name,
                            "${it.product} Produk",
                            menuItem[0].satuan,
                        )
                    )
                    menuItem.set(
                        1, MenuItemTable(
                            menuItem[1].menu_id,
                            menuItem[1].icon,
                            menuItem[1].name,
                            "Rp " + viewmodel.stringUtil.formatCurrency2(it.incoming_amount),
                            menuItem[1].satuan,
                        )
                    )
                    menuItem.set(
                        2, MenuItemTable(
                            menuItem[2].menu_id,
                            menuItem[2].icon,
                            menuItem[2].name,
                            "${it.incoming_order}",
                            menuItem[2].satuan,
                        )
                    )
                    menuItem.set(
                        3, MenuItemTable(
                            menuItem[3].menu_id,
                            menuItem[3].icon,
                            menuItem[3].name,
                            "${it.review} ",
                            menuItem[3].satuan,
                        )
                    )
                    menuItem.set(
                        4, MenuItemTable(
                            menuItem[4].menu_id,
                            menuItem[4].icon,
                            menuItem[4].name,
                            "${it.history_order} ",
                            menuItem[4].satuan,
                        )
                    )
                    adaptermenu.notifyDataSetChanged()
                } catch (e: Exception) {
                }
            }
        }

        binding.rvPenjualanMenu.apply {
            layoutManager = LinearLayoutManager(this@MyMerchantPage, RecyclerView.VERTICAL, false)
            adapter = adaptermenu
        }

        binding.rvProduct.apply {
            layoutManager = LinearLayoutManager(this@MyMerchantPage, RecyclerView.VERTICAL, false)
            adapter = ProductAdapter
        }
        binding.btnAddProduct.setOnClickListener {
            if (viewmodel.pref.getBoolean("has_store")) {
                startActivity(Intent(this@MyMerchantPage, AddProductPage::class.java))
            } else {
                editMerchantDialog("Masukkan Nama Toko", false)
            }
        }

        binding.actionEdit.setOnClickListener {
            editMerchantDialog("Edit Nama Toko")
        }

        viewmodel.editMerchantResponse.observe(this, {
            lifecycleScope.launchWhenCreated {
                viewmodel.loadMerchantUser().collectLatest {
                    try {
                        binding.item = it
                    } catch (e: Exception) {
                    }
                }
            }
        })

        viewmodel.createMerchantResponse.observe(this, Observer {
            lifecycleScope.launchWhenCreated {
                viewmodel.loadMerchantUser().collectLatest {
                    try {
                        binding.item = it
                    } catch (e: Exception) {
                    }
                }
            }
        })



        binding.img.setOnClickListener {
            viewmodel.intentUtil.openGalleryPhoto(this, "Profile Toko")
        }
        viewmodel.errorString.observe(this, Observer {
            toast(it)
//            if (it.isNotEmpty()) {
//                binding.actionEdit.visibility = View.GONE
//            }
        })

        Timber.e("onklas pro ${viewmodel.pref.getBoolean("onklas_pro")}")
        if (!viewmodel.pref.getBoolean("onklas_pro")) {
            bukanProDialog()
        }
        if (!viewmodel.pref.getBoolean("has_store")) {
            editMerchantDialog("Masukkan Nama Toko", false)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IntentUtil.RC_GALLERY_PHOTO) {
            data?.data?.let { imageEditor(it) }

        } else if (requestCode == UCrop.REQUEST_CROP && data != null) {
            UCrop.getOutput(data)?.let { uri ->
                Timber.e("uri path .tostring -------------  ${uri.path.toString()}")
                lifecycleScope.launch {
                    viewmodel.editImgMerchantUser(uri.path.toString())
                }
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private val colorTextBlack by lazy { ContextCompat.getColor(this, R.color.textBlack) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private fun imageEditor(uri: Uri) {
        Timber.e("uri: $uri")
        UCrop.of(uri, Uri.fromFile(File(cacheDir, "${System.currentTimeMillis()}.jpg")))
            .withOptions(UCrop.Options().apply {
                setCompressionFormat(Bitmap.CompressFormat.JPEG)
                setCompressionQuality(80)
                setShowCropGrid(false)
                withMaxResultSize(720, 720)
                withAspectRatio(1f, 1f)
                setDimmedLayerColor(Color.parseColor("#99FFFFFF"))
                setRootViewBackgroundColor(Color.WHITE)
                setShowCropFrame(false)
                setStatusBarColor(Color.WHITE)
                setToolbarColor(Color.WHITE)
                setActiveControlsWidgetColor(colorPrimary)
                setToolbarWidgetColor(colorTextBlack)
                setToolbarTitle("Atur Gambar")
            })
            .start(this)
    }


    override fun onPostResume() {
        super.onPostResume()
        lifecycleScope.launchWhenCreated {
            viewmodel.loadMerchantUser().collectLatest {
                try {
                    binding.item = it
                } catch (e: Exception) {
                    Timber.e("load merchant uset $e")
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
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
                binding.icon.setImageResource(item.icon)
                binding.name.text = item.name
                binding.data.text = item.data
                binding.root.setOnClickListener {
                    if (!viewmodel.pref.getBoolean("has_store")) {
                        editMerchantDialog("Masukkan Nama Toko", false)
                    }
                    if (viewmodel.pref.getBoolean("onklas_pro") && viewmodel.pref.getBoolean("has_store")) {
                        onClick.invoke(
                            adapterPosition,
                            binding.name
                        )
                    }

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
            observe(this@MyMerchantPage, listObserver)
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
        object : PagingAdapter<ProductMerchantTable, ProdukVH>(object :
            DiffUtil.ItemCallback<ProductMerchantTable>() {
            override fun areItemsTheSame(
                oldItem: ProductMerchantTable,
                newItem: ProductMerchantTable
            ): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(
                oldItem: ProductMerchantTable,
                newItem: ProductMerchantTable
            ): Boolean =
                oldItem == newItem
        }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): ProdukVH =
                ProdukVH(parent)


            override fun bindItemViewholder(holder: ProdukVH, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: ProdukVH,
                position: Int,
                payloads: MutableList<Any>
            ) {
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
            binding.stringUtil = viewmodel.stringUtil

//            binding.lblHarga.text = "Rp. " + viewmodel.stringUtil.formatCurrency2(item.price)

            binding.root.setOnClickListener {
                DetailProduk.open(this@MyMerchantPage as Privatepage, item.product_id)
            }
        }
    }

    private val menuClickHandler by lazy {
        { pos: Int, view: View ->
            when (pos) {
                0 -> startActivity(
                    Intent(
                        this@MyMerchantPage,
                        Mystore::class.java
                    )
                )
                1 -> startActivity(
                    Intent(
                        this@MyMerchantPage,
                        IncomePage::class.java
                    )
                )
                2 -> startActivity(
                    Intent(
                        this@MyMerchantPage,
                        IncomingOrderMainPage::class.java
                    )
                )
                3 -> startActivity(
                    Intent(
                        this@MyMerchantPage,
                        WaitingReviewPage::class.java
                    )
                )
                4 -> startActivity(
                    Intent(
                        this@MyMerchantPage,
                        HistoryOrderPage::class.java
                    )
                )
            }
        }
    }

    private fun editMerchantDialog(
        title: String,
        edit: Boolean = true

    ) {
        val bindingDialog = EditMerchantDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(bindingDialog.root)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }
        bindingDialog.txtTitle.text = title
        bindingDialog.inName.hint = "Masukkan Nama Toko"
        bindingDialog.inName.text?.let { bindingDialog.inName.setSelection(it.length) }
        showKeyboard()

        bindingDialog.inName.setText(binding.username.text.toString())
        bindingDialog.actionKonfirmasi.setOnClickListener {
            if (bindingDialog.inName.text?.isNotEmpty() == true) {
                lifecycleScope.launch {
                    if (edit) {
                        viewmodel.editMerchantUser(bindingDialog.inName.text.toString())
                    } else {
                        viewmodel.createMerchantUser(bindingDialog.inName.text.toString())
                    }
                }
                dialog.dismiss()
                hideKeyboard()
            } else {
                toast("Nama Toko Tidak Boleh Kosong")
            }
        }

        bindingDialog.actionBatal.setOnClickListener {
            dialog.dismiss()
            hideKeyboard()
        }
    }


    private fun bukanProDialog() {
        val bindingDialog = KwuBukkanproDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(bindingDialog.root)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

        bindingDialog.btnDone.setOnClickListener {
            dialog.dismiss()
        }
    }
}