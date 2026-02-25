package com.emissor.mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.emissor.mobile.data.local.dao.ItemDao
import com.emissor.mobile.data.local.entity.ItemEntity

@Database(
    entities = [ItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun itemDao(): ItemDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "emissor_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
