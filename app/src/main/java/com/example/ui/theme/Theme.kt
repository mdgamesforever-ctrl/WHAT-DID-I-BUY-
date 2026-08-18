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

private val DarkColorScheme = darkColorScheme(
    primary = DarkEmeraldPrimary,
    onPrimary = NavyDark,
    primaryContainer = DarkEmeraldContainer,
    onPrimaryContainer = Color(0xFF99F6E4),
    secondary = Color(0xFF93C5FD),
    onSecondary = NavyDark,
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color(0xFFDBEAFE),
    tertiary = Color(0xFFFDE047),
    onTertiary = NavyDark,
    tertiaryContainer = DarkAmberContainer,
    onTertiaryContainer = Color(0xFFFEF08A),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = Color(0xFFFB7185),
    errorContainer = DarkRoseContainer,
    onErrorContainer = Color(0xFFFFE4E6)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = SlateDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = SlateDark,
    tertiary = AmberAlert,
    onTertiary = Color.White,
    tertiaryContainer = AmberAlertContainer,
    onTertiaryContainer = OnAmberContainer,
    background = SlateBg,
    onBackground = SlateDark,
    surface = SlateSurface,
    onSurface = SlateDark,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SlateLight,
    outline = SlateBorder,
    error = RoseDanger,
    errorContainer = RoseDangerContainer,
    onErrorContainer = OnRoseContainer
)

@Composable
fun WhatDidIBuyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted trustworthy palette by default
    content: @Composable () -> Unit
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

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    WhatDidIBuyTheme(darkTheme, dynamicColor, content)
}

