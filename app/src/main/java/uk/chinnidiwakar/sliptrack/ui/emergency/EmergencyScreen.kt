package uk.chinnidiwakar.sliptrack.ui.emergency

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.chinnidiwakar.sliptrack.HapticEngine


@Composable
fun EmergencyScreen(onClose: () -> Unit) {
    var selectedMode by remember { mutableStateOf("Wave") }
    var step by remember { mutableIntStateOf(0) }
    var isBreathingRunning by remember { mutableStateOf(false) }
    var breathingPhase by remember { mutableStateOf("Ready") }
    val breathingScale = remember { androidx.compose.animation.core.Animatable(0.6f) }
    val context = LocalContext.current
    val engine = remember { HapticEngine(context) }

    LaunchedEffect(Unit) {
        engine.emergencyGround()
    }


    LaunchedEffect(selectedMode) {
        if (selectedMode != "Breathe") {
            isBreathingRunning = false
            breathingPhase = "Ready"
            breathingScale.snapTo(0.6f) // ✅ now inside coroutine
        }
    }

    LaunchedEffect(isBreathingRunning) {

        if (!isBreathingRunning) {
            engine.cancel()
            return@LaunchedEffect
        }

        while (isBreathingRunning) {

            breathingPhase = "Inhale"
            engine.waveform(
                longArrayOf(0, 20, 40, 20, 40, 20),
                intArrayOf(0, 80, 0, 140, 0, 200)
            )
            breathingScale.animateTo(1f, tween(4000))

            breathingPhase = "Hold"
            engine.pulse(30, 90)
            kotlinx.coroutines.delay(4000)

            breathingPhase = "Exhale"
            engine.waveform(
                longArrayOf(0, 40, 40, 40),
                intArrayOf(0, 160, 0, 120)
            )
            breathingScale.animateTo(0.6f, tween(6000))

            breathingPhase = "Hold"
            engine.pulse(20, 70)
            kotlinx.coroutines.delay(2000)
        }
    }


    val steps = listOf(
        "Take a deep breath. This feeling is just a chemical signal. It will pass.",
        "Drink a glass of cold water. Change your physical environment immediately.",
        "Set a timer for 15 minutes. Tell yourself: 'I can decide again when the timer ends.'",
        "Recall why you started. Discipline is choosing between what you want now and what you want most.",
        "Connect with your 'Future Self'. Visualize the peace of tomorrow morning if you stay strong now."
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF080808) // Slightly lifted black for better contrast
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 110.dp), // 👈 reserve space for floating dock
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Text(
                "Urge Emergency",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                ),
                color = Color.White
            )

            Spacer(Modifier.height(24.dp))

            // GLASSMORPHIC TOGGLE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White.withAlpha(0.05f)) // Low alpha base
                    .border(1.dp, Color.White.withAlpha(0.1f), RoundedCornerShape(26.dp))
            ) {
                listOf("Wave", "Breathe").forEach { mode ->
                    val isSelected = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (isSelected) Color.White.withAlpha(0.15f) else Color.Transparent)
                            .clickable {
                                selectedMode = mode
                                if (mode != "Breathe") isBreathingRunning = false
                                breathingPhase = "Ready"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode,
                            color = if (isSelected) Color.White else Color.White.withAlpha(0.4f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.weight(0.5f))

            // FROSTED VISUALIZER CONTAINER
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(Color.White.withAlpha(0.03f), CircleShape)
                    .border(0.5.dp, Color.White.withAlpha(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selectedMode == "Wave") {
                    UrgeWaveVisualizer()
                } else {
                    BoxBreathingVisualizer(
                        phase = breathingPhase,
                        scale = breathingScale.value
                    )
                }

            }

            if (selectedMode == "Breathe") {
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { isBreathingRunning = !isBreathingRunning },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (isBreathingRunning) "Stop" else "Start")
                }
            }


            Spacer(Modifier.weight(0.5f))

            // GLASS STEP CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.withAlpha(0.1f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White.withAlpha(0.07f)),
                shape = RoundedCornerShape(20.dp)
            ) {

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Step ${step + 1} of ${steps.size}".uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = steps[step],
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // NEON-EDGED BUTTON
            Button(
                onClick = {
                    if (step < steps.lastIndex) step++ else step = 0
                    engine.cancel()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    if (step < steps.lastIndex) "Next Step" else "Restart Protocol",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            TextButton(
                onClick = {
                    isBreathingRunning = false
                    engine.cancel()
                    onClose()
                },
                modifier = Modifier.padding(top = 12.dp)
            )
            {
                Text("I am calm now", color = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}

// Extension for cleaner alpha usage
private fun Color.withAlpha(alpha: Float): Color = this.copy(alpha = alpha)

@Composable
fun UrgeWaveVisualizer() {
    val transition = rememberInfiniteTransition(label = "GlassWave")

    val waveOffset1 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing))
    )
    val waveOffset2 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing))
    )

    Canvas(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        val width = size.width
        val height = size.height

        fun drawGlassWave(offset: Float, alpha: Float, amplitude: Float, strokeWidth: Float, reverse: Boolean = false) {
            val path = Path()
            path.moveTo(0f, height / 2)

            for (x in 0..width.toInt() step 4) {
                val relativeX = x / width
                val direction = if (reverse) -1f else 1f
                val sine = kotlin.math.sin((relativeX + (offset * direction)) * 2 * kotlin.math.PI)
                val y = (height / 2) + (sine * amplitude).toFloat()
                path.lineTo(x.toFloat(), y)
            }

            drawPath(
                path = path,
                color = Color.White.copy(alpha = alpha),
                style = Stroke(width = strokeWidth)
            )
        }

        // Layer 1: Deep subtle glow
        drawGlassWave(waveOffset1, 0.05f, 70f, 8.dp.toPx())
        // Layer 2: Mid-tone frosted line
        drawGlassWave(waveOffset2, 0.15f, 45f, 3.dp.toPx(), reverse = true)
        // Layer 3: Crisp highlight line
        drawGlassWave(waveOffset1 * 1.5f, 0.4f, 25f, 1.5.dp.toPx())
    }
}

@Composable
fun BoxBreathingVisualizer(
    phase: String,
    scale: Float
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Box(contentAlignment = Alignment.Center) {

            Surface(
                modifier = Modifier.size(240.dp * scale),
                shape = CircleShape,
                color = Color(0xFF81C784).copy(alpha = 0.1f)
            ) {}

            Surface(
                modifier = Modifier.size(160.dp * scale),
                shape = CircleShape,
                color = if (phase == "Hold")
                    Color(0xFFFFB74D)
                else
                    Color(0xFF81C784),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = phase,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
