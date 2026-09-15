package com.orbit.starsystems

import com.orbit.starsystems.core.ALL_FACTS
import com.orbit.starsystems.core.OrbitData
import com.orbit.starsystems.core.Quiz
import com.orbit.starsystems.core.QuizKind
import com.orbit.starsystems.core.QuizQuestion
import com.orbit.starsystems.core.factById
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.random.Random

/**
 * The quiz invents its wrong answers, so the things that make a question bad are properties of
 * generated text rather than of any one fact: an option that repeats, an answer that is also a
 * distractor, a number that is given away by the title it sits under. None of that is visible
 * by reading the generator — it only shows up across the whole catalog, which is what this
 * checks, over many seeds.
 */
class QuizTest {

    @Before
    fun loadCatalog() {
        OrbitData.load { path -> File("src/main/assets/$path").readText() }
    }

    private fun allQuestions(seeds: IntRange = 0..60): List<QuizQuestion> =
        seeds.flatMap { Quiz.round(Quiz.ROUND_SIZE, Random(it)) }

    @Test
    fun `catalog loads for the quiz`() {
        assertEquals(131, ALL_FACTS.size)
    }

    @Test
    fun `every round is full length and never repeats a fact`() {
        (0..60).forEach { seed ->
            val round = Quiz.round(Quiz.ROUND_SIZE, Random(seed))
            assertEquals("seed $seed: short round", Quiz.ROUND_SIZE, round.size)
            val ids = round.map { it.factId }
            assertEquals("seed $seed: repeated a fact", ids.size, ids.distinct().size)
        }
    }

    @Test
    fun `every question has four distinct options and a valid answer`() {
        allQuestions().forEach { q ->
            assertEquals("${q.prompt}: not four options — ${q.options}", 4, q.options.size)
            assertEquals("${q.prompt}: duplicate options — ${q.options}", 4, q.options.distinct().size)
            assertTrue("${q.prompt}: answer index out of range", q.answerIndex in q.options.indices)
            q.options.forEach { assertTrue("${q.prompt}: blank option", it.isNotBlank()) }
        }
    }

    /** The whole point of a distractor is that it is wrong; an equal value is not. */
    @Test
    fun `no distractor equals the answer after formatting`() {
        allQuestions().forEach { q ->
            val others = q.options.filterIndexed { i, _ -> i != q.answerIndex }
            assertTrue("${q.prompt}: distractor equals answer — ${q.options}", q.answer !in others)
        }
    }

    /**
     * A generated number must be written the way the authored one is. If the answer is the only
     * option with a comma, a degree sign or a decimal point, it can be picked without knowing
     * anything. Only quantities are comparable this way — the other kinds offer titles, which
     * differ in shape by nature.
     */
    @Test
    fun `quantity options share the shape of the answer`() {
        allQuestions().filter { it.kind == QuizKind.QUANTITY }.forEach { q ->
            val shapes = q.options.map { option -> option.filter { !it.isDigit() } }
            assertEquals(
                "${q.subject} / ${q.prompt}: options differ in format — ${q.options}",
                1,
                shapes.distinct().size,
            )
        }
    }

    /** An option that rounds away to zero is a non-answer nobody would pick. */
    @Test
    fun `no option is a zero`() {
        allQuestions().filter { it.kind == QuizKind.QUANTITY }.forEach { q ->
            q.options.forEach { option ->
                assertTrue(
                    "${q.subject} / ${q.prompt}: zero option in ${q.options}",
                    option.any { it in '1'..'9' },
                )
            }
        }
    }

    /** Years are positions on a scale, not magnitudes — scaling them produces nonsense. */
    @Test
    fun `year options stay in a believable range`() {
        allQuestions().filter { it.kind == QuizKind.QUANTITY }.forEach { q ->
            val years = q.options.mapNotNull { Regex("""\b(\d{4})\b""").find(it)?.groupValues?.get(1)?.toInt() }
            if (years.size == q.options.size && years.any { it in 1500..2100 }) {
                assertTrue(
                    "${q.subject} / ${q.prompt}: implausible year in ${q.options}",
                    years.all { it in 1400..2200 },
                )
            }
        }
    }

    /** The subject line must not contain the answer. */
    @Test
    fun `no question gives its answer away in the prompt`() {
        allQuestions().forEach { q ->
            val shown = "${q.subject.orEmpty()} ${q.prompt}".lowercase()
            val answer = q.answer.lowercase().trim()
            assertTrue("${q.subject} / ${q.prompt}: answer visible in prompt", answer !in shown)
        }
    }

    /** Solar-system facts make system questions that answer themselves. */
    @Test
    fun `system questions never ask about our own system`() {
        allQuestions().filter { it.kind == QuizKind.SYSTEM }.forEach { q ->
            assertTrue("${q.subject}: Sol asked as a system question", factById(q.factId)?.sys != "sol")
        }
    }

    /** Every question has to be traceable back to a real fact, for the end-of-round review. */
    @Test
    fun `every question points at a fact that exists`() {
        allQuestions().forEach { q ->
            assertTrue("unknown fact id ${q.factId}", factById(q.factId) != null)
        }
    }

    @Test
    fun `a seed always produces the same round`() {
        val a = Quiz.round(Quiz.ROUND_SIZE, Random(99))
        val b = Quiz.round(Quiz.ROUND_SIZE, Random(99))
        assertEquals(a, b)
    }

    /** Rounds must not all be the same question type, or the mix logic has silently broken. */
    @Test
    fun `a round mixes question types`() {
        (0..20).forEach { seed ->
            val prompts = Quiz.round(Quiz.ROUND_SIZE, Random(seed)).map { it.prompt }
            assertTrue("seed $seed: only one kind of question", prompts.distinct().size >= 3)
        }
    }
}
