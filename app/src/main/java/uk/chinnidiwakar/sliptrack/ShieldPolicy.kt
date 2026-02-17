package uk.chinnidiwakar.sliptrack

object ShieldPolicy {
    const val RESISTS_PER_SHIELD = 10
    val shieldMilestones = listOf(7, 14, 30, 60, 90, 180, 365)

    fun shieldsFromResists(totalResists: Int): Int {
        if (totalResists <= 0) return 0
        return totalResists / RESISTS_PER_SHIELD
    }

    fun countNewMilestoneShields(previousMilestoneAward: Int, currentStreakDays: Int): Int {
        return shieldMilestones.count { it > previousMilestoneAward && it <= currentStreakDays }
    }

    fun highestReachedMilestone(currentStreakDays: Int): Int {
        return shieldMilestones.lastOrNull { it <= currentStreakDays } ?: 0
    }
}
