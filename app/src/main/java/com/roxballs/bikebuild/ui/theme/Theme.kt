package com.roxballs.bikebuild.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = WorkshopGreen,
    onPrimary = WorkshopPaper,
    primaryContainer = WorkshopGreenSoft,
    onPrimaryContainer = WorkshopInk,
    secondary = WorkshopOrange,
    onSecondary = WorkshopPaper,
    secondaryContainer = WorkshopOrangeSoft,
    onSecondaryContainer = WorkshopInk,
    tertiary = WorkshopSteel,
    onTertiary = WorkshopPaper,
    tertiaryContainer = WorkshopSand,
    onTertiaryContainer = WorkshopInk,
    background = WorkshopIvory,
    onBackground = WorkshopInk,
    surface = WorkshopPaper,
    onSurface = WorkshopInk,
    surfaceVariant = WorkshopSand.copy(alpha = 0.82f),
    onSurfaceVariant = WorkshopSteel,
)

private val DarkColors = darkColorScheme(
    primary = WorkshopGreen,
    secondary = WorkshopOrange,
    tertiary = WorkshopSteel,
)

@Composable
fun BikeBuilderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content,
    )
}
