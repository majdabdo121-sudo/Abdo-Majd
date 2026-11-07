package com.example.data.model

enum class AlertType {
    HEAVY_RAIN,
    THUNDERSTORM,
    HIGH_WIND,
    TEMP_DROP
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class WeatherAlert(
    val type: AlertType,
    val title: String,
    val message: String,
    val severity: AlertSeverity,
    val timestamp: String? = null
)

data class AlertSettings(
    val systemNotificationsEnabled: Boolean = true,
    val heavyRainEnabled: Boolean = true,
    val rainProbThreshold: Int = 70,
    val thunderstormEnabled: Boolean = true,
    val highWindEnabled: Boolean = true,
    val windThresholdKmh: Int = 50,
    val tempDropEnabled: Boolean = true,
    val tempDropThreshold: Int = 6
)
