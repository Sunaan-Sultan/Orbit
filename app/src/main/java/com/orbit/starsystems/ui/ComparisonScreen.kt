package com.orbit.starsystems.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sin

// ───────────────────────── the objects, smallest → largest ─────────────────────────

private enum class CompKind { PLANET, STAR, HOLE }

private class CompObj(
    val name: String,
    val sub: String,                   // classification shown beneath the name
    val sizeText: String,
    val diaKm: Double,                 // true diameter, drives the scaling
    val kind: CompKind,
    val colors: List<Color>,
    val glow: Color = Color.Transparent,
    val ring: Color? = null,
)

private fun cc(v: Long) = Color(v or 0xFF000000)

private val COMP_OBJECTS = listOf(
    CompObj("The Moon", "Earth's Moon", "3,474 km", 3_474.0, CompKind.PLANET, listOf(cc(0xdadae0), cc(0x9a9aa2), cc(0x46464d))),
    CompObj("Mercury", "Terrestrial Planet", "4,879 km", 4_879.0, CompKind.PLANET, listOf(cc(0xc9bdae), cc(0x8c8073), cc(0x40382e))),
    CompObj("Mars", "Terrestrial Planet", "6,779 km", 6_779.0, CompKind.PLANET, listOf(cc(0xe6915a), cc(0xb5552c), cc(0x5a2210))),
    CompObj("Earth", "Terrestrial Planet", "12,742 km", 12_742.0, CompKind.PLANET, listOf(cc(0x9fd6ff), cc(0x2f7fd0), cc(0x123a64))),
    CompObj("Neptune", "Ice Giant", "49,244 km", 49_244.0, CompKind.PLANET, listOf(cc(0x9cc4ff), cc(0x3a6fd0), cc(0x16245f))),
    CompObj("Saturn", "Gas Giant", "116,460 km", 116_460.0, CompKind.PLANET, listOf(cc(0xf0dcae), cc(0xd0a85e), cc(0x6e4e22)), ring = cc(0xc9b486)),
    CompObj("Jupiter", "Gas Giant", "139,820 km", 139_820.0, CompKind.PLANET, listOf(cc(0xf0d8b0), cc(0xc89a64), cc(0x6e4426))),
    CompObj("The Sun", "Yellow Dwarf Star", "1.39 million km", 1_391_000.0, CompKind.STAR, listOf(cc(0xfff4d4), cc(0xffb24a), cc(0xff7a1e)), glow = Color(0x99ffae4a)),
    CompObj("Sirius A", "Main-Sequence Star", "2.38 million km", 2_380_000.0, CompKind.STAR, listOf(cc(0xffffff), cc(0xcfe0ff), cc(0x7fa6da)), glow = Color(0x99a8c8ff)),
    CompObj("Betelgeuse", "Red Supergiant", "1.2 billion km", 1_234_000_000.0, CompKind.STAR, listOf(cc(0xffdcb6), cc(0xff7a4a), cc(0xb5331e)), glow = Color(0x88ff5a32)),
    CompObj("UY Scuti", "Red Hypergiant", "2.4 billion km", 2_360_000_000.0, CompKind.STAR, listOf(cc(0xffe2bc), cc(0xff8a4a), cc(0xc0451e)), glow = Color(0x88ff6a36)),
    CompObj("TON 618", "Supermassive Black Hole", "≈ 390 billion km", 390_000_000_000.0, CompKind.HOLE, listOf(Color.Black, Color.Black, Color.Black), glow = Color(0x99ffb060)),
)

// On-screen radius of the focused object as a fraction of width, by kind: planets
// stay modest so 2–3 similar worlds share the frame, stars/holes dominate the view.
private fun CompObj.screenRFrac(): Float = when (kind) {
    CompKind.PLANET -> 0.20f
    CompKind.STAR -> 0.31f
    CompKind.HOLE -> 0.28f
}

// Cumulative world-space centres (km) with a generous gap between neighbours.
private val COMP_X: DoubleArray = run {
    val n = COMP_OBJECTS.size
    val x = DoubleArray(n)
    for (i in 1 until n) {
        val rPrev = COMP_OBJECTS[i - 1].diaKm / 2.0
        val rCur = COMP_OBJECTS[i].diaKm / 2.0
        x[i] = x[i - 1] + rPrev + 0.35 * max(rPrev, rCur) + rCur
    }
    x
}

// ───────────────────────── the screen ─────────────────────────

private const val STEP_DUR = 2.6f          // seconds spent gliding between two neighbours
private const val END_HOLD = 1.8f          // pause on the final object before looping

@Composable
fun ComparisonScreen() {
    val n = COMP_OBJECTS.size
    val travel = (n - 1) * STEP_DUR
    val loop = travel + END_HOLD

    var clock by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            clock = (clock + (now - last) / 1_000_000_000f) % loop
            last = now
            // touch a derived value so the loop keeps requesting frames
        }
    }

    val pRaw = clock / STEP_DUR
    val base = floor(pRaw).toInt().coerceIn(0, n - 2)
    val fracLin = (pRaw - base).coerceIn(0f, 1f)
    val frac = easeInOut(fracLin)
    // gentle global fade at the loop seam
    val fadeIn = (clock / 0.6f).coerceIn(0f, 1f)
    val fadeOut = ((loop - clock) / 0.7f).coerceIn(0f, 1f)
    val globalAlpha = fadeIn * fadeOut

    val measurer = rememberTextMeasurer()
    val stars = remember {
        var s = 20259L
        val rnd = { s = (s * 9301 + 49297) % 233280; (s / 233280f) }
        List(110) { floatArrayOf(rnd(), rnd(), 0.3f + rnd() * 1.7f, 0.2f + rnd() * 0.5f, rnd() * 6.28f) }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF03030A))) {
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
            val anchorX = w * 0.40f
            val cy = h * 0.45f
            // on-screen radius of the focused object, easing between the two neighbours'
            // preferred sizes so planets show 2–3 per frame and stars swell to fill it
            val targetR = w * (COMP_OBJECTS[base].screenRFrac() +
                (COMP_OBJECTS[base + 1].screenRFrac() - COMP_OBJECTS[base].screenRFrac()) * frac)

            // interpolate focus radius in log space → smooth, continuous zoom
            val rBase = COMP_OBJECTS[base].diaKm / 2.0
            val rNext = COMP_OBJECTS[base + 1].diaKm / 2.0
            val rFocus = exp(ln(rBase) + (ln(rNext) - ln(rBase)) * frac)
            val s = targetR / rFocus                 // px per km
            val xFocus = COMP_X[base] + (COMP_X[base + 1] - COMP_X[base]) * frac

            // draw a small window of neighbours, largest first so the focus stays on top
            val lo = (base - 1).coerceAtLeast(0)
            val hi = (base + 2).coerceAtMost(n - 1)
            val window = (lo..hi).sortedByDescending { COMP_OBJECTS[it].diaKm }
            window.forEach { i ->
                val o = COMP_OBJECTS[i]
                val sr = (o.diaKm / 2.0 * s).toFloat()
                if (sr < 0.4f) return@forEach
                val sx = (anchorX + (COMP_X[i] - xFocus) * s).toFloat()
                if (sx - sr > w + 40f || sx + sr < -40f) return@forEach
                val ctr = Offset(sx, cy)
                when (o.kind) {
                    CompKind.PLANET -> drawPlanet(ctr, sr, o.colors, o.ring)
                    CompKind.STAR -> drawStar(ctr, sr, o.colors, o.glow)
                    CompKind.HOLE -> drawHole(ctr, sr, clock)
                }

                // labels: size under every visible body; name + classification above the
                // ones the header isn't already naming (i.e. the off-focus neighbours)
                val labelAlpha = ((sr - 11f) / 34f).coerceIn(0f, 1f) * ((w * 0.72f - sr) / (w * 0.22f)).coerceIn(0f, 1f)
                if (labelAlpha > 0.02f) {
                    val showHead = i != base && i != base + 1
                    drawObjLabel(measurer, o.name, o.sub, o.sizeText, sx, cy, sr, labelAlpha, w, h, showHead)
                }
            }
        }

        // header: eyebrow + the focused object's name, cross-fading to the next
        Column(
            Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 14.dp).alpha(globalAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            androidx.compose.material3.Text(
                "SIZE COMPARISON",
                style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 0.4.em, color = Color(0xFF9fc2ff)),
            )
            Spacer(Modifier.height(8.dp))
            Box(contentAlignment = Alignment.TopCenter) {
                CompHeaderName(COMP_OBJECTS[base], 1f - fracLin)
                CompHeaderName(COMP_OBJECTS[base + 1], fracLin)
            }
        }

        // progress dots, one per object, sitting above the bottom nav
        val activeIndex = if (fracLin > 0.5f) base + 1 else base
        Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp).alpha(globalAlpha),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(n) { i ->
                val on = i == activeIndex
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (on) 8.dp else 5.dp)
                        .clip(CircleShape)
                        .background(if (on) Color.White else Color.White.copy(alpha = 0.28f)),
                )
            }
        }
    }
}

private fun easeInOut(t: Float): Float =
    if (t < 0.5f) 4f * t * t * t else (t - 1f) * (2f * t - 2f) * (2f * t - 2f) + 1f

// the focused object's name + classification, used by the cross-fading header
@Composable
private fun CompHeaderName(o: CompObj, alpha: Float) {
    Column(
        Modifier.alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        androidx.compose.material3.Text(
            o.name,
            style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 32.sp, color = Color.White),
        )
        Spacer(Modifier.height(2.dp))
        androidx.compose.material3.Text(
            o.sub,
            style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Normal, fontSize = 13.sp, letterSpacing = 0.05.em, color = Color(0xFF9fc2ff)),
        )
    }
}

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

private val LABEL_NAME = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, letterSpacing = 0.02.em, color = Color.White)
private val LABEL_SUB = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, letterSpacing = 0.04.em, color = Color(0xFFb8c6e0))
private val LABEL_SIZE = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 15.sp, color = Color(0xFFffd9a0))

private fun DrawScope.drawObjLabel(
    measurer: androidx.compose.ui.text.TextMeasurer,
    name: String, sub: String, sizeText: String,
    cx: Float, cy: Float, sr: Float, alpha: Float, w: Float, h: Float, showHead: Boolean,
) {
    // size, always sitting just below the body
    val sl = measurer.measure(sizeText, LABEL_SIZE)
    val sizeY = (cy + sr + 22f).coerceAtMost(h - 150f)
    val sx = cx.coerceIn(sl.size.width / 2f + 24f, w - sl.size.width / 2f - 24f)
    drawText(sl, topLeft = Offset(sx - sl.size.width / 2f, sizeY), alpha = alpha)

    // name + classification, stacked just above the body (header names the focused pair)
    if (showHead) {
        val nl = measurer.measure(name, LABEL_NAME)
        val ul = measurer.measure(sub, LABEL_SUB)
        val halfMax = max(nl.size.width, ul.size.width) / 2f
        val x = cx.coerceIn(halfMax + 24f, w - halfMax - 24f)
        val subTop = (cy - sr - 12f - ul.size.height).coerceAtLeast(8f)
        val nameTop = (subTop - 2f - nl.size.height).coerceAtLeast(8f)
        drawText(nl, topLeft = Offset(x - nl.size.width / 2f, nameTop), alpha = alpha)
        drawText(ul, topLeft = Offset(x - ul.size.width / 2f, subTop), alpha = alpha)
    }
}
