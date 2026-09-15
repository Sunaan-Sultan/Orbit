package com.orbit.starsystems.core

import java.util.Locale
import kotlin.math.roundToLong
import kotlin.random.Random

/** What a question asks for, which decides how its options are compared and rendered. */
enum class QuizKind {
    /** A measurement, with distractors perturbed from the real value. */
    QUANTITY,

    /** A subtitle, with other fact titles as distractors. */
    IDENTIFY,

    /** A fact's home system, with other system names as distractors. */
    SYSTEM,
}

/** One multiple-choice question, with its options already shuffled. */
data class QuizQuestion(
    val kind: QuizKind,
    val factId: String,
    /** The thing being asked about — a fact title, or null when the prompt stands alone. */
    val subject: String?,
    val prompt: String,
    val options: List<String>,
    val answerIndex: Int,
) {
    val answer: String get() = options[answerIndex]
}

/**
 * Builds quiz rounds out of the catalog that already ships — no new authoring.
 *
 * Every fact carries a `stats` list of label/value pairs (393 of them across the 131 facts),
 * written to be read rather than answered. Three generators turn them into questions:
 *
 * - [quantityPool] asks for a measurement and invents wrong numbers by perturbing the real
 *   one, re-rendered in exactly the authored format so no option stands out by its shape.
 * - [identifyPool] shows a fact's subtitle and asks which fact it describes.
 * - [systemPool] asks which system a fact belongs to.
 *
 * Two hazards drove the design, both found by measuring the data rather than assuming:
 *
 * 1. **Leaked answers.** Many titles and subtitles contain the very number being asked for
 *    ("41 Light-Years Away", distance 41 light-years). 23 of 182 candidate quantities are
 *    unusable for that reason and are dropped by [leaks].
 * 2. **Ambiguous comparisons.** A fourth generator — "which of these is closest?" — was
 *    dropped: only four label/unit groups have four or more members, and those contain tied
 *    values (two facts at 4.24 light-years, two at 0.09 solar masses), so the question would
 *    have had more than one right answer.
 */
object Quiz {

    const val ROUND_SIZE = 10

    private const val SOL = "sol"

    /** Drawn in this repeating order so a round always mixes question types. */
    private val MIX = listOf(QuizKind.QUANTITY, QuizKind.QUANTITY, QuizKind.IDENTIFY, QuizKind.QUANTITY, QuizKind.SYSTEM)

    /** A value that is one measurement, not a compound like "G2 · Sun-like" or "Red dwarf (M5.5)". */
    private val QUANTITY = Regex("""^([^\d]*?)(\d[\d,]*(?:\.\d+)?)([^\d]*)$""")
    private val COMPOUND = listOf("·", "(", ")", "/", "+")

    /**
     * A round of [size] questions, at most one per fact so the same title never comes up twice.
     * [random] is a parameter so tests can pin a round.
     */
    fun round(size: Int = ROUND_SIZE, random: Random = Random.Default): List<QuizQuestion> {
        val pools = mapOf(
            QuizKind.QUANTITY to quantityPool(random).shuffled(random).toMutableList(),
            QuizKind.IDENTIFY to identifyPool(random).shuffled(random).toMutableList(),
            QuizKind.SYSTEM to systemPool(random).shuffled(random).toMutableList(),
        )
        val used = HashSet<String>()
        val out = ArrayList<QuizQuestion>(size)

        fun drawFrom(kind: QuizKind): QuizQuestion? {
            val pool = pools.getValue(kind)
            while (pool.isNotEmpty()) {
                val q = pool.removeAt(pool.lastIndex)
                if (used.add(q.factId)) return q
            }
            return null
        }

        var i = 0
        while (out.size < size && pools.values.any { it.isNotEmpty() }) {
            val preferred = MIX[i % MIX.size]
            // Fall back through the other kinds rather than ending the round short when one
            // pool runs dry.
            val q = drawFrom(preferred) ?: QuizKind.entries.firstNotNullOfOrNull { drawFrom(it) }
            if (q != null) out += q
            i++
        }
        return out
    }

    // ───────────────────────── generators ─────────────────────────

    private fun quantityPool(random: Random): List<QuizQuestion> = ALL_FACTS.flatMap { fact ->
        fact.stats.mapNotNull { (label, value) ->
            if (COMPOUND.any { it in value }) return@mapNotNull null
            val clean = value.trim()
            val match = QUANTITY.matchEntire(clean) ?: return@mapNotNull null
            if (leaks(match.groupValues[2], fact)) return@mapNotNull null
            val wrong = distractors(match, random) ?: return@mapNotNull null
            build(QuizKind.QUANTITY, fact.id, fact.title, "$label?", clean, wrong, random)
        }
    }

    private fun identifyPool(random: Random): List<QuizQuestion> {
        val byTitle = ALL_FACTS.distinctBy { it.title }
        return byTitle.mapNotNull { fact ->
            if (fact.sub.isBlank()) return@mapNotNull null
            // A subtitle that repeats a word of its own title answers itself.
            if (titleWords(fact.title).any { it in fact.sub.lowercase() }) return@mapNotNull null
            val wrong = byTitle
                .filter { it.title != fact.title }
                // Same category first, then same system: near misses make a real question.
                .sortedBy { if (it.cat == fact.cat) 0 else if (it.sys == fact.sys) 1 else 2 }
                .take(8)
                .shuffled(random)
                .take(3)
                .map { it.title }
            if (wrong.size < 3) return@mapNotNull null
            build(QuizKind.IDENTIFY, fact.id, "“${fact.sub}”", "Which fact is this?", fact.title, wrong, random)
        }
    }

    private fun systemPool(random: Random): List<QuizQuestion> {
        val labels = SYS_META.mapValues { it.value.label }
        if (labels.size < 4) return emptyList()
        return ALL_FACTS.mapNotNull { fact ->
            // Sol is excluded: everyone already knows the Moon and Saturn are in our own
            // system, so "The Sun — which system?" tests nothing.
            if (fact.sys == SOL) return@mapNotNull null
            val answer = labels[fact.sys] ?: return@mapNotNull null
            // Skip when the system names itself in the fact's own copy.
            val hay = "${fact.title} ${fact.sub}".lowercase()
            if (titleWords(answer).any { it in hay }) return@mapNotNull null
            val wrong = labels.values.filter { it != answer }.shuffled(random).take(3)
            if (wrong.size < 3) return@mapNotNull null
            build(QuizKind.SYSTEM, fact.id, fact.title, "Which system is this from?", answer, wrong, random)
        }
    }

    // ───────────────────────── helpers ─────────────────────────

    private fun build(
        kind: QuizKind,
        factId: String,
        subject: String?,
        prompt: String,
        answer: String,
        wrong: List<String>,
        random: Random,
    ): QuizQuestion? {
        val options = (wrong + answer).distinct()
        if (options.size < 4) return null
        val shuffled = options.shuffled(random)
        return QuizQuestion(kind, factId, subject, prompt, shuffled, shuffled.indexOf(answer))
    }

    /** Words worth matching on — short ones ("the", "a") match everything. */
    private fun titleWords(title: String): List<String> =
        title.lowercase().split(Regex("[^a-z0-9]+")).filter { it.length >= 4 }

    /** True when the answer is printed in the fact's own title or subtitle. */
    private fun leaks(numText: String, fact: Fact): Boolean {
        val hay = "${fact.title} ${fact.sub}".lowercase()
        return numText.lowercase() in hay || numText.replace(",", "").lowercase() in hay
    }

    /**
     * Three wrong numbers for one right one, rendered in the authored format — same prefix,
     * same unit, same decimal places, same thousands separators.
     *
     * Small whole numbers step by addition, because scaling 3 by 0.2 gives 0.6 and a lone
     * fractional option among whole ones is the answer by elimination.
     *
     * Every candidate must render to the same [shape] as the answer. Without that the Sun's
     * diameter — 1,391,000 km — drew 556,000 and 278,000 as distractors, and the only
     * seven-digit option was the right one: countable at a glance, no astronomy required.
     * Candidates that fail are skipped, and a value that cannot produce three is dropped from
     * the pool rather than asked badly.
     */
    private fun distractors(match: MatchResult, random: Random): List<String>? {
        val (prefix, numText, suffix) = match.destructured
        val base = numText.replace(",", "").toDoubleOrNull() ?: return null
        if (base <= 0.0) return null
        val decimals = numText.substringAfter('.', "").length
        val grouped = ',' in numText

        // A year is a position on a scale, not a magnitude: six times 2015 is not a wrong
        // answer, it is a different kind of thing. Scaling produced "Copernicus, 12090".
        val isYear = decimals == 0 && !grouped && base >= 1000.0 && base <= 2999.0
        val candidates = when {
            isYear -> listOf(3.0, 5.0, 8.0, 12.0, 17.0, 24.0, 33.0, 46.0, 61.0)
                .flatMap { listOf(base + it, base - it) }
            decimals == 0 && base < 12 ->
                listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0).flatMap { listOf(base + it, base - it) }
            else -> listOf(0.2, 0.25, 0.3, 0.4, 0.5, 0.6, 0.75, 1.25, 1.5, 1.75, 2.0, 2.5, 3.0, 4.0, 5.0, 6.0)
                .map { base * it }
        }

        val answer = prefix + numText + suffix
        val answerShape = shape(answer)
        val out = LinkedHashSet<String>()
        for (value in candidates.shuffled(random)) {
            if (value <= 0.0) continue
            val rendered = render(prefix, if (isYear) value else roundLike(base, value), decimals, grouped, suffix)
            // A value that rounds away to nothing is not a wrong answer, it is a non-answer:
            // "< 0.1%" drew "< 0.0%", which no one would pick.
            if (rendered.none { it in '1'..'9' }) continue
            if (rendered != answer && shape(rendered) == answerShape) out += rendered
            if (out.size == 3) break
        }
        return out.toList().takeIf { it.size == 3 }
    }

    /**
     * Everything about a rendered value except its digits — the separators, the unit and the
     * decimal point. Two values with the same shape have the same number of digits in the same
     * groups, so neither can be told from the other without reading them.
     */
    private fun shape(value: String): String = value.filter { !it.isDigit() }

    /**
     * Rounds to the granularity the author used, so a perturbed 282,000 reads as 705,000
     * rather than 705,120 — a value that is precise where its neighbours are round is the odd
     * one out, and the quiz would be guessable on formatting alone.
     */
    private fun roundLike(base: Double, value: Double): Double {
        if (base % 1.0 != 0.0) return value
        val whole = base.roundToLong()
        if (whole == 0L) return value
        var step = 1L
        while (step <= 1_000_000_000L && whole % (step * 10) == 0L) step *= 10
        return (value / step).roundToLong() * step.toDouble()
    }

    private fun render(
        prefix: String,
        value: Double,
        decimals: Int,
        grouped: Boolean,
        suffix: String,
    ): String {
        val body = if (decimals == 0) {
            val whole = value.roundToLong()
            if (grouped) String.format(Locale.US, "%,d", whole) else whole.toString()
        } else {
            String.format(Locale.US, if (grouped) "%,.${decimals}f" else "%.${decimals}f", value)
        }
        return prefix + body + suffix
    }
}
