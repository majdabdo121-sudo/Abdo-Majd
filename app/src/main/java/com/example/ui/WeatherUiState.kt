package com.example.ui

import com.example.data.local.SavedCity
import com.example.data.model.GeocodingResult
import com.example.data.model.WeatherResponse

enum class TempUnit {
    CELSIUS,
    FAHRENHEIT
}

data class CurrentCity(
    val id: Long? = null,
    val name: String,
    val country: String,
    val admin1: String? = null,
    val latitude: Double,
    val longitude: Double,
    val isCurrentLocation: Boolean = false
)

data class WeatherUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val currentCity: CurrentCity = CurrentCity(
        name = "Paris",
        country = "France",
        admin1 = "Île-de-France",
        latitude = 48.8566,
        longitude = 2.3522
    ),
    val weather: WeatherResponse? = null,
    val isFavorite: Boolean = true,
    val tempUnit: TempUnit = TempUnit.CELSIUS,
    val searchQuery: String = "",
    val searchResults: List<GeocodingResult> = emptyList(),
    val isSearching: Boolean = false,
    val savedCities: List<SavedCity> = emptyList(),
    val lastUpdated: String? = null,
    val alerts: List<com.example.data.model.WeatherAlert> = emptyList(),
    val alertSettings: com.example.data.model.AlertSettings = com.example.data.model.AlertSettings()
)
