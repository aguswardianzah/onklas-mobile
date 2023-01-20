package id.onklas.app.pages.ppob.listrik

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import id.onklas.app.R
import id.onklas.app.databinding.ListrikPageBinding
import id.onklas.app.databinding.PulsaRegularItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.pembayaran.ConfirmPinPage
import id.onklas.app.pages.ppob.*
import id.onklas.app.utils.GridSpacingItemDecoration
import kotlinx.coroutines.launch

class ListrikPage : Privatepage() {

    private val binding by lazy { ListrikPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<ListrikViewModel> { component.listrikVmFactory }

    init {
        lifecycleScope.launchWhenCreated {
            viewmodel.loadListPln()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }

        binding.lifecycleOwner = this
        binding.viewmodel = viewmodel
        binding.listrikInfoLabel.text = infoToken

        binding.buttonToken.setOnClickListener {
            setButtonTop(binding.buttonToken)
            binding.listrikInfoLabel.text = infoToken
            viewmodel.isTagihan.postValue(false)
            binding.executePendingBindings()
        }
        binding.buttonTagihan.setOnClickListener {
            setButtonTop(binding.buttonTagihan)
            binding.listrikInfoLabel.text = infoTagihan
            viewmodel.isTagihan.postValue(true)
            binding.executePendingBindings()
        }

        binding.imageContact.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, 982)
            }
        }

        binding.rvToken.adapter = adapter
        binding.rvToken.addItemDecoration(
            GridSpacingItemDecoration(
                2,
                resources.getDimensionPixelSize(R.dimen._8sdp),
                true,
                resources.getDimensionPixelSize(R.dimen._16sdp)
            )
        )

        viewmodel.listPlnItem.observe(this, Observer(adapter::submitList))
        viewmodel.errorString.observe(this, Observer(this::toast))

        binding.btnCheck.setOnClickListener {
            checkNoPelanggan { ConfirmPinPage.getPin(this) }
        }

        binding.executePendingBindings()
    }

    private fun checkNoPelanggan(then: () -> Unit) {
        if (binding.editMeter.text.toString().isNotEmpty())
            then.invoke()
        else
            toast("Mohon isikan nomor meter pelanggan terlebih dahulu")
    }

    private val adapter by lazy {
        object : ListAdapter<PlnItem, Viewholder>(object : DiffUtil.ItemCallback<PlnItem>() {
            override fun areItemsTheSame(oldItem: PlnItem, newItem: PlnItem): Boolean =
                oldItem.product_id == newItem.product_id

            override fun areContentsTheSame(oldItem: PlnItem, newItem: PlnItem): Boolean =
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

        fun bind(item: PlnItem) {
            val price = viewmodel.stringUtil.formatCurrency2(item.nominal.toInt())
            binding.name = "Token $price"
            binding.price = price
            binding.root.setOnClickListener { select(item) }
            binding.priceLabel.setOnClickListener { select(item) }
            binding.title.setOnClickListener { select(item) }
            binding.executePendingBindings()
        }

        private fun select(item: PlnItem) {
            viewmodel.selectedNominal.postValue(item.nominal)
            checkNoPelanggan { ConfirmPinPage.getPin(this@ListrikPage) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 982 && resultCode == RESULT_OK) {
            data?.data?.let {
                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                contentResolver.query(it, projection, null, null, null)?.use { cursor ->
                    // If the cursor returned is valid, get the phone number
                    if (cursor.moveToFirst()) {
                        val numberIndex =
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        var number = cursor.getString(numberIndex)
                        number = number.replace(Regex("\\D"), "")
                        if (number.startsWith("62")) number = number.replaceFirst("62", "0")

                        binding.editMeter.setText(number)
                    }
                }
            }
        } else if (requestCode == ConfirmPinPage.RC && resultCode == RESULT_OK && data != null) {
            lifecycleScope.launch {
                loading(msg = "sedang mencek tagihan Anda")
                val res = viewmodel.inqPln(data.getStringExtra("pin"))
                if (res.isNotEmpty())
                    PpobCheckoutPage.openByTrxId(this@ListrikPage, res)

                dismissloading()
            }
        } else if (requestCode == PpobCheckoutPage.RC && resultCode == Activity.RESULT_OK) {
            finish()
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private val infoToken by lazy { Html.fromHtml("1. Produk Listrik PLN tidak tersedia pada <b>jam cut off / maintenance (23.00 - 01.00)</b>") }
    private val infoTagihan by lazy {
        Html.fromHtml(
            "1. Produk Listrik PLN tidak tersedia pada <b>jam cut off / maintenance (23.00 - 01.00)</b><br>" +
                    "2. Jatuh tempo pembayaran tagihan listrik adalah tanggal 20 di setiap bulannya<br>" +
                    "3. Proses verifikasi pembayaran maksimum <b>2 x 24 jam kerja</b>"
        )
    }

    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private val buttons by lazy { listOf(binding.buttonToken, binding.buttonTagihan) }
    private fun setButtonTop(view: MaterialButton) {
        buttons.forEach {
            when (it) {
                view -> {
                    it.setBackgroundColor(colorPrimary)
                    it.setStrokeColorResource(R.color.colorPrimary)
                    it.setTextColor(Color.WHITE)
                }
                else -> {
                    it.setBackgroundColor(Color.TRANSPARENT)
                    it.setStrokeColorResource(R.color.gray)
                    it.setTextColor(colorGray)
                }
            }
        }
    }
}