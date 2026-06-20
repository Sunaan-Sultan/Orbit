package com.orbit.starsystems.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import com.orbit.starsystems.core.AC_FACTS
import com.orbit.starsystems.core.SOL_FACTS
import com.orbit.starsystems.core.SYSTEMS
import com.orbit.starsystems.core.SceneId

@Composable
fun SystemsList(onOpenSol: () -> Unit, onOpenAcen: () -> Unit) {
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
            Text("ORBIT", style = ts(12f, FontWeight.Bold, Mute, 0.34f))
            Text("Star systems", style = ts(34f, FontWeight.Bold, Color.White, -0.02f), modifier = Modifier.padding(top = 6.dp))
            Text(
                "Worlds beyond worlds — explored one system at a time.",
                style = ts(15f, FontWeight.Light, Mute, lineHeight = 22f),
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        FeaturedSystem(
            scene = SceneId.SUN, dur = 9.4f, hero = 6.6f,
            pill = "Our system", pillColor = Color(0xFFFF9E34),
            title = "Sol", subtitle = "The Solar System · ${SOL_FACTS.size} facts",
            onClick = onOpenSol,
        )
        FeaturedSystem(
            scene = SceneId.AC_TRIPLE, dur = 9.4f, hero = 4.8f,
            pill = "Nearest neighbour", pillColor = Color(0xFFFFCF8A),
            title = "Alpha Centauri", subtitle = "Triple-star system · ${AC_FACTS.size} facts",
            onClick = onOpenAcen,
        )

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

@Composable
private fun FeaturedSystem(
    scene: SceneId,
    dur: Float,
    hero: Float,
    pill: String,
    pillColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
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
