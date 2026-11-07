package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Query("SELECT * FROM saved_cities ORDER BY orderIndex ASC, id ASC")
    fun getAllCities(): Flow<List<SavedCity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: SavedCity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(cities: List<SavedCity>)

    @Update
    suspend fun updateCity(city: SavedCity)

    @Delete
    suspend fun deleteCity(city: SavedCity)

    @Query("SELECT * FROM saved_cities WHERE id = :id LIMIT 1")
    suspend fun getCityById(id: Long): SavedCity?

    @Query("SELECT * FROM saved_cities WHERE ABS(latitude - :lat) < 0.05 AND ABS(longitude - :lon) < 0.05 LIMIT 1")
    suspend fun findByCoords(lat: Double, lon: Double): SavedCity?

    @Query("SELECT COUNT(*) FROM saved_cities")
    suspend fun getCityCount(): Int
}
