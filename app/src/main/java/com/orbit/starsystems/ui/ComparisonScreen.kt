package com.orbit.starsystems.ui

import android.media.MediaPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.orbit.starsystems.AdManager
import com.orbit.starsystems.R
import com.orbit.starsystems.core.CompKind
import com.orbit.starsystems.core.OrbitData
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// ───────────────────────── the objects, smallest → largest ─────────────────────────
// Loaded from assets/comparison.json by OrbitData; CompKind / CompObj live in core/Models.

private val COMP_OBJECTS = OrbitData.compObjects

private fun cc(v: Long) = Color(v or 0xFF000000)

// how many previous objects to keep in frame alongside the current (largest) one
private const val LINEUP_K = 4

// Cumulative world-space centres (km). Objects sit edge-to-edge with a small gap
// scaled to the SMALLER neighbour, so tiny worlds nestle right up against the giants
// and stay visible beside them instead of being flung far off in world space.
private val COMP_X: DoubleArray = run {
    val n = COMP_OBJECTS.size
    val x = DoubleArray(n)
    for (i in 1 until n) {
        val rPrev = COMP_OBJECTS[i - 1].diaKm / 2.0
        val rCur = COMP_OBJECTS[i].diaKm / 2.0
        x[i] = x[i - 1] + rPrev + 0.5 * min(rPrev, rCur) + rCur
    }
    x
}

private fun objR(i: Int): Double = COMP_OBJECTS[i].diaKm / 2.0

// The frame is anchored on the lead (largest, right-most) object and stretches back
// to take in its previous LINEUP_K neighbours.
private fun leadRight(l: Int): Double = COMP_X[l] + objR(l)
private fun backLeft(l: Int): Double {
    val b = (l - LINEUP_K).coerceAtLeast(0)
    return COMP_X[b] - objR(b)
}
private fun frameExtent(l: Int): Double = (leadRight(l) - backLeft(l)).coerceAtLeast(1.0)

// ───────────────────────── the screen ─────────────────────────

private const val STEP_DUR = 4.4f          // seconds spent gliding between two neighbours
private const val END_HOLD = 2.2f          // pause on the final object before looping

@Composable
fun ComparisonScreen() {
    val n = COMP_OBJECTS.size
    val activity = LocalContext.current as? android.app.Activity

    // clock advances always (ambient twinkle + black-hole shimmer); pos is the
    // navigation position in step-space [0, n-1] and only auto-advances while playing
    var clock by remember { mutableFloatStateOf(0f) }
    var pos by remember { mutableFloatStateOf(0f) }
    var paused by remember { mutableStateOf(false) }
    var endHold by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = (now - last) / 1_000_000_000f
            last = now
            clock += dt
            if (!paused) {
                if (pos >= n - 1f) {
                    // hold on the final object, then — the fly-through is complete —
                    // offer an interstitial (shared frequency cap, so not every loop)
                    // and fade-loop back to the start once it's dismissed.
                    endHold += dt
                    if (endHold >= END_HOLD) {
                        if (activity != null) {
                            paused = true // freeze on the final object behind the ad
                            AdManager.maybeShowInterstitial(activity) {
                                pos = 0f; endHold = 0f; paused = false
                            }
                        } else {
                            pos = 0f; endHold = 0f
                        }
                    }
                } else {
                    pos = (pos + dt / STEP_DUR).coerceAtMost(n - 1f)
                    endHold = 0f
                }
            }
        }
    }

    // looping ambient music while this page is on screen; pauses with the app and with
    // the tap-to-pause state, and is released when the page leaves
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val music = remember { MediaPlayer.create(context, R.raw.music1)?.apply { isLooping = true } }
    DisposableEffect(lifecycleOwner) {
        music?.start()
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> music?.takeIf { it.isPlaying }?.pause()
                Lifecycle.Event.ON_RESUME -> if (!paused) music?.start()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            music?.stop()
            music?.release()
        }
    }
    // mirror the tap-to-pause state onto the music
    LaunchedEffect(paused) {
        if (paused) music?.takeIf { it.isPlaying }?.pause() else music?.start()
    }

    // gentle fade at the loop seam only while auto-playing; held steady when paused
    val fadeIn = (pos / 0.3f).coerceIn(0f, 1f)
    val fadeOut = if (pos >= n - 1f) ((END_HOLD - endHold) / 0.5f).coerceIn(0f, 1f) else 1f
    val globalAlpha = if (paused) 1f else min(fadeIn, fadeOut)

    val measurer = rememberTextMeasurer()
    val stars = remember {
        var s = 20259L
        val rnd = { s = (s * 9301 + 49297) % 233280; (s / 233280f) }
        List(110) { floatArrayOf(rnd(), rnd(), 0.3f + rnd() * 1.7f, 0.2f + rnd() * 0.5f, rnd() * 6.28f) }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF03030A))
            // tap anywhere to pause / resume the fly-through
            .pointerInput(Unit) { detectTapGestures { paused = !paused } }
            // swipe to scrub: a leftward swipe scans ahead to bigger objects, a
            // rightward swipe rewinds to the previously compared ones. Grabbing it pauses.
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { paused = true; endHold = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        pos = (pos - dragAmount / (size.width * 0.45f)).coerceIn(0f, n - 1f)
                    },
                )
            },
    ) {
        // static twinkling backdrop
        Canvas(Modifier.fillMaxSize()) {
            stars.forEach { st ->
                val tw = 0.6f + 0.4f * sin(clock * 1.3f + st[4])
                drawCircle(
                    Color(0xFFCFE0FF), radius = st[2], alpha = (st[3] * tw).coerceIn(0f, 1f),
                    center = Offset(st[0] * size.width, st[1] * size.height),
                )
            }
        }

        // the comparison itself
        Canvas(Modifier.fillMaxSize().alpha(globalAlpha)) {
            val w = size.width
            val h = size.height
            val cy = h * 0.45f
            val rightAnchor = w * 0.90f               // the lead object's right edge sits here
            val frameW = w * 0.80f                    // span filled by the lead + LINEUP_K back

            // The lead (largest, right-most) object advances with `pos`; the frame is
            // anchored on its right edge and zoomed to also take in its previous
            // LINEUP_K neighbours, so a giant arrives on the right with the few objects
            // before it trailing off to the left at true relative scale.
            val l0 = floor(pos).toInt().coerceIn(0, n - 1)
            val l1 = (l0 + 1).coerceAtMost(n - 1)
            val fr = easeInOut((pos - l0).coerceIn(0f, 1f))
            val s0 = frameW / frameExtent(l0)
            val s1 = frameW / frameExtent(l1)
            val s = exp(ln(s0) + (ln(s1) - ln(s0)) * fr)          // log-interp zoom, px per km
            val leadR = leadRight(l0) + (leadRight(l1) - leadRight(l0)) * fr

            // draw the lead and its trailing neighbours, largest first so the smaller
            // ones stay on top and visible
            val lo = (l0 - LINEUP_K).coerceAtLeast(0)
            val hi = l1
            val window = (lo..hi).sortedByDescending { COMP_OBJECTS[it].diaKm }
            window.forEach { i ->
                val o = COMP_OBJECTS[i]
                val sr = (o.diaKm / 2.0 * s).toFloat()
                if (sr < 0.4f) return@forEach
                val sx = (rightAnchor + (COMP_X[i] - leadR) * s).toFloat()
                if (sx - sr > w + 40f || sx + sr < -40f) return@forEach
                val ctr = Offset(sx, cy)
                when (o.kind) {
                    CompKind.PLANET -> drawPlanet(ctr, sr, o.colors, o.ring)
                    CompKind.STAR -> drawStar(ctr, sr, o.colors, o.glow)
                    CompKind.HOLE -> drawHole(ctr, sr, clock)
                    CompKind.NEBULA -> drawNebula(ctr, sr, o.colors, clock)
                    CompKind.CLUSTER -> drawCluster(ctr, sr, o.colors)
                    CompKind.GALAXY -> drawGalaxy(ctr, sr, o.colors, o.glow)
                    CompKind.WEB -> drawWeb(ctr, sr, WEB_PTS, WEB_EDGES, o.colors[0])
                    CompKind.UNIVERSE -> drawUniverse(ctr, sr)
                }

                // labels: name + classification sit directly above each body, diameter
                // below it — the lead object's name reads large, the trailing neighbours
                // smaller. Fade with on-screen size so the tiny trailing bodies don't
                // crowd the frame with labels, and fade near the edges as they slide off.
                val sizeFade = ((sr - w * 0.035f) / (w * 0.04f)).coerceIn(0f, 1f)
                val edgeFade = (sx / (w * 0.14f)).coerceIn(0f, 1f) *
                    ((w - sx) / (w * 0.14f)).coerceIn(0f, 1f)
                val labelAlpha = sizeFade * edgeFade
                if (labelAlpha > 0.02f) {
                    drawObjLabel(measurer, o.name, o.sub, o.sizeText, sx, cy, sr, labelAlpha, w, h)
                }
            }
        }

        // persistent hint at the top, reflecting the current play/pause state
        androidx.compose.material3.Text(
            if (paused) "❙❙  Tap to resume  ·  Swipe to explore" else "Tap to pause  ·  Swipe to explore",
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 16.dp),
            style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, letterSpacing = 0.06.em, color = Color.White.copy(alpha = 0.5f)),
        )
    }
}

private fun easeInOut(t: Float): Float =
    if (t < 0.5f) 4f * t * t * t else (t - 1f) * (2f * t - 2f) * (2f * t - 2f) + 1f

// ───────────────────────── object painters ─────────────────────────

private fun DrawScope.drawPlanet(c: Offset, r: Float, colors: List<Color>, ring: Color?) {
    ring?.let {
        val rw = r * 2.3f; val rh = r * 0.62f; val sw = max(1.5f, r * 0.05f)
        rotate(degrees = -17f, pivot = c) {
            drawOval(it, topLeft = Offset(c.x - rw / 2f, c.y - rh / 2f), size = Size(rw, rh), style = Stroke(width = sw))
        }
    }
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(0f to colors[0], 0.52f to colors[1], 1f to colors[2]),
            center = Offset(c.x - r * 0.32f, c.y - r * 0.40f), radius = r * 1.05f,
        ),
        radius = r, center = c,
    )
    // dark far limb
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(0.45f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.5f)),
            center = Offset(c.x - r * 0.32f, c.y - r * 0.40f), radius = r * 1.08f,
        ),
        radius = r, center = c,
    )
}

private fun DrawScope.drawStar(c: Offset, r: Float, colors: List<Color>, glow: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(0f to glow, 0.55f to glow.copy(alpha = glow.alpha * 0.35f), 1f to glow.copy(alpha = 0f)),
            center = c, radius = r * 2.1f,
        ),
        radius = r * 2.1f, center = c,
    )
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(0f to colors[0], 0.58f to colors[1], 1f to colors[2]),
            center = c, radius = r * 1.04f,
        ),
        radius = r, center = c,
    )
    drawCircle(colors[0].copy(alpha = 0.5f), radius = r * 1.02f, center = c, style = Stroke(width = max(1f, r * 0.03f)))
}

private fun DrawScope.drawHole(c: Offset, r: Float, t: Float) {
    val shimmer = 0.82f + 0.18f * sin(t * 3f)
    // warm accretion halo hugging the shadow
    drawCircle(
        brush = Brush.radialGradient(
            0f to Color(0x00000000), 0.34f to Color(0x00000000),
            0.5f to cc(0xffb060).copy(alpha = 0.55f * shimmer),
            0.66f to cc(0xff8a30).copy(alpha = 0.3f), 1f to Color(0x00000000),
            center = c, radius = r * 2.0f,
        ),
        radius = r * 2.0f, center = c,
    )
    drawCircle(Color.Black, radius = r, center = c)
    drawCircle(cc(0xffd9a0).copy(alpha = 0.45f * shimmer), radius = r * 1.12f, center = c, style = Stroke(width = max(2f, r * 0.10f)))
    drawCircle(cc(0xfff2d6).copy(alpha = shimmer), radius = r * 1.04f, center = c, style = Stroke(width = max(1.5f, r * 0.03f)))
}

// ───────────────────── procedural point clouds (deterministic) ─────────────────────

private fun seeded(seed: Long): () -> Float {
    var s = seed
    return { s = (s * 9301 + 49297) % 233280; (s / 233280f) }
}

// nebula: overlapping translucent blobs [x, y, size, colourIndex] + embedded stars
private val NEBULA_BLOBS: List<FloatArray> = run {
    val rnd = seeded(5150)
    List(10) {
        val a = rnd() * 6.2832f; val d = rnd() * 0.55f
        floatArrayOf(cos(a) * d, sin(a) * d, 0.45f + rnd() * 0.55f, (rnd() * 3f))
    }
}
private val NEBULA_STARS: List<FloatArray> = run {
    val rnd = seeded(424242)
    List(26) {
        val a = rnd() * 6.2832f; val d = rnd() * 0.9f
        floatArrayOf(cos(a) * d, sin(a) * d, 0.4f + rnd() * 0.6f)
    }
}

// star cluster: a swarm denser toward the core [x, y, brightness]
private val CLUSTER_PTS: List<FloatArray> = run {
    val rnd = seeded(7777)
    List(150) {
        val a = rnd() * 6.2832f
        val d = rnd() * rnd()                     // bias toward centre
        floatArrayOf(cos(a) * d, sin(a) * d, 0.4f + rnd() * 0.6f)
    }
}

// galaxy: stars strung along two log-spiral arms [x, y, brightness]
private val GALAXY_PTS: List<FloatArray> = run {
    val rnd = seeded(2024)
    val pts = ArrayList<FloatArray>()
    for (arm in 0..1) {
        val base = arm * 3.14159f
        for (k in 0 until 120) {
            val tk = k / 120f
            val rr = 0.10f + tk * 0.90f
            val ang = base + tk * 6.0f + (rnd() - 0.5f) * 0.30f
            val rj = rr + (rnd() - 0.5f) * 0.10f
            pts.add(floatArrayOf(cos(ang) * rj, sin(ang) * rj, 0.3f + rnd() * 0.7f))
        }
    }
    pts
}

// cosmic web: galaxies gathered into clumps [x, y, brightness], plus filament edges
private fun webNodes(seed: Long, clumps: Int): List<FloatArray> {
    val rnd = seeded(seed)
    val pts = ArrayList<FloatArray>()
    repeat(clumps) {
        val cxp = (rnd() * 2 - 1) * 0.72f; val cyp = (rnd() * 2 - 1) * 0.72f
        repeat(4 + (rnd() * 7).toInt()) {
            val x = (cxp + (rnd() - 0.5f) * 0.32f).coerceIn(-0.96f, 0.96f)
            val y = (cyp + (rnd() - 0.5f) * 0.32f).coerceIn(-0.96f, 0.96f)
            pts.add(floatArrayOf(x, y, 0.4f + rnd() * 0.6f))
        }
    }
    return pts
}
private fun webEdges(pts: List<FloatArray>, maxD2: Float): List<IntArray> {
    val e = ArrayList<IntArray>()
    for (i in pts.indices) for (j in i + 1 until pts.size) {
        val dx = pts[i][0] - pts[j][0]; val dy = pts[i][1] - pts[j][1]
        if (dx * dx + dy * dy < maxD2) e.add(intArrayOf(i, j))
    }
    return e
}
private val WEB_PTS = webNodes(909090, 7)
private val WEB_EDGES = webEdges(WEB_PTS, 0.10f)
private val UNIVERSE_PTS = webNodes(13131, 16)
private val UNIVERSE_EDGES = webEdges(UNIVERSE_PTS, 0.07f)

// ───────────────────── large-scale painters ─────────────────────

private fun DrawScope.drawNebula(c: Offset, r: Float, colors: List<Color>, t: Float) {
    val pulse = 0.9f + 0.1f * sin(t * 0.6f)
    NEBULA_BLOBS.forEach { b ->
        val bc = Offset(c.x + b[0] * r, c.y + b[1] * r)
        val br = b[2] * r * pulse
        val col = colors[b[3].toInt().coerceIn(0, colors.size - 1)]
        drawCircle(
            brush = Brush.radialGradient(0f to col.copy(alpha = 0.28f), 1f to Color.Transparent, center = bc, radius = br),
            radius = br, center = bc,
        )
    }
    val sr = (0.006f * r).coerceAtLeast(0.6f)
    NEBULA_STARS.forEach { p ->
        drawCircle(Color.White, radius = sr, center = Offset(c.x + p[0] * r, c.y + p[1] * r), alpha = p[2])
    }
}

private fun DrawScope.drawCluster(c: Offset, r: Float, colors: List<Color>) {
    drawCircle(
        brush = Brush.radialGradient(0f to colors[0].copy(alpha = 0.20f), 1f to Color.Transparent, center = c, radius = r),
        radius = r, center = c,
    )
    val dot = (0.011f * r).coerceAtLeast(0.6f)
    CLUSTER_PTS.forEach { p ->
        val col = if (p[2] > 0.85f) colors[0] else Color.White
        drawCircle(col, radius = dot, center = Offset(c.x + p[0] * r, c.y + p[1] * r), alpha = p[2])
    }
}

private fun DrawScope.drawGalaxy(c: Offset, r: Float, colors: List<Color>, glow: Color) {
    rotate(degrees = -24f, pivot = c) {
        // flattened disk haze
        drawOval(
            brush = Brush.radialGradient(0f to glow.copy(alpha = 0.32f), 1f to Color.Transparent, center = c, radius = r),
            topLeft = Offset(c.x - r, c.y - r * 0.42f), size = Size(r * 2f, r * 0.84f),
        )
        val dot = (0.010f * r).coerceAtLeast(0.6f)
        GALAXY_PTS.forEach { p ->
            val pc = Offset(c.x + p[0] * r, c.y + p[1] * r * 0.42f)
            val col = if (p[2] > 0.8f) Color.White else colors[1]
            drawCircle(col, radius = dot, center = pc, alpha = p[2] * 0.9f)
        }
    }
    // bright bulge
    drawCircle(
        brush = Brush.radialGradient(0f to Color.White, 0.35f to colors[0], 1f to Color.Transparent, center = c, radius = r * 0.34f),
        radius = r * 0.34f, center = c,
    )
}

private fun DrawScope.drawWeb(c: Offset, r: Float, pts: List<FloatArray>, edges: List<IntArray>, tint: Color) {
    val lw = (0.004f * r).coerceAtLeast(0.5f)
    edges.forEach { e ->
        drawLine(
            tint.copy(alpha = 0.12f),
            Offset(c.x + pts[e[0]][0] * r, c.y + pts[e[0]][1] * r),
            Offset(c.x + pts[e[1]][0] * r, c.y + pts[e[1]][1] * r),
            strokeWidth = lw,
        )
    }
    val dot = (0.013f * r).coerceAtLeast(0.7f)
    pts.forEach { p ->
        val pc = Offset(c.x + p[0] * r, c.y + p[1] * r)
        drawCircle(tint.copy(alpha = p[2]), radius = dot, center = pc)
        drawCircle(Color.White.copy(alpha = p[2] * 0.6f), radius = dot * 0.45f, center = pc)
    }
}

private fun DrawScope.drawUniverse(c: Offset, r: Float) {
    drawCircle(
        brush = Brush.radialGradient(0f to Color(0xFF0c1030), 0.85f to Color(0xCC0a0f24), 1f to Color(0x66556aff), center = c, radius = r),
        radius = r, center = c,
    )
    drawWeb(c, r * 0.95f, UNIVERSE_PTS, UNIVERSE_EDGES, Color(0xFFa9c0ff))
    drawCircle(Color(0x88aac0ff), radius = r, center = c, style = Stroke(width = (0.008f * r).coerceAtLeast(1f)))
}

private val LABEL_NAME = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, letterSpacing = 0.02.em, color = Color.White)
private val LABEL_SUB = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, letterSpacing = 0.04.em, color = Color(0xFFb8c6e0))
private val LABEL_SIZE = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 15.sp, color = Color(0xFFffd9a0))

// keep a centred label fully on screen; if it is wider than the screen, just centre it
// (clamping would otherwise build an empty range and crash)
private fun centeredX(cx: Float, half: Float, w: Float): Float {
    val lo = half + 24f
    val hi = w - half - 24f
    return if (lo <= hi) cx.coerceIn(lo, hi) else w / 2f
}

private fun DrawScope.drawObjLabel(
    measurer: androidx.compose.ui.text.TextMeasurer,
    name: String, sub: String, sizeText: String,
    cx: Float, cy: Float, sr: Float, alpha: Float, w: Float, h: Float,
) {
    // type scales with the body so the focal object's name reads large while its
    // smaller neighbours carry proportionally smaller names
    val nameSp = (11f + sr * 0.085f).coerceIn(14f, 32f)
    val subSp = (nameSp * 0.42f).coerceIn(10f, 14f)
    val sizeSp = (nameSp * 0.6f).coerceIn(13f, 20f)
    val nameStyle = LABEL_NAME.copy(fontSize = nameSp.sp)
    val subStyle = LABEL_SUB.copy(fontSize = subSp.sp)
    val sizeStyle = LABEL_SIZE.copy(fontSize = sizeSp.sp)

    // diameter, sitting just below the body
    val sl = measurer.measure(sizeText, sizeStyle)
    val sizeY = (cy + sr + 20f).coerceAtMost(h - 150f)
    val szx = centeredX(cx, sl.size.width / 2f, w)
    drawText(sl, topLeft = Offset(szx - sl.size.width / 2f, sizeY), alpha = alpha)

    // name + classification, stacked directly above the body; shrink a name that would
    // overrun the screen (some structures have very long names) so it stays readable
    var nl = measurer.measure(name, nameStyle)
    if (nl.size.width > w - 32f) {
        val shrunk = (nameSp * (w - 32f) / nl.size.width).coerceAtLeast(11f)
        nl = measurer.measure(name, nameStyle.copy(fontSize = shrunk.sp))
    }
    val ul = measurer.measure(sub, subStyle)
    val halfMax = max(nl.size.width, ul.size.width) / 2f
    val x = centeredX(cx, halfMax, w)
    val subTop = (cy - sr - 14f - ul.size.height).coerceAtLeast(8f)
    val nameTop = (subTop - 4f - nl.size.height).coerceAtLeast(8f)
    drawText(nl, topLeft = Offset(x - nl.size.width / 2f, nameTop), alpha = alpha)
    drawText(ul, topLeft = Offset(x - ul.size.width / 2f, subTop), alpha = alpha)
}
