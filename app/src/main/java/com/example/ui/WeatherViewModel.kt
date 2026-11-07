package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.SavedCity
import com.example.data.model.AlertSeverity
import com.example.data.model.AlertSettings
import com.example.data.model.AlertType
import com.example.data.model.GeocodingResult
import com.example.data.model.WeatherAlert
import com.example.data.model.WeatherCodeMapper
import com.example.data.model.WeatherResponse
import com.example.data.repository.WeatherRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val appContext: Context? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Observe saved cities from Room
        viewModelScope.launch {
            repository.savedCities.collect { cities ->
                _uiState.update { current ->
                    val isFav = cities.any {
                        Math.abs(it.latitude - current.currentCity.latitude) < 0.05 &&
                                Math.abs(it.longitude - current.currentCity.longitude) < 0.05
                    }
                    current.copy(
                        savedCities = cities,
                        isFavorite = isFav
                    )
                }
            }
        }

        // Initial weather load
        fetchWeather(_uiState.value.currentCity)
    }

    fun selectCity(city: CurrentCity) {
        _uiState.update { it.copy(currentCity = city) }
        fetchWeather(city)
    }

    fun selectSavedCity(savedCity: SavedCity) {
        val city = CurrentCity(
            id = savedCity.id,
            name = savedCity.name,
            country = savedCity.country,
            admin1 = savedCity.admin1,
            latitude = savedCity.latitude,
            longitude = savedCity.longitude,
            isCurrentLocation = savedCity.isCurrentLocation
        )
        selectCity(city)
    }

    fun selectSearchResult(result: GeocodingResult) {
        val city = CurrentCity(
            name = result.name,
            country = result.country ?: "",
            admin1 = result.admin1,
            latitude = result.latitude,
            longitude = result.longitude
        )
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),
                currentCity = city
            )
        }
        fetchWeather(city)
    }

    fun setLocation(latitude: Double, longitude: Double, cityName: String = "Ma position") {
        val city = CurrentCity(
            name = cityName,
            country = "Localisation GPS",
            latitude = latitude,
            longitude = longitude,
            isCurrentLocation = true
        )
        selectCity(city)
    }

    fun refreshWeather() {
        val current = _uiState.value.currentCity
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        fetchWeather(current, isRefresh = true)
    }

    private fun fetchWeather(city: CurrentCity, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            val result = repository.getForecast(city.latitude, city.longitude)
            val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            result.onSuccess { weatherResponse ->
                val current = weatherResponse.current
                if (current != null && city.id != null) {
                    val info = WeatherCodeMapper.getWeatherInfo(current.weatherCode, current.isDay == 1)
                    repository.updateCachedWeather(
                        cityId = city.id,
                        temp = current.temperature,
                        code = current.weatherCode,
                        condition = info.titleFr
                    )
                }

                val isFav = repository.isCitySaved(city.latitude, city.longitude)
                val detectedAlerts = computeAlerts(weatherResponse, _uiState.value.alertSettings)

                // Dispatch system notification if enabled and alerts detected
                if (_uiState.value.alertSettings.systemNotificationsEnabled && detectedAlerts.isNotEmpty() && appContext != null) {
                    detectedAlerts.firstOrNull()?.let { alert ->
                        NotificationHelper.sendAlertNotification(appContext, alert, city.name)
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        weather = weatherResponse,
                        errorMessage = null,
                        lastUpdated = nowTime,
                        isFavorite = isFav,
                        alerts = detectedAlerts
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = error.localizedMessage ?: "Erreur de connexion"
                    )
                }
            }
        }
    }

    private fun computeAlerts(weather: WeatherResponse, settings: AlertSettings): List<WeatherAlert> {
        val alerts = mutableListOf<WeatherAlert>()
        val current = weather.current
        val hourly = weather.hourly
        val daily = weather.daily

        // 1. Orages
        if (settings.thunderstormEnabled) {
            val isCurrentStorm = current?.weatherCode in listOf(95, 96, 99)
            val isUpcomingStorm = hourly?.weatherCode?.take(24)?.any { it in listOf(95, 96, 99) } == true
            if (isCurrentStorm || isUpcomingStorm) {
                alerts.add(
                    WeatherAlert(
                        type = AlertType.THUNDERSTORM,
                        title = "Vigilance Orages",
                        message = "Risque d'orages et d'activité électrique dans les prochaines heures. Prudence en extérieur.",
                        severity = AlertSeverity.CRITICAL
                    )
                )
            }
        }

        // 2. Vents violents
        if (settings.highWindEnabled) {
            val currentWind = current?.windSpeed ?: 0.0
            val maxHourlyWind = hourly?.windSpeed?.take(24)?.maxOrNull() ?: 0.0
            val maxWind = maxOf(currentWind, maxHourlyWind)

            if (maxWind >= settings.windThresholdKmh) {
                alerts.add(
                    WeatherAlert(
                        type = AlertType.HIGH_WIND,
                        title = "Alerte Vents Violents",
                        message = "Rafales prévues jusqu'à ${maxWind.roundToInt()} km/h (seuil fixé à ${settings.windThresholdKmh} km/h).",
                        severity = if (maxWind >= 70) AlertSeverity.CRITICAL else AlertSeverity.WARNING
                    )
                )
            }
        }

        // 3. Fortes pluies
        if (settings.heavyRainEnabled) {
            val maxPrecipProb = hourly?.precipitationProbability?.take(24)?.maxOrNull() ?: 0
            val isCurrentHeavyRain = current?.weatherCode in listOf(65, 81, 82) || (current?.precipitation ?: 0.0) >= 4.0

            if (isCurrentHeavyRain || maxPrecipProb >= settings.rainProbThreshold) {
                alerts.add(
                    WeatherAlert(
                        type = AlertType.HEAVY_RAIN,
                        title = "Alerte Fortes Pluies",
                        message = "Fortes précipitations attendues (${maxPrecipProb}% de probabilité). Risque de chaussées glissantes.",
                        severity = AlertSeverity.WARNING
                    )
                )
            }
        }

        // 4. Chute brutale de température
        if (settings.tempDropEnabled) {
            val hourlyTemps = hourly?.temperature?.take(24) ?: emptyList()
            if (hourlyTemps.size >= 2) {
                val tempMax = hourlyTemps.maxOrNull() ?: 0.0
                val tempMin = hourlyTemps.minOrNull() ?: 0.0
                val drop = tempMax - tempMin
                if (drop >= settings.tempDropThreshold) {
                    alerts.add(
                        WeatherAlert(
                            type = AlertType.TEMP_DROP,
                            title = "Chute de Température",
                            message = "Écart thermique important de ${drop.roundToInt()}°C prévu sur les prochaines 24h.",
                            severity = AlertSeverity.INFO
                        )
                    )
                }
            }
        }

        return alerts
    }

    fun updateAlertSettings(newSettings: AlertSettings) {
        _uiState.update { current ->
            val updatedAlerts = current.weather?.let { computeAlerts(it, newSettings) } ?: current.alerts
            current.copy(alertSettings = newSettings, alerts = updatedAlerts)
        }
    }

    fun testNotification(context: Context) {
        val testAlert = WeatherAlert(
            type = AlertType.THUNDERSTORM,
            title = "Test Alerte Météo",
            message = "Votre système d'alerte météo personnalisé fonctionne correctement !",
            severity = AlertSeverity.WARNING
        )
        NotificationHelper.sendAlertNotification(context, testAlert, _uiState.value.currentCity.name)
    }

    fun toggleFavorite() {
        val city = _uiState.value.currentCity
        viewModelScope.launch {
            val existing = repository.getSavedCityByCoords(city.latitude, city.longitude)
            if (existing != null) {
                repository.deleteCity(existing)
                _uiState.update { it.copy(isFavorite = false) }
            } else {
                val newId = repository.saveCity(
                    SavedCity(
                        name = city.name,
                        country = city.country,
                        admin1 = city.admin1,
                        latitude = city.latitude,
                        longitude = city.longitude,
                        isCurrentLocation = city.isCurrentLocation,
                        cachedTemp = _uiState.value.weather?.current?.temperature,
                        cachedWeatherCode = _uiState.value.weather?.current?.weatherCode,
                        cachedCondition = _uiState.value.weather?.current?.let {
                            WeatherCodeMapper.getWeatherInfo(it.weatherCode, it.isDay == 1).titleFr
                        }
                    )
                )
                _uiState.update {
                    it.copy(
                        isFavorite = true,
                        currentCity = city.copy(id = newId)
                    )
                }
            }
        }
    }

    fun deleteCity(savedCity: SavedCity) {
        viewModelScope.launch {
            repository.deleteCity(savedCity)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(350) // debounce
            _uiState.update { it.copy(isSearching = true) }
            val res = repository.searchCities(query)
            res.onSuccess { results ->
                _uiState.update { it.copy(searchResults = results, isSearching = false) }
            }.onFailure {
                _uiState.update { it.copy(isSearching = false) }
            }
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList(), isSearching = false) }
    }

    fun toggleTempUnit() {
        _uiState.update {
            it.copy(
                tempUnit = if (it.tempUnit == TempUnit.CELSIUS) TempUnit.FAHRENHEIT else TempUnit.CELSIUS
            )
        }
    }
}

class WeatherViewModelFactory(
    private val repository: WeatherRepository,
    private val context: Context? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
