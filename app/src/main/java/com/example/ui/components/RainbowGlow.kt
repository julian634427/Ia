package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.RainbowColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modern AI Studio Rainbow Border modifier with continuous fluid rotation animation.
 */
@Composable
fun Modifier.rainbowBorder(
    strokeWidth: Dp = 1.8.dp,
    shape: Shape = RoundedCornerShape(20.dp),
    isAnimated: Boolean = true,
    durationMillis: Int = 4000
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow_rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbow_angle"
    )

    val rad = (angle * PI / 180f).toFloat()
    val startX = 0.5f + 0.5f * cos(rad)
    val startY = 0.5f + 0.5f * sin(rad)
    val endX = 0.5f - 0.5f * cos(rad)
    val endY = 0.5f - 0.5f * sin(rad)

    val animatedBrush = Brush.linearGradient(
        colors = RainbowColors,
        start = Offset(startX * 1000f, startY * 1000f),
        end = Offset(endX * 1000f, endY * 1000f)
    )

    return this.border(
        width = strokeWidth,
        brush = animatedBrush,
        shape = shape
    )
}

/**
 * Pulsing glow effect when Gemini is thinking or streaming content.
 */
@Composable
fun Modifier.rainbowPulse(
    enabled: Boolean = true
): Modifier {
    if (!enabled) return this

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    return this.drawBehind {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    RainbowColors[1].copy(alpha = alpha * 0.4f),
                    RainbowColors[3].copy(alpha = alpha * 0.25f),
                    Color.Transparent
                ),
                radius = size.maxDimension * 0.75f
            )
        )
    }
}

/**
 * An iridescent Gemini Sparkle badge with rotating aura.
 */
@Composable
fun GeminiSparkleBadge(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    isThinking: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle_anim")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isThinking) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .rainbowPulse(enabled = isThinking),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Rainbow Ring
        Box(
            modifier = Modifier
                .size(size)
                .rainbowBorder(
                    strokeWidth = 2.dp,
                    shape = CircleShape,
                    isAnimated = true,
                    durationMillis = if (isThinking) 1800 else 4000
                )
        )
    }
}
