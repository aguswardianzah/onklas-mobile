package id.onklas.app.db

import androidx.room.TypeConverter
import java.util.*

class DbConverter {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date {
        return value?.let { Date(it) } ?: Date()
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long {
        return date?.time ?: 0
    }
}