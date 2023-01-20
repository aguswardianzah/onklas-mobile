package id.onklas.app.pages.ppob.pulsa

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.google.android.material.button.MaterialButton
import id.onklas.app.R
import id.onklas.app.databinding.PrabayarPageBinding
import id.onklas.app.databinding.PulsaRegularItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BaseFragment
import id.onklas.app.pages.ppob.*
import id.onklas.app.utils.GridSpacingItemDecoration
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch

class PrabayarPage : BaseFragment() {

    private lateinit var binding: PrabayarPageBinding
    private val viewmodel by activityViewModels<PulsaViewModel> { component.pulsaVmFactory }
    private val ppobViewmodel by activityViewModels<PpobViewModel> { component.ppobVmFactory }

    init {
        lifecycleScope.launchWhenCreated {
            try {
                ppobViewmodel.klaspayBalance.postValue(ppobViewmodel.apiService.klaspayWallet().data.balance)
            } catch (e: Exception) {
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = PrabayarPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewmodel
        binding.rvPulsa.adapter = adapter
        binding.rvPulsa.itemAnimator = null
        binding.rvPulsa.addItemDecoration(gridItemDecoration)

        viewmodel.listProduct.observe(viewLifecycleOwner) {
            setButton(binding.buttonPulsa)
        }

        binding.buttonPulsa.setOnClickListener { setButton(binding.buttonPulsa) }
        binding.buttonData.setOnClickListener { setButton(binding.buttonData) }
        binding.buttonPaket.setOnClickListener { setButton(binding.buttonPaket) }

        binding.executePendingBindings()
    }

    private val colorGray by lazy { ContextCompat.getColor(requireContext(), R.color.gray) }
    private val colorPrimary by lazy {
        ContextCompat.getColor(
            requireContext(),
            R.color.colorPrimary
        )
    }
    private val buttons by lazy {
        listOf(
            binding.buttonPulsa,
            binding.buttonData,
            binding.buttonPaket
        )
    }

    private fun setButton(button: MaterialButton) {
        binding.rvPulsa.apply {
            removeItemDecoration(linearItemDecoration)
            removeItemDecoration(gridItemDecoration)

            if (button == binding.buttonPulsa) {
                layoutManager = gridLayoutManager
                addItemDecoration(gridItemDecoration)
            } else {
                layoutManager = linearLayoutManager
                addItemDecoration(linearItemDecoration)
            }
        }

        buttons.forEach {
            if (it == button) {
                it.setBackgroundColor(colorPrimary)
                it.setTextColor(Color.WHITE)
                it.setStrokeColorResource(R.color.colorPrimary)
            } else {
                it.setBackgroundColor(Color.WHITE)
                it.setTextColor(colorGray)
                it.setStrokeColorResource(R.color.gray)
            }
        }

        binding.executePendingBindings()

        when (button) {
            binding.buttonPulsa -> {
                adapter.submitList(viewmodel.listProduct.value?.firstOrNull {
                    it.first.equals("Pulsa", true)
                }?.second)

                viewmodel.pulsaSubType.postValue(PulsaPrabayar)
            }

            binding.buttonData -> {
                adapter.submitList(viewmodel.listProduct.value?.firstOrNull {
                    it.first.equals("Paket Data", true)
                }?.second)

                viewmodel.pulsaSubType.postValue(PulsaPaket)
            }

            binding.buttonPaket -> {
                adapter.submitList(viewmodel.listProduct.value?.firstOrNull {
                    it.first.equals("Paket", true)
                }?.second)

                viewmodel.pulsaSubType.postValue(PulsaSms)
            }
        }
    }

    private val adapter by lazy {
        object : ListAdapter<PulsaItem, Viewholder>(object : DiffUtil.ItemCallback<PulsaItem>() {
            override fun areItemsTheSame(oldItem: PulsaItem, newItem: PulsaItem): Boolean =
                oldItem.product_id == newItem.product_id

            override fun areContentsTheSame(oldItem: PulsaItem, newItem: PulsaItem): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun onBindViewHolder(holder: Viewholder, position: Int) {
                holder.bind(getItem(position))
            }
        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: PulsaRegularItemBinding = PulsaRegularItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PulsaItem) {
            binding.name = item.name
            binding.price = viewmodel.stringUtil.formatCurrency2(item.price_end)
            binding.root.setOnClickListener { openDetail(item) }
            binding.priceLabel.setOnClickListener { openDetail(item) }
            binding.title.setOnClickListener { openDetail(item) }
            binding.executePendingBindings()
        }
    }

    private fun openDetail(item: PulsaItem) {
        lifecycleScope.launch {
            val createdAt = System.currentTimeMillis()
            viewmodel.db.ppob().insert(
                PpobTransaction(
                    createdAt = createdAt,
                    productId = item.product_id,
                    productType = PpobPulsa,
                    productSubType = viewmodel.pulsaSubType.value ?: PulsaPrabayar,
                    productName = item.name,
                    productLabel = "Pulsa",
                    productProvider = item.provider,
                    productIcon = R.drawable.ic_pulsa,
                    custId = viewmodel.inputPhone.value.orEmpty(),
                    status = "Proses",
                    amount = item.price_end,
                    totalAmount = item.price_end,
                    cashback = item.cashback
                )
            )

            PpobCheckoutPage.openByCreatedAt(this@PrabayarPage, createdAt)
        }
    }

    var trxId = ""
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PpobCheckoutPage.RC && resultCode == Activity.RESULT_OK) {
            requireActivity().finish()
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private val gridLayoutManager by lazy { GridLayoutManager(requireContext(), 2) }
    private val gridItemDecoration by lazy {
        GridSpacingItemDecoration(
            2,
            resources.getDimensionPixelSize(R.dimen._8sdp),
            true,
            resources.getDimensionPixelSize(R.dimen._16sdp)
        )
    }

    private val linearLayoutManager by lazy { LinearLayoutManager(requireContext()) }
    private val linearItemDecoration by lazy {
        LinearSpaceDecoration(
            space = resources.getDimensionPixelSize(R.dimen._4sdp),
            includeTop = true,
            includeBottom = true,
            customEdge = resources.getDimensionPixelSize(R.dimen._8sdp),
            crossEdge = resources.getDimensionPixelSize(R.dimen._16sdp)
        )
    }
}