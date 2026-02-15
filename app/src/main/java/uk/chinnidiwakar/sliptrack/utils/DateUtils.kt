package uk.chinnidiwakar.sliptrack.utils

import uk.chinnidiwakar.sliptrack.SlipEvent
import uk.chinnidiwakar.sliptrack.ui.calendar.CalendarDay
import uk.chinnidiwakar.sliptrack.ui.calendar.DaySummary
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

object DateUtils {

    // 1. Helper moved here so everyone can use it
    fun normalizeTimestamp(raw: Long): Long {
        return if (raw < 1_000_000_000_000L) raw * 1000 else raw
    }

    private fun toLocalDate(timestamp: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate {
        return Instant.ofEpochMilli(normalizeTimestamp(timestamp))
            .atZone(zone)
            .toLocalDate()
    }

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
        val actualSlips = slips.filter { !it.isResist }
        if (actualSlips.isEmpty()) return emptyList()

        val grouped = actualSlips.groupBy {
            toLocalDate(it.timestamp)
        }

        return grouped.entries
            .sortedByDescending { it.key }
            .map { entry ->
                DaySummary(
                    date = entry.key.toString(),
                    relapses = entry.value.size,
                    longestStreak = "—"
                )
            }
    }

    fun buildCalendarDays(
        slips: List<SlipEvent>,
        month: YearMonth
    ): List<CalendarDay> {
        val zone = ZoneId.systemDefault()
        val daysInMonth = month.lengthOfMonth()

        // Group EVERY event (Slips and Resists) by date
        val groupedByDate = slips.groupBy {
            toLocalDate(it.timestamp, zone)
        }

        return (1..daysInMonth).map { dayNum ->
            val date = month.atDay(dayNum)
            val dayEvents = groupedByDate[date] ?: emptyList()

            CalendarDay(
                day = dayNum,
                relapses = dayEvents.count { !it.isResist },
                urgesResisted = dayEvents.count { it.isResist }
            )
        }
    }
}
