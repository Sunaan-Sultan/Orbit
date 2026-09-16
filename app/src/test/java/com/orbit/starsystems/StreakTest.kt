package com.orbit.starsystems

import com.orbit.starsystems.core.Streak
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The streak is the reward the whole daily loop pays out, so the arithmetic behind it is held
 * to the cases that actually happen to a phone: the same day twice, a clock dragged backwards
 * by a time zone, a month away, and an upgrade from a build that stored less than this one.
 */
class StreakTest {

    private val day = 20_000L

    // ───────────────────────── recording ─────────────────────────

    @Test
    fun `a first ever day starts a run of one`() {
        val s = Streak.EMPTY.record(day)
        assertEquals(1, s.current)
        assertEquals(1, s.longest)
        assertEquals(day, s.lastDay)
        assertTrue(s.isActiveOn(day))
    }

    /** `recordActivity` is called on every fact opened, so the second call must change nothing. */
    @Test
    fun `the same day twice changes nothing`() {
        val once = Streak.EMPTY.record(day)
        assertSame(once, once.record(day))
    }

    @Test
    fun `yesterday extends the run`() {
        val s = Streak.EMPTY.record(day).record(day + 1).record(day + 2)
        assertEquals(3, s.current)
        assertEquals(listOf(false, false, false, false, true, true, true), s.week(day + 2))
    }

    @Test
    fun `a two day gap resets to one`() {
        val s = Streak.EMPTY.record(day).record(day + 1).record(day + 3)
        assertEquals(1, s.current)
        assertEquals(2, s.longest)
        assertFalse("the missed day must stay unlit", s.isActiveOn(day + 2))
    }

    /** A clock wound back must not be able to farm days, and a westbound flight must not lose a run. */
    @Test
    fun `a day in the past is ignored`() {
        val s = Streak.EMPTY.record(day).record(day + 1)
        assertSame(s, s.record(day))
        assertSame(s, s.record(day - 40))
    }

    @Test
    fun `longest never falls`() {
        val s = Streak.EMPTY.record(day).record(day + 1).record(day + 2).record(day + 10)
        assertEquals(1, s.current)
        assertEquals(3, s.longest)
    }

    @Test
    fun `longest updates the moment the run passes it`() {
        var s = Streak.EMPTY.record(day).record(day + 1)   // best 2
        s = s.record(day + 5).record(day + 6).record(day + 7)
        assertEquals(3, s.current)
        assertEquals(3, s.longest)
    }

    // ───────────────────────── the week ─────────────────────────

    @Test
    fun `the week reads oldest first with today last`() {
        val s = Streak.EMPTY.record(day - 1).record(day)
        val week = s.week(day)
        assertEquals(7, week.size)
        assertTrue("today is last", week.last())
        assertTrue("yesterday is second to last", week[5])
        assertFalse("six days ago was not active", week.first())
    }

    /** Reading a later day must not need a write — the anchor simply sits further down the mask. */
    @Test
    fun `an inactive stretch leaves the recent days unlit without any write`() {
        val s = Streak.EMPTY.record(day)
        val week = s.week(day + 3)
        assertEquals(listOf(false, false, false, true, false, false, false), week)
        assertEquals("reading must not mutate", day, s.lastDay)
    }

    @Test
    fun `a seven day run lights all seven dots`() {
        var s = Streak.EMPTY
        (0..6).forEach { s = s.record(day + it) }
        assertEquals(List(7) { true }, s.week(day + 6))
    }

    @Test
    fun `a day in the future is never active`() {
        val s = Streak.EMPTY.record(day)
        assertFalse(s.isActiveOn(day + 1))
    }

    // ───────────────────────── the shift guard ─────────────────────────

    /**
     * `shl` uses only the low five bits of its count, so `mask shl 32 == mask`. Without the
     * explicit guard a user returning after exactly 32 days is shown a week they never had.
     */
    @Test
    fun `history shifted by exactly thirty-two days is empty`() {
        assertEquals(0, Streak.shift(-1, Streak.HISTORY_DAYS.toLong()))
    }

    @Test
    fun `history shifted by thirty-three and by a hundred days is empty`() {
        assertEquals(0, Streak.shift(-1, 33L))
        assertEquals(0, Streak.shift(-1, 100L))
    }

    @Test
    fun `a month away leaves no phantom activity`() {
        var s = Streak.EMPTY
        (0..6).forEach { s = s.record(day + it) }
        val back = s.record(day + 6 + Streak.HISTORY_DAYS)
        assertEquals(listOf(false, false, false, false, false, false, true), back.week(back.lastDay))
    }

    @Test
    fun `history is never shifted backwards`() {
        assertEquals(0b1011, Streak.shift(0b1011, 0L))
        assertEquals(0b1011, Streak.shift(0b1011, -5L))
    }

    // ───────────────────────── migration ─────────────────────────

    @Test
    fun `seeding from an existing streak reconstructs the week`() {
        val s = Streak.seed(current = 5, lastDay = day)
        assertEquals(5, s.current)
        assertEquals(5, s.longest)
        assertEquals(listOf(false, false, true, true, true, true, true), s.week(day))
    }

    @Test
    fun `seeding from a run longer than the history fills it`() {
        val s = Streak.seed(current = 400, lastDay = day)
        assertEquals(400, s.current)
        assertEquals(List(7) { true }, s.week(day))
        assertFalse(s.isActiveOn(day - Streak.HISTORY_DAYS.toLong()))
    }

    @Test
    fun `seeding a fresh install yields nothing`() {
        assertEquals(Streak.EMPTY, Streak.seed(current = 1, lastDay = Streak.NEVER))
        assertEquals(Streak.EMPTY, Streak.seed(current = 0, lastDay = day))
    }

    @Test
    fun `recording after a seeded migration extends rather than resets`() {
        val s = Streak.seed(current = 5, lastDay = day).record(day + 1)
        assertEquals(6, s.current)
        assertEquals(6, s.longest)
        // Six days of run in a seven-day window: the oldest dot is still dark.
        assertEquals(listOf(false) + List(6) { true }, s.week(day + 1))
    }

    // ───────────────────────── breaks and repair ─────────────────────────

    @Test
    fun `a break is seen only after a day was actually missed`() {
        val s = Streak.EMPTY.record(day)
        assertFalse(s.breaksOn(day))
        assertFalse(s.breaksOn(day + 1))
        assertTrue(s.breaksOn(day + 2))
        assertEquals(0, s.gapOn(day + 1))
        assertEquals(1, s.gapOn(day + 2))
        assertEquals(4, s.gapOn(day + 5))
    }

    @Test
    fun `a first ever day never counts as a break`() {
        assertFalse(Streak.EMPTY.breaksOn(day))
        assertEquals(0, Streak.EMPTY.gapOn(day))
    }

    /** Repair takes the run that was lost, adds today, and closes the one-day hole between them. */
    @Test
    fun `repairing a one day break restores the run and lights the missed day`() {
        var s = Streak.EMPTY
        (0..4).forEach { s = s.record(day + it) }      // a run of 5, ending day+4
        val broken = s.record(day + 6)                  // day+5 missed
        assertEquals(1, broken.current)

        val fixed = broken.repaired(lost = 5, today = day + 6)
        assertEquals(6, fixed.current)
        assertEquals(6, fixed.longest)
        assertTrue("the missed day now counts", fixed.isActiveOn(day + 5))
        assertEquals(List(7) { true }, fixed.week(day + 6))
    }

    @Test
    fun `a repaired run keeps extending the next day`() {
        val broken = Streak.EMPTY.record(day).record(day + 2)
        val fixed = broken.repaired(lost = 1, today = day + 2).record(day + 3)
        assertEquals(3, fixed.current)
    }

    @Test
    fun `repair cannot invent a day it was not asked about`() {
        val s = Streak.EMPTY.record(day)
        assertSame("today is not the recorded day", s, s.repaired(lost = 5, today = day + 9))
        assertSame("nothing was lost", s, s.repaired(lost = 0, today = day))
    }
}
