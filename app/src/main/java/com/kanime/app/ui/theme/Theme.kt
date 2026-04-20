package com.kanime.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val KanimeColorScheme = lightColorScheme(
    primary = KanimePrimary,
    onPrimary = KanimeOnPrimary,
    secondary = KanimeSecondary,
    background = KanimeBackground,
    surface = KanimeSurface,
    surfaceVariant = KanimeSurfaceVariant,
    onSurface = KanimeOnSurface,
    onSurfaceVariant = KanimeOnSurfaceVariant
)

@Composable
fun KanimeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KanimeColorScheme,
        typography = KanimeTypography,
        content = content
    )
}
