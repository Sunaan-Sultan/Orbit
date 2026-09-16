package com.orbit.starsystems

import com.orbit.starsystems.core.Streak
import com.orbit.starsystems.notify.DailyReminder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * The reminder is the only thing that runs while the app is closed and the only way a lapsing
 * streak can be rescued, so both halves of its decision — when to fire, and what to say — are
 * pinned here. `millisUntilNextRun` has been `internal` and testable since it was written; this
 * is the first thing to test it.
 */
class DailyReminderTest {

    private val today = 20_000L

    // ───────────────────────── what to say ─────────────────────────

    private fun nudge(
        streak: Int = 5,
        quizDoneToday: Boolean = false,
        lastActiveDay: Long = today - 1,
    ) = DailyReminder.nudgeFor(streak, quizDoneToday, lastActiveDay, today)

    @Test
    fun `a live streak with today still open calls for the quiz`() {
        assertEquals(DailyReminder.Nudge.QUIZ, nudge())
    }

    @Test
    fun `a finished daily goes back to the fact`() {
        assertEquals(DailyReminder.Nudge.FACT, nudge(quizDoneToday = true))
    }

    /** Naming a one-day "streak" is noise; a new user gets the fact instead. */
    @Test
    fun `a streak too short to be worth naming goes back to the fact`() {
        assertEquals(DailyReminder.Nudge.FACT, nudge(streak = 1))
        assertEquals(DailyReminder.Nudge.QUIZ, nudge(streak = DailyReminder.MIN_STREAK_FOR_QUIZ_NUDGE))
    }

    /** A number left over from last week is not a streak at risk. */
    @Test
    fun `a stale streak goes back to the fact`() {
        assertEquals(DailyReminder.Nudge.FACT, nudge(lastActiveDay = today - 2))
        assertEquals(DailyReminder.Nudge.FACT, nudge(lastActiveDay = today - 30))
        assertEquals(DailyReminder.Nudge.FACT, nudge(lastActiveDay = Streak.NEVER))
    }

    /** Nothing is at risk if the user has already been in today. */
    @Test
    fun `an already active day goes back to the fact`() {
        assertEquals(DailyReminder.Nudge.FACT, nudge(lastActiveDay = today))
    }

    @Test
    fun `a fresh install is never told about a streak`() {
        assertEquals(
            DailyReminder.Nudge.FACT,
            DailyReminder.nudgeFor(1, quizDoneToday = false, lastActiveDay = Streak.NEVER, today = today),
        )
    }

    // ───────────────────────── when to fire ─────────────────────────

    private fun at(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, dayOfMonth, hour, minute, 0)
        }.timeInMillis

    private fun hourOf(millis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.HOUR_OF_DAY)

    @Test
    fun `an hour later today is later today`() {
        val now = at(2026, Calendar.MARCH, 10, 9, 0)
        val delay = DailyReminder.millisUntilNextRun(19, now)
        assertEquals(10 * 3_600_000L, delay)
        assertEquals(19, hourOf(now + delay))
    }

    @Test
    fun `an hour already past today rolls to tomorrow`() {
        val now = at(2026, Calendar.MARCH, 10, 21, 30)
        val delay = DailyReminder.millisUntilNextRun(19, now)
        assertTrue("must be in the future", delay > 0)
        assertTrue("must be within a day", delay <= 24 * 3_600_000L)
        assertEquals(19, hourOf(now + delay))
    }

    /** On the hour exactly, the run is already gone — it must book tomorrow, not fire instantly. */
    @Test
    fun `the hour itself rolls to tomorrow`() {
        val now = at(2026, Calendar.MARCH, 10, 19, 0)
        val delay = DailyReminder.millisUntilNextRun(19, now)
        assertTrue(delay > 0)
        assertEquals(19, hourOf(now + delay))
    }

    @Test
    fun `midnight and the last hour of the day both resolve`() {
        val now = at(2026, Calendar.MARCH, 10, 12, 0)
        listOf(0, 23).forEach { hour ->
            val delay = DailyReminder.millisUntilNextRun(hour, now)
            assertTrue("hour $hour: not in the future", delay > 0)
            assertEquals(hour, hourOf(now + delay))
        }
    }

    /** An hour out of range must be clamped rather than thrown away. */
    @Test
    fun `an impossible hour is clamped into the day`() {
        val now = at(2026, Calendar.MARCH, 10, 12, 0)
        assertTrue(DailyReminder.millisUntilNextRun(-5, now) > 0)
        assertTrue(DailyReminder.millisUntilNextRun(99, now) > 0)
    }

    /**
     * Spring forward: in most zones 02:00 does not exist on the changeover day. The reminder
     * must still land on a real instant rather than returning a negative delay.
     */
    @Test
    fun `a daylight saving jump still books a real time`() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
            val now = at(2026, Calendar.MARCH, 7, 23, 0)
            val delay = DailyReminder.millisUntilNextRun(2, now)
            assertTrue("must be in the future", delay > 0)
            assertTrue("must be within two days", delay <= 48 * 3_600_000L)
        } finally {
            TimeZone.setDefault(original)
        }
    }
}
