package id.onklas.app.pages.klaspay.riwayat

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import id.onklas.app.di.modules.NullToEmptyString

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayHistoryResponse(
    @NullToEmptyString val rc: String = "",
    @NullToEmptyString val rd: String = "",
    val data: KlaspayHistoryData = KlaspayHistoryData()
)

@JsonClass(generateAdapter = true)
@Keep
data class KlaspayHistoryData(
    val transaction_history: List<TransactionHistory> = emptyList()
)

@JsonClass(generateAdapter = true)
@Keep
data class TransactionHistory(
    @NullToEmptyString var transaction_id: String = "",
    val price: Int = 0,
    @NullToEmptyString var priceLabel: String = "",
    @NullToEmptyString val type: String = "",
    @NullToEmptyString val transaction_status: String = "",
    @NullToEmptyString var created_at: String = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class TransactionDetailResponse(
    @NullToEmptyString val rc: String = "",
    @NullToEmptyString val rd: String = "",
    val data: TransactionDetailData = TransactionDetailData()
)

@JsonClass(generateAdapter = true)
@Keep
data class TransactionDetailData(
    val transaction_detail: TransactionHistoryDetail = TransactionHistoryDetail()
)

@JsonClass(generateAdapter = true)
@Keep
data class TransactionHistoryDetail(
    @NullToEmptyString val transaction_id: String = "",
    val price: Int? = 0,
    val amount: Int? = 0,
    val fee_admin: Int? = 0,
    val fee_service: Int? = 0,
    val fee_other: Int? = 0,
    val total_amount: Int? = 0,
    @NullToEmptyString val type: String = "",
    @NullToEmptyString val transaction_status: String = "",
    @NullToEmptyString val created_at: String = "",
    @NullToEmptyString val invoice_id: String = "",
    @NullToEmptyString val note: String = "",
    @NullToEmptyString var paid_date: String = "",
    @NullToEmptyString var payment_code: String = "",
    @NullToEmptyString var payment_method_code: String = "Klaspay",
    @NullToEmptyString var payment_method: String = "KLASPAY"
)

//@JsonClass(generateAdapter = true)
//@Keep
//data class TransactionHistoryDetail(
//        val transaction_id: String? = "",
//        val wallet_id: String? = "",
//        val product_id: String? = "",
//        val bill_info1: Int? = 0,
//        val bill_info2: String? = "",
//        val bill_info5: Int? = 0,
//        val bill_info7: Int? = 0,
//        val bill_info9: Int? = 0,
//        val status: Int? = 0,
//        val transaction_type: String? = "",
//        var created_date: String? = "",
//        val updated_date: String? = ""
//)