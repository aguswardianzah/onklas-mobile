package id.onklas.app.utils

import android.os.Build
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.*
import javax.inject.Inject

class DateUtil @Inject constructor() {

    fun formatDate(input: String): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                OffsetDateTime.parse(input).toEpochSecond()
            } catch (e: Exception) {
                0
            }
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale("id")).parse(input)?.time ?: 0
        }

    fun formatString(timeInMilis: Long): String {
        val now = Calendar.getInstance()
        val dateNow = now.get(Calendar.DAY_OF_YEAR)
        val cal = Calendar.getInstance().apply { timeInMillis = timeInMilis }
        val date = cal.get(Calendar.DAY_OF_YEAR)
        val dayFormat =
            if (date == dateNow) "Hari ini" else if (date - dateNow == 1) "Kemarin" else ""
        val dateFormat =
            SimpleDateFormat(
                "${if (dayFormat.isNotEmpty()) "" else "dd MMMM"} ${
                    if (cal.get(
                            Calendar.YEAR
                        ) != now.get(Calendar.YEAR)
                    ) "yyyy" else ""
                }, HH:mm", Locale("id")
            )
        return "${if (dayFormat.isNotEmpty()) dayFormat else ""}${dateFormat.format(date)}"
    }

    fun getDateTime(s: Long): String? {
        try {
            val locale = Locale("id", "ID")
            val sdf = SimpleDateFormat("dd MMMM yyyy",locale)
            val netDate = Date(s.toLong() * 1000)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }
    fun getDateTime2(s: Long): String {
        try {
            val locale = Locale("id", "ID")
            val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm",locale)
            val netDate = Date(s.toLong() * 1000)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }
    fun getDateTime3(s: Long): String {
        try {
            val locale = Locale("id", "ID")
            val sdf = SimpleDateFormat("yyyy-MM-dd",locale)
            val netDate = Date(s.toLong() * 1000)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }
    fun getDateTimeTomorrow(s: Long): String {
        try {
            val locale = Locale("id", "ID")
            val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm",locale)
            val netDate = Date((s.toLong() * 1000) + (1000 * 60 * 60 * 24))
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

}