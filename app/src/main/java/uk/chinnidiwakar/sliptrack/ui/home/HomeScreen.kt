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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- HEADER BOX ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp) // Slightly taller to ensure the row fits
            ) {
                // Layer 0: Background
                SkyTheme(
                    streak = currentStreak,
                    modifier = Modifier.matchParentSize()
                )

                // Layer 1: The Actual Text Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp) // Avoid status bar area
                        .zIndex(1f), // FORCES this to the front
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Good day 🌿",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Text(
                        text = "\"$quote\"",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White, // Force white here too
                        fontStyle = FontStyle.Italic
                    )

                    Spacer(Modifier.height(16.dp))

                    StreakRing(progress = (currentStreak.coerceAtMost(30)) / 30f) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = elapsedText,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "since last slip",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // This Row is likely what was getting cut off
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StreakItem(value = currentStreak, label = "Current")
                        StreakItem(value = longestStreak, label = "Best")
                    }
                }
            }

            // --- BOTTOM CONTENT (Tightened) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp), // Controlled padding
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp)) // Small gap after the sky header

                Text(
                    text = "You're trying — that matters 🤍",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Spacer(Modifier.height(32.dp)) // Gap between text and button

                RelapseButton { viewModel.logSlip() }

                // This ensures everything stays pushed toward the top
                // rather than spreading across the whole screen
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