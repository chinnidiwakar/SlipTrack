package uk.chinnidiwakar.sliptrack.ui.emergency

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmergencyScreen(onClose: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    val haptic = LocalHapticFeedback.current
    val steps = listOf(
        "Step 1: Pause. Take 10 slow breaths before any action.",
        "Step 2: Change your environment now (stand up, leave room, cold water).",
        "Step 3: Run a 10-minute replacement (walk, pushups, journaling, shower).",
        "Step 4: Message one trusted person: 'I need a quick check-in.'",
        "Step 5: If urge remains, log it as a victory with trigger + intensity."
    )
    var isBreathing by remember { mutableStateOf(false) }

    // This animates a circle to guide breathing
    val breatheScale by animateFloatAsState(
        targetValue = if (isBreathing) 1.5f else 1f, // If off, stay at 1f
        animationSpec = if (isBreathing) {
            infiniteRepeatable(
                animation = tween(4000, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(500) // Smoothly shrink back to normal when turned off
        },
        label = "breathe"
    )

    LaunchedEffect(breatheScale) {
        if (isBreathing) {
            if (breatheScale >= 1.49f || breatheScale <= 1.01f) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp)) {

            Text("Immediate Calm", fontWeight = FontWeight.Bold, fontSize = 20.sp)

            // The Breathing Circle
            Box(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Glow
                Surface(
                    modifier = Modifier.size(120.dp * breatheScale),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ) {}

                // Interaction Button
                Button(
                    onClick = { isBreathing = !isBreathing },
                    shape = CircleShape,
                    modifier = Modifier.size(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBreathing) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = if (isBreathing) "Stop" else "Breathe",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text("Emergency Protocol", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))

            // The Protocol Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = steps[step],
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Next Step Button
            Button(
                onClick = {
                    if (step < steps.lastIndex) step++ else step = 0
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(if (step < steps.lastIndex) "Next Step" else "Restart Protocol")
            }

            Spacer(Modifier.weight(1f))

            TextButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("I feel better now, go back", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}