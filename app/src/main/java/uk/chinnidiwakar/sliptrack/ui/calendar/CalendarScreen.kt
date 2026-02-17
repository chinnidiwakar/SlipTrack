package uk.chinnidiwakar.sliptrack.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import uk.chinnidiwakar.sliptrack.HistoryViewModel
import uk.chinnidiwakar.sliptrack.HistoryViewModelFactory
import java.time.LocalDate
import java.time.YearMonth

// --- DATA CLASSES ---
data class CalendarDay(val day: Int, val relapses: Int, val urgesResisted: Int = 0)
data class DaySummary(val date: String, val relapses: Int, val urgesResisted: Int = 0, val longestStreak: String)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(onOpenFullHistory: (() -> Unit)? = null) {
    val context = LocalContext.current
    val calendarViewModel: CalendarViewModel = viewModel(factory = CalendarViewModelFactory(context))
    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(context))

    val month by calendarViewModel.currentMonth.collectAsState()
    val days by calendarViewModel.days.collectAsState()
    val selectedDate by calendarViewModel.selectedDate.collectAsState()
    val history by historyViewModel.history.collectAsState()

    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val selectedDayData = remember(selectedDate, days) { calendarViewModel.getDayData(selectedDate) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { CalendarHeader(month) }

            // 1. CALENDAR CARD (Glass)
            item {
                GlassCard {
                    Box(modifier = Modifier.padding(16.dp).height(340.dp)) {
                        HorizontalPager(state = pagerState) { page ->
                            val pageMonth = when (page) {
                                0 -> month.minusMonths(1)
                                1 -> month
                                else -> month.plusMonths(1)
                            }
                            CalendarGrid(
                                month = pageMonth,
                                days = if (pageMonth == month) days else calendarViewModel.getDaysForMonth(pageMonth),
                                viewModel = calendarViewModel,
                                selectedDate = selectedDate
                            )
                        }
                    }
                }
            }

            // 2. SELECTED DAY INSIGHT (Glass)
            item {
                DayInsightCard(selectedDate, selectedDayData)
            }

            // 3. RECENT ACTIVITY HEADER
            item {
                Text(
                    "Recent Activity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )
            }

            // 4. HISTORY LIST (Glass Rows)
            if (history.isEmpty()) {
                item {
                    Text("No activity recorded yet.",
                        fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(12.dp))
                }
            } else {
                items(history.take(10)) { day ->
                    MiniHistoryRow(day)
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        if (pagerState.settledPage == 0) {
            calendarViewModel.previousMonth()
            pagerState.scrollToPage(1)
        } else if (pagerState.settledPage == 2) {
            calendarViewModel.nextMonth()
            pagerState.scrollToPage(1)
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
fun DayInsightCard(date: LocalDate, data: CalendarDay?) {
    val relapses = data?.relapses ?: 0
    val resisted = data?.urgesResisted ?: 0

    GlassCard(opacity = 0.3f) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(date.toString(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(
                    if (relapses > 0) "Relapse Recorded" else "Clean Day",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (relapses > 0) Color(0xFFE57373) else Color(0xFF81C784)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricItem(relapses.toString(), "Slips")
                MetricItem(resisted.toString(), "Resisted")
            }
        }
    }
}

@Composable
fun MetricItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun MiniHistoryRow(day: DaySummary) {
    // Each row now has its own subtle glass container
    GlassCard(
        modifier = Modifier.padding(vertical = 4.dp),
        opacity = 0.15f
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (day.relapses == 0) Color(0xFF81C784) else Color(0xFFE57373),
                        RoundedCornerShape(100)
                    )
            )

            Spacer(Modifier.width(16.dp))

            Text(day.date, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)

            Text(
                if (day.relapses == 0) "Clean" else "${day.relapses} slips",
                color = if (day.relapses == 0) Color(0xFF81C784) else Color(0xFFE57373),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CalendarHeader(month: YearMonth) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = month.month.name.lowercase().replaceFirstChar { it.uppercase() } + " ${month.year}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text("Your path, your pace.", fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
private fun WeekdayHeader() {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        labels.forEach {
            Text(it, modifier = Modifier.width(44.dp), textAlign = TextAlign.Center, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun CalendarGrid(month: YearMonth, days: List<CalendarDay>, viewModel: CalendarViewModel, selectedDate: LocalDate) {
    val firstDayOffset = (month.atDay(1).dayOfWeek.value + 6) % 7
    val rows = (days.size + firstDayOffset + 6) / 7
    Column {
        WeekdayHeader()
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(rows) { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    repeat(7) { col ->
                        val index = row * 7 + col - firstDayOffset
                        if (index in days.indices && days[index].day <= month.lengthOfMonth()) {
                            val date = month.atDay(days[index].day)
                            PremiumDayCell(
                                date = date,
                                relapses = days[index].relapses,
                                urgesResisted = days[index].urgesResisted,
                                isSelected = date == selectedDate,
                                onClick = { viewModel.selectDate(date) }
                            )
                        } else { Spacer(Modifier.size(44.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumDayCell(date: LocalDate, relapses: Int, urgesResisted: Int, isSelected: Boolean, onClick: () -> Unit) {
    val isToday = date == LocalDate.now()
    val isFuture = date.isAfter(LocalDate.now())
    val haptic = LocalHapticFeedback.current

    val cellColor = when {
        isFuture -> Color.Transparent
        relapses > 0 -> Color(0xFFE57373).copy(alpha = 0.25f)
        urgesResisted > 0 -> Color(0xFFFFD700).copy(alpha = 0.25f)
        else -> Color(0xFF81C784).copy(alpha = 0.25f)
    }

    Box(
        modifier = Modifier.size(44.dp)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else cellColor, RoundedCornerShape(12.dp))
            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else if (isToday) MaterialTheme.colorScheme.primary.copy(0.5f) else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable(enabled = !isFuture) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(date.dayOfMonth.toString(), fontSize = 14.sp, fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium)
    }
}