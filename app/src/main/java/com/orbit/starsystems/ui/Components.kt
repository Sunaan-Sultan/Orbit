package com.orbit.starsystems.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.orbit.starsystems.core.Fact
import com.orbit.starsystems.core.SceneId
import com.orbit.starsystems.scenes.RenderScene

/**
 * Plays a single scene on its own looping clock. When [active] flips on it restarts
 * from 0; when it goes inactive it freezes on the [hero] frame (the most photogenic
 * moment), so cards show a meaningful still.
 */
@Composable
fun MiniStage(
    scene: SceneId,
    dur: Float,
    hero: Float,
    active: Boolean,
    paused: Boolean,
    modifier: Modifier = Modifier,
    cover: Boolean = true,
) {
    var time by remember { mutableFloatStateOf(hero) }
    LaunchedEffect(active) { time = if (active) 0f else hero }
    LaunchedEffect(active, paused, dur) {
        if (!active || paused) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = (now - last) / 1_000_000_000f
            last = now
            time = (time + dt).let { if (it >= dur) it % dur else it }
        }
    }
    SceneCanvas(modifier, cover = cover) {
        Starfield(time)
        Vignette()
        RenderScene(scene, time, dur)
    }
}

/** Accent-coloured category chip with a leading dot. */
@Composable
fun CategoryPill(label: String, accent: Color, filledBg: Color? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(100))
            .background(filledBg ?: Color.Transparent)
            .border(1.dp, accent, RoundedCornerShape(100))
            .padding(horizontal = 11.dp, vertical = 5.dp),
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(accent))
        Spacer(Modifier.width(6.dp))
        Text(
            label.uppercase(),
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                letterSpacing = 0.12.em, color = accent,
            ),
        )
    }
}

/** A thumbnail card showing a frozen scene with a title overlay (Explore / Saved). */
@Composable
fun FactCard(fact: Fact, onClick: () -> Unit, modifier: Modifier = Modifier, wide: Boolean = false) {
    Box(
        modifier
            .aspectRatio(if (wide) 16f / 10f else 3f / 4f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        MiniStage(fact.scene, fact.dur, fact.hero, active = false, paused = true, modifier = Modifier.fillMaxSize())
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0.45f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.85f))),
        )
        Column(Modifier.align(Alignment.BottomStart).padding(start = 12.dp, end = 12.dp, bottom = 11.dp)) {
            Text(
                fact.cat.uppercase(),
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp,
                    letterSpacing = 0.10.em, color = fact.accent,
                ),
            )
            Text(
                fact.title,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                    letterSpacing = (-0.01).em, color = Color.White,
                ),
            )
        }
    }
}

/** Translucent bottom navigation with three destinations. */
@Composable
fun BottomNav(tab: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val items = listOf("systems" to "Systems", "spotlight" to "Spotlight", "compare" to "Compare", "saved" to "Saved", "you" to "You")
    Row(
        modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF08080A).copy(alpha = 0.92f))
            .border(width = 0.dp, color = Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { (id, label) ->
            val on = tab == id
            val color = if (on) Color.White else Color(0xFF6A6A6A)
            val icon = if (id == "you") "profile" else id
            Column(
                Modifier
                    .weight(1f)
                    .clickable { onSelect(id) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Ico(icon, size = 24.dp, color = color, filled = on && id == "saved")
                Spacer(Modifier.height(3.dp))
                Text(
                    label,
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = OrbitFont, fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 11.sp, color = color,
                    ),
                )
            }
        }
    }
}
