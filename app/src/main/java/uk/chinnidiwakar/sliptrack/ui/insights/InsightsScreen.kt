package uk.chinnidiwakar.sliptrack.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.chinnidiwakar.sliptrack.InsightsViewModel
import uk.chinnidiwakar.sliptrack.InsightsViewModelFactory

@Composable
fun InsightsScreen() {
    val context = LocalContext.current
    val viewModel: InsightsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = InsightsViewModelFactory(context)
    )

    val insights by viewModel.insights.collectAsState()
    val weeklyReport by viewModel.weeklyReport.collectAsState()

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Insights",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )

            InsightCard(
                "Weekly report",
                "${weeklyReport.cleanDaysThisWeek} clean days • ${weeklyReport.victoriesThisWeek} victories • ${weeklyReport.slipsThisWeek} slips"
            )

            // ... inside your Column ...
            if (insights == null) {
                // Show "Willpower Meter" even with low data to encourage them
                WillpowerMeter(weeklyReport.victoriesThisWeek, weeklyReport.slipsThisWeek)
            } else {
                Text("Pattern Analysis", style = MaterialTheme.typography.labelLarge)

                // Group 1: The Danger Zones (Red/Warning vibe)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniInsightCard("Danger Hour", insights!!.mostCommonHour ?: "--", Modifier.weight(1f))
                    MiniInsightCard("Hardest Day", insights!!.mostCommonDay ?: "--", Modifier.weight(1f))
                }

                // Group 2: The Coach (AI/Suggestion vibe)
                insights?.suggestedAction?.let { action ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Smart Recovery Plan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(action, fontSize = 15.sp, lineHeight = 20.sp)
                        }
                    }
                }

                WillpowerMeter(score = insights!!.willpowerScore)

                Text("Pattern Analysis", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun InsightCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun WillpowerMeter(score: Int) {
    val floatScore = score / 100f

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Overall Resilience", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = floatScore,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            )
            Text("$score% of urges defeated", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

// Overload to handle the "Weekly" case from step 2
@Composable
fun WillpowerMeter(victories: Int, slips: Int) {
    val total = (victories + slips).coerceAtLeast(1)
    val score = ((victories.toFloat() / total) * 100).toInt()
    WillpowerMeter(score = score)
}

@Composable
fun MiniInsightCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}