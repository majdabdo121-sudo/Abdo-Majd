package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSettings
import com.example.data.model.WeatherAlert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsSheet(
    activeAlerts: List<WeatherAlert>,
    settings: AlertSettings,
    cityName: String,
    onUpdateSettings: (AlertSettings) -> Unit,
    onTestNotification: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxHeight(0.9f)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Alertes météo personnalisées",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Pour $cityName et vos lieux enregistrés",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Active alerts section
            if (activeAlerts.isNotEmpty()) {
                item {
                    Text(
                        text = "ALERTES EN COURS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            letterSpacing = 1.sp
                        )
                    )
                }

                items(activeAlerts) { alert ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = alert.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = alert.message,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                    }
                }
            }

            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            // System notifications master toggle
            item {
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
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Notifications système",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Recevoir les alertes dans la barre d'état",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Switch(
                        checked = settings.systemNotificationsEnabled,
                        onCheckedChange = {
                            onUpdateSettings(settings.copy(systemNotificationsEnabled = it))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("system_notifications_switch")
                    )
                }
            }

            item {
                Text(
                    text = "TYPES D'ÉVÉNEMENTS À SURVEILLER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                )
            }

            // 1. Heavy Rain
            item {
                AlertToggleRow(
                    icon = Icons.Default.WaterDrop,
                    title = "Fortes pluies",
                    subtitle = "Alerte si probabilité de pluie > ${settings.rainProbThreshold}%",
                    enabled = settings.heavyRainEnabled,
                    onToggle = { onUpdateSettings(settings.copy(heavyRainEnabled = it)) },
                    thresholdSelector = {
                        ThresholdSelector(
                            options = listOf(50, 70, 85),
                            selected = settings.rainProbThreshold,
                            suffix = "%",
                            onSelect = { onUpdateSettings(settings.copy(rainProbThreshold = it)) }
                        )
                    }
                )
            }

            // 2. Thunderstorm
            item {
                AlertToggleRow(
                    icon = Icons.Default.Thunderstorm,
                    title = "Orages et foudre",
                    subtitle = "Alerte immédiate en cas de risque orageux",
                    enabled = settings.thunderstormEnabled,
                    onToggle = { onUpdateSettings(settings.copy(thunderstormEnabled = it)) }
                )
            }

            // 3. High Wind
            item {
                AlertToggleRow(
                    icon = Icons.Default.Air,
                    title = "Vents violents",
                    subtitle = "Alerte si les rafales dépassent ${settings.windThresholdKmh} km/h",
                    enabled = settings.highWindEnabled,
                    onToggle = { onUpdateSettings(settings.copy(highWindEnabled = it)) },
                    thresholdSelector = {
                        ThresholdSelector(
                            options = listOf(40, 50, 65),
                            selected = settings.windThresholdKmh,
                            suffix = " km/h",
                            onSelect = { onUpdateSettings(settings.copy(windThresholdKmh = it)) }
                        )
                    }
                )
            }

            // 4. Temperature Drop
            item {
                AlertToggleRow(
                    icon = Icons.Default.Thermostat,
                    title = "Chutes brutales de température",
                    subtitle = "Alerte si la température chute de plus de ${settings.tempDropThreshold}°C",
                    enabled = settings.tempDropEnabled,
                    onToggle = { onUpdateSettings(settings.copy(tempDropEnabled = it)) },
                    thresholdSelector = {
                        ThresholdSelector(
                            options = listOf(4, 6, 8),
                            selected = settings.tempDropThreshold,
                            suffix = "°C",
                            onSelect = { onUpdateSettings(settings.copy(tempDropThreshold = it)) }
                        )
                    }
                )
            }

            // Test Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onTestNotification,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_alert_notification_button")
                ) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tester une notification d'alerte", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun AlertToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    thresholdSelector: (@Composable () -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }

            if (enabled && thresholdSelector != null) {
                Spacer(modifier = Modifier.height(10.dp))
                thresholdSelector()
            }
        }
    }
}

@Composable
private fun ThresholdSelector(
    options: List<Int>,
    selected: Int,
    suffix: String,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { value ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                    )
                    .clickable { onSelect(value) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$value$suffix",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}
