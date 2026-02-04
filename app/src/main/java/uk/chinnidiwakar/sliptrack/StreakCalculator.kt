package uk.chinnidiwakar.sliptrack

import java.time.ZoneId
import java.time.temporal.ChronoUnit

object StreakCalculator {

    fun currentStreak(slips: List<SlipEvent>): Int {
        // We handle filtering in the ViewModel now, so 'slips' here
        // should already be actual slips only.
        if (slips.isEmpty()) return 0

        val zone = ZoneId.systemDefault()
        val today = java.time.LocalDate.now()

        val lastSlipDate = slips
            .maxByOrNull { it.timestamp }!!
            .timestamp
            .let { java.time.Instant.ofEpochMilli(normalizeTimestamp(it)).atZone(zone).toLocalDate() }

        return ChronoUnit.DAYS.between(lastSlipDate, today).toInt()
    }

    fun longestStreak(slips: List<SlipEvent>): Int {
        // Ensure this list is also only actual slips
        if (slips.size < 2) return 0

        val zone = ZoneId.systemDefault()
        val dates = slips
            .map { java.time.Instant.ofEpochMilli(normalizeTimestamp(it.timestamp)).atZone(zone).toLocalDate() }
            .distinct()
            .sorted()

        var longest = 0
        for (i in 1 until dates.size) {
            val gap = ChronoUnit.DAYS.between(dates[i - 1], dates[i]).toInt()
            longest = maxOf(longest, gap)
        }
        return longest
    }


    fun averageStreak(events: List<SlipEvent>): Int {
        val actualSlips = events.filter { !it.isResist } // 👈 Added filter
        if (actualSlips.size < 2) return 0

        val zone = ZoneId.systemDefault()
        val dates = actualSlips
            .map { java.time.Instant.ofEpochMilli(normalizeTimestamp(it.timestamp)).atZone(zone).toLocalDate() }
            .distinct()
            .sorted()

        val gaps = (1 until dates.size).map {
            ChronoUnit.DAYS.between(dates[it - 1], dates[it]).toInt()
        }
        return gaps.average().toInt()
    }

    private fun normalizeTimestamp(raw: Long): Long {
        return if (raw < 1_000_000_000_000L) raw * 1000 else raw
    }
}
