package id.onklas.app.di.modules

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import id.onklas.app.BuildConfig
import id.onklas.app.api.*
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
object ApiModules {

    @Provides
    @Singleton
    @JvmStatic
    fun provideApiService(
        moshi: Moshi,
        okHttpClient: OkHttpClient,
        callAdapterFactory: CallAdapter.Factory
    ): ApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL)
        .addCallAdapterFactory(callAdapterFactory)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()
        .create(ApiService::class.java)

    @Provides
    @Singleton
    @JvmStatic
    fun provideCallAdapterFactory(): CallAdapter.Factory = object : CallAdapter.Factory() {
        override fun get(
            returnType: Type,
            annotations: Array<Annotation>,
            retrofit: Retrofit
        ): CallAdapter<*, *>? {
            if (getRawType(returnType) != retrofit2.Call::class.java) {
                return null
            }

            annotations.forEach {
                if (it.annotationClass == Timeout::class) {
                    val timeout = it as Timeout
                    Timber.e("set custom timeout for api with @Timeout annotation, timeout: $timeout")
                    val delegate = retrofit.nextCallAdapter(this, returnType, annotations)
                    return object : CallAdapter<Any, retrofit2.Call<Any>> {
                        override fun responseType(): Type = delegate.responseType()

                        override fun adapt(call: retrofit2.Call<Any>): retrofit2.Call<Any> {
                            call.timeout().timeout(timeout.value.toLong(), timeout.unit)
                            return call
                        }
                    }
                }
            }

            return null
        }
    }

    @Provides
    @Singleton
    @JvmStatic
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor { message ->
            Timber.tag("API_REQUEST").e(message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    @JvmStatic
    fun provideOkHttpClient(
        context: Context,
        requestInterceptor: RequestInterceptor,
        responseInterceptor: ResponseInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        logFileInterceptor: LogFileInterceptor,
        chuckInterceptor: ChuckerInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(requestInterceptor)
        .addInterceptor(responseInterceptor)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(logFileInterceptor)
        .addInterceptor(chuckInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .cache(Cache(context.cacheDir, 10 * 1024 * 1024))
        .build()

    @Provides
    @Singleton
    @JvmStatic
    fun providechuckInterceptor(context: Context): ChuckerInterceptor =
        ChuckerInterceptor.Builder(context).build()
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Timeout(val value: Int, val unit: TimeUnit)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class LogFile