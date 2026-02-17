package uk.chinnidiwakar.sliptrack.ui.emergency

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.chinnidiwakar.sliptrack.HapticEngine
import uk.chinnidiwakar.sliptrack.ui.settings.SettingsSection


@Composable
fun EmergencyScreen(onClose: () -> Unit) {
    var selectedMode by remember { mutableStateOf("Wave") }
    var step by remember { mutableIntStateOf(0) }
    var isBreathingRunning by remember { mutableStateOf(false) }
    var breathingPhase by remember { mutableStateOf("Ready") }
    val breathingScale = remember { androidx.compose.animation.core.Animatable(0.6f) }
    val context = LocalContext.current
    val engine = remember { HapticEngine(context) }
    val configuration = LocalConfiguration.current
    val isWideLayout = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || configuration.screenWidthDp >= 840

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

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF080808)) { Column(modifier = Modifier.fillMaxSize()) {
        EmergencyHeader(
            selectedMode = selectedMode,
            onModeChange = {
                selectedMode = it
                if (it != "Breathe") isBreathingRunning = false },
            onBack = {
                isBreathingRunning = false
                engine.cancel()
                onClose()
            }
        )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))

                if (isWideLayout) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(320.dp)
                                    .background(Color.White.copy(alpha = 0.03f), CircleShape)
                                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedMode == "Wave") UrgeWaveVisualizer()
                                else BoxBreathingVisualizer(phase = breathingPhase, scale = breathingScale.value)
                            }

                            Spacer(Modifier.height(24.dp))

                            BreathingActionButton(
                                isRunning = isBreathingRunning,
                                onClick = { isBreathingRunning = !isBreathingRunning }
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            SettingsSection(title = "Step ${step + 1}") {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = steps[step],
                                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                                        color = Color.White.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(24.dp))
                                    Button(
                                        onClick = { if (step < steps.lastIndex) step++ else step = 0 },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("NEXT STEP", fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .background(Color.White.copy(alpha = 0.03f), CircleShape)
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedMode == "Wave") UrgeWaveVisualizer()
                        else BoxBreathingVisualizer(phase = breathingPhase, scale = breathingScale.value)
                    }

                    Spacer(Modifier.height(56.dp))

                    BreathingActionButton(
                        isRunning = isBreathingRunning,
                        onClick = { isBreathingRunning = !isBreathingRunning }
                    )

                    Spacer(Modifier.height(32.dp))

                    SettingsSection(title = "Step ${step + 1}") {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = steps[step],
                                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { if (step < steps.lastIndex) step++ else step = 0 },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("NEXT STEP", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyModeSelector(selectedMode: String, onModeChange: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            listOf("Wave", "Breathe").forEach { mode ->
                val isSelected = selectedMode == mode
                Surface(
                    onClick = { onModeChange(mode) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier.width(120.dp)
                ) {
                    Text(
                        mode,
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BreathingActionButton(isRunning: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (isRunning) Color(0xFFE53935).copy(alpha = 0.2f) else Color(0xFF00897B).copy(alpha = 0.2f),
        border = BorderStroke(1.dp, if (isRunning) Color(0xFFE53935) else Color(0xFF00897B)),
        modifier = Modifier.fillMaxWidth().height(64.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                if (isRunning) "STOP SESSION" else "BEGIN BREATHING",
                color = if (isRunning) Color(0xFFFF8A80) else Color(0xFF80CBC4),
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun EmergencyHeader(
    selectedMode: String,
    onModeChange: (String) -> Unit,
    onBack: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Modern Back Button using ripple()
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape) // Ensures the ripple is circular
                    .clickable(
                        onClick = onBack,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = androidx.compose.material3.ripple(bounded = false) // NEW API
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(4.dp))

            // The Mode Pill
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Wave", "Breathe").forEach { mode ->
                        val isSelected = selectedMode == mode

                        Surface(
                            onClick = { onModeChange(mode) },
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = mode,
                                    color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Balancing spacer to keep the pill centered
            Spacer(Modifier.width(48.dp))
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color.White.copy(alpha = 0.05f)
        )
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
