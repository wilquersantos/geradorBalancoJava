package com.emissor.mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.emissor.mobile.data.local.dao.CollectionGroupDao
import com.emissor.mobile.data.local.dao.ItemDao
import com.emissor.mobile.data.local.entity.CollectionGroupEntity
import com.emissor.mobile.data.local.entity.ItemEntity

@Database(
    entities = [ItemEntity::class, CollectionGroupEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun itemDao(): ItemDao
    abstract fun collectionGroupDao(): CollectionGroupDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "emissor_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
