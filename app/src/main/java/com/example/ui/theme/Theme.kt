package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

private val BaseNavyColorScheme = lightColorScheme(
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
  primaryColorHex: String = "#0F294A",
  fontStyle: String = "DEFAULT",
  darkTheme: Boolean = false,
  content: @Composable () -> Unit,
) {
  val dynamicPrimary = try {
    Color(android.graphics.Color.parseColor(primaryColorHex))
  } catch (_: Exception) {
    NavyPrimary
  }

  val dynamicFontFamily = when (fontStyle.uppercase()) {
    "TYPEWRITER", "MONOSPACE" -> FontFamily.Monospace
    "SERIF" -> FontFamily.Serif
    else -> FontFamily.Default
  }

  val dynamicTypography = Typography(
    bodyLarge = Typography.bodyLarge.copy(fontFamily = dynamicFontFamily),
    bodyMedium = Typography.bodyMedium.copy(fontFamily = dynamicFontFamily),
    bodySmall = Typography.bodySmall.copy(fontFamily = dynamicFontFamily),
    titleLarge = Typography.titleLarge.copy(fontFamily = dynamicFontFamily),
    titleMedium = Typography.titleMedium.copy(fontFamily = dynamicFontFamily),
    titleSmall = Typography.titleSmall.copy(fontFamily = dynamicFontFamily),
    labelLarge = Typography.labelLarge.copy(fontFamily = dynamicFontFamily),
    labelMedium = Typography.labelMedium.copy(fontFamily = dynamicFontFamily),
    labelSmall = Typography.labelSmall.copy(fontFamily = dynamicFontFamily)
  )

  val dynamicColorScheme = BaseNavyColorScheme.copy(
    primary = dynamicPrimary,
    onPrimaryContainer = dynamicPrimary
  )

  MaterialTheme(
    colorScheme = dynamicColorScheme,
    typography = dynamicTypography,
    content = content
  )
}
