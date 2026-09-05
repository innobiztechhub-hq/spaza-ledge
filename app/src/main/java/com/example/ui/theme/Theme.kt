package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = ImmersivePrimary,
    onPrimary = Color.White,
    primaryContainer = ImmersivePrimaryContainer,
    onPrimaryContainer = ImmersiveOnPrimaryContainer,
    secondary = ImmersiveSecondary,
    onSecondary = Color.White,
    secondaryContainer = ImmersiveSecondaryContainer,
    onSecondaryContainer = ImmersiveOnSecondaryContainer,
    tertiary = ImmersiveTertiary,
    onTertiary = Color.White,
    background = ImmersiveBackground,
    onBackground = ImmersiveTextPrimary,
    surface = ImmersiveSurface,
    onSurface = ImmersiveTextPrimary,
    surfaceVariant = ImmersiveSurfaceElevated,
    onSurfaceVariant = ImmersiveTextSecondary,
    outline = ImmersiveBorder,
    outlineVariant = ImmersiveBorderMedium,
    error = AccentRose,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = CloudySky,
    onPrimary = OceanBlueDeep,
    primaryContainer = OceanBlueDark,
    onPrimaryContainer = CloudySkySoft,
    secondary = OceanBlueLight,
    onSecondary = Color.White,
    secondaryContainer = OceanBlueDeep,
    onSecondaryContainer = CloudySky,
    tertiary = CloudySkyBright,
    onTertiary = OceanBlueDeep,
    background = Color(0xFF0A1C28),
    onBackground = Color(0xFFF4F8FA),
    surface = Color(0xFF0F2637),
    onSurface = Color(0xFFF4F8FA),
    surfaceVariant = Color(0xFF16374D),
    onSurfaceVariant = CloudySky,
    outline = Color(0xFF285472),
    outlineVariant = Color(0xFF38688A),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF7F1D1D)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default is Light Theme as requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
