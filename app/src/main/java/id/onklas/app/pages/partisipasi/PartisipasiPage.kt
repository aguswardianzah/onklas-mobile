package id.onklas.app.pages.partisipasi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.PartisipasiFinishItemBinding
import id.onklas.app.databinding.PartisipasiItemBinding
import id.onklas.app.databinding.PartisipasiPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.flow.collectLatest

class PartisipasiPage : Privatepage() {

    private val binding by lazy { PartisipasiPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PartisipasiViewModel> { component.partisipasiVmFactory }
    private val klaspayId by lazy { intent.getStringExtra("klaspayId") ?: "" }
    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }

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
        binding.viewmodel = viewModel
        viewModel.klaspayId = klaspayId

        binding.btnTagihan.setOnClickListener {
            if (!viewModel.isPageOnGoing)
                binding.rvItems.visibility = View.GONE

            binding.btnTagihan.setBackgroundColor(colorPrimary)
            binding.btnTagihan.setStrokeColorResource(R.color.colorPrimary)
            binding.btnTagihan.setTextColor(Color.WHITE)

            binding.btnPaid.setBackgroundColor(Color.WHITE)
            binding.btnPaid.setStrokeColorResource(R.color.gray)
            binding.btnPaid.setTextColor(colorGray)

            viewModel.isPageOnGoing = true
            viewModel.refresh()
        }

        binding.btnPaid.setOnClickListener {
            if (viewModel.isPageOnGoing)
                binding.rvItems.visibility = View.GONE

            binding.btnPaid.setBackgroundColor(colorPrimary)
            binding.btnPaid.setStrokeColorResource(R.color.colorPrimary)
            binding.btnPaid.setTextColor(Color.WHITE)

            binding.btnTagihan.setBackgroundColor(Color.WHITE)
            binding.btnTagihan.setStrokeColorResource(R.color.gray)
            binding.btnTagihan.setTextColor(colorGray)

            viewModel.isPageOnGoing = false
            viewModel.refresh()
        }

        viewModel.errorString.observe(this, Observer(this::toast))

        viewModel.loadingItem.observe(this, Observer(binding.swipeRefresh::setRefreshing))
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchData()
            viewModel.refresh()
        }

        binding.rvItems.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                customEdge = resources.getDimensionPixelSize(R.dimen._12sdp),
                includeTop = true,
                includeBottom = true
            )
        )
        binding.rvItems.adapter = partisipasiAdapter
        lifecycleScope.launchWhenCreated {
            viewModel.listItem.collectLatest {
                it.observe(this@PartisipasiPage) {
                    partisipasiAdapter.submitList(it) {
                        binding.rvItems.visibility = View.VISIBLE
                        viewModel.loadingItem.postValue(false)
                    }
                }
            }
        }

        viewModel.refresh()
        binding.executePendingBindings()
    }

    private val partisipasiAdapter by lazy {
        object : PagingAdapter<PartisipasiItem, PartisipasiVH>(object :
            DiffUtil.ItemCallback<PartisipasiItem>() {
            override fun areItemsTheSame(
                oldItem: PartisipasiItem,
                newItem: PartisipasiItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: PartisipasiItem,
                newItem: PartisipasiItem
            ): Boolean = oldItem == newItem

            override fun getChangePayload(
                oldItem: PartisipasiItem,
                newItem: PartisipasiItem
            ): Any = newItem
        }) {
            override fun getItemViewType(position: Int): Int =
                if (viewModel.isPageOnGoing) 2
                else if (!viewModel.isPageOnGoing) 3
                else super.getItemViewType(position)

            override fun createItemViewholder(parent: ViewGroup, viewType: Int): PartisipasiVH =
                if (viewType == 2)
                    OnGoingVH(parent)
                else
                    FinishedVH(parent)

            override fun bindItemViewholder(holder: PartisipasiVH, position: Int) {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }

            override fun bindItemViewholder(
                holder: PartisipasiVH,
                position: Int,
                payloads: MutableList<Any>
            ) {
                if (payloads.isNotEmpty()) {
                    holder.update(payloads.first() as PartisipasiItem)
                } else bindItemViewholder(holder, position)
            }
        }
    }

    private abstract inner class PartisipasiVH(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: PartisipasiItem)
        abstract fun update(item: PartisipasiItem)
    }

    private inner class OnGoingVH(
        parent: ViewGroup,
        val binding: PartisipasiItemBinding = PartisipasiItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : PartisipasiVH(binding.root) {

        override fun bind(item: PartisipasiItem) {
            binding.item = item
            binding.stringUtil = viewModel.stringUtil
            binding.dateFormat = viewModel.dateFormat

            binding.btnPartisipasi.setOnClickListener {
                startActivityForResult(
                    Intent(
                        this@PartisipasiPage,
                        PartisipasiAmountPage::class.java
                    ).putExtra("id", item.id), 328
                )
            }

            binding.btnHistory.setOnClickListener {
                startActivityForResult(
                    Intent(
                        this@PartisipasiPage,
                        PartisipasiHistoryPage::class.java
                    ).putExtra("id", item.id), 329
                )
            }

            update(item)
        }

        override fun update(item: PartisipasiItem) {
            binding.progressSize = item.current_amount
            binding.executePendingBindings()
        }
    }

    private inner class FinishedVH(
        parent: ViewGroup,
        val binding: PartisipasiFinishItemBinding = PartisipasiFinishItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : PartisipasiVH(binding.root) {

        override fun bind(item: PartisipasiItem) {
            binding.item = item
            binding.stringUtil = viewModel.stringUtil
            binding.dateFormat = viewModel.dateFormat

            binding.btnHistory.setOnClickListener {
                startActivityForResult(
                    Intent(
                        this@PartisipasiPage,
                        PartisipasiHistoryPage::class.java
                    ).putExtra("id", item.id), 329
                )
            }

            update(item)
        }

        override fun update(item: PartisipasiItem) {
            binding.progressSize = item.current_amount
            binding.executePendingBindings()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 328 && resultCode == RESULT_OK)
            viewModel.fetchData()
        else
            super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onStart() {
        super.onStart()

        viewModel.fetchData()
        lifecycleScope.launchWhenCreated {
            viewModel.balance.postValue(viewModel.api.klaspayWallet().data.balance)
        }
    }
}