package id.onklas.app.api

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import id.onklas.app.R
import id.onklas.app.databinding.DialogActivateKlaspayBinding
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.nio.charset.Charset
import javax.inject.Inject

class ResponseInterceptor @Inject constructor(val context: Context, moshi: Moshi) : Interceptor {

    private val errorAdapter by lazy {
        moshi.adapter<Map<String, Any>>(
            Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                Any::class.java
            )
        )
    }

    private val defaultErrorMsg by lazy { "Terjadi gangguan pada koneksi internet Anda, silahkan ulangi beberapa saat lagi" }

    @SuppressLint("SetTextI18n")
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val responseCode = response.code
        val responseBody = response.body

        return try {
            val content =
                responseBody?.source()?.also { it.request(Long.MAX_VALUE) }?.buffer?.clone()
                    ?.readString(Charset.forName("UTF-8"))
                    ?: defaultErrorMsg

            if ((responseCode / 100) == 2) {
                val contentMap: Map<String, Any> = try {
                    errorAdapter.fromJson(content) ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }
                val errorTypes: Array<String> = try {
                    (contentMap["errors"] as? Map<String, *>)?.keys?.toTypedArray()
                        ?: emptyArray()
                } catch (e: Exception) {
                    emptyArray()
                }
                if (contentMap.containsKey("rc") && contentMap["rc"] as? String != "00")
                    throw ApiException(
                        contentMap["rd"] as? String ?: defaultErrorMsg,
                        responseCode,
                        errorTypes
                    )
                else
                    response
            } else {
                if (responseCode == 503 || responseCode == 504) {
                    try {
                        val dialogBinding =
                            DialogActivateKlaspayBinding.inflate(LayoutInflater.from(context))
                                .apply {
                                    title.text = "Perbaikan Sistem"
                                    message.text =
                                        "Kami sedang melakukan perbaikan sistem untuk meningkatkan layanan kami kepada Anda. Kami akan segera kembali."
                                    btnActivate.text = "OK"
                                    btnLater.visibility = View.GONE
                                }
                        val dialog = AlertDialog.Builder(context)
                            .setView(dialogBinding.root)
                            .show()
                            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

                        dialogBinding.btnActivate.setOnClickListener { dialog.dismiss() }
                    } catch (e: Exception) {
                        Timber.e("failed to open maintenance dialog, ${e.message}")
                    }
                }

                val contentMap: Map<String, Any> = try {
                    errorAdapter.fromJson(content) ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }

                val errorTypes: Array<String> = try {
                    (contentMap["errors"] as? Map<String, *>)?.keys?.toTypedArray()
                        ?: emptyArray()
                } catch (e: Exception) {
                    emptyArray()
                }

                val errorData: Map<String, Any> = try {
                    (contentMap["data"] as? Map<String, Any>) ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }

                val errorBody = try {
                    "${(contentMap["message"] as? String) ?: "Mohon maaf, terjadi kesalahan"}. ${
                        ((contentMap["error"] as? String)?.plus(".")) ?: ""
                    } ${(contentMap["rd"] as? String) ?: ""}"
                } catch (e: Exception) {
                    defaultErrorMsg
                }

                responseBody?.close()
                throw ApiException(errorBody, responseCode, errorTypes, errorData)
            }
        } catch (e: Exception) {
            responseBody?.close()
            Timber.e(e)
            throw e
        }
    }
}

class ApiException(
    override val message: String?,
    val responseCode: Int?,
    val errorTypes: Array<String> = emptyArray(),
    val data: Map<String, Any> = emptyMap()
) : IOException(message)