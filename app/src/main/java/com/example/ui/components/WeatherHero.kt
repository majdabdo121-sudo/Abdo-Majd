package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeatherCodeMapper
import com.example.data.model.WeatherResponse
import com.example.ui.CurrentCity
import com.example.ui.TempUnit

@Composable
fun WeatherHero(
    city: CurrentCity,
    weather: WeatherResponse?,
    isFavorite: Boolean,
    isRefreshing: Boolean,
    tempUnit: TempUnit,
    lastUpdated: String?,
    onToggleFavorite: () -> Unit,
    onRefresh: () -> Unit,
    onToggleTempUnit: () -> Unit,
    onOpenAlerts: () -> Unit,
    alertsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val current = weather?.current
    val daily = weather?.daily
    val isDay = (current?.isDay ?: 1) == 1
    val info = current?.let { WeatherCodeMapper.getWeatherInfo(it.weatherCode, isDay) }
    val todayMax = daily?.temperatureMax?.firstOrNull()
    val todayMin = daily?.temperatureMin?.firstOrNull()

    val refreshRotation by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        label = "refreshRotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Location Bar with Favorite and Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (city.isCurrentLocation) Icons.Default.MyLocation else Icons.Default.LocationOn,
                    contentDescription = "Position",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    val subtitle = when {
                        city.admin1 != null && city.country.isNotEmpty() -> "${city.admin1}, ${city.country}"
                        city.country.isNotEmpty() -> city.country
                        else -> null
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Unit Switcher Button (°C / °F)
                Surface(
                    onClick = onToggleTempUnit,
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .testTag("temp_unit_toggle")
                        .padding(end = 4.dp)
                ) {
                    Text(
                        text = if (tempUnit == TempUnit.CELSIUS) "°C" else "°F",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Bookmark / Favorite
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("favorite_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                        tint = if (isFavorite) Color(0xFFFDE047) else Color.White
                    )
                }

                // Alerts Bell Button
                IconButton(
                    onClick = onOpenAlerts,
                    modifier = Modifier.testTag("alerts_bell_button")
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = if (alertsCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = "Alertes météo",
                            tint = if (alertsCount > 0) Color(0xFFFDE047) else Color.White
                        )
                        if (alertsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                        }
                    }
                }

                // Refresh Button
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("refresh_weather_button")
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualiser",
                            tint = Color.White,
                            modifier = Modifier.rotate(refreshRotation)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Big Weather Condition Icon & Big Temperature
        if (current != null && info != null) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = WeatherFormatters.getConditionIcon(info.category, isDay),
                    contentDescription = info.titleFr,
                    tint = if (isDay && info.category == com.example.data.model.WeatherConditionCategory.CLEAR) Color(0xFFFDE047) else Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Temperature
            Text(
                text = WeatherFormatters.formatTemp(current.temperature, tempUnit),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 76.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
            )

            // Condition Title
            Text(
                text = info.titleFr,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Min / Max & Feels like
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (todayMax != null && todayMin != null) {
                    Text(
                        text = "Max ${WeatherFormatters.formatTemp(todayMax, tempUnit)}  •  Min ${WeatherFormatters.formatTemp(todayMin, tempUnit)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = "Ressenti ${WeatherFormatters.formatTemp(current.apparentTemperature, tempUnit)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }

            if (lastUpdated != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Mis à jour à $lastUpdated",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.65f)
                    )
                )
            }
        }
    }
}
