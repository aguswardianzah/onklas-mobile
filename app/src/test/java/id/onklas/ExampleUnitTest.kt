package id.onklas

import androidx.work.*
import com.squareup.moshi.*
import id.onklas.app.di.modules.NullToEmptyString
import id.onklas.app.pages.klaspay.tagihan.KlaspayInvoiceResponse
import id.onklas.app.worker.AkmDownloader
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assert("1.0.9-alpha03" > "1.0.9-alpha04")
//        assertEquals(4, 2 + 2)
    }

    lateinit var moshi: Moshi
    lateinit var adapter: JsonAdapter<KlaspayInvoiceResponse>
    lateinit var adapterAny: JsonAdapter<KlaspayInvoiceResponse>

    @Before
    fun init() {
        moshi = Moshi.Builder()
            .add(object {
                @ToJson
                fun toJson(@NullToEmptyString value: String?): String? = value

                @FromJson
                @NullToEmptyString
                fun fromJson(reader: JsonReader): String? {
                    val result = if (reader.peek() === JsonReader.Token.NULL) {
                        reader.nextNull()
                    } else {
                        reader.nextString()
                    }

                    return result ?: ""
                }
            }).build()
        adapter = moshi.adapter(KlaspayInvoiceResponse::class.java)
    }

    @Test
    fun test_guide() {
        assertNotNull(adapter.fromJson("jojo"))
    }

    @Test
    fun testUuid() {
        val workRequest = OneTimeWorkRequestBuilder<AkmDownloader>()
            .setInputData(
                workDataOf("id" to 0)
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag("akm_downloader_0")
            .build()
        val workId = workRequest.id

        val testUuid = UUID(workId.mostSignificantBits, workId.leastSignificantBits)

        assertEquals(workId, testUuid)
    }
}