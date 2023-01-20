package id.onklas.app.di.modules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.PersistentDB
import javax.inject.Singleton

@Module
class DbModules {

    @Singleton
    @Provides
    fun instance(context: Context): MemoryDB =
//        Room.inMemoryDatabaseBuilder(context, MemoryDB::class.java)
        Room.databaseBuilder(context, MemoryDB::class.java, "onklas.db")
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun instanceCache(context: Context): PersistentDB =
        Room.databaseBuilder(context, PersistentDB::class.java, "onklas.db")
//        Room.inMemoryDatabaseBuilder(context, PersistentDB::class.java)
            .fallbackToDestructiveMigration()
            .build()
}