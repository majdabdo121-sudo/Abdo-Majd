package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.model.WeatherConditionCategory

@Composable
fun WeatherAtmosphere(
    category: WeatherConditionCategory,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val gradientColors = when {
        !isDay -> listOf(
            Color(0xFF0A0F1D),
            Color(0xFF0F172A),
            Color(0xFF1E1B4B),
            Color(0xFF111827)
        )
        category == WeatherConditionCategory.CLEAR -> listOf(
            Color(0xFF0284C7),
            Color(0xFF0EA5E9),
            Color(0xFF38BDF8),
            Color(0xFF7DD3FC)
        )
        category == WeatherConditionCategory.PARTLY_CLOUDY -> listOf(
            Color(0xFF0369A1),
            Color(0xFF0284C7),
            Color(0xFF64748B),
            Color(0xFF94A3B8)
        )
        category == WeatherConditionCategory.CLOUDY || category == WeatherConditionCategory.FOG -> listOf(
            Color(0xFF334155),
            Color(0xFF475569),
            Color(0xFF64748B),
            Color(0xFF94A3B8)
        )
        category == WeatherConditionCategory.DRIZZLE || category == WeatherConditionCategory.RAIN -> listOf(
            Color(0xFF1E293B),
            Color(0xFF0F766E),
            Color(0xFF164E63),
            Color(0xFF0E7490)
        )
        category == WeatherConditionCategory.THUNDERSTORM -> listOf(
            Color(0xFF09090B),
            Color(0xFF2E1065),
            Color(0xFF3B0764),
            Color(0xFF18181B)
        )
        category == WeatherConditionCategory.SNOW -> listOf(
            Color(0xFF1E3A8A),
            Color(0xFF2563EB),
            Color(0xFF60A5FA),
            Color(0xFFBAE6FD)
        )
        else -> listOf(
            Color(0xFF0284C7),
            Color(0xFF0EA5E9),
            Color(0xFF38BDF8)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "atmosphere")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween( durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        // Decorative canvas glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (isDay && (category == WeatherConditionCategory.CLEAR || category == WeatherConditionCategory.PARTLY_CLOUDY)) {
                // Sun atmospheric golden radial bloom
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x66FDE047),
                            Color(0x33F59E0B),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.8f, height * 0.15f),
                        radius = (width * 0.6f) + (pulse * 25f)
                    ),
                    center = Offset(width * 0.8f, height * 0.15f),
                    radius = (width * 0.6f) + (pulse * 25f)
                )
            } else if (!isDay) {
                // Subtle stars & moon halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x44818CF8),
                            Color(0x116366F1),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.82f, height * 0.12f),
                        radius = width * 0.45f
                    ),
                    center = Offset(width * 0.82f, height * 0.12f),
                    radius = width * 0.45f
                )
            }
        }

        content()
    }
}
