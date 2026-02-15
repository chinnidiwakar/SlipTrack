package uk.chinnidiwakar.sliptrack.domain

data class ShieldState(
    val charges: Int,
    val highestMilestoneAwarded: Int
)

object ShieldPolicy {
    val milestones = listOf(3, 7, 14, 30, 60, 90, 120, 180, 365)

    fun awardForStreak(streak: Int, current: ShieldState): ShieldState {
        val nextMilestone = milestones.firstOrNull { it > current.highestMilestoneAwarded && streak >= it }
            ?: return current
        return current.copy(
            charges = current.charges + 1,
            highestMilestoneAwarded = nextMilestone
        )
    }
}
