package id.onklas.app.pages.presensi

import java.text.SimpleDateFormat
import java.util.*

object PresensiBindConverter {

    val fullDateFormat by lazy { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id")) }
    val monthFormat by lazy { SimpleDateFormat("MMMM", Locale("id")) }

    @JvmStatic
    fun calendarToDateFormat(time: Calendar?): String? = time?.let {
        val cal = Calendar.getInstance()
        if (
            it.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) &&
            it.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
        )
            "Hari ini"
        else
            fullDateFormat.format(Date(it.timeInMillis))
    }

    @JvmStatic
    fun calendarToMonthFormat(time: Calendar?): String? = time?.let {
        val cal = Calendar.getInstance()
        if (
            it.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
            it.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
        )
            "Bulan ini"
        else
            monthFormat.format(Date(it.timeInMillis))
    }
}