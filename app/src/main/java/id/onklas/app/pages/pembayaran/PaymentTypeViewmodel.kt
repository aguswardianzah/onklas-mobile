package id.onklas.app.pages.pembayaran

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.onklas.app.R
import id.onklas.app.api.ApiService
import id.onklas.app.pages.klaspay.topup.TypeTopupItem
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.PreferenceClass
import id.onklas.app.utils.StringUtil
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class PaymentTypeViewmodel @Inject constructor(
    val pref: PreferenceClass,
    val apiService: ApiService,
    val apiWrapper: ApiWrapper,
    val stringUtil: StringUtil
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }
    val isRefreshing by lazy { MutableLiveData(true) }
    val paymentChannels by lazy { MutableLiveData<List<TypeTopupItem>>() }
    val balance by lazy { MutableLiveData<String>() }

    suspend fun getBalance() = try {
        apiService.klaspayWallet().data.balance ?: 0
    } catch (e: Exception) {
        Timber.e(e)
        0
    }

    fun getPaymentType(amount: Int) {
        viewModelScope.launch {
            try {
                isRefreshing.postValue(true)
                val entries = apiService.sppPaymentChannels().data.products.entries

                paymentChannels.postValue(entries.map {
                    val parent = getTypeName(it.key)
                    TypeTopupItem(
                        it.key,
                        getTypeImgRes(it.key),
                        parent,
                        getTypeInfo(it.key),
                        it.value.mapIndexed { index, child ->
                            TypeTopupItem(
                                child.payment_code,
                                child.payment_logo,
                                child.payment_name,
                                parentName = it.key,
                                payment_code = child.payment_code
                            )
                        }
                    ).apply {
                        if (it.key.equals("klaspay", true)) {
                            val currBalance = getBalance()
                            payment_code = "KLASPAY"
                            balance = "Rp " + stringUtil.formatCurrency2(currBalance)
                            needTopup = currBalance < amount
                        }
                    }
                })
            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
            } finally {
                isRefreshing.postValue(false)
            }
        }
    }

    private fun getTypeImgRes(name: String) = when (name) {
        "bank transfer" -> R.drawable.ic_atm
        "modern store" -> R.drawable.ic_merchant
        "klaspay" -> R.drawable.ic_nominal
        else -> R.drawable.ic_topup_internet
    }

    private fun getTypeName(name: String) = when (name) {
        "e-money" -> "E-Money"
        "credit card" -> "Kartu Kredit"
        "virtual account" -> "Virtual Account"
        "clickpay" -> "Click Pay"
        "modern store" -> "Minimarket"
        "bank transfer" -> "Transfer Bank"
        "qris" -> "QRIS"
        "klaspay" -> "Klaspay"
        else -> "Lainnya"
    }

    private fun getTypeInfo(name: String) = when (name) {
        "e-money" -> "Bayar menggunakan akun dompet"
        "credit card" -> "Bayar menggunakan kartu kredit"
        "virtual account" -> "Bayar cepat dengan akun virtual bank"
        "clickpay" -> "Bayar cepat dengan aplikasi pembayaran"
        "modern store" -> "Bayar melalui minimarket"
        "bank transfer" -> "Bayar melalui transfer bank"
        "qris" -> "Scan untuk membayar"
        else -> "Pembayaran lainnya"
    }
}