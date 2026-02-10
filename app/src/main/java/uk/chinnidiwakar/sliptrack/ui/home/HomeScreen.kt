package uk.chinnidiwakar.sliptrack.ui.home

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import uk.chinnidiwakar.sliptrack.HomeViewModel
import uk.chinnidiwakar.sliptrack.HomeViewModelFactory
import uk.chinnidiwakar.sliptrack.SkyBackground
import uk.chinnidiwakar.sliptrack.navigation.Screen
import uk.chinnidiwakar.sliptrack.ui.theme.AccentButton

@Composable
fun HomeScreen(navController: NavController) {
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

    if (showVictoryDialog) {
        TriggerDialog(
            title = "How strong was the urge?",
            actionLabel = "Victory",
            onDismiss = { showVictoryDialog = false },
            onConfirm = { level, selectedTrigger ->
                viewModel.logEvent(isResist = true, intensity = level, triggerLabel = selectedTrigger)
                showVictoryDialog = false
            }
        )
    }

    if (showSlipDialog) {
        TriggerDialog(
            title = "What triggered the slip?",
            actionLabel = "Slip",
            onDismiss = { showSlipDialog = false },
            onConfirm = { _, selectedTrigger ->
                viewModel.logSlip(triggerLabel = selectedTrigger)
                showSlipDialog = false
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Background layer
            SkyBackground(streak = currentStreak)

            IconButton(
                onClick = { navController.navigate(Screen.Settings.route) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp) // padding top 48 to avoid the clock
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }

            // 2. UI layer
            Column(modifier = Modifier.fillMaxSize()) {

                // TOP SECTION: Stats & Sky
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(top = 40.dp, bottom = 24.dp),
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

                    Box(modifier = Modifier.padding(vertical = 16.dp)) {
                        StreakRing(progress = (currentStreak.coerceAtMost(30)) / 30f) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = elapsedText, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = "since last slip", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StreakItem(value = currentStreak, label = "Current")
                        StreakItem(value = longestStreak, label = "Best")
                    }
                }

                // BOTTOM SECTION: Action Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "You're trying — that matters 🤍",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = { showVictoryDialog = true },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("I resisted an urge 🛡️", fontSize = 17.sp, color = Color.White)
                    }

                    Spacer(Modifier.height(12.dp))

                    RelapseButton { showSlipDialog = true }
                }

                LargeFloatingActionButton(
                    onClick = { navController.navigate(Screen.Emergency.route) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("SOS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- Helper Components Moved Outside to Fix Scope Errors ---

@Composable
fun StreakItem(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge.copy(
                shadow = Shadow(color = Color.Black, offset = Offset(2f, 2f), blurRadius = 8f)
            )
        )
        Text(text = label, fontSize = 12.sp, color = Color.White)
    }
}

@Composable
fun StreakRing(progress: Float, content: @Composable () -> Unit) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "ring"
    )
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            strokeWidth = 8.dp,
            color = AccentButton,
            trackColor = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.size(200.dp)
        )
        content()
    }
}

@Composable
fun RelapseButton(onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Button(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.height(60.dp).fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
    ) {
        Text("I slipped today", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

@Composable
fun TriggerDialog(title: String, actionLabel: String, onDismiss: () -> Unit, onConfirm: (Int, String?) -> Unit) {
    var selectedIntensity by remember { mutableStateOf(2) }
    var selectedTrigger by remember { mutableStateOf<String?>(null) }
    val triggerOptions = listOf("Stress", "Boredom", "Loneliness", "Social media", "Fatigue", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Intensity", fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    FilterChip(selected = selectedIntensity == 1, onClick = { selectedIntensity = 1 }, label = { Text("Mild") })
                    FilterChip(selected = selectedIntensity == 2, onClick = { selectedIntensity = 2 }, label = { Text("Strong") })
                    FilterChip(selected = selectedIntensity == 3, onClick = { selectedIntensity = 3 }, label = { Text("Extreme") })
                }
                Spacer(Modifier.height(8.dp))
                Text("Trigger", fontWeight = FontWeight.Bold)
                FlowRow(maxItemsInEachRow = 3) {
                    triggerOptions.forEach { option ->
                        FilterChip(
                            selected = selectedTrigger == option,
                            onClick = { selectedTrigger = option },
                            label = { Text(option) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(selectedIntensity, selectedTrigger) }) { Text("Log $actionLabel") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}