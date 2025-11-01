package com.demoapp.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenLight,
    onPrimary = Color(0xFF000000),
    primaryContainer = PrimaryGreenDark.copy(alpha = 0.3f),
    onPrimaryContainer = PrimaryGreenLight,
    
    secondary = SecondaryGreen,
    onSecondary = Color(0xFF000000),
    secondaryContainer = DarkGreen,
    onSecondaryContainer = SecondaryGreen,
    
    tertiary = InfoBlue,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF0D47A1),
    onTertiaryContainer = Color(0xFFBBDEFB),
    
    error = ErrorRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Color(0xFFFFCDD2),
    
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    
    outline = Color(0xFF9E9E9E),
    outlineVariant = Color(0xFF616161),
    
    inverseSurface = Color(0xFFF5F5F5),
    inverseOnSurface = Color(0xFF1E1E1E),
    inversePrimary = PrimaryGreen,
    
    scrim = Color(0xFF000000),
    surfaceTint = PrimaryGreenLight
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = LightGreen.copy(alpha = 0.3f),
    onPrimaryContainer = PrimaryGreenDark,
    
    secondary = SecondaryGreen,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = PaleGreen,
    onSecondaryContainer = PrimaryGreenDark,
    
    tertiary = InfoBlue,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBBDEFB),
    onTertiaryContainer = Color(0xFF0D47A1),
    
    error = ErrorRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFB71C1C),
    
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFBDBDBD),
    
    inverseSurface = Color(0xFF1E1E1E),
    inverseOnSurface = Color(0xFFF5F5F5),
    inversePrimary = PrimaryGreenLight,
    
    scrim = Color(0xFF000000),
    surfaceTint = PrimaryGreen
)

@Composable
fun DemoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
