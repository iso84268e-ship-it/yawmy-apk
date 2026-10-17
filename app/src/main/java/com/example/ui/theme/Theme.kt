package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val YawmiColorScheme = lightColorScheme(
    primary = InkPrimary,
    onPrimary = White,
    secondary = ActivityStudy,
    onSecondary = InkPrimary,
    tertiary = ActivityLesson,
    onTertiary = InkPrimary,
    background = BgGradientMid,
    onBackground = InkPrimary,
    surface = GlassWhite34,
    onSurface = InkPrimary,
    surfaceVariant = GlassWhite34,
    onSurfaceVariant = InkSecondary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = YawmiColorScheme,
        typography = YawmiTypography,
        content = content
    )
}
