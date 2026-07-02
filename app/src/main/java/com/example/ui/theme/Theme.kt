package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EduPilotColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    secondary = BlueSecondary,
    onSecondary = Color.White,
    tertiary = BlueDark,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    error = ColorAbsent
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Forced light theme only as per requirements
    dynamicColor: Boolean = false, // Force consistent branding
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = EduPilotColorScheme,
        typography = Typography,
        content = content
    )
}
