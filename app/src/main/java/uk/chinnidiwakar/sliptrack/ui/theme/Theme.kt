package uk.chinnidiwakar.sliptrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Accent
private val Accent = Color(0xFFFFB703)
val AccentButton = Accent

// Light theme colors
private val LightBackground = Color(0xFFF6F7FB)
private val LightSurface = Color(0xFFFFFFFF)
private val LightText = Color(0xFF1F2937)

// Dark theme colors (AMOLED)
private val AmoledBlack = Color(0xFF000000)
private val DarkSurface = Color(0xFF0B0B0B)
private val DarkText = Color(0xFFE5E7EB)

private val LightColors = lightColorScheme(
    background = LightBackground,
    surface = LightSurface,
    primary = Accent,
    onBackground = LightText,
    onSurface = LightText
)

private val DarkColors = darkColorScheme(
    background = AmoledBlack,
    surface = DarkSurface,
    primary = Accent,
    onBackground = DarkText,
    onSurface = DarkText
)

@Composable
fun RelapseTrackerTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
