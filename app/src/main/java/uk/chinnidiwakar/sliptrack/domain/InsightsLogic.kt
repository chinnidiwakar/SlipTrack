package uk.chinnidiwakar.sliptrack.domain

import uk.chinnidiwakar.sliptrack.SlipEvent
import uk.chinnidiwakar.sliptrack.StreakCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private const val UNKNOWN_TRIGGER = "Unspecified"

data class InsightsData(
    val mostCommonHour: String?,
    val mostCommonDay: String?,
    val weekComparison: String?,
    val averageStreak: String?,
    val topTrigger: String?,
    val suggestedAction: String?
)

data class WeeklyReport(
    val slipsThisWeek: Int,
    val victoriesThisWeek: Int,
    val cleanDaysThisWeek: Int
)

fun computeInsights(slips: List<SlipEvent>): InsightsData? {
    if (slips.size < 3) return null

    val zone = ZoneId.systemDefault()
    val times = slips.map {
        Instant.ofEpochMilli(it.timestamp).atZone(zone)
    }

    val mostCommonHour = times
        .groupingBy { it.hour }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
        ?.let { hour ->
            when {
                hour == 0 -> "around midnight"
                hour < 12 -> "$hour AM"
                hour == 12 -> "12 PM"
                else -> "${hour - 12} PM"
            }
        }

    val mostCommonDay = times
        .groupingBy { it.dayOfWeek }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
        ?.toString()
        ?.lowercase()
        ?.replaceFirstChar { it.uppercase() }

    val today = LocalDate.now()
    val thisWeekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
    val lastWeekStart = thisWeekStart.minusWeeks(1)

    val thisWeekCount = times.count { it.toLocalDate() >= thisWeekStart }
    val lastWeekCount = times.count {
        it.toLocalDate() >= lastWeekStart && it.toLocalDate() < thisWeekStart
    }

    val weekComparison =
        if (thisWeekCount + lastWeekCount >= 2) {
            when {
                thisWeekCount < lastWeekCount -> "$thisWeekCount ↓ from $lastWeekCount"
                thisWeekCount > lastWeekCount -> "$thisWeekCount ↑ from $lastWeekCount"
                else -> "$thisWeekCount same as last week"
            }
        } else null

    val avg = StreakCalculator.averageStreak(slips)
    val averageStreak = if (avg > 0) "$avg days" else null

    val topTrigger = slips
        .map { it.trigger?.trim().orEmpty() }
        .filter { it.isNotEmpty() }
        .ifEmpty { listOf(UNKNOWN_TRIGGER) }
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key

    val suggestedAction = buildSuggestion(mostCommonHour, topTrigger)

    return InsightsData(
        mostCommonHour = mostCommonHour,
        mostCommonDay = mostCommonDay,
        weekComparison = weekComparison,
        averageStreak = averageStreak,
        topTrigger = topTrigger,
        suggestedAction = suggestedAction
    )
}

fun computeWeeklyReport(allEvents: List<SlipEvent>): WeeklyReport {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now()
    val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)

    val eventsThisWeek = allEvents.filter {
        Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() >= weekStart
    }

    val slipsThisWeek = eventsThisWeek.count { !it.isResist }
    val victoriesThisWeek = eventsThisWeek.count { it.isResist }

    val slipDates = eventsThisWeek
        .filter { !it.isResist }
        .map { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
        .toSet()

    val cleanDaysThisWeek = (0..today.dayOfWeek.value - 1)
        .map { weekStart.plusDays(it.toLong()) }
        .count { it !in slipDates }

    return WeeklyReport(
        slipsThisWeek = slipsThisWeek,
        victoriesThisWeek = victoriesThisWeek,
        cleanDaysThisWeek = cleanDaysThisWeek
    )
}

private fun buildSuggestion(mostCommonHour: String?, topTrigger: String?): String? {
    val hourPlan = mostCommonHour?.let { "Set a 15-minute buffer routine before $it (walk, shower, journal)." }

    val triggerPlan = when (topTrigger) {
        null -> null
        UNKNOWN_TRIGGER -> "Add trigger tags when logging slips to unlock smarter insights."
        "Stress" -> "High stress is a pattern. Try a 4-7-8 breathing reset when urges spike."
        "Boredom" -> "Boredom spikes detected. Keep a quick replacement list ready (pushups, walk, call)."
        "Loneliness" -> "Loneliness is a key trigger. Schedule one social check-in daily this week."
        "Social media" -> "Social media is a trigger. Add a night-time app limit and mute risky feeds."
        else -> "Top trigger: $topTrigger. Create a short pre-commit plan for that situation."
    }

    return listOfNotNull(triggerPlan, hourPlan).joinToString(" ").ifBlank { null }
}
