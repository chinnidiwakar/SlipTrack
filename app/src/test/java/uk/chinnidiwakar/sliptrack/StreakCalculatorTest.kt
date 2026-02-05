package uk.chinnidiwakar.sliptrack

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StreakCalculatorTest {

    private fun atStartOfDayEpochMillis(daysAgo: Long): Long {
        return LocalDate.now()
            .minusDays(daysAgo)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    @Test
    fun currentStreak_usesMostRecentSlip() {
        val slips = listOf(
            SlipEvent(timestamp = atStartOfDayEpochMillis(10)),
            SlipEvent(timestamp = atStartOfDayEpochMillis(4)),
            SlipEvent(timestamp = atStartOfDayEpochMillis(2))
        )

        assertEquals(2, StreakCalculator.currentStreak(slips))
    }

    @Test
    fun longestStreak_returnsLargestGapBetweenSlipDays() {
        val slips = listOf(
            SlipEvent(timestamp = atStartOfDayEpochMillis(20)),
            SlipEvent(timestamp = atStartOfDayEpochMillis(15)),
            SlipEvent(timestamp = atStartOfDayEpochMillis(4)),
            SlipEvent(timestamp = atStartOfDayEpochMillis(1))
        )

        assertEquals(11, StreakCalculator.longestStreak(slips))
    }

    @Test
    fun averageStreak_ignoresResistEvents() {
        val events = listOf(
            SlipEvent(timestamp = atStartOfDayEpochMillis(12), isResist = false),
            SlipEvent(timestamp = atStartOfDayEpochMillis(8), isResist = false),
            SlipEvent(timestamp = atStartOfDayEpochMillis(7), isResist = true),
            SlipEvent(timestamp = atStartOfDayEpochMillis(3), isResist = false)
        )

        assertEquals(4, StreakCalculator.averageStreak(events))
    }
}
