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

private val DarkColorScheme =
  darkColorScheme(
    primary = RiderPrimary,
    onPrimary = Color.White,
    primaryContainer = RiderPrimaryDark,
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = RiderSecondary,
    onSecondary = Color.White,
    secondaryContainer = RiderSecondaryDark,
    tertiary = RiderTertiary,
    background = RiderDarkBg,
    onBackground = Color(0xFFF1F5F9),
    surface = RiderDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = RiderDarkSurfaceCard,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = RiderDarkBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = RiderPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = RiderPrimaryDark,
    secondary = RiderSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    tertiary = RiderTertiary,
    background = RiderLightBg,
    onBackground = Color(0xFF0F172A),
    surface = RiderLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = RiderLightSurfaceCard,
    onSurfaceVariant = Color(0xFF475569),
    outline = RiderLightBorder
  )

@Composable
fun FoodRiderTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  FoodRiderTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

