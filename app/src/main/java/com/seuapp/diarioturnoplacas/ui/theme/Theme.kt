package com.seuapp.diarioturnoplacas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = SurfaceLight,
    secondary = BrandBlueDark,
    tertiary = BrandCyan,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceSoft,
    onSurface = TextStrong,
    onSurfaceVariant = TextMuted,
    outline = BorderSoft,
    error = Danger
)

private val DarkColors = darkColorScheme(
    primary = BrandCyan,
    onPrimary = TextStrong,
    secondary = BrandBlue,
    tertiary = BrandBlueDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceDarkSoft,
    onSurface = SurfaceLight,
    onSurfaceVariant = TextMuted,
    error = Danger
)

@Composable
fun DiarioTurnoPlacasTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
