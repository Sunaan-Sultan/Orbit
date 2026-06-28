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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.orbit.starsystems.core.Easing
import com.orbit.starsystems.core.animate
import com.orbit.starsystems.core.reveal
import com.orbit.starsystems.ui.OrbitFont
import com.orbit.starsystems.ui.Sphere
import com.orbit.starsystems.ui.Starfield
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

// Five facts of the Kepler-186 system — each given a deliberately different visual
// language so the system never feels like one animation on repeat.

private fun kpLabel(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = size.sp,
    letterSpacing = 0.14.em, color = color,
)

// ───────────────────── 1 · A smaller, redder sun (size comparison) ─────────────────────

@Composable
fun BoxScope.KpDwarf(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xff8a5a)

    val sunA = reveal(t, 1.0f, dur = 0.9f).opacity
    val sunCx = 350f; val sunCy = 980f; val sunR = 232f

    // The red dwarf grows in beside our Sun and gently flares.
    val grow = animate(0.2f, 1f, 1.4f, 2.6f, Easing.easeOutBack)(t)
    val flare = 1f + 0.06f * sin(t * 2.2f)
    val dCx = 792f; val dCy = 1024f; val dR = 126f * grow * flare

    RadialDisc(
        sunCx, sunCy, sunR,
        arrayOf(0f to c(0xfff6d6), 0.45f to c(0xffd451), 0.78f to c(0xffa838), 1f to c(0xff8a1e)),
        0.5f, 0.42f, 0.6f,
    )
    RadialDisc(
        dCx, dCy, dR + 120f,
        arrayOf(0f to Color(0x55ff6a3a), 0.5f to Color(0x18ff5a2e), 0.8f to Color(0x00000000), 1f to Color(0x00000000)),
        0.5f, 0.5f, 0.5f,
    )
    RadialDisc(
        dCx, dCy, dR,
        arrayOf(0f to c(0xffd9b0), 0.45f to c(0xff7a4a), 0.8f to c(0xd0401f), 1f to c(0x9a2a14)),
        0.5f, 0.42f, 0.6f,
    )

    CenterLabel(sunCx, sunCy + sunR + 18f, sunA) { Text("OUR SUN", style = kpLabel(18f, c(0xffd07a))) }
    CenterLabel(dCx, dCy + 150f, reveal(t, 2.4f).opacity) { Text("KEPLER-186", style = kpLabel(18f, c(0xffa074))) }

    Eyebrow("A different kind of star", accent, 200f, e1)
    Head(headline("A smaller,\n", "redder", " sun."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Kepler-186 is a red dwarf — about ", "half the Sun's size", " and far cooler, glowing a deep ember red."))
}

// ───────────────────── 2 · Caught in transit (a live light curve) ─────────────────────

@Composable
fun BoxScope.KpTransit(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xffd36a)
    val starCx = 540f; val starCy = 700f; val starR = 250f
    val appear = reveal(t, 0.7f, dur = 0.9f).opacity

    RadialDisc(
        starCx, starCy, starR + 90f,
        arrayOf(0f to Color(0x44ffd36a), 0.55f to Color(0x12ffae40), 0.85f to Color(0x00000000), 1f to Color(0x00000000)),
        0.5f, 0.5f, 0.5f,
    )
    RadialDisc(
        starCx, starCy, starR,
        arrayOf(0f to c(0xfff0d0), 0.5f to c(0xffc658), 0.82f to c(0xff9433), 1f to c(0xe06f22)),
        0.5f, 0.44f, 0.6f,
    )

    val p = (t * 0.16f) % 1f
    val dip = 0.14f
    fun bright(x: Float) = 1f - dip * exp(-((x - 0.5f) * (x - 0.5f)) / (2f * 0.06f * 0.06f))
    val planetX = -150f + p * 1380f

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        // the transiting planet — a dark disc sliding across the star's face
        val pc = Offset(planetX * k, (starCy + 6f) * k)
        drawCircle(c(0x1a1206), radius = 30f * k, center = pc)
        drawCircle(Color.Black.copy(alpha = 0.88f), radius = 26f * k, center = pc)

        // ── the light-curve readout ──
        val gx0 = 130f * k; val gx1 = 950f * k
        val gyTop = 1300f * k; val gyBot = 1470f * k
        drawLine(Color.White.copy(alpha = 0.16f), Offset(gx0, gyTop - 18f * k), Offset(gx0, gyBot + 26f * k), strokeWidth = 2f * k)
        drawLine(Color.White.copy(alpha = 0.16f), Offset(gx0, gyBot + 26f * k), Offset(gx1 + 12f * k, gyBot + 26f * k), strokeWidth = 2f * k)
        drawLine(c(0xffd36a).copy(alpha = 0.22f), Offset(gx0, gyTop), Offset(gx1, gyTop), strokeWidth = 1.5f * k)

        fun gx(u: Float) = gx0 + u * (gx1 - gx0)
        fun gy(b: Float) = gyTop + ((1f - b) / dip) * (gyBot - gyTop)
        val path = Path()
        var u = 0f; var first = true
        while (u <= 1.001f) {
            val x = gx(u); val y = gy(bright(u))
            if (first) { path.moveTo(x, y); first = false } else path.lineTo(x, y)
            u += 0.02f
        }
        drawPath(path, c(0xffe08a), style = Stroke(width = 4f * k, cap = StrokeCap.Round))

        // marker riding the curve in lock-step with the planet
        val mx = gx(p); val my = gy(bright(p))
        drawCircle(c(0xffd36a).copy(alpha = 0.4f), radius = 14f * k, center = Offset(mx, my))
        drawCircle(Color.White, radius = 7f * k, center = Offset(mx, my))
    }

    At(132f, 1240f, appear) { Text("STAR BRIGHTNESS", style = kpLabel(14f, c(0xffc878))) }

    Eyebrow("How we found them", accent, 200f, e1)
    Head(headline("Caught in\n", "transit", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Each world betrays itself by ", "dimming its star", " a fraction every time it crosses in front."))
}

// ───────────────────── 3 · The Goldilocks zone (a temperature band) ─────────────────────

@Composable
fun BoxScope.KpHz(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x6fcaa0)
    val cx = 540f; val cy = 1000f
    val appear = reveal(t, 0.6f, dur = 1.0f).opacity
    val rIn = 150f; val rOut = 430f
    val grow = animate(0f, 1f, 0.5f, 1.8f, Easing.easeOutCubic)(t)

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        // hot (inner, red) → temperate (green) → cold (outer, blue)
        val hot = Color(0xffd34a2a); val temperate = Color(0xff43c98a); val cold = Color(0xff3a6fd0)
        val steps = 60
        for (i in steps downTo 0) {
            val u = i.toFloat() / steps
            val r = (rIn + (rOut - rIn) * u) * k * grow
            val col = if (u < 0.5f) lerp(hot, temperate, u / 0.5f) else lerp(temperate, cold, (u - 0.5f) / 0.5f)
            drawCircle(col.copy(alpha = 0.16f), radius = r, center = ctr)
        }
    }

    // the small red-dwarf star at the centre
    RadialDisc(
        cx, cy, 110f,
        arrayOf(0f to c(0xffd9b0), 0.45f to c(0xff7a4a), 0.82f to c(0xd0401f), 1f to c(0x8a2410)),
        0.5f, 0.42f, 0.6f,
    )

    // planet f circling in the temperate band
    val rMid = (rIn + rOut) / 2f
    val ang = t * 0.55f
    val px = cx + cos(ang) * rMid
    val py = cy + sin(ang) * rMid
    RingArc(cx, cy, rMid * 2f, rMid * 2f, 0f, c(0x43c98a).copy(alpha = 0.5f), 2f, dash = true)
    Sphere(46f, listOf(c(0xbfeaff), c(0x3f9fd0), c(0x123a64)), glow = Color(0x6643c98a), modifier = Modifier.offset((px - 23f).dp, (py - 23f).dp))
    CenterLabel(px, py + 34f, reveal(t, 2.2f).opacity) { Text("KEPLER-186f", style = kpLabel(16f, accent)) }

    Eyebrow("The Goldilocks zone", accent, 200f, e1)
    Head(headline("Not too hot,\n", "not too cold", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Planet f circles in the ", "temperate band", " — where a rocky world could keep liquid water."))
}

// ───────────────────── 4 · A sky of endless sunset (surface view) ─────────────────────

private val KP_SKY_STARS: List<FloatArray> = run {
    var s = 0x4F3A21
    fun rnd(): Float { s = s * 1664525 + 1013904223; return ((s ushr 9) and 0xFFFF) / 65535f }
    List(70) { floatArrayOf(rnd(), rnd(), 0.8f + rnd() * 1.6f, 0.3f + rnd() * 0.5f) }
}

@Composable
fun BoxScope.KpSunset(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xff7a52)
    val horizon = 1300f
    val appear = reveal(t, 0.4f, dur = 1.0f).opacity
    val shimmer = sin(t * 1.3f) * 6f

    // reddish sky + dim daytime stars (the sun is too faint to wash them out)
    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val hY = horizon * k
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color(0xff1a0a10), 0.45f to Color(0xff5a1e18), 0.8f to Color(0xffb5482a), 1f to Color(0xffe07a3a),
                startY = 0f, endY = hY,
            ),
            size = Size(size.width, hY),
        )
        KP_SKY_STARS.forEach { st ->
            val yy = st[1] * (horizon - 80f)
            val tw = 0.5f + 0.5f * sin(t * 1.6f + st[0] * 24f)
            drawCircle(Color(0xffe0d0ff), radius = st[2] * k, center = Offset(st[0] * size.width, yy * k), alpha = (st[3] * tw).coerceIn(0f, 1f) * 0.55f)
        }
    }

    // the huge, dim red sun resting on the horizon
    val sunCy = horizon - 60f + shimmer
    RadialDisc(
        540f, sunCy, 470f,
        arrayOf(0f to Color(0x55ff6a3a), 0.55f to Color(0x18d0401f), 0.85f to Color(0x00000000), 1f to Color(0x00000000)),
        0.5f, 0.5f, 0.5f,
    )
    RadialDisc(
        540f, sunCy, 300f,
        arrayOf(0f to c(0xff9a5a), 0.5f to c(0xff5a30), 0.82f to c(0xc83418), 1f to c(0x922510)),
        0.5f, 0.5f, 0.5f,
    )

    // the ground, drawn over the sun's lower limb, with dark vegetation silhouettes
    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val hY = horizon * k
        drawRect(
            brush = Brush.verticalGradient(0f to Color(0xff2a1410), 1f to Color(0xff070403), startY = hY, endY = size.height),
            topLeft = Offset(0f, hY), size = Size(size.width, size.height - hY),
        )
        fun frond(baseX: Float) {
            val bx = baseX * k
            for (j in -2..2) {
                val a = -1.5708f + j * 0.34f
                val len = (160f + (j % 2) * 22f) * k
                drawLine(Color(0xff0a0608), Offset(bx, hY), Offset(bx + cos(a) * len, hY + sin(a) * len), strokeWidth = 7f * k, cap = StrokeCap.Round)
            }
        }
        frond(175f); frond(910f)
    }

    Eyebrow("Standing on Kepler-186f", accent, 200f, e1)
    Head(headline("A sky of endless\n", "sunset", "."), 80f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Its faint red sun hangs dim and huge — high noon would glow like a perpetual ", "dusk", "."))
}

// ───────────────────── 5 · Hidden in the Swan (constellation map) ─────────────────────

private class KpStar(val x: Float, val y: Float, val r: Float, val name: String = "")

private val CYGNUS = listOf(
    KpStar(540f, 460f, 13f, "Deneb"),   // 0 · tail
    KpStar(545f, 700f, 9f),             // 1
    KpStar(548f, 930f, 14f, "Sadr"),    // 2 · heart of the cross
    KpStar(305f, 880f, 9f),             // 3 · left wing
    KpStar(150f, 845f, 8f, "Gienah"),   // 4
    KpStar(785f, 985f, 9f),             // 5 · right wing
    KpStar(935f, 1035f, 8f),            // 6
    KpStar(560f, 1215f, 9f, "Albireo"), // 7 · beak
)
private val CYG_EDGES = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 2 to 5, 5 to 6, 2 to 7)

@Composable
fun BoxScope.KpCygnus(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x9fc2ff)
    Starfield(t)
    val draw = animate(0f, 1f, 0.8f, 3.2f, Easing.easeInOutCubic)(t)
    val appear = reveal(t, 0.7f, dur = 0.9f).opacity

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        // constellation lines trace in, one segment after another
        CYG_EDGES.forEachIndexed { idx, edge ->
            val seg = (draw * CYG_EDGES.size - idx).coerceIn(0f, 1f)
            if (seg <= 0f) return@forEachIndexed
            val pa = CYGNUS[edge.first]; val pb = CYGNUS[edge.second]
            val ex = pa.x + (pb.x - pa.x) * seg; val ey = pa.y + (pb.y - pa.y) * seg
            drawLine(c(0x8fb0ff).copy(alpha = 0.5f), Offset(pa.x * k, pa.y * k), Offset(ex * k, ey * k), strokeWidth = 2f * k)
        }
        // the constellation's stars
        CYGNUS.forEach { s ->
            val tw = 0.7f + 0.3f * sin(t * 1.5f + s.x)
            drawCircle(Color.White.copy(alpha = 0.3f), radius = s.r * 1.9f * k, center = Offset(s.x * k, s.y * k))
            drawCircle(Color.White, radius = s.r * tw * k, center = Offset(s.x * k, s.y * k))
        }
        // Kepler-186 — a faint pulsing point tucked inside the Swan
        val mp = 0.6f + 0.4f * sin(t * 2.4f)
        val mx = 690f * k; val my = 1085f * k
        drawCircle(c(0xff9e6a).copy(alpha = 0.7f * mp), radius = 26f * k, center = Offset(mx, my), style = Stroke(width = 3f * k))
        drawCircle(c(0xffcaa0), radius = 6f * k, center = Offset(mx, my))
    }

    val la = reveal(t, 3.0f).opacity
    CYGNUS.filter { it.name.isNotEmpty() }.forEach { s ->
        At(s.x + 18f, s.y - 12f, la) { Text(s.name.uppercase(), style = kpLabel(13f, c(0x9fc2ff))) }
    }
    CenterLabel(690f, 1108f, reveal(t, 3.4f).opacity) { Text("KEPLER-186", style = kpLabel(15f, c(0xffcaa0))) }

    Eyebrow("580 light-years away", accent, 200f, e1)
    Head(headline("Hidden in the\n", "Swan", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Kepler-186 lies in the constellation ", "Cygnus", " — its light set out before the Middle Ages."))
}
