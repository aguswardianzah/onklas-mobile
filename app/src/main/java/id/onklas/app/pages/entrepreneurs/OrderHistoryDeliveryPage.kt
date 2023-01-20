package id.onklas.app.pages.entrepreneurs

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.OrderHistoryDeliveryItemBinding
import id.onklas.app.databinding.OrderHistoryDeliveryPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

class OrderHistoryDeliveryPage : Privatepage() {

    companion object {
        fun open(activity: Privatepage, TransactionId:Int) {
            activity.startActivity(
                    Intent(activity, OrderHistoryDeliveryPage::class.java)
                            .putExtra("transactionId",TransactionId)
            )
        }
    }

    private val binding by lazy { OrderHistoryDeliveryPageBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodel by viewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private var transaksiId  = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        transaksiId = intent.getIntExtra("transactionId",0)

        Timber.e("transaksi id----- $transaksiId")


        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Detail Pengiriman "
            binding.toolbar.title = "Detail Pengiriman"
        }

        binding.rvDeliveryHistory.apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryDeliveryPage, RecyclerView.VERTICAL, false)
            adapter = ProductAdapter
        }

        lifecycleScope.launchWhenCreated {
            viewmodel.loadTrackingDetail(transaksiId).collectLatest {
                try {
                    ProductAdapter.submitList(it)
                } catch (e: Exception) { }
            }
        }

    }

    private val ProductAdapter by lazy {
        object : ListAdapter<TrackingDetail, ProductViewholder>(object : DiffUtil.ItemCallback<TrackingDetail>() {
            override fun areItemsTheSame(oldItem: TrackingDetail, newItem: TrackingDetail): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: TrackingDetail, newItem: TrackingDetail): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewholder =
                ProductViewholder(parent)

            override fun onBindViewHolder(holder: ProductViewholder, position: Int) =
                holder.bind(getItem(position),position)

        }
    }
    private inner class ProductViewholder(
        parent: ViewGroup,
        val binding: OrderHistoryDeliveryItemBinding = OrderHistoryDeliveryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TrackingDetail, position: Int) {
//            Timber.e("adapter list $item")
            binding.item  = item
            if (position == 0){
                binding.imgDot.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary),
                    PorterDuff.Mode.SRC_ATOP)
            }else{
                binding.imgDot.getBackground().setColorFilter(getResources().getColor(R.color.gray),
                    PorterDuff.Mode.SRC_ATOP)
            }
        }

    }
}