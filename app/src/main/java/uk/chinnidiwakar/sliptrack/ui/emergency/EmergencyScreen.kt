package uk.chinnidiwakar.sliptrack.ui.emergency

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmergencyScreen(onClose: () -> Unit) {
    var selectedMode by remember { mutableStateOf("Wave") }
    var step by remember { mutableStateOf(0) }
    val haptic = LocalHapticFeedback.current

    val steps = listOf(
        "Take a deep breath. This feeling is just a chemical signal. It will pass.",
        "Drink a glass of cold water. Change your physical environment immediately.",
        "Set a timer for 15 minutes. Tell yourself: 'I can decide again when the timer ends.'",
        "Recall why you started. Discipline is choosing between what you want now and what you want most.",
        "Connect with your 'Future Self'. Visualize the peace of tomorrow morning if you stay strong now."
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF000000) // Deep Tamas Black for focus
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                "Urge Emergency",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(20.dp))

            // MODE TOGGLE (Segmented Control)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                listOf("Wave", "Breathe").forEach { mode ->
                    val isSelected = selectedMode == mode
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(20.dp),
                        onClick = {
                            selectedMode = mode
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = mode,
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(0.5f))

            // VISUALIZER BOX
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedMode == "Wave") {
                    UrgeWaveVisualizer()
                } else {
                    BoxBreathingVisualizer()
                }
            }

            Spacer(Modifier.weight(0.5f))

            // EMERGENCY PROTOCOL STEPS
            Text(
                "Protocol Step ${step + 1} of ${steps.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = steps[step],
                    modifier = Modifier.padding(20.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    if (step < steps.lastIndex) step++ else step = 0
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (step < steps.lastIndex) "Next Step" else "Restart Protocol")
            }

            TextButton(onClick = onClose, modifier = Modifier.padding(top = 16.dp)) {
                Text("I am calm now", color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun UrgeWaveVisualizer() {
    val transition = rememberInfiniteTransition(label = "WaveTransition")

    // Three different offsets for three different layers
    val waveOffset1 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing))
    )
    val waveOffset2 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing))
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        fun drawWave(offset: Float, color: Color, amplitude: Float, strokeWidth: Float) {
            val path = Path()
            path.moveTo(0f, height / 2)
            for (x in 0..width.toInt() step 5) {
                val relativeX = x / width
                val sine = kotlin.math.sin((relativeX + offset) * 2 * kotlin.math.PI)
                val y = (height / 2) + (sine * amplitude).toFloat()
                path.lineTo(x.toFloat(), y)
            }
            drawPath(path, color = color, style = Stroke(width = strokeWidth))
        }

        // Layer 1: Background slow wave
        drawWave(waveOffset1, Color(0xFF1E88E5).copy(alpha = 0.3f), 60f, 2.dp.toPx())
        // Layer 2: Faster mid wave
        drawWave(waveOffset2, Color(0xFF64B5F6).copy(alpha = 0.6f), 40f, 3.dp.toPx())
        // Layer 3: Main focus wave
        drawWave(waveOffset1 * -1.2f, Color(0xFFBBDEFB), 25f, 5.dp.toPx())
    }
}

@Composable
fun BoxBreathingVisualizer() {
    val transition = rememberInfiniteTransition(label = "BreatheTransition")

    // This creates a 4-part cycle (In, Hold, Out, Hold)
    val progress by transition.animateFloat(
        initialValue = 0f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing))
    )

    val (text, scale) = when {
        progress < 1f -> "Inhale" to 0.6f + (progress * 0.4f)      // Growing
        progress < 2f -> "Hold" to 1.0f                            // Full
        progress < 3f -> "Exhale" to 1.0f - ((progress - 2f) * 0.4f) // Shrinking
        else -> "Hold" to 0.6f                                     // Empty
    }

    Box(contentAlignment = Alignment.Center) {
        // Pulse Effect
        Surface(
            modifier = Modifier.size(240.dp * scale),
            shape = CircleShape,
            color = Color(0xFF81C784).copy(alpha = 0.1f)
        ) {}

        Surface(
            modifier = Modifier.size(160.dp * scale),
            shape = CircleShape,
            color = if (text == "Hold") Color(0xFFFFB74D) else Color(0xFF81C784),
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}