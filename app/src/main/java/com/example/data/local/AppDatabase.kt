package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [SavedCity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meteo_weather_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).cityDao()
                                dao.insertAll(
                                    listOf(
                                        SavedCity(
                                            name = "Paris",
                                            country = "France",
                                            admin1 = "Île-de-France",
                                            latitude = 48.8566,
                                            longitude = 2.3522,
                                            orderIndex = 0
                                        ),
                                        SavedCity(
                                            name = "Marseille",
                                            country = "France",
                                            admin1 = "Provence-Alpes-Côte d'Azur",
                                            latitude = 43.2965,
                                            longitude = 5.3698,
                                            orderIndex = 1
                                        ),
                                        SavedCity(
                                            name = "Lyon",
                                            country = "France",
                                            admin1 = "Auvergne-Rhône-Alpes",
                                            latitude = 45.7640,
                                            longitude = 4.8357,
                                            orderIndex = 2
                                        ),
                                        SavedCity(
                                            name = "Montréal",
                                            country = "Canada",
                                            admin1 = "Québec",
                                            latitude = 45.5017,
                                            longitude = -73.5673,
                                            orderIndex = 3
                                        )
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
