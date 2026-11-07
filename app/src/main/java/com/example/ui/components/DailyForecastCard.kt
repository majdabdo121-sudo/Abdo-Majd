package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyWeather
import com.example.data.model.WeatherCodeMapper
import com.example.ui.TempUnit
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DailyForecastCard(
    daily: DailyWeather?,
    tempUnit: TempUnit,
    modifier: Modifier = Modifier
) {
    if (daily == null || daily.time.isEmpty()) return

    val daysCount = minOf(7, daily.time.size)
    val globalMin = daily.temperatureMin.take(daysCount).minOrNull() ?: 0.0
    val globalMax = daily.temperatureMax.take(daysCount).maxOrNull() ?: 30.0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(26.dp), baseAlpha = 0.16f, borderAlpha = 0.4f)
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PRÉVISIONS SUR 7 JOURS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (i in 0 until daysCount) {
                    val dateStr = daily.time.getOrNull(i) ?: ""
                    val code = daily.weatherCode.getOrNull(i) ?: 0
                    val minTemp = daily.temperatureMin.getOrNull(i) ?: 0.0
                    val maxTemp = daily.temperatureMax.getOrNull(i) ?: 0.0
                    val precipProb = daily.precipitationProbabilityMax?.getOrNull(i) ?: 0

                    val dayName = if (i == 0) {
                        "Aujourd'hui"
                    } else {
                        try {
                            val inFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val date = inFormat.parse(dateStr)
                            val outFormat = SimpleDateFormat("EEEE", Locale.FRENCH)
                            date?.let { outFormat.format(it).replaceFirstChar { c -> c.uppercase() } } ?: dateStr
                        } catch (e: Exception) {
                            dateStr
                        }
                    }

                    val info = WeatherCodeMapper.getWeatherInfo(code, isDay = true)

                    DailyRow(
                        dayName = dayName,
                        category = info.category,
                        precipProb = precipProb,
                        minTemp = minTemp,
                        maxTemp = maxTemp,
                        globalMin = globalMin,
                        globalMax = globalMax,
                        tempUnit = tempUnit
                    )

                    if (i < daysCount - 1) {
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.12f),
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyRow(
    dayName: String,
    category: com.example.data.model.WeatherConditionCategory,
    precipProb: Int,
    minTemp: Double,
    maxTemp: Double,
    globalMin: Double,
    globalMax: Double,
    tempUnit: TempUnit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day name
        Text(
            text = dayName,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            ),
            modifier = Modifier.width(96.dp)
        )

        // Weather Icon + Rain Prob
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(64.dp)
        ) {
            Icon(
                imageVector = WeatherFormatters.getConditionIcon(category, isDay = true),
                contentDescription = null,
                tint = if (category == com.example.data.model.WeatherConditionCategory.CLEAR) Color(0xFFFDE047) else Color.White,
                modifier = Modifier.size(24.dp)
            )
            if (precipProb > 10) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$precipProb%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Min Temp Label
        Text(
            text = WeatherFormatters.formatTemp(minTemp, tempUnit),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.75f)
            ),
            modifier = Modifier.width(36.dp)
        )

        // Temperature range visual indicator bar
        val totalRange = (globalMax - globalMin).coerceAtLeast(1.0)
        val startFraction = ((minTemp - globalMin) / totalRange).coerceIn(0.0, 1.0).toFloat()
        val endFraction = ((maxTemp - globalMin) / totalRange).coerceIn(0.0, 1.0).toFloat()

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (endFraction - startFraction).coerceAtLeast(0.15f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFFFBBF24))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Max Temp Label
        Text(
            text = WeatherFormatters.formatTemp(maxTemp, tempUnit),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.width(36.dp)
        )
    }
}
