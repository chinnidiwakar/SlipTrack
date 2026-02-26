package uk.chinnidiwakar.sliptrack

import uk.chinnidiwakar.sliptrack.utils.normalizeTimestamp

object StreakCalculator {

    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

    fun currentStreak(slips: List<SlipEvent>, nowMillis: Long = System.currentTimeMillis()): Int {
        if (slips.isEmpty()) return 0

        val lastSlipTimestamp = slips.maxOfOrNull { normalizeTimestamp(it.timestamp) } ?: return 0
        return fullDaysSince(lastSlipTimestamp, nowMillis)
    }

    fun fullDaysSince(rawTimestamp: Long, nowMillis: Long = System.currentTimeMillis()): Int {
        val normalized = normalizeTimestamp(rawTimestamp)
        return ((nowMillis - normalized).coerceAtLeast(0L) / DAY_MILLIS).toInt()
    }

    fun longestStreak(slips: List<SlipEvent>, nowMillis: Long = System.currentTimeMillis()): Int {
        if (slips.isEmpty()) return 0

        val timestamps = slips
            .map { normalizeTimestamp(it.timestamp) }
            .distinct()
            .sorted()
        if (timestamps.isEmpty()) return 0

        var longest = 0
        for (i in 1 until timestamps.size) {
            val gap = ((timestamps[i] - timestamps[i - 1]).coerceAtLeast(0L) / DAY_MILLIS).toInt()
            longest = maxOf(longest, gap)
        }

        val currentGap = fullDaysSince(timestamps.last(), nowMillis)
        longest = maxOf(longest, currentGap)

        return longest
    }

    fun averageStreak(events: List<SlipEvent>, nowMillis: Long = System.currentTimeMillis()): Int {
        val actualSlips = events.filter { !it.isResist }
        if (actualSlips.size < 2) return 0

        val timestamps = actualSlips
            .map { normalizeTimestamp(it.timestamp) }
            .distinct()
            .sorted()

        val gaps = (1 until timestamps.size).map {
            ((timestamps[it] - timestamps[it - 1]).coerceAtLeast(0L) / DAY_MILLIS).toInt()
        }
        return gaps.average().toInt()
    }

}
