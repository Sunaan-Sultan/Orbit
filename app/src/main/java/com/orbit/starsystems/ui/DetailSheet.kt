package com.orbit.starsystems.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.orbit.starsystems.core.ALL_FACTS
import com.orbit.starsystems.core.Fact

@Composable
fun DetailSheet(
    fact: Fact,
    open: Boolean,
    onClose: () -> Unit,
    onJump: (String) -> Unit,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
) {
    // Related facts: prefer same system + category, then same system, then anything.
    val related = remember(fact.id) {
        val sameSys = ALL_FACTS.filter { it.sys == fact.sys && it.id != fact.id }
        (sameSys.filter { it.cat == fact.cat } + sameSys.filter { it.cat != fact.cat } +
            ALL_FACTS.filter { it.sys != fact.sys }).distinct().take(3)
    }
    val scrim by animateFloatAsState(if (open) 1f else 0f, tween(300), label = "scrim")
    val slide by animateFloatAsState(if (open) 0f else 1f, tween(420), label = "slide")

    if (scrim <= 0.001f && !open) return

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val sheetHeightDp = maxHeight * 0.78f
        val sheetPx = with(LocalDensity.current) { sheetHeightDp.toPx() }
        Box(
            Modifier
                .fillMaxSize()
                .alpha(scrim * 0.5f)
                .background(Color.Black)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClose() },
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
                Box(
                    Modifier.align(Alignment.TopEnd).padding(end = 14.dp).size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)).clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) { Ico("close", size = 18.dp, color = Color.White, sw = 2f) }
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
                        Modifier.fillMaxWidth().padding(vertical = 14.dp),
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
                Text("Source · NASA / ESA", style = ts(12f, color = Color(0xFF5A5A5A)), modifier = Modifier.padding(top = 16.dp))
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
