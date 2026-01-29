package uk.chinnidiwakar.sliptrack.ui.calendar

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import uk.chinnidiwakar.sliptrack.CalendarViewModel
import uk.chinnidiwakar.sliptrack.CalendarViewModelFactory
import uk.chinnidiwakar.sliptrack.SlipEvent
import uk.chinnidiwakar.sliptrack.ui.theme.AccentButton

// ---------------- CALENDAR UI ----------------

@Composable
fun CalendarScreen() {
    val context = LocalContext.current
    val viewModel: CalendarViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = CalendarViewModelFactory(context)
    )

    val days by viewModel.days.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "This month",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Patterns, not judgement",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(24.dp))

            if (days.isEmpty()) {
                Text(
                    text = "No data yet 🌱",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            } else {
                CalendarGrid(days)
            }
        }
    }
}


@Composable
fun CalendarGrid(days: List<CalendarDay>) {
    val columns = 7
    val rows = (days.size + columns - 1) / columns

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    if (index < days.size) {
                        CalendarDayCell(days[index])
                    } else {
                        Spacer(modifier = Modifier.size(44.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(day: CalendarDay) {
    val today = java.time.LocalDate.now().dayOfMonth
    val isToday = day.day == today

    val background = when {
        isToday -> AccentButton.copy(alpha = 0.5f)
        day.relapses == 0 -> Color(0xFFE6F4EA)
        day.relapses == 1 -> Color(0xFFFFF3D6)
        day.relapses == 2 -> Color(0xFFFFE0B2)
        else -> Color(0xFFFFCDD2)
    }


    Box(
        modifier = Modifier
            .size(44.dp)
            .background(background, shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.day.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111111)   // 👈 change here
            )

            if (day.relapses > 0) {
                Text(
                    text = "${day.relapses}",
                    fontSize = 10.sp,
                    color = Color(0xFF444444)  // 👈 change here
                )
            }
        }
    }
}

// ---------------- CALENDAR MODEL ----------------

data class CalendarDay(
    val day: Int,
    val relapses: Int
)

fun buildCalendarDays(slips: List<SlipEvent>): List<CalendarDay> {
    val zone = java.time.ZoneId.systemDefault()
    val today = java.time.LocalDate.now()
    val startOfMonth = today.withDayOfMonth(1)
    val daysInMonth = today.lengthOfMonth()

    // Count slips per date
    val counts = slips.groupBy {
        java.time.Instant.ofEpochMilli(it.timestamp)
            .atZone(zone)
            .toLocalDate()
    }.mapValues { it.value.size }

    // Build full month grid
    return (1..daysInMonth).map { dayNum ->
        val date = startOfMonth.withDayOfMonth(dayNum)
        val count = counts[date] ?: 0
        CalendarDay(day = dayNum, relapses = count)
    }
}