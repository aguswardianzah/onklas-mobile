package id.onklas.app.pages.pembayaran.spp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.DialogActivateKlaspayBinding
import id.onklas.app.databinding.SppTagihanItemBinding
import id.onklas.app.databinding.SppTagihanPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.klaspay.tagihan.KlaspayTagihanPage
import id.onklas.app.pages.pembayaran.CheckoutPage
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch
import timber.log.Timber

class SppTagihanPage : Fragment() {

    private lateinit var binding: SppTagihanPageBinding
    private val viewmodel by activityViewModels<SppViewModel> { component.sppVmFactory }
    private var firstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = SppTagihanPageBinding.inflate(inflater, container, false).also { binding = it }.root

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewmodel

        binding.swipeRefresh.setOnRefreshListener {
            refresh()
        }

        binding.rvSpp.adapter = adapter
        binding.rvSpp.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(
                    R.dimen._8sdp
                ), includeTop = true, includeBottom = true
            )
        )

        viewmodel.listTagihan.observe(viewLifecycleOwner, {
            adapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager =
                        (binding.rvSpp.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvSpp.scrollToPosition(position)
                    }
                } else {
                    binding.rvSpp.scrollToPosition(0)
                    firstLoad = false
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        })

        viewmodel.checkedIds.observe(viewLifecycleOwner, {
            Timber.e("checkedIds: $it")
            // update checked
            (0 until adapter.itemCount).forEach { i ->
                adapter.notifyItemChanged(i, it.contains(i))
                Timber.e("pos: $i check: ${it.contains(i)}")
            }
        })

        viewmodel.checkedItems.observe(viewLifecycleOwner, {
            binding.dialog.count = it.size
            if (it.isEmpty()) {
                binding.dialog.root.visibility = View.GONE
            } else {
                binding.dialog.root.visibility = View.VISIBLE
                binding.dialog.totalBayar =
                    viewmodel.stringUtil.formatCurrency(it.values.sumByDouble { it.total_fee })
            }

            // update checked
//            (0..adapter.itemCount).forEach { i ->
//                adapter.notifyItemChanged(i, it.keys.contains(i))
//                Timber.e("pos: $i check: ${it.keys.contains(i)} -- month: ${it[i]?.name}")
//            }
        })

        binding.dialog.btnCheckout.setOnClickListener {
            val keys = mutableListOf<String>()
            val values = mutableListOf<String>()
            val spp_ids = mutableListOf<String>()
            var total = 0.0

            val listRequested = mutableListOf<SppTable>()

            viewmodel.checkedItems.value?.values?.forEachIndexed { index, sppTable ->
                total += sppTable.total_fee

                if (sppTable.is_inquiry_channel)
                    listRequested.add(sppTable)
                else {
                    spp_ids.add(sppTable.transaction_id)
                    keys.add(sppTable.name)
                    values.add("Rp" + viewmodel.stringUtil.formatCurrency(sppTable.total_fee))
                }
            }

            if (listRequested.isNotEmpty()) {
                val dialogBinding = DialogActivateKlaspayBinding.inflate(layoutInflater).apply {
                    title.text = "Selesaikan Tagihan SPP Sebelumnya"
                    message.text =
                        "Selesaikan pembayaran tagihan SPP yang sebelumnya. Kamu bisa cek di menu tagihanku"
                    btnActivate.text = "Cek Tagihanku"
                    btnLater.text = "Batalkan"
                }
                val dialog = AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                    .setView(dialogBinding.root)
                    .show()
                    .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

                dialogBinding.btnActivate.setOnClickListener {
                    dialog.dismiss()
                    startActivity(Intent(requireActivity(), KlaspayTagihanPage::class.java))
                }
                dialogBinding.btnLater.setOnClickListener { dialog.dismiss() }
            } else {
                CheckoutPage.open(
                    requireActivity(),
                    "SPP",
                    keys.toTypedArray(),
                    values.toTypedArray(),
                    "Rp" + viewmodel.stringUtil.formatCurrency(total),
                    spp_ids.toTypedArray(),
                    total.toInt()
                )
            }
        }

        binding.swipeRefresh.isRefreshing = true
//        lifecycleScope.launch { viewmodel.fetchSpp(1) }
    }

    override fun onResume() {
        super.onResume()
        if (this::binding.isInitialized)
            refresh()
    }

    private fun refresh() {
        lifecycleScope.launch {
            firstLoad = true
            viewmodel.pageTagihan = 0
            viewmodel.fetchSpp(1)
            viewmodel.checkedIds.postValue(mutableListOf())
            viewmodel.checkedItems.postValue(HashMap())
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.e("activity result: $requestCode -- $resultCode")
        if (requestCode == CheckoutPage.RC) {
            refresh()
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private val adapter by lazy {
        object : PagingAdapter<SppTable, Viewholder>(viewmodel.diffUtil) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun bindItemViewholder(holder: Viewholder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: Viewholder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                if (payloads.isNotEmpty())
                    getItem(position)?.let { holder.update(payloads.first() as Boolean, it) }
                else
                    bindItemViewholder(holder, position)
            }
        }
    }

    inner class Viewholder(
        parent: ViewGroup,
        val binding: SppTagihanItemBinding = SppTagihanItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SppTable) {
            binding.item = item
            binding.stringUtil = viewmodel.stringUtil
            binding.checked = viewmodel.checkedIds.value?.contains(adapterPosition) == true
            binding.student = viewmodel.student
            binding.executePendingBindings()
            binding.name.setOnClickListener { binding.checkbox.performClick() }
            binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                val value = viewmodel.checkedIds.value ?: mutableListOf()
                if (isChecked) {
                    for (i in 0..adapterPosition) {
                        Timber.e("check $i")
                        if (!value.contains(i)) value.add(i)
                    }
                } else {
                    for (i in adapterPosition until adapter.itemCount) {
                        Timber.e("uncheck $i")
                        value.remove(i)
                    }
                }
                viewmodel.checkedIds.postValue(value)
            }

            updateItemChecked(item)
        }

        fun update(checked: Boolean, item: SppTable) {
            if (binding.checked != checked)
                binding.checked = checked

            updateItemChecked(item)
        }

        private fun updateItemChecked(item: SppTable) {
            val value = viewmodel.checkedItems.value ?: HashMap()
            if (binding.checked == true) value[adapterPosition] = item else value.remove(
                adapterPosition
            )
            viewmodel.checkedItems.postValue(value)
        }
    }
}