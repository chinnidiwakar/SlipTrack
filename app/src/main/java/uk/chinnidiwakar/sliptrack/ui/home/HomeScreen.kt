package uk.chinnidiwakar.sliptrack.ui.home

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import uk.chinnidiwakar.sliptrack.HapticEngine
import uk.chinnidiwakar.sliptrack.HomeViewModel
import uk.chinnidiwakar.sliptrack.HomeViewModelFactory
import uk.chinnidiwakar.sliptrack.InsightsViewModel
import uk.chinnidiwakar.sliptrack.InsightsViewModelFactory
import uk.chinnidiwakar.sliptrack.SkyBackground
import uk.chinnidiwakar.sliptrack.domain.RiskLevel


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
    val insightsViewModel: InsightsViewModel = viewModel(
        factory = InsightsViewModelFactory(context)
    )

    // 3. Collect State
    val elapsedText by viewModel.elapsedText.collectAsState()
    val journeyName by viewModel.journeyName.collectAsState() // 👈 Get journeyName here
    val currentStreak by viewModel.currentStreak.collectAsState()
    val longestStreak by viewModel.longestStreak.collectAsState()
    val quote by viewModel.dailyQuote.collectAsState()
    val streakShields by viewModel.streakShields.collectAsState()
    val engine = remember { HapticEngine(context) }
    val configuration = LocalConfiguration.current
    val isWideLayout =
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || configuration.screenWidthDp >= 840
    val isTablet = configuration.screenWidthDp >= 840
    var showVictoryDialog by remember { mutableStateOf(false) }
    var showSlipDialog by remember { mutableStateOf(false) }
    var showShieldPrompt by remember { mutableStateOf(false) }
    var pendingTrigger by remember { mutableStateOf<String?>(null) }

    val insights by insightsViewModel.insights.collectAsState()
    val riskLevel = insights?.riskAssessment?.level

    LaunchedEffect(Unit) {
        viewModel.uiMessages.collect { message: String ->
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
                viewModel.logEvent(
                    isResist = true,
                    intensity = level,
                    triggerLabel = selectedTrigger
                )
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
                if (streakShields > 0) {
                    pendingTrigger = selectedTrigger
                    showShieldPrompt = true
                } else {
                    viewModel.logSlip(triggerLabel = selectedTrigger)
                }
                showSlipDialog = false
            }
        )
    }


    if (showShieldPrompt) {
        AlertDialog(
            onDismissRequest = { showShieldPrompt = false },
            title = { Text("Use a streak shield?") },
            text = { Text("You have $streakShields shield(s). Use one to protect your streak this time?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.logSlipWithShield(triggerLabel = pendingTrigger)
                    showShieldPrompt = false
                    pendingTrigger = null
                }) { Text("Use Shield") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.logSlip(triggerLabel = pendingTrigger)
                    showShieldPrompt = false
                    pendingTrigger = null
                }) { Text("Log Slip") }
            }
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
        Box(modifier = Modifier.fillMaxSize()) {
            SkyBackground(streak = currentStreak)

            IconButton(
                onClick = { navController.navigate("settings") },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(end = 8.dp)
                    .zIndex(2f)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 50.dp), // 👈 reserve dock space properly
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Good day 🌿",
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    "\"$quote\"",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.9f),
                    fontStyle = FontStyle.Italic
                )

                if (isWideLayout) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            StreakRing(
                                progress = (currentStreak.coerceAtMost(30)) / 30f,
                                elapsedText = elapsedText,
                                journeyName = journeyName,
                                ringSize = if (isTablet) 260.dp else 220.dp,
                                valueFontSize = if (isTablet) 40.sp else 34.sp
                            )
                            Spacer(Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StreakItem(value = currentStreak, label = "Current")
                                StreakItem(value = longestStreak, label = "Best")
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "You're trying — that matters 🤍",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(18.dp))
                            ActionButtonsRow(
                                streakShields = streakShields,
                                riskLevel = riskLevel,
                                onResist = {
                                    engine.victory()
                                    showVictoryDialog = true
                                },
                                onSlip = {
                                    engine.victory()
                                    showSlipDialog = true
                                },
                                onEmergency = { navController.navigate("emergency") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.weight(0.5f))
                    StreakRing(
                        progress = (currentStreak.coerceAtMost(30)) / 30f,
                        elapsedText = elapsedText,
                        journeyName = journeyName
                    )
                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StreakItem(value = currentStreak, label = "Current")
                        StreakItem(value = longestStreak, label = "Best")
                    }

                    Spacer(Modifier.weight(1f))
                    Text(
                        "You're trying — that matters 🤍",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(24.dp))
                    ActionButtonsRow(
                        streakShields = streakShields,
                        riskLevel = riskLevel,
                        onResist = {
                            engine.victory()
                            showVictoryDialog = true
                        },
                        onSlip = {
                            engine.victory()
                            showSlipDialog = true
                        },
                        onEmergency = { navController.navigate("emergency") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsRow(
    streakShields: Int,
    riskLevel: RiskLevel?,
    onResist: () -> Unit,
    onSlip: () -> Unit,
    onEmergency: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleAnim"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAnim"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PremiumGlassButton(
            text = "Resist 🛡️",
            accentColor = Color(0xFF66BB6A),
            onClick = onResist,
            modifier = Modifier.weight(1f),
            riskLevel = riskLevel,
            isPrimary = true
        )

        Box(
            modifier = Modifier
                .size(76.dp)
                .graphicsLayer {
                    scaleX =
                        if (riskLevel == RiskLevel.HIGH) 1.18f else if (streakShields > 0) scale else 1f
                    scaleY =
                        if (riskLevel == RiskLevel.HIGH) 1.18f else if (streakShields > 0) scale else 1f
                },
            contentAlignment = Alignment.Center
        ) {
            if (streakShields > 0) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8E2A2A).copy(alpha = glowAlpha))
                        .blur(16.dp)
                )
            }

            Surface(
                onClick = onEmergency,
                shape = CircleShape,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                color = Color(0xFF7A1F1F),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "SOS",
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    )

                    if (streakShields > 0) {
                        Text(
                            text = streakShields.coerceAtMost(9).toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.4f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 8f
                                )
                            )
                        )
                    }
                }
            }
        }

        PremiumGlassButton(
            text = "Slip",
            accentColor = Color(0xFFEF5350),
            onClick = onSlip,
            modifier = Modifier.weight(1f),
            riskLevel = riskLevel,
            isPrimary = false
        )
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
fun StreakRing(
    progress: Float,
    elapsedText: String,
    journeyName: String,
    ringSize: Dp = 200.dp,
    valueFontSize: TextUnit = 34.sp
) {
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
            modifier = Modifier.size(ringSize)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = elapsedText,
                fontSize = valueFontSize,
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
    val triggerOptions =
        listOf("Stress", "Boredom", "Loneliness", "Social Media", "Fatigue", "Late Night", "Other")

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
                                Text(
                                    when (level) {
                                        1 -> "Mild"
                                        2 -> "Strong"
                                        else -> "Extreme"
                                    }
                                )
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
                    containerColor = if (actionLabel == "Victory") Color(0xFF4CAF50) else Color(
                        0xFFE57373
                    )
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

@Composable
fun ShieldCountBadge(count: Int, modifier: Modifier = Modifier) {
    if (count <= 0) return

    Surface(
        shape = CircleShape,
        color = Color(0xFFFFD54F),
        shadowElevation = 6.dp,
        modifier = modifier.size(22.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = count.coerceAtMost(9).toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun PremiumGlassButton(
    text: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    riskLevel: RiskLevel? = null,
    isPrimary: Boolean
) {

    val infiniteTransition = rememberInfiniteTransition(label = "sweep")

    // Light sweep animation
    val sweepOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAnim"
    )

    // Subtle glow if Stable and primary (Resist)
    val glowAlpha = if (riskLevel == RiskLevel.STABLE && isPrimary) 0.12f else 0f

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.04f),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = modifier
            .height(60.dp)
            .border(
                width = if (glowAlpha > 0f) 1.dp else 0.dp,
                color = accentColor.copy(alpha = glowAlpha),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Light sweep effect (only when stable + primary)
            if (riskLevel == RiskLevel.STABLE && isPrimary) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                        .graphicsLayer {
                            translationX = sweepOffset
                            rotationZ = 15f
                        }
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    accentColor.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }
    }
}