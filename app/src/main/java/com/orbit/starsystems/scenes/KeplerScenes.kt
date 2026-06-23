package com.orbit.starsystems.scenes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.orbit.starsystems.core.Easing
import com.orbit.starsystems.core.animate
import com.orbit.starsystems.core.interpolate
import com.orbit.starsystems.core.reveal
import com.orbit.starsystems.ui.OrbitFont
import com.orbit.starsystems.ui.Sphere
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

private const val KEP_HALF_PI = 1.5707964f
private const val KEP_TAU = 6.2831855f

// Kepler-90 is a Sun-like G star — a familiar yellow-white glow.
private val KEP_STAR = listOf(c(0xfff3c8), c(0xffd35e), c(0xf0992c))
private val KEP_STAR_GLOW = Color(0x80ffce5a)
private val KEP_ACCENT = c(0xa9c2ff)

/** A Kepler-90 planet: radius (Earth=1), period (days), distance (AU), palette, giant. */
private data class Kep(val n: String, val r: Float, val days: Float, val au: Float, val cc: List<Color>, val giant: Boolean)

// b · c · i · d · e · f · g · h  (i is the AI-discovered eighth world)
private val KEP = listOf(
    Kep("b", 1.31f, 7.01f, 0.074f, listOf(c(0xe7c39a), c(0xb47a4c), c(0x5e3a22)), false),
    Kep("c", 1.18f, 8.72f, 0.089f, listOf(c(0xe0b488), c(0xad6f44), c(0x5a3420)), false),
    Kep("i", 1.32f, 14.45f, 0.121f, listOf(c(0xbfe6da), c(0x6fb6a2), c(0x2f5e54)), false),
    Kep("d", 2.87f, 59.74f, 0.32f, listOf(c(0xcfd8ee), c(0x8597c4), c(0x3b4870)), false),
    Kep("e", 2.66f, 91.94f, 0.42f, listOf(c(0xc6d2ec), c(0x7d92c6), c(0x37466e)), false),
    Kep("f", 2.88f, 124.91f, 0.48f, listOf(c(0xd2ddf2), c(0x8ba0cf), c(0x404f76)), false),
    Kep("g", 8.1f, 210.6f, 0.71f, listOf(c(0xecd8b4), c(0xc8a072), c(0x8f6a40)), true),
    Kep("h", 11.3f, 331.6f, 0.97f, listOf(c(0xe9d3a6), c(0xc69a5e), c(0x8a5f34)), true),
)

private fun kepLabel(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = size.sp, letterSpacing = 0.12.em, color = color,
)

// ───────────────────── 1 · Eight worlds, one star ─────────────────────

@Composable
fun BoxScope.KepEight(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val cx = 540f; val cy = 1190f
    val grow = animate(0f, 1f, 0.4f, 2.2f, Easing.easeOutBack)(t)
    Eyebrow("Kepler-90 · a rival to the Sun", KEP_ACCENT, 200f, e1)
    Head(headline("Eight worlds,\none ", "Sun-like", " star."), 84f, 246f, e2)
    BottomLine(1640f, reveal(t, 5.5f), body("The first star ever found to host ", "as many planets as the Sun", "."))
    Sphere(150f * grow, KEP_STAR, glow = KEP_STAR_GLOW, modifier = Modifier.offset((cx - 75f * grow).dp, (cy - 75f * grow).dp))
    KEP.forEachIndexed { i, p ->
        val r = 150f + i * 46f
        val ringO = reveal(t, 0.6f + i * 0.12f).opacity
        val ang = t * (1.1f - i * 0.1f) + i * 0.9f
        val px = cx + cos(ang) * r; val py = cy + sin(ang) * r * 0.42f
        val ds = max(12f, p.r * 7f)
        RingArc(cx, cy, r * 2f, r * 0.84f, 0f, Color(0x2982a6ff), 1f, alpha = ringO)
        At(px - ds / 2f, py - ds / 2f, ringO) { Sphere(ds, p.cc) }
    }
}

// ───────────────────── 2 · A sun much like ours ─────────────────────

@Composable
fun BoxScope.KepSunlike(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val grow = animate(0f, 1f, 0.6f, 2.4f, Easing.easeOutCubic)(t)
    val kepR = 158f; val sunR = kepR / 1.2f; val cy = 820f
    Eyebrow("Kepler-90 vs. the Sun", KEP_ACCENT, 200f, e1)
    Head(headline("A sun much\nlike ", "ours", "."), 84f, 246f, e2)
    BottomLine(1640f, reveal(t, 5.5f), body("A ", "G-type star", " like the Sun — about a fifth larger, and a little hotter."))
    Sphere(kepR * 2 * grow, KEP_STAR, glow = KEP_STAR_GLOW, modifier = Modifier.offset((370f - kepR * grow).dp, (cy - kepR * grow).dp))
    Sphere(sunR * 2 * grow, listOf(c(0xfff7d6), c(0xffc23a), c(0xff8a26)), glow = Color(0x80ffa03c), modifier = Modifier.offset((770f - sunR * grow).dp, (cy - sunR * grow).dp))
    val la = reveal(t, 2.2f)
    CenterLabel(370f, cy + kepR + 22f, la.opacity) { Text("KEPLER-90", style = kepLabel(22f, KEP_ACCENT)) }
    CenterLabel(770f, cy + kepR + 22f, la.opacity) { Text("OUR SUN", style = kepLabel(22f, c(0xffc23a))) }
    data class RowDef(val l: String, val v: String, val frac: Float)
    val rows = listOf(RowDef("Radius", "1.2×", 0.6f), RowDef("Mass", "1.1×", 0.55f), RowDef("Temperature", "1.05×", 0.52f))
    val barL = 110f; val barW = 560f; val top0 = 1180f; val rowH = 130f
    val draw = animate(0f, 1f, 3f, 5f, Easing.easeOutCubic)(t)
    rows.forEachIndexed { i, r ->
        val y = top0 + i * rowH
        val a = reveal(t, 2.8f + i * 0.18f)
        At(barL, y - 42f, a.opacity) { Text(r.l, style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, letterSpacing = 0.06.em, color = c(0x9fb1d6))) }
        At(barL, y, a.opacity) { Box(Modifier.size(barW.dp, 14.dp).clip(RoundedCornerShape(7.dp)).background(Color.White.copy(alpha = 0.08f))) }
        At(barL, y, a.opacity) { Box(Modifier.size((barW * r.frac * draw).coerceAtLeast(0f).dp, 14.dp).clip(RoundedCornerShape(7.dp)).background(Brush.horizontalGradient(listOf(Color(0x66a9c2ff), KEP_ACCENT)))) }
        At(barL + barW + 18f, y - 13f, a.opacity) { Text(r.v, style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)) }
    }
}

// ───────────────────── 3 · Packed inside Earth's orbit ─────────────────────

@Composable
fun BoxScope.KepCrowded(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val cx = 540f; val cy = 980f; val rE = 430f
    val ringDraw = animate(0f, 1f, 0.8f, 3f, Easing.easeOutCubic)(t)
    val eAng = -KEP_HALF_PI + t * 0.4f
    val ex = cx + cos(eAng) * rE; val ey = cy + sin(eAng) * rE
    Eyebrow("The whole system vs. Earth's orbit", KEP_ACCENT, 200f, e1)
    Head(headline("Eight worlds,\npacked ", "tight", "."), 80f, 246f, e2)
    BottomLine(1648f, reveal(t, 6f), body("Every planet orbits closer than Earth — the outermost sits right about ", "where we do", "."))
    // Earth's orbit as the outer reference circle
    RingArc(cx, cy, rE * 2f, rE * 2f, 0f, Color(0x66a9c2ff), 1.5f, dash = true, alpha = ringDraw)
    At(ex - 11f, ey - 11f, ringDraw) { Sphere(22f, listOf(c(0x9cc4ec), c(0x3d72b8), c(0x16223f))) }
    At(cx + 18f, cy - rE - 6f, reveal(t, 2f).opacity) { Text("EARTH · 1.0 AU", style = kepLabel(19f, c(0x9cc4ec))) }
    // the eight Kepler-90 worlds scaled into that same orbit (h ≈ 0.97 AU → near the edge)
    Sphere(48f, KEP_STAR, glow = KEP_STAR_GLOW, modifier = Modifier.offset((cx - 24f).dp, (cy - 24f).dp))
    KEP.forEachIndexed { i, p ->
        val r = (p.au / 1.0f) * rE
        val ringO = reveal(t, 1.2f + i * 0.1f).opacity
        val ang = -KEP_HALF_PI + t * (1.0f - i * 0.08f) + i
        val px = cx + cos(ang) * r; val py = cy + sin(ang) * r
        val ds = max(10f, p.r * 5f)
        RingArc(cx, cy, r * 2f, r * 2f, 0f, Color(0x2482a6ff), 1f, alpha = ringO)
        At(px - ds / 2f, py - ds / 2f, ringO) { Sphere(ds, p.cc) }
    }
}

// ───────────────────── 4 · Found by AI ─────────────────────

@Composable
fun BoxScope.KepAi(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val ai = c(0x7fd6c0)
    Eyebrow("The eighth planet", ai, 200f, e1)
    Head(headline("Found by\n", "artificial", " intelligence."), 80f, 246f, e2)
    BottomLine(1648f, reveal(t, 6f), body("A Google neural network spotted ", "Kepler-90i", " in the data — a world too faint for human eyes."))
    // A brightness light-curve with a transit dip the network "detects".
    val plotL = 110f; val plotR = 980f; val baseY = 1020f; val dipY = 1150f
    val dipC = (plotL + plotR) / 2f
    val curve = reveal(t, 1.2f, 1.2f)
    Canvas(Modifier.fillMaxSize().alpha(curve.opacity)) {
        val k = size.width / 1080f
        fun p(x: Float, y: Float) = Offset(x * k, y * k)
        // dashed baseline
        var x = plotL
        while (x < plotR) {
            drawLine(c(0x33ffffff), p(x, baseY), p(x + 10f, baseY), strokeWidth = 1.5f * k)
            x += 18f
        }
        val path = Path().apply {
            moveTo(p(plotL, baseY).x, p(plotL, baseY).y)
            lineTo(p(dipC - 150f, baseY).x, p(dipC - 150f, baseY).y)
            lineTo(p(dipC - 70f, dipY).x, p(dipC - 70f, dipY).y)
            lineTo(p(dipC + 70f, dipY).x, p(dipC + 70f, dipY).y)
            lineTo(p(dipC + 150f, baseY).x, p(dipC + 150f, baseY).y)
            lineTo(p(plotR, baseY).x, p(plotR, baseY).y)
        }
        drawPath(path, color = ai, style = Stroke(width = 4f * k))
    }
    // detection box snapping onto the dip
    val box = reveal(t, 3.2f)
    val boxScale = animate(1.25f, 1f, 3.2f, 3.9f, Easing.easeOutBack)(t)
    At(dipC - 130f * boxScale, dipY - 70f, box.opacity) {
        Box(Modifier.size((260f * boxScale).dp, 150.dp).border(2.dp, ai, RoundedCornerShape(10.dp)))
    }
    CenterLabel(dipC, dipY + 96f, box.opacity) { Text("TRANSIT DETECTED", style = kepLabel(20f, ai)) }
    // a planet crossing the star, top-left
    val starCx = 300f; val starCy = 560f
    Sphere(150f, KEP_STAR, glow = KEP_STAR_GLOW, modifier = Modifier.offset((starCx - 75f).dp, (starCy - 75f).dp))
    val cross = interpolate(listOf(1.4f, 4.6f), listOf(starCx - 130f, starCx + 130f), Easing.easeInOutSine)(t)
    if (t in 1.4f..4.8f) {
        At(cross - 14f, starCy - 14f) { Box(Modifier.size(28.dp).clip(CircleShape).background(c(0x1a242f))) }
    }
}

// ───────────────────── 5 · 2,840 light-years away ─────────────────────

@Composable
fun BoxScope.KepDistance(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f)
    val num = interpolate(listOf(1.2f, 4.2f), listOf(0f, 2840f), Easing.easeOutCubic)(t).roundToInt()
    val sunX = 180f; val kepX = 900f; val mapY = 1260f
    val photon = interpolate(listOf(2f, 6.4f), listOf(sunX, kepX), Easing.easeInOutSine)(t)
    val moving = t in 2f..6.6f
    Eyebrow("How far is it?", KEP_ACCENT, 300f, e1)
    BottomLine(1648f, reveal(t, 6.6f), body("Its light set out long before history — we see Kepler-90 as it was ", "millennia ago", "."))
    At(84f, 370f + e2.ty, e2.opacity) {
        Text(String.format(Locale.US, "%,d", num), style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 170.sp, color = Color.White, letterSpacing = (-0.04).em, lineHeight = 170.sp))
    }
    At(84f, 600f + e2.ty, e2.opacity) {
        Text("light-years away.", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 54.sp, color = Color.White))
    }
    At(84f, 686f + e2.ty, e2.opacity) {
        Text("≈ 27 quadrillion km · in the constellation Draco", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 26.sp, color = c(0x8794ac)))
    }
    At(sunX, mapY, reveal(t, 1.6f).opacity) {
        Box(Modifier.size((kepX - sunX).dp, 1.dp).background(c(0x3a4660)))
    }
    Sphere(40f, listOf(c(0xfff7d6), c(0xffc23a), c(0xff8a26)), glow = Color(0x80ffa03c), modifier = Modifier.offset((sunX - 20f).dp, (mapY - 20f).dp))
    Sphere(34f, KEP_STAR, glow = KEP_STAR_GLOW, modifier = Modifier.offset((kepX - 17f).dp, (mapY - 17f).dp))
    CenterLabel(sunX, mapY + 36f, reveal(t, 2f).opacity) { Text("OUR SUN", style = kepLabel(18f, c(0xd9a24e))) }
    CenterLabel(kepX, mapY + 36f, reveal(t, 2f).opacity) { Text("KEPLER-90", style = kepLabel(18f, KEP_ACCENT)) }
    if (moving) {
        At(photon - 6f, mapY - 6f) { Box(Modifier.size(12.dp).clip(CircleShape).background(Color.White)) }
    }
}
