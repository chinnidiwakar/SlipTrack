package uk.chinnidiwakar.sliptrack.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class TimestampUtilsTest {

    @Test
    fun normalizeTimestamp_convertsSecondsToMillis() {
        assertEquals(1_700_000_000_000L, normalizeTimestamp(1_700_000_000L))
    }

    @Test
    fun toLocalDate_sameResultForSecondsAndMillisEpoch() {
        val zone = ZoneId.systemDefault()
        val date = LocalDate.of(2025, 1, 10)
        val millis = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val seconds = millis / 1000

        assertEquals(date, toLocalDate(millis, zone))
        assertEquals(date, toLocalDate(seconds, zone))
    }
}
