package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "timezone") val timezone: String?,
    @Json(name = "current") val current: CurrentWeather?,
    @Json(name = "hourly") val hourly: HourlyWeather?,
    @Json(name = "daily") val daily: DailyWeather?
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    @Json(name = "time") val time: String,
    @Json(name = "temperature_2m") val temperature: Double,
    @Json(name = "relative_humidity_2m") val relativeHumidity: Int,
    @Json(name = "apparent_temperature") val apparentTemperature: Double,
    @Json(name = "is_day") val isDay: Int,
    @Json(name = "precipitation") val precipitation: Double,
    @Json(name = "weather_code") val weatherCode: Int,
    @Json(name = "surface_pressure") val surfacePressure: Double,
    @Json(name = "wind_speed_10m") val windSpeed: Double,
    @Json(name = "wind_direction_10m") val windDirection: Int
)

@JsonClass(generateAdapter = true)
data class HourlyWeather(
    @Json(name = "time") val time: List<String>,
    @Json(name = "temperature_2m") val temperature: List<Double>,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>?,
    @Json(name = "weather_code") val weatherCode: List<Int>,
    @Json(name = "wind_speed_10m") val windSpeed: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class DailyWeather(
    @Json(name = "time") val time: List<String>,
    @Json(name = "weather_code") val weatherCode: List<Int>,
    @Json(name = "temperature_2m_max") val temperatureMax: List<Double>,
    @Json(name = "temperature_2m_min") val temperatureMin: List<Double>,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>?,
    @Json(name = "sunrise") val sunrise: List<String>?,
    @Json(name = "sunset") val sunset: List<String>?,
    @Json(name = "uv_index_max") val uvIndexMax: List<Double>?
)

enum class WeatherConditionCategory {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    THUNDERSTORM
}

data class WeatherInfo(
    val titleFr: String,
    val category: WeatherConditionCategory
)

object WeatherCodeMapper {
    fun getWeatherInfo(code: Int, isDay: Boolean = true): WeatherInfo {
        return when (code) {
            0 -> WeatherInfo(if (isDay) "Ciel dégagé" else "Nuit étoilée", WeatherConditionCategory.CLEAR)
            1 -> WeatherInfo(if (isDay) "Ensoleillé" else "Nuit claire", WeatherConditionCategory.CLEAR)
            2 -> WeatherInfo("Éclaircies", WeatherConditionCategory.PARTLY_CLOUDY)
            3 -> WeatherInfo("Nuageux", WeatherConditionCategory.CLOUDY)
            45, 48 -> WeatherInfo("Brouillard", WeatherConditionCategory.FOG)
            51, 53, 55 -> WeatherInfo("Bruine", WeatherConditionCategory.DRIZZLE)
            56, 57 -> WeatherInfo("Bruine verglaçante", WeatherConditionCategory.DRIZZLE)
            61, 63 -> WeatherInfo("Pluie", WeatherConditionCategory.RAIN)
            65 -> WeatherInfo("Forte pluie", WeatherConditionCategory.RAIN)
            66, 67 -> WeatherInfo("Pluie verglaçante", WeatherConditionCategory.RAIN)
            71, 73 -> WeatherInfo("Chutes de neige", WeatherConditionCategory.SNOW)
            75 -> WeatherInfo("Forte neige", WeatherConditionCategory.SNOW)
            77 -> WeatherInfo("Grains de neige", WeatherConditionCategory.SNOW)
            80, 81, 82 -> WeatherInfo("Averses", WeatherConditionCategory.RAIN)
            85, 86 -> WeatherInfo("Averses de neige", WeatherConditionCategory.SNOW)
            95 -> WeatherInfo("Orage", WeatherConditionCategory.THUNDERSTORM)
            96, 99 -> WeatherInfo("Orage avec grêle", WeatherConditionCategory.THUNDERSTORM)
            else -> WeatherInfo("Variable", WeatherConditionCategory.PARTLY_CLOUDY)
        }
    }
}
