package uk.chinnidiwakar.sliptrack.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.chinnidiwakar.sliptrack.SlipEvent
import java.time.LocalDateTime
import java.time.ZoneId

class InsightsLogicTest {

    private fun epochMillis(daysAgo: Long, hour: Int): Long {
        return LocalDateTime.now()
            .minusDays(daysAgo)
            .withHour(hour)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    @Test
    fun computeInsights_returnsNullWithTooLittleData() {
        val slips = listOf(
            SlipEvent(timestamp = epochMillis(2, 9)),
            SlipEvent(timestamp = epochMillis(1, 9))
        )

        assertEquals(null, computeInsights(slips))
    }

    @Test
    fun computeInsights_populatesAllFieldsForValidData() {
        val slips = listOf(
            SlipEvent(timestamp = epochMillis(10, 21), trigger = "Stress"),
            SlipEvent(timestamp = epochMillis(7, 21), trigger = "Stress"),
            SlipEvent(timestamp = epochMillis(3, 21), trigger = "Boredom"),
            SlipEvent(timestamp = epochMillis(1, 10), trigger = "Stress")
        )

        val insights = computeInsights(slips)

        assertNotNull(insights)
        assertEquals("9 PM", insights?.mostCommonHour)
        assertTrue(insights?.mostCommonDay?.isNotBlank() == true)
        assertTrue(insights?.averageStreak?.contains("days") == true)
        assertEquals("Stress", insights?.topTrigger)
        assertTrue(insights?.suggestedAction?.isNotBlank() == true)
    }
    @Test
    fun computeWeeklyReport_countsWeeklySlipsVictoriesAndCleanDays() {
        val report = computeWeeklyReport(
            listOf(
                SlipEvent(timestamp = epochMillis(1, 10), isResist = false),
                SlipEvent(timestamp = epochMillis(1, 12), isResist = true),
                SlipEvent(timestamp = epochMillis(0, 9), isResist = true)
            )
        )

        assertEquals(1, report.slipsThisWeek)
        assertEquals(2, report.victoriesThisWeek)
        assertTrue(report.cleanDaysThisWeek >= 0)
    }

}
