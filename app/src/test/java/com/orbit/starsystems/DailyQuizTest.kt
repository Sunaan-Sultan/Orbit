package com.orbit.starsystems

import com.orbit.starsystems.core.DailyQuiz
import com.orbit.starsystems.core.OrbitData
import com.orbit.starsystems.core.Quiz
import com.orbit.starsystems.core.factById
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * The daily round is the same ten questions for everyone, worked out from the date alone with
 * nothing stored. That only holds if the derivation is stable, so these pin it down — including
 * one test that is *supposed* to fail when the question bank is edited.
 */
class DailyQuizTest {

    @Before
    fun loadCatalog() {
        OrbitData.load { path -> File("src/main/assets/$path").readText() }
    }

    @Test
    fun `a day always produces the same round`() {
        assertEquals(DailyQuiz.roundForDay(20_100), DailyQuiz.roundForDay(20_100))
    }

    @Test
    fun `consecutive days produce different rounds`() {
        val a = DailyQuiz.roundForDay(20_100).map { it.prompt }
        val b = DailyQuiz.roundForDay(20_101).map { it.prompt }
        assertNotEquals(a, b)
    }

    @Test
    fun `every day of a year produces a full round with no repeated fact`() {
        (20_000L until 20_365L).forEach { day ->
            val round = DailyQuiz.roundForDay(day)
            assertEquals("day $day: short round", Quiz.ROUND_SIZE, round.size)
            val ids = round.map { it.factId }
            assertEquals("day $day: repeated a fact", ids.size, ids.distinct().size)
        }
    }

    /** A year of dailies must not grind the same corner of the bank. */
    @Test
    fun `a year of daily rounds covers a wide slice of the bank`() {
        val seen = (20_000L until 20_365L)
            .flatMap { DailyQuiz.roundForDay(it) }
            .mapTo(HashSet()) { it.prompt }
        assertTrue("only ${seen.size} of ${Quiz.bank.size} questions used", seen.size >= 150)
    }

    @Test
    fun `a year of daily rounds spreads across systems`() {
        (20_000L until 20_365L).forEach { day ->
            val systems = DailyQuiz.roundForDay(day).mapNotNull { factById(it.factId)?.sys }.distinct()
            assertTrue("day $day: only ${systems.size} system(s)", systems.size >= 5)
        }
    }

    /** A device clock set before 1970 gives a negative day index; it must still play. */
    @Test
    fun `a negative day index still produces a round`() {
        assertEquals(Quiz.ROUND_SIZE, DailyQuiz.roundForDay(-500).size)
    }

    /** The salt exists so the daily does not track anything else that seeds off a day index. */
    @Test
    fun `the daily round is salted away from the raw day index`() {
        val day = 20_100L
        val unsalted = Quiz.round(Quiz.ROUND_SIZE, kotlin.random.Random(day)).map { it.prompt }
        assertNotEquals(unsalted, DailyQuiz.roundForDay(day).map { it.prompt })
    }

    /**
     * The canary. The daily round is derived from the live bank, so adding, removing or
     * reordering a question in `assets/quiz.json` changes every future daily round.
     *
     * That is acceptable — a user who has already played is unaffected, because the completion
     * record is a day and a score and never references a question — but it should be a decision
     * rather than a surprise. If this fails, confirm the bank change was intended and update the
     * expected list.
     */
    @Test
    fun `the daily round for a pinned day is unchanged`() {
        val actual = DailyQuiz.roundForDay(20_100).map { it.prompt }
        assertEquals(
            "The question bank changed, so every daily round changed with it. " +
                "Confirm that was intended, then update this list.",
            PINNED_DAY_20100,
            actual,
        )
    }

    private companion object {
        val PINNED_DAY_20100 = listOf(
            "What lies beneath Europa's cracked shell of ice?",
            "How long do Sirius A and B take to orbit each other?",
            "How does Kepler-90 compare with the Sun in size?",
            "In which constellation does Wolf 359 lie?",
            "Why might plants on Kepler-186f have evolved darker colours?",
            "Pluto was discovered in 1930. How many orbits has it completed since?",
            "How long does K2-18 b take to orbit its star?",
            "In which constellation does 55 Cancri lie?",
            "How far out does TRAPPIST-1's outermost planet orbit?",
            "Which system is the only one closer to us than Barnard's Star?",
        )
    }
}
