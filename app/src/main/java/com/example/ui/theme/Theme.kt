package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldSuccess,
    onPrimary = ObsidianBg,
    primaryContainer = EmeraldSuccessContainer,
    onPrimaryContainer = EmeraldSuccess,
    secondary = ElectricBlue,
    onSecondary = ObsidianBg,
    secondaryContainer = ElectricBlueContainer,
    onSecondaryContainer = ElectricBlue,
    tertiary = AmberAttention,
    onTertiary = ObsidianBg,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = AmberAttention,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = TableBorder,
    outlineVariant = CellBorder,
    error = CoralError,
    onError = ObsidianBg,
    errorContainer = CoralErrorContainer,
    onErrorContainer = CoralError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default as requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
