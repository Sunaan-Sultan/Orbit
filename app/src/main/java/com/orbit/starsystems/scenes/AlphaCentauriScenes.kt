package com.orbit.starsystems.scenes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
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
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private fun marker(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = size.sp, letterSpacing = 0.1.em, color = color,
)

// ───────────────────── Three suns (pull OUT) ─────────────────────

@Composable
fun BoxScope.SceneAcTriple(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val zoom = animate(1.9f, 0.82f, 0.5f, 5f, Easing.easeOutCubic)(t)
    val cx = 540f; val cy = 1140f
    val ang = t * 0.5f
    val ax = cx + cos(ang) * 80f; val ay = cy + sin(ang) * 80f * 0.5f
    val bx = cx - cos(ang) * 108f; val by = cy - sin(ang) * 108f * 0.5f
    val rx = 470f; val ry = 300f; val pa = 0.62f
    val pxx = cx + cos(pa) * rx; val pyy = cy + sin(pa) * ry
    val proxIn = reveal(t, 2.6f, 1.2f); val lab = reveal(t, 1.6f)
    Camera(zoom, cx, cy) {
        RingArc(cx, cy, rx * 2f, ry * 2f, -10f, Color(0x66e0744a), 1f, dash = true, alpha = proxIn.opacity)
        Sphere(150f, listOf(c(0xfff3cf), c(0xffd34a), c(0xe8951f)), glow = Color(0x66ffc850), modifier = Modifier.offset((ax - 75f).dp, (ay - 75f).dp))
        Sphere(108f, listOf(c(0xffe2b0), c(0xff9e4a), c(0xb5531a)), glow = Color(0x59ff9646), modifier = Modifier.offset((bx - 54f).dp, (by - 54f).dp))
        At(pxx - 17f, pyy - 17f, proxIn.opacity) { Sphere(34f, listOf(c(0xffc9a0), c(0xe0744a), c(0x7a2c14)), glow = Color(0x8Ce0744a)) }
        At(ax + 60f, ay - 20f, lab.opacity) { Text("A", style = marker(22f, c(0xffd34a))) }
        At(bx - 86f, by - 20f, lab.opacity) { Text("B", style = marker(22f, c(0xff9e4a))) }
        CenterLabel(pxx, pyy + 26f, proxIn.opacity) { Text("PROXIMA", style = marker(16f, c(0xe0744a))) }
    }
    Eyebrow("Alpha Centauri", c(0xffcf8a), 200f, e1)
    Head(headline("Not one sun,\n", "but three."), 92f, 246f, e2)
    At(90f, 480f, reveal(t, 2.2f).opacity) {
        StatCol("3", null, "stars, gravitationally bound", 86f, Color.White, c(0xc9a982))
    }
    BottomLine(1640f, e3, body("A Sun-like pair — ", "A and B", " — orbit close together, while faint red Proxima drifts thousands of AU farther out."))
}

// ───────────────────── The 80-year waltz ─────────────────────

@Composable
fun BoxScope.SceneAcWaltz(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val zoom = animate(1.45f, 0.95f, 0.5f, 5f, Easing.easeOutCubic)(t)
    val cx = 540f; val cy = 1150f; val aR = 150f; val bR = 250f; val sq = 0.6f; val tilt = -14f
    val ang = t * 0.7f
    val rad = tilt * 0.017453292f
    fun rot(x: Float, y: Float) = Offset(cx + x * cos(rad) - y * sin(rad), cy + x * sin(rad) + y * cos(rad))
    val a = rot(cos(ang) * aR, sin(ang) * aR * sq)
    val b = rot(-cos(ang) * bR, -sin(ang) * bR * sq)
    Camera(zoom, cx, cy) {
        RingArc(cx, cy, aR * 2f, aR * 2f * sq, tilt, Color(0x4Dffd34a), 1f)
        RingArc(cx, cy, bR * 2f, bR * 2f * sq, tilt, Color(0x4Dff9e4a), 1f)
        At(cx - 3f, cy - 3f) { Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0x66FFFFFF))) }
        Sphere(118f, listOf(c(0xfff3cf), c(0xffd34a), c(0xe8951f)), glow = Color(0x66ffc850), modifier = Modifier.offset((a.x - 59f).dp, (a.y - 59f).dp))
        Sphere(88f, listOf(c(0xffe2b0), c(0xff9e4a), c(0xb5531a)), glow = Color(0x59ff9646), modifier = Modifier.offset((b.x - 44f).dp, (b.y - 44f).dp))
    }
    Eyebrow("The inner pair", c(0xffcf8a), 200f, e1)
    Head(headline("An ", "80-year", "\nwaltz."), 88f, 246f, e2)
    val sa = reveal(t, 2.2f)
    At(90f, 470f, sa.opacity) {
        Row {
            StatCol("79", " yrs", "per orbit", 86f, Color.White, c(0xc9a982))
            Spacer(Modifier.width(54.dp))
            StatCol("11–36", " AU", "apart, and back", 86f, Color.White, c(0xc9a982))
        }
    }
    BottomLine(1640f, e3, body("A and B swing from ", "Saturn-close to beyond-Neptune", " apart over one slow, 79-year orbit around their shared centre."))
}

// ───────────────────── A solar twin (push IN) ─────────────────────

@Composable
fun BoxScope.SceneAcTwin(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val zoom = animate(0.82f, 1.3f, 0.5f, 8f, Easing.easeInOutSine)(t)
    val cy = 1180f
    val sunIn = reveal(t, 1.2f); val aIn = reveal(t, 1.8f); val bIn = reveal(t, 3.2f, 1.0f)
    Camera(zoom, 540f, cy) {
        Sphere(300f, listOf(c(0xfff7d6), c(0xffc23a), c(0xff8a26)), glow = Color(0x66ffa03c), modifier = Modifier.offset((250f - 150f).dp, (cy - 150f).dp))
        Sphere(322f, listOf(c(0xfff3cf), c(0xffd34a), c(0xe8951f)), glow = Color(0x6Bffc850), modifier = Modifier.offset((770f - 161f).dp, (cy - 161f).dp))
        At(800f - 75f, cy + 250f - 75f, bIn.opacity) { Sphere(150f, listOf(c(0xffe2b0), c(0xff9e4a), c(0xb5531a)), glow = Color(0x52ff9646)) }
        CenterLabel(250f, cy + 188f, sunIn.opacity) { Text("OUR SUN", style = marker(22f, c(0xffc23a))) }
        CenterLabel(770f, cy + 200f, aIn.opacity) { Text("ALPHA CEN A", style = marker(22f, c(0xffd34a))) }
        CenterLabel(800f, cy + 250f + 92f, bIn.opacity) { Text("CEN B", style = marker(16f, c(0xff9e4a))) }
    }
    Eyebrow("Alpha Centauri A", c(0xffcf8a), 200f, e1)
    Head(headline("A near-twin\nof our ", "Sun."), 88f, 246f, e2)
    BottomLine(1640f, e3, body("Alpha Cen A is a yellow star almost identical to the Sun in size and colour — its partner ", "B is a smaller, cooler orange star."))
}

// ───────────────────── Proxima, a flaring red dwarf (push IN) ─────────────────────

@Composable
fun BoxScope.SceneAcProxima(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val zoom = animate(0.78f, 1.4f, 0.5f, 8f, Easing.easeInOutSine)(t)
    val cx = 540f; val cy = 1180f; val r = 300f
    val flare = max(0f, sin(t * 1.1f)); val flare2 = max(0f, sin(t * 0.8f + 2f))
    Camera(zoom, cx, cy) {
        Canvas(Modifier.fillMaxSize().alpha((0.5f + 0.5f * flare).coerceIn(0f, 1f))) {
            val k = size.width / 1080f
            drawCircle(
                Brush.radialGradient(listOf(Color(0x59e0744a), Color(0x0De0744a), Color(0x00e0744a)), center = Offset(cx * k, cy * k), radius = (r + 120f) * k),
                radius = (r + 120f) * k, center = Offset(cx * k, cy * k),
            )
        }
        Sphere(r * 2, listOf(c(0xffc9a0), c(0xe0744a), c(0x7a2c14)), glow = Color(0x66e0744a), modifier = Modifier.offset((cx - r).dp, (cy - r).dp))
        RingArc(cx + r * 0.5f + 60f, cy - r * 0.78f + 35f, 120f, 70f, 20f, Color(0xD9ffb478), 5f, alpha = flare.coerceIn(0f, 1f))
        RingArc(cx - r * 0.9f + 45f, cy + r * 0.42f + 27f, 90f, 54f, -25f, Color(0xBFffa064), 4f, alpha = flare2.coerceIn(0f, 1f))
    }
    Eyebrow("Proxima Centauri", c(0xe0744a), 200f, e1)
    Head(headline("Small, dim,\n", "and stormy."), 88f, 246f, e2)
    val sa = reveal(t, 2.2f)
    At(90f, 470f, sa.opacity) {
        Row {
            StatCol("1/7", null, "the Sun's width", 86f, Color.White, c(0xcf9b7a))
            Spacer(Modifier.width(54.dp))
            StatCol("3,000", "°C", "surface, half the Sun's", 86f, Color.White, c(0xcf9b7a))
        }
    }
    BottomLine(1640f, e3, body("This faint red dwarf is the closest star to the Sun — and it ", "flares violently", ", sometimes brightening many times over in minutes."))
}

// ───────────────────── Proxima b (push IN) ─────────────────────

@Composable
fun BoxScope.SceneAcProximaB(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val zoom = animate(0.82f, 1.45f, 0.5f, 8f, Easing.easeInOutSine)(t)
    val cx = 540f; val cy = 1180f; val orbitR = 250f; val sq = 0.42f; val tilt = -16f
    val ang = t * 1.4f
    val rad = tilt * 0.017453292f
    fun rot(x: Float, y: Float) = Offset(cx + x * cos(rad) - y * sin(rad), cy + x * sin(rad) + y * cos(rad))
    val p = rot(cos(ang) * orbitR, sin(ang) * orbitR * sq)
    val hzIn = reveal(t, 2.0f, 1.0f)
    Camera(zoom, cx, cy) {
        RingArc(cx, cy, orbitR * 2f + 120f, (orbitR * 2f + 120f) * sq, tilt, Color(0x1A5ab478), 58f, alpha = hzIn.opacity)
        RingArc(cx, cy, orbitR * 2f, orbitR * 2f * sq, tilt, Color(0x40FFFFFF), 1f, dash = true)
        Sphere(150f, listOf(c(0xffc9a0), c(0xe0744a), c(0x7a2c14)), glow = Color(0x80e0744a), modifier = Modifier.offset((cx - 75f).dp, (cy - 75f).dp))
        Sphere(40f, listOf(c(0x9cc4ec), c(0x3d72b8), c(0x1a3360)), glow = Color(0x4D508cd2), modifier = Modifier.offset((p.x - 20f).dp, (p.y - 20f).dp))
    }
    Eyebrow("Proxima b", c(0x7fb0e6), 200f, e1)
    Head(headline("A planet\n", "next door."), 88f, 246f, e2)
    val sa = reveal(t, 2.2f)
    At(90f, 470f, sa.opacity) {
        Row {
            StatCol("11", " days", "one full year", 86f, Color.White, c(0x9ab0c8))
            Spacer(Modifier.width(54.dp))
            StatCol("1.1", "×", "Earth's mass", 86f, Color.White, c(0x9ab0c8))
        }
    }
    BottomLine(1640f, e3, body("Proxima b laps its star every 11 days — so close, yet still inside the ", "habitable zone", " where liquid water could exist."))
}

// ───────────────────── So near, so far (static track) ─────────────────────

@Composable
fun BoxScope.SceneAcTravel(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val trackL = 150f; val trackR = 930f; val midY = 720f
    val px = interpolate(listOf(2.6f, 7.5f), listOf(trackL, trackR), Easing.easeInOutSine)(t)
    val moving = t >= 2.6f

    Eyebrow("Getting there", c(0xcdd6e0), 230f, e1)
    Head(headline("So near —\nand ", "so far."), 84f, 276f, e2)
    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        drawLine(
            Color(0x4DFFFFFF), Offset(trackL * k, midY * k), Offset(trackR * k, midY * k),
            strokeWidth = 2f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * k, 8f * k)),
        )
    }
    Sphere(40f, listOf(c(0xfff7d6), c(0xffc23a), c(0xff8a26)), glow = Color(0x80ffa03c), modifier = Modifier.offset((trackL - 20f).dp, (midY - 20f).dp))
    Sphere(30f, listOf(c(0xfff3cf), c(0xffd34a), c(0xe8951f)), glow = Color(0x80ffc850), modifier = Modifier.offset((trackR - 15f).dp, (midY - 15f).dp))
    At(trackL - 6f, midY + 26f) { Text("SOL", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 0.14.em, color = c(0xffc23a))) }
    At(trackR - 34f, midY + 26f) { Text("α CEN", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 0.14.em, color = c(0xffd34a))) }
    if (moving) {
        At(px - 6f, midY - 6f) { Box(Modifier.size(12.dp).clip(CircleShape).background(Color.White)) }
    }
    At(90f, midY + 120f, reveal(t, 2.0f).opacity) {
        Column {
            LabeledStat("OUR FASTEST PROBE", c(0x8a93a4), "73,000", " yrs", null, 92f)
            Spacer(Modifier.height(26.dp))
            LabeledStat("A LASER-SAIL CRAFT", c(0x7fb0e6), "~20", " yrs", null, 92f)
        }
    }
    BottomLine(1640f, e3, body("At 4.24 light-years, even our nearest neighbour is out of practical reach — unless we learn to ride ", "beams of light."))
}
