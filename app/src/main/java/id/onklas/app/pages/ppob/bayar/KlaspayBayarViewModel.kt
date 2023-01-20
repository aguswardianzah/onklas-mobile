package id.onklas.app.pages.ppob.bayar

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.onklas.app.pages.klaspay.tagihan.KlaspayTransactionInvoiceItem
import id.onklas.app.utils.NumberUtil
import id.onklas.app.utils.StringUtil
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class KlaspayBayarViewModel @Inject constructor(
    val numberUtil: NumberUtil,
    val stringUtil: StringUtil
) : ViewModel() {

    val titleBar: MutableLiveData<String> = MutableLiveData("")
    val totalAmount: MutableLiveData<String> = MutableLiveData()
    val feeAdmin: MutableLiveData<String> = MutableLiveData()
    val bayarData: MutableLiveData<KlaspayBayarData> = MutableLiveData()
    val guidanceList: MutableLiveData<List<String>> = MutableLiveData()

//    val bayarDataMap: MutableLiveData<KlaspayBayarDataMap> = MutableLiveData()
//    val guidanceData by lazy { MutableLiveData<List<KlaspayGuidanceData>>() }

    fun fromTopup ( topup: KlaspayBayarResponse ) {
        bayarData.value = topup.data
        feeAdmin.value = "Rp ${numberUtil.formatCurrency(topup.data.fee_admin!!)}"
        totalAmount.value = "Rp ${numberUtil.formatCurrency(topup.data.total_amount!!)}"

        getPayment(topup.data.payment_method!!)
        getGuidance( topup.data.guidance )
    }

    fun fromInvoice ( invoice: KlaspayTransactionInvoiceItem ) {
        val detail = invoice.details!!
        bayarData.value = invoice.details
        feeAdmin.value = "Rp ${numberUtil.formatCurrency(detail.fee_admin!!)}"
        totalAmount.value = "Rp ${numberUtil.formatCurrency(detail.total_amount!!)}"
        Timber.e("fromInvoice: ${detail.guidance}")

        getPayment(detail.payment_method!!)
        getGuidance( detail.guidance )
//        val guidace = detail.guidance.entries
//        Timber.e("guidace $guidace")
//        guidanceData.postValue(  )

    }

    private fun getGuidance(list: List<KlaspayBayarGuidance>){
        list.forEach {
            when {
                it.virtualAccount.isNotEmpty() -> guidanceList.value = it.virtualAccount
                it.emoney.isNotEmpty() -> guidanceList.value = it.emoney
                it.creditCard.isNotEmpty() -> guidanceList.value = it.creditCard
                it.clickPay.isNotEmpty() -> guidanceList.value = it.clickPay
                it.modernStore.isNotEmpty() -> guidanceList.value = it.modernStore
                it.bankTransfer.isNotEmpty() -> guidanceList.value = it.bankTransfer
                it.direct.isNotEmpty() -> guidanceList.value = it.direct
            }
        }
    }

    val labelPayment by lazy { MutableLiveData("") }
    private fun getPayment(payment: String) {
        when (payment) {
            "Alfamart" -> labelPayment.value = "Nomor Tagihan Pembelian"
            else -> labelPayment.value = "Nomor Virtual Account"
        }
    }

    fun dateFormat(string: String?): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val outputFormatDate = SimpleDateFormat("dd MMM yyyy", Locale("id"))
        string?.let {
            val date: Date = inputFormat.parse(it)!!
            return outputFormatDate.format(date)
        }
        return ""
    }

    fun timeFormat(string: String?): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val outputFormatTime = SimpleDateFormat("HH:mm:ss", Locale("id"))
        string?.let {
            val date: Date = inputFormat.parse(it)!!
            return outputFormatTime.format(date)
        }
        return ""
    }

    fun copyText(activity: Activity, text: String?){
        val clipboard: ClipboardManager? = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("label", text)
        clipboard!!.setPrimaryClip(clip)
        Toast.makeText(activity, "Copied!", Toast.LENGTH_SHORT).show();
    }
}