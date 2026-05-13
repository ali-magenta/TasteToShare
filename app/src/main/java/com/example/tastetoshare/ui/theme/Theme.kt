package com.example.tastetoshare.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.tastetoshare.ui.theme.DarkGray

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = TextBlack,
    primaryContainer = GreenAccent,
    onPrimaryContainer = TextBlack,
    secondary = GreenLight,
    onSecondary = TextBlack,
    tertiary = DarkGray,
    onTertiary = TextBlack,
    tertiaryContainer  = DarkBlue,
    onTertiaryContainer  = TextWhite,
    background = DarkBackground,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    error = ErrorRed,
    onError = TextWhite,
    surfaceVariant = DarkGray,
    surfaceDim = DarkGray
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = TextWhite,
    primaryContainer = GreenAccent,
    onPrimaryContainer = TextBlack,
    secondary = GreenLight,
    onSecondary = TextBlack,
    tertiary = GreenLight,
    onTertiary = TextBlack,
    tertiaryContainer  = DarkBlue,
    onTertiaryContainer  = TextWhite,
    background = BackgroundApp,
    onBackground = TextBlack,
    surface = BackgroundApp,
    onSurface = TextBlack,
    error = ErrorRed,
    onError = TextWhite,
    surfaceVariant = GreenLight,
    surfaceDim = DarkGray
)


@Composable
fun TasteToShareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}