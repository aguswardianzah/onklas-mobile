package id.onklas.app.pages.sekolah.store

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.*
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import kotlinx.coroutines.launch

class StoreSearchPage : Privatepage() {

    private val binding by lazy { StoreSearchPageBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodel by viewModels<StoreVm> { component.storeVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.inName.requestFocus()
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        binding.inName.doAfterTextChanged {
            Handler().postDelayed({
                lifecycleScope.launch {
                    viewmodel.loadSuggestProduk(binding.inName.text.toString())
                    viewmodel.loadSuggestMerchant(binding.inName.text.toString())
                }
            }, 800)

        }
        binding.allEmpty = true // kondiis pertamakali
        binding.merchantIsEmpty = true

        binding.actClear.setOnClickListener {
            binding.inName.setText("")
        }

        viewmodel.suggestProductLData.observe(this, Observer {
            binding.productIsEmpty = it.isEmpty()
            ProductSuggestAdapter.submitList(it)
        })


        viewmodel.suggestMerchantLData.observe(this, Observer {
            try {
                binding.merchantIsEmpty = it.isEmpty()
                var merchatSuggestItem = ArrayList<SuggestMerchantItem>()
                merchatSuggestItem.clear()
                for (i in 0 until 5){
                    merchatSuggestItem.add(it[i])
                }
                MerchantSuggestAdapter.submitList(merchatSuggestItem)
            }catch (e:Exception){}

        })


        binding.allEmpty = binding.productIsEmpty == true && binding.merchantIsEmpty == true


        binding.empLay.imgEmp.apply { setImageDrawable(resources.getDrawable(R.drawable.ic_emp_suggest)) }
        binding.empLay.txtTitle.text ="Pencarian Tidak Ditemukan"
        binding.empLay.txtSubtitle.text = "Koreksi kata pencarian atau ubah menggunakan kata yang lainnya"
        binding.empLay.actionBtn.visibility = View.INVISIBLE

        binding.rvProductSuggest.apply {
            layoutManager = LinearLayoutManager(this@StoreSearchPage, RecyclerView.VERTICAL, false)
            adapter = ProductSuggestAdapter
        }
        binding.rvSellerSuggest.apply {
            layoutManager = LinearLayoutManager(this@StoreSearchPage, RecyclerView.VERTICAL, false)
            adapter = MerchantSuggestAdapter
        }
        binding.actionSeeAllSeller.setOnClickListener {
            bottomSeetFilter(layoutInflater)
        }

    }

    private val ProductSuggestAdapter by lazy {
        object : ListAdapter<SuggestProductItem, ProductViewholder>(object : DiffUtil.ItemCallback<SuggestProductItem>() {
            override fun areItemsTheSame(oldItem: SuggestProductItem, newItem: SuggestProductItem): Boolean =
                    oldItem == newItem

            override fun areContentsTheSame(oldItem: SuggestProductItem, newItem: SuggestProductItem): Boolean =
                    oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewholder =
                    ProductViewholder(parent)

            override fun onBindViewHolder(holder: ProductViewholder, position: Int) =
                    holder.bind(getItem(position), position)

        }
    }

    private inner class ProductViewholder(
            parent: ViewGroup,
            val binding: StoreSearchSuggestProductItemBinding = StoreSearchSuggestProductItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SuggestProductItem, position: Int) {
            binding.txtSearch.text = item.name

            binding.root.setOnClickListener {
                StoreSearchResultPage.open(
                        this@StoreSearchPage,
                        item.name
                )
            }
        }

    }


    private val MerchantSuggestAdapter by lazy {
        object : ListAdapter<SuggestMerchantItem, MerchantViewholder>(object : DiffUtil.ItemCallback<SuggestMerchantItem>() {
            override fun areItemsTheSame(oldItem: SuggestMerchantItem, newItem: SuggestMerchantItem): Boolean =
                    oldItem == newItem

            override fun areContentsTheSame(oldItem: SuggestMerchantItem, newItem: SuggestMerchantItem): Boolean =
                    oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MerchantViewholder =
                    MerchantViewholder(parent)

            override fun onBindViewHolder(holder: MerchantViewholder, position: Int) =
                    holder.bind(getItem(position), position)

        }
    }

    private inner class MerchantViewholder(
            parent: ViewGroup,
            val binding: StoreSearchSuggestSellerItemBinding = StoreSearchSuggestSellerItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SuggestMerchantItem, position: Int) {

            binding.img.clipToOutline = true
            glide.load(item.avatar)
                    .centerCrop()
                    .into(binding.img)
            binding.txtName.text = item.name
            binding.txtSchool.text = item.school_name

            binding.root.setOnClickListener {
                SellerProfilePage.open(
                        this@StoreSearchPage,
                        item.name,
                        item.id,
                )
            }
        }

    }



    fun bottomSeetFilter(layoutInflater: LayoutInflater) {
        val dialog = BottomSheetDialog(this)
        val bindingDialog by lazy { BottomSeetAllSellerBinding.inflate(layoutInflater) }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = bindingDialog.root

        viewmodel.suggestMerchantLData.observe(this, Observer {
            MerchantSuggestAdapter.submitList(it)
        })
        bindingDialog.rvSellerSuggest.apply {
            layoutManager = LinearLayoutManager(view.context, RecyclerView.VERTICAL, false)
            adapter = MerchantSuggestAdapter
        }
        setSupportActionBar(bindingDialog.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            bindingDialog.toolbar.setNavigationOnClickListener { dialog.dismiss() }
            title = "Penjual"
            bindingDialog.toolbar.title = "Penjual"
        }

        dialog.setContentView(view)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val bottomSheetDialog = dialog
        val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        parentLayout?.let { it ->
            val behaviour = BottomSheetBehavior.from(it)
            setupFullHeight(it)
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }


}