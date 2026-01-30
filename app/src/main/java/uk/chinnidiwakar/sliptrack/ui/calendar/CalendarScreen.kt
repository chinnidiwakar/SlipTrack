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
import uk.chinnidiwakar.sliptrack.ui.theme.AccentButton
import java.time.LocalDate
import java.time.YearMonth

// ================= HEADER =================

@Composable
private fun CalendarHeader(month: YearMonth) {
    Column {
        Text(
            text = month.month.name.lowercase()
                .replaceFirstChar { it.uppercase() } + " ${month.year}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.6.sp
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Patterns, not judgement",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
        )
    }
}

// ================= SCREEN =================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen() {
    val context = LocalContext.current
    val viewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(context)
    )

    val month by viewModel.currentMonth.collectAsState()
    val days by viewModel.days.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = 1,
        pageCount = { 3 }
    )


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            CalendarHeader(month)

            Spacer(Modifier.height(16.dp))

            HorizontalPager(state = pagerState) { page ->
                val pageMonth = when (page) {
                    0 -> month.minusMonths(1)
                    1 -> month
                    else -> month.plusMonths(1)
                }

                CalendarGrid(
                    month = pageMonth,
                    days = if (page == 1) days else days, // same size, stable layout
                    viewModel = viewModel
                )
            }



            val selectedDate by viewModel.selectedDate.collectAsState()

            selectedDate?.let { date ->
                val relapseCount = viewModel.getRelapseCount(date)

                if (relapseCount > 0) {
                    Spacer(Modifier.height(20.dp))

                    Surface(
                        tonalElevation = 1.dp,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(16.dp).fillMaxWidth()) {
                            Text(
                                text = "Relapse details",
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = "${date.dayOfMonth} ${date.month.name.lowercase()
                                    .replaceFirstChar { it.uppercase() }}"
                            )

                            Text(
                                text = "Relapses: $relapseCount",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            LaunchedEffect(pagerState.currentPage) {
                if (pagerState.currentPage == 0) {
                    viewModel.previousMonth()
                    pagerState.scrollToPage(1)
                } else if (pagerState.currentPage == 2) {
                    viewModel.nextMonth()
                    pagerState.scrollToPage(1)
                }
            }

        }
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
    viewModel: CalendarViewModel
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

                                PremiumDayCell(
                                    date = date,
                                    relapses = day.relapses,
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
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .size(44.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .border(
                if (isToday) BorderStroke(1.5.dp, AccentButton)
                else BorderStroke(0.dp, Color.Transparent),
                RoundedCornerShape(14.dp)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = date.dayOfMonth.toString())

            if (relapses > 0) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width((relapses.coerceAtMost(3) * 8).dp)
                        .background(Color(0xFFE57373), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}


// ================= MODEL =================

data class CalendarDay(
    val day: Int,
    val relapses: Int
)
