package id.onklas.app.pages.pembayaran

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.utils.ApiWrapper
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.PreferenceClass
import id.onklas.app.utils.StringUtil
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ConfirmPinViewModel @Inject constructor(
    val apiWrapper: ApiWrapper,
    val apiService: ApiService,
    val stringUtil: StringUtil,
    val db: MemoryDB,
    val moshi: Moshi,
    val pref: PreferenceClass,
    val intentUtil: IntentUtil
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }
    val pin by lazy { MutableLiveData("") }

    private val userAdapter by lazy { moshi.adapter(UserTable::class.java) }
    val userTable: UserTable by lazy {
        try {
            userAdapter.fromJson(pref.getString("user")) ?: UserTable(pref.getInt("user_id"))
        } catch (e: Exception) {
            UserTable(pref.getInt("user_id"))
        }
    }

    suspend fun resetPin() = try {
        apiService.resetPinKlaspay(mapOf("email" to userTable.email))
    } catch (e: Exception) {
        errorString.postValue(e.message)
        Timber.e(e)
        null
    }

    suspend fun setPin(token: String?) = try {
        apiService.setPinKlaspay(
            mapOf(
                "pin" to setPin.value,
                "reset_token" to token
            )
        )
        true
    } catch (e: Exception) {
        errorString.postValue(e.message)
        Timber.e(e)
        false
    }

    val setPin by lazy { MutableLiveData("") }
    val confPin by lazy { MutableLiveData("") }

    suspend fun paySpp(transationId: String, channel: String) = try {
        apiWrapper.paySppChannel(transationId, pin.value, channel)
        true
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        false
    }

    suspend fun paySppChannel(transationId: String, channel: String) = try {
        apiWrapper.paySppChannel(transationId, pin.value, channel).also {
            val data = it.data
            val createdAt = Date()
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id"))
            val srcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            val expiredAt = try { srcFormat.parse(data.expired_date) } catch (e: Exception) { Date() }
            val feeAdamin = data.fee_admin ?: 0
            val total = data.total_amount ?: 0
            val nominal = total - feeAdamin
            db.payment().insert(
                PaymentInvoice(
                    data.transaction_id,
                    "SPP",
                    createdAt,
                    dateFormat.format(createdAt),
                    expiredAt,
                    expiredAt?.let { dateFormat.format(it) } ?: "",
                    "Baru",
                    data.payment_method_code,
                    data.payment_method,
                    "",
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
        }
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.localizedMessage)
        null
    }
}