package com.example.damn_practica6.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Esquema de colores para el tema oscuro (Guinda y Negro)
private val DarkColorScheme = darkColorScheme(
    primary = WineRed,
    onPrimary = White,
    primaryContainer = DarkerWineRed,
    onPrimaryContainer = White,
    secondary = DarkGraySurface,
    onSecondary = White,
    secondaryContainer = Black,
    onSecondaryContainer = White,
    tertiary = LightGrayText, // Usamos LightGrayText como terciario en oscuro
    onTertiary = Black,
    background = Black,
    onBackground = White,
    surface = DarkGraySurface,
    onSurface = White,
    error = DarkErrorRed,
    onError = Black
)

// Esquema de colores para el tema claro (Azul Marino y Blanco)
private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,
    onPrimary = White,
    primaryContainer = DarkerNavyBlue,
    onPrimaryContainer = White,
    secondary = LightBlue,
    onSecondary = White,
    secondaryContainer = White,
    onSecondaryContainer = NavyBlue,
    tertiary = NavyBlue, // Usamos NavyBlue como terciario en claro
    onTertiary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    error = ErrorRed,
    onError = White
)

@Composable
fun DAMNPractica6Theme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Detecta si el sistema está en modo oscuro
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Puedes habilitar esto si quieres Material You en Android 12+
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb() // Color de la barra de estado
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme // Iconos de la barra de estado
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Se define en Type.kt
        content = content
    )
}