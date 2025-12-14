package com.example.organizador.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.organizador.data.local.dao.ActivityDao
import com.example.organizador.data.local.dao.CategoryDao
import com.example.organizador.data.local.entities.ActivityEntity
import com.example.organizador.data.local.entities.CategoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ActivityEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "organizador_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.categoryDao())
                    }
                }
            }
            
            suspend fun populateDatabase(categoryDao: CategoryDao) {
                 if (categoryDao.getCount() == 0) {
                     categoryDao.insertCategory(CategoryEntity(name = "Estudio", isDefault = true))
                     categoryDao.insertCategory(CategoryEntity(name = "Trabajo", isDefault = true))
                     categoryDao.insertCategory(CategoryEntity(name = "Hogar", isDefault = true))
                 }
            }
        }
    }
}
