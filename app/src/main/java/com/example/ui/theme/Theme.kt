package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

private val AxialDarkColorScheme = darkColorScheme(
    primary = AxialPrimary,
    onPrimary = AxialBg,
    secondary = AxialSecondary,
    onSecondary = AxialTextPrimary,
    tertiary = AxialAccent,
    background = AxialBg,
    onBackground = AxialTextPrimary,
    surface = AxialSurface,
    onSurface = AxialTextPrimary,
    surfaceVariant = AxialSurfaceElevated,
    onSurfaceVariant = AxialTextSecondary,
    outline = AxialBorder,
    error = AxialRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark Theme for Axial low-emission environment
    dynamicColor: Boolean = false, // Force consistent branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AxialDarkColorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Custom glassmorphic modifier that creates a translucent glass plate
 * styled with a premium light-diffracting thin double-gradient border.
 */
fun Modifier.frostedGlass(
    shape: Shape = RoundedCornerShape(16.dp),
    borderAlpha: Float = 0.15f,
    primaryHighlightAlpha: Float = 0.12f,
    surfaceAlpha: Float = 0.50f
): Modifier = this.composed {
    this.then(
        Modifier
            .clip(shape)
            .background(Color(0xFF0F0F12).copy(alpha = surfaceAlpha)) // Warm translucent dark carbon glass
            .border(
                BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = borderAlpha),
                            Color.White.copy(alpha = 0.02f),
                            Color(0xFF00EDFF).copy(alpha = primaryHighlightAlpha),
                            Color.White.copy(alpha = borderAlpha * 0.4f)
                        )
                    )
                ),
                shape = shape
            )
    )
}

