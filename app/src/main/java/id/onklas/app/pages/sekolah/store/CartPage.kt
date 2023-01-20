package id.onklas.app.pages.sekolah.store

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.CartItemGoodBinding
import id.onklas.app.databinding.CartItemStoreBinding
import id.onklas.app.databinding.CartPageBinding
import id.onklas.app.databinding.EntrepreneursCostomDialogBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch
import timber.log.Timber


class CartPage : Privatepage() {
    companion object {
        fun open(activity: Activity) {
            activity.startActivity(Intent(activity, CartPage::class.java))
        }
}

    private val binding by lazy { CartPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<CartViewModel> { component.cartVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.isLoadingCart.observe(this, Observer {
            Timber.e("isloadng  $it")
//            binding.swipeRefresh::setRefreshing
            binding.swipeRefresh.isRefreshing = it
        })

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchCart()
        }

        binding.rvCart.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                binding.swipeRefresh.isEnabled =
                    (binding.rvCart.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() == 0
//                            || binding.swipeRefresh.isRefreshing
            }
        })

        binding.rvCart.adapter = adapter
        binding.rvCart.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)

                val position = parent.getChildAdapterPosition(view)

                if (position > 0 && parent.findViewHolderForAdapterPosition(position) is CartMerchantVH) {
                    outRect.top = resources.getDimensionPixelSize(R.dimen._8sdp)
                }
            }
        })

        viewModel.cartData.observe(this, Observer {
            val selected = it.filter { it.cart.showItem && it.cart.selected }
            viewModel.countSelected.postValue(selected.size)
            viewModel.totalPrice.postValue(selected.sumBy {
                it.cart.quantity * (it.product?.price ?: 0)
            })
            adapter.submitList(it)
        })

        binding.actionCheckout.setOnClickListener {
            lifecycleScope.launch {
                if (viewModel.db.cart().merchantSelected() > 1) {
                    toast("Maaf checkout hanya per 1 toko")
                } else {
                    startActivityForResult(
                        Intent(this@CartPage, CheckoutStorePage::class.java),
                        243
                    )
                }
            }
        }

        viewModel.errorString.observe(this, Observer(this::toast))

        binding.inclEmpCart.actionLihatProduct.setOnClickListener {
            finish()
        }

        binding.executePendingBindings()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 243 && resultCode == RESULT_OK) {
            finish()
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private val adapter by lazy {
        object :
            PagingAdapter<CartPaging, CartViewHolder>(object : DiffUtil.ItemCallback<CartPaging>() {
                override fun areItemsTheSame(oldItem: CartPaging, newItem: CartPaging): Boolean =
                    oldItem.cart.merchant_id == newItem.cart.merchant_id && oldItem.cart.product_id == newItem.cart.product_id

                override fun areContentsTheSame(oldItem: CartPaging, newItem: CartPaging): Boolean =
                    oldItem == newItem
            }) {

            override fun getItemViewType(position: Int): Int = getItem(position)?.let {
                if (it.cart.showItem) 100 else 200
            } ?: 100

            override fun createItemViewholder(parent: ViewGroup, viewType: Int): CartViewHolder =
                if (viewType == 100) CartItemVH(parent) else CartMerchantVH(parent)

            override fun bindItemViewholder(holder: CartViewHolder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: CartViewHolder,
                position: Int,
                payloads: MutableList<Any>
            ) = bindItemViewholder(holder, position)
        }
    }

    private abstract class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: CartPaging)
        abstract fun update(selected: Boolean, quantity: Int)
    }

    private inner class CartMerchantVH(
        parent: ViewGroup,
        val binding: CartItemStoreBinding = CartItemStoreBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : CartViewHolder(binding.root) {

        override fun bind(item: CartPaging) {
            binding.item = item

            binding.check.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.selectItem(item, isChecked)
            }

            binding.root.setOnClickListener {
                SellerProfilePage.open(
                    this@CartPage,
                    item.cart.merchant_name,
                    item.cart.merchant_id
                )
            }

            update(item.cart.selected, item.cart.quantity)
        }

        override fun update(selected: Boolean, quantity: Int) {
            binding.selected = selected
            binding.executePendingBindings()
        }
    }

    private inner class CartItemVH(
        parent: ViewGroup,
        val bindingGood: CartItemGoodBinding = CartItemGoodBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : CartViewHolder(bindingGood.root) {

        override fun bind(item: CartPaging) {
            bindingGood.item = item
            bindingGood.stringUtil = viewModel.stringUtil

            bindingGood.check.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.selectItem(item, isChecked)
            }

            bindingGood.actionMin.setOnClickListener {
                if (item.cart.quantity > 1) {
                    // decrease quantity
                    lifecycleScope.launch {
                        loading("memproses permintaan")
                        val newCart = item.cart.copy(quantity = item.cart.quantity - 1)
                        viewModel.db.cart().insert(newCart)
                        viewModel.apiWrapper.updateCart(newCart.product_id, newCart.quantity)
                        dismissloading()
                        binding.executePendingBindings()
                    }
                } else {
                    // show dialog delete cart item
                    button2Dialog(item)
                }
            }

            bindingGood.actionPlus.setOnClickListener {
                lifecycleScope.launch {
                    if (item.cart.quantity < item.product?.stock ?: 0) {
                        loading("memproses permintaan")
                        val newCart = item.cart.copy(quantity = item.cart.quantity + 1)
                        viewModel.db.cart().insert(newCart)
                        viewModel.apiWrapper.updateCart(newCart.product_id, newCart.quantity)
                        dismissloading()
                    } else
                        alert(msg = "Jumlah barang tidak dapat melebihi stok")
                }
            }
            bindingGood.root.setOnClickListener {
                item.product?.product_id?.let { it1 -> DetailProduk.open(this@CartPage, it1) }
            }

            update(item.cart.selected, item.cart.quantity)
        }

        override fun update(selected: Boolean, quantity: Int) {
            bindingGood.selected = selected
            bindingGood.quantity = quantity
            bindingGood.executePendingBindings()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun button2Dialog(item: CartPaging) {
        val bindingDialog = EntrepreneursCostomDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(bindingDialog.root)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

        bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_danger) }

        bindingDialog.txtTitle.text = "Konfirmasi Hapus"
        bindingDialog.txtDesc.text = "Produk akan dihapus dari keranjang belanja"

        bindingDialog.button2.root.visibility = View.VISIBLE
        bindingDialog.button2.btnDone.text = "Batalkan"
        bindingDialog.button2.btnAction.text = "Hapus"
        bindingDialog.button2.btnAction.setOnClickListener {
            lifecycleScope.launch {
                loading(msg = "Proses menghapus")
                viewModel.db.cart().delete(item.cart)
                if (viewModel.db.cart().countByMerchant(item.cart.merchant_id) == 1)
                    viewModel.db.cart().deleteMerchant(item.cart.merchant_id)
                try {
                    viewModel.api.deleteCart(item.cart.product_id)
                    val count = viewModel.db.cart().countCart()
                    viewModel.countCart.postValue(count)
                } catch (e: Exception) {
                    Timber.e(e)
                }

                dismissloading()
            }
            dialog.dismiss()
        }
        bindingDialog.button2.btnDone.setOnClickListener {
            dialog.dismiss()
        }
    }
}