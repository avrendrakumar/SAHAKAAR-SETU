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
    primary = ElegantLavender,
    onPrimary = ElegantOnLavender,
    primaryContainer = ElegantLavenderContainer,
    onPrimaryContainer = ElegantOnLavenderContainer,
    secondary = SahakaarSaffron,
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF693C00),
    onSecondaryContainer = Color(0xFFFFDBC9),
    tertiary = ElegantMintGreen,
    onTertiary = ElegantMintGreenDark,
    tertiaryContainer = ElegantMintGreenContainer,
    onTertiaryContainer = Color(0xFFD2E8D4),
    background = ElegantDarkBg,
    onBackground = ElegantTextPrimary,
    surface = ElegantDarkSurface,
    onSurface = ElegantTextPrimary,
    surfaceVariant = ElegantDarkSurfaceVariant,
    onSurfaceVariant = ElegantTextSecondary,
    outline = ElegantDarkBorder,
    outlineVariant = Color(0xFF383A40),
    error = SahakaarEmergencyRed,
    onError = Color(0xFF690005),
    errorContainer = ElegantEmergencyBg,
    onErrorContainer = ElegantOnEmergency
)

private val LightColorScheme = DarkColorScheme // Elegant Dark is active application-wide

@Composable
fun SahakaarTheme(
    darkTheme: Boolean = true, // Default to Elegant Dark as requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
