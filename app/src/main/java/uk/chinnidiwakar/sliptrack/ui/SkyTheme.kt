package uk.chinnidiwakar.sliptrack

import android.app.Activity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import kotlin.random.Random

@Composable
fun SkyBackground(streak: Int) {
    // 1. Improved Gradient for AMOLED (no sharp lines)
    val gradient = when {
        streak < 7 -> listOf(Color(0xFF1B2735), Color(0xFF0D1117), Color(0xFF000000))
        streak < 30 -> listOf(Color(0xFF0F2027), Color(0xFF000000), Color(0xFF000000))
        else -> listOf(Color(0xFF050A1F), Color(0xFF000000), Color(0xFF000000))
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // This sets the status bar color to Black
            window.statusBarColor = Color.Black.toArgb()
            // This ensures the icons (clock, battery) are WHITE
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    // Inside SkyBackground
    val starCount = remember(streak) { (150 + (streak * 2).coerceAtMost(300)) }
    val starPositions = remember(starCount) {
        val rand = Random(2024)
        List(starCount) {
            Offset(rand.nextFloat(), rand.nextFloat()) to (rand.nextFloat() > 0.97f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(gradient))) {
        // 1. Draw atmosphere first
        drawAtmosphere()

        // 2. Draw stars using the REMEMBERED positions
        starPositions.forEachIndexed { index, (pos, isBig) ->
            val x = pos.x * size.width
            val y = pos.y * size.height

            val twinkle = if (index % 3 == 0) alphaAnim else 1f

            drawCircle(
                color = Color.White.copy(alpha = (if(isBig) 0.7f else 0.3f) * twinkle),
                radius = if(isBig) 1.8f else 0.6f,
                center = Offset(x, y)
            )
        }

        // 3. Draw the moon
        drawMoonWithCraters(streak)
    }
}

private fun DrawScope.drawAtmosphere() {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
            center = Offset(size.width / 2, size.height / 2),
            radius = size.maxDimension * 0.8f
        )
    )
}

private fun DrawScope.drawMoonWithCraters(streak: Int) {
    if (streak < 30) return

    val phase = when {
        streak < 60 -> 0.4f
        streak < 90 -> 0.7f
        else -> 1f
    }

    val center = Offset(size.width * 0.8f, size.height * 0.22f)
    val radius = size.minDimension * 0.05f

    // Moon Glow (Soft light around it)
    drawCircle(
        color = Color(0xFFFFF2C2).copy(alpha = 0.1f),
        radius = radius * 2.5f,
        center = center
    )

    // Moon Body
    drawCircle(Color(0xFFFFF2C2), radius, center)

    // Crater Logic: Adding 3-4 small subtle spots to give depth
    val craterColor = Color(0xFFD4C594).copy(alpha = 0.4f)
    drawCircle(craterColor, radius * 0.2f, Offset(center.x - radius * 0.3f, center.y - radius * 0.2f))
    drawCircle(craterColor, radius * 0.15f, Offset(center.x + radius * 0.4f, center.y + radius * 0.1f))
    drawCircle(craterColor, radius * 0.25f, Offset(center.x - radius * 0.1f, center.y + radius * 0.4f))

    // Phase Shadow (The dark part of the moon)
    if (phase < 1f) {
        drawCircle(
            color = Color.Black.copy(alpha = 0.85f), // Semi-transparent shadow looks better
            radius = radius,
            center = Offset(center.x + radius * (1.1f - phase), center.y)
        )
    }
}