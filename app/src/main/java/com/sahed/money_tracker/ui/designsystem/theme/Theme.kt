package com.sahed.money_tracker.ui.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sahed.money_tracker.data.preferences.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPalette.SoftEmerald,
    onPrimary = EmeraldPalette.LightText,
    primaryContainer = EmeraldPalette.DeepGreen,
    onPrimaryContainer = EmeraldPalette.EmeraldGlow,
    secondary = EmeraldPalette.DeepGreen,
    onSecondary = EmeraldPalette.LightText,
    secondaryContainer = EmeraldPalette.Surface2Dark,
    onSecondaryContainer = EmeraldPalette.EmeraldGlow,
    tertiary = EmeraldPalette.EmeraldGlow,
    onTertiary = EmeraldPalette.DarkBackground,
    background = EmeraldPalette.DarkBackground,
    onBackground = EmeraldPalette.LightText,
    surface = EmeraldPalette.Surface1Dark,
    onSurface = EmeraldPalette.LightText,
    surfaceVariant = EmeraldPalette.Surface2Dark,
    onSurfaceVariant = EmeraldPalette.SubTextGrey,
    outline = EmeraldPalette.Surface3Dark,
    outlineVariant = EmeraldPalette.Surface2Dark,
    error = EmeraldPalette.ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPalette.SoftEmerald,
    onPrimary = EmeraldPalette.LightText,
    primaryContainer = EmeraldPalette.Surface2Light,
    onPrimaryContainer = EmeraldPalette.DeepGreen,
    secondary = EmeraldPalette.DeepGreen,
    onSecondary = EmeraldPalette.LightText,
    secondaryContainer = EmeraldPalette.Surface3Light,
    onSecondaryContainer = EmeraldPalette.DeepGreen,
    tertiary = EmeraldPalette.EmeraldGlow,
    onTertiary = EmeraldPalette.LightText,
    background = EmeraldPalette.LightBackground,
    onBackground = EmeraldPalette.DarkText,
    surface = EmeraldPalette.Surface1Light,
    onSurface = EmeraldPalette.DarkText,
    surfaceVariant = EmeraldPalette.Surface2Light,
    onSurfaceVariant = EmeraldPalette.LightSubText,
    outline = EmeraldPalette.Surface3Light,
    outlineVariant = EmeraldPalette.Surface2Light,
    error = EmeraldPalette.ErrorRed
)

private val DarkExtendedColors = ExtendedColors(
    surfaceTier1 = EmeraldPalette.Surface1Dark,
    surfaceTier2 = EmeraldPalette.Surface2Dark,
    surfaceTier3 = EmeraldPalette.Surface3Dark,
    glassFill = EmeraldPalette.DarkGlassFill,
    glassBorder = EmeraldPalette.DarkGlassBorder,
    subText = EmeraldPalette.SubTextGrey,
    success = EmeraldPalette.SuccessGreen,
    warning = EmeraldPalette.WarningAmber,
    error = EmeraldPalette.ErrorRed,
    accent = EmeraldPalette.AccentPurple
)

private val LightExtendedColors = ExtendedColors(
    surfaceTier1 = EmeraldPalette.Surface1Light,
    surfaceTier2 = EmeraldPalette.Surface2Light,
    surfaceTier3 = EmeraldPalette.Surface3Light,
    glassFill = EmeraldPalette.LightGlassFill,
    glassBorder = EmeraldPalette.LightGlassBorder,
    subText = EmeraldPalette.LightSubText,
    success = EmeraldPalette.SuccessGreen,
    warning = EmeraldPalette.WarningAmber,
    error = EmeraldPalette.ErrorRed,
    accent = EmeraldPalette.AccentPurple
)

object EmeraldTheme {
    val extended: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}

@Composable
fun EmeraldDesignTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = AppShapes,
            typography = AppTypography,
            content = content
        )
    }
}

@Composable
fun EmeraldDesignTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    EmeraldDesignTheme(darkTheme = darkTheme, content = content)
}
