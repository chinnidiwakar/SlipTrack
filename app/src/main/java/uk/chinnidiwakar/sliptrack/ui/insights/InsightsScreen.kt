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

            if (insights == null) {
                Text(
                    "Not enough data yet.\nInsights will appear once patterns emerge.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            } else {

                insights?.mostCommonHour?.let {
                    InsightCard("Most difficult time", it)
                }

                insights?.mostCommonDay?.let {
                    InsightCard("Hardest day", it)
                }

                insights?.weekComparison?.let {
                    InsightCard("This week vs last week", it)
                }

                insights?.averageStreak?.let {
                    InsightCard("Typical streak length", it)
                }
            }
        }
    }
}
@Composable
fun InsightCard(title: String, value: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
