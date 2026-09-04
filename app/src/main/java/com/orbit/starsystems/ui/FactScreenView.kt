package com.orbit.starsystems.ui

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    isActive: Boolean,
    onTogglePause: () -> Unit,
    onBack: () -> Unit,
    onLearn: () -> Unit,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mp = remember(fact.id, isActive) {
        if (!isActive) return@remember null
        val resId = fact.musicResId ?: return@remember null
        MediaPlayer.create(context, resId).apply { isLooping = true }
    }

    DisposableEffect(mp, lifecycleOwner) {
        if (mp == null) return@DisposableEffect onDispose {}

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> mp.pause()
                Lifecycle.Event.ON_RESUME -> if (!paused) mp.start()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mp.stop()
            mp.release()
        }
    }

    LaunchedEffect(mp, paused) {
        if (paused) mp?.pause() else mp?.start()
    }

    // Looping clock (0 → dur) driving the top progress bar; restarts per fact, only ticks while active.
    var clock by remember(fact.id) { mutableFloatStateOf(0f) }
    LaunchedEffect(fact.id, isActive, paused, fact.dur) {
        if (!isActive || paused) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            clock = (clock + (now - last) / 1_000_000_000f).let { if (it >= fact.dur) it % fact.dur else it }
            last = now
        }
    }
    val progress = (clock / fact.dur).coerceIn(0f, 1f)

    Box(
        Modifier.fillMaxSize().background(Color.Black).clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
        ) { onTogglePause() },
    ) {
        MiniStage(fact.scene, fact.dur, fact.hero, active = isActive, paused = paused, modifier = Modifier.fillMaxSize(), cover = false)

        // Thin progress line across the top, filling in the fact's accent colour.
        Box(
            Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.White.copy(alpha = 0.14f)),
        ) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(progress).background(fact.accent))
        }

        if (paused) {
            Box(
                Modifier.align(Alignment.Center).size(74.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) { Ico("play", size = 30.dp, color = Color.White) }
        }

        // Scrim so the caption + button stay readable over bright scenes (e.g. the Sun).
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(320.dp)
                .background(Brush.verticalGradient(0f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.72f))),
        )

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
