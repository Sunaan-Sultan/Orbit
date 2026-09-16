package com.orbit.starsystems.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.orbit.starsystems.core.Analytics
import com.orbit.starsystems.core.DailyFact
import com.orbit.starsystems.core.OrbitPrefs
import com.orbit.starsystems.core.Quiz
import com.orbit.starsystems.core.Streak
import com.orbit.starsystems.core.FEATURED_SYSTEMS
import com.orbit.starsystems.core.Fact
import com.orbit.starsystems.core.FeaturedSystem
import com.orbit.starsystems.core.SYSTEMS
import com.orbit.starsystems.core.SceneId
import com.orbit.starsystems.core.factsForSys

@Composable
fun SystemsList(
    viewed: Set<String>,
    onOpenSystem: (String) -> Unit,
    onOpenFact: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenQuiz: () -> Unit,
) {
    // Recomputed per composition rather than remembered: the pick rolls over at local
    // midnight, and a session can outlive that.
    val today = DailyFact.factForToday()

    // The revision is what makes the quiz card honest. It used to be safe to read these
    // straight off disk, because nothing could change them while this screen was alive; now
    // finishing a round does, and a card still urging you to keep a streak you just kept is
    // worse than no card.
    val revision = OrbitPrefs.revision
    val dayIndex = OrbitPrefs.dayIndex
    val streak = remember(revision, dayIndex) { OrbitPrefs.streakState }
    val doneToday = remember(revision, dayIndex) { OrbitPrefs.isDailyQuizDone(dayIndex) }
    val offer = remember(revision, dayIndex) { OrbitPrefs.repairOffer() }
    var repairOpen by remember { mutableStateOf(false) }
    var repaired by remember { mutableStateOf(0) }
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp),
    ) {
        Column(
            Modifier
                .statusBarsPadding()
                .padding(start = 22.dp, end = 22.dp, top = 28.dp, bottom = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("SPACE FACTS", style = ts(12f, FontWeight.Bold, Mute, 0.34f))
                    Text("Star systems", style = ts(34f, FontWeight.Bold, Color.White, -0.02f), modifier = Modifier.padding(top = 6.dp))
                }
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable(onClick = onOpenSearch),
                    contentAlignment = Alignment.Center,
                ) { Ico("search", size = 20.dp, color = Color.White, sw = 2f) }
            }
            Text(
                "Worlds beyond worlds — explored one system at a time.",
                style = ts(15f, FontWeight.Light, Mute, lineHeight = 22f),
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        today?.let { TodayCard(fact = it, onClick = { onOpenFact(it.id) }) }

        // A card rather than a dialog on launch: an offer that opens itself one tap from a
        // video reads as an ad nobody asked for, whatever it is offering.
        if (repaired > 0) {
            StreakRestoredCard(streak = repaired)
        } else {
            offer?.let { StreakRepairCard(lost = it.lostStreak, onClick = { repairOpen = true }) }
        }

        QuizCard(
            streak = streak,
            today = dayIndex,
            doneToday = doneToday,
            onClick = onOpenQuiz,
        )

        FEATURED_SYSTEMS.forEach { sys ->
            val inSys = factsForSys(sys.sysId)
            FeaturedCard(
                sys = sys,
                factCount = inSys.size,
                seenCount = inSys.count { it.id in viewed },
                onClick = { onOpenSystem(sys.sysId) },
            )
        }

        Row(
            Modifier.padding(start = 22.dp, end = 22.dp, top = 30.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("MORE SYSTEMS", style = ts(12f, FontWeight.Bold, Mute, 0.24f))
            Spacer(Modifier.width(14.dp))
            Box(Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.10f)))
        }
        SYSTEMS.forEach { s ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 15.dp).alpha(0.8f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Sphere(sizeUnits = 44f, colors = s.color, glow = s.color[1].copy(alpha = 0.4f))
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(s.name, style = ts(16f, FontWeight.SemiBold, Color.White))
                    Text("${s.dist} · ${s.desc}", style = ts(12.5f, color = Mute), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
                }
                Spacer(Modifier.width(10.dp))
                Row(
                    Modifier.clip(RoundedCornerShape(100)).border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(100)).padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Ico("lock", size = 12.dp, color = Mute, sw = 2f)
                    Spacer(Modifier.width(5.dp))
                    Text("SOON", style = ts(11f, FontWeight.SemiBold, Mute, 0.08f))
                }
            }
        }
        Text(
            "More systems are charted and added over time.",
            style = ts(12.5f, color = Color(0xFF5A5A5A), lineHeight = 19f),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 22.dp),
        )
    }

    LaunchedEffect(offer?.lostStreak) {
        offer?.let { Analytics.streakRepairOffered(it.lostStreak) }
    }

    if (repairOpen && offer != null) {
        RewardPrompt(
            title = "Restore your ${offer.lostStreak}-day streak",
            body = "It picks up where it left off, as though yesterday had counted.",
            cta = "Restore my streak",
            placement = Analytics.Placement.STREAK_REPAIR,
            accent = StreakAccent,
            onGranted = { method ->
                repaired = OrbitPrefs.repairStreak()
                repairOpen = false
                Analytics.streakRepaired(repaired, method)
            },
            onDismiss = { repairOpen = false },
            // Our own fill failure must not cost someone a month-long run. After a couple of
            // honest attempts the streak is simply given back; the grant is logged as a
            // fallback so the size of the leak stays visible.
            onFailed = { OrbitPrefs.noteRepairFailure() },
        )
    }
}

/** The receipt for a repair, so watching a video visibly bought something. */
@Composable
private fun StreakRestoredCard(streak: Int) {
    Row(
        Modifier
            .padding(start = 22.dp, end = 22.dp, top = 14.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Right.copy(alpha = 0.10f))
            .border(1.dp, Right.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ico("check", size = 20.dp, color = Right, sw = 2.4f)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("Streak restored", style = ts(16f, FontWeight.Bold, Color.White))
            Text(
                "You are back to $streak days. Keep it going tomorrow.",
                style = ts(12.5f, color = Mute),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/**
 * The way into the quiz, and the only place on the home screen the streak is visible.
 *
 * Still a row rather than another big card — it sits between the daily fact and the system
 * library without competing with either — but the subtitle now carries the reason to tap:
 * either a run that is still open today, or the score that closed it.
 */
@Composable
private fun QuizCard(
    streak: Streak,
    today: Long,
    doneToday: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .padding(start = 22.dp, end = 22.dp, top = 14.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(QuizAccent.copy(alpha = 0.16f), StreakAccent.copy(alpha = 0.05f)),
                ),
            )
            .border(1.dp, QuizAccent.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ico("spotlight", size = 26.dp, color = QuizAccent, filled = true)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("Daily Quiz", style = ts(18f, FontWeight.Bold, Color.White))
            Text(
                when {
                    doneToday -> "Done today · ${OrbitPrefs.dailyQuizScore} of ${Quiz.ROUND_SIZE}"
                    streak.current > 1 -> "Ten questions · keep your ${streak.current}-day streak"
                    else -> "Ten questions · the same for everyone today"
                },
                style = ts(13f, color = Color(0xFFC9A98A)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
            if (streak.current > 0) {
                WeekDots(streak.week(today), dot = 6.dp, gap = 5.dp, modifier = Modifier.padding(top = 8.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        if (doneToday) {
            Box(
                Modifier.size(26.dp).clip(CircleShape).background(Right.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) { Ico("check", size = 14.dp, color = Right, sw = 2.6f) }
        } else {
            Ico("chevR", size = 18.dp, color = QuizAccent)
        }
    }
}

/**
 * Shown only on the day a run of three or more is lost to a single missed day — see
 * [com.orbit.starsystems.core.Perks] for why those are the limits.
 */
@Composable
private fun StreakRepairCard(lost: Int, onClick: () -> Unit) {
    Row(
        Modifier
            .padding(start = 22.dp, end = 22.dp, top = 14.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(StreakAccent.copy(alpha = 0.10f))
            .border(1.dp, StreakAccent.copy(alpha = 0.30f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ico("bolt", size = 24.dp, color = StreakAccent, filled = true)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("Your $lost-day streak ended", style = ts(16f, FontWeight.Bold, Color.White))
            Text(
                "You missed yesterday — you can still get it back today.",
                style = ts(12.5f, color = Color(0xFFC9A98A)),
                maxLines = 2,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Ico("chevR", size = 18.dp, color = StreakAccent)
    }
}

@Composable
private fun TodayCard(fact: Fact, onClick: () -> Unit) {
    Box(Modifier.padding(start = 22.dp, end = 22.dp, top = 16.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(178.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, fact.accent.copy(alpha = 0.32f), RoundedCornerShape(22.dp))
                .clickable(onClick = onClick),
        ) {
            MiniStage(fact.scene, fact.dur, fact.hero, active = false, paused = true, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.3f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.88f))))
            Box(Modifier.align(Alignment.TopStart).padding(14.dp)) {
                CategoryPill("Today", fact.accent, filledBg = fact.accent.copy(alpha = 0.16f))
            }
            Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, end = 16.dp, bottom = 15.dp)) {
                Text(fact.title, style = ts(26f, FontWeight.Bold, Color.White, -0.02f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    fact.sub,
                    style = ts(13.5f, FontWeight.Medium, Color(0xFFD4D4D4)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun FeaturedCard(
    sys: FeaturedSystem,
    factCount: Int,
    seenCount: Int,
    onClick: () -> Unit,
) {
    val scene: SceneId = sys.scene
    val dur = sys.dur
    val hero = sys.hero
    val pill = sys.pill
    val pillColor = sys.pillColor
    val title = sys.title
    val subtitle = when {
        seenCount == 0 -> "${sys.tagline} · $factCount facts"
        seenCount >= factCount -> "${sys.tagline} · all $factCount explored"
        else -> "${sys.tagline} · $seenCount of $factCount explored"
    }
    val progress = if (factCount == 0) 0f else seenCount.toFloat() / factCount
    Box(Modifier.padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 2.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
                .clickable(onClick = onClick),
        ) {
            MiniStage(scene, dur, hero, active = false, paused = true, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.38f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.86f))))
            Box(Modifier.align(Alignment.TopStart).padding(14.dp)) {
                CategoryPill(pill, pillColor, filledBg = pillColor.copy(alpha = 0.16f))
            }
            if (seenCount > 0) {
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.14f)),
                ) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(progress).background(sys.pillColor))
                }
            }
            Row(
                Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(title, style = ts(34f, FontWeight.Bold, Color.White, -0.02f))
                    Text(subtitle, style = ts(13.5f, FontWeight.Medium, Color(0xFFD4D4D4)), modifier = Modifier.padding(top = 6.dp))
                }
                Row(
                    Modifier.clip(RoundedCornerShape(100)).background(Color.White.copy(alpha = 0.16f)).padding(horizontal = 13.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Explore", style = ts(13f, FontWeight.SemiBold, Color.White))
                    Spacer(Modifier.width(5.dp))
                    Ico("chevR", size = 15.dp, color = Color.White, sw = 2.2f)
                }
            }
        }
    }
}
