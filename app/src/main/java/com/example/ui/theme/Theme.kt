package com.example.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

fun getWidgetBackgroundColor(context: Context): Int {
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val appColor = prefs.getString("app_color", "dynamic") ?: "dynamic"
    val isDark = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
    val dynamicColor = appColor == "dynamic"

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            if (isDark) darkColorScheme() else lightColorScheme()
        }
    }
    return colorScheme.surfaceVariant.toArgb()
}

fun getWidgetForegroundColor(context: Context): Int {
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val appColor = prefs.getString("app_color", "dynamic") ?: "dynamic"
    val isDark = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
    val dynamicColor = appColor == "dynamic"

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            if (isDark) darkColorScheme() else lightColorScheme()
        }
    }
    return colorScheme.onSurfaceVariant.toArgb()
}

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

@Composable
fun MyApplicationTheme(
  appColor: String = "dynamic",
  content: @Composable () -> Unit,
) {
  val darkTheme = isSystemInDarkTheme()

  val dynamicColor = appColor == "dynamic"

  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      else -> {
          // Preset colors
          val primaryColorValue = when (appColor) {
              "blue" -> Color(0xFF1976D2) to Color(0xFF90CAF9)
              "green" -> Color(0xFF388E3C) to Color(0xFFA5D6A7)
              "red" -> Color(0xFFD32F2F) to Color(0xFFEF9A9A)
              "yellow" -> Color(0xFFFBC02D) to Color(0xFFFFF59D)
              "purple" -> Color(0xFF7B1FA2) to Color(0xFFCE93D8)
              else -> Color(0xFF6650a4) to Color(0xFFD0BCFF) // Default
          }
          
          if (darkTheme) {
              val c = primaryColorValue.second
              darkColorScheme(
                  primary = c,
                  onPrimary = Color.Black,
                  primaryContainer = c.copy(alpha = 0.3f),
                  onPrimaryContainer = c,
                  secondary = c.copy(alpha = 0.7f),
                  onSecondary = Color.Black,
                  secondaryContainer = c.copy(alpha = 0.2f),
                  onSecondaryContainer = c,
                  tertiary = c.copy(alpha = 0.5f),
                  surfaceTint = c
              )
          } else {
              val c = primaryColorValue.first
              lightColorScheme(
                  primary = c,
                  onPrimary = Color.White,
                  primaryContainer = c.copy(alpha = 0.2f),
                  onPrimaryContainer = c,
                  secondary = c.copy(alpha = 0.7f),
                  onSecondary = Color.White,
                  secondaryContainer = c.copy(alpha = 0.15f),
                  onSecondaryContainer = c,
                  tertiary = c.copy(alpha = 0.5f),
                  surfaceTint = c
              )
          }
      }
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography) {
    androidx.compose.runtime.CompositionLocalProvider(
      androidx.compose.foundation.LocalIndication provides androidx.compose.material3.ripple()
    ) {
      content()
    }
  }
}
