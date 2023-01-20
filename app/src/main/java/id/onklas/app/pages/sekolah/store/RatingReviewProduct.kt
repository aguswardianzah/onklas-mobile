package id.onklas.app.pages.sekolah.store

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.BottomSheetAddCartBinding
import id.onklas.app.databinding.RatingFilterItemBinding
import id.onklas.app.databinding.RatingReviewItemBinding
import id.onklas.app.databinding.RatingReviewProductBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.DetailReviewPage
import id.onklas.app.pages.entrepreneurs.pembelian.review.AddReviewBuyerPage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RatingReviewProduct : Privatepage() {

    companion object {
        fun open(activity: AppCompatActivity, goodieId: Int,stock:Int,merchantId:Int) {
            activity.startActivity(
                    Intent(activity, RatingReviewProduct::class.java)
                            .putExtra("goodieId", goodieId)
                            .putExtra("stock",stock)
                            .putExtra("merchantId",merchantId)
            )
        }
    }

    private val binding by lazy { RatingReviewProductBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodel by viewModels<StoreVm> { component.storeVmFactory }
    val dateFormat by lazy { SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id")) }

    private var reviewFlter = ArrayList<ReviewFilter>()
    private val colorGray by lazy { ResourcesCompat.getColor(resources, R.color.bg_gray, null) }
    private val darkGray by lazy { ResourcesCompat.getColor(resources, R.color.gray, null) }
    private val colorGold by lazy { ResourcesCompat.getColor(resources, R.color.gold, null) }

    private var goodieId = 0
    private var starFilter = "all"
    private var merchantId = 0

    private var stock = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        goodieId = intent.getIntExtra("goodieId", 0)
        stock = intent.getIntExtra("stock",0)
        merchantId = intent.getIntExtra("merchantId", 0)


        binding.merchantId = merchantId
        binding.perfMerchantId = viewmodel.pref.getString("merchantId").toInt()

        lifecycleScope.launchWhenCreated {
            viewmodel.loadDetailReview(goodieId)
            viewmodel.loadReviewList(goodieId, starFilter)
        }

        viewmodel.goodieDetailReview.observe(this, Observer {
            binding.txtAverangeRating.text = viewmodel.stringUtil.limitComma(it.average_rating)
            binding.ratingBar.rating = it.average_rating.toFloat()
            binding.txtJumlahReviewer.text = it.jumlah_reviewer.toString()
        })

        viewmodel.goodieListReview.observe(this, Observer {
            reviewProduct.submitList(it)
        })


        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Review dan Rating "
            binding.toolbar.title = "Review dan Rating"
        }

        binding.rvReview.apply {
            layoutManager = LinearLayoutManager(this@RatingReviewProduct, RecyclerView.VERTICAL, false)
            adapter = reviewProduct
        }

        reviewFlter.addAll(viewmodel.reviewFilter)

        FilterAdapter.submitList(reviewFlter)

        binding.rvFilter.apply {
            layoutManager = LinearLayoutManager(this@RatingReviewProduct, RecyclerView.HORIZONTAL, false)
            adapter = FilterAdapter
        }
        lifecycleScope.launchWhenCreated {
            viewmodel.loadDetailGoodie(goodieId)
        }

        binding.actionBuyNow.setOnClickListener {
            if (stock > 0) {
                bottomSeetAddtoCart(
                        this,
                        layoutInflater,
                        "Beli Produk",
                        0
                )
            }else{
                alert(msg = "Stok produk Kosong")
            }

        }
        binding.actionAddCart.setOnClickListener {
            if (stock > 0) {
                bottomSeetAddtoCart(
                        this,
                        layoutInflater,
                        "Tambah ke Keranjang",
                        1
                )
            }else{
                alert(msg = "Stok produk Kosong")
            }

        }


    }


    private val reviewProduct by lazy {
        object : ListAdapter<ListReviewItem, ReviewViewholder>(object : DiffUtil.ItemCallback<ListReviewItem>() {
            override fun areItemsTheSame(oldItem: ListReviewItem, newItem: ListReviewItem): Boolean =
                    oldItem == newItem

            override fun areContentsTheSame(oldItem: ListReviewItem, newItem: ListReviewItem): Boolean =
                    oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewholder =
                    ReviewViewholder(parent)

            override fun onBindViewHolder(holder: ReviewViewholder, position: Int) =
                    holder.bind(getItem(position), position)

        }
    }

    private inner class ReviewViewholder(
            parent: ViewGroup,
            val binding: RatingReviewItemBinding = RatingReviewItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ListReviewItem, position: Int) {
            binding.item = item

            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val outputFormat = SimpleDateFormat("dd-MM-yyyy")
            val date = inputFormat.parse(item.date)
            val formattedDate = outputFormat.format(date)
            //println(formattedDate) // prints 10-04-2018


            binding.txtDate.text = formattedDate

//            binding.root.setOnClickListener {
//                DetailReviewPage.open(
//                        this@RatingReviewProduct,
//                        "",
//                        "",
//                        "",
//                        "",
//                        "",
//                )
//            }
        }
    }


    private val FilterAdapter by lazy {
        object : ListAdapter<ReviewFilter, FilterViewholder>(object : DiffUtil.ItemCallback<ReviewFilter>() {
            override fun areItemsTheSame(oldItem: ReviewFilter, newItem: ReviewFilter): Boolean =
                    oldItem == newItem

            override fun areContentsTheSame(oldItem: ReviewFilter, newItem: ReviewFilter): Boolean =
                    oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewholder =
                    FilterViewholder(parent)

            override fun onBindViewHolder(holder: FilterViewholder, position: Int) =
                    holder.bind(getItem(position), position)

        }
    }

    private inner class FilterViewholder(
            parent: ViewGroup,
            val binding: RatingFilterItemBinding = RatingFilterItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReviewFilter, position: Int) {
            if (item.id == 1) {
                binding.star.apply { visibility = View.GONE }
            } else {
                binding.star.apply { visibility = View.VISIBLE }
            }
            binding.txtFilter.text = item.name
            if (item.is_selected) {
                binding.root.apply {
                    setBackgroundResource(R.drawable.border_gold_radius6)
                }
                binding.star.apply { setColorFilter(colorGold) }
                binding.txtFilter.apply { setTextColor(colorGold) }
            } else {
                binding.root.apply {
                    setBackgroundResource(R.drawable.border_gray_radius6)
                }
                binding.star.apply { setColorFilter(colorGray) }
                binding.txtFilter.apply { setTextColor(darkGray) }
            }

            binding.root.setOnClickListener {
                if (reviewFlter.find { it.is_selected } != null) {
                    val Item = reviewFlter.find { it.is_selected }
                    val posisiItem = reviewFlter.indexOf(Item)
                    val olddata = reviewFlter[posisiItem]
                    reviewFlter.set(posisiItem, ReviewFilter(
                            olddata.id,
                            olddata.name,
                            olddata.filter_code,
                            false,
                    )
                    )
                    FilterAdapter?.notifyItemChanged(posisiItem)
                }


                reviewFlter.set(position,
                        ReviewFilter(
                                item.id,
                                item.name,
                                item.filter_code,
                                true,
                        )
                )
                FilterAdapter?.notifyItemChanged(position)

                starFilter = item.filter_code
                lifecycleScope.launch {
                    viewmodel.loadReviewList(goodieId, starFilter)
                }

            }

        }

    }


    fun bottomSeetAddtoCart(
            context: Context,
            layoutInflater: LayoutInflater,
            judul: String,
            position: Int
    ) {
        val dialog = BottomSheetDialog(context)
        val bindingAddCart by lazy { BottomSheetAddCartBinding.inflate(layoutInflater) }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = bindingAddCart.root
        var stock = 0
        var goodieId = 0

        viewmodel.goodieDetailProduk.observe(this, Observer {
            bindingAddCart.imgProduct.clipToOutline = true
            bindingAddCart.item = it
            bindingAddCart.stringUtil = viewmodel.stringUtil
            bindingAddCart.viewholder = viewmodel
            stock = it.stock
            goodieId = it.id
        })

        bindingAddCart.txtJudul.text = judul
        bindingAddCart.actionExit.setOnClickListener {
            dialog.dismiss()
        }
        bindingAddCart.actionMin.setOnClickListener {
            var data = bindingAddCart.txtQty.text.toString().toInt()
            if (data > 1) {
                bindingAddCart.txtQty.text = (data - 1).toString()
            }
        }
        bindingAddCart.actionPlus.setOnClickListener {
            var data = bindingAddCart.txtQty.text.toString().toInt()
            if (data < stock) {
                if (data > 0) {
                    bindingAddCart.txtQty.text = (data + 1).toString()

                }
            } else {
                alert(msg = "Jumlah barang tidak dapat melebihi stok")
            }
        }
        bindingAddCart.actionAddCart.setOnClickListener {
            if (stock > 0) {
                var data = bindingAddCart.txtQty.text.toString().toInt()
                lifecycleScope.launch {
                    loading(msg = "memproses permintaan")
                    viewmodel.apiWrapper.addToCart(
                            goodieId,
                            data
                    )
                    dismissloading()
                    dialog.dismiss()
                    if (position == 0) {
                        Handler().postDelayed({
                            CartPage.open(this@RatingReviewProduct)
                        }, 800)
                    } else {
                        dialog.dismiss()
                    }
                }
            } else {
                alert(msg = "Stok produk Kosong")
            }
        }
        dialog.setContentView(view)
        dialog.show()
    }
}