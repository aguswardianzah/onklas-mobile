package id.onklas.app.pages.pembayaran

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.SuccessPayItemBinding
import id.onklas.app.databinding.SuccessPayPageBinding
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.ppob.PpobPaymentDetailPage
import id.onklas.app.utils.LinearSpaceDecoration

class SuccessPayPage : Privatepage() {

    private val binding by lazy { SuccessPayPageBinding.inflate(layoutInflater) }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val view = window.decorView
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            view.systemUiVisibility = flags
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }

        setContentView(binding.root)

        if (isPpob && !isSuccess) {
            binding.title.text = "Pembayaran dalam Proses"
            binding.imgSuccess.setImageResource(R.drawable.img_pay_process)
        } else {
            binding.title.text = "Pembayaran Berhasil"
            binding.imgSuccess.setImageResource(R.drawable.img_pay_success)
        }

        binding.type = type
        binding.amount = amount.replace("rp", "", true)
        binding.rvItems.apply {
            adapter = this@SuccessPayPage.adapter
            addItemDecoration(
                LinearSpaceDecoration(
                    space = resources.getDimensionPixelSize(R.dimen._2sdp),
                    includeBottom = true,
                    includeTop = true
                )
            )
        }

        binding.btnDetail.setOnClickListener {
            if (isPpob)
                PpobPaymentDetailPage.open(this, trxId)
            else
                PaymentDetailPage.open(this, trxId)
        }

        binding.btnOk.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        binding.executePendingBindings()
    }

    private val adapter by lazy {
        object : RecyclerView.Adapter<Viewholder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(items?.get(position).orEmpty())

            override fun getItemCount(): Int = items?.size ?: 0
        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: SuccessPayItemBinding = SuccessPayItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(name: String) {
            binding.name = name
            binding.executePendingBindings()
        }
    }

    companion object {
        const val RC = 290

        fun open(
            activity: Activity,
            type: String,
            amount: String,
            items: Array<String> = emptyArray()
        ) {
            activity.startActivityForResult(
                Intent(activity, SuccessPayPage::class.java)
                    .putExtra("type", type)
                    .putExtra("amount", amount)
                    .putExtra("items", items), RC
            )
        }

        fun open(
            activity: Activity,
            isPpob: Boolean,
            type: String,
            amount: String,
            trxId: String,
            items: Array<String> = emptyArray(),
            isSuccess: Boolean = true
        ) {
            activity.startActivity(
                Intent(activity, SuccessPayPage::class.java)
                    .putExtra("isPpob", isPpob)
                    .putExtra("type", type)
                    .putExtra("amount", amount)
                    .putExtra("items", items)
                    .putExtra("trxId", trxId)
                    .putExtra("isSuccess", isSuccess)
            )
        }
    }

    private val isPpob by lazy { intent.getBooleanExtra("isPpob", false) }
    private val isSuccess by lazy { intent.getBooleanExtra("isSuccess", false) }
    private val type by lazy { intent.getStringExtra("type").orEmpty() }
    private val amount by lazy { intent.getStringExtra("amount").orEmpty() }
    private val trxId by lazy { intent.getStringExtra("trxId").orEmpty() }
    private val items by lazy { intent.getStringArrayExtra("items") }
}