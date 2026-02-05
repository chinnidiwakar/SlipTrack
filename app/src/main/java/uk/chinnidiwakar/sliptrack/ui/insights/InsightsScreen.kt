package uk.chinnidiwakar.sliptrack.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.chinnidiwakar.sliptrack.InsightsViewModel
import uk.chinnidiwakar.sliptrack.InsightsViewModelFactory

//--------------------Insights --------------------

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
                .padding(20.dp),
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

            if (insights == null) {
                Text(
                    "Not enough data yet.\nInsights will appear once patterns emerge.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            } else {

                insights?.mostCommonHour?.let { value: String ->
                    InsightCard("Most difficult time", value.toString())
                }

                insights?.mostCommonDay?.let { value: String ->
                    InsightCard("Hardest day", value.toString())
                }

                insights?.weekComparison?.let { value: String ->
                    InsightCard("This week vs last week", value.toString())
                }

                insights?.averageStreak?.let { value: String ->
                    InsightCard("Typical streak length", value.toString())
                }

                insights?.topTrigger?.let { value: String ->
                    InsightCard("Top trigger", value)
                }

                insights?.suggestedAction?.let { value: String ->
                    InsightCard("Suggested next step", value)
                }

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