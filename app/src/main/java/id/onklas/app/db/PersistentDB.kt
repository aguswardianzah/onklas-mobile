package id.onklas.app.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import id.onklas.app.pages.ujian.*

@Database(
    version = 18,
    exportSchema = false,
    entities = [
        ExamTable::class,
        QuestionTable::class,
        AnswerTable::class,
        MyAnswerTable::class,
    ]
)
@TypeConverters(DbConverter::class)
abstract class PersistentDB : RoomDatabase() {

    abstract fun ujian(): UjianDao
}