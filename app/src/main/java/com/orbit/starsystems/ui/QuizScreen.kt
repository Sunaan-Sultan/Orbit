package com.orbit.starsystems.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.orbit.starsystems.core.Analytics
import com.orbit.starsystems.core.Fact
import com.orbit.starsystems.core.OrbitPrefs
import com.orbit.starsystems.core.Quiz
import com.orbit.starsystems.core.QuizQuestion
import com.orbit.starsystems.core.SYS_META
import com.orbit.starsystems.core.factById

private val Right = Color(0xFF5BD68C)
private val Wrong = Color(0xFFFF6B5A)
private val QuizAccent = Color(0xFFFFC24D)

/** One step darker than pure black, so the cards and the accent glow have something to sit on. */
private val Ink = Color(0xFF08080C)
private val Card = Color(0xFFFFFFFF).copy(alpha = 0.045f)
private val Hairline = Color(0xFFFFFFFF).copy(alpha = 0.08f)

private val CardShape = RoundedCornerShape(22.dp)
private val ButtonShape = RoundedCornerShape(16.dp)

/** What the screen is showing: the opening card, a round in progress, or the score. */
private enum class Stage { INTRO, PLAYING, RESULT }

/**
 * Ten authored questions drawn from the facts the app already ships — see [Quiz] for the bank.
 *
 * A wrong answer is the interesting moment, so it is never just marked wrong: the right answer is
 * shown immediately along with the fact it came from, and the round ends with every fact that was
 * missed, tappable, so the quiz sends people back into the content rather than out of it.
 *
 * The screen keeps its colour from the fact being asked about, so the backdrop shifts from
 * question to question — the same accent the fact's own scene and card use.
 */
@Composable
fun QuizScreen(onOpenFact: (String) -> Unit, onClose: () -> Unit) {
    var stage by remember { mutableStateOf(Stage.INTRO) }
    var questions by remember { mutableStateOf(emptyList<QuizQuestion>()) }
    var index by remember { mutableIntStateOf(0) }
    var picked by remember { mutableStateOf<Int?>(null) }
    /** One entry per answered question, in order — drives the score, the pips and the accuracy. */
    var results by remember { mutableStateOf(emptyList<Boolean>()) }
    var missed by remember { mutableStateOf(emptyList<String>()) }
    var beatBest by remember { mutableStateOf(false) }

    val score = results.count { it }
    val question = questions.getOrNull(index)
    val fact = factById(question?.factId)

    fun start() {
        questions = Quiz.round()
        index = 0
        picked = null
        results = emptyList()
        missed = emptyList()
        beatBest = false
        stage = Stage.PLAYING
        Analytics.quizStarted()
    }

    fun advance() {
        if (index + 1 < questions.size) {
            index += 1
            picked = null
        } else {
            beatBest = OrbitPrefs.recordQuizRound(score)
            Analytics.quizFinished(score, questions.size)
            stage = Stage.RESULT
        }
    }

    val accent = when (stage) {
        Stage.PLAYING -> fact?.accent ?: QuizAccent
        else -> QuizAccent
    }
    val glow by animateColorAsState(accent, tween(500), label = "quizGlow")

    Column(Modifier.fillMaxSize().background(Ink).auroraGlow(glow)) {
        TopBar(
            stage = stage,
            index = index,
            total = questions.size,
            score = score,
            accent = accent,
            onClose = onClose,
        )

        when (stage) {
            Stage.INTRO -> Intro(onStart = { start() })

            // The pool cannot realistically empty, but an empty round must not strand.
            Stage.PLAYING -> if (question == null) Intro(onStart = { start() }) else {
                Pips(total = questions.size, results = results, current = index, accent = accent)
                Question(
                    question = question,
                    fact = fact,
                    accent = accent,
                    picked = picked,
                    isLast = index + 1 == questions.size,
                    onPick = { choice ->
                        if (picked == null) {
                            picked = choice
                            val correct = choice == question.answerIndex
                            results = results + correct
                            if (!correct) missed = missed + question.factId
                        }
                    },
                    onNext = { advance() },
                    onOpenFact = onOpenFact,
                )
            }

            Stage.RESULT -> Result(
                score = score,
                total = questions.size,
                beatBest = beatBest,
                missed = missed,
                onOpenFact = onOpenFact,
                onAgain = { start() },
                onClose = onClose,
            )
        }
    }
}

/** A soft wash of the current accent behind everything, bled in from the top corners. */
private fun Modifier.auroraGlow(color: Color) = drawBehind {
    drawRect(
        Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.22f), Color.Transparent),
            center = Offset(size.width * 0.22f, 0f),
            radius = size.width * 1.05f,
        ),
    )
    drawRect(
        Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.10f), Color.Transparent),
            center = Offset(size.width * 0.95f, size.height * 0.18f),
            radius = size.width * 0.85f,
        ),
    )
}

@Composable
private fun TopBar(
    stage: Stage,
    index: Int,
    total: Int,
    score: Int,
    accent: Color,
    onClose: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
                .border(1.dp, Hairline, CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) { Ico("back", size = 19.dp, color = Color.White, sw = 2.1f) }

        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("COSMIC QUIZ", style = ts(10.5f, FontWeight.Bold, Dim, 0.22f))
            if (stage == Stage.PLAYING && total > 0) {
                Text(
                    "Question ${index + 1} of $total",
                    style = ts(16f, FontWeight.SemiBold, Color.White),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        if (stage == Stage.PLAYING) {
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f))
                    .border(1.dp, accent.copy(alpha = 0.30f), CircleShape)
                    .padding(horizontal = 13.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Ico("check", size = 13.dp, color = accent, sw = 2.6f)
                Spacer(Modifier.width(6.dp))
                Text("$score", style = ts(14f, FontWeight.Bold, accent))
            }
        }
    }
}

/**
 * One pip per question, so progress and performance read in a single glance: answered pips carry
 * their own result, the current one is a wider bar in the accent, the rest stay faint.
 */
@Composable
private fun Pips(total: Int, results: List<Boolean>, current: Int, accent: Color) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { i ->
            val answered = results.getOrNull(i)
            val target = when {
                answered == true -> Right
                answered == false -> Wrong
                i == current -> accent
                else -> Color.White.copy(alpha = 0.12f)
            }
            val color by animateColorAsState(target, tween(280), label = "pip$i")
            val weight by animateFloatAsState(if (i == current) 2.2f else 1f, tween(280), label = "pipW$i")
            Box(
                Modifier
                    .weight(weight)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

// ───────────────────────── intro ─────────────────────────

@Composable
private fun Intro(onStart: () -> Unit) {
    val best = OrbitPrefs.quizBest
    val rounds = OrbitPrefs.quizRounds

    // A slow breath on the badge, so the opening screen is not completely static.
    val pulse = rememberInfiniteTransition(label = "quizPulse")
    val breath by pulse.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Reverse),
        label = "breath",
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(30.dp))
        Box(contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(150.dp)
                    .graphicsLayer { scaleX = breath; scaleY = breath }
                    .background(
                        Brush.radialGradient(listOf(QuizAccent.copy(alpha = 0.26f), Color.Transparent)),
                        CircleShape,
                    ),
            )
            Box(
                Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(QuizAccent.copy(alpha = 0.12f))
                    .border(1.dp, QuizAccent.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Ico("spotlight", size = 38.dp, color = QuizAccent, filled = true) }
        }

        Text(
            "Cosmic Quiz",
            style = ts(36f, FontWeight.Bold, Color.White, -0.025f),
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            "Ten questions, drawn from the facts in this app. Miss one and you'll see the answer — and the fact it came from.",
            style = ts(15f, FontWeight.Light, Mute, lineHeight = 23f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp, start = 6.dp, end = 6.dp),
        )

        Row(
            Modifier.fillMaxWidth().padding(top = 26.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatTile("$best", "BEST SCORE", Modifier.weight(1f))
            StatTile("$rounds", if (rounds == 1) "ROUND PLAYED" else "ROUNDS PLAYED", Modifier.weight(1f))
            StatTile("${Quiz.bank.size}", "QUESTIONS", Modifier.weight(1f))
        }

        Spacer(Modifier.height(26.dp))
        PrimaryButton(if (rounds == 0) "Start the round" else "Play again", QuizAccent, onStart)
        Spacer(Modifier.height(34.dp))
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(CardShape)
            .background(Card)
            .border(1.dp, Hairline, CardShape)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = ts(24f, FontWeight.Bold, Color.White))
        Text(
            label,
            style = ts(9.5f, FontWeight.SemiBold, Dim, 0.13f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
        )
    }
}

// ───────────────────────── a question ─────────────────────────

@Composable
private fun Question(
    question: QuizQuestion,
    fact: Fact?,
    accent: Color,
    picked: Int?,
    isLast: Boolean,
    onPick: (Int) -> Unit,
    onNext: () -> Unit,
    onOpenFact: (String) -> Unit,
) {
    // Each question slides up as it arrives, so moving on reads as a new card rather than as
    // text being swapped underneath the same options.
    val enter = remember(question.prompt) { Animatable(0f) }
    LaunchedEffect(question.prompt) { enter.animateTo(1f, tween(340, easing = FastOutSlowInEasing)) }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 12.dp)
                .graphicsLayer {
                    alpha = enter.value
                    translationY = (1f - enter.value) * 26.dp.toPx()
                },
        ) {
            fact?.let { Eyebrow(it, accent) }
            Text(
                question.prompt,
                style = ts(26f, FontWeight.Bold, Color.White, -0.02f, lineHeight = 33f),
                modifier = Modifier.padding(top = 12.dp, bottom = 22.dp),
            )
            question.options.forEachIndexed { i, option ->
                Option(
                    letter = ('A' + i).toString(),
                    text = option,
                    accent = accent,
                    state = when {
                        picked == null -> OptionState.IDLE
                        i == question.answerIndex -> OptionState.RIGHT
                        i == picked -> OptionState.WRONG
                        else -> OptionState.MUTED
                    },
                    onClick = { onPick(i) },
                )
                Spacer(Modifier.height(9.dp))
            }
            if (picked != null && fact != null) {
                Spacer(Modifier.height(8.dp))
                Verdict(
                    correct = picked == question.answerIndex,
                    answer = question.answer,
                    fact = fact,
                    onOpenFact = { onOpenFact(fact.id) },
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        // Pinned to the bottom so the next question is always one tap away in the same place,
        // however long the options run.
        if (picked != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Ink)))
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                PrimaryButton(if (isLast) "See your score" else "Next question", accent, onNext)
            }
        } else {
            Spacer(Modifier.navigationBarsPadding().height(12.dp))
        }
    }
}

/** Where the question came from: its system and the category it sits in. */
@Composable
private fun Eyebrow(fact: Fact, accent: Color) {
    val system = SYS_META[fact.sys]?.label
    val label = if (system == null) fact.cat else "$system · ${fact.cat}"
    Row(
        Modifier
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.26f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(5.dp).clip(CircleShape).background(accent))
        Spacer(Modifier.width(7.dp))
        Text(label.uppercase(), style = ts(10.5f, FontWeight.Bold, accent, 0.12f))
    }
}

private enum class OptionState { IDLE, RIGHT, WRONG, MUTED }

@Composable
private fun Option(
    letter: String,
    text: String,
    accent: Color,
    state: OptionState,
    onClick: () -> Unit,
) {
    val tint = when (state) {
        OptionState.IDLE -> accent
        OptionState.RIGHT -> Right
        OptionState.WRONG -> Wrong
        OptionState.MUTED -> Color.White.copy(alpha = 0.22f)
    }
    val border by animateColorAsState(
        when (state) {
            OptionState.IDLE -> Color.White.copy(alpha = 0.10f)
            OptionState.MUTED -> Color.White.copy(alpha = 0.05f)
            else -> tint.copy(alpha = 0.75f)
        },
        tween(260),
        label = "optBorder",
    )
    val fill by animateColorAsState(
        when (state) {
            OptionState.RIGHT -> Right.copy(alpha = 0.13f)
            OptionState.WRONG -> Wrong.copy(alpha = 0.13f)
            OptionState.MUTED -> Color.White.copy(alpha = 0.02f)
            else -> Card
        },
        tween(260),
        label = "optFill",
    )
    val label by animateColorAsState(
        if (state == OptionState.MUTED) Color(0xFF6E6E76) else Color.White,
        tween(260),
        label = "optLabel",
    )
    // The chosen row lifts very slightly, so the tap has a physical answer to it.
    val scale by animateFloatAsState(
        if (state == OptionState.RIGHT || state == OptionState.WRONG) 1.015f else 1f,
        tween(260, easing = FastOutSlowInEasing),
        label = "optScale",
    )

    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(fill)
            .border(1.dp, border, shape)
            .clickable(enabled = state == OptionState.IDLE, onClick = onClick)
            .padding(start = 12.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tint.copy(alpha = if (state == OptionState.MUTED) 0.05f else 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                OptionState.RIGHT -> Ico("check", size = 15.dp, color = Right, sw = 2.6f)
                OptionState.WRONG -> Ico("close", size = 14.dp, color = Wrong, sw = 2.4f)
                else -> Text(
                    letter,
                    style = ts(
                        13f,
                        FontWeight.Bold,
                        if (state == OptionState.MUTED) Color(0xFF6E6E76) else tint,
                    ),
                )
            }
        }
        Spacer(Modifier.width(13.dp))
        Text(text, style = ts(15.5f, FontWeight.Medium, label, lineHeight = 21f), modifier = Modifier.weight(1f))
    }
}

/** Shown once an answer is locked in: the verdict, the answer, and a way back to the fact. */
@Composable
private fun Verdict(correct: Boolean, answer: String, fact: Fact, onOpenFact: () -> Unit) {
    val tone = if (correct) Right else Wrong
    Column(
        Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(tone.copy(alpha = 0.07f))
            .border(1.dp, tone.copy(alpha = 0.22f), CardShape)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Ico(if (correct) "check" else "close", size = 15.dp, color = tone, sw = 2.5f)
            Spacer(Modifier.width(8.dp))
            Text(
                if (correct) "Correct" else "Not quite",
                style = ts(14f, FontWeight.Bold, tone, 0.02f),
            )
        }
        if (!correct) {
            // Set on its own line under a label rather than folded into a sentence: the options
            // are written as phrases, and "The answer is By a tug on the star…" reads badly.
            Text(
                "THE ANSWER",
                style = ts(9.5f, FontWeight.Bold, Dim, 0.16f),
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                answer,
                style = ts(16f, FontWeight.SemiBold, Color.White, lineHeight = 22f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Row(
            Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .clickable(onClick = onOpenFact)
                .padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(fact.title, style = ts(14f, FontWeight.SemiBold, Color.White))
                if (fact.sub.isNotBlank()) {
                    Text(
                        fact.sub,
                        style = ts(12.5f, color = Mute, lineHeight = 17f),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Box(Modifier.graphicsLayer { rotationZ = 180f }) {
                Ico("back", size = 16.dp, color = fact.accent, sw = 2.1f)
            }
        }
    }
}

// ───────────────────────── the score ─────────────────────────

@Composable
private fun Result(
    score: Int,
    total: Int,
    beatBest: Boolean,
    missed: List<String>,
    onOpenFact: (String) -> Unit,
    onAgain: () -> Unit,
    onClose: () -> Unit,
) {
    val facts = remember(missed) { missed.mapNotNull { factById(it) } }
    val pct = if (total == 0) 0 else score * 100 / total
    LazyColumn(
        Modifier.fillMaxSize().navigationBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 30.dp),
    ) {
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                ScoreRing(score = score, total = total)
                if (beatBest) {
                    Row(
                        Modifier
                            .padding(top = 16.dp)
                            .clip(CircleShape)
                            .background(QuizAccent.copy(alpha = 0.14f))
                            .border(1.dp, QuizAccent.copy(alpha = 0.32f), CircleShape)
                            .padding(horizontal = 13.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Ico("bolt", size = 13.dp, color = QuizAccent, filled = true)
                        Spacer(Modifier.width(6.dp))
                        Text("NEW PERSONAL BEST", style = ts(10.5f, FontWeight.Bold, QuizAccent, 0.14f))
                    }
                }
                Text(
                    when {
                        score == total -> "Every one. Nothing left to catch you out."
                        pct >= 75 -> "Strong round."
                        pct >= 50 -> "Over halfway there."
                        else -> "Plenty of sky left to learn."
                    },
                    style = ts(16f, FontWeight.Light, Color(0xFFCFCFCF), lineHeight = 24f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Spacer(Modifier.height(22.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("$pct%", "ACCURACY", Modifier.weight(1f))
                    StatTile("${total - score}", if (total - score == 1) "MISSED" else "MISSES", Modifier.weight(1f))
                    StatTile("${OrbitPrefs.quizBest}", "BEST SCORE", Modifier.weight(1f))
                }
                Spacer(Modifier.height(26.dp))
            }
        }
        if (facts.isNotEmpty()) {
            item {
                Text(
                    "WORTH A SECOND LOOK",
                    style = ts(11f, FontWeight.Bold, Dim, 0.2f),
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
            items(facts.size) { i ->
                val fact = facts[i]
                Box(Modifier.padding(bottom = 10.dp)) {
                    FactListCard(fact = fact, onClick = { onOpenFact(fact.id) })
                }
            }
        }
        item {
            Spacer(Modifier.height(14.dp))
            PrimaryButton("Play again", QuizAccent, onAgain)
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(ButtonShape)
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Hairline, ButtonShape)
                    .clickable(onClick = onClose)
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) { Text("Done", style = ts(15.5f, FontWeight.SemiBold, Color.White)) }
        }
    }
}

/** The score as a sweep around a ring, drawn up from zero when the round ends. */
@Composable
private fun ScoreRing(score: Int, total: Int) {
    val target = if (total == 0) 0f else score.toFloat() / total
    val sweep = remember { Animatable(0f) }
    LaunchedEffect(target) { sweep.animateTo(target, tween(900, easing = FastOutSlowInEasing)) }

    Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            if (sweep.value > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(QuizAccent, Right, QuizAccent)),
                    startAngle = -90f,
                    sweepAngle = 360f * sweep.value,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$score", style = ts(62f, FontWeight.Bold, Color.White, -0.04f))
            Text("OUT OF $total", style = ts(11f, FontWeight.SemiBold, Dim, 0.18f))
        }
    }
}

@Composable
private fun PrimaryButton(label: String, accent: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(ButtonShape)
            .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.82f))))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = ts(15.5f, FontWeight.Bold, Color.Black.copy(alpha = 0.88f)))
    }
}
