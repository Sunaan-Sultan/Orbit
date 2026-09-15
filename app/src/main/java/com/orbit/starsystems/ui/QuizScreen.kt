package com.orbit.starsystems.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.orbit.starsystems.core.Analytics
import com.orbit.starsystems.core.OrbitPrefs
import com.orbit.starsystems.core.Quiz
import com.orbit.starsystems.core.QuizQuestion
import com.orbit.starsystems.core.factById

private val Right = Color(0xFF6BC08A)
private val Wrong = Color(0xFFE0654A)
private val QuizAccent = Color(0xFFFFC24D)

/** What the screen is showing: the opening card, a round in progress, or the score. */
private enum class Stage { INTRO, PLAYING, RESULT }

/**
 * Ten questions built from facts the app already ships — see [Quiz] for how they are made.
 *
 * A wrong answer is the interesting moment, so it is never just marked wrong: the right answer
 * is shown immediately, and the round ends with every fact that was missed, tappable, so the
 * quiz sends people back into the content rather than out of it.
 */
@Composable
fun QuizScreen(onOpenFact: (String) -> Unit, onClose: () -> Unit) {
    var stage by remember { mutableStateOf(Stage.INTRO) }
    var questions by remember { mutableStateOf(emptyList<QuizQuestion>()) }
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var picked by remember { mutableStateOf<Int?>(null) }
    var missed by remember { mutableStateOf(emptyList<String>()) }
    var beatBest by remember { mutableStateOf(false) }

    fun start() {
        questions = Quiz.round()
        index = 0
        score = 0
        picked = null
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

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Ico("back", size = 20.dp, color = Color.White, sw = 2.1f) }
            Spacer(Modifier.width(12.dp))
            Text(
                if (stage == Stage.PLAYING) "Question ${index + 1} of ${questions.size}" else "Cosmic Quiz",
                style = ts(16f, FontWeight.SemiBold, Color.White),
                modifier = Modifier.weight(1f),
            )
            if (stage == Stage.PLAYING) {
                Text("$score correct", style = ts(13.5f, FontWeight.Medium, QuizAccent))
            }
        }

        when (stage) {
            Stage.INTRO -> Intro(onStart = { start() })
            Stage.PLAYING -> {
                val question = questions.getOrNull(index)
                if (question == null) {
                    // The pool cannot realistically empty, but an empty round must not strand.
                    Intro(onStart = { start() })
                } else {
                    Progress(index, questions.size)
                    Question(
                        question = question,
                        picked = picked,
                        onPick = { choice ->
                            if (picked == null) {
                                picked = choice
                                if (choice == question.answerIndex) score += 1
                                else missed = missed + question.factId
                            }
                        },
                        onNext = { advance() },
                        isLast = index + 1 == questions.size,
                    )
                }
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

@Composable
private fun Progress(index: Int, total: Int) {
    val target = if (total == 0) 0f else index.toFloat() / total
    val width by animateFloatAsState(target, tween(320), label = "quizProgress")
    Box(Modifier.fillMaxWidth().height(3.dp).background(Color.White.copy(alpha = 0.10f))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(width).background(QuizAccent))
    }
}

@Composable
private fun Intro(onStart: () -> Unit) {
    val best = OrbitPrefs.quizBest
    val rounds = OrbitPrefs.quizRounds
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(40.dp))
        Box(
            Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(QuizAccent.copy(alpha = 0.30f), Color.Transparent))),
            contentAlignment = Alignment.Center,
        ) { Ico("spotlight", size = 46.dp, color = QuizAccent, filled = true) }
        Text(
            "Cosmic Quiz",
            style = ts(34f, FontWeight.Bold, Color.White, -0.02f),
            modifier = Modifier.padding(top = 18.dp),
        )
        Text(
            "Ten questions, drawn from the facts in this app. Miss one and you'll see the answer — and the fact it came from.",
            style = ts(15.5f, FontWeight.Light, Mute, lineHeight = 24f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
        if (rounds > 0) {
            Row(
                Modifier
                    .padding(top = 26.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 30.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(38.dp),
            ) {
                Stat("$best/${Quiz.ROUND_SIZE}", "BEST")
                Stat(rounds.toString(), if (rounds == 1) "ROUND" else "ROUNDS")
            }
        }
        Spacer(Modifier.height(30.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(QuizAccent)
                .clickable(onClick = onStart)
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (rounds == 0) "Start" else "Play again", style = ts(16f, FontWeight.Bold, Color.Black))
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = ts(26f, FontWeight.Bold, Color.White))
        Text(label, style = ts(10.5f, FontWeight.SemiBold, Dim, 0.14f), modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun Question(
    question: QuizQuestion,
    picked: Int?,
    onPick: (Int) -> Unit,
    onNext: () -> Unit,
    isLast: Boolean,
) {
    val accent = factById(question.factId)?.accent ?: QuizAccent
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding()
            .padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 24.dp),
    ) {
        question.subject?.let {
            Text(it, style = ts(13f, FontWeight.SemiBold, accent, 0.10f))
        }
        Text(
            question.prompt,
            style = ts(28f, FontWeight.Bold, Color.White, -0.02f, lineHeight = 34f),
            modifier = Modifier.padding(top = 8.dp, bottom = 26.dp),
        )
        question.options.forEachIndexed { i, option ->
            Option(
                text = option,
                state = when {
                    picked == null -> OptionState.IDLE
                    i == question.answerIndex -> OptionState.RIGHT
                    i == picked -> OptionState.WRONG
                    else -> OptionState.MUTED
                },
                onClick = { onPick(i) },
            )
            Spacer(Modifier.height(10.dp))
        }
        if (picked != null) {
            val correct = picked == question.answerIndex
            Spacer(Modifier.height(10.dp))
            Text(
                if (correct) "Correct." else "The answer is ${question.answer}.",
                style = ts(15f, FontWeight.SemiBold, if (correct) Right else Wrong),
            )
            factById(question.factId)?.let { fact ->
                Text(
                    "From “${fact.title}”.",
                    style = ts(13.5f, color = Dim, lineHeight = 20f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable(onClick = onNext)
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (isLast) "See your score" else "Next", style = ts(16f, FontWeight.SemiBold, Color.White))
            }
        }
    }
}

private enum class OptionState { IDLE, RIGHT, WRONG, MUTED }

@Composable
private fun Option(text: String, state: OptionState, onClick: () -> Unit) {
    val border = when (state) {
        OptionState.IDLE -> Color.White.copy(alpha = 0.16f)
        OptionState.RIGHT -> Right
        OptionState.WRONG -> Wrong
        OptionState.MUTED -> Color.White.copy(alpha = 0.08f)
    }
    val fill = when (state) {
        OptionState.RIGHT -> Right.copy(alpha = 0.14f)
        OptionState.WRONG -> Wrong.copy(alpha = 0.14f)
        else -> Color.White.copy(alpha = 0.04f)
    }
    val label = when (state) {
        OptionState.MUTED -> Color(0xFF7A7A7A)
        else -> Color.White
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(fill)
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickable(enabled = state == OptionState.IDLE, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = ts(16f, FontWeight.Medium, label), modifier = Modifier.weight(1f))
        when (state) {
            OptionState.RIGHT -> Ico("check", size = 18.dp, color = Right, sw = 2.4f)
            OptionState.WRONG -> Ico("close", size = 17.dp, color = Wrong, sw = 2.2f)
            else -> Unit
        }
    }
}

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
    LazyColumn(
        Modifier.fillMaxSize().navigationBarsPadding(),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 30.dp),
    ) {
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$score", style = ts(72f, FontWeight.Bold, QuizAccent, -0.03f))
                Text("out of $total".uppercase(), style = ts(12f, FontWeight.SemiBold, Dim, 0.2f))
                Text(
                    when {
                        beatBest -> "A new personal best."
                        score == total -> "Every one. Nothing left to catch you out."
                        score >= total * 3 / 4 -> "Strong round."
                        score >= total / 2 -> "Over halfway there."
                        else -> "Plenty of sky left to learn."
                    },
                    style = ts(16f, FontWeight.Light, Color(0xFFCFCFCF), lineHeight = 24f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Spacer(Modifier.height(28.dp))
            }
        }
        if (facts.isNotEmpty()) {
            item {
                Text(
                    "WORTH A SECOND LOOK",
                    style = ts(11.5f, FontWeight.Bold, Dim, 0.22f),
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
            items(facts.size) { i ->
                val fact = facts[i]
                Box(Modifier.padding(bottom = 12.dp)) {
                    FactListCard(fact = fact, onClick = { onOpenFact(fact.id) })
                }
            }
        }
        item {
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(QuizAccent)
                    .clickable(onClick = onAgain)
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) { Text("Play again", style = ts(16f, FontWeight.Bold, Color.Black)) }
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable(onClick = onClose)
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) { Text("Done", style = ts(16f, FontWeight.SemiBold, Color.White)) }
        }
    }
}
