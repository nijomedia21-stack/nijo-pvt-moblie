package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NavyWhiteColorScheme = lightColorScheme(
  primary = NavyPrimary,
  onPrimary = Color.White,
  primaryContainer = NavyLight,
  onPrimaryContainer = NavyPrimary,
  secondary = NavySecondary,
  onSecondary = Color.White,
  secondaryContainer = NavyLight,
  onSecondaryContainer = NavySecondary,
  tertiary = StudioTeal,
  onTertiary = Color.White,
  background = CleanBackground,
  onBackground = TextPrimary,
  surface = CleanSurface,
  onSurface = TextPrimary,
  surfaceVariant = CleanSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = CleanOutline
)

@Composable
fun OmniSyncTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = NavyWhiteColorScheme,
    typography = Typography,
    content = content
  )
}
