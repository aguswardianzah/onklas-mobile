package id.onklas.app.pages.sekolah.store

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.CheckoutItemItemBinding
import id.onklas.app.databinding.CheckoutStoreItemBinding
import id.onklas.app.databinding.CheckoutStorePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.EntrepreneursPage
import id.onklas.app.pages.entrepreneurs.pembelian.PembelianMainPage
import id.onklas.app.pages.pembayaran.ConfirmPinPage

class CheckoutStorePage : Privatepage() {

    private val binding by lazy { CheckoutStorePageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<CheckoutStoreViewModel> { component.checkoutStoreVmFactory }

    init {
        lifecycleScope.launchWhenCreated {
            try {
                viewModel.klaspayBalance.postValue(viewModel.apiService.klaspayWallet().data.balance)
            } catch (e: Exception) {
            }
        }
    }

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

        binding.rvItems.adapter = adapter

        viewModel.listItems.observe(this, Observer(adapter::submitList))

        binding.shipName.setOnClickListener { selectShipping() }
        binding.shipDesc.setOnClickListener { selectShipping() }
        binding.shipPrice.setOnClickListener { selectShipping() }
        binding.shipAction.setOnClickListener { selectShipping() }

        binding.address.setOnClickListener { openAddress() }
        binding.addressAction.setOnClickListener { openAddress() }

        binding.btnPay.setOnClickListener {
            if (viewModel.address.value != null)
                ConfirmPinPage.getPin(this)
            else {
                prettyAlert(
                    titleText = "Alamat Pengiriman",
                    msg = "Mohon isikan alamat pengiriman lengkap terlebih dahulu",
                    okLabel = "Isi Alamat",
                    okClick = this::openAddress
                )
            }
        }

        viewModel.errorString.observe(this, Observer(this::toast))

        viewModel.isLoading.observe(this) {
            if (it)
                loading(msg = "Menampilkan data")
            else
                dismissloading()
        }

        binding.executePendingBindings()
    }

    private fun openAddress() =
        startActivityForResult(Intent(this, CheckoutStoreAddressPage::class.java), 283)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 283 && resultCode == RESULT_OK) {
            lifecycleScope.launchWhenCreated {
                loading(msg = "memproses data")
                viewModel.fetchShipments()
                dismissloading()
            }
        } else if (requestCode == ConfirmPinPage.RC && resultCode == RESULT_OK) {
            val pin = data?.getStringExtra("pin").orEmpty()
            lifecycleScope.launchWhenCreated {
                loading(msg = "memproses permintaan")
                val success = viewModel.buyProduct(pin)
                dismissloading()
                if (success) {
                    prettyAlert(
                        showImage = true, isSuccess = true,
                        titleText = "Pembelian produk berhasil",
                        msg = "Penjual akan segera memproses orderan kamu, mohon ditunggu ya",
                        okLabel = "Lihat Status Pembelian",
                        okClick = {
                            startActivity(
                                Intent(
                                    this@CheckoutStorePage,
                                    PembelianMainPage::class.java
                                )
                            )
                            setResult(RESULT_OK)
                            finish()
                        },
                        abortLabel = "Kembali ke Beranda",
                        abortClick = {
                            setResult(RESULT_OK)
                            finish()
                        }
                    )
                }
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private fun selectShipping() {
        CheckoutShippingPage {}.show(supportFragmentManager, "shipping")
    }

    private abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: CartPaging)
    }

    private inner class MerchantVH(
        parent: ViewGroup,
        val binding: CheckoutStoreItemBinding = CheckoutStoreItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : ViewHolder(binding.root) {

        override fun bind(item: CartPaging) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    private inner class ItemVH(
        parent: ViewGroup,
        val binding: CheckoutItemItemBinding = CheckoutItemItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : ViewHolder(binding.root) {

        override fun bind(item: CartPaging) {
            binding.item = item
            binding.stringUtil = viewModel.stringUtil
            binding.executePendingBindings()
        }
    }

    private val adapter by lazy {
        object : ListAdapter<CartPaging, ViewHolder>(object : DiffUtil.ItemCallback<CartPaging>() {
            override fun areItemsTheSame(oldItem: CartPaging, newItem: CartPaging): Boolean =
                oldItem.cart.merchant_id == newItem.cart.merchant_id && oldItem.cart.product_id == newItem.cart.product_id

            override fun areContentsTheSame(oldItem: CartPaging, newItem: CartPaging): Boolean =
                oldItem == newItem
        }) {

            override fun getItemViewType(position: Int): Int = getItem(position)?.let {
                if (it.cart.showItem) 100 else 200
            } ?: 100

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                if (viewType == 100) ItemVH(parent) else MerchantVH(parent)

            override fun onBindViewHolder(holder: ViewHolder, position: Int) =
                holder.bind(getItem(position))
        }
    }
}