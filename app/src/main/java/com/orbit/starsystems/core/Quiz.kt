package com.orbit.starsystems.core

import kotlin.random.Random

/**
 * One authored question, exactly as it sits in `assets/quiz.json`.
 *
 * The options are kept apart here — answer and distractors — because the shuffle belongs to the
 * round, not the bank: the same question must not always put its answer in the same place.
 */
data class QuizEntry(
    /** The fact this question came from, so a missed one can send the reader back to it. */
    val factId: String,
    val prompt: String,
    val answer: String,
    val wrong: List<String>,
)

/** One multiple-choice question, with its options already shuffled for this round. */
data class QuizQuestion(
    val factId: String,
    val prompt: String,
    val options: List<String>,
    val answerIndex: Int,
) {
    val answer: String get() = options[answerIndex]
}

/**
 * Rounds of ten, drawn from an authored bank in `assets/quiz.json`.
 *
 * An earlier version of this built its questions by machine out of the catalog's `stats` rows —
 * the label became the prompt, the value became the answer, and wrong answers were invented by
 * perturbing the real number. It could not work, because those rows are written to be read
 * beside a fact, not answered without one:
 *
 * - Many are yardsticks rather than facts about their subject. "The Sun — 4.6 billion years"
 *   sits under Barnard's Star and under Teegarden's Star to give the reader something to measure
 *   against; asked as a question it has nothing to do with either star, and is the same question
 *   twice.
 * - Many labels mean nothing alone. "Undone?", "Seen?", "Rank?", "From?" are column headings, not
 *   questions, and no amount of formatting makes them answerable.
 * - Many values are not quantities at all. "TRAPPIST-1e", "Kepler-90i" and "5th-closest system"
 *   all contain a digit, so the number generator took them and produced "TRAPPIST-3e" and
 *   "9th-closest system" as plausible wrong answers.
 * - Some titles give the answer away in words rather than digits — "A Sixth of a Sun" over a
 *   question whose answer is 0.16 × Sun — which a digit-matching check cannot catch.
 *
 * So the questions are written now, one to three per fact across all 131 of them, and this file
 * only assembles rounds. [QuizTest] holds the bank to the rules the generator used to enforce.
 */
object Quiz {

    const val ROUND_SIZE = 10

    /** Every authored question. Empty until [OrbitData.init] has run. */
    val bank: List<QuizEntry> get() = OrbitData.quiz

    /**
     * A round of [size] questions, at most one per fact so no title comes up twice, spread
     * across systems so a round is never ten questions about one star.
     * [random] is a parameter so tests can pin a round.
     */
    fun round(size: Int = ROUND_SIZE, random: Random = Random.Default): List<QuizQuestion> {
        val queues = bank
            .groupBy { factById(it.factId)?.sys ?: "" }
            .values
            .map { it.shuffled(random).toMutableList() }
            .shuffled(random)
        if (queues.isEmpty()) return emptyList()

        val used = HashSet<String>()
        val out = ArrayList<QuizEntry>(size)
        // Round-robin: one question from each system in turn, then round again. A system that
        // runs dry simply stops contributing rather than ending the round short.
        while (out.size < size && queues.any { it.isNotEmpty() }) {
            for (queue in queues) {
                if (out.size == size) break
                while (queue.isNotEmpty()) {
                    val entry = queue.removeAt(queue.lastIndex)
                    if (used.add(entry.factId)) {
                        out += entry
                        break
                    }
                }
            }
        }
        return out.map { it.toQuestion(random) }
    }

    /**
     * Two wrong options to strike out, for the 50/50 hint.
     *
     * Never returns the answer's index, and hands back fewer than two only if a question somehow
     * ships with fewer than three wrong options — the bank is checked for four distinct options
     * in [QuizTest], so in practice it is always exactly two.
     */
    fun fiftyFiftyHidden(question: QuizQuestion, random: Random = Random.Default): Set<Int> =
        question.options.indices
            .filter { it != question.answerIndex }
            .shuffled(random)
            .take(2)
            .toSet()

    private fun QuizEntry.toQuestion(random: Random): QuizQuestion {
        val options = (wrong + answer).shuffled(random)
        return QuizQuestion(factId, prompt, options, options.indexOf(answer))
    }
}
