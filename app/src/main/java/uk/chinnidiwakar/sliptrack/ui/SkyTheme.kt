package uk.chinnidiwakar.sliptrack

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import uk.chinnidiwakar.sliptrack.ui.theme.AccentButton
import kotlin.random.Random

@Composable
fun SkyTheme(
    streak: Int,
    modifier: Modifier = Modifier
) {
    val gradient = when {
        streak < 7 -> listOf(Color(0xFF1B2735), Color(0xFF090A0F))
        streak < 30 -> listOf(Color(0xFF0F2027), Color(0xFF000000))
        streak < 90 -> listOf(Color(0xFF0B1D3A), Color(0xFF000814))
        else -> listOf(Color(0xFF050A1F), Color(0xFF000000))
    }

    val AppBackground = Color(0xFF000000)
    val AppSurface = Color(0xFF161616)
    Color(0xFF1F1F1F)

    Color(0xFF66BB6A)
    Color(0xFFFFB74D)
    Color(0xFFE57373)

    darkColorScheme(
        background = AppBackground,
        surface = AppSurface,
        surfaceTint = Color.Unspecified, // ← THIS LINE
        primary = AccentButton,
        onSurface = Color.White,
    )


    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Brush.verticalGradient(gradient))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawAtmosphere()
            drawStarField(streak)
            drawMoon(streak)
        }
    }
}

/* ---------------- Atmosphere depth ---------------- */

private fun DrawScope.drawAtmosphere() {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
            center = Offset(size.width / 2, size.height / 2),
            radius = size.maxDimension * 0.75f
        )
    )
}

/* ---------------- Star field ---------------- */

private fun DrawScope.drawStarField(streak: Int) {
    val rand = Random(2024)

    val area = size.width * size.height
    val baseDensity = area / 15000f   // auto scales with tablet size

    val multiplier = when {
        streak < 7 -> 0.6f
        streak < 30 -> 1f
        streak < 90 -> 1.4f
        else -> 1.8f
    }

    val starCount = (baseDensity * multiplier).toInt()

    repeat(starCount) {
        val x = rand.nextFloat() * size.width
        val y = rand.nextFloat() * size.height

        // keep moon area cleaner
        if (x > size.width * 0.65f && y < size.height * 0.35f) return@repeat

        val depth = rand.nextFloat()

        val radius = when {
            depth > 0.97f -> rand.nextFloat() * 2.5f + 1.8f   // big stars
            depth > 0.85f -> rand.nextFloat() * 1.5f + 1f
            else -> rand.nextFloat() * 0.8f + 0.4f
        }

        val alpha = when {
            depth > 0.97f -> 0.9f
            depth > 0.85f -> 0.7f
            else -> 0.4f
        }

        // glow
        drawCircle(
            color = Color.White.copy(alpha = alpha * 0.25f),
            radius = radius * 3f,
            center = Offset(x, y)
        )

        // core
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = radius,
            center = Offset(x, y)
        )
    }
}

/* ---------------- Moon ---------------- */

private fun DrawScope.drawMoon(streak: Int) {
    if (streak < 30) return

    val phase = when {
        streak < 60 -> 0.35f
        streak < 90 -> 0.65f
        else -> 1f
    }

    val center = Offset(size.width * 0.8f, size.height * 0.22f)
    val radius = size.minDimension * 0.06f  // scales for tablet nicely

    // moon glow
    drawCircle(
        color = Color(0xFFFFF2C2).copy(alpha = 0.15f),
        radius = radius * 2.4f,
        center = center
    )

    // moon body
    drawCircle(Color(0xFFFFF2C2), radius, center)

    // phase shadow
    if (phase < 1f) {
        drawCircle(
            color = Color.Black,
            radius = radius,
            center = Offset(center.x + radius * (1 - phase), center.y)
        )
    }
}
