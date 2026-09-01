package com.orbit.starsystems.scenes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
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
import com.orbit.starsystems.ui.Starfield
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin

private const val CN_TAU = 6.2831855f

private val CN_GOLD = c(0xffc24a)
private val CN_DIM = c(0x9a8f78)

private val CN_MOLTEN = listOf(c(0xffd89a), c(0xff6a2a), c(0x7a1a06))
private val CN_MOLTEN_GLOW = Color(0x77ff5a20)
private val CN_GAS = listOf(c(0xf4dcb4), c(0xc99a62), c(0x63421f))
private val CN_DUST = listOf(c(0xe8cfa8), c(0xb08a5c), c(0x4e3520))
private val CN_ICE = listOf(c(0xd4dce8), c(0x8a97a8), c(0x35404e))
private val CN_AMBER = listOf(c(0xf0d4a8), c(0xc08a4e), c(0x5a3a1c))

private val CN_STAR_STOPS = arrayOf(
    0f to c(0xfff8e0), 0.45f to c(0xffd267), 0.8f to c(0xf0a52e), 1f to c(0xc57a1e),
)
private val CN_HALO_STOPS = arrayOf(
    0f to Color(0x55ffc24a), 0.5f to Color(0x18ffa838), 0.85f to Color(0x00000000), 1f to Color(0x00000000),
)

private fun cnLabel(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = size.sp,
    letterSpacing = 0.14.em, color = color,
)

private fun cnBody(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = size.sp, color = color,
)

private class CnWorld(val label: String, val size: Float, val colors: List<Color>, val glow: Color?)

private val CN_WORLDS = listOf(
    CnWorld("JANSSEN", 42f, CN_MOLTEN, CN_MOLTEN_GLOW),
    CnWorld("GALILEO", 78f, CN_GAS, null),
    CnWorld("BRAHE", 56f, CN_DUST, null),
    CnWorld("HARRIOT", 50f, CN_ICE, null),
    CnWorld("LIPPERHEY", 94f, CN_AMBER, null),
)

@Composable
fun BoxScope.CnIntro(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    Starfield(t)

    val rise = animate(0.05f, 1f, 0.5f, 2.2f, Easing.easeOutCubic)(t)
    val breathe = 1f + 0.02f * sin(t * 1.3f)
    val starR = 166f * rise * breathe
    val cy = 830f

    RadialDisc(540f, cy, starR + 210f, CN_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(540f, cy, starR, CN_STAR_STOPS, 0.5f, 0.42f, 0.6f)
    CenterLabel(540f, cy + starR + 30f, reveal(t, 2.2f).opacity) {
        Text("55 CANCRI A", style = cnLabel(19f, CN_GOLD))
    }

    val rowY = 1300f
    CN_WORLDS.forEachIndexed { i, w ->
        val a = reveal(t, 2.7f + i * 0.26f, dur = 0.7f)
        val x = 156f + i * 192f
        Sphere(
            w.size, w.colors, glow = w.glow,
            modifier = Modifier
                .offset((x - w.size / 2f).dp, (rowY - w.size / 2f + a.ty).dp)
                .alpha(a.opacity),
        )
        CenterLabel(x, rowY + 64f, a.opacity) { Text(w.label, style = cnLabel(13f, CN_DIM)) }
    }

    Eyebrow("41 light-years away", CN_GOLD, 200f, e1)
    Head(headline("The ", "Copernicus", "\nsystem."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Five worlds circle a star bright enough to see — each named for an ", "early astronomer", "."))
}

private class CnOrbit(val r: Float, val speed: Float, val size: Float, val colors: List<Color>)

private val CN_ORBITS = listOf(
    CnOrbit(96f, 1.85f, 20f, CN_MOLTEN),
    CnOrbit(170f, 0.95f, 34f, CN_GAS),
    CnOrbit(246f, 0.60f, 26f, CN_DUST),
    CnOrbit(332f, 0.33f, 23f, CN_ICE),
    CnOrbit(432f, 0.13f, 44f, CN_AMBER),
)

@Composable
fun BoxScope.CnFamily(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val cx = 540f; val cy = 1050f
    Starfield(t)

    RadialDisc(cx, cy, 170f, CN_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(cx, cy, 54f, CN_STAR_STOPS, 0.5f, 0.42f, 0.6f)

    CN_ORBITS.forEachIndexed { i, o ->
        val a = animate(0f, 1f, 0.6f + i * 0.28f, 1.7f + i * 0.28f, Easing.easeOutCubic)(t)
        if (a <= 0.01f) return@forEachIndexed
        RingArc(cx, cy, o.r * 2f, o.r * 0.78f, 0f, c(0xffd48a).copy(alpha = 0.24f * a), 2f, dash = i > 2)
        val ang = t * o.speed + i * 1.15f
        val px = cx + cos(ang) * o.r
        val py = cy + sin(ang) * o.r * 0.39f
        Sphere(
            o.size, o.colors,
            modifier = Modifier.offset((px - o.size / 2f).dp, (py - o.size / 2f).dp).alpha(a),
        )
    }

    val s = reveal(t, 3.6f)
    At(90f, 1400f + s.ty, s.opacity) { StatCol("5", null, "known worlds", 76f, Color.White, CN_DIM) }
    At(400f, 1400f + s.ty, s.opacity) { StatCol("1996", null, "the first found", 76f, Color.White, CN_DIM) }
    At(768f, 1400f + s.ty, s.opacity) { StatCol("2007", null, "the fifth found", 76f, Color.White, CN_DIM) }

    Eyebrow("Found one at a time", c(0xffd48a), 200f, e1)
    Head(headline("A family of\n", "five worlds", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("In 2007 it became the ", "first star known to hold five planets", " beyond our own."))
}

@Composable
fun BoxScope.CnGiant(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xe8a86a)
    val cx = 240f; val cy = 1080f
    Starfield(t)

    val appear = reveal(t, 0.6f, dur = 1.0f).opacity
    val grow = animate(0.1f, 1f, 0.7f, 2.8f, Easing.easeOutCubic)(t)
    val rd = 706f * grow
    val rj = 668f * grow

    RadialDisc(cx, cy, 150f, CN_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(cx, cy, 46f, CN_STAR_STOPS, 0.5f, 0.42f, 0.6f)

    RingArc(cx, cy, rj * 2f, rj * 1.1f, 0f, Color.White.copy(alpha = 0.16f), 2f, dash = true, alpha = appear)
    RingArc(cx, cy, rd * 2f, rd * 1.1f, 0f, accent.copy(alpha = 0.45f), 3f, alpha = appear)

    val ang = -0.62f + sin(t * 0.15f) * 0.10f
    val px = cx + cos(ang) * rd
    val py = cy + sin(ang) * rd * 0.55f

    Canvas(Modifier.fillMaxSize().alpha(reveal(t, 2.6f).opacity)) {
        val k = size.width / 1080f
        drawLine(
            accent.copy(alpha = 0.35f),
            Offset(cx * k, cy * k), Offset(px * k, py * k),
            strokeWidth = 2f * k,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f * k, 11f * k)),
        )
    }

    Sphere(158f, CN_GAS, glow = Color(0x44e0b070), modifier = Modifier.offset((px - 79f).dp, (py - 79f).dp).alpha(appear))
    CenterLabel(px, py + 96f, reveal(t, 3.0f).opacity) { Text("55 CANCRI d", style = cnLabel(17f, accent)) }
    CenterLabel((cx + px) / 2f, (cy + py) / 2f - 48f, reveal(t, 3.2f).opacity) { Text("≈ 5.5 AU", style = cnLabel(16f, c(0xb8a68a))) }
    CenterLabel(cx + 300f, cy + rj * 0.55f - 46f, reveal(t, 3.6f).opacity) { Text("JUPITER'S ORBIT", style = cnLabel(14f, c(0x7f8898))) }

    val s = reveal(t, 4.0f)
    At(90f, 1470f + s.ty, s.opacity) { StatCol("14", " yr", "to circle once", 78f, accent, CN_DIM) }
    At(560f, 1470f + s.ty, s.opacity) { StatCol("2002", null, "the year it was found", 78f, accent, CN_DIM) }

    Eyebrow("The outermost world", accent, 200f, e1)
    Head(headline("A Jupiter of\n", "its own", "."), 86f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("55 Cancri d was the first exoplanet found on an orbit as ", "wide as Jupiter's", "."))
}

@Composable
fun BoxScope.CnBinary(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xffd27a)
    Starfield(t)

    val ax = 250f; val bx = 930f; val starY = 980f
    val grow = animate(0.15f, 1f, 0.5f, 2.2f, Easing.easeOutBack)(t)
    val flicker = 1f + 0.03f * sin(t * 2.1f)

    RadialDisc(ax, starY, 150f * grow + 190f, CN_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(ax, starY, 150f * grow, CN_STAR_STOPS, 0.5f, 0.42f, 0.6f)
    RadialDisc(
        bx, starY, 30f * grow * flicker + 60f,
        arrayOf(0f to Color(0x55e0603a), 0.5f to Color(0x14d8542a), 0.85f to Color(0x00000000), 1f to Color(0x00000000)),
        0.5f, 0.5f, 0.5f,
    )
    RadialDisc(
        bx, starY, 30f * grow * flicker,
        arrayOf(0f to c(0xffd2b0), 0.45f to c(0xe0704a), 0.82f to c(0xc04422), 1f to c(0x8a2a14)),
        0.5f, 0.42f, 0.6f,
    )

    CenterLabel(ax, starY + 180f, reveal(t, 2.4f).opacity) { Text("55 CANCRI A", style = cnLabel(18f, accent)) }
    CenterLabel(bx, starY + 76f, reveal(t, 2.6f).opacity) { Text("55 CANCRI B", style = cnLabel(18f, c(0xe08a68))) }

    val ruler = animate(0f, 1f, 2.2f, 3.6f, Easing.easeInOutCubic)(t)
    val rulerY = 1300f
    val nx = ax + (bx - ax) * 0.03f

    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val y = rulerY * k
        val sx = ax * k
        val ex = (ax + (bx - ax) * ruler) * k
        drawLine(Color.White.copy(alpha = 0.28f), Offset(sx, y), Offset(ex, y), strokeWidth = 2f * k)
        drawLine(Color.White.copy(alpha = 0.4f), Offset(sx, y - 16f * k), Offset(sx, y + 16f * k), strokeWidth = 2f * k)
        if (ruler > 0.98f) {
            drawLine(Color.White.copy(alpha = 0.4f), Offset(bx * k, y - 16f * k), Offset(bx * k, y + 16f * k), strokeWidth = 2f * k)
        }
        val na = animate(0f, 1f, 3.8f, 4.6f, Easing.easeOutCubic)(t)
        if (na > 0.01f) {
            drawLine(c(0x6fa8e0).copy(alpha = 0.9f * na), Offset(sx, y), Offset(nx * k, y), strokeWidth = 7f * k, cap = StrokeCap.Round)
            drawLine(c(0x6fa8e0).copy(alpha = 0.45f * na), Offset(nx * k, y + 8f * k), Offset(340f * k, 1408f * k), strokeWidth = 2f * k)
        }
    }

    CenterLabel((ax + bx) / 2f, rulerY - 76f, reveal(t, 3.4f).opacity) { Text("≈ 1,000 AU", style = cnLabel(26f, Color.White)) }
    At(348f, 1414f, animate(0f, 1f, 4.2f, 5.0f, Easing.easeOutCubic)(t)) {
        Text("our whole solar system — 30 AU", style = cnBody(24f, c(0x6fa8e0)))
    }

    Eyebrow("Two stars, one system", accent, 200f, e1)
    Head(headline("A sun and a\n", "distant ember", "."), 82f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Its red-dwarf companion drifts ", "a thousand times further", " than Earth sits from the Sun."))
}

@Composable
fun BoxScope.CnNaked(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xcfd8ff)
    Starfield(t)

    val axisY = 1080f; val x0 = 130f; val x1 = 950f
    fun mx(m: Float) = x0 + (m - 1f) / 6f * (x1 - x0)
    val draw = animate(0f, 1f, 0.6f, 2.4f, Easing.easeInOutCubic)(t)
    val appear = reveal(t, 0.5f, dur = 0.9f).opacity
    val markX = mx(5.95f)
    val limitX = mx(6.5f)

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val y = axisY * k
        drawLine(Color.White.copy(alpha = 0.14f), Offset(x0 * k, y), Offset((x0 + (x1 - x0) * draw) * k, y), strokeWidth = 2f * k)

        for (i in 0..6) {
            val on = (draw * 7f - i).coerceIn(0f, 1f)
            if (on <= 0f) continue
            val r = (23f - i * 3.1f) * k
            val a = (0.95f - i * 0.115f) * on
            val tw = 0.82f + 0.18f * sin(t * 1.6f + i * 1.4f)
            val sx = mx(1f + i) * k
            drawCircle(Color.White.copy(alpha = (a * 0.22f).coerceIn(0f, 1f)), radius = r * 2.1f, center = Offset(sx, y))
            drawCircle(Color.White.copy(alpha = (a * tw).coerceIn(0f, 1f)), radius = r, center = Offset(sx, y))
        }

        val la = animate(0f, 1f, 2.6f, 3.4f, Easing.easeOutCubic)(t)
        if (la > 0.01f) {
            drawLine(
                accent.copy(alpha = 0.55f * la),
                Offset(limitX * k, (axisY - 92f) * k), Offset(limitX * k, (axisY + 92f) * k),
                strokeWidth = 2f * k,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f * k, 10f * k)),
            )
        }

        val ma = animate(0f, 1f, 3.2f, 4.0f, Easing.easeOutBack)(t)
        if (ma > 0.01f) {
            val pulse = 0.6f + 0.4f * sin(t * 2.6f)
            drawCircle(
                CN_GOLD.copy(alpha = (0.85f * ma * pulse).coerceIn(0f, 1f)),
                radius = 36f * k * ma, center = Offset(markX * k, y), style = Stroke(width = 3f * k),
            )
            drawLine(CN_GOLD.copy(alpha = 0.5f * ma), Offset(markX * k, (axisY - 44f) * k), Offset(markX * k, 1004f * k), strokeWidth = 2f * k)
        }
    }

    CenterLabel(markX - 60f, 950f, animate(0f, 1f, 3.4f, 4.2f, Easing.easeOutCubic)(t)) {
        Text("55 CANCRI · 5.95", style = cnLabel(17f, CN_GOLD))
    }
    CenterLabel(limitX - 90f, 1194f, animate(0f, 1f, 2.8f, 3.6f, Easing.easeOutCubic)(t)) {
        Text("LIMIT OF THE EYE", style = cnLabel(14f, accent))
    }
    listOf(1, 3, 5, 7).forEach { m ->
        CenterLabel(mx(m.toFloat()), 1124f, appear * draw) { Text("MAG " + m, style = cnLabel(12f, c(0x6d7488))) }
    }

    val s = reveal(t, 4.4f)
    At(90f, 1330f + s.ty, s.opacity) { StatCol("5.95", null, "how bright it looks", 78f, Color.White, CN_DIM) }
    At(600f, 1330f + s.ty, s.opacity) { StatCol("6.5", null, "faintest the eye can see", 78f, Color.White, CN_DIM) }

    Eyebrow("Look up and find it", accent, 200f, e1)
    Head(headline("Visible to the\n", "naked eye", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Under a dark sky it sits ", "just inside", " what an unaided eye can catch."))
}

private val CN_VEINS: List<FloatArray> = run {
    var s = 0x55CA71
    fun rnd(): Float { s = s * 1664525 + 1013904223; return ((s ushr 9) and 0xFFFF) / 65535f }
    List(30) { floatArrayOf(rnd(), rnd(), rnd(), rnd(), rnd()) }
}

@Composable
fun BoxScope.CnLava(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xff6a3a)
    val cx = 540f; val cy = 960f
    val appear = reveal(t, 0.4f, dur = 1.0f).opacity
    val grow = animate(0.25f, 1f, 0.4f, 2.4f, Easing.easeOutCubic)(t)
    val r = 296f * grow

    RadialDisc(
        cx, cy, r + 190f,
        arrayOf(0f to Color(0x66ff5a20), 0.5f to Color(0x1ad8401a), 0.85f to Color(0x00000000), 1f to Color(0x00000000)),
        0.5f, 0.5f, 0.5f,
    )

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        val rr = r * k
        if (rr <= 0f) return@Canvas
        val lit = Offset(ctr.x - rr * 0.28f, ctr.y - rr * 0.32f)

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(0f to c(0xff8f4a), 0.5f to c(0x9e2c0e), 1f to c(0x2c0a04)),
                center = lit, radius = rr * 1.75f,
            ),
            radius = rr, center = ctr,
        )

        val body = Path().apply { addOval(Rect(ctr.x - rr, ctr.y - rr, ctr.x + rr, ctr.y + rr)) }
        clipPath(body) {
            CN_VEINS.forEach { v ->
                val a0 = v[0] * CN_TAU
                val drift = (v[1] - 0.5f) * 1.3f
                val rStart = 0.10f + v[2] * 0.22f
                val rEnd = 0.58f + v[3] * 0.40f
                val hot = 0.35f + 0.65f * (0.5f + 0.5f * sin(t * 1.6f + v[4] * CN_TAU))
                val p = Path()
                for (i in 0..8) {
                    val u = i / 8f
                    val ang = a0 + drift * u
                    val rad = (rStart + (rEnd - rStart) * u) * rr
                    val vx = ctr.x + cos(ang) * rad
                    val vy = ctr.y + sin(ang) * rad * 0.96f
                    if (i == 0) p.moveTo(vx, vy) else p.lineTo(vx, vy)
                }
                drawPath(p, c(0xff8a3a).copy(alpha = 0.20f * hot), style = Stroke(width = 10f * k, cap = StrokeCap.Round))
                drawPath(p, c(0xffdc92).copy(alpha = 0.72f * hot), style = Stroke(width = 3.2f * k, cap = StrokeCap.Round))
            }
        }

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(0.42f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.62f)),
                center = lit, radius = rr * 1.02f,
            ),
            radius = rr, center = ctr,
        )

        for (i in 0..5) {
            val ph = ((t * 0.34f) + i * 0.17f) % 1f
            val ex = cx + (i - 2.5f) * 96f + sin(t * 1.1f + i) * 22f
            val ey = cy - r * 0.86f - ph * 200f
            val ea = (sin(ph * 3.1415927f) * 0.55f).coerceIn(0f, 1f)
            drawCircle(c(0xffb46a).copy(alpha = ea), radius = (5f - ph * 2.4f) * k, center = Offset(ex * k, ey * k))
        }
    }

    val temp = interpolate(listOf(1.6f, 4.4f), listOf(0f, 2400f), Easing.easeOutCubic)(t)
    val s = reveal(t, 1.6f)
    At(90f, 1400f + s.ty, s.opacity) { StatCol(fmt(temp), " °C", "on the day side", 92f, c(0xffb46a), CN_DIM) }
    At(628f, 1400f + s.ty, s.opacity) { StatCol("1.9", "× Earth", "across", 92f, c(0xffb46a), CN_DIM) }

    Eyebrow("55 Cancri e", accent, 200f, e1)
    Head(headline("A world of\n", "molten rock", "."), 86f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("It orbits so close that its surface never cools enough to ", "harden into stone", "."))
}

@Composable
fun BoxScope.CnYear(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xff9a4a)
    val cx = 540f; val cy = 1000f; val r = 292f
    val appear = reveal(t, 0.5f, dur = 0.9f).opacity
    val sweep = animate(0f, 265.5f, 1.0f, 3.0f, Easing.easeInOutCubic)(t)

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        val rr = r * k

        drawCircle(Color.White.copy(alpha = 0.10f), radius = rr, center = ctr, style = Stroke(width = 2f * k))
        for (i in 0 until 24) {
            val ang = (-90f + i * 15f) * 0.017453292f
            val long = i % 6 == 0
            val inner = rr - (if (long) 26f else 14f) * k
            drawLine(
                Color.White.copy(alpha = if (long) 0.42f else 0.18f),
                Offset(ctr.x + cos(ang) * inner, ctr.y + sin(ang) * inner),
                Offset(ctr.x + cos(ang) * rr, ctr.y + sin(ang) * rr),
                strokeWidth = (if (long) 3f else 2f) * k,
            )
        }

        drawArc(
            color = accent.copy(alpha = 0.85f),
            startAngle = -90f, sweepAngle = sweep, useCenter = false,
            topLeft = Offset(ctr.x - rr, ctr.y - rr), size = Size(rr * 2f, rr * 2f),
            style = Stroke(width = 9f * k, cap = StrokeCap.Round),
        )

        val lap = (t / 2.4f) % 1f
        val pa = (-90f + lap * 360f) * 0.017453292f
        val pp = Offset(ctr.x + cos(pa) * rr, ctr.y + sin(pa) * rr)
        drawCircle(CN_MOLTEN_GLOW, radius = 30f * k, center = pp)
        drawCircle(c(0xffd89a), radius = 13f * k, center = pp)
    }

    val ca = reveal(t, 2.2f)
    CenterLabel(cx, cy - 118f + ca.ty, ca.opacity) {
        Text(
            "17.7",
            style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 128.sp, color = Color.White, lineHeight = 128.sp),
        )
        Text("HOURS", style = cnLabel(22f, accent))
        Text("one whole year", style = cnBody(26f, CN_DIM))
    }

    val s = reveal(t, 4.0f)
    At(90f, 1424f + s.ty, s.opacity) { StatCol("495", null, "of its years in one of ours", 76f, accent, CN_DIM) }
    At(628f, 1424f + s.ty, s.opacity) { StatCol("0.015", " AU", "from its star", 76f, accent, CN_DIM) }

    Eyebrow("The innermost world", accent, 200f, e1)
    Head(headline("A year in\n", "eighteen hours", "."), 82f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Fall asleep tonight and 55 Cancri e will have lived ", "a full year", " by morning."))
}

@Composable
fun BoxScope.CnTidal(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xffb07a)
    val cx = 660f; val cy = 1000f; val r = 232f
    val appear = reveal(t, 0.5f, dur = 1.0f).opacity

    RadialDisc(
        -60f, cy, 460f,
        arrayOf(0f to c(0xfff0c8), 0.35f to c(0xffc24a), 0.7f to Color(0x33ff9a2a), 1f to Color(0x00000000)),
        0.5f, 0.5f, 0.5f,
    )

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        val rr = r * k
        val lit = Offset(ctr.x - rr * 0.72f, ctr.y - rr * 0.16f)

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(0f to c(0xfff2d0), 0.28f to c(0xffa03a), 0.62f to c(0xa8300e), 1f to c(0x35100a)),
                center = lit, radius = rr * 1.9f,
            ),
            radius = rr, center = ctr,
        )

        val body = Path().apply { addOval(Rect(ctr.x - rr, ctr.y - rr, ctr.x + rr, ctr.y + rr)) }
        clipPath(body) {
            drawRect(
                brush = Brush.horizontalGradient(
                    0f to Color.Transparent, 0.34f to Color.Transparent,
                    0.72f to Color(0xcc140604), 1f to Color(0xf00b0403),
                    startX = ctr.x - rr, endX = ctr.x + rr,
                ),
                topLeft = Offset(ctr.x - rr, ctr.y - rr), size = Size(rr * 2f, rr * 2f),
            )
        }

        val fa = animate(0f, 1f, 2.4f, 3.4f, Easing.easeOutCubic)(t)
        if (fa > 0.01f) {
            for (i in 0..4) {
                val ph = ((t * 0.26f) + i * 0.2f) % 1f
                val ang = -3.0f + ph * 2.5f
                val rad = rr + 62f * k
                val a = (sin(ph * 3.1415927f) * 0.8f * fa).coerceIn(0f, 1f)
                drawCircle(
                    c(0xffb07a).copy(alpha = a), radius = 7f * k,
                    center = Offset(ctr.x + cos(ang) * rad, ctr.y + sin(ang) * rad),
                )
            }
        }
    }

    val la = reveal(t, 2.8f).opacity
    val na = reveal(t, 3.2f).opacity
    CenterLabel(cx - 236f, cy + 296f, la) { Text("DAY SIDE", style = cnLabel(17f, c(0xffc98a))) }
    CenterLabel(cx - 236f, cy + 330f, la) { Text("≈ 2,400 °C", style = cnBody(24f, CN_DIM)) }
    CenterLabel(cx + 196f, cy + 296f, na) { Text("NIGHT SIDE", style = cnLabel(17f, c(0x8e93a8))) }
    CenterLabel(cx + 196f, cy + 330f, na) { Text("never dawns", style = cnBody(24f, CN_DIM)) }
    CenterLabel(cx, cy - r - 120f, reveal(t, 3.6f).opacity) { Text("HEAT CARRIED ACROSS", style = cnLabel(14f, accent)) }

    Eyebrow("Locked to its star", accent, 200f, e1)
    Head(headline("One face,\n", "forever", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("One hemisphere burns in endless day — yet the dark side stays ", "strangely warm", "."))
}

@Composable
fun BoxScope.CnDiamond(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xcfe6ff)
    Starfield(t)

    val pop = animate(0.05f, 1f, 0.6f, 2.2f, Easing.easeOutBack)(t)
    val apexY = 690f; val girdleY = 880f; val baseY = 1160f
    val cx = 540f; val hw = 186f
    val bx0 = 300f; val bw = 620f; val bMax = 1.4f
    val thresh = bx0 + bw * (1f / bMax)

    Canvas(Modifier.fillMaxSize().alpha(pop.coerceIn(0f, 1f))) {
        val k = size.width / 1080f
        val scale = pop.coerceIn(0.05f, 1.2f)
        fun gx(x: Float) = (cx + (x - cx) * scale) * k
        fun gy(y: Float) = (930f + (y - 930f) * scale) * k

        val gem = Path().apply {
            moveTo(gx(cx), gy(apexY))
            lineTo(gx(cx + hw), gy(girdleY))
            lineTo(gx(cx), gy(baseY))
            lineTo(gx(cx - hw), gy(girdleY))
            close()
        }

        val shift = ((t * 0.22f) % 1f - 0.5f) * 520f * k
        drawPath(
            gem,
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0f to c(0x3f5f84), 0.34f to c(0x9fc4e4), 0.5f to c(0xf2fbff),
                    0.66f to c(0xa8cde8), 1f to c(0x35506f),
                ),
                start = Offset(gx(cx - hw) + shift, gy(apexY)),
                end = Offset(gx(cx + hw) + shift, gy(baseY)),
            ),
        )
        drawPath(gem, accent.copy(alpha = 0.75f), style = Stroke(width = 3f * k))

        val facets = listOf(
            floatArrayOf(cx - hw, girdleY, cx + hw, girdleY),
            floatArrayOf(cx, apexY, cx, baseY),
            floatArrayOf(cx - hw, girdleY, cx, baseY - 150f),
            floatArrayOf(cx + hw, girdleY, cx, baseY - 150f),
            floatArrayOf(cx - hw * 0.5f, girdleY - 95f, cx + hw * 0.5f, girdleY - 95f),
        )
        facets.forEach { f ->
            drawLine(
                Color.White.copy(alpha = 0.30f),
                Offset(gx(f[0]), gy(f[1])), Offset(gx(f[2]), gy(f[3])),
                strokeWidth = 2f * k,
            )
        }

        val sparks = listOf(
            floatArrayOf(cx, apexY, 0f), floatArrayOf(cx + hw, girdleY, 2.1f),
            floatArrayOf(cx - hw, girdleY, 4.2f), floatArrayOf(cx, baseY, 1.1f),
        )
        sparks.forEach { s ->
            val a = (0.3f + 0.7f * sin(t * 2.4f + s[2])).coerceIn(0f, 1f)
            val len = (16f + 20f * a) * k
            val p = Offset(gx(s[0]), gy(s[1]))
            drawLine(Color.White.copy(alpha = a), Offset(p.x - len, p.y), Offset(p.x + len, p.y), strokeWidth = 2.5f * k, cap = StrokeCap.Round)
            drawLine(Color.White.copy(alpha = a), Offset(p.x, p.y - len), Offset(p.x, p.y + len), strokeWidth = 2.5f * k, cap = StrokeCap.Round)
        }
    }

    val b1 = animate(0f, 1.15f, 2.6f, 3.6f, Easing.easeOutCubic)(t)
    val b2 = animate(0f, 0.8f, 3.6f, 4.6f, Easing.easeOutCubic)(t)

    Canvas(Modifier.fillMaxSize().alpha(reveal(t, 2.4f).opacity)) {
        val k = size.width / 1080f
        listOf(1350f to b1, 1440f to b2).forEachIndexed { i, row ->
            val y = row.first * k
            val col = if (i == 0) CN_GOLD else accent
            drawLine(Color.White.copy(alpha = 0.10f), Offset(bx0 * k, y), Offset((bx0 + bw) * k, y), strokeWidth = 22f * k, cap = StrokeCap.Round)
            if (row.second > 0.01f) {
                drawLine(col, Offset(bx0 * k, y), Offset((bx0 + bw * (row.second / bMax)) * k, y), strokeWidth = 22f * k, cap = StrokeCap.Round)
            }
        }
        drawLine(
            Color.White.copy(alpha = 0.45f),
            Offset(thresh * k, 1310f * k), Offset(thresh * k, 1482f * k),
            strokeWidth = 2f * k,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * k, 10f * k)),
        )
    }

    val ba = reveal(t, 2.4f)
    At(90f, 1256f + ba.ty, ba.opacity) { Text("CARBON-TO-OXYGEN RATIO", style = cnLabel(14f, c(0x7f8898))) }
    At(90f, 1336f + ba.ty, ba.opacity) { Text("2012 estimate", style = cnBody(24f, CN_GOLD)) }
    At(90f, 1426f + ba.ty, ba.opacity) { Text("measured since", style = cnBody(24f, accent)) }
    At(thresh - 74f, 1498f, reveal(t, 3.4f).opacity) { Text("carbon-rich", style = cnBody(20f, c(0x7f8898))) }

    Eyebrow("A headline that faded", accent, 200f, e1)
    Head(headline("The ", "diamond", "\nplanet."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("A diamond mantle was proposed in 2012 — then a sharper look found ", "far less carbon", "."))
}

private val CN_MOLECULES = listOf("CO", "CO₂", "CO", "CO₂", "CO")

@Composable
fun BoxScope.CnAtmos(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x9fd6c0)
    Starfield(t)

    val limbCy = 2600f; val limbR = 1240f
    val limbTop = limbCy - limbR
    val appear = reveal(t, 0.4f, dur = 1.0f).opacity
    val shellA = animate(0f, 1f, 1.0f, 2.8f, Easing.easeOutCubic)(t)

    RadialDisc(
        540f, limbCy, limbR,
        arrayOf(0f to c(0xffc06a), 0.35f to c(0xd8461a), 0.7f to c(0x5a1206), 1f to c(0x140502)),
        0.5f, 0f, 0.20f,
    )

    listOf(26f to 0.34f, 92f to 0.15f, 172f to 0.08f, 264f to 0.05f).forEach { shell ->
        val rr = limbR + shell.first
        RingArc(
            540f, limbCy, rr * 2f, rr * 2f, 0f,
            accent.copy(alpha = shell.second * shellA),
            if (shell.first < 40f) 3f else 52f,
            alpha = appear,
        )
    }

    CN_MOLECULES.forEachIndexed { i, m ->
        val ph = ((t * 0.20f) + i * 0.19f) % 1f
        val x = 138f + i * 176f + sin(t * 0.9f + i * 1.7f) * 16f
        val y = limbTop - 24f - ph * 250f
        val a = (sin(ph * 3.1415927f) * 0.95f).coerceIn(0f, 1f) * shellA
        At(x, y, a) { Text(m, style = cnLabel(26f, accent)) }
    }

    val s = reveal(t, 3.2f)
    At(90f, 1096f + s.ty, s.opacity) { Text("DETECTED BY JWST · 2024", style = cnLabel(15f, accent)) }
    CenterLabel(540f, limbTop + 46f, reveal(t, 4.0f).opacity) { Text("A MAGMA OCEAN BELOW", style = cnLabel(14f, c(0xffd0a0))) }

    Eyebrow("The strongest sign yet", accent, 200f, e1)
    Head(headline("Air above the\n", "magma", "."), 86f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Carbon monoxide and CO₂ appear to be ", "venting from the lava", " below."))
}

private class CnStop(val ly: Float, val label: String, val size: Float, val color: Color)

private val CN_LADDER = listOf(
    CnStop(0f, "OUR SUN", 22f, c(0xffcf6a)),
    CnStop(4.4f, "ALPHA CENTAURI", 15f, c(0xffcf8a)),
    CnStop(8.6f, "SIRIUS", 15f, c(0x8fc0ff)),
)

@Composable
fun BoxScope.CnDistance(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f)
    val accent = c(0x9cc4ec)
    Starfield(t)

    val axisY = 1210f; val x0 = 120f; val x1 = 960f
    val span = ln(42f)
    fun lx(ly: Float) = x0 + ln(1f + ly) / span * (x1 - x0)
    val draw = animate(0f, 1f, 1.6f, 3.6f, Easing.easeInOutCubic)(t)
    val num = interpolate(listOf(1.2f, 4.0f), listOf(0f, 41f), Easing.easeOutCubic)(t)

    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val y = axisY * k
        val edge = x0 + (x1 - x0) * draw
        drawLine(c(0x3a4660), Offset(x0 * k, y), Offset(edge * k, y), strokeWidth = 2f * k)
        CN_LADDER.forEach { st ->
            val sx = lx(st.ly)
            val on = (edge - sx).coerceIn(0f, 60f) / 60f
            if (on <= 0f) return@forEach
            drawCircle(st.color.copy(alpha = 0.26f * on), radius = st.size * 2f * k, center = Offset(sx * k, y))
            drawCircle(st.color.copy(alpha = on), radius = st.size * k, center = Offset(sx * k, y))
        }
        val ma = animate(0f, 1f, 3.4f, 4.2f, Easing.easeOutBack)(t)
        if (ma > 0.01f) {
            val pulse = 0.55f + 0.45f * sin(t * 2.4f)
            val mp = Offset(lx(41f) * k, y)
            drawCircle(CN_GOLD.copy(alpha = (0.8f * ma * pulse).coerceIn(0f, 1f)), radius = 40f * k * ma, center = mp, style = Stroke(width = 3f * k))
            drawCircle(c(0xffd267), radius = 17f * k * ma, center = mp)
        }
    }

    CN_LADDER.forEach { st ->
        val la = animate(0f, 1f, 2.4f + st.ly * 0.06f, 3.2f + st.ly * 0.06f, Easing.easeOutCubic)(t)
        CenterLabel(lx(st.ly), axisY + 50f, la) { Text(st.label, style = cnLabel(13f, st.color)) }
    }
    CenterLabel(lx(41f) - 130f, axisY - 98f, animate(0f, 1f, 3.8f, 4.6f, Easing.easeOutCubic)(t)) {
        Text("55 CANCRI", style = cnLabel(18f, CN_GOLD))
    }

    Eyebrow("How far is it?", accent, 300f, e1)
    At(84f, 370f + e2.ty, e2.opacity) {
        Text(
            fmt(num),
            style = TextStyle(
                fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 200.sp,
                color = Color.White, letterSpacing = (-0.04).em, lineHeight = 200.sp,
            ),
        )
    }
    At(84f, 640f + e2.ty, e2.opacity) { Text("light-years away.", style = cnBody(54f, Color.White)) }
    At(84f, 726f + e2.ty, e2.opacity) { Text("≈ 390 trillion km · in the constellation Cancer", style = cnBody(26f, c(0x8794ac))) }

    BottomLine(1648f, reveal(t, 6.4f), body("Its light set out before we had found ", "a single planet", " around another star."))
}
