package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberZenColorScheme = darkColorScheme(
    primary = RadiantTurquoise,
    onPrimary = SpaceVoid,
    primaryContainer = CyberSurface,
    onPrimaryContainer = RadiantTurquoise,
    secondary = QuantumJade,
    onSecondary = SpaceVoid,
    secondaryContainer = DeepAbyss,
    onSecondaryContainer = QuantumJade,
    tertiary = SingularityGold,
    onTertiary = SpaceVoid,
    background = SpaceVoid,
    onBackground = TextPrimary,
    surface = DeepAbyss,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurface,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    error = CriticalHorizon,
    onError = TextPrimary
)

@Composable
fun TimeDensityTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberZenColorScheme,
        typography = Typography,
        content = content
    )
}
