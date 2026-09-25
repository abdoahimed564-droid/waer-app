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
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = PulseTeal,
    onPrimaryContainer = Color.White,
    secondary = PulseTealLight,
    onSecondary = Color.Black,
    background = DarkBg,
    onBackground = Color(0xFFE9EDEF),
    surface = DarkSurface,
    onSurface = Color(0xFFE9EDEF),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF8696A0),
    error = CallRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = EmeraldPrimary,
    onPrimaryContainer = Color.White,
    secondary = PulseTeal,
    onSecondary = Color.White,
    background = LightBg,
    onBackground = Color(0xFF111B21),
    surface = LightSurface,
    onSurface = Color(0xFF111B21),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF667781),
    error = CallRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep ChatPulse branded look consistent
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
