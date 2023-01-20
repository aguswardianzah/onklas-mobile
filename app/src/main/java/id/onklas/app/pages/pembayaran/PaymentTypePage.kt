package id.onklas.app.pages.pembayaran

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.R
import id.onklas.app.databinding.PaymentTypePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.klaspay.topup.TopUpAdapter
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch
import timber.log.Timber

class PaymentTypePage : Privatepage() {

    private val binding by lazy { PaymentTypePageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<PaymentTypeViewmodel> { component.paymentTypeVmFactory }
    private val amount by lazy { intent.getIntExtra("amount", 0) }
    private val getChannelOnly by lazy { intent.getBooleanExtra("get_channel_only", false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch { viewmodel.getPaymentType(amount) }
        }

        binding.rvPayment.apply {
            addItemDecoration(itemSpace)
            adapter = this@PaymentTypePage.adapter
        }

        viewmodel.isRefreshing.observe(this, Observer(binding.swipeRefresh::setRefreshing))
        viewmodel.paymentChannels.observe(this, Observer(adapter::submitList))
        viewmodel.errorString.observe(this) {
            alert(msg = it, okClick = { finish() })
        }
        viewmodel.getPaymentType(amount)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 8392 && resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private val itemSpace by lazy {
        LinearSpaceDecoration(
            space = resources.getDimensionPixelSize(R.dimen._6sdp),
            includeTop = true,
            includeBottom = true
        )
    }

    private val adapter by lazy {
        TopUpAdapter({ item, position ->
            viewmodel.paymentChannels.value?.let { currentList ->
                val newList = ArrayList(currentList)
                val newItem = currentList.first { it.nameId == item.nameId }.copy()
                val newShowChild = !newItem.showChild

                val index = newList.indexOf(newItem)
                if (index > -1)
                    newList[index] = newItem.apply { showChild = newShowChild }

                if (newShowChild)
                    newList.addAll(position + 1, item.items)
                else
                    newList.removeAll { it.parentName == item.nameId }

                viewmodel.paymentChannels.postValue(newList)
            }
        }, { item ->
            if (getChannelOnly) {
                setResult(
                    RESULT_OK,
                    intent.putExtra("payment_code", item.payment_code)
                        .putExtra("payment_name", item.name)
                )
                Timber.e("payment code: ${item.payment_code} -- payment name: ${item.name}")
                finish()
            } else {
                intent.putExtra("use_klaspay", item.nameId.equals("klaspay", true))
                intent.putExtra("payment_code", item.payment_code)
                startActivityForResult(
                    Intent(this, ConfirmPinPage::class.java).putExtras(intent),
                    8392
                )
            }
        })
    }
}