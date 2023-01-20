package id.onklas.app.pages.pembayaran

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.KlaspayTopupFinishItemBinding
import id.onklas.app.databinding.KlaspayTopupGuideItemBinding
import id.onklas.app.databinding.PaymentGuidePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import timber.log.Timber

class PaymentGuidePage : Privatepage() {

    private val trxId by lazy { intent.getStringExtra("trxId").orEmpty() }
    private val viewmodel by viewModels<PaymentViewModel> { component.paymentVmFactory }
    private val binding by lazy { PaymentGuidePageBinding.inflate(layoutInflater) }

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
        binding.showGuide = false

        lifecycleScope.launchWhenCreated {
            viewmodel.db.payment().getSingle(trxId)?.let {
                binding.item = it
                binding.expiredDate = it.expired_at?.let { viewmodel.dateFormat.format(it) }
                binding.expiredTime = it.expired_at?.let { viewmodel.timeFormat.format(it) }
                binding.totalLabel = viewmodel.stringUtil.formatCurrency2(it.total)

                binding.buttonNumberCopy.setOnClickListener { _ ->
                    viewmodel.intentUtil.copyText(this@PaymentGuidePage, it.payment_code)
                    toast("Nomor ${it.payment_code_type} tersalin")
                }

                binding.buttonTotalCopy.setOnClickListener { _ ->
                    viewmodel.intentUtil.copyText(this@PaymentGuidePage, it.total.toString())
                    toast("Total pembayaran tersalin")
                }

                binding.textDetail.setOnClickListener {
                    PaymentDetailPage.open(this@PaymentGuidePage, trxId)
                }

                binding.labelPetunjuk.setOnClickListener { switchGuide() }
                binding.imgSwitch.setOnClickListener { switchGuide() }

                binding.listPetunjuk.adapter = guideAdapter

                viewmodel.getGuidance(it.channel)
                viewmodel.listGuidance.observe(this@PaymentGuidePage) {
                    guideAdapter.submitList(it)
                }

                binding.executePendingBindings()
            } ?: run {
                AlertDialog.Builder(this@PaymentGuidePage, R.style.DialogTheme)
                    .setTitle("Perhatian")
                    .setMessage("Data transaksi tidak ditemukan")
                    .setPositiveButton("Kembali") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .show()
            }
        }
    }

    private fun switchGuide() {
        binding.showGuide = !binding.listPetunjuk.isShown
        binding.executePendingBindings()
    }

    private val guideAdapter by lazy {
        object : ListAdapter<GuideItem, RecyclerView.ViewHolder>(
            object : DiffUtil.ItemCallback<GuideItem>() {
                override fun areItemsTheSame(
                    oldItem: GuideItem,
                    newItem: GuideItem
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: GuideItem,
                    newItem: GuideItem
                ): Boolean = oldItem == newItem

                override fun getChangePayload(
                    oldItem: GuideItem,
                    newItem: GuideItem
                ): Any = newItem.showChild
            }
        ) {
            override fun getItemViewType(position: Int): Int =
                if (getItem(position).parent == 0) 0 else 1

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder =
                if (viewType == 0) TypeViewholder(parent) else ItemViewholder(parent)

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                if (holder is TypeViewholder)
                    holder.bind(getItem(position))
                else
                    (holder as ItemViewholder).bind(getItem(position))
            }

            override fun onBindViewHolder(
                holder: RecyclerView.ViewHolder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                if (payloads.isNotEmpty() && holder is TypeViewholder)
                    holder.showChild(payloads.first() as Boolean)
                else
                    onBindViewHolder(holder, position)
            }
        }
    }

    private inner class TypeViewholder(
        parent: ViewGroup,
        val binding: KlaspayTopupGuideItemBinding = KlaspayTopupGuideItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GuideItem) {
            binding.item = item
            binding.title.setOnClickListener { select(item) }
            binding.imgSwitch.setOnClickListener { select(item) }
            binding.root.setOnClickListener { select(item) }
            showChild(item.showChild)
        }

        fun select(item: GuideItem) {
            viewmodel.listGuidance.value?.let { currentList ->
                val newList = ArrayList(currentList)
                val newItem = currentList.first { it.id == item.id }.copy()
                val newShowChild = !newItem.showChild

                val index = newList.indexOf(newItem)
                if (index > -1)
                    newList[index] = newItem.apply { showChild = newShowChild }

                if (newShowChild)
                    newList.addAll(adapterPosition + 1, item.childs)
                else
                    newList.removeAll { it.parent == item.id }

                viewmodel.listGuidance.postValue(newList)
            }
        }

        fun showChild(show: Boolean) {
            binding.showChild = show
            binding.executePendingBindings()
        }
    }

    private inner class ItemViewholder(
        parent: ViewGroup,
        val binding: KlaspayTopupFinishItemBinding = KlaspayTopupFinishItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GuideItem) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    companion object {
        const val RC = 4328
        fun open(activity: Activity, trxId: String) {
            activity.startActivityForResult(
                Intent(activity, PaymentGuidePage::class.java).putExtra(
                    "trxId", trxId
                ), RC
            )
        }
    }
}