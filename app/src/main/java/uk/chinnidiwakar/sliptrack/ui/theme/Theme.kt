package uk.chinnidiwakar.sliptrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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

private val AmoledColorScheme = darkColorScheme(
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color(0xFF111111),
    primary = Color(0xFF4CAF50),
    secondary = Color(0xFF80CBC4),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val DarkColors = darkColorScheme(
    background = Color(0xFF141A24),
    surface = Color(0xFF1C2430),
    surfaceVariant = Color(0xFF243041),
    primary = Accent,
    secondary = Color(0xFF9FA8DA),
    onBackground = DarkText,
    onSurface = DarkText
)

@Composable
fun RelapseTrackerTheme(themeMode: String = "material", content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        "sky" -> AmoledColorScheme
        else -> if (darkTheme) DarkColors else LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
