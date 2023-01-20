package id.onklas.app.pages.ppob.game

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.SelectVoucherPageBinding
import id.onklas.app.databinding.VoucherItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BaseFragment
import id.onklas.app.pages.ppob.*
import id.onklas.app.pages.ppob.pulsa.PulsaItem
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch

class SelectVoucherPage : BaseFragment() {

    private lateinit var binding: SelectVoucherPageBinding
    private val viewmodel by activityViewModels<GameViewModel> { component.gameVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        SelectVoucherPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewmodel

        binding.editNomor.doAfterTextChanged { viewmodel.errorInput.postValue(false) }

        binding.imageContact.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            }
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivityForResult(intent, 982)
            }
        }

        binding.rvVoucher.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                includeBottom = true,
                includeTop = true
            )
        )
        binding.rvVoucher.adapter = adapter

        viewmodel.providerItems.observe(viewLifecycleOwner, Observer(adapter::submitList))

        binding.executePendingBindings()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 982 && resultCode == AppCompatActivity.RESULT_OK) {
            data?.data?.let {
                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                requireContext().contentResolver.query(it, projection, null, null, null)
                    ?.use { cursor ->
                        // If the cursor returned is valid, get the phone number
                        if (cursor.moveToFirst()) {
                            val numberIndex =
                                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            var number = cursor.getString(numberIndex)
                            number = number.replace(Regex("\\D"), "")
                            if (number.startsWith("62")) number = number.replaceFirst("62", "0")
                            binding.editNomor.setText(number)
                        }
                    }
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
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

            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private inner class Viewholder(
        parent: ViewGroup, val binding: VoucherItemBinding = VoucherItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PulsaItem) {
            binding.item = item
            binding.stringUtil = viewmodel.stringUtil
            binding.name.setOnClickListener { select(item) }
            binding.price.setOnClickListener { select(item) }
            binding.root.setOnClickListener { select(item) }
            binding.executePendingBindings()
        }

        private fun select(item: PulsaItem) {
            if (viewmodel.inputNomor.value.isNullOrEmpty()) {
                viewmodel.errorInput.postValue(true)
            } else {
                lifecycleScope.launch {
                    val createdAt = System.currentTimeMillis()
                    viewmodel.db.ppob().insert(
                        PpobTransaction(
                            createdAt = createdAt,
                            productId = item.product_id,
                            productType = PpobGame,
                            productSubType = item.provider_text,
                            productName = item.name,
                            productLabel = "Voucher Game",
                            productProvider = item.provider,
                            productIcon = R.drawable.ic_voucher_game,
                            custId = viewmodel.inputNomor.value.orEmpty(),
                            status = "Proses",
                            amount = item.price_end,
                            feeAdmin = item.admin,
                            totalAmount = item.price_end,
                            cashback = item.cashback
                        )
                    )

                    PpobCheckoutPage.openByCreatedAt(this@SelectVoucherPage, createdAt)
                }
            }
        }
    }
}