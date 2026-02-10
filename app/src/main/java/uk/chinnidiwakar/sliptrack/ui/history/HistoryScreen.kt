package uk.chinnidiwakar.sliptrack.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen() {
    Spacer(Modifier.height(15.dp))
    val context = LocalContext.current
    val viewModel: HistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = HistoryViewModelFactory(context)
    )
    val history by viewModel.history.collectAsState()

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .statusBarsPadding()
        ) {
            Text(
                text = "Your journey",
                style = MaterialTheme.typography.headlineSmall, // Future-proof typography
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Patterns over time",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(24.dp))

            // ADD THE HEATMAP HERE
            if (history.isNotEmpty()) {
                ProgressHeatmap(history = history)
                Spacer(Modifier.height(24.dp))
            }

            if (history.isEmpty()) {
                // Friendly empty state
                Text(
                    text = "No entries yet. Great Achivements Ahead!",
                    modifier = Modifier.padding(top = 40.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = history,
                        key = { it.date }
                    ) { day ->
                        HistoryRow(
                            day = day,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryRow(
    day: DaySummary,
    modifier: Modifier = Modifier,
) {

    val indicatorColor = when {
        day.relapses == 0 -> MaterialTheme.colorScheme.primary
        day.relapses == 1 -> MaterialTheme.colorScheme.tertiary
        day.relapses <= 2 -> MaterialTheme.colorScheme.error
        else -> Color(0xFFE57373)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
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

@Composable
fun ProgressHeatmap(history: List<DaySummary>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Last 30 Days",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // We create exactly 30 slots to represent a rolling month
        val totalSlots = 30
        val displayList = List(totalSlots) { index ->
            history.getOrNull(index)
        }

        // Increased height slightly to fit the extra row (5 rows of 6 or 6 rows of 5)
        // Using 6 columns makes 30 days fit perfectly (5 rows)
        Box(modifier = Modifier.height(140.dp).fillMaxWidth()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayList) { day ->
                    val color = when {
                        // 1. Gray: No data (User hasn't reached this day or didn't track)
                        day == null -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

                        // 2. Red: Relapse (Priority 1)
                        day.relapses > 0 -> MaterialTheme.colorScheme.error

                        // 3. Gold: Resisted Urge (The "Battle-Hardened" Win)
                        day.urgesResisted > 0 -> Color(0xFFFFD700)

                        // 4. Green: Smooth sailing (No urges, No slips)
                        else -> Color(0xFF66BB6A)
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(
                                color = color,
                                shape = RoundedCornerShape(6.dp) // Slightly rounder for a modern look
                            )
                    )


                }


            }
        }
    }
}

data class DaySummary(
    val date: String,
    val relapses: Int,
    val urgesResisted: Int = 0, // Added this
    val longestStreak: String
)