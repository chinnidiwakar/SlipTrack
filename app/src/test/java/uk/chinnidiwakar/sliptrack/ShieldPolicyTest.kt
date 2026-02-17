package uk.chinnidiwakar.sliptrack

import org.junit.Assert.assertEquals
import org.junit.Test

class ShieldPolicyTest {

    @Test
    fun shieldsFromResists_returnsEveryTen() {
        assertEquals(0, ShieldPolicy.shieldsFromResists(0))
        assertEquals(0, ShieldPolicy.shieldsFromResists(9))
        assertEquals(1, ShieldPolicy.shieldsFromResists(10))
        assertEquals(2, ShieldPolicy.shieldsFromResists(25))
    }

    @Test
    fun countNewMilestoneShields_countsOnlyNewCrossedMilestones() {
        assertEquals(0, ShieldPolicy.countNewMilestoneShields(previousMilestoneAward = 14, currentStreakDays = 14))
        assertEquals(1, ShieldPolicy.countNewMilestoneShields(previousMilestoneAward = 14, currentStreakDays = 30))
        assertEquals(3, ShieldPolicy.countNewMilestoneShields(previousMilestoneAward = 7, currentStreakDays = 90))
    }

    @Test
    fun highestReachedMilestone_returnsLatestThreshold() {
        assertEquals(0, ShieldPolicy.highestReachedMilestone(5))
        assertEquals(7, ShieldPolicy.highestReachedMilestone(7))
        assertEquals(30, ShieldPolicy.highestReachedMilestone(45))
    }
}
