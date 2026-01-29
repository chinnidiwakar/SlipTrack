package uk.chinnidiwakar.sliptrack.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
                .padding(18.dp)
        ) {
            Text(
                text = "Your journey",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "No judgement. Just awareness.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(24.dp))

            if (history.isEmpty()) {
                Text(
                    text = "No data yet. Your journey starts today 🌱",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    history.forEach { day ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically()
                        ) {
                            HistoryCard(day)
                        }
                    }
                }

            }
        }
    }
}


// ---------------- HISTORY CARD ----------------

@Composable
fun HistoryCard(day: DaySummary) {

    val background = when {
        day.relapses == 0 -> Color(0xFFE8F5E9)
        day.relapses == 1 -> Color(0xFFFFF3E0)
        else -> Color(0xFFFFEBEE)
    }

    val message = when {
        day.relapses == 0 -> "Strong day 🌿"
        day.relapses == 1 -> "A stumble, still aware 🤍"
        day.relapses <= 3 -> "Tough day, but you showed up"
        else -> "Heavy day. Awareness still matters"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = day.date,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111111)
                )

                Text(
                    text = "${day.relapses} slips",
                    fontSize = 13.sp,
                    color = Color(0xFF555555)
                )
            }

            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF444444)
            )

            if (day.longestStreak.isNotBlank() && day.longestStreak != "—") {
                Text(
                    text = "Best stretch: ${day.longestStreak}",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
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
    if (slips.isEmpty()) return emptyList()

    val grouped = slips.groupBy {
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

