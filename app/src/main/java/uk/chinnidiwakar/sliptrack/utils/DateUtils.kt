package uk.chinnidiwakar.sliptrack.utils

import uk.chinnidiwakar.sliptrack.SlipEvent
import uk.chinnidiwakar.sliptrack.ui.history.DaySummary
import uk.chinnidiwakar.sliptrack.ui.calendar.CalendarDay
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

// ---------------- UTIL ----------------

fun formatElapsedTime(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ${hours % 24}h"
        hours > 0 -> "${hours}h ${minutes % 60}m"
        else -> "${minutes}m"
    }
}

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

fun buildCalendarDays(
    slips: List<SlipEvent>,
    month: YearMonth
): List<CalendarDay> {

    val zone = ZoneId.systemDefault()
    val daysInMonth = month.lengthOfMonth()

    val counts = slips.groupBy {
        Instant.ofEpochMilli(it.timestamp)
            .atZone(zone)
            .toLocalDate()
    }.mapValues { it.value.size }

    return (1..daysInMonth).map { dayNum ->
        val date = month.atDay(dayNum)
        CalendarDay(
            day = dayNum,
            relapses = counts[date] ?: 0
        )
    }
}