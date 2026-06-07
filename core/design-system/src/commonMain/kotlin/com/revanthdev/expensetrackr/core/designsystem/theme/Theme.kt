package com.revanthdev.expensetrackr.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = Color(0xFF1A1B22),
    surface = SurfaceLight,
    onSurface = Color(0xFF1A1B22),
    surfaceVariant = Color(0xFFE3E5F0),
    onSurfaceVariant = Color(0xFF45464F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = Color(0xFFE0E2EC),
    outline = Color(0xFFC4C6D0),
    outlineVariant = Color(0xFFE0E2EC),
    error = ErrorLight,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigoDark,
    onPrimary = Color(0xFF0C1565),
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryTealDark,
    onSecondary = Color(0xFF003731),
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = Color(0xFF5E1138),
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = Color(0xFFE3E1E9),
    surface = SurfaceDark,
    onSurface = Color(0xFFE3E1E9),
    surfaceVariant = Color(0xFF45464F),
    onSurfaceVariant = Color(0xFFC5C6D0),
    surfaceContainerLowest = Color(0xFF0C0E13),
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = Color(0xFF373942),
    outline = Color(0xFF8E909A),
    outlineVariant = Color(0xFF44464F),
    error = ErrorDark,
    onError = Color(0xFF601410),
)

@Composable
expect fun dynamicColorScheme(darkTheme: Boolean): ColorScheme?

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = dynamicColorScheme(darkTheme) ?: if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
