package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// AI Studio Core Dark Palette
val StudioBackground = Color(0xFF131314)
val StudioSurface = Color(0xFF1E1F20)
val StudioSurfaceVariant = Color(0xFF282A2C)
val StudioSurfaceHighlight = Color(0xFF333538)
val StudioBorder = Color(0xFF3C4043)
val StudioDivider = Color(0xFF2D2F31)

// Text Colors
val StudioTextPrimary = Color(0xFFE3E3E3)
val StudioTextSecondary = Color(0xFFA8AAAD)
val StudioTextTertiary = Color(0xFF75777A)

// Rainbow / Aurora Gradient Palette
val RainbowCyan = Color(0xFF00E5FF)
val RainbowBlue = Color(0xFF4285F4)
val RainbowPurple = Color(0xFF7C4DFF)
val RainbowMagenta = Color(0xFFE040FB)
val RainbowPink = Color(0xFFFF4081)
val RainbowAmber = Color(0xFFFFB300)

val RainbowColors = listOf(
    RainbowCyan,
    RainbowBlue,
    RainbowPurple,
    RainbowMagenta,
    RainbowPink,
    RainbowAmber,
    RainbowCyan
)

fun createRainbowBrush(): Brush = Brush.linearGradient(
    colors = RainbowColors
)

// Accent Colors
val GeminiSparkleBlue = Color(0xFF4D90FE)
val UserBubbleColor = Color(0xFF2A2D32)
val CodeBlockBackground = Color(0xFF181A1F)
val CodeBlockBorder = Color(0xFF2E323A)
val ErrorRed = Color(0xFFF28B82)
val SuccessGreen = Color(0xFF81C995)
