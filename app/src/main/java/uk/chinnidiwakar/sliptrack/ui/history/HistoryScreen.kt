package uk.chinnidiwakar.sliptrack.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.chinnidiwakar.sliptrack.HistoryViewModel
import uk.chinnidiwakar.sliptrack.HistoryViewModelFactory
import uk.chinnidiwakar.sliptrack.SlipEvent

// ---------------- HISTORY UI ----------------

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val viewModel: HistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = HistoryViewModelFactory(context)
    )
    val history by viewModel.history.collectAsState()

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Your journey",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Patterns over time",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )

            Spacer(Modifier.height(20.dp))

            history.forEachIndexed { index, day ->
                HistoryRow(day)
            }
        }

    }
}


// ---------------- HISTORY CARD ----------------

@Composable
fun HistoryRow(day: DaySummary) {

    val indicatorColor = when {
        day.relapses == 0 -> Color(0xFF66BB6A)
        day.relapses == 1 -> Color(0xFFFFB74D)
        else -> Color(0xFFE57373)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = day.date,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${day.relapses} slips",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            if (day.relapses > 0) {
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width((day.relapses.coerceAtMost(3) * 18).dp)
                        .background(
                            indicatorColor.copy(alpha = 0.7f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}


// ---------------- DATA MODEL FOR UI ----------------

data class DaySummary(
    val date: String,
    val relapses: Int,
    val longestStreak: String
)

fun buildDaySummaries(slips: List<SlipEvent>): List<DaySummary> {
    val actualSlips = slips.filter { !it.isResist }
    if (actualSlips.isEmpty()) return emptyList()

    val grouped = actualSlips.groupBy {
        val date = java.time.Instant.ofEpochMilli(it.timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        date
    }

    return grouped.entries
        .sortedByDescending { it.key }
        .map { entry ->
            val dateLabel = entry.key.toString() // e.g. 2026-01-28
            val count = entry.value.size

            DaySummary(
                date = dateLabel,
                relapses = count,
                longestStreak = "—" // we’ll compute this later properly
            )
        }
}
