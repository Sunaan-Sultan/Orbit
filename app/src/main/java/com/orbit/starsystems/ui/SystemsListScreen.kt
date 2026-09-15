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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.orbit.starsystems.core.DailyFact
import com.orbit.starsystems.core.OrbitPrefs
import com.orbit.starsystems.core.Quiz
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

        QuizCard(onClick = onOpenQuiz)

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
}

/**
 * The way into the quiz. A row rather than another big card: it sits between the daily fact and
 * the system library without competing with either for the eye.
 */
@Composable
private fun QuizCard(onClick: () -> Unit) {
    val best = OrbitPrefs.quizBest
    val played = OrbitPrefs.quizRounds
    Row(
        Modifier
            .padding(start = 22.dp, end = 22.dp, top = 14.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFFFC24D).copy(alpha = 0.16f), Color(0xFFFF9E34).copy(alpha = 0.05f))))
            .border(1.dp, Color(0xFFFFC24D).copy(alpha = 0.28f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ico("spotlight", size = 26.dp, color = Color(0xFFFFC24D), filled = true)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("Cosmic Quiz", style = ts(18f, FontWeight.Bold, Color.White))
            Text(
                if (played == 0) {
                    "Ten questions from the facts you've been reading"
                } else {
                    "Best so far · $best out of ${Quiz.ROUND_SIZE}"
                },
                style = ts(13f, color = Color(0xFFC9A98A)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Ico("chevR", size = 18.dp, color = Color(0xFFFFC24D))
    }
}

/**
 * The one fact the app leads with today, above the system library. Deliberately shorter
 * than a [FeaturedCard] so it reads as a daily pick rather than a twelfth system.
 */
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
