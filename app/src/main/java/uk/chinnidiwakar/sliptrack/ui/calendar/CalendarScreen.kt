package uk.chinnidiwakar.sliptrack.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import uk.chinnidiwakar.sliptrack.CalendarViewModel
import uk.chinnidiwakar.sliptrack.CalendarViewModelFactory
import java.time.LocalDate
import java.time.YearMonth


// ================= HEADER =================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen() {
    val context = LocalContext.current
    val viewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(context)
    )

    val month by viewModel.currentMonth.collectAsState()
    val days by viewModel.days.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = 1,
        pageCount = { 3 }
    )

    // Using Surface as the root container
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // 👈 Move padding here to affect the whole screen
                .padding(horizontal = 20.dp)
        ) {
            // Updated Header: Removed fillMaxSize()
            CalendarHeader(month)

            Spacer(Modifier.height(16.dp))

            // The Pager needs a specific weight or height to show up correctly
            Box(modifier = Modifier.weight(1f)) {
                HorizontalPager(state = pagerState) { page ->
                    val pageMonth = remember(page, month) {
                        when (page) {
                            0 -> month.minusMonths(1)
                            1 -> month
                            else -> month.plusMonths(1)
                        }
                    }

                    val displayDays = if (pageMonth == month) {
                        days
                    } else {
                        remember(pageMonth) { viewModel.getDaysForMonth(pageMonth) }
                    }

                    CalendarGrid(
                        month = pageMonth,
                        days = displayDays,
                        viewModel = viewModel,
                        selectedDate = selectedDate
                    )
                }
            }

            // Bottom Info Card
            val selectedDayData = remember(selectedDate, days) {
                viewModel.getDayData(selectedDate)
            }

            val relapses = selectedDayData?.relapses ?: 0
            val resisted = selectedDayData?.urgesResisted ?: 0

            Spacer(Modifier.height(16.dp))

            Surface(
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = when {
                            relapses > 0 -> "Relapse recorded"
                            resisted > 0 -> "Battle Won!"
                            else -> "Clean Day"
                        },
                        fontWeight = FontWeight.Bold,
                        color = when {
                            relapses > 0 -> Color(0xFFE57373)
                            resisted > 0 -> Color(0xFFD4AF37)
                            else -> Color(0xFF66BB6A)
                        }
                    )
                    Text(text = "Slips: $relapses | Urges Resisted: $resisted", fontSize = 12.sp)
                }
            }
        }
    }

    // Month Swapping Logic
    LaunchedEffect(pagerState.settledPage) {
        when (pagerState.settledPage) {
            0 -> {
                viewModel.previousMonth()
                pagerState.scrollToPage(1)
            }
            2 -> {
                viewModel.nextMonth()
                pagerState.scrollToPage(1)
            }
        }
    }
}

@Composable
private fun CalendarHeader(month: YearMonth) {
    // REMOVED: fillMaxSize() and statusBarsPadding() from here
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = month.month.name.lowercase()
                .replaceFirstChar { it.uppercase() } + " ${month.year}",
            fontWeight = FontWeight.Bold, // Made bolder for the new UI
            fontSize = 24.sp,
            letterSpacing = 0.6.sp
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Patterns, not judgement",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

// ================= WEEKDAY HEADER =================

@Composable
private fun WeekdayHeader() {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labels.forEach {
            Text(
                text = it,
                modifier = Modifier.width(44.dp),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
            )
        }
    }
}

// ================= GRID =================


@Composable
private fun CalendarGrid(
    month: YearMonth,
    days: List<CalendarDay>,
    viewModel: CalendarViewModel,
    selectedDate: LocalDate
) {
    val firstDayOffset = (month.atDay(1).dayOfWeek.value + 6) % 7
    val totalCells = days.size + firstDayOffset
    val rows = (totalCells + 6) / 7

    Column {
        WeekdayHeader()
        Spacer(Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(rows) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(7) { col ->
                        val index = row * 7 + col - firstDayOffset


                        if (index in days.indices) {
                            val day = days[index]

                            // guard against invalid day numbers (e.g. Feb 29 in non-leap year)
                            if (day.day <= month.lengthOfMonth()) {
                                val date = month.atDay(day.day)
                                val isSelected = date == selectedDate

                                PremiumDayCell(
                                    date = date,
                                    relapses = day.relapses,
                                    urgesResisted = day.urgesResisted, // Pass it here
                                    isSelected = isSelected,
                                    onClick = { viewModel.selectDate(date) }
                                )
                            } else {
                                Spacer(Modifier.size(44.dp))
                            }
                        } else {
                            Spacer(Modifier.size(44.dp))
                        }
                    }
                }
            }
        }
    }
}

// ================= DAY CELL =================

@Composable
private fun PremiumDayCell(
    date: LocalDate,
    relapses: Int,
    urgesResisted: Int, // Add this
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()
    val isFuture = date.isAfter(LocalDate.now())
    val haptic = LocalHapticFeedback.current

    // Logic for the color of the day
    val cellColor = when {
        isFuture -> MaterialTheme.colorScheme.surface // Future is plain surface
        relapses > 0 -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f) // Light Red for slips
        urgesResisted > 0 -> Color(0xFFFFD700).copy(alpha = 0.3f) // Gold for resisting urges
        else -> Color(0xFF66BB6A).copy(alpha = 0.2f) // Soft Green for smooth sailing
    }

    val contentColor = when {
        isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        relapses > 0 -> MaterialTheme.colorScheme.error
        urgesResisted > 0 -> Color(0xFFD4AF37) // Darker Gold for text
        else -> Color(0xFF2E7D32) // Darker Green for text
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else cellColor,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                border = when {
                    isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    isToday -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    else -> BorderStroke(1.dp, Color.Transparent)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isFuture) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else contentColor
            )

            // Visual indicator for slips (little dots)
            if (relapses > 0 && !isSelected) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(relapses.coerceAtMost(3)) {
                        Box(Modifier.size(4.dp).background(contentColor, RoundedCornerShape(100)))
                    }
                }
            }
        }
    }
}
data class CalendarDay(
    val day: Int,
    val relapses: Int,
    val urgesResisted: Int = 0 // Add this!
)
