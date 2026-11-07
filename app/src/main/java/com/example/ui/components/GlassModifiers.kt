package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Creates an iOS glass effect with layered translucency,
 * subtle linear top-to-bottom frosted reflection, and a specular border.
 */
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(24.dp),
    baseAlpha: Float = 0.16f,
    borderAlpha: Float = 0.35f,
    elevation: Dp = 0.dp
): Modifier = this
    .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape) else Modifier)
    .clip(shape)
    .background(
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = baseAlpha + 0.08f),
                Color.White.copy(alpha = baseAlpha)
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = borderAlpha),
                Color.White.copy(alpha = borderAlpha * 0.4f)
            )
        ),
        shape = shape
    )
