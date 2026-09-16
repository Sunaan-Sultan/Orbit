package com.orbit.starsystems

import com.orbit.starsystems.core.OrbitData
import com.orbit.starsystems.core.Quiz
import com.orbit.starsystems.core.QuizQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.random.Random

/** The 50/50 is paid for with an ad, so it must never strike out the answer it was bought for. */
class QuizHintTest {

    @Before
    fun loadCatalog() {
        OrbitData.load { path -> File("src/main/assets/$path").readText() }
    }

    @Test
    fun `the hint never hides the answer`() {
        (0..40).forEach { seed ->
            Quiz.round(Quiz.ROUND_SIZE, Random(seed)).forEach { q ->
                val hidden = Quiz.fiftyFiftyHidden(q, Random(seed))
                assertFalse("${q.prompt}: hid the answer", q.answerIndex in hidden)
            }
        }
    }

    @Test
    fun `the hint strikes out exactly two of four options`() {
        (0..40).forEach { seed ->
            Quiz.round(Quiz.ROUND_SIZE, Random(seed)).forEach { q ->
                assertEquals("${q.prompt}: wrong number struck", 2, Quiz.fiftyFiftyHidden(q).size)
            }
        }
    }

    @Test
    fun `the hint leaves the answer and one distractor standing`() {
        val q = Quiz.round(Quiz.ROUND_SIZE, Random(3)).first()
        val standing = q.options.indices - Quiz.fiftyFiftyHidden(q)
        assertEquals(2, standing.size)
        assertTrue(q.answerIndex in standing)
    }

    /** A question with fewer options than usual must degrade rather than throw. */
    @Test
    fun `a short question degrades instead of failing`() {
        val two = QuizQuestion("sun", "How wide?", listOf("A", "B"), answerIndex = 0)
        assertEquals(setOf(1), Quiz.fiftyFiftyHidden(two))
        val one = QuizQuestion("sun", "How wide?", listOf("A"), answerIndex = 0)
        assertTrue(Quiz.fiftyFiftyHidden(one).isEmpty())
    }

    @Test
    fun `a seeded hint is stable`() {
        val q = Quiz.round(Quiz.ROUND_SIZE, Random(9)).first()
        assertEquals(Quiz.fiftyFiftyHidden(q, Random(7)), Quiz.fiftyFiftyHidden(q, Random(7)))
    }
}
