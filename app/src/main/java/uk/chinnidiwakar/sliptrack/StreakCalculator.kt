package uk.chinnidiwakar.sliptrack

import uk.chinnidiwakar.sliptrack.utils.normalizeTimestamp

object StreakCalculator {

    private const val DAY_MILLIS = 1000L * 60 * 60 * 24

    fun currentStreak(slips: List<SlipEvent>): Int {
        if (slips.isEmpty()) return 0

        val lastSlip = slips.maxOf { normalizeTimestamp(it.timestamp) }
        val diff = System.currentTimeMillis() - lastSlip
        return (diff / DAY_MILLIS).toInt()
    }

    fun longestStreak(slips: List<SlipEvent>): Int {
        if (slips.size < 2) return currentStreak(slips)

        val sorted = slips
            .map { normalizeTimestamp(it.timestamp) }
            .sorted()

        var longest = 0

        for (i in 1 until sorted.size) {
            val gap = (sorted[i] - sorted[i - 1]) / DAY_MILLIS
            longest = maxOf(longest, gap.toInt())
        }

        val currentGap = currentStreak(slips)
        return maxOf(longest, currentGap)
    }

    fun averageStreak(events: List<SlipEvent>): Int {
        val actualSlips = events.filter { !it.isResist }
        if (actualSlips.size < 2) return 0

        val sorted = actualSlips
            .map { normalizeTimestamp(it.timestamp) }
            .sorted()

        val gaps = (1 until sorted.size).map {
            ((sorted[it] - sorted[it - 1]) / DAY_MILLIS).toInt()
        }

        return gaps.average().toInt()
    }
}