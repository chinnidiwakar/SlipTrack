package uk.chinnidiwakar.sliptrack.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import uk.chinnidiwakar.sliptrack.HomeViewModel
import uk.chinnidiwakar.sliptrack.HomeViewModelFactory
import uk.chinnidiwakar.sliptrack.SkyTheme
import uk.chinnidiwakar.sliptrack.ui.theme.AccentButton

// ---------------- HOME UI ----------------

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = HomeViewModelFactory(context)
    )
    val elapsedText by viewModel.elapsedText.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val longestStreak by viewModel.longestStreak.collectAsState()
    val quote by viewModel.dailyQuote.collectAsState()

    var showVictoryDialog by remember { mutableStateOf(false) }
    var showSlipDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiMessages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiMessages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    if (showVictoryDialog) {
        TriggerDialog(
            title = "How strong was the urge?",
            actionLabel = "Victory",
            onDismiss = { showVictoryDialog = false },
            onConfirm = { level, trigger ->
                viewModel.logEvent(isResist = true, intensity = level, trigger = trigger)
                showVictoryDialog = false
            }
        )
    }

    if (showSlipDialog) {
        TriggerDialog(
            title = "What triggered the slip?",
            actionLabel = "Slip",
            onDismiss = { showSlipDialog = false },
            onConfirm = { _, trigger ->
                viewModel.logSlip(trigger = trigger)
                showSlipDialog = false
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER BOX (Self-Adjusting) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight() // 👈 FIX: Adjusts to content height
            ) {
                // Background fills the space the content creates
                SkyTheme(
                    streak = currentStreak,
                    modifier = Modifier.matchParentSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 24.dp) // Added bottom padding
                        .zIndex(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Good day 🌿", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Color.White)

                    Text(
                        text = "\"$quote\"",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontStyle = FontStyle.Italic
                    )

                    // Controlled size for the ring
                    Box(modifier = Modifier.padding(vertical = 16.dp)) {
                        StreakRing(progress = (currentStreak.coerceAtMost(30)) / 30f) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = elapsedText, fontSize = 38.sp, fontWeight = FontWeight.Bold, color = Color.White) // Slightly smaller font
                                Text(text = "since last slip", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StreakItem(value = currentStreak, label = "Current")
                        StreakItem(value = longestStreak, label = "Best")
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "You're trying — that matters 🤍",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Spacer(Modifier.height(32.dp))

                // THE NEW DUAL ACTION SECTION
                Button(
                    onClick = { showVictoryDialog = true },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("I resisted an urge 🛡️", fontSize = 17.sp)
                }

                Spacer(Modifier.height(12.dp))

                RelapseButton { showSlipDialog = true }

                Spacer(Modifier.weight(1f))
            }
        }
    }
}
@Composable
fun StreakItem(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold, // Make it bolder
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge.copy(
                shadow = Shadow(
                    color = Color.Black, // Solid black shadow
                    offset = Offset(2f, 2f),
                    blurRadius = 8f
                )
            )
        )

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    blurRadius = 6f
                )
            )
        )
    }
}


@Composable
fun StreakRing(
    progress: Float,
    content: @Composable () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = ""
    )

    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = animatedProgress,
            strokeWidth = 6.dp,
            color = AccentButton,
            trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
            modifier = Modifier.size(180.dp)
        )

        content()
    }
}


// ---------------- BUTTON ----------------

@Composable
fun RelapseButton(onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }

    val haptics = LocalHapticFeedback.current

    Button(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentButton
        ),
        elevation = ButtonDefaults.buttonElevation(2.dp)
    ) {
        Text("I slipped today", fontSize = 17.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun UrgeTrackerSection(onResist: (Int) -> Unit, onSlip: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // THE VICTORY BUTTON (Larger, more positive)
        Button(
            onClick = { onResist(2) }, // Default to "Heavy Urge" for now
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Green for Victory
        ) {
            Text("I fought an urge 🛡️", fontSize = 18.sp)
        }

        Spacer(Modifier.height(16.dp))

        // THE SLIP BUTTON (Secondary)
        TextButton(onClick = onSlip) {
            Text(
                "I slipped today",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun TriggerDialog(
    title: String,
    actionLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (Int, String?) -> Unit
) {
    var selectedIntensity by remember { mutableStateOf(2) }
    var selectedTrigger by remember { mutableStateOf<String?>(null) }
    val triggerOptions = listOf("Stress", "Boredom", "Loneliness", "Social media", "Fatigue", "Other")

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Intensity")
                TextButton(onClick = { selectedIntensity = 1 }) { Text(if (selectedIntensity == 1) "✅ 🌱 Early Spark" else "🌱 Early Spark") }
                TextButton(onClick = { selectedIntensity = 2 }) { Text(if (selectedIntensity == 2) "✅ ⚔️ Heavy Urge" else "⚔️ Heavy Urge") }
                TextButton(onClick = { selectedIntensity = 3 }) { Text(if (selectedIntensity == 3) "✅ 🏆 Near Miss" else "🏆 Near Miss") }

                Spacer(Modifier.height(8.dp))
                Text("Trigger")
                triggerOptions.forEach { option ->
                    TextButton(onClick = { selectedTrigger = option }) {
                        Text(if (selectedTrigger == option) "✅ $option" else option)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedIntensity, selectedTrigger) }) {
                Text("Log $actionLabel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
