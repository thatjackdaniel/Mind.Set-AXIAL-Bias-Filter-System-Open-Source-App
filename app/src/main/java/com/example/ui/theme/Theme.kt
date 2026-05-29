package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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
