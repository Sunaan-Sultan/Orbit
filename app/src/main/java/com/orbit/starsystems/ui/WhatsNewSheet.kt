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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.unit.dp
import com.orbit.starsystems.core.FEATURED_SYSTEMS
import com.orbit.starsystems.core.factsForSys

/**
 * The release notes shown once after an update. Edit this per release — the system ids
 * are looked up in the catalog, so the cards stay in step with the content in assets/.
 */
private object Release {
    const val HEADLINE = "A new neighbour, and five new wonders"
    val NEW_SYSTEMS = listOf("teeg")
    val ALSO = listOf(
        "Five new Spotlight facts: Titan, the Oort Cloud, gravitational waves, ʻOumuamua and the oldest light in the universe.",
        "Fewer interruptions — full-screen ads are far rarer, and Remove ads in the You tab turns them off for good.",
        "Facts no longer skip ahead behind an ad: the animation and music now pause and pick up exactly where they left off.",
    )
}

/**
 * "What's new" bottom sheet. Mirrors [DetailSheet]'s scrim-and-slide structure so the two
 * feel like the same surface; tapping a system closes the sheet and opens it.
 */
@Composable
fun WhatsNewSheet(open: Boolean, onClose: () -> Unit, onOpenSystem: (String) -> Unit) {
    val scrim by animateFloatAsState(if (open) 1f else 0f, tween(300), label = "wnScrim")
    val slide by animateFloatAsState(if (open) 0f else 1f, tween(420), label = "wnSlide")

    if (scrim <= 0.001f && !open) return

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val sheetHeightDp = maxHeight * 0.70f
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
                .border(1.dp, Color(0xFFFF9E34).copy(alpha = 0.33f), RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
        ) {
            Box(Modifier.fillMaxWidth().padding(top = 10.dp), contentAlignment = Alignment.TopCenter) {
                Box(Modifier.width(44.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.25f)))
                Box(
                    Modifier.align(Alignment.TopEnd).padding(end = 14.dp).size(34.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f)).clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) { Ico("close", size = 18.dp, color = Color.White, sw = 2f) }
            }
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 24.dp),
            ) {
                Text("WHAT'S NEW", style = ts(12f, FontWeight.SemiBold, Color(0xFFFF9E34), 0.12f))
                Text(
                    Release.HEADLINE,
                    style = ts(32f, FontWeight.Bold, Color.White, -0.02f, lineHeight = 36f),
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    if (Release.NEW_SYSTEMS.isEmpty()) {
                        "Here's what changed in this update."
                    } else {
                        "Freshly charted and ready to explore."
                    },
                    style = ts(15f, FontWeight.Light, Color(0xFFCFCFCF), lineHeight = 24f),
                    modifier = Modifier.padding(top = 10.dp),
                )
                if (Release.NEW_SYSTEMS.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Release.NEW_SYSTEMS.forEach { id -> NewSystemCard(id, onOpen = { onOpenSystem(id) }) }
                    }
                }
                if (Release.ALSO.isNotEmpty()) {
                    Text(
                        if (Release.NEW_SYSTEMS.isEmpty()) "IN THIS UPDATE" else "ALSO IN THIS UPDATE",
                        style = ts(12f, FontWeight.SemiBold, Dim, 0.12f),
                        modifier = Modifier.padding(top = 26.dp, bottom = 10.dp),
                    )
                    Release.ALSO.forEach { line ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                            Text("·", style = ts(14.5f, FontWeight.Bold, Color(0xFFFF9E34)))
                            Spacer(Modifier.width(10.dp))
                            Text(line, style = ts(14f, color = Mute, lineHeight = 21f))
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable(onClick = onClose)
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Start exploring", style = ts(15f, FontWeight.SemiBold, Color.Black))
                }
            }
        }
    }
}

/** One new system, previewed with its own featured scene frozen on its hero frame. */
@Composable
private fun NewSystemCard(sysId: String, onOpen: () -> Unit) {
    val sys = FEATURED_SYSTEMS.find { it.sysId == sysId } ?: return
    val facts = remember(sysId) { factsForSys(sysId).size }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, sys.pillColor.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen)
            .padding(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(width = 76.dp, height = 76.dp).clip(RoundedCornerShape(10.dp))) {
            MiniStage(sys.scene, sys.dur, sys.hero, active = false, paused = true, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(sys.pill.uppercase(), style = ts(10.5f, FontWeight.SemiBold, sys.pillColor, 0.1f))
            Text(sys.title, style = ts(17f, FontWeight.Bold, Color.White), modifier = Modifier.padding(top = 2.dp))
            Text("${sys.tagline} · $facts facts", style = ts(12.5f, color = Mute), modifier = Modifier.padding(top = 2.dp))
        }
        Ico("chevR", size = 17.dp, color = sys.pillColor)
    }
}
