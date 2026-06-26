package com.cryptvault.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

private data class Orb(
    val radiusFraction: Float,
    val color: Color,
    val phaseOffset: Float,
    val speed: Float,
    val orbitTilt: Float,
)

@Composable
fun AuroraBackground(
    baseColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val orbs = remember(baseColor, accentColor) {
        listOf(
            Orb(0.55f, accentColor.copy(alpha = 0.30f), 0f, 0.35f, 0.2f),
            Orb(0.45f, baseColor.copy(alpha = 0.28f), 2.1f, 0.22f, 0.6f),
            Orb(0.35f, accentColor.copy(alpha = 0.20f), 4.2f, 0.45f, 1.0f),
        )
    }
    val transition = rememberInfiniteTransition(label = "aurora")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "angle",
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension * 0.9f
        orbs.forEach { orb ->
            val a = angle * orb.speed + orb.phaseOffset
            val tx = center.x + cos(a.toDouble()).toFloat() * maxR * 0.45f
            val ty = center.y + sin(a.toDouble() * (1.0 + orb.orbitTilt)).toFloat() * maxR * 0.30f
            val radius = size.minDimension * orb.radiusFraction
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb.color, orb.color.copy(alpha = 0f)),
                    center = Offset(tx, ty),
                    radius = radius,
                ),
                radius = radius,
                center = Offset(tx, ty),
            )
        }
    }
}