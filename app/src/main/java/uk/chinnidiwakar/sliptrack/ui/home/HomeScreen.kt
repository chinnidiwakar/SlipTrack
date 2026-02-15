package uk.chinnidiwakar.sliptrack.ui.home

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import uk.chinnidiwakar.sliptrack.HomeViewModel
import uk.chinnidiwakar.sliptrack.HomeViewModelFactory
import uk.chinnidiwakar.sliptrack.SkyBackground

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    // 1. Get the tools needed for the Factory
    val database = remember { uk.chinnidiwakar.sliptrack.DatabaseProvider.get(context) }
    val dao = remember { database.slipDao() }
    val preferenceManager = remember { uk.chinnidiwakar.sliptrack.PreferenceManager(context) }

    // 2. Fix the Factory call
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(dao, preferenceManager)
    )

    // 3. Collect State
    val elapsedText by viewModel.elapsedText.collectAsState()
    val journeyName by viewModel.journeyName.collectAsState() // 👈 Get journeyName here
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

    // --- Dialogs (Keep your existing Handle Dialogs logic here) ---
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

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
        Box(modifier = Modifier.fillMaxSize()) {
            SkyBackground(streak = currentStreak)

            IconButton(
                onClick = { navController.navigate("settings") },
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(end = 8.dp).zIndex(2f)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }

            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Good day 🌿", modifier = Modifier.padding(top = 16.dp), fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text("\"$quote\"", modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = Color.White.copy(alpha = 0.9f), fontStyle = FontStyle.Italic)

                Spacer(Modifier.weight(0.5f))

                // 4. Pass the data INTO the StreakRing
                StreakRing(
                    progress = (currentStreak.coerceAtMost(30)) / 30f,
                    elapsedText = elapsedText,
                    journeyName = journeyName
                )

                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StreakItem(value = currentStreak, label = "Current")
                    StreakItem(value = longestStreak, label = "Best")
                }

                Spacer(Modifier.weight(1f))
                Text("You're trying — that matters 🤍", fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                Spacer(Modifier.height(24.dp))

                // --- THE TRIO BUTTONS ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { showVictoryDialog = true }, modifier = Modifier.weight(1f).height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), shape = RoundedCornerShape(20.dp)) {
                        Text("Resist 🛡️", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = { navController.navigate("emergency") }, modifier = Modifier.size(60.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(0.dp)) {
                        Icon(Icons.Default.Shield, contentDescription = "SOS", tint = Color.White)
                    }
                    Button(onClick = { showSlipDialog = true }, modifier = Modifier.weight(1f).height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)), shape = RoundedCornerShape(20.dp)) {
                        Text("Slip", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
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
fun StreakRing(progress: Float, elapsedText: String, journeyName: String) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "ring"
    )

    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            strokeWidth = 8.dp,
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.size(200.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = elapsedText,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "since $journeyName",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TriggerDialog(
    title: String,
    actionLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (Int, String?) -> Unit
) {
    var selectedIntensity by remember { mutableIntStateOf(2) }
    var selectedTrigger by remember { mutableStateOf<String?>(null) }
    val triggerOptions = listOf("Stress", "Boredom", "Loneliness", "Social Media", "Fatigue", "Late Night", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("How strong is it?", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 3).forEach { level ->
                        FilterChip(
                            selected = selectedIntensity == level,
                            onClick = { selectedIntensity = level },
                            label = {
                                Text(when(level) {
                                    1 -> "Mild"
                                    2 -> "Strong"
                                    else -> "Extreme"
                                })
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text("What triggered it?", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    triggerOptions.forEach { option ->
                        FilterChip(
                            selected = selectedTrigger == option,
                            onClick = { selectedTrigger = option },
                            label = { Text(option) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedIntensity, selectedTrigger) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (actionLabel == "Victory") Color(0xFF4CAF50) else Color(0xFFE57373)
                )
            ) {
                Text("Log $actionLabel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}