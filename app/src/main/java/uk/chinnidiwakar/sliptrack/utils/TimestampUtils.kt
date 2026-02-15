package uk.chinnidiwakar.sliptrack.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

private const val MILLIS_THRESHOLD = 1_000_000_000_000L

fun normalizeTimestamp(raw: Long): Long {
    return if (raw < MILLIS_THRESHOLD) raw * 1000 else raw
}

fun toZonedDateTime(rawTimestamp: Long, zone: ZoneId = ZoneId.systemDefault()): ZonedDateTime {
    return Instant.ofEpochMilli(normalizeTimestamp(rawTimestamp)).atZone(zone)
}

fun toLocalDate(rawTimestamp: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate {
    return toZonedDateTime(rawTimestamp, zone).toLocalDate()
}
