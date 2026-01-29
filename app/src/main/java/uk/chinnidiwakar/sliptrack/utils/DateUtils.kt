package uk.chinnidiwakar.sliptrack.utils

import uk.chinnidiwakar.sliptrack.SlipEvent
import uk.chinnidiwakar.sliptrack.ui.history.DaySummary
import uk.chinnidiwakar.sliptrack.ui.calendar.CalendarDay

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