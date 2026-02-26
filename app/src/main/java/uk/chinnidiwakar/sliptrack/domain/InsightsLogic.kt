package uk.chinnidiwakar.sliptrack.domain

import uk.chinnidiwakar.sliptrack.SlipEvent
import uk.chinnidiwakar.sliptrack.StreakCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private const val UNKNOWN_TRIGGER = "Unspecified"

data class RiskAssessment(
    val score: Int,
    val level: RiskLevel
)

enum class RiskLevel {
    STABLE,
    CAUTION,
    HIGH
}

data class InsightsData(
    val mostCommonHour: String?,
    val mostCommonDay: String?,
    val weekComparison: String?,
    val averageStreak: String?,
    val currentStreak: String?,
    val recentSlipRate: String?,
    val hardestWindow: String?,
    val topTrigger: String?,
    val suggestedAction: String?,
    val willpowerScore: Int,
    val riskAssessment: RiskAssessment
)

data class WeeklyReport(
    val slipsThisWeek: Int,
    val victoriesThisWeek: Int,
    val cleanDaysThisWeek: Int
)

fun calculateRiskIndex(
    currentStreak: Int,
    slipsLast7Days: Int,
    weekDelta: Int
): RiskAssessment {

    val streakPenalty = when {
        currentStreak >= 14 -> 0
        currentStreak >= 7 -> 10
        currentStreak >= 3 -> 25
        else -> 40
    }

    val velocityScore = (slipsLast7Days * 5).coerceAtMost(35)

    val trendScore = when {
        weekDelta > 0 -> 25
        weekDelta < 0 -> -10
        else -> 0
    }

    val rawScore = (streakPenalty + velocityScore + trendScore)
        .coerceIn(0, 100)

    val level = when {
        rawScore <= 33 -> RiskLevel.STABLE
        rawScore <= 66 -> RiskLevel.CAUTION
        else -> RiskLevel.HIGH
    }

    return RiskAssessment(rawScore, level)
}

fun calculateWillpower(events: List<SlipEvent>): Int {
    val totalUrges = events.size
    if (totalUrges == 0) return 100
    val resists = events.count { it.isResist }
    return ((resists.toFloat() / totalUrges.toFloat()) * 100).toInt()
}

fun computeInsights(events: List<SlipEvent>): InsightsData? {
    val slips = events.filter { !it.isResist }
    if (slips.size < 3) return null

    val zone = ZoneId.systemDefault()
    val times = slips.map {
        Instant.ofEpochMilli(normalizeTimestamp(it.timestamp)).atZone(zone)
    }
    val riskAssessment: RiskAssessment

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
    val currentStreak = "${StreakCalculator.currentStreak(slips)} days"

    val recentSlipRate = recentSlipRate(events)
    val slipsLast7Days = events
        .filter { !it.isResist }
        .count {
            Instant.ofEpochMilli(normalizeTimestamp(it.timestamp))
                .atZone(ZoneId.systemDefault())
                .toLocalDate() >= LocalDate.now().minusDays(6)
        }

    val weekDelta = thisWeekCount - lastWeekCount

    val currentStreakValue = StreakCalculator.currentStreak(slips)

    val risk = calculateRiskIndex(
        currentStreak = currentStreakValue,
        slipsLast7Days = slipsLast7Days,
        weekDelta = weekDelta
    )

    val hardestWindow = times
        .groupingBy { (it.hour / 6) * 6 }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
        ?.let { startHour ->
            val endHour = (startHour + 6) % 24
            "${formatHour(startHour)}-${formatHour(endHour)}"
        }

    val topTrigger = slips
        .map { it.trigger?.trim().orEmpty() }
        .filter { it.isNotEmpty() }
        .ifEmpty { listOf(UNKNOWN_TRIGGER) }
        .groupingBy { it.lowercase() }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
        ?.replaceFirstChar { it.uppercase() }

    val suggestedAction = buildSuggestion(mostCommonHour, topTrigger, hardestWindow)

    return InsightsData(
        mostCommonHour = mostCommonHour,
        mostCommonDay = mostCommonDay,
        weekComparison = weekComparison,
        averageStreak = averageStreak,
        currentStreak = currentStreak,
        recentSlipRate = recentSlipRate,
        hardestWindow = hardestWindow,
        topTrigger = topTrigger,
        suggestedAction = suggestedAction,
        willpowerScore = calculateWillpower(events),
        riskAssessment = risk
    )
}

fun computeWeeklyReport(allEvents: List<SlipEvent>): WeeklyReport {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now()
    val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)

    val eventsThisWeek = allEvents.filter {
        Instant.ofEpochMilli(normalizeTimestamp(it.timestamp)).atZone(zone).toLocalDate() >= weekStart
    }

    val slipsThisWeek = eventsThisWeek.count { !it.isResist }
    val victoriesThisWeek = eventsThisWeek.count { it.isResist }

    val slipDates = eventsThisWeek
        .filter { !it.isResist }
        .map { Instant.ofEpochMilli(normalizeTimestamp(it.timestamp)).atZone(zone).toLocalDate() }
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

fun normalizeTimestamp(raw: Long): Long {
    return if (raw < 1_000_000_000_000L) raw * 1000 else raw
}

fun recentSlipRate(events: List<SlipEvent>): String {
    val today = LocalDate.now()
    val windowStart = today.minusDays(6)

    val slipsInWindow = events
        .filter { !it.isResist }
        .count {
            Instant.ofEpochMilli(normalizeTimestamp(it.timestamp))
                .atZone(ZoneId.systemDefault())
                .toLocalDate() >= windowStart
        }

    val averagePerDay = slipsInWindow / 7.0
    return "${"%.2f".format(averagePerDay)} slips/day (7d)"
}

private fun formatHour(hour24: Int): String {
    return when {
        hour24 == 0 -> "12am"
        hour24 < 12 -> "${hour24}am"
        hour24 == 12 -> "12pm"
        else -> "${hour24 - 12}pm"
    }
}

private fun buildSuggestion(mostCommonHour: String?, topTrigger: String?, hardestWindow: String?): String? {
    val hourPlan = mostCommonHour?.let { "Set a 15-minute buffer routine before $it (walk, shower, journal)." }
    val windowPlan = hardestWindow?.let { "Your highest-risk window is $it; pre-plan one healthy distraction there." }

    val triggerPlan = when (topTrigger) {
        null -> null
        UNKNOWN_TRIGGER -> "Add trigger tags when logging slips to unlock smarter insights."
        "Stress" -> "High stress is a pattern. Try a 4-7-8 breathing reset when urges spike."
        "Boredom" -> "Boredom spikes detected. Keep a quick replacement list ready (pushups, walk, call)."
        "Loneliness" -> "Loneliness is a key trigger. Schedule one social check-in daily this week."
        "Social Media" -> "Social media is a trigger. Add a night-time app limit and mute risky feeds."
        else -> "Top trigger: $topTrigger. Create a short pre-commit plan for that situation."
    }

    return listOfNotNull(triggerPlan, windowPlan, hourPlan).joinToString(" ").ifBlank { null }
}
