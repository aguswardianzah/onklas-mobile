package id.onklas.app.api

import id.onklas.app.utils.PreferenceClass
import id.onklas.app.utils.Utils
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class RequestInterceptor @Inject constructor(val utils: Utils, val pref: PreferenceClass) :
    Interceptor {

    private val defaultErrorMsg by lazy { "Terjadi gangguan pada koneksi internet Anda, silahkan ulangi beberapa saat lagi" }

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!utils.isInternetAvailable()) {
            throw ApiException(defaultErrorMsg, 0)
        }

        return chain.proceed(
            chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer ${pref.getString("user_token")}")
                .build()
        )
    }
}