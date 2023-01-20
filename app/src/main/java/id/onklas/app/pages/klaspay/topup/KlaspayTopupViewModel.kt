package id.onklas.app.pages.klaspay.topup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.onklas.app.R
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.pembayaran.PaymentInvoice
import id.onklas.app.utils.NumberUtil
import id.onklas.app.utils.StringUtil
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class KlaspayTopupViewModel @Inject constructor(
    val apiService: ApiService,
    val numberUtil: NumberUtil,
    val stringUtil: StringUtil,
    val db: MemoryDB
) : ViewModel() {

    val titleBar: MutableLiveData<String> = MutableLiveData("")
    val balance by lazy { MutableLiveData("") }
    val transactionIdInquiry by lazy { MutableLiveData("") }

    //    val transactionId by lazy { MutableLiveData("") }
    val channelSelected by lazy { MutableLiveData<TypeTopupItem>() }

    //    val bayarTopup: MutableLiveData<KlaspayBayarResponse> = MutableLiveData()
    val errorString by lazy { MutableLiveData<String>() }

    suspend fun klaspayWallet() = try {
        apiService.klaspayWallet().also {
            balance.postValue(stringUtil.formatCurrency2(it.data.balance ?: 0))
        }
    } catch (e: Exception) {
        Timber.e(e)
    }

    val paymentMethodList by lazy { MutableLiveData<List<TypeTopupItem>>() }
    val loadingList by lazy { MutableLiveData(true) }

    suspend fun klaspayToolbar() = try {
        loadingList.postValue(true)
        val toolbarData = apiService.klaspayToolbar().data.products.entries
        Timber.e("toolbarData $toolbarData")

        paymentMethodList.postValue(toolbarData.map {
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
                        child.payment_name.orEmpty(),
                        parentName = it.key,
                        payment_code = child.payment_code.orEmpty()
                    )
                }
            )
        })
    } catch (e: Exception) {
        Timber.e(e)
    } finally {
        loadingList.postValue(false)
    }

    private fun getTypeImgRes(name: String) = when (name) {
        "bank transfer" -> R.drawable.ic_atm
        "modern store" -> R.drawable.ic_merchant
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

    var nominal: Int = 0
    suspend fun klaspayTopupInq(nominal: Int) = try {
        this.nominal = nominal
        transactionIdInquiry.value = apiService.klaspayTopupInq(mapOf("nominal" to nominal))
            .data
            .transaction_id_inquiry
        true
    } catch (e: Exception) {
        errorString.value = e.localizedMessage
        Timber.e(e)
        false
    }

    suspend fun klaspayTopupTrx() = try {
        val res = apiService.klaspayTopupTrx(
            mapOf(
                "biller" to 1,
                "channel" to channelSelected.value?.payment_code,
                "transaction_id" to transactionIdInquiry.value
            )
        )
        val data = res.data
        val createdAt = Date()
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id"))
        val srcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val expiredAt = try { srcFormat.parse(data.expired_date) } catch (e: Exception) { Date() }
        db.payment().insert(
            PaymentInvoice(
                data.transaction_id,
                "TOPUP",
                createdAt,
                dateFormat.format(createdAt),
                expiredAt,
                expiredAt?.let { dateFormat.format(it) } ?: "",
                "Baru",
                data.payment_method_code,
                data.payment_method,
                channelSelected.value?.img?.toString().orEmpty(),
                data.payment_code,
                if (data.guidance_code.equals("alfamart", true))
                    "Tagihan Pembelian"
                else "Virtual Account",
                nominal,
                data.fee_admin,
                data.fee_service,
                data.fee_other,
                data.total_amount
            )
        )

        data.transaction_id
//        bayarTopup.value = res
//        transactionId.postValue(res.data.)
    } catch (e: Exception) {
        errorString.value = e.localizedMessage
        Timber.e(e)
        ""
    }
}