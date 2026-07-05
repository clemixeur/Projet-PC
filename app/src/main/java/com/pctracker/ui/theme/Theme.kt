package com.pctracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = SeriesBlueDark,
    background = PagePlaneDark,
    surface = SurfaceDark,
    onBackground = PrimaryInkDark,
    onSurface = PrimaryInkDark
)

private val LightColors = lightColorScheme(
    primary = SeriesBlueLight,
    background = PagePlaneLight,
    surface = SurfaceLight,
    onBackground = PrimaryInkLight,
    onSurface = PrimaryInkLight
)

@Composable
fun PCTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
