package com.example.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsDark = compositionLocalOf { true }

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

private val DarkColorScheme =
  darkColorScheme(
    primary = MonoWhite,
    onPrimary = MonoBlack,
    primaryContainer = MonoDarkGrey,
    onPrimaryContainer = MonoWhite,
    secondary = MonoLightGrey,
    onSecondary = MonoBlack,
    background = MonoBlack,
    onBackground = MonoWhite,
    surface = Color(0xFF1E1E1E),
    onSurface = MonoWhite,
    surfaceVariant = MonoDarkGrey,
    onSurfaceVariant = MonoLightGrey,
    outline = MonoMediumGrey,
    outlineVariant = Color(0xFF404040)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MonoBlack,
    onPrimary = MonoWhite,
    primaryContainer = MonoLightGrey,
    onPrimaryContainer = MonoBlack,
    secondary = MonoDarkGrey,
    onSecondary = MonoWhite,
    background = MonoWhite,
    onBackground = MonoBlack,
    surface = MonoSubtleGrey,
    onSurface = MonoBlack,
    surfaceVariant = MonoLightGrey,
    onSurfaceVariant = MonoDarkGrey,
    outline = MonoMediumGrey,
    outlineVariant = Color(0xFFCCCCCC)
  )

@Composable
fun SudokuOrganizerTheme(
  themeMode: ThemeMode = ThemeMode.DARK,
  content: @Composable () -> Unit,
) {
  val isDark = when (themeMode) {
      ThemeMode.SYSTEM -> isSystemInDarkTheme()
      ThemeMode.DARK -> true
      ThemeMode.LIGHT -> false
  }
  val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

  val view = LocalView.current
  if (!view.isInEditMode) {
      SideEffect {
          val window = (view.context as? Activity)?.window
              ?: generateSequence(view.context) { (it as? ContextWrapper)?.baseContext }
                  .filterIsInstance<Activity>()
                  .firstOrNull()?.window
          if (window != null) {
              val insetsController = WindowCompat.getInsetsController(window, view)
              // When isDark is true, background is dark, so status bar text/icons should be light (false).
              // When isDark is false, background is light, so status bar text/icons should be dark (true).
              insetsController.isAppearanceLightStatusBars = !isDark
              insetsController.isAppearanceLightNavigationBars = !isDark
          }
      }
  }

  CompositionLocalProvider(LocalIsDark provides isDark) {
      MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
