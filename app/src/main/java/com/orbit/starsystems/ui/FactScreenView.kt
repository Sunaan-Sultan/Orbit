package com.orbit.starsystems.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.orbit.starsystems.core.Fact

/**
 * Full-screen looping fact player. Trimmed to just the subtitle + "Learn more" —
 * the top bar (back / save) and the title/category pill are intentionally omitted.
 * System back exits the player (handled by OrbitApp's BackHandler).
 */
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
    Box(
        Modifier.fillMaxSize().background(Color.Black).clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
        ) { onTogglePause() },
    ) {
        MiniStage(fact.scene, fact.dur, fact.hero, active = true, paused = paused, modifier = Modifier.fillMaxSize(), cover = false)

        if (paused) {
            Box(
                Modifier.align(Alignment.Center).size(74.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) { Ico("play", size = 30.dp, color = Color.White) }
        }

        // caption + learn more (taps here don't toggle play)
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
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
