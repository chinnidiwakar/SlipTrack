package uk.chinnidiwakar.sliptrack

import java.time.ZoneId
import java.time.temporal.ChronoUnit
import uk.chinnidiwakar.sliptrack.utils.normalizeTimestamp

object StreakCalculator {

    fun currentStreak(slips: List<SlipEvent>): Int {
        if (slips.isEmpty()) return 0

        val today = java.time.LocalDate.now()
        val lastSlipDate = slipDates(slips).maxOrNull() ?: return 0

        return ChronoUnit.DAYS.between(lastSlipDate, today).toInt()
    }

    fun longestStreak(slips: List<SlipEvent>): Int {
        if (slips.isEmpty()) return 0

        val dates = slipDates(slips).sorted()
        if (dates.isEmpty()) return 0

        var longest = 0
        for (i in 1 until dates.size) {
            val gap = ChronoUnit.DAYS.between(dates[i - 1], dates[i]).toInt()
            longest = maxOf(longest, gap)
        }

        val currentGap = ChronoUnit.DAYS.between(dates.last(), java.time.LocalDate.now()).toInt()
        longest = maxOf(longest, currentGap)

        return longest
    }

    fun averageStreak(events: List<SlipEvent>): Int {
        val actualSlips = events.filter { !it.isResist }
        if (actualSlips.size < 2) return 0

        val dates = slipDates(actualSlips).sorted()

        val gaps = (1 until dates.size).map {
            ChronoUnit.DAYS.between(dates[it - 1], dates[it]).toInt()
        }
        return gaps.average().toInt()
    }

    private fun slipDates(slips: List<SlipEvent>): Set<java.time.LocalDate> {
        val zone = ZoneId.systemDefault()
        return slips
            .map { java.time.Instant.ofEpochMilli(normalizeTimestamp(it.timestamp)).atZone(zone).toLocalDate() }
            .toSet()
    }

}
