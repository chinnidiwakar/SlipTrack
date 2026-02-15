package uk.chinnidiwakar.sliptrack.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ShieldPolicyTest {

    @Test
    fun awardsSingleShieldAtNextMilestone() {
        val updated = ShieldPolicy.awardForStreak(
            streak = 8,
            current = ShieldState(charges = 0, highestMilestoneAwarded = 3)
        )

        assertEquals(1, updated.charges)
        assertEquals(7, updated.highestMilestoneAwarded)
    }

    @Test
    fun doesNotAwardTwiceForSameMilestoneBand() {
        val unchanged = ShieldPolicy.awardForStreak(
            streak = 10,
            current = ShieldState(charges = 2, highestMilestoneAwarded = 7)
        )

        assertEquals(2, unchanged.charges)
        assertEquals(7, unchanged.highestMilestoneAwarded)
    }
}
