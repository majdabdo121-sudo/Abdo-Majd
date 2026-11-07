package com.example.ui.components

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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyWeather
import com.example.ui.TempUnit
import kotlin.math.roundToInt

@Composable
fun WeatherDetailsGrid(
    current: CurrentWeather?,
    daily: DailyWeather?,
    tempUnit: TempUnit,
    modifier: Modifier = Modifier
) {
    if (current == null) return

    val uvMax = daily?.uvIndexMax?.firstOrNull() ?: 2.0
    val uvCategory = when {
        uvMax < 3 -> "Faible"
        uvMax < 6 -> "Modéré"
        uvMax < 8 -> "Élevé"
        uvMax < 11 -> "Très élevé"
        else -> "Extrême"
    }

    val windDirectionText = when (current.windDirection) {
        in 23..67 -> "Nord-Est"
        in 68..112 -> "Est"
        in 113..157 -> "Sud-Est"
        in 158..202 -> "Sud"
        in 203..247 -> "Sud-Ouest"
        in 248..292 -> "Ouest"
        in 293..337 -> "Nord-Ouest"
        else -> "Nord"
    }

    val sunriseTime = daily?.sunrise?.firstOrNull()?.split("T")?.getOrNull(1) ?: "--:--"
    val sunsetTime = daily?.sunset?.firstOrNull()?.split("T")?.getOrNull(1) ?: "--:--"

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Ressenti & Vent
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailTile(
                icon = Icons.Default.Thermostat,
                label = "RESSENTI",
                value = WeatherFormatters.formatTemp(current.apparentTemperature, tempUnit),
                subtitle = if (current.apparentTemperature < current.temperature) "Plus frais à cause du vent" else "Température similaire",
                modifier = Modifier.weight(1f)
            )

            DetailTile(
                icon = Icons.Default.Air,
                label = "VENT",
                value = "${current.windSpeed.roundToInt()} km/h",
                subtitle = "Direction : $windDirectionText",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Humidité & Indice UV
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailTile(
                icon = Icons.Default.WaterDrop,
                label = "HUMIDITÉ",
                value = "${current.relativeHumidity}%",
                subtitle = if (current.relativeHumidity > 70) "Humide" else "Confortable",
                modifier = Modifier.weight(1f)
            )

            DetailTile(
                icon = Icons.Default.WbSunny,
                label = "INDICE UV",
                value = "${uvMax.roundToInt()} • $uvCategory",
                subtitle = if (uvMax >= 3) "Protection solaire recommandée" else "Aucune protection requise",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Pression & Soleil
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailTile(
                icon = Icons.Default.Compress,
                label = "PRESSION",
                value = "${current.surfacePressure.roundToInt()} hPa",
                subtitle = if (current.surfacePressure > 1013) "Haute pression (Beau temps)" else "Basse pression",
                modifier = Modifier.weight(1f)
            )

            DetailTile(
                icon = Icons.Default.WbTwilight,
                label = "SOLEIL",
                value = "🌅 $sunriseTime",
                subtitle = "Coucher à 🌇 $sunsetTime",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DetailTile(
    icon: ImageVector,
    label: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .glassCard(shape = RoundedCornerShape(22.dp), baseAlpha = 0.16f, borderAlpha = 0.35f)
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.75f)
                ),
                maxLines = 2
            )
        }
    }
}
