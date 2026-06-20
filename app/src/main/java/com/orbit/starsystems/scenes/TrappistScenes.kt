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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

private const val HALF_PI = 1.5707964f
private const val TAU = 6.2831855f

internal val STAR_C = listOf(c(0xffe0ad), c(0xff7338), c(0xb8351a))
internal val STAR_GLOW = Color(0x80ff5a28)

/** The seven TRAPPIST-1 planets: radius (Earth=1), period (days), distance (AU), palette, habitable. */
private data class Tp(val n: String, val r: Float, val days: Float, val au: Float, val cc: List<Color>, val hz: Boolean)

private val TP = listOf(
    Tp("b", 1.116f, 1.51f, 0.0115f, listOf(c(0xf0b07a), c(0xbd5a30), c(0x5f2412)), false),
    Tp("c", 1.097f, 2.42f, 0.0158f, listOf(c(0xe89c70), c(0xad5d3a), c(0x5a2a18)), false),
    Tp("d", 0.788f, 4.05f, 0.0223f, listOf(c(0xe0b488), c(0xb27c4e), c(0x5e3c24)), false),
    Tp("e", 0.920f, 6.10f, 0.0293f, listOf(c(0x9cc4ec), c(0x3d72b8), c(0x1a3360)), true),
    Tp("f", 1.045f, 9.21f, 0.0385f, listOf(c(0xa6cfe0), c(0x4d8fb0), c(0x235468)), true),
    Tp("g", 1.129f, 12.35f, 0.0468f, listOf(c(0xbcd8d2), c(0x6fa69c), c(0x356058)), true),
    Tp("h", 0.755f, 18.77f, 0.0619f, listOf(c(0xd6e8ee), c(0x9ab8c8), c(0x5a7886)), false),
)

private fun hzGlow(hz: Boolean, color: Color = Color(0x4D5a96d2)) = if (hz) color else null

private fun tpLabel(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = size.sp, letterSpacing = 0.12.em, color = color,
)

// ───────────────────── Seven Earths, one tiny star ─────────────────────

@Composable
fun BoxScope.TpIntro(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val cx = 540f; val cy = 1190f
    val grow = animate(0f, 1f, 0.4f, 2.2f, Easing.easeOutBack)(t)
    Eyebrow("TRAPPIST-1 · the system next door", c(0xe07a4a), 200f, e1)
    Head(headline("Seven Earths,\none ", "tiny", " star."), 90f, 246f, e2)
    Sphere(150f * grow, STAR_C, glow = STAR_GLOW, modifier = Modifier.offset((cx - 75f * grow).dp, (cy - 75f * grow).dp))
    TP.forEachIndexed { i, p ->
        val r = 150f + i * 48f
        val ringO = reveal(t, 0.6f + i * 0.12f).opacity
        val ang = t * (1.15f - i * 0.11f) + i * 0.9f
        val px = cx + cos(ang) * r; val py = cy + sin(ang) * r * 0.42f
        val ds = max(11f, p.r * 14f)
        RingArc(cx, cy, r * 2f, r * 0.84f, 0f, Color(0x29ff8250), 1f, alpha = ringO)
        At(px - ds / 2f, py - ds / 2f, ringO) { Sphere(ds, p.cc, glow = hzGlow(p.hz)) }
    }
}

// ───────────────────── A star barely bigger than Jupiter ─────────────────────

@Composable
fun BoxScope.TpStarSize(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val grow = animate(0f, 1f, 0.6f, 2.4f, Easing.easeOutCubic)(t)
    val starR = 150f; val jupR = starR / 1.16f; val cy = 820f
    val barY = 1300f; val barL = 90f; val barW = 900f
    val barDraw = animate(0f, 1f, 3.0f, 5.2f, Easing.easeInOutCubic)(t)
    val starSeg = barW * 0.12f
    Eyebrow("An ultracool red dwarf", c(0xe07a4a), 200f, e1)
    Head(headline("Barely bigger\nthan ", "Jupiter", "."), 84f, 246f, e2)
    Sphere(starR * 2 * grow, STAR_C, glow = STAR_GLOW, modifier = Modifier.offset((370f - starR * grow).dp, (cy - starR * grow).dp))
    Sphere(jupR * 2 * grow, listOf(c(0xecd8b4), c(0xc8a072), c(0x8f6a40)), modifier = Modifier.offset((760f - jupR * grow).dp, (cy - jupR * grow).dp))
    val la = reveal(t, 2.2f)
    CenterLabel(370f, cy + starR + 22f, la.opacity) { Text("TRAPPIST-1", style = tpLabel(22f, c(0xe07a4a))) }
    CenterLabel(760f, cy + starR + 22f, la.opacity) { Text("JUPITER", style = tpLabel(22f, c(0xc8a072))) }
    At(barL, barY - 50f, reveal(t, 3f).opacity) {
        Text("Width compared to our Sun", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 24.sp, color = c(0xbdbdbd)))
    }
    At(barL, barY) { Box(Modifier.size((barW * barDraw).coerceAtLeast(0f).dp, 10.dp).clip(CircleShape).background(c(0x3a3a3a))) }
    At(barL, barY) { Box(Modifier.size(minOf(starSeg, barW * barDraw).coerceAtLeast(0f).dp, 10.dp).clip(CircleShape).background(Brush.horizontalGradient(listOf(c(0xffd0a0), c(0xff6a34))))) }
    val ba = reveal(t, 5f)
    At(barL, barY + 22f, ba.opacity) { Text("TRAPPIST-1 · 12%", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, letterSpacing = 0.08.em, color = c(0xff8a4a))) }
    At(barL, barY + 22f, ba.opacity, modifier = Modifier.size(900.dp, 30.dp)) {
        Text("SUN · 100%", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, letterSpacing = 0.08.em, color = c(0x888888), textAlign = androidx.compose.ui.text.style.TextAlign.End), modifier = Modifier.size(900.dp, 30.dp))
    }
}

// ───────────────────── Smaller than Mercury's orbit ─────────────────────

@Composable
fun BoxScope.TpMercury(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val cx = 540f; val cy = 640f; val rm = 360f
    val ringDraw = animate(0f, 1f, 0.8f, 3f, Easing.easeOutCubic)(t)
    val mAng = -HALF_PI + t * 0.5f
    val mx = cx + cos(mAng) * rm; val my = cy + sin(mAng) * rm
    val tinyR = rm * (0.062f / 0.39f)
    val insetCx = 540f; val insetCy = 1440f; val insetMax = 290f
    val insetDraw = reveal(t, 3.4f, 1.0f)
    Eyebrow("The whole system vs. Mercury", c(0x8c8c8c), 96f, e1)
    Head(headline("Smaller than one ", "orbit", "."), 76f, 140f, e2)
    RingArc(cx, cy, rm * 2f, rm * 2f, 0f, Color(0x66b4b4b4), 1.5f, dash = true, alpha = ringDraw)
    Sphere(30f, listOf(c(0xfff7d6), c(0xffc23a), c(0xff8a26)), glow = Color(0x80ffa03c), modifier = Modifier.offset((cx - 15f).dp, (cy - 15f).dp))
    At(mx - 7f, my - 7f, ringDraw) { Sphere(14f, listOf(c(0xd8ccba), c(0x9c9078), c(0x4f4738))) }
    At(cx + rm + 14f, cy - 10f, reveal(t, 2f).opacity) { Text("MERCURY · 0.39 AU", style = tpLabel(19f, c(0xcfcfcf))) }
    At(cx - tinyR, cy - tinyR, reveal(t, 2.6f).opacity) {
        Box(Modifier.size((tinyR * 2f).dp).clip(CircleShape).border(1.dp, Color(0x99ff7846), CircleShape))
    }
    At(cx + tinyR + 10f, cy + tinyR - 6f, reveal(t, 3f).opacity) {
        Text("← entire TRAPPIST-1 system", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, letterSpacing = 0.06.em, color = c(0xff8a4a)))
    }
    At(90f, 1130f, insetDraw.opacity) { Text("MAGNIFIED ↓", style = tpLabel(20f, c(0x666666))) }
    At(insetCx - 13f, insetCy - 13f, insetDraw.opacity) { Sphere(26f, STAR_C, glow = STAR_GLOW) }
    TP.forEachIndexed { i, p ->
        val r = (p.au / 0.0619f) * insetMax
        val ang = -HALF_PI + t * (1.0f - i * 0.1f) + i
        val dx = insetCx + cos(ang) * r; val dy = insetCy + sin(ang) * r
        val ds = max(9f, p.r * 9f)
        RingArc(insetCx, insetCy, r * 2f, r * 2f, 0f, Color(0x47ff8250), 1f, alpha = insetDraw.opacity)
        At(dx - ds / 2f, dy - ds / 2f, insetDraw.opacity) { Sphere(ds, p.cc) }
    }
}

// ───────────────────── A year in days ─────────────────────

@Composable
fun BoxScope.TpOrbits(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val cx = 540f; val cy = 1080f; val speedK = 1.34f
    Eyebrow("Orbital periods", c(0xe07a4a), 200f, e1)
    Head(headline("A year in ", "days", "."), 84f, 246f, e2)
    Sphere(70f, STAR_C, glow = STAR_GLOW, modifier = Modifier.offset((cx - 35f).dp, (cy - 35f).dp))
    TP.forEachIndexed { i, p ->
        val r = 120f + i * 56f
        val ringO = reveal(t, 0.6f + i * 0.1f).opacity
        val ang = -HALF_PI + t * TAU * speedK / p.days
        val px = cx + cos(ang) * r; val py = cy + sin(ang) * r
        val ds = max(16f, p.r * 20f)
        RingArc(cx, cy, r * 2f, r * 2f, 0f, Color(0x24ff8250), 1f, alpha = ringO)
        At(px - ds / 2f, py - ds / 2f, ringO) { Sphere(ds, p.cc, glow = hzGlow(p.hz)) }
    }
}

// ───────────────────── The habitable zone ─────────────────────

@Composable
fun BoxScope.TpHabitable(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val starX = 150f; val axisY = 1020f; val auScale = 13500f
    val draw = animate(0f, 1f, 0.6f, 2.4f, Easing.easeOutCubic)(t)
    fun x(au: Float) = starX + au * auScale
    val hzL = x(0.024f); val hzR = x(0.052f)
    val bandO = reveal(t, 2.6f, 1.0f)
    Eyebrow("The habitable zone", c(0x5fae7a), 200f, e1)
    Head(headline("Three in the\n", "water zone", "."), 80f, 246f, e2)
    At(hzL, 600f, bandO.opacity) {
        Box(Modifier.size((hzR - hzL).dp, 820.dp).background(Brush.horizontalGradient(listOf(Color(0x0A3caa6e), Color(0x2E46be82), Color(0x0A3caa6e)))))
    }
    CenterLabel((hzL + hzR) / 2f, 628f, bandO.opacity) { Text("LIQUID WATER POSSIBLE", style = tpLabel(19f, c(0x6fd29a))) }
    At(starX, axisY) { Box(Modifier.size(((x(0.066f) - starX) * draw).coerceAtLeast(0f).dp, 1.dp).background(c(0x3a3a3a))) }
    Sphere(64f, STAR_C, glow = STAR_GLOW, modifier = Modifier.offset((starX - 32f).dp, (axisY - 32f).dp))
    TP.forEachIndexed { i, p ->
        val cxp = x(p.au)
        val a = reveal(t, 1f + i * 0.12f)
        val ds = max(30f, p.r * 36f)
        val up = i % 2 == 0
        val py = if (up) axisY - 130f else axisY + 130f
        At(cxp, minOf(py, axisY), a.opacity) { Box(Modifier.size(1.dp, abs(py - axisY).dp).background(Color(0x24FFFFFF))) }
        At(cxp - ds / 2f, py - ds / 2f, a.opacity) { Sphere(ds, p.cc, glow = if (p.hz) Color(0x666ed29a) else null) }
        CenterLabel(cxp, if (up) py - ds / 2f - 32f else py + ds / 2f + 8f, a.opacity) {
            Text(p.n, style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = if (p.hz) c(0x6fd29a) else c(0x8a8a8a)))
        }
    }
}

// ───────────────────── Tidally locked ─────────────────────

@Composable
fun BoxScope.TpTidal(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val cx = 600f; val cy = 1080f; val r = 300f
    val grow = animate(0f, 1f, 0.5f, 2.2f, Easing.easeOutCubic)(t)
    val starX = 130f; val starY = 1080f
    Eyebrow("Tidally locked", c(0xe07a4a), 200f, e1)
    Head(headline("One face, ", "always", "\ntoward the star."), 80f, 246f, e2)
    Sphere(120f, STAR_C, glow = STAR_GLOW, modifier = Modifier.offset((starX - 60f).dp, (starY - 60f).dp))
    val beam = reveal(t, 1.6f).opacity
    At(starX, starY - 60f, beam) { Box(Modifier.size((cx - starX).dp, 2.dp).background(Brush.horizontalGradient(listOf(Color(0x80ff8c46), Color(0x00ff8c46))))) }
    At(starX, starY + 60f, beam) { Box(Modifier.size((cx - starX).dp, 2.dp).background(Brush.horizontalGradient(listOf(Color(0x80ff8c46), Color(0x00ff8c46))))) }
    // tidally-locked world: lit on the star-facing (left) side, dark on the right
    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val rr = r * grow * k
        if (rr > 1f) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(c(0xffd9b0), c(0xd98b66), c(0x3a1a12)),
                    center = Offset(cx * k - rr * 0.56f, cy * k),
                    radius = rr * 1.4f,
                ),
                radius = rr, center = Offset(cx * k, cy * k),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.Transparent, Color(0xB3000000)),
                    center = Offset(cx * k + rr * 0.5f, cy * k), radius = rr * 1.1f,
                ),
                radius = rr, center = Offset(cx * k, cy * k),
            )
        }
    }
    At(cx - 130f, cy - r - 26f, reveal(t, 2.6f).opacity) { Text("PERMANENT DAY", style = tpLabel(22f, c(0xffb070))) }
    At(cx + 80f, cy + r + 6f, reveal(t, 2.6f).opacity) { Text("PERMANENT NIGHT", style = tpLabel(22f, c(0x7da0c8))) }
}

// ───────────────────── Worlds that fill the sky ─────────────────────

@Composable
fun BoxScope.TpSky(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val rise = animate(120f, 0f, 0.6f, 3f, Easing.easeOutCubic)(t)
    val bigR = 280f
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.30f to Color(0x0028100c), 0.75f to Color(0x80782310), 1f to Color(0x9eb43c1c))))
    Eyebrow("The view from the surface", c(0xe07a4a), 200f, e1)
    Head(headline("Worlds that\nfill the ", "sky", "."), 80f, 246f, e2)
    // a sister world looming overhead
    RadialDisc(660f, 920f + rise, bigR, arrayOf(0f to c(0xa8d0e0), 0.52f to c(0x3d72b8), 1f to c(0x16223f)), 0.38f, 0.36f, 0.62f)
    CenterLabel(660f, 920f - bigR - 36f + rise, reveal(t, 3f).opacity) { Text("A SISTER WORLD", style = tpLabel(20f, c(0x9cc4ec))) }
    At(250f - 28f, 600f, reveal(t, 4f).opacity) { Sphere(56f, listOf(c(0xd8d4ca), c(0xa8a49a), c(0x5e5a52))) }
    CenterLabel(250f, 668f, reveal(t, 4f).opacity) { Text("our Moon, same view", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, letterSpacing = 0.08.em, color = c(0xb9b5ab))) }
    // jagged horizon silhouette across the bottom
    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val top = 1560f
        fun pt(xf: Float, yf: Float) = Offset(xf * 1080f * k, (top + yf * 360f) * k)
        val path = Path().apply {
            moveTo(pt(0f, 0.38f).x, pt(0f, 0.38f).y)
            lineTo(pt(0.18f, 0.30f).x, pt(0.18f, 0.30f).y)
            lineTo(pt(0.38f, 0.40f).x, pt(0.38f, 0.40f).y)
            lineTo(pt(0.60f, 0.26f).x, pt(0.60f, 0.26f).y)
            lineTo(pt(0.82f, 0.38f).x, pt(0.82f, 0.38f).y)
            lineTo(pt(1f, 0.30f).x, pt(1f, 0.30f).y)
            lineTo(pt(1f, 1f).x, pt(1f, 1f).y)
            lineTo(pt(0f, 1f).x, pt(0f, 1f).y)
            close()
        }
        drawPath(path, brush = Brush.verticalGradient(listOf(c(0x2a0f08), c(0x0a0402)), startY = top * k, endY = 1920f * k))
    }
}

// ───────────────────── 39 light-years away ─────────────────────

@Composable
fun BoxScope.TpDistance(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f)
    val num = interpolate(listOf(1.2f, 4f), listOf(0f, 39f), Easing.easeOutCubic)(t).roundToInt()
    val sunX = 200f; val tpX = 880f; val mapY = 1240f
    val photon = interpolate(listOf(2f, 6f), listOf(sunX, tpX), Easing.easeInOutSine)(t)
    val moving = t in 2f..6.2f
    Eyebrow("How far is it?", c(0xe07a4a), 300f, e1)
    At(84f, 370f + e2.ty, e2.opacity) {
        Text("$num", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 240.sp, color = Color.White, letterSpacing = (-0.04).em, lineHeight = 216.sp))
    }
    At(84f, 760f + e2.ty, e2.opacity) {
        Text("light-years away.", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 54.sp, color = Color.White))
    }
    At(84f, 840f + e2.ty, e2.opacity) {
        Text("≈ 370 trillion km · in the constellation Aquarius", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 28.sp, color = c(0x8c8c8c)))
    }
    At(sunX, mapY, reveal(t, 1.6f).opacity) {
        Box(Modifier.size((tpX - sunX).dp, 1.dp).background(c(0x444444)))
    }
    Sphere(40f, listOf(c(0xfff7d6), c(0xffc23a), c(0xff8a26)), glow = Color(0x80ffa03c), modifier = Modifier.offset((sunX - 20f).dp, (mapY - 20f).dp))
    Sphere(30f, STAR_C, glow = STAR_GLOW, modifier = Modifier.offset((tpX - 15f).dp, (mapY - 15f).dp))
    CenterLabel(sunX, mapY + 36f, reveal(t, 2f).opacity) { Text("OUR SUN", style = tpLabel(18f, c(0xd9a24e))) }
    CenterLabel(tpX, mapY + 36f, reveal(t, 2f).opacity) { Text("TRAPPIST-1", style = tpLabel(18f, c(0xe07a4a))) }
    if (moving) {
        At(photon - 6f, mapY - 6f) { Box(Modifier.size(12.dp).clip(CircleShape).background(Color.White)) }
    }
}
