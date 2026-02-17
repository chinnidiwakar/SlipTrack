package uk.chinnidiwakar.sliptrack.ui.insights

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import uk.chinnidiwakar.sliptrack.InsightsViewModel
import uk.chinnidiwakar.sliptrack.InsightsViewModelFactory

@Composable
fun InsightsScreen() {
    val context = LocalContext.current
    val viewModel: InsightsViewModel = viewModel(factory = InsightsViewModelFactory(context))

    val insights by viewModel.insights.collectAsState()
    val weeklyReport by viewModel.weeklyReport.collectAsState()

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                "Insights",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )

            // --- Weekly Summary Glass Card ---
            InsightCard(
                "Weekly Report",
                "${weeklyReport.cleanDaysThisWeek} Clean • ${weeklyReport.victoriesThisWeek} Won • ${weeklyReport.slipsThisWeek} Slips"
            )

            if (insights == null) {
                WillpowerMeter(weeklyReport.victoriesThisWeek, weeklyReport.slipsThisWeek)
            } else {
                Text("Pattern Analysis", style = MaterialTheme.typography.labelLarge, color = Color.Gray)

                // --- Danger Zones (Side by Side Glass) ---
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniInsightCard("Danger Hour", insights!!.mostCommonHour ?: "--", Modifier.weight(1f))
                    MiniInsightCard("Hardest Day", insights!!.mostCommonDay ?: "--", Modifier.weight(1f))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniInsightCard("Current Streak", insights!!.currentStreak ?: "--", Modifier.weight(1f))
                    MiniInsightCard("Risk Window", insights!!.hardestWindow ?: "--", Modifier.weight(1f))
                }

                insights?.recentSlipRate?.let {
                    InsightCard("Recent Slip Velocity", it)
                }

                // --- Smart Recovery Plan (Glow Glass) ---
                insights?.suggestedAction?.let { action ->
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Smart Recovery Plan", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text(action, fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                WillpowerMeter(score = insights!!.willpowerScore)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// --- REUSABLE GLASS CONTAINER ---
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    opacity: Float = 0.2f,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = opacity),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        tonalElevation = 2.dp
    ) {
        content()
    }
}

@Composable
fun InsightCard(title: String, value: String) {
    GlassCard(opacity = 0.25f) {
        Column(Modifier.padding(20.dp)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun WillpowerMeter(score: Int) {
    val floatScore = score / 100f

    GlassCard(opacity = 0.2f) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Overall Resilience", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("$score%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))

            // Modernized Progress Bar
            Box(contentAlignment = Alignment.CenterStart) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(100))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(floatScore)
                        .height(10.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(100))
                )
            }

            Text("Defeated $score% of recorded urges", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun WillpowerMeter(victories: Int, slips: Int) {
    val total = (victories + slips).coerceAtLeast(1)
    val score = ((victories.toFloat() / total) * 100).toInt()
    WillpowerMeter(score = score)
}

@Composable
fun MiniInsightCard(title: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, opacity = 0.25f) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}