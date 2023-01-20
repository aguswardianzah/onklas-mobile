package id.onklas.app.pages.ppob

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import id.onklas.app.R
import id.onklas.app.di.modules.NullToEmptyString
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

const val PpobPulsa = "Pulsa"
const val PpobListrik = "Listrik"
const val PpobAir = "Air"
const val PpobInternet = "Internet"
const val PpobGame = "Game"
const val PpobBpjs = "Bpjs"

const val PulsaPrabayar = "Pulsa Prabayar"
const val PulsaPaket = "Paket Pulsa"
const val PulsaSms = "Paket SMS"
const val PulsaPascabayar = "Pulsa Pascabayar"

const val ListrikToken = "Token Listrik"
const val ListrikTagihan = "Tagihan Listrik"

data class PpobPaymentData(
    val type: String = "",
    val productType: String = "",
    val customerName: String = "",
    val customerId1: String = "",
    val customerId2: String = "",
    val customerInfo1: String = "",
    val customerInfo2: String = "",
    val provider: String = "",
    val providerIcon: String = ""
) : Serializable

@JsonClass(generateAdapter = true)
@Keep
data class InqResponse(
    @NullToEmptyString val rc: String = "",
    @NullToEmptyString val rd: String = "",
    val data: InqResponseData = InqResponseData()
)

@JsonClass(generateAdapter = true)
@Keep
data class InqResponseData(val transaction_detail: InqResponseDetail = InqResponseDetail())

@JsonClass(generateAdapter = true)
@Keep
data class InqResponseDetail(
    @NullToEmptyString val transaction_id: String = "",
    val price: Int? = 0,
    val nominal: Int? = 0,
    val total_amount: Int? = 0,
    val cashback: Int? = 0,
    val fee_service: Int? = 0,
    val fee_other: Int? = 0,
    val amount: Int? = 0,
    val fee_admin: Int? = 0,
    @NullToEmptyString val type: String = "",
    @NullToEmptyString val transaction_status: String = "",
    @NullToEmptyString val created_at: String = "",
    @NullToEmptyString val note: String = "",
    @NullToEmptyString var paid_date: String = "",
    @NullToEmptyString var payment_code: String = "",
    @NullToEmptyString var payment_method_code: String = "Klaspay",
    @NullToEmptyString var payment_method: String = "KLASPAY",
    val pulsa: PpobCustomerInfo = PpobCustomerInfo(),
    val game: PpobCustomerInfo = PpobCustomerInfo(),
    val pln: PpobCustomerInfo = PpobCustomerInfo(),
    val pdam: PpobCustomerInfo = PpobCustomerInfo(),
    val internet: PpobCustomerInfo = PpobCustomerInfo(),
    val bpjs: PpobCustomerInfo = PpobCustomerInfo()
)

@JsonClass(generateAdapter = true)
@Keep
data class PpobCustomerInfo(
    @NullToEmptyString val product_id: String = "",
    @NullToEmptyString val pulsa_provider: String = "",
    @NullToEmptyString val pulsa_type: String = "",
    @NullToEmptyString val pulsa_name: String = "",
    @NullToEmptyString val idpel1: String = "",
    @NullToEmptyString val idpel2: String = "",
    @NullToEmptyString val idpel3: String = "",
    @NullToEmptyString val ref1: String = "",
    @NullToEmptyString val ref2: String = "",
    @NullToEmptyString val ref3: String = "",
    @NullToEmptyString val SN: String = "",
    @NullToEmptyString val nama_pelanggan: String = "",
    @NullToEmptyString val periode: String = "",
    @NullToEmptyString val standawal: String = "0",
    @NullToEmptyString val standakhir: String = "0",
    val nominal: Int? = 0,
    val admin: Int? = 0,
    val total_bayar: Int? = 0
)

@Entity(
    tableName = "ppob",
    indices = [Index("trxId", unique = true), Index("createdAt", unique = true)]
)
data class PpobTransaction(
    @PrimaryKey var trxId: String = "",
    val createdAt: Long = 0,
    var paidAt: Long = 0,
    var status: String = "",
    var amount: Int = 0,
    var feeAdmin: Int = 0,
    var feeService: Int = 0,
    var feeOther: Int = 0,
    var totalAmount: Int = 0,
    var cashback: Int = 0,
    var productId: String = "",
    var productType: String = "",
    var productIcon: Int = 0,
    var productProviderIcon: Int = 0,
    var productName: String = "",
    var productProvider: String = "",
    var productLabel: String = "",
    var productSubType: String = "",
    var custId: String = "",
    var custName: String = "",
    var custRef1: String = "",
    var custRef2: String = "",
    var custRef3: String = "",
    var trxRef1: String = "",
    var trxRef2: String = "",
    var trxRef3: String = "",
    var info: String = ""
) {
    companion object {
        fun formInqResponseDetail(trxId: String, data: InqResponseDetail): PpobTransaction {
            val periodeSrc = SimpleDateFormat("yyyyMM", Locale.ENGLISH)
            val periodeDest = SimpleDateFormat("MMM yyyy", Locale("id"))
            val srcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)

            return PpobTransaction(
                trxId = trxId,
                createdAt = try {
                    srcFormat.parse(data.created_at).time
                } catch (e: Exception) {
                    0
                },
                status = data.transaction_status,
                amount = data.amount ?: 0,
                feeAdmin = data.fee_admin ?: 0,
                feeService = data.fee_service ?: 0,
                feeOther = data.fee_other ?: 0,
                totalAmount = data.total_amount ?: 0,
                cashback = data.cashback ?: 0,
                info = data.note
            ).apply {
                when {
                    data.pulsa.product_id.isNotEmpty() -> {
                        productType = PpobPulsa
                        productId = data.pulsa.product_id
                        productSubType = if (data.pulsa.pulsa_type.contains(
                                "pasca",
                                true
                            )
                        ) PulsaPascabayar else data.pulsa.pulsa_type
                        productLabel = data.pulsa.pulsa_type
                        productIcon = R.drawable.ic_pulsa
                        custId =
                            if (data.pulsa.idpel1.isNotEmpty()) data.pulsa.idpel1 else data.pulsa.idpel2
                        productProvider = data.pulsa.pulsa_provider
                        productSubType = if (data.pulsa.pulsa_type.contains(
                                "pasca",
                                true
                            )
                        ) PulsaPascabayar else PulsaPrabayar
                        custName = data.pulsa.nama_pelanggan
                    }
                    data.game.product_id.isNotEmpty() -> {
                        productType = PpobGame
                        productLabel = "Voucher Game"
                        productIcon = R.drawable.ic_voucher_game
                        productId = data.game.product_id
                        productProvider = data.game.pulsa_provider
                        productName = data.game.pulsa_name
                        custId = data.game.idpel1
                        custRef1 = data.game.SN
                    }
                    data.pln.product_id.isNotEmpty() -> {
                        productType = PpobListrik
                        productId = data.pln.product_id
                        productSubType = if (data.pln.product_id.equals(
                                "PLNPRAH",
                                true
                            )
                        ) ListrikToken else ListrikTagihan
                        productLabel = if (data.pln.product_id.equals(
                                "PLNPRAH",
                                true
                            )
                        ) ListrikToken else ListrikTagihan
                        productIcon = R.drawable.ic_bayar_listrik
                        custId = data.pln.idpel1
                        custName = data.pln.nama_pelanggan
                        productName = data.pln.product_id
                        custRef1 = if (data.pln.product_id == "PLNPRAH") data.pln.SN else try {
                            data.pln.periode.split(",").joinToString(", ") {
                                periodeDest.format(periodeSrc.parse(it))
                            }
                        } catch (e: Exception) {
                            try {
                                periodeDest.format(periodeSrc.parse(data.pln.periode))
                            } catch (e: Exception) {
                                ""
                            }
                        }
                    }
                    data.pdam.product_id.isNotEmpty() -> {
                        productType = PpobAir
                        productId = data.pdam.product_id
                        productLabel = "Tagihan Air"
                        productIcon = R.drawable.ic_bayar_air
                        custId = data.pdam.idpel1
                        productProvider = data.pdam.pulsa_provider
                        productSubType = data.pdam.pulsa_name
                        custName = data.pdam.nama_pelanggan
                        custRef1 = try {
                            data.pdam.periode.split(",").joinToString(", ") {
                                periodeDest.format(periodeSrc.parse(it))
                            }
                        } catch (e: Exception) {
                            try {
                                periodeDest.format(periodeSrc.parse(data.pdam.periode))
                            } catch (e: Exception) {
                                ""
                            }
                        }
                        custRef2 = data.pdam.standawal + "-" + data.pdam.standakhir
                    }
                    data.internet.product_id.isNotEmpty() -> {
                        productType = PpobInternet
                        productSubType = "Internet"
                        productId = data.internet.product_id
                        productLabel = "Internet"
                        productIcon = R.drawable.ic_internet
                        productName = data.internet.pulsa_name
                        productProvider = data.internet.pulsa_name
                        custId = "${data.internet.idpel1}-${data.internet.idpel2}"
                        custName = data.internet.nama_pelanggan
                    }
                    data.bpjs.product_id.isNotEmpty() -> {
                        productType = PpobBpjs
                        productSubType = "BPJS Kesehatan"
                        productId = data.bpjs.product_id
                        productLabel = PpobBpjs
                        productIcon = R.drawable.ic_bpjs
                        productName = "BPJS Kesehatan"
                        custId = data.bpjs.idpel1
                        custName = data.bpjs.nama_pelanggan
                        custRef1 = data.bpjs.periode
                    }
                }
            }
        }
    }
}