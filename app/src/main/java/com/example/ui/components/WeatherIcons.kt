package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.WeatherConditionCategory
import com.example.ui.TempUnit
import kotlin.math.roundToInt

object WeatherFormatters {
    fun formatTemp(celsius: Double?, unit: TempUnit): String {
        if (celsius == null) return "--°"
        return when (unit) {
            TempUnit.CELSIUS -> "${celsius.roundToInt()}°"
            TempUnit.FAHRENHEIT -> "${(celsius * 9 / 5 + 32).roundToInt()}°"
        }
    }

    fun formatWind(kmh: Double?): String {
        if (kmh == null) return "-- km/h"
        return "${kmh.roundToInt()} km/h"
    }

    fun getConditionIcon(category: WeatherConditionCategory, isDay: Boolean): ImageVector {
        return when (category) {
            WeatherConditionCategory.CLEAR -> if (isDay) Icons.Default.WbSunny else Icons.Default.NightsStay
            WeatherConditionCategory.PARTLY_CLOUDY -> Icons.Default.WbCloudy
            WeatherConditionCategory.CLOUDY -> Icons.Default.Cloud
            WeatherConditionCategory.FOG -> Icons.Default.Grain
            WeatherConditionCategory.DRIZZLE -> Icons.Default.WaterDrop
            WeatherConditionCategory.RAIN -> Icons.Default.WaterDrop
            WeatherConditionCategory.SNOW -> Icons.Default.AcUnit
            WeatherConditionCategory.THUNDERSTORM -> Icons.Default.Thunderstorm
        }
    }
}
