package uk.chinnidiwakar.sliptrack.ui.insights

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import uk.chinnidiwakar.sliptrack.InsightsViewModel
import uk.chinnidiwakar.sliptrack.InsightsViewModelFactory

@Composable
fun InsightsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: InsightsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = InsightsViewModelFactory(context)
    )

    val insights by viewModel.insights.collectAsState()
    val weeklyReport by viewModel.weeklyReport.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching { viewModel.exportData(uri) }
                    .onSuccess { count ->
                        Toast.makeText(context, "Exported $count events", Toast.LENGTH_LONG).show()
                    }
                    .onFailure {
                        Toast.makeText(context, "Export failed: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching { viewModel.importData(uri) }
                    .onSuccess { count ->
                        Toast.makeText(context, "Imported $count events", Toast.LENGTH_LONG).show()
                    }
                    .onFailure {
                        Toast.makeText(context, "Import failed: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = { exportLauncher.launch("sliptrack-backup.json") }, modifier = Modifier.weight(1f)) {
                    Text("Export data")
                }
                Button(onClick = { importLauncher.launch(arrayOf("application/json")) }, modifier = Modifier.weight(1f)) {
                    Text("Import data")
                }
            }

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
                    InsightCard("Most difficult time", value)
                }

                insights?.mostCommonDay?.let { value: String ->
                    InsightCard("Hardest day", value)
                }

                insights?.weekComparison?.let { value: String ->
                    InsightCard("This week vs last week", value)
                }

                insights?.averageStreak?.let { value: String ->
                    InsightCard("Typical streak length", value)
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
