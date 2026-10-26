package com.example.hireme.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val HireMeDarkColorScheme =
  darkColorScheme(
    primary = MagentaPrimary,
    onPrimary = PureWhite,
    primaryContainer = PlumDark,
    onPrimaryContainer = PureWhite,
    secondary = RoseAccent,
    onSecondary = PureWhite,
    background = MidnightBackground,
    onBackground = PureWhite,
    surface = MidnightCard,
    onSurface = PureWhite,
    surfaceVariant = MidnightDark,
    onSurfaceVariant = LavenderSubtext,
    outline = MidnightBorder,
  )

@Composable
fun HireMeTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = HireMeDarkColorScheme,
    typography = Typography,
    content = content,
  )
}
