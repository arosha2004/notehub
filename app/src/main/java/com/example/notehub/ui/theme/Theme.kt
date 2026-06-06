package com.example.notehub.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Indigo Theme - Dark Color Scheme
private val IndigoDarkColorScheme = darkColorScheme(
    primary = DarkPrimaryBlue,
    onPrimary = TextOnPrimary,
    primaryContainer = DarkPrimaryBlueDark,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkSurface,
    onSecondary = DarkTextPrimary,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = InfoBlue,
    onTertiary = TextOnPrimary,
    error = ErrorRed,
    onError = TextOnPrimary,
    errorContainer = ErrorRed,
    onErrorContainer = TextOnPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorderMedium,
    outlineVariant = DarkBorderLight,
    scrim = ShadowMedium
)

// Indigo Theme - Light Color Scheme
private val IndigoLightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = TextPrimary,
    secondary = DarkNavy,
    onSecondary = TextOnDark,
    secondaryContainer = DarkNavyLight,
    onSecondaryContainer = TextOnDark,
    tertiary = InfoBlue,
    onTertiary = TextOnPrimary,
    error = ErrorRed,
    onError = TextOnPrimary,
    errorContainer = ErrorRed,
    onErrorContainer = TextOnPrimary,
    background = LightBlueGrey,
    onBackground = TextPrimary,
    surface = BackgroundWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceWhite,
    onSurfaceVariant = TextSecondary,
    outline = BorderMedium,
    outlineVariant = BorderLight,
    scrim = ShadowMedium
)

// Ocean Theme - Dark Color Scheme
private val OceanDarkColorScheme = darkColorScheme(
    primary = OceanDarkPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = OceanDarkPrimaryDark,
    onPrimaryContainer = DarkTextPrimary,
    secondary = OceanDarkSurface,
    onSecondary = DarkTextPrimary,
    secondaryContainer = OceanDarkSurfaceVariant,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = InfoBlue,
    onTertiary = TextOnPrimary,
    error = ErrorRed,
    onError = TextOnPrimary,
    errorContainer = ErrorRed,
    onErrorContainer = TextOnPrimary,
    background = OceanDarkBackground,
    onBackground = DarkTextPrimary,
    surface = OceanDarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = OceanDarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorderMedium,
    outlineVariant = DarkBorderLight,
    scrim = ShadowMedium
)

// Ocean Theme - Light Color Scheme
private val OceanLightColorScheme = lightColorScheme(
    primary = OceanPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = OceanPrimaryLight,
    onPrimaryContainer = TextPrimary,
    secondary = DarkNavy,
    onSecondary = TextOnDark,
    secondaryContainer = DarkNavyLight,
    onSecondaryContainer = TextOnDark,
    tertiary = InfoBlue,
    onTertiary = TextOnPrimary,
    error = ErrorRed,
    onError = TextOnPrimary,
    errorContainer = ErrorRed,
    onErrorContainer = TextOnPrimary,
    background = OceanLightBackground,
    onBackground = TextPrimary,
    surface = OceanCardBackground,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceWhite,
    onSurfaceVariant = TextSecondary,
    outline = BorderMedium,
    outlineVariant = BorderLight,
    scrim = ShadowMedium
)

@Composable
fun NoteHubTheme(
    darkTheme: Boolean = if (ThemeManager.hasUserSetDarkModeManualPref) {
        ThemeManager.isDarkMode
    } else {
        isSystemInDarkTheme()
    },
    currentTheme: AppTheme = ThemeManager.currentTheme,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        currentTheme == AppTheme.OCEAN -> {
            if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
        }
        else -> {
            if (darkTheme) IndigoDarkColorScheme else IndigoLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}