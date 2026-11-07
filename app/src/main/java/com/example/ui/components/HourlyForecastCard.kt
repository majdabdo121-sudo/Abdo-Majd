package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HourlyWeather
import com.example.data.model.WeatherCodeMapper
import com.example.ui.TempUnit
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HourlyForecastCard(
    hourly: HourlyWeather?,
    tempUnit: TempUnit,
    modifier: Modifier = Modifier
) {
    if (hourly == null || hourly.time.isEmpty()) return

    var selectedHourRange by remember { mutableIntStateOf(24) } // 24 or 48

    val totalAvailable = hourly.time.size
    val displayCount = minOf(selectedHourRange, totalAvailable)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(26.dp), baseAlpha = 0.16f, borderAlpha = 0.4f)
            .padding(vertical = 16.dp)
    ) {
        Column {
            // Header with range switcher (24h / 48h)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PRÉVISIONS HORAIRES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 1.sp
                        )
                    )
                }

                // Range Toggle (24h / 48h)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(2.dp)
                ) {
                    RangeTab(
                        label = "24h",
                        isSelected = selectedHourRange == 24,
                        onClick = { selectedHourRange = 24 },
                        tag = "hourly_tab_24h"
                    )
                    RangeTab(
                        label = "48h",
                        isSelected = selectedHourRange == 48,
                        onClick = { selectedHourRange = 48 },
                        tag = "hourly_tab_48h"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayCount) { index ->
                    val rawTime = hourly.time.getOrNull(index) ?: ""
                    val temp = hourly.temperature.getOrNull(index)
                    val code = hourly.weatherCode.getOrNull(index) ?: 0
                    val precipProb = hourly.precipitationProbability?.getOrNull(index) ?: 0
                    val windSpeed = hourly.windSpeed?.getOrNull(index)

                    // Format Time and Day indicator
                    val (hourLabel, daySubtitle) = formatHourAndDay(rawTime, index)

                    val hourInt = rawTime.split("T").getOrNull(1)?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 12
                    val isHourDay = hourInt in 7..20
                    val info = WeatherCodeMapper.getWeatherInfo(code, isHourDay)

                    HourlyItem(
                        time = hourLabel,
                        daySubtitle = daySubtitle,
                        temp = WeatherFormatters.formatTemp(temp, tempUnit),
                        precipProb = precipProb,
                        windSpeedKmh = windSpeed,
                        category = info.category,
                        isDay = isHourDay,
                        isCurrentHour = index == 0
                    )
                }
            }
        }
    }
}

@Composable
private fun RangeTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.35f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = Color.White
            )
        )
    }
}

private fun formatHourAndDay(rawTime: String, index: Int): Pair<String, String?> {
    if (index == 0) return Pair("Maint.", null)

    val parts = rawTime.split("T")
    if (parts.size < 2) return Pair(rawTime, null)

    val hourPart = parts[1].split(":").firstOrNull()?.let { "${it}h" } ?: rawTime

    // Detect if this time belongs to tomorrow or the day after
    var dayTag: String? = null
    try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = parser.parse(parts[0])
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())
        if (parts[0] != todayStr && date != null) {
            val dayName = SimpleDateFormat("EEE", Locale.FRENCH).format(date)
            dayTag = dayName.replace(".", "").replaceFirstChar { it.uppercase() }
        }
    } catch (e: Exception) {
        // Fallback
    }

    return Pair(hourPart, dayTag)
}

@Composable
private fun HourlyItem(
    time: String,
    daySubtitle: String?,
    temp: String,
    precipProb: Int,
    windSpeedKmh: Double?,
    category: com.example.data.model.WeatherConditionCategory,
    isDay: Boolean,
    isCurrentHour: Boolean
) {
    Box(
        modifier = Modifier
            .width(76.dp)
            .glassCard(
                shape = RoundedCornerShape(20.dp),
                baseAlpha = if (isCurrentHour) 0.30f else 0.12f,
                borderAlpha = if (isCurrentHour) 0.55f else 0.20f
            )
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Day tag if tomorrow/next day
            if (daySubtitle != null) {
                Text(
                    text = daySubtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Hour
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isCurrentHour) FontWeight.Bold else FontWeight.Medium,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Weather Icon
            Icon(
                imageVector = WeatherFormatters.getConditionIcon(category, isDay),
                contentDescription = null,
                tint = if (isDay && category == com.example.data.model.WeatherConditionCategory.CLEAR) Color(0xFFFDE047) else Color.White,
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Temperature
            Text(
                text = temp,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Precipitation probability
            if (precipProb > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "$precipProb%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Wind Speed
            if (windSpeedKmh != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${windSpeedKmh.roundToInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    )
                    Text(
                        text = "k",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    )
                }
            }
        }
    }
}
