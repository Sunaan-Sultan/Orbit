package com.orbit.starsystems

import com.orbit.starsystems.core.Perks
import com.orbit.starsystems.core.Streak
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Streak repair is the one place the app asks for an ad in exchange for something the user
 * cares about, so the rules that decide when to ask are pinned here rather than left implicit
 * in the UI. Each rule exists to stop the streak becoming something that can simply be bought.
 */
class PerksTest {

    private val today = 20_000L

    private fun offer(
        brokenStreak: Int = 10,
        brokenDay: Long = today,
        lastRepairDay: Long = Streak.NEVER,
        gapDays: Int = 1,
    ) = Perks.repairOffer(today, brokenStreak, brokenDay, lastRepairDay, gapDays)

    @Test
    fun `a one day break on a decent run is repairable`() {
        assertEquals(Perks.RepairOffer(10), offer())
    }

    /** Restoring a week away would be a lie; one day is the only genuinely unfair loss. */
    @Test
    fun `only a single missed day can be repaired`() {
        assertNull("nothing was missed", offer(gapDays = 0))
        assertNull("two days missed", offer(gapDays = 2))
        assertNull("a week missed", offer(gapDays = 7))
    }

    @Test
    fun `a run too short to be worth an ad is not offered`() {
        assertNull(offer(brokenStreak = 1))
        assertNull(offer(brokenStreak = Perks.MIN_REPAIRABLE_STREAK - 1))
        assertNotNull(offer(brokenStreak = Perks.MIN_REPAIRABLE_STREAK))
    }

    /** The offer is for the day the loss is noticed. Tomorrow it is gone. */
    @Test
    fun `the offer expires at local midnight`() {
        assertNull(offer(brokenDay = today - 1))
        assertNull(offer(brokenDay = today - 30))
    }

    @Test
    fun `a repair inside the cooldown is not offered`() {
        assertNull(offer(lastRepairDay = today))
        assertNull(offer(lastRepairDay = today - (Perks.REPAIR_COOLDOWN_DAYS - 1)))
        assertNotNull(offer(lastRepairDay = today - Perks.REPAIR_COOLDOWN_DAYS))
    }

    /** A user who has never repaired must not be read as having repaired at the epoch. */
    @Test
    fun `never having repaired does not block the first offer`() {
        assertNotNull(offer(lastRepairDay = Streak.NEVER))
    }

    @Test
    fun `nothing is offered to a user who has never had a run`() {
        assertNull(offer(brokenStreak = 0, gapDays = 0))
    }
}
