package com.refind.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Teal700,
    onPrimary = SurfaceLight,
    primaryContainer = Teal100,
    onPrimaryContainer = Teal900,
    secondary = Teal600,
    onSecondary = SurfaceLight,
    secondaryContainer = Teal50,
    onSecondaryContainer = Teal900,
    tertiary = Teal400,
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
    primary = Teal400,
    onPrimary = Teal900Dark,
    primaryContainer = Color(0xFF0F4144),
    onPrimaryContainer = Color(0xFFB8E4E6),
    secondary = Teal500,
    onSecondary = Teal900Dark,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = Color(0xFFB8E4E6),
    tertiary = Teal500,
    background = BackgroundDark,
    onBackground = Color(0xFFDDEAEA),
    surface = SurfaceDark,
    onSurface = Color(0xFFDDEAEA),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFF93A7A8),
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
    // Brand teal stays consistent across devices: dynamic color is off.
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
