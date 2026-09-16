package com.orbit.starsystems

import com.orbit.starsystems.core.ALL_FACTS
import com.orbit.starsystems.core.OrbitData
import com.orbit.starsystems.core.Quiz
import com.orbit.starsystems.core.QuizQuestion
import com.orbit.starsystems.core.factById
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.random.Random

/**
 * The quiz is authored now rather than generated, so these checks fall into two halves: the bank
 * itself has to be sound (every question answerable, every option distinct, every fact covered),
 * and rounds built from it have to stay varied and non-repeating.
 *
 * Several of these guard against the specific ways the old generator produced nonsense — an
 * answer repeated as its own distractor, an option that is a bare column heading, a question
 * asked about a yardstick rather than its subject — so that hand-written additions cannot
 * quietly reintroduce them.
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

    // ───────────────────────── the bank ─────────────────────────

    @Test
    fun `every question points at a fact that ships`() {
        assertTrue("bank is empty", Quiz.bank.isNotEmpty())
        Quiz.bank.forEach { entry ->
            assertTrue("unknown fact id ${entry.factId}", factById(entry.factId) != null)
        }
    }

    /** A fact with no question can never be revisited from a missed answer. */
    @Test
    fun `every fact has at least one question`() {
        val covered = Quiz.bank.mapTo(HashSet()) { it.factId }
        val missing = ALL_FACTS.filter { it.id !in covered }.map { it.id }
        assertTrue("facts with no question: $missing", missing.isEmpty())
    }

    /** A round needs one question per fact, so the bank must hold at least a round's worth. */
    @Test
    fun `the bank is comfortably larger than a round`() {
        assertTrue("bank too small: ${Quiz.bank.size}", Quiz.bank.size >= Quiz.ROUND_SIZE * 4)
    }

    @Test
    fun `every entry offers four distinct options`() {
        Quiz.bank.forEach { entry ->
            val options = listOf(entry.answer) + entry.wrong
            assertEquals("${entry.prompt}: not three distractors — ${entry.wrong}", 3, entry.wrong.size)
            assertEquals("${entry.prompt}: duplicate options — $options", 4, options.distinct().size)
            options.forEach { assertTrue("${entry.prompt}: blank option", it.isNotBlank()) }
        }
    }

    /** A prompt has to read as a question on its own, not as a column heading like "Undone?". */
    @Test
    fun `every prompt is a written-out question`() {
        Quiz.bank.forEach { entry ->
            assertTrue("not a question: ${entry.prompt}", entry.prompt.trim().endsWith("?"))
            assertTrue(
                "prompt too short to stand alone: ${entry.prompt}",
                entry.prompt.trim().split(" ").size >= 4,
            )
        }
    }

    /** Two identical prompts are one question asked twice, whatever facts they hang off. */
    @Test
    fun `no prompt appears twice`() {
        val repeated = Quiz.bank.groupBy { it.prompt }.filterValues { it.size > 1 }.keys
        assertTrue("repeated prompts: $repeated", repeated.isEmpty())
    }

    /** The question must not contain its own answer. */
    @Test
    fun `no question gives its answer away in the prompt`() {
        Quiz.bank.forEach { entry ->
            val answer = entry.answer.lowercase().trim()
            assertTrue(
                "${entry.prompt}: answer visible in prompt",
                answer.length < 4 || answer !in entry.prompt.lowercase(),
            )
        }
    }

    // ───────────────────────── rounds ─────────────────────────

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
    fun `every question has four options and a valid answer index`() {
        allQuestions().forEach { q ->
            assertEquals("${q.prompt}: not four options — ${q.options}", 4, q.options.size)
            assertTrue("${q.prompt}: answer index out of range", q.answerIndex in q.options.indices)
        }
    }

    /** If the answer always landed in the same slot, the quiz could be played without reading. */
    @Test
    fun `the answer moves around the four slots`() {
        val slots = allQuestions().groupingBy { it.answerIndex }.eachCount()
        assertEquals("answer never reaches some slots: $slots", 4, slots.size)
        val total = allQuestions().size
        slots.forEach { (slot, count) ->
            assertTrue("slot $slot is over-used: $slots", count < total / 2)
        }
    }

    /** Ten questions about one star is a worse round than ten across the catalogue. */
    @Test
    fun `a round spreads across systems`() {
        (0..20).forEach { seed ->
            val systems = Quiz.round(Quiz.ROUND_SIZE, Random(seed))
                .mapNotNull { factById(it.factId)?.sys }
                .distinct()
            assertTrue("seed $seed: only ${systems.size} system(s) — $systems", systems.size >= 5)
        }
    }

    @Test
    fun `a seed always produces the same round`() {
        assertEquals(Quiz.round(Quiz.ROUND_SIZE, Random(99)), Quiz.round(Quiz.ROUND_SIZE, Random(99)))
    }

    @Test
    fun `different seeds produce different rounds`() {
        val a = Quiz.round(Quiz.ROUND_SIZE, Random(1)).map { it.prompt }
        val b = Quiz.round(Quiz.ROUND_SIZE, Random(2)).map { it.prompt }
        assertTrue("two seeds gave the same round", a != b)
    }
}
