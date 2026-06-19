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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.orbit.starsystems.core.FACTS
import com.orbit.starsystems.core.FACT_CATEGORIES
import com.orbit.starsystems.core.Fact
import com.orbit.starsystems.core.SYSTEMS
import com.orbit.starsystems.core.SceneId

private fun ts(
    size: Float,
    weight: FontWeight = FontWeight.Normal,
    color: Color = Color.White,
    spacingEm: Float = 0f,
    lineHeight: Float = 0f,
) = TextStyle(
    fontFamily = OrbitFont, fontSize = size.sp, fontWeight = weight, color = color,
    letterSpacing = spacingEm.em,
    lineHeight = if (lineHeight > 0f) lineHeight.sp else TextStyle.Default.lineHeight,
)

private val Mute = Color(0xFF8A8A8A)
private val Dim = Color(0xFF7A7A7A)

// ───────────────────────── Systems (home) ─────────────────────────

@Composable
fun SystemsList(onOpenSol: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp),
    ) {
        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 6.dp)) {
            Text("ORBIT", style = ts(13f, FontWeight.SemiBold, Dim, 0.16f))
            Text("Star systems", style = ts(33f, FontWeight.Bold, Color.White, -0.02f), modifier = Modifier.padding(top = 2.dp))
            Text(
                "Worlds beyond worlds — explored one system at a time.",
                style = ts(15f, FontWeight.Light, Mute, lineHeight = 22f),
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        // Featured: Sol
        Box(Modifier.padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 2.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                    .clickable(onClick = onOpenSol),
            ) {
                MiniStage(SceneId.SUN, 9.4f, 6.6f, active = false, paused = true, modifier = Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.38f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.86f))))
                Box(Modifier.align(Alignment.TopStart).padding(14.dp)) {
                    CategoryPill("Our system", Color(0xFFFF9E34), filledBg = Color(0xFFFF9E34).copy(alpha = 0.16f))
                }
                Row(
                    Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Text("Sol", style = ts(34f, FontWeight.Bold, Color.White, -0.02f))
                        Text("The Solar System · ${FACTS.size} facts", style = ts(14.5f, FontWeight.Light, Color(0xFFD4D4D4)), modifier = Modifier.padding(top = 4.dp))
                    }
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(100))
                            .background(Color.White.copy(alpha = 0.16f))
                            .padding(horizontal = 13.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Explore", style = ts(13f, FontWeight.SemiBold, Color.White))
                        Spacer(Modifier.width(5.dp))
                        Ico("chevR", size = 15.dp, color = Color.White, sw = 2.2f)
                    }
                }
            }
        }

        // Beyond Sol
        Text(
            "BEYOND SOL",
            style = ts(13f, FontWeight.SemiBold, Dim, 0.10f),
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 4.dp),
        )
        SYSTEMS.forEach { s ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .border(width = 0.dp, color = Color.Transparent)
                    .padding(horizontal = 18.dp, vertical = 13.dp)
                    .alpha(0.7f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Sphere(sizeUnits = 42f, colors = s.color, glow = s.color[1].copy(alpha = 0.4f))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(s.name, style = ts(16f, FontWeight.SemiBold, Color.White))
                    Text("${s.dist} · ${s.desc}", style = ts(13f, color = Mute), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(10.dp))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(100))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(100))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
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

// ───────────────────────── Sol Explore ─────────────────────────

@Composable
fun SystemExplore(onOpenFact: (String) -> Unit, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.78f))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { Ico("back", size = 20.dp, color = Color.White, sw = 2.1f) }
            Spacer(Modifier.width(12.dp))
            Text("Sol", style = ts(18f, FontWeight.SemiBold, Color.White))
        }
        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 6.dp)) {
            Text("OUR SYSTEM", style = ts(13f, FontWeight.SemiBold, Color(0xFFFF9E34), 0.14f))
            Text("The Solar System", style = ts(32f, FontWeight.Bold, Color.White, -0.02f), modifier = Modifier.padding(top = 2.dp))
            Text("The Sun and its eight worlds — tap any fact to watch it unfold.", style = ts(15f, FontWeight.Light, Mute, lineHeight = 22f), modifier = Modifier.padding(top = 6.dp))
        }
        FACT_CATEGORIES.forEach { cat ->
            val inCat = FACTS.filter { it.cat == cat }
            Row(
                Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(cat, style = ts(18f, FontWeight.SemiBold, Color.White))
                Text("${inCat.size} ${if (inCat.size == 1) "fact" else "facts"}", style = ts(13f, color = Color(0xFF6A6A6A)))
            }
            // grid of cards (2 columns)
            val rows = inCat.chunked(2)
            Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { f ->
                            FactCard(f, onClick = { onOpenFact(f.id) }, modifier = Modifier.weight(1f))
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ───────────────────────── Saved ─────────────────────────

@Composable
fun SavedScreen(saved: Set<String>, onOpen: (String) -> Unit) {
    val items = FACTS.filter { saved.contains(it.id) }
    Column(Modifier.fillMaxSize().background(Color.Black).padding(bottom = 80.dp)) {
        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 14.dp)) {
            Text("YOUR COLLECTION", style = ts(13f, FontWeight.SemiBold, Dim, 0.14f))
            Text("Saved", style = ts(32f, FontWeight.Bold, Color.White, -0.02f), modifier = Modifier.padding(top = 2.dp))
        }
        if (items.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 70.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)), contentAlignment = Alignment.Center) {
                    Ico("saved", size = 28.dp, color = Color(0xFF6A6A6A))
                }
                Spacer(Modifier.height(18.dp))
                Text("Nothing saved yet", style = ts(17f, FontWeight.SemiBold, Color(0xFFCFCFCF)))
                Text("Tap the bookmark on any fact to keep it here.", style = ts(14f, color = Dim, lineHeight = 21f), modifier = Modifier.padding(top = 6.dp))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items.size) { i -> FactCard(items[i], onClick = { onOpen(items[i].id) }) }
            }
        }
    }
}

// ───────────────────────── Profile ─────────────────────────

@Composable
fun ProfileScreen(savedCount: Int, viewed: Int) {
    Column(
        Modifier.fillMaxSize().background(Color.Black).verticalScroll(rememberScrollState()).padding(bottom = 80.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(top = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Sphere(sizeUnits = 84f, colors = listOf(Color(0xFF9CC4EC), Color(0xFF3D72B8), Color(0xFF1A3360)), glow = Color(0xFF508CD2).copy(alpha = 0.4f))
            Text("Stargazer", style = ts(22f, FontWeight.Bold, Color.White), modifier = Modifier.padding(top = 14.dp))
            Text("Exploring since today", style = ts(14f, color = Dim), modifier = Modifier.padding(top = 2.dp))
        }
        Row(
            Modifier
                .padding(18.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
                .padding(vertical = 18.dp),
        ) {
            ProfileStat(viewed.toString(), "Facts seen")
            ProfileStat("1", "Systems")
            ProfileStat(savedCount.toString(), "Saved")
        }
        Row(
            Modifier
                .padding(horizontal = 18.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFE0744A).copy(alpha = 0.18f), Color(0xFFFF9E34).copy(alpha = 0.06f))))
                .border(1.dp, Color(0xFFFF9E34).copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Ico("bolt", size = 30.dp, color = Color(0xFFFF9E34), filled = true)
            Spacer(Modifier.width(14.dp))
            Column {
                Text("3-day streak", style = ts(19f, FontWeight.Bold, Color.White))
                Text("Come back tomorrow for a new fact.", style = ts(13.5f, color = Color(0xFFC9A98A)), modifier = Modifier.padding(top = 1.dp))
            }
        }
        Text(
            "A daily window onto the cosmos.",
            style = ts(13f, color = Color(0xFF5A5A5A)),
            modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ProfileStat(n: String, label: String) {
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(n, style = ts(30f, FontWeight.Bold, Color.White))
        Text(label, style = ts(12f, color = Dim), modifier = Modifier.padding(top = 2.dp))
    }
}

// ───────────────────────── Fact player ─────────────────────────

@Composable
fun FactScreen(
    fact: Fact,
    paused: Boolean,
    onTogglePause: () -> Unit,
    onBack: () -> Unit,
    onLearn: () -> Unit,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(Color.Black).clickable(
        indication = null,
        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
    ) { onTogglePause() }) {
        MiniStage(fact.scene, fact.dur, fact.hero, active = true, paused = paused, modifier = Modifier.fillMaxSize(), cover = false)

        if (paused) {
            Box(
                Modifier.align(Alignment.Center).size(74.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) { Ico("play", size = 30.dp, color = Color.White) }
        }

        // top bar
        Row(
            Modifier.align(Alignment.TopStart).fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            RoundButton(onClick = onBack) { Ico("back", size = 22.dp, color = Color.White, sw = 2.1f) }
            RoundButton(onClick = onToggleSave) {
                Ico("saved", size = 20.dp, color = if (isSaved) fact.accent else Color.White, filled = isSaved)
            }
        }

        // caption + learn more (taps here don't toggle play)
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                ) {},
        ) {
            Text(fact.sub, style = ts(16f, FontWeight.Light, Color(0xFFD4D4D4)))
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier
                    .clip(RoundedCornerShape(100))
                    .background(Color.White.copy(alpha = 0.14f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(100))
                    .clickable(onClick = onLearn)
                    .padding(horizontal = 18.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Learn more", style = ts(14.5f, FontWeight.SemiBold, Color.White))
                Spacer(Modifier.width(8.dp))
                Ico("chevUp", size = 16.dp, color = Color.White, sw = 2.2f)
            }
        }
    }
}

@Composable
private fun RoundButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.42f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

// ───────────────────────── Detail sheet ─────────────────────────

@Composable
fun DetailSheet(
    fact: Fact,
    open: Boolean,
    onClose: () -> Unit,
    onJump: (String) -> Unit,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
) {
    val related = remember(fact.id) {
        (FACTS.filter { it.cat == fact.cat && it.id != fact.id } + FACTS.filter { it.cat != fact.cat }).take(3)
    }
    val scrim by animateFloatAsState(if (open) 1f else 0f, tween(300), label = "scrim")
    val slide by animateFloatAsState(if (open) 0f else 1f, tween(420), label = "slide")

    if (scrim <= 0.001f && !open) return

    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
        val sheetHeightDp = maxHeight * 0.78f
        val sheetPx = with(LocalDensity.current) { sheetHeightDp.toPx() }
        // scrim
        Box(
            Modifier
                .fillMaxSize()
                .alpha(scrim * 0.5f)
                .background(Color.Black)
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                ) { onClose() },
        )
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(sheetHeightDp)
                .graphicsLayer { translationY = slide * sheetPx }
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(Color(0xFF0C0C0F))
                .border(1.dp, fact.accent.copy(alpha = 0.33f), RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
        ) {
            Box(Modifier.fillMaxWidth().padding(top = 10.dp), contentAlignment = Alignment.TopCenter) {
                Box(Modifier.width(44.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.25f)))
                Box(Modifier.align(Alignment.TopEnd).padding(end = 14.dp).size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)).clickable(onClick = onClose), contentAlignment = Alignment.Center) {
                    Ico("close", size = 18.dp, color = Color.White, sw = 2f)
                }
            }
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 24.dp),
            ) {
                Text(fact.cat.uppercase(), style = ts(12f, FontWeight.SemiBold, fact.accent, 0.12f))
                Text(fact.title, style = ts(34f, FontWeight.Bold, Color.White, -0.02f, lineHeight = 36f), modifier = Modifier.padding(top = 6.dp))
                Text(fact.blurb, style = ts(16.5f, FontWeight.Light, Color(0xFFCFCFCF), lineHeight = 26f), modifier = Modifier.padding(top = 14.dp))
                Spacer(Modifier.height(22.dp))
                fact.stats.forEach { (k, v) ->
                    Row(
                        Modifier.fillMaxWidth().border(width = 0.dp, color = Color.Transparent).padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(k, style = ts(14f, color = Mute))
                        Text(v, style = ts(17f, FontWeight.SemiBold, Color.White))
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                }
                Spacer(Modifier.height(22.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSaved) fact.accent else Color.White.copy(alpha = 0.10f))
                        .clickable(onClick = onToggleSave)
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (isSaved) "Saved ✓" else "Save fact", style = ts(15f, FontWeight.SemiBold, if (isSaved) Color.Black else Color.White))
                }
                Text("Source · NASA Solar System Exploration", style = ts(12f, color = Color(0xFF5A5A5A)), modifier = Modifier.padding(top = 16.dp))
                Text("KEEP EXPLORING", style = ts(13f, FontWeight.SemiBold, Dim, 0.08f), modifier = Modifier.padding(top = 26.dp, bottom = 12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    related.forEach { r ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .clickable { onJump(r.id) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(width = 54.dp, height = 72.dp).clip(RoundedCornerShape(9.dp))) {
                                MiniStage(r.scene, r.dur, r.hero, active = false, paused = true, modifier = Modifier.fillMaxSize())
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(r.title, style = ts(15f, FontWeight.SemiBold, Color.White))
                                Text(r.sub, style = ts(13f, color = Mute), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text("›", style = ts(18f, color = r.accent))
                        }
                    }
                }
            }
        }
    }
}
