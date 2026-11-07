package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.local.CityDao
import com.example.data.local.SavedCity
import com.example.data.model.GeocodingResult
import com.example.data.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WeatherRepository(private val cityDao: CityDao) {

    val savedCities: Flow<List<SavedCity>> = cityDao.getAllCities()

    suspend fun getForecast(latitude: Double, longitude: Double): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.weatherService.getForecast(latitude, longitude)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchCities(query: String): Result<List<GeocodingResult>> {
        return withContext(Dispatchers.IO) {
            try {
                if (query.isBlank()) return@withContext Result.success(emptyList())
                val response = ApiClient.geocodingService.searchCities(name = query.trim())
                Result.success(response.results ?: emptyList())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun saveCity(city: SavedCity): Long {
        return withContext(Dispatchers.IO) {
            cityDao.insertCity(city)
        }
    }

    suspend fun deleteCity(city: SavedCity) {
        withContext(Dispatchers.IO) {
            cityDao.deleteCity(city)
        }
    }

    suspend fun updateCachedWeather(cityId: Long, temp: Double, code: Int, condition: String) {
        withContext(Dispatchers.IO) {
            val city = cityDao.getCityById(cityId)
            if (city != null) {
                cityDao.updateCity(
                    city.copy(
                        cachedTemp = temp,
                        cachedWeatherCode = code,
                        cachedCondition = condition
                    )
                )
            }
        }
    }

    suspend fun isCitySaved(latitude: Double, longitude: Double): Boolean {
        return withContext(Dispatchers.IO) {
            cityDao.findByCoords(latitude, longitude) != null
        }
    }

    suspend fun getSavedCityByCoords(latitude: Double, longitude: Double): SavedCity? {
        return withContext(Dispatchers.IO) {
            cityDao.findByCoords(latitude, longitude)
        }
    }
}
