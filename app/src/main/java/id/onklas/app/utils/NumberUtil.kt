package id.onklas.app.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

class NumberUtil @Inject constructor() {

    fun formatCurrency (amount: Int): String {
//        val decimalFormat = DecimalFormat("#,###").apply {
//            maximumFractionDigits = 0
//        }
        val decimalFormat = NumberFormat.getCurrencyInstance(Locale("id")).apply {
            maximumFractionDigits = 0
            (this as? DecimalFormat)?.decimalFormatSymbols =
                (this as? DecimalFormat)?.decimalFormatSymbols?.apply { currencySymbol = "" }
        }
        return decimalFormat.format(amount)
    }
}