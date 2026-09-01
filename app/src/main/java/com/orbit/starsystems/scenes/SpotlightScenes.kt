package com.orbit.starsystems.scenes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
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
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val SP_TAU = 6.2831855f

/** A background star the travelling black hole lenses: position (scene units), radius, base brightness. */
private class BHStar(val x: Float, val y: Float, val r: Float, val a: Float)

/** A fixed, deterministic starfield scattered across the open band the black hole crosses. */
private val BH_STARS: List<BHStar> = run {
    var s = 0x9E3779B9.toInt()
    fun rnd(): Float { s = s * 1664525 + 1013904223; return ((s ushr 8) and 0xFFFF) / 65535f }
    List(120) {
        BHStar(rnd() * 1080f, 380f + rnd() * 1320f, 1.4f + rnd() * 3.4f, 0.4f + rnd() * 0.55f)
    }
}

// The galaxy is a four-arm barred spiral; these constants are shared by the starfield and the dust lanes.
private const val MW_ARMS = 4
private const val MW_WIND = 0.55f          // how far each arm wraps (in turns)

/** One drawn dot of the galaxy: radius fraction, base angle, size, brightness, colour, soft-glow flag. */
private class GalDot(val rFrac: Float, val ang: Float, val sz: Float, val a: Float, val col: Color, val soft: Boolean)

/** A deterministic, multi-population starfield: spiral arms, HII regions, blue clusters, disc, halo. */
private val MW_DOTS: List<GalDot> = run {
    var s = 0x51ED2719
    fun rnd(): Float { s = s * 1664525 + 1013904223; return ((s ushr 8) and 0xFFFF) / 65535f }
    fun gauss(): Float = (rnd() + rnd() + rnd() - 1.5f) / 1.5f   // ~N(0,1)-ish in [-1,1]
    fun armAngle(arm: Int, along: Float) = arm * (SP_TAU / MW_ARMS) + along * MW_WIND * SP_TAU
    val out = ArrayList<GalDot>()

    // 1 — arm stars, tightly concentrated on each arm's ridgeline (warm inner → blue outer)
    for (i in 0 until 560) {
        val arm = i % MW_ARMS
        val along = rnd()
        val rFrac = 0.15f + 0.85f * sqrt(along) + gauss() * 0.018f
        val ang = armAngle(arm, along) + gauss() * (0.09f + 0.16f * (1f - along))
        val col = lerp(c(0x9fb8ff), c(0xffe6b6), (1f - along).coerceIn(0f, 1f))
        out.add(GalDot(rFrac, ang, 1.0f + rnd() * 2.6f, 0.4f + rnd() * 0.55f, col, false))
    }
    // 2 — pink star-forming (HII) regions, glowing knots strung along the arms
    for (i in 0 until 30) {
        val along = 0.25f + rnd() * 0.7f
        val ang = armAngle(i % MW_ARMS, along) + gauss() * 0.05f
        out.add(GalDot(0.2f + 0.8f * sqrt(along) + gauss() * 0.012f, ang, 5f + rnd() * 5f, 0.55f, c(0xff86b8), true))
    }
    // 3 — hot blue O/B star clusters lighting the arms
    for (i in 0 until 54) {
        val along = 0.3f + rnd() * 0.68f
        val ang = armAngle((i + 1) % MW_ARMS, along) + gauss() * 0.045f
        out.add(GalDot(0.22f + 0.78f * sqrt(along) + gauss() * 0.01f, ang, 2.6f + rnd() * 3.0f, 0.9f, c(0xc6e6ff), true))
    }
    // 4 — faint inter-arm disc field stars (uniform over the disc area)
    for (i in 0 until 170) {
        out.add(GalDot(sqrt(rnd()) * 0.98f, rnd() * SP_TAU, 0.9f + rnd() * 1.7f, 0.22f + rnd() * 0.4f, lerp(c(0xcfe0ff), c(0xfff0d8), rnd()), false))
    }
    // 5 — sparse halo stars drifting beyond the disc
    for (i in 0 until 70) {
        out.add(GalDot(0.6f + rnd() * 0.72f, rnd() * SP_TAU, 0.8f + rnd() * 1.3f, 0.16f + rnd() * 0.3f, c(0xdfe6ff), false))
    }
    // 6 — a handful of globular clusters as fuzzy points in the halo
    for (i in 0 until 8) {
        out.add(GalDot(0.7f + rnd() * 0.6f, rnd() * SP_TAU, 7f + rnd() * 4f, 0.42f, c(0xffe9c8), true))
    }
    out
}

private fun spLabel(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = size.sp, letterSpacing = 0.12.em, color = color,
)

/** A small sleek ship drawn at (cx,cy) px, scaled by [s], heading [angleDeg]. */
private fun DrawScope.drawShip(cx: Float, cy: Float, s: Float, angleDeg: Float, alpha: Float, k: Float) {
    if (alpha <= 0.01f || s <= 0.01f) return
    val sk = s * k
    rotate(angleDeg, pivot = Offset(cx, cy)) {
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xCC8ff0d8), Color(0x008ff0d8)), center = Offset(cx - 64f * sk, cy), radius = 58f * sk),
            radius = 58f * sk, center = Offset(cx - 64f * sk, cy), alpha = alpha,
        )
        val hull = Path().apply {
            moveTo(cx + 88f * sk, cy)
            lineTo(cx - 54f * sk, cy - 26f * sk)
            lineTo(cx - 68f * sk, cy)
            lineTo(cx - 54f * sk, cy + 26f * sk)
            close()
        }
        drawPath(hull, c(0xe7edf2), alpha = alpha)
        drawPath(hull, c(0x8fd6ff), alpha = alpha, style = Stroke(width = 2.5f * k))
        drawCircle(c(0x6fd6ff), radius = 8f * sk, center = Offset(cx + 34f * sk, cy), alpha = alpha)
    }
}

// ───────────────────── Voyager — the farthest we've reached ─────────────────────

@Composable
fun BoxScope.SpVoyager(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xe8d6a6)
    val sunX = 70f; val sunY = 1180f
    // The probe drifts out and away from the Sun.
    val px = interpolate(listOf(0.6f, 8f), listOf(330f, 880f), Easing.easeInOutSine)(t)
    val py = interpolate(listOf(0.6f, 8f), listOf(1080f, 940f), Easing.easeInOutSine)(t)
    val km = interpolate(listOf(1.2f, 6f), listOf(0f, 24f), Easing.easeOutCubic)(t)
    val helio = reveal(t, 1.4f, 1.4f)

    Eyebrow("Voyager · 1977 → forever", accent, 200f, e1)
    Head(headline("The farthest\nwe've ever ", "reached", "."), 84f, 246f, e2)
    BottomLine(1640f, reveal(t, 5.5f), body("Adrift in interstellar space, still calling home across ", "billions of kilometres", "."))

    // Heliosphere — the Sun's bubble the probe is leaving.
    RingArc(sunX, sunY, 1560f, 1560f, 0f, Color(0x33ffd9a0), 1.5f, dash = true, alpha = helio.opacity)
    Sphere(70f, listOf(c(0xfff7d6), c(0xffc23a), c(0xff8a26)), glow = Color(0x80ffa03c), modifier = Modifier.offset((sunX - 35f).dp, (sunY - 35f).dp))

    // Dotted trail from the Sun out to the craft.
    val trail = reveal(t, 1f)
    Canvas(Modifier.fillMaxSize().alpha(trail.opacity)) {
        val k = size.width / 1080f
        drawLine(
            Color(0x55ffe6bd), Offset(sunX * k, sunY * k), Offset(px * k, py * k),
            strokeWidth = 2f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f * k, 14f * k)),
        )
    }

    // The probe: high-gain dish + bus + booms.
    val appear = reveal(t, 0.9f)
    Canvas(Modifier.fillMaxSize().alpha(appear.opacity)) {
        val k = size.width / 1080f
        val cx = px * k; val cy = py * k
        // booms (magnetometer + RTG)
        drawLine(c(0x8a8f99), Offset(cx, cy), Offset(cx + 150f * k, cy + 70f * k), strokeWidth = 3f * k)
        drawLine(c(0x8a8f99), Offset(cx, cy), Offset(cx - 40f * k, cy + 120f * k), strokeWidth = 3f * k)
        // bus (body)
        rotate(-18f, pivot = Offset(cx, cy)) {
            drawRect(c(0xc9cdd4), topLeft = Offset(cx - 26f * k, cy - 24f * k), size = Size(52f * k, 48f * k))
        }
        // RTG tip
        drawCircle(c(0xffb169), radius = 8f * k, center = Offset(cx - 40f * k, cy + 120f * k))
        // high-gain dish, facing back toward the Sun (left)
        drawCircle(Color(0x1affffff), radius = 60f * k, center = Offset(cx, cy - 6f * k))
        drawCircle(Color.White, radius = 60f * k, center = Offset(cx, cy - 6f * k), style = Stroke(width = 3.5f * k))
        drawLine(Color.White, Offset(cx, cy - 6f * k), Offset(cx - 64f * k, cy - 6f * k), strokeWidth = 2.5f * k)
        drawCircle(Color.White, radius = 5f * k, center = Offset(cx - 64f * k, cy - 6f * k))
    }
    CenterLabel(px, py - 120f, reveal(t, 2.4f).opacity) { Text("VOYAGER 1", style = spLabel(18f, accent)) }

    // Distance read-out.
    At(90f, 1380f, reveal(t, 2.2f).opacity) {
        Text(
            String.format(Locale.US, "%.0f", km),
            style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 150.sp, color = Color.White, letterSpacing = (-0.04).em, lineHeight = 150.sp),
        )
    }
    At(360f, 1452f, reveal(t, 2.6f).opacity) {
        Text("billion km from Earth", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 40.sp, color = accent))
    }
}

// ───────────────────── The Space Station — a city in orbit ─────────────────────

@Composable
fun BoxScope.SpIss(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x9cc4ec)
    // Curved Earth limb across the lower third.
    RadialDisc(540f, 2520f, 1240f, arrayOf(0f to c(0xbfe0ff), 0.5f to c(0x3d72b8), 1f to c(0x101f3a)), 0.42f, 0.28f, 0.7f)

    Eyebrow("The International Space Station", accent, 200f, e1)
    Head(headline("A city in ", "orbit", "."), 92f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Circling Earth every 90 minutes, the crew sees ", "16 sunrises a day", "."))

    // The station tracks left → right above the limb.
    val ix = interpolate(listOf(0.6f, 9f), listOf(230f, 880f), Easing.easeInOutSine)(t)
    val iy = 980f + sin((ix - 230f) / 650f * 3.1416f) * -40f
    val appear = reveal(t, 1f)
    // faint ground track
    RingArc(540f, 1640f, 1500f, 1180f, 0f, Color(0x2299c4ff), 1.5f, dash = true, clipTop = 760f, alpha = appear.opacity)

    Canvas(Modifier.fillMaxSize().alpha(appear.opacity)) {
        val k = size.width / 1080f
        val cx = ix * k; val cy = iy * k
        rotate(-16f, pivot = Offset(cx, cy)) {
            // central truss
            drawLine(c(0xb9bec8), Offset(cx - 150f * k, cy), Offset(cx + 150f * k, cy), strokeWidth = 7f * k)
            // pressurised modules at the hub
            drawRect(c(0xe6e9ef), topLeft = Offset(cx - 34f * k, cy - 15f * k), size = Size(68f * k, 30f * k))
            drawRect(c(0xc4c9d2), topLeft = Offset(cx - 10f * k, cy - 40f * k), size = Size(20f * k, 80f * k))
            // four solar array wings
            val panel = c(0x2f4a7a); val edge = c(0x6f9fd6)
            listOf(-150f, -86f, 86f, 150f).forEach { ox ->
                val left = cx + (ox - 28f) * k
                val top = cy - 56f * k
                drawRect(panel, topLeft = Offset(left, top), size = Size(56f * k, 112f * k))
                drawRect(edge, topLeft = Offset(left, top), size = Size(56f * k, 112f * k), style = Stroke(width = 1.5f * k))
            }
        }
    }
    CenterLabel(ix, iy - 150f, reveal(t, 2.4f).opacity) { Text("ISS · 408 km up", style = spLabel(18f, accent)) }

    // Speed read-out.
    val sp = reveal(t, 2.8f)
    At(90f, 1180f, sp.opacity) {
        Text("27,600", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 120.sp, color = Color.White, letterSpacing = (-0.03).em, lineHeight = 120.sp))
    }
    At(96f, 1320f, reveal(t, 3.1f).opacity) {
        Text("km/h — eight times faster than a rifle bullet", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 30.sp, color = accent))
    }
}

// ───────────────────── Starships to come — interstellar concept ─────────────────────

@Composable
fun BoxScope.SpStarship(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x7fd6c0)
    val cx = 540f; val cy = 1120f
    val launch = animate(0f, 1f, 0.8f, 4f, Easing.easeInCubic)(t)
    val thrust = 0.6f + 0.4f * sin(t * 9f)

    // Streaks rushing past to imply speed.
    val streaks = reveal(t, 0.8f)
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.4f to Color.Transparent, 1f to Color(0x14123a52))))
    Canvas(Modifier.fillMaxSize().alpha(streaks.opacity)) {
        val k = size.width / 1080f
        var s = 12345L
        val rnd = { s = (s * 9301 + 49297) % 233280; s / 233280f }
        repeat(28) {
            val y = rnd() * 1920f
            val len = (60f + rnd() * 220f) * (0.4f + launch)
            val speed = 300f + rnd() * 700f
            val x = (1080f - ((t * speed + rnd() * 1080f) % 1400f))
            val a = (0.10f + rnd() * 0.22f) * (0.5f + 0.5f * launch)
            drawLine(
                Color(0x80a9f0e0).copy(alpha = a),
                Offset((x + len) * k, y * k), Offset(x * k, y * k), strokeWidth = 2f * k,
            )
        }
    }

    Eyebrow("Concept · interstellar flight", accent, 200f, e1)
    Head(headline("Built for the\n", "stars", "."), 88f, 246f, e2)
    BottomLine(1640f, reveal(t, 5.5f), body("Fusion, ion drives and laser-pushed sails — engines that might one day carry us ", "to another star", "."))

    // Destination star ahead.
    val starA = reveal(t, 2.4f)
    Sphere(40f, listOf(c(0xffffff), c(0xcfe0ff), c(0x9fb9dd)), glow = Color(0x99cfe0ff), modifier = Modifier.offset((900f - 20f).dp, (760f - 20f).dp))
    CenterLabel(900f, 800f, starA.opacity) { Text("A NEW SUN", style = spLabel(15f, c(0xcfe0ff))) }

    // The ship: a sleek hull with a pulsing engine, climbing toward the star.
    val appear = reveal(t, 0.9f)
    Canvas(Modifier.fillMaxSize().alpha(appear.opacity)) {
        val k = size.width / 1080f
        rotate(-30f, pivot = Offset(cx * k, cy * k)) {
            // engine plume behind
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xCC8ff0d8), Color(0x66ff9a5a), Color(0x00000000)),
                    center = Offset((cx - 150f) * k, cy * k), radius = 130f * k * thrust,
                ),
                radius = 130f * k * thrust, center = Offset((cx - 150f) * k, cy * k),
            )
            // hull
            val hull = Path().apply {
                moveTo((cx + 170f) * k, cy * k)          // nose
                lineTo((cx - 110f) * k, (cy - 42f) * k)
                lineTo((cx - 130f) * k, cy * k)
                lineTo((cx - 110f) * k, (cy + 42f) * k)
                close()
            }
            drawPath(hull, c(0xe7edf2))
            drawPath(hull, c(0x7fd6c0), style = Stroke(width = 3f * k))
            // fin
            val fin = Path().apply {
                moveTo((cx - 10f) * k, (cy - 30f) * k)
                lineTo((cx - 70f) * k, (cy - 96f) * k)
                lineTo((cx - 70f) * k, (cy - 26f) * k)
                close()
            }
            drawPath(fin, c(0x9aa6b2))
            // cockpit
            drawCircle(c(0x6fd6ff), radius = 13f * k, center = Offset((cx + 70f) * k, cy * k))
        }
    }

    // Speed read-out.
    val pct = interpolate(listOf(2f, 5f), listOf(0f, 15f), Easing.easeOutCubic)(t).roundToInt()
    At(90f, 1320f, reveal(t, 2.2f).opacity) {
        Text("$pct%", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 130.sp, color = Color.White, letterSpacing = (-0.03).em, lineHeight = 130.sp))
    }
    At(96f, 1470f, reveal(t, 2.6f).opacity) {
        Text("of the speed of light — in theory", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 30.sp, color = accent))
    }
}

// ───────────────────── The Sun — our star up close ─────────────────────

@Composable
fun BoxScope.SpSun(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xff9e34)
    val cx = 540f; val cy = 1100f
    val r = animate(40f, 300f, 0.4f, 2.2f, Easing.easeOutCubic)(t)
    val pulse = 1f + 0.05f * sin(t * 1.8f)

    // Corona halo (breathing) and the glowing body — an evenly-lit star, not a planet sphere.
    RadialDisc(
        cx, cy, (r + 230f) * pulse,
        arrayOf(0f to Color(0x59ffb240), 0.5f to Color(0x1Aff8c28), 0.75f to Color(0x00ff8c28), 1f to Color(0x00ff8c28)),
        0.5f, 0.5f, 0.5f,
    )
    RadialDisc(
        cx, cy, r,
        arrayOf(0f to c(0xfff7d6), 0.4f to c(0xffd451), 0.74f to c(0xff9e34), 1f to c(0xff7a1f)),
        0.5f, 0.5f, 0.5f,
    )

    // Looping prominences off the limb + drifting sunspots (the rotating surface).
    val pa = reveal(t, 1.2f)
    Canvas(Modifier.fillMaxSize().alpha(pa.opacity)) {
        val k = size.width / 1080f
        val rr = r * k
        val ctr = Offset(cx * k, cy * k)
        fun limb(ang: Float, rad: Float) = Offset(ctr.x + cos(ang) * rad, ctr.y + sin(ang) * rad)
        val proms = listOf(-2.4f, -1.2f, 0.2f, 1.5f, 2.8f)
        proms.forEachIndexed { i, base ->
            val ang = base + t * 0.22f
            val flick = 0.55f + 0.45f * sin(t * (2.4f + i) + i)
            val h = (55f + 45f * flick) * k
            val dθ = 0.17f
            val p1 = limb(ang - dθ, rr); val p2 = limb(ang + dθ, rr)
            val apex = limb(ang, r * k + h)
            val path = Path().apply { moveTo(p1.x, p1.y); quadraticBezierTo(apex.x, apex.y, p2.x, p2.y) }
            drawPath(path, color = c(0xffa83a).copy(alpha = 0.85f * flick), style = Stroke(width = 5f * k))
        }
        // sunspots drift across, wrapping — implies the Sun rotating
        val spots = listOf(Triple(-0.3f, 0.18f, 26f), Triple(0.4f, -0.12f, 18f), Triple(0.05f, 0.46f, 13f))
        spots.forEach { (fx, fy, sz) ->
            val drift = ((fx + t * 0.05f + 1f) % 2f) - 1f
            val sx = ctr.x + drift * rr * 0.72f
            val sy = ctr.y + fy * rr
            val dx = sx - ctr.x; val dy = sy - ctr.y
            if (dx * dx + dy * dy < (rr * 0.8f) * (rr * 0.8f)) {
                drawCircle(c(0x6a2400).copy(alpha = 0.55f), radius = sz * k, center = Offset(sx, sy))
            }
        }
    }

    Eyebrow("Our star, up close", accent, 200f, e1)
    Head(headline("Closer to\nthe ", "fire", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Every second it fuses ", "600 million tonnes", " of hydrogen into light."))

    // Earth, roughly to scale, sitting just off the limb.
    val ea = reveal(t, 2.8f)
    Sphere(16f, listOf(c(0x9cc4ec), c(0x3d72b8), c(0x16223f)), modifier = Modifier.offset((cx + r + 30f).dp, (cy + r - 40f).dp))
    CenterLabel(cx + r + 38f, cy + r + 0f, ea.opacity) { Text("EARTH · 109 fit across", style = spLabel(15f, c(0x9cc4ec))) }
}

// ───────────────────── Olympus Mons — seen from above ─────────────────────

@Composable
fun BoxScope.SpOlympus(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f)
    val accent = c(0xe8915c)
    val cx = 540f; val cy = 1140f; val baseR = 380f
    val grow = animate(0.7f, 1f, 0.4f, 2.4f, Easing.easeOutCubic)(t)

    Eyebrow("Mars · Olympus Mons", accent, 200f, e1)
    Head(headline("A volcano the\nsize of a ", "country", "."), 78f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Its base would blanket Arizona — and it rises ", "2.5× higher than Everest", "."))

    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        val rr = baseR * grow * k
        // shield-volcano slope, shaded from the centre out
        drawCircle(
            brush = Brush.radialGradient(listOf(c(0x66b5462a), c(0x44823018), c(0x10401409)), center = ctr, radius = rr),
            radius = rr, center = ctr,
        )
        // concentric lava-flow ridges, rotating slowly
        rotate(t * 3.2f, pivot = ctr) {
            for (i in 1..7) {
                val rev = reveal(t, 0.6f + i * 0.12f).opacity
                drawCircle(
                    c(0xe8915c).copy(alpha = 0.30f * rev), radius = rr * (0.26f + 0.105f * i), center = ctr,
                    style = Stroke(width = 2.2f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f * k, 10f * k))),
                )
            }
        }
        // the escarpment — the kilometres-high cliff ringing the base
        val edge = reveal(t, 1f).opacity
        drawCircle(c(0xe8915c).copy(alpha = 0.7f * edge), radius = rr, center = ctr, style = Stroke(width = 4f * k))
        // central caldera complex — overlapping collapse pits
        val cal = reveal(t, 2f).opacity
        if (cal > 0.01f) {
            listOf(Triple(0f, 0f, 80f), Triple(-48f, 24f, 52f), Triple(42f, 34f, 44f), Triple(22f, -42f, 36f)).forEach { (ox, oy, sz) ->
                val cc = Offset(ctr.x + ox * grow * k, ctr.y + oy * grow * k)
                drawCircle(c(0x3a160c).copy(alpha = 0.85f * cal), radius = sz * grow * k, center = cc)
                drawCircle(c(0xe8915c).copy(alpha = 0.5f * cal), radius = sz * grow * k, center = cc, style = Stroke(width = 2f * k))
            }
        }
    }

    // Scale tick from the centre to the rim.
    val sa = reveal(t, 2.6f)
    Canvas(Modifier.fillMaxSize().alpha(sa.opacity)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        drawLine(
            Color(0x99FFFFFF), ctr, Offset(ctr.x + baseR * grow * k, ctr.y),
            strokeWidth = 1.5f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * k, 8f * k)),
        )
    }
    CenterLabel(cx + baseR / 2f, cy - 46f, sa.opacity) { Text("≈ 300 km", style = spLabel(16f, c(0xf0b48c))) }
    CenterLabel(cx, cy + baseR + 26f, reveal(t, 3f).opacity) { Text("OLYMPUS MONS · FROM ABOVE", style = spLabel(17f, accent)) }
}

// ───────────────────── Black hole — where light can't escape ─────────────────────

@Composable
fun BoxScope.SpBlackHole(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xffb060)

    // The hole glides left → right across the open mid-band; everything bends around it.
    val cross = animate(-0.24f, 1.24f, 0.5f, 13.6f, Easing.easeInOutSine)(t)
    val cxU = cross * 1080f
    val cyU = 1024f
    val rEU = 200f                                  // Einstein (lensing) radius in scene units
    val rsU = 104f                                  // event-horizon shadow radius
    val shimmer = 0.82f + 0.18f * sin(t * 3f)
    val appear = reveal(t, 0.3f, dur = 0.9f).opacity

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(cxU * k, cyU * k)
        val rE = rEU * k
        val rs = rsU * k

        // Point-lens deflection: a background point at distance d appears pushed outward to
        // r' = (d + √(d² + 4rE²)) / 2 — so light sweeps aside and rings the dark centre.
        fun lens(px: Float, py: Float): Offset {
            val dx = px - ctr.x; val dy = py - ctr.y
            val d = sqrt(dx * dx + dy * dy)
            if (d < 0.5f) return Offset(ctr.x, ctr.y - rE)
            val rp = (d + sqrt(d * d + 4f * rE * rE)) * 0.5f
            val f = rp / d
            return Offset(ctr.x + dx * f, ctr.y + dy * f)
        }

        // 1 — the fabric of space, warped: a faint grid bent by the passing mass.
        val gridCol = c(0x7c93c4)
        val stepU = 124f
        run {
            // verticals
            var gx = -60f
            while (gx <= 1140f) {
                val path = Path()
                var first = true
                var gy = 360f
                while (gy <= 1700f) {
                    val p = lens(gx * k, gy * k)
                    if (first) { path.moveTo(p.x, p.y); first = false } else path.lineTo(p.x, p.y)
                    gy += 38f
                }
                drawPath(path, gridCol.copy(alpha = 0.12f), style = Stroke(width = 1.2f * k))
                gx += stepU
            }
            // horizontals
            var gy = 360f
            while (gy <= 1700f) {
                val path = Path()
                var first = true
                var hx = -60f
                while (hx <= 1140f) {
                    val p = lens(hx * k, gy * k)
                    if (first) { path.moveTo(p.x, p.y); first = false } else path.lineTo(p.x, p.y)
                    hx += 38f
                }
                drawPath(path, gridCol.copy(alpha = 0.12f), style = Stroke(width = 1.2f * k))
                gy += stepU
            }
        }

        // 2 — the starfield, lensed: near the hole stars are flung wide and brighten into a ring.
        BH_STARS.forEach { st ->
            val d0 = sqrt((st.x - cxU) * (st.x - cxU) + (st.y - cyU) * (st.y - cyU)) * k
            val pos = lens(st.x * k, st.y * k)
            val mag = ((sqrt(d0 * d0 + 4f * rE * rE) + d0) / (2f * d0.coerceAtLeast(1f))).coerceIn(1f, 3.2f)
            val twinkle = 0.7f + 0.3f * sin(t * 2.2f + st.x * 0.03f)
            val a = (st.a * twinkle * (0.55f + 0.55f * (mag - 1f))).coerceIn(0f, 1f)
            drawCircle(c(0xdfe9ff), radius = st.r * k * sqrt(mag), center = pos, alpha = a)
        }

        // 3 — accretion glow: a hot warm halo hugging just outside the shadow.
        drawCircle(
            brush = Brush.radialGradient(
                0f to Color(0x00000000), 0.34f to Color(0x00000000),
                0.50f to c(0xffb060).copy(alpha = 0.55f * shimmer),
                0.66f to c(0xff8a30).copy(alpha = 0.30f),
                1f to Color(0x00000000),
                center = ctr, radius = rE * 1.9f,
            ),
            radius = rE * 1.9f, center = ctr,
        )

        // 4 — the event horizon: a pure black disc nothing escapes.
        drawCircle(Color.Black, radius = rs, center = ctr)

        // 5 — photon ring + a brighter leading arc (the bent light grazing the horizon).
        drawCircle(c(0xffd9a0).copy(alpha = 0.45f * shimmer), radius = rs * 1.15f, center = ctr, style = Stroke(width = 16f * k))
        drawCircle(c(0xfff2d6).copy(alpha = shimmer), radius = rs * 1.06f, center = ctr, style = Stroke(width = 4.5f * k))
        drawArc(
            color = c(0xffffff).copy(alpha = 0.9f * shimmer),
            startAngle = 196f, sweepAngle = 148f, useCenter = false,
            topLeft = Offset(ctr.x - rs * 1.06f, ctr.y - rs * 1.06f),
            size = Size(rs * 2.12f, rs * 2.12f), style = Stroke(width = 5f * k),
        )
    }

    Eyebrow("Extreme gravity", accent, 200f, e1)
    Head(headline("It bends\n", "space itself", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Its gravity is so fierce it warps spacetime — bending the very light of the stars ", "passing behind it", "."))
}

// ───────────────────── Wormhole — a tunnel through spacetime ─────────────────────

@Composable
fun BoxScope.SpWormhole(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x8fd6ff)
    val cx = 540f; val cy = 1080f
    val grow = animate(0.4f, 1f, 0.4f, 2.4f, Easing.easeOutCubic)(t)
    val m = ((t - 5.4f) / 2.2f).coerceIn(0f, 1f)            // home galaxy → far galaxy
    val flash = (1f - (abs(t - 6.4f) / 1.3f)).coerceIn(0f, 1f)

    // Background nebulae of the two galaxies, cross-fading as the ship transits.
    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        fun nebula(xf: Float, yf: Float, rf: Float, home: Color, far: Color) {
            val col = lerp(home, far, m)
            drawCircle(
                brush = Brush.radialGradient(listOf(col, Color(0x00000000)), center = Offset(xf * k, yf * k), radius = rf * k),
                radius = rf * k, center = Offset(xf * k, yf * k),
            )
        }
        nebula(240f, 540f, 460f, Color(0x33356fd0), Color(0x33b0408f))
        nebula(860f, 1520f, 500f, Color(0x282aa0c0), Color(0x28d08a3a))
    }

    RadialDisc(
        cx, cy, 380f * grow,
        arrayOf(0f to Color(0x00000000), 0.62f to Color(0x00000000), 0.82f to Color(0x338fd6ff), 1f to Color(0x008fd6ff)),
        0.5f, 0.5f, 0.5f,
    )

    val appear = reveal(t, 0.8f)
    Canvas(Modifier.fillMaxSize().alpha(appear.opacity)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        val maxR = 360f * grow * k
        val rings = 16
        for (i in 0 until rings) {
            val frac = (((i.toFloat() / rings) + t * 0.12f) % 1f)
            val rad = maxR * frac
            if (rad > 3f) {
                // swirl each ring along a tightening spiral toward the throat
                val tw = frac * SP_TAU * 1.5f + t * 0.6f
                val off = (1f - frac) * 18f * k
                val rc = Offset(ctr.x + cos(tw) * off, ctr.y + sin(tw) * off)
                val col = lerp(c(0x6fe6ff), c(0x3a4fd0), frac)
                val a = sin(frac * 3.1416f).coerceIn(0f, 1f)
                drawCircle(col.copy(alpha = 0.55f * a), radius = rad, center = rc, style = Stroke(width = 3f * k))
            }
        }
        // bright throat — the view out the far side, flaring as the ship punches through
        val throatR = maxR * (0.2f + 0.16f * flash)
        drawCircle(
            brush = Brush.radialGradient(listOf(c(0xffffff), c(0x9ff0e0), c(0x2a6fd0)), center = ctr, radius = throatR * 1.1f),
            radius = throatR, center = ctr,
        )
        if (flash > 0.01f) {
            drawCircle(Color.White.copy(alpha = flash * 0.8f), radius = maxR * (0.25f + 0.9f * (1f - flash)), center = ctr, style = Stroke(width = 10f * k * flash))
        }
        listOf(Offset(-0.05f, -0.04f), Offset(0.06f, 0.03f), Offset(0f, 0.07f)).forEach { o ->
            drawCircle(Color.White.copy(alpha = 0.9f), radius = 2.5f * k, center = Offset(ctr.x + o.x * maxR, ctr.y + o.y * maxR))
        }
        // the mouth
        drawCircle(c(0x8fd6ff), radius = maxR, center = ctr, style = Stroke(width = 4f * k))

        // The ship dives in from the home side…
        if (t in 1.6f..5.6f) {
            val p = Easing.easeInCubic(((t - 1.6f) / 4f).coerceIn(0f, 1f))
            val sx = (170f + (cx - 170f) * p) * k
            val sy = (1500f + (cy - 1500f) * p) * k
            val al = 1f - ((p - 0.82f) / 0.18f).coerceIn(0f, 1f)
            drawShip(sx, sy, 1f - 0.94f * p, atan2(cy - 1500f, cx - 170f) * 57.2957795f, al, k)
        }
        // …and pops out the far side into the other galaxy.
        if (t in 7.4f..11.6f) {
            val p = Easing.easeOutCubic(((t - 7.4f) / 4.2f).coerceIn(0f, 1f))
            val ex = (cx + (940f - cx) * p) * k
            val ey = (cy + (640f - cy) * p) * k
            val al = (p / 0.18f).coerceIn(0f, 1f)
            drawShip(ex, ey, 0.06f + 0.94f * p, atan2(640f - cy, 940f - cx) * 57.2957795f, al, k)
        }
    }

    Eyebrow("A shortcut through spacetime", accent, 200f, e1)
    Head(headline("In one side, out\nin ", "another galaxy", "."), 74f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("In theory a wormhole could fling a ship across the universe in a single step — but none has ever ", "been found", "."))
}

// ───────────────────── Pulsar — a spinning neutron star ─────────────────────

@Composable
fun BoxScope.SpPulsar(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x9fe8ff)
    val cx = 540f; val cy = 1090f
    val grow = animate(0.4f, 1f, 0.4f, 2.0f, Easing.easeOutCubic)(t)
    val spin = t * 230f
    val beat = 0.45f + 0.55f * abs(sin(t * 6.0f))

    RadialDisc(
        cx, cy, (150f + 40f * beat) * grow,
        arrayOf(0f to Color(0x99cfeeff), 0.4f to Color(0x33a0dfff), 0.75f to Color(0x00000000), 1f to Color(0x00000000)),
        0.5f, 0.5f, 0.5f,
    )

    val appear = reveal(t, 0.7f)
    Canvas(Modifier.fillMaxSize().alpha(appear.opacity)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        // two opposite lighthouse beams sweeping with the spin
        rotate(spin, pivot = ctr) {
            listOf(0f, 180f).forEach { baseAng ->
                rotate(baseAng, pivot = ctr) {
                    val len = 760f * k; val halfW = 120f * k
                    val path = Path().apply {
                        moveTo(ctr.x, ctr.y)
                        lineTo(ctr.x + len, ctr.y - halfW)
                        lineTo(ctr.x + len, ctr.y + halfW)
                        close()
                    }
                    drawPath(path, brush = Brush.horizontalGradient(listOf(c(0x9fe8ff).copy(alpha = 0.55f), Color(0x009fe8ff)), startX = ctr.x, endX = ctr.x + len), alpha = beat)
                }
            }
        }
        // the neutron star itself: tiny, intensely bright
        val rcore = 42f * grow * k
        drawCircle(brush = Brush.radialGradient(listOf(Color.White, c(0xbfe8ff), c(0x3a7fb0)), center = ctr, radius = rcore * 1.4f), radius = rcore, center = ctr)
        drawCircle(Color.White.copy(alpha = 0.6f * beat), radius = rcore * 1.5f, center = ctr, style = Stroke(width = 4f * k))
    }

    Eyebrow("A lighthouse in space", accent, 200f, e1)
    Head(headline("A dead star\nthat ", "spins", "."), 86f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("A city-sized neutron star, spinning so fast its beam flashes us ", "hundreds of times a second", "."))
}

// ───────────────────── Supernova — a star detonates ─────────────────────

@Composable
fun BoxScope.SpSupernova(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xffd36a)
    val cx = 540f; val cy = 1080f
    val tBlast = 4.0f
    val swell = animate(70f, 150f, 0.5f, tBlast, Easing.easeInCubic)(t)
    val flash = (1f - (abs(t - tBlast) / 0.9f)).coerceIn(0f, 1f)
    val shellP = ((t - tBlast) / 7f).coerceIn(0f, 1f)
    val shellR = 90f + shellP * 560f
    val shellA = 1f - shellP

    if (t < tBlast + 0.5f) {
        RadialDisc(
            cx, cy, swell * (0.92f + 0.08f * sin(t * 9f)),
            arrayOf(0f to c(0xfff3d0), 0.45f to c(0xffd35e), 0.8f to c(0xff8a30), 1f to Color(0x00000000)),
            0.5f, 0.42f, 0.6f,
        )
    }
    if (flash > 0.01f) {
        RadialDisc(
            cx, cy, 720f * flash,
            arrayOf(0f to Color.White.copy(alpha = flash), 0.5f to Color(0xCCfff0d0).copy(alpha = flash), 0.85f to Color(0x00000000), 1f to Color(0x00000000)),
            0.5f, 0.5f, 0.5f,
        )
    }

    val appear = reveal(t, 0.6f)
    Canvas(Modifier.fillMaxSize().alpha(appear.opacity)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        if (shellP > 0f && shellP < 1f) {
            drawCircle(c(0xffb347).copy(alpha = 0.5f * shellA), radius = shellR * k, center = ctr, style = Stroke(width = (38f * (1f - shellP) + 6f) * k))
            drawCircle(Color.White.copy(alpha = 0.6f * shellA), radius = shellR * k, center = ctr, style = Stroke(width = 4f * k))
            val n = 18
            for (i in 0 until n) {
                val ang = i.toFloat() / n * SP_TAU + i * 0.6f
                val rr = shellR * (0.78f + 0.2f * sin(i * 2.3f)) * k
                val col = if (i % 2 == 0) c(0xffe9b0) else c(0xff8a3a)
                drawCircle(col.copy(alpha = 0.7f * shellA), radius = (4f + 3f * (i % 3)) * k, center = Offset(ctr.x + cos(ang) * rr, ctr.y + sin(ang) * rr))
            }
        }
        if (t > tBlast) {
            val ra = (1f - shellP) * 0.7f + 0.3f
            drawCircle(brush = Brush.radialGradient(listOf(Color.White, c(0x9fe8ff), Color(0x00000000)), center = ctr, radius = 60f * k), radius = 52f * k, center = ctr, alpha = ra)
        }
    }

    Eyebrow("The death of a giant star", accent, 200f, e1)
    Head(headline("Brighter than\na ", "galaxy", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("A collapsing giant explodes — forging the ", "gold, iron and oxygen", " that build new worlds."))
}

// ───────────────────── Comet — tails that flee the Sun ─────────────────────

@Composable
fun BoxScope.SpComet(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xbfe8ff)
    val sunX = 940f; val sunY = 560f
    val p = interpolate(listOf(0.6f, 9.5f), listOf(0f, 1f), Easing.easeInOutSine)(t)
    val cmx = 120f + p * 840f
    val cmy = 1320f - sin(p * 3.1416f) * 540f

    RadialDisc(sunX, sunY, 230f, arrayOf(0f to Color(0x40ffd35e), 0.5f to Color(0x14ffa030), 0.8f to Color(0x00000000), 1f to Color(0x00000000)), 0.5f, 0.5f, 0.5f)
    Sphere(96f, listOf(c(0xfff7d6), c(0xffc23a), c(0xff8a26)), glow = Color(0x80ffa03c), modifier = Modifier.offset((sunX - 48f).dp, (sunY - 48f).dp))

    val appear = reveal(t, 0.9f)
    Canvas(Modifier.fillMaxSize().alpha(appear.opacity)) {
        val k = size.width / 1080f
        val nuc = Offset(cmx * k, cmy * k)
        val sun = Offset(sunX * k, sunY * k)
        var dx = nuc.x - sun.x; var dy = nuc.y - sun.y
        val d = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        dx /= d; dy /= d
        val px = -dy; val py = dx
        val near = (1f - d / (900f * k)).coerceIn(0.15f, 1f)
        val tailLen = (220f + 470f * near) * k
        // ion tail — straight, bluish
        val ibx = nuc.x + dx * tailLen; val iby = nuc.y + dy * tailLen
        drawPath(
            Path().apply { moveTo(nuc.x, nuc.y); lineTo(ibx + px * 30f * k, iby + py * 30f * k); lineTo(ibx - px * 30f * k, iby - py * 30f * k); close() },
            brush = Brush.linearGradient(listOf(c(0x99bfe8ff), Color(0x00bfe8ff)), start = nuc, end = Offset(ibx, iby)),
        )
        // dust tail — shorter, warmer, fanned slightly off-axis
        val ddx = dx + px * 0.18f; val ddy = dy + py * 0.18f
        val dbx = nuc.x + ddx * tailLen * 0.8f; val dby = nuc.y + ddy * tailLen * 0.8f
        drawPath(
            Path().apply { moveTo(nuc.x, nuc.y); lineTo(dbx + px * 48f * k, dby + py * 48f * k); lineTo(dbx - px * 48f * k, dby - py * 48f * k); close() },
            brush = Brush.linearGradient(listOf(c(0x66ffe2b0), Color(0x00ffe2b0)), start = nuc, end = Offset(dbx, dby)),
        )
        // nucleus + coma
        drawCircle(brush = Brush.radialGradient(listOf(Color.White, c(0xbfe8ff), Color(0x00bfe8ff)), center = nuc, radius = 34f * k), radius = 30f * k, center = nuc)
    }

    Eyebrow("An icy visitor", accent, 200f, e1)
    Head(headline("Tails that flee\nthe ", "Sun", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Sunlight boils its ice into glowing tails that always stream ", "away from the Sun", "."))
}

// ───────────────────── Total eclipse — the Moon hides the Sun ─────────────────────

@Composable
fun BoxScope.SpEclipse(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xffcaa0)
    val cx = 540f; val cy = 1040f; val R = 240f
    val mp = interpolate(listOf(1f, 11f), listOf(-1.7f, 1.7f), Easing.linear)(t)
    val moonX = cx + mp * R * 1.6f
    val moonY = cy - mp * R * 0.18f
    val coverage = (1f - abs(mp) / 1.0f).coerceIn(0f, 1f)
    val corona = ((coverage - 0.4f) / 0.6f).coerceIn(0f, 1f)

    RadialDisc(cx, cy, R + 120f, arrayOf(0f to Color(0x55ffd58a), 0.5f to Color(0x18ffa030), 0.8f to Color(0x00000000), 1f to Color(0x00000000)), 0.5f, 0.5f, 0.5f)
    RadialDisc(cx, cy, R, arrayOf(0f to c(0xfff7e0), 0.55f to c(0xffd35e), 1f to c(0xff9e34)), 0.5f, 0.42f, 0.62f)

    val appear = reveal(t, 0.7f)
    Canvas(Modifier.fillMaxSize().alpha(appear.opacity)) {
        val k = size.width / 1080f
        val mc = Offset(moonX * k, moonY * k)
        val rk = R * k
        if (corona > 0.02f) {
            val n = 40
            for (i in 0 until n) {
                val ang = i.toFloat() / n * SP_TAU
                val l = (1.1f + 0.5f * abs(sin(i * 1.7f))) * rk * (0.7f + 0.3f * corona)
                val a = 0.5f * corona * (0.5f + 0.5f * abs(sin(i * 2.1f)))
                drawLine(c(0xfff0d8).copy(alpha = a), Offset(mc.x + cos(ang) * rk * 1.02f, mc.y + sin(ang) * rk * 1.02f), Offset(mc.x + cos(ang) * l, mc.y + sin(ang) * l), strokeWidth = 2.5f * k)
            }
            drawCircle(c(0xfff0d8).copy(alpha = 0.35f * corona), radius = rk * 1.06f, center = mc, style = Stroke(width = 10f * k))
        }
        drawCircle(c(0x0b0b10), radius = rk, center = mc)
        if (coverage in 0.82f..0.985f) {
            val edgeAng = if (mp < 0f) 0.6f else 3.74f
            val b = Offset(mc.x + cos(edgeAng) * rk, mc.y + sin(edgeAng) * rk)
            drawCircle(Color.White.copy(alpha = 0.5f), radius = 30f * k, center = b)
            drawCircle(Color.White, radius = 16f * k, center = b)
        }
    }

    Eyebrow("A cosmic coincidence", accent, 200f, e1)
    Head(headline("When the Moon\nhides the ", "Sun", "."), 80f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Moon and Sun look the same size — so now and then one ", "perfectly eclipses", " the other."))
}

// ───────────────────── Neutron star — a Sun crushed into a city ─────────────────────

@Composable
fun BoxScope.SpNeutron(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xb8c4ff)
    val cx = 540f; val cy = 1004f
    val tCrush = 3.0f
    // a giant star collapses: its body slams inward from huge to a pinpoint, warm → icy blue.
    val starR = animate(380f, 30f, 0.7f, tCrush, Easing.easeInCubic)(t)
    val crushed = ((t - 0.7f) / (tCrush - 0.7f)).coerceIn(0f, 1f)
    val flash = (1f - (abs(t - tCrush) / 0.6f)).coerceIn(0f, 1f)
    val pulse = 0.92f + 0.08f * sin(t * 4.5f)

    // the collapsing star body, tinted from warm-giant to blue-white as it shrinks
    if (t < tCrush + 0.3f) {
        val inner = lerp(c(0xfff3e0), Color.White, crushed)
        val mid = lerp(c(0xffcf7a), c(0xbfd0ff), crushed)
        val outer = lerp(c(0xff8a3a), c(0x5a78d8), crushed)
        RadialDisc(
            cx, cy, starR,
            arrayOf(0f to inner, 0.45f to mid, 0.82f to outer.copy(alpha = 0.5f), 1f to Color(0x00000000)),
            0.5f, 0.46f, 0.6f,
        )
    }
    // the collapse flash at the instant of crush
    if (flash > 0.01f) {
        RadialDisc(
            cx, cy, 560f * flash,
            arrayOf(0f to Color.White.copy(alpha = flash), 0.45f to c(0xcdd6ff).copy(alpha = 0.8f * flash), 0.85f to Color(0x00000000), 1f to Color(0x00000000)),
            0.5f, 0.5f, 0.5f,
        )
    }
    // settled gravitational halo around the new neutron star
    if (t > tCrush - 0.4f) {
        RadialDisc(
            cx, cy, 150f * pulse,
            arrayOf(0f to Color(0x99cdd6ff), 0.4f to Color(0x33aebcff), 0.78f to Color(0x00000000), 1f to Color(0x00000000)),
            0.5f, 0.5f, 0.5f,
        )
    }

    val appear = reveal(t, 0.6f).opacity
    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        val settle = ((t - tCrush) / 0.8f).coerceIn(0f, 1f)

        // matter still raining in — concentric shells collapsing toward the core (relentless gravity)
        if (settle > 0f) {
            val n = 5
            for (i in 0 until n) {
                val p = (((t * 0.45f) + i.toFloat() / n) % 1f)   // 1 = far out, 0 = swallowed
                val rr = (36f + p * 300f) * k
                val a = (settle * (1f - p) * p * 2.4f).coerceIn(0f, 0.5f)
                if (a > 0.01f) drawCircle(accent.copy(alpha = a), radius = rr, center = ctr, style = Stroke(width = (2f + 5f * (1f - p)) * k))
            }
        }

        // the neutron star itself: tiny, blinding, blue-white
        if (t > tCrush - 0.5f) {
            val rc = 30f * pulse * k
            drawCircle(brush = Brush.radialGradient(listOf(Color.White, c(0xcdd6ff), c(0x3a4f9a)), center = ctr, radius = rc * 1.5f), radius = rc, center = ctr)
            drawCircle(Color.White.copy(alpha = 0.7f * pulse), radius = rc * 1.4f, center = ctr, style = Stroke(width = 3.5f * k))
        }

        // size callout: a ~20 km scale bar beneath the star
        val ma = reveal(t, 4.4f).opacity
        if (ma > 0.01f) {
            val y = (cy + 150f) * k
            val half = 150f * k
            val col = accent.copy(alpha = 0.8f * ma)
            drawLine(col, Offset(ctr.x - half, y), Offset(ctr.x + half, y), strokeWidth = 2f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * k, 8f * k)))
            drawLine(col, Offset(ctr.x - half, y - 12f * k), Offset(ctr.x - half, y + 12f * k), strokeWidth = 2f * k)
            drawLine(col, Offset(ctr.x + half, y - 12f * k), Offset(ctr.x + half, y + 12f * k), strokeWidth = 2f * k)
        }
    }
    CenterLabel(cx, cy + 168f, reveal(t, 4.6f).opacity) { Text("≈ 20 KM ACROSS", style = spLabel(17f, accent)) }

    Eyebrow("The densest thing in the universe", accent, 200f, e1)
    Head(headline("A whole Sun\ncrushed into a ", "city", "."), 80f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Squeeze a giant star's core into a ball 20 km wide — one teaspoon would weigh about ", "a billion tonnes", "."))
}

// ───────────────────── Milky Way — our home galaxy ─────────────────────

@Composable
fun BoxScope.SpMilkyWay(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x9fc2ff)
    val cx = 540f; val cy = 1010f
    val maxRu = 430f
    val tilt = 0.46f                                    // vertical squash → an inclined disc
    val spin = t * 0.10f                                // a slow, majestic rotation
    val sunAng = 0.7f; val sunRu = 0.66f * maxRu        // our Sun, parked in an outer arm

    // a warm glow standing in for the whole disc's light
    RadialDisc(
        cx, cy, maxRu * 0.95f,
        arrayOf(0f to Color(0x44ffe6b0), 0.4f to Color(0x18b9c6ff), 0.78f to Color(0x00000000), 1f to Color(0x00000000)),
        0.5f, 0.5f, 0.5f,
    )

    val appear = reveal(t, 0.5f, dur = 0.9f).opacity
    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        val maxR = maxRu * k
        val spinDeg = spin * 57.2957795f

        // the whole disc — squashed by [tilt] so we view it on a slant
        scale(1f, tilt, pivot = ctr) {
            // diffuse disc light
            drawCircle(
                brush = Brush.radialGradient(
                    0f to c(0xffe9c0).copy(alpha = 0.30f), 0.45f to c(0x8aa6e0).copy(alpha = 0.14f),
                    0.85f to Color(0x00000000), 1f to Color(0x00000000),
                    center = ctr, radius = maxR,
                ),
                radius = maxR, center = ctr,
            )

            // every population of dots
            MW_DOTS.forEach { d ->
                val ang = d.ang + spin
                val r = d.rFrac * maxR
                val pos = Offset(ctr.x + cos(ang) * r, ctr.y + sin(ang) * r)
                val twinkle = 0.74f + 0.26f * sin(t * 1.8f + d.ang * 5f)
                val a = (d.a * twinkle).coerceIn(0f, 1f)
                if (d.soft) {
                    drawCircle(
                        brush = Brush.radialGradient(listOf(d.col.copy(alpha = a), Color(0x00000000)), center = pos, radius = d.sz * 2.4f * k),
                        radius = d.sz * 2.4f * k, center = pos,
                    )
                    drawCircle(Color.White, radius = d.sz * 0.4f * k, center = pos, alpha = a * 0.8f)
                } else {
                    drawCircle(d.col, radius = d.sz * k, center = pos, alpha = a)
                }
            }

            // dark dust lanes hugging the inner edge of each arm
            val laneCol = Color(0x73140d0a)
            for (arm in 0 until MW_ARMS) {
                val path = Path()
                var first = true
                var along = 0.12f
                while (along <= 1.0f) {
                    val ang = arm * (SP_TAU / MW_ARMS) + along * MW_WIND * SP_TAU + spin - 0.11f
                    val r = (0.15f + 0.85f * sqrt(along)) * maxR * 0.97f
                    val p = Offset(ctr.x + cos(ang) * r, ctr.y + sin(ang) * r)
                    if (first) { path.moveTo(p.x, p.y); first = false } else path.lineTo(p.x, p.y)
                    along += 0.04f
                }
                drawPath(path, laneCol, style = Stroke(width = 24f * k, cap = StrokeCap.Round))
            }

            // the central bar (Milky Way is a barred spiral) — an elongated warm core
            rotate(28f + spinDeg, pivot = ctr) {
                scale(1.95f, 0.6f, pivot = ctr) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            0f to c(0xfff0cf), 0.55f to c(0xffca7a).copy(alpha = 0.7f), 1f to Color(0x00000000),
                            center = ctr, radius = 132f * k,
                        ),
                        radius = 132f * k, center = ctr,
                    )
                }
            }
            // the bright round bulge sitting over the bar
            drawCircle(
                brush = Brush.radialGradient(
                    0f to Color.White, 0.3f to c(0xfff0c4), 0.66f to c(0xffba60).copy(alpha = 0.45f), 1f to Color(0x00000000),
                    center = ctr, radius = 150f * k,
                ),
                radius = 138f * k, center = ctr,
            )
        }

        // our Sun: a highlighted star sitting in one of the outer arms (held still — "you are here")
        val sunPulse = 0.8f + 0.2f * sin(t * 3f)
        val sun = Offset(ctr.x + cos(sunAng) * sunRu * k, ctr.y + sin(sunAng) * sunRu * k * tilt)
        val sa = reveal(t, 4.2f).opacity
        if (sa > 0.01f) {
            drawCircle(Color.White.copy(alpha = 0.5f * sa * sunPulse), radius = 24f * k, center = sun, style = Stroke(width = 2f * k))
            drawCircle(c(0xfff0c8), radius = 6.5f * k, center = sun, alpha = sa)
            drawCircle(Color.White, radius = 3f * k, center = sun, alpha = sa)
        }
    }
    CenterLabel(cx + cos(sunAng) * sunRu, cy + sin(sunAng) * sunRu * tilt + 30f, reveal(t, 4.4f).opacity) {
        Text("THE SUN · YOU ARE HERE", style = spLabel(15f, accent))
    }

    Eyebrow("Our home galaxy", accent, 200f, e1)
    Head(headline("Home to a hundred\nbillion ", "stars", "."), 78f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Our Sun is just one star in a spiral ", "100,000 light-years", " wide — one lap takes 225 million years."))
}

// ───────────────────── The Kuiper Belt — a frozen ring past Neptune ─────────────────────

/** A deterministic scatter of icy bodies filling the belt: angle, radius-fraction, size, alpha. */
private val KUIPER_DOTS: List<FloatArray> = run {
    var s = 0x51ED2C
    fun rnd(): Float { s = s * 1664525 + 1013904223; return ((s ushr 9) and 0xFFFF) / 65535f }
    List(170) {
        floatArrayOf(rnd() * SP_TAU, 0.60f + rnd() * 0.40f, 1.1f + rnd() * 2.3f, 0.30f + rnd() * 0.5f)
    }
}

@Composable
fun BoxScope.SpKuiper(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xa9d6e0)
    val cx = 540f; val cy = 1020f
    val maxRu = 430f
    val tilt = 0.40f
    val spin = t * 0.05f

    // the distant Sun at the centre — just a warm point with a soft halo
    RadialDisc(
        cx, cy, 120f,
        arrayOf(0f to Color(0x66ffd9a0), 0.4f to Color(0x1Affb060), 0.8f to Color(0x00000000), 1f to Color(0x00000000)),
        0.5f, 0.5f, 0.5f,
    )

    val appear = reveal(t, 0.5f, dur = 0.9f).opacity
    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        val maxR = maxRu * k
        scale(1f, tilt, pivot = ctr) {
            drawCircle(Color.White, radius = 7f * k, center = ctr)
            drawCircle(c(0xffe6b0), radius = 13f * k, center = ctr, alpha = 0.6f)

            // Neptune's orbit — the inner edge of the belt
            drawCircle(c(0x6fa6d0).copy(alpha = 0.5f), radius = maxR * 0.52f, center = ctr, style = Stroke(width = 2f * k))

            // the belt itself — a wide ring of icy specks
            KUIPER_DOTS.forEach { d ->
                val ang = d[0] + spin
                val r = d[1] * maxR
                val pos = Offset(ctr.x + cos(ang) * r, ctr.y + sin(ang) * r)
                val twinkle = 0.7f + 0.3f * sin(t * 1.6f + d[0] * 6f)
                drawCircle(c(0xcfeaf2), radius = d[2] * k, center = pos, alpha = (d[3] * twinkle).coerceIn(0f, 1f))
            }
        }
    }

    // Pluto — a highlighted member of the belt
    val pa = reveal(t, 3.0f)
    val pAng = 2.3f; val pR = 0.82f * maxRu
    val px = cx + cos(pAng) * pR; val py = cy + sin(pAng) * pR * tilt
    Sphere(26f, listOf(c(0xe7d3b4), c(0xb08560), c(0x55392a)), glow = Color(0x55c9a87a), modifier = Modifier.offset((px - 13f).dp, (py - 13f).dp))
    CenterLabel(px, py + 22f, pa.opacity) { Text("PLUTO", style = spLabel(15f, accent)) }

    Eyebrow("Past the last planet", accent, 200f, e1)
    Head(headline("A frozen ring\nbeyond ", "Neptune", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Thousands of icy worlds drift here — leftovers from the ", "birth of the solar system", "."))
}

// ───────────────────── Saturn's hexagon — a six-sided polar storm ─────────────────────

@Composable
fun BoxScope.SpHexagon(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xe8d6a6)
    val cx = 540f; val cy = 1040f
    val R = 300f
    val grow = animate(0.2f, 1f, 0.4f, 1.8f, Easing.easeOutCubic)(t)
    val spin = t * 0.18f

    // Saturn's golden cloud-tops as the backdrop
    RadialDisc(
        cx, cy, 520f,
        arrayOf(0f to c(0xf0dcae), 0.5f to c(0xd0a85e), 0.85f to c(0x8a6230), 1f to c(0x6e4e22)),
        0.42f, 0.4f, 0.62f,
    )

    val appear = reveal(t, 0.6f, dur = 0.9f).opacity
    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        fun hexPath(rad: Float, rot: Float): Path {
            val p = Path()
            for (i in 0..6) {
                val a = rot + i * (SP_TAU / 6f) - SP_TAU / 4f
                val x = ctr.x + cos(a) * rad; val y = ctr.y + sin(a) * rad
                if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
            }
            p.close()
            return p
        }
        // nested hexagonal jet streams
        val rings = 5
        for (i in 0 until rings) {
            val f = (rings - i).toFloat() / rings
            val rad = R * k * grow * f
            val a = 0.18f + 0.5f * (1f - f)
            drawPath(hexPath(rad, spin), c(0xfff0c8).copy(alpha = a), style = Stroke(width = (5f - 3f * (1f - f)).coerceAtLeast(1.5f) * k))
        }
        // the central cyclone eye
        val eyeR = R * 0.42f * k * grow
        drawCircle(
            brush = Brush.radialGradient(
                0f to c(0x6e3a14), 0.5f to c(0xb5722e).copy(alpha = 0.7f), 1f to Color(0x00000000),
                center = ctr, radius = eyeR.coerceAtLeast(1f),
            ),
            radius = eyeR.coerceAtLeast(1f), center = ctr,
        )
        // swirling streaks inside the eye
        val streaks = 9
        for (i in 0 until streaks) {
            val a0 = spin * 2.2f + i * (SP_TAU / streaks)
            val r0 = R * 0.10f * k * grow; val r1 = R * 0.40f * k * grow
            val p = Path()
            var rr = r0; var aa = a0; var first = true
            while (rr <= r1) {
                val x = ctr.x + cos(aa) * rr; val y = ctr.y + sin(aa) * rr
                if (first) { p.moveTo(x, y); first = false } else p.lineTo(x, y)
                aa += 0.25f; rr += (r1 - r0) / 18f
            }
            drawPath(p, c(0xffe6b0).copy(alpha = 0.4f), style = Stroke(width = 2.5f * k, cap = StrokeCap.Round))
        }
    }

    Eyebrow("Saturn's north pole", accent, 200f, e1)
    Head(headline("A six-sided\n", "storm", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("A hexagon of racing cloud, wider than ", "two Earths", ", crowns Saturn's pole."))
}

// ───────────────────── Rogue planet — a starless world adrift ─────────────────────

/** A cold, sparse starfield: x-frac, y-frac, size, alpha. */
private val ROGUE_STARS: List<FloatArray> = run {
    var s = 0x2BAD17
    fun rnd(): Float { s = s * 1664525 + 1013904223; return ((s ushr 9) and 0xFFFF) / 65535f }
    List(130) { floatArrayOf(rnd(), rnd(), 0.8f + rnd() * 2.4f, 0.3f + rnd() * 0.55f) }
}

@Composable
fun BoxScope.SpRogue(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x7f8cff)
    val cy = 1060f
    val px = interpolate(listOf(0.5f, 12f), listOf(300f, 760f), Easing.linear)(t)
    val bob = sin(t * 0.5f) * 18f

    // a cold, sparse starfield
    Canvas(Modifier.fillMaxSize()) {
        ROGUE_STARS.forEach { st ->
            val tw = 0.6f + 0.4f * sin(t * 1.4f + st[0] * 30f)
            drawCircle(
                c(0xcfe0ff),
                radius = st[2] * (size.width / 1080f),
                center = Offset(st[0] * size.width, st[1] * size.height),
                alpha = (st[3] * tw).coerceIn(0f, 1f),
            )
        }
    }

    val appear = reveal(t, 0.6f, dur = 1.0f).opacity
    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(px * k, (cy + bob) * k)
        val r = 150f * k
        // the planet body — almost black, lit by nothing
        drawCircle(
            brush = Brush.radialGradient(
                0f to c(0x2a2f4a), 0.6f to c(0x14172a), 1f to c(0x070810),
                center = Offset(ctr.x - r * 0.3f, ctr.y - r * 0.3f), radius = r * 1.4f,
            ),
            radius = r, center = ctr,
        )
        // a thin rim of distant starlight catching one edge
        drawArc(
            color = c(0x9fb0ff).copy(alpha = 0.6f),
            startAngle = 35f, sweepAngle = 120f, useCenter = false,
            topLeft = Offset(ctr.x - r, ctr.y - r), size = Size(r * 2f, r * 2f),
            style = Stroke(width = 4f * k, cap = StrokeCap.Round),
        )
    }

    Eyebrow("A world without a sun", accent, 200f, e1)
    Head(headline("Adrift in the\n", "endless dark", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Cast out of its system, a rogue planet wanders alone in ", "perpetual night", "."))
}

// ───────────────────── Aurora — solar wind paints the sky ─────────────────────

/** High-altitude pinprick stars above the curtains: x-frac, y-frac (upper sky only), size, alpha. */
private val AURORA_STARS: List<FloatArray> = run {
    var s = 0x5C1A7B
    fun rnd(): Float { s = s * 1664525 + 1013904223; return ((s ushr 9) and 0xFFFF) / 65535f }
    List(110) { floatArrayOf(rnd(), rnd() * 0.62f, 0.8f + rnd() * 2.2f, 0.3f + rnd() * 0.5f) }
}

@Composable
fun BoxScope.SpAurora(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x6ff0b0)
    val appear = reveal(t, 0.5f, dur = 1.2f).opacity

    // faint high-altitude starfield
    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        AURORA_STARS.forEach { st ->
            val tw = 0.6f + 0.4f * sin(t * 1.3f + st[0] * 40f)
            drawCircle(
                c(0xdfe9ff), radius = st[2] * k,
                center = Offset(st[0] * size.width, st[1] * size.height),
                alpha = (st[3] * tw).coerceIn(0f, 1f),
            )
        }
    }

    // Earth's curved limb glowing at the horizon (a huge disc centred far below)
    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val lr = 1500f * k
        val lc = Offset(540f * k, 1180f * k + lr)
        drawCircle(
            brush = Brush.radialGradient(0f to c(0x0c1838), 0.7f to c(0x0a1430), 1f to c(0x060a1c), center = lc, radius = lr),
            radius = lr, center = lc,
        )
        // thin atmospheric rim catching the aurora's glow
        drawCircle(accent.copy(alpha = 0.45f), radius = lr + 5f * k, center = lc, style = Stroke(width = 6f * k))
    }

    // shimmering aurora curtains rising off the horizon
    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        for (ci in 0 until 3) {
            val baseX = (300f + ci * 250f) * k
            val hue = when (ci) { 0 -> c(0x4fe6a0); 1 -> c(0x7ff0c0); else -> c(0x9a86f0) }
            val rays = 26
            for (ri in 0 until rays) {
                val f = ri / (rays - 1f)
                val sway = sin(t * 0.8f + ci * 1.3f + f * 3.0f) * 70f * k
                val x = baseX + (f - 0.5f) * 230f * k + sway
                val topY = (360f + sin(t * 0.6f + f * 5f + ci) * 60f) * k
                val botY = (1170f + sin(t * 0.5f + f * 4f) * 40f) * k
                val shimmer = (0.35f + 0.45f * sin(t * 2.2f + ri * 0.6f + ci * 2f)).coerceIn(0.05f, 0.85f)
                drawLine(
                    brush = Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.5f to hue.copy(alpha = 0.06f + 0.08f * shimmer),
                        1f to hue.copy(alpha = 0.7f * shimmer),
                        startY = topY, endY = botY,
                    ),
                    start = Offset(x, topY), end = Offset(x, botY),
                    strokeWidth = 11f * k, cap = StrokeCap.Round,
                )
            }
        }
    }

    Eyebrow("The northern lights", accent, 200f, e1)
    Head(headline("The sky\ncatches ", "fire", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Solar wind strikes the air high above the poles and the night glows in ", "rippling curtains", "."))
}

// ───────────────────── Europa — an ocean beneath the ice ─────────────────────

/** Surface lineae as great-circle chords: start angle, end angle, inward bow (0..1). */
private val EUROPA_CRACKS: List<FloatArray> = run {
    var s = 0x7A1C9F
    fun rnd(): Float { s = s * 1664525 + 1013904223; return ((s ushr 9) and 0xFFFF) / 65535f }
    List(16) { floatArrayOf(rnd() * SP_TAU, rnd() * SP_TAU, 0.05f + rnd() * 0.38f) }
}

@Composable
fun BoxScope.SpEuropa(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x9fd6e6)
    val cracks = reveal(t, 1.1f, dur = 1.4f).opacity

    // a sparse, cold starfield
    Canvas(Modifier.fillMaxSize()) {
        ROGUE_STARS.forEach { st ->
            val tw = 0.6f + 0.4f * sin(t * 1.2f + st[1] * 28f)
            drawCircle(
                c(0xcfe0ff), radius = st[2] * 0.8f * (size.width / 1080f),
                center = Offset(st[0] * size.width, st[1] * size.height),
                alpha = (st[3] * tw * 0.7f).coerceIn(0f, 1f),
            )
        }
    }

    // Jupiter looming, partly off the top-right corner
    val jup = reveal(t, 0.4f, dur = 1.2f).opacity
    At(640f, -180f, jup) {
        Sphere(560f, listOf(c(0xf2e4c4), c(0xcea06a), c(0x95643a)), glow = Color(0x44e0b070))
    }

    // Europa — a bright shell of ice, lit from the upper-left
    val cx = 470f; val cy = 1060f; val r = 230f
    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        val rk = r * k
        drawCircle(
            brush = Brush.radialGradient(
                0f to c(0xf4f8ff), 0.5f to c(0xd2dde9), 0.85f to c(0x93a1b6), 1f to c(0x5d6b80),
                center = Offset(ctr.x - rk * 0.32f, ctr.y - rk * 0.34f), radius = rk * 1.35f,
            ),
            radius = rk, center = ctr,
        )

        // reddish-brown lineae streaking the surface (great-circle chords, bowed inward to stay on the disc)
        EUROPA_CRACKS.forEach { ck ->
            val p0 = Offset(ctr.x + cos(ck[0]) * rk, ctr.y + sin(ck[0]) * rk)
            val p1 = Offset(ctr.x + cos(ck[1]) * rk, ctr.y + sin(ck[1]) * rk)
            val mid = Offset((p0.x + p1.x) / 2f, (p0.y + p1.y) / 2f)
            // unit vector from the chord midpoint toward the disc centre
            val dx = ctr.x - mid.x; val dy = ctr.y - mid.y
            val len = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
            val ux = dx / len; val uy = dy / len
            val bow = ck[2] * rk
            val path = Path()
            val steps = 16
            for (i in 0..steps) {
                val s = i / steps.toFloat()
                val push = bow * sin((s * 3.1415927f))
                val x = p0.x + (p1.x - p0.x) * s + ux * push
                val y = p0.y + (p1.y - p0.y) * s + uy * push
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, c(0xb5623f).copy(alpha = 0.7f * cracks), style = Stroke(width = 3f * k, cap = StrokeCap.Round))
            drawPath(path, c(0x7a3a24).copy(alpha = 0.35f * cracks), style = Stroke(width = 6f * k, cap = StrokeCap.Round))
        }

        // a soft terminator shadow on the far side
        drawArc(
            color = Color(0x33000814),
            startAngle = 25f, sweepAngle = 130f, useCenter = true,
            topLeft = Offset(ctr.x - rk, ctr.y - rk), size = Size(rk * 2f, rk * 2f),
        )
    }

    Eyebrow("Europa · moon of Jupiter", accent, 200f, e1)
    Head(headline("An ocean\nbeneath the ", "ice", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Under a frozen shell hides a global sea holding ", "more water than all of Earth's oceans", "."))
}

// ───────────────────── Quasar — the brightest beacons ─────────────────────

@Composable
fun BoxScope.SpQuasar(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xffb06a)
    val cx = 540f; val cy = 980f
    val grow = animate(0.15f, 1f, 0.4f, 2.0f, Easing.easeOutCubic)(t)
    val pulse = 0.85f + 0.15f * sin(t * 2.4f)

    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)

        // faint host galaxy the quasar outshines
        drawCircle(
            brush = Brush.radialGradient(0f to c(0x4a3a66).copy(alpha = 0.5f), 1f to Color.Transparent, center = ctr, radius = 470f * k),
            radius = 470f * k, center = ctr, alpha = 0.7f * grow,
        )

        // twin relativistic jets, shooting from the core (drawn behind the disc)
        for (dir in intArrayOf(-1, 1)) {
            val len = (560f + 50f * sin(t * 1.5f)) * k * grow
            val baseW = 16f * k; val tipW = 78f * k
            val tipY = ctr.y + dir * len
            val jet = Path().apply {
                moveTo(ctr.x - baseW, ctr.y)
                lineTo(ctr.x + baseW, ctr.y)
                lineTo(ctr.x + tipW, tipY)
                lineTo(ctr.x - tipW, tipY)
                close()
            }
            drawPath(
                jet,
                brush = Brush.linearGradient(
                    listOf(c(0xddf0ff).copy(alpha = 0.85f), c(0x6fb4ff).copy(alpha = 0.35f), Color.Transparent),
                    start = ctr, end = Offset(ctr.x, tipY),
                ),
            )
            // bright spine down the centre of each jet
            drawLine(
                c(0xeaf6ff).copy(alpha = 0.9f * pulse), start = ctr, end = Offset(ctr.x, tipY),
                strokeWidth = 4f * k, cap = StrokeCap.Round,
            )
        }

        // accretion disc — a hot, tilted ring of in-falling gas
        rotate(-24f, pivot = ctr) {
            val dw = 360f * k * grow; val dh = 120f * k * grow
            drawOval(
                color = c(0xffca6a).copy(alpha = 0.5f),
                topLeft = Offset(ctr.x - dw, ctr.y - dh), size = Size(dw * 2f, dh * 2f),
                style = Stroke(width = 26f * k),
            )
            drawOval(
                color = c(0xfff0d0).copy(alpha = 0.85f),
                topLeft = Offset(ctr.x - dw * 0.72f, ctr.y - dh * 0.72f), size = Size(dw * 1.44f, dh * 1.44f),
                style = Stroke(width = 12f * k),
            )
        }

        // central bloom + the black hole and its photon ring
        val coreR = 150f * k * grow
        drawCircle(
            brush = Brush.radialGradient(0f to c(0xffffff).copy(alpha = pulse), 0.4f to c(0xffd79a).copy(alpha = 0.6f * pulse), 1f to Color.Transparent, center = ctr, radius = coreR.coerceAtLeast(1f)),
            radius = coreR.coerceAtLeast(1f), center = ctr,
        )
        val bhR = 30f * k * grow
        drawCircle(c(0x05070f), radius = bhR.coerceAtLeast(1f), center = ctr)
        drawCircle(c(0xfff2d8).copy(alpha = pulse), radius = (bhR + 5f * k).coerceAtLeast(1f), center = ctr, style = Stroke(width = 4f * k))
    }

    Eyebrow("Quasars · galactic beacons", accent, 200f, e1)
    Head(headline("The brightest\nlights in the ", "universe", "."), 80f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("A giant black hole feeds, blazing ", "brighter than entire galaxies", " — seen clear across the cosmos."))
}
