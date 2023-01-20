package id.onklas.app.utils

import java.util.*

class RomanNumber {

    companion object {
        private val map: TreeMap<Int, String> = TreeMap<Int, String>().apply {
            put(1000, "M")
            put(900, "CM")
            put(500, "D")
            put(400, "CD")
            put(100, "C")
            put(90, "XC")
            put(50, "L")
            put(40, "XL")
            put(10, "X")
            put(9, "IX")
            put(5, "V")
            put(4, "IV")
            put(1, "I")
        }

        fun toRoman(number: Int): String? {
            val l: Int = map.floorKey(number) ?: 1
            return if (number == l) {
                map[number]
            } else map[l].toString() + toRoman(number - l)
        }
    }
}