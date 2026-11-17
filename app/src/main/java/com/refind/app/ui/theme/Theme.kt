package com.refind.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = SurfaceLight,
    primaryContainer = BrandContainer,
    onPrimaryContainer = BrandInk,
    secondary = BrandDeep,
    onSecondary = SurfaceLight,
    secondaryContainer = BrandMist,
    onSecondaryContainer = BrandInk,
    tertiary = BrandBright,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    error = Color(0xFFC92A2A),
    errorContainer = Color(0xFFFFE3E3),
    onError = SurfaceLight,
    onErrorContainer = Color(0xFF7A1F1F),
)

private val DarkColors = darkColorScheme(
    primary = BrandLight,
    onPrimary = BrandNight,
    primaryContainer = Color(0xFF14524A),
    onPrimaryContainer = BrandContainer,
    secondary = BrandBright,
    onSecondary = BrandNight,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = BrandContainer,
    tertiary = BrandBright,
    background = BackgroundDark,
    onBackground = Color(0xFFD9EAE4),
    surface = SurfaceDark,
    onSurface = Color(0xFFD9EAE4),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFF8FB0A6),
    error = Color(0xFFFF8787),
    errorContainer = Color(0xFF4A1010),
    onError = Color(0xFF2B0505),
    onErrorContainer = Color(0xFFFFE3E3),
)

@Composable
fun ReFindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Brand palette stays consistent across devices: dynamic color is off.
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
