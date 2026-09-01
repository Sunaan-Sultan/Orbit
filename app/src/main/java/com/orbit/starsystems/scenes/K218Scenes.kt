package com.orbit.starsystems.scenes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
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
import kotlin.math.roundToInt
import kotlin.math.sin

private const val K2_HALF_PI = 1.5707964f

// K2-18 is a cool M-type red dwarf — a dim, ruddy ember of a star.
private val K2_STAR = listOf(c(0xffd2b0), c(0xe0744a), c(0x8a3320))
private val K2_STAR_GLOW = Color(0x80e0744a)
private val K2_ACCENT = c(0x7fd6c0)

// K2-18 b — a hazy sub-Neptune we picture as a deep-blue ocean world under cloud.
private val K2_PLANET = listOf(c(0xcfeaff), c(0x4f9fd0), c(0x163a5a))
private val K2_PLANET_GLOW = Color(0x556fbfe6)

// Our Sun, for the side-by-side comparisons.
private val SUN = listOf(c(0xfff7d6), c(0xffc23a), c(0xff8a26))
private val SUN_GLOW = Color(0x80ffa03c)

private fun k2Label(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = size.sp, letterSpacing = 0.12.em, color = color,
)

// ───────────────────── 1 · An ocean world, just maybe ─────────────────────

@Composable
fun BoxScope.K2Intro(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val grow = animate(0f, 1f, 0.4f, 2.2f, Easing.easeOutBack)(t)
    val cx = 560f; val cy = 1080f
    val bob = sin(t * 0.6f) * 10f

    Eyebrow("K2-18 · 124 light-years away", K2_ACCENT, 200f, e1)
    Head(headline("An ocean world,\njust ", "maybe", "."), 84f, 246f, e2)
    BottomLine(1640f, reveal(t, 5.5f), body("A sub-Neptune sitting in its star's ", "habitable zone", " — one of our best hopes for life."))

    // the red dwarf, low and to the left, half off-frame
    Sphere(360f * grow, K2_STAR, glow = K2_STAR_GLOW, modifier = Modifier.offset((40f - 180f * grow).dp, (820f - 180f * grow).dp))
    // a faint orbit sweeping up toward the world
    RingArc(40f, 820f, 1640f, 980f, -14f, Color(0x33ffd9a0), 1.5f, dash = true, alpha = reveal(t, 1.4f, 1.4f).opacity)
    // the world itself
    Sphere(236f * grow, K2_PLANET, glow = K2_PLANET_GLOW, modifier = Modifier.offset((cx - 118f * grow).dp, (cy - 118f * grow + bob).dp))
    CenterLabel(cx, cy + 150f, reveal(t, 2.6f).opacity) { Text("K2-18 b", style = k2Label(20f, K2_ACCENT)) }
}

// ───────────────────── 2 · A cool red dwarf ─────────────────────

@Composable
fun BoxScope.K2Dwarf(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val grow = animate(0f, 1f, 0.6f, 2.4f, Easing.easeOutCubic)(t)
    val sunR = 180f; val dwarfR = sunR * 0.41f; val cy = 940f

    Eyebrow("K2-18 vs. the Sun", K2_ACCENT, 200f, e1)
    Head(headline("A cool\n", "red dwarf", "."), 88f, 246f, e2)
    BottomLine(1640f, reveal(t, 5.5f), body("Barely 40% the Sun's width and far cooler — a small, slow-burning star that will shine for ", "trillions of years", "."))

    Sphere(sunR * 2 * grow, SUN, glow = SUN_GLOW, modifier = Modifier.offset((760f - sunR * grow).dp, (cy - sunR * grow).dp))
    Sphere(dwarfR * 2 * grow, K2_STAR, glow = K2_STAR_GLOW, modifier = Modifier.offset((330f - dwarfR * grow).dp, (cy - dwarfR * grow).dp))

    val la = reveal(t, 2.2f)
    CenterLabel(330f, cy + 130f, la.opacity) { Text("K2-18 · 0.41× SUN", style = k2Label(20f, K2_ACCENT)) }
    CenterLabel(760f, cy + sunR + 26f, la.opacity) { Text("OUR SUN", style = k2Label(20f, c(0xffc23a))) }
    CenterLabel(540f, 1300f, reveal(t, 3f).opacity) { Text("≈ 3,500 °C  ·  half as hot", style = k2Label(24f, c(0xffb38a))) }
}

// ───────────────────── 3 · Bigger than Earth, smaller than Neptune ─────────────────────

@Composable
fun BoxScope.K2Size(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val grow = animate(0f, 1f, 0.5f, 2.3f, Easing.easeOutCubic)(t)
    val cy = 1040f
    val earth = listOf(c(0x9cc4ec), c(0x3d72b8), c(0x16223f))
    val neptune = listOf(c(0x9fb8e6), c(0x3f5fae), c(0x1b2c55))

    Eyebrow("A sub-Neptune", K2_ACCENT, 200f, e1)
    Head(headline("Between\n", "two worlds", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Far bigger than Earth yet smaller than Neptune — ", "2.6× Earth's width", ", and the most common size of world we find."))

    // diameters in Earth-radii: Earth 1, K2-18 b 2.6, Neptune 3.9
    val unit = 64f
    data class Body(val label: String, val rE: Float, val cc: List<Color>, val x: Float)
    val bodies = listOf(
        Body("EARTH", 1f, earth, 180f),
        Body("K2-18 b", 2.6f, K2_PLANET, 500f),
        Body("NEPTUNE", 3.9f, neptune, 850f),
    )
    bodies.forEachIndexed { i, b ->
        val a = reveal(t, 0.8f + i * 0.25f)
        val d = b.rE * unit * grow
        val glow = if (b.label == "K2-18 b") K2_PLANET_GLOW else null
        At(b.x - d / 2f, cy - d / 2f, a.opacity) { Sphere(d, b.cc, glow = glow) }
        CenterLabel(b.x, cy + 220f, a.opacity) { Text(b.label, style = k2Label(18f, if (b.label == "K2-18 b") K2_ACCENT else c(0x9fb1c6))) }
        CenterLabel(b.x, cy + 252f, a.opacity) { Text("${b.rE}× Earth", style = k2Label(15f, c(0x768498))) }
    }
}

// ───────────────────── 4 · In the habitable zone ─────────────────────

@Composable
fun BoxScope.K2Hz(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val cx = 540f; val cy = 1010f
    val aw = 380f; val ah = 168f
    val band = reveal(t, 0.8f, 1.2f)
    val orbit = reveal(t, 1.4f)
    val ang = -K2_HALF_PI + t * 0.7f
    val px = cx + cos(ang) * aw; val py = cy + sin(ang) * ah

    Eyebrow("Where water could stay liquid", K2_ACCENT, 200f, e1)
    Head(headline("In the\n", "habitable zone", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Circling its dim star every ", "33 days", ", K2-18 b sits where sunlight is just gentle enough for liquid water."))

    // the green "Goldilocks" band, drawn as a thick translucent ring
    RingArc(cx, cy, aw * 2f, ah * 2f, 0f, Color(0x3354e0a0), 96f, alpha = band.opacity)
    RingArc(cx, cy, aw * 2f, ah * 2f, 0f, Color(0x8060e8a8), 2f, dash = true, alpha = band.opacity)
    // the orbit line and the world riding it
    RingArc(cx, cy, aw * 2f, ah * 2f, 0f, Color(0x55ffffff), 1.5f, alpha = orbit.opacity)
    Sphere(70f, K2_STAR, glow = K2_STAR_GLOW, modifier = Modifier.offset((cx - 35f).dp, (cy - 35f).dp))
    At(px - 22f, py - 22f, orbit.opacity) { Sphere(44f, K2_PLANET, glow = K2_PLANET_GLOW) }
    CenterLabel(cx, cy - ah - 120f, reveal(t, 2.4f).opacity) { Text("HABITABLE ZONE", style = k2Label(20f, c(0x6fe8b0))) }
}

// ───────────────────── 5 · Caught in transit ─────────────────────

@Composable
fun BoxScope.K2Transit(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x9cc4ec)
    val starCx = 540f; val starCy = 620f; val starR = 150f

    Eyebrow("Discovered · K2 mission, 2015", accent, 200f, e1)
    Head(headline("Caught in\n", "transit", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 6f), body("Each time the world crosses its star it blocks a sliver of light — a ", "tiny, telltale dip", " we can measure from Earth."))

    // the red dwarf, with the planet crossing its face
    Sphere(starR * 2f, K2_STAR, glow = K2_STAR_GLOW, modifier = Modifier.offset((starCx - starR).dp, (starCy - starR).dp))
    val cross = interpolate(listOf(1.4f, 5.0f), listOf(starCx - 200f, starCx + 200f), Easing.easeInOutSine)(t)
    if (t in 1.4f..5.2f) {
        At(cross - 18f, starCy - 18f) { Box(Modifier.size(36.dp).clip(CircleShape).background(c(0x10202c))) }
    }

    // the light-curve below, with a dip timed to the crossing
    val plotL = 110f; val plotR = 980f; val baseY = 1130f; val dipY = 1250f
    val dipC = (plotL + plotR) / 2f
    val curve = reveal(t, 1.2f, 1.2f)
    Canvas(Modifier.fillMaxSize().alpha(curve.opacity)) {
        val k = size.width / 1080f
        fun p(x: Float, y: Float) = Offset(x * k, y * k)
        var x = plotL
        while (x < plotR) {
            drawLine(c(0x33ffffff), p(x, baseY), p(x + 10f, baseY), strokeWidth = 1.5f * k)
            x += 18f
        }
        val path = Path().apply {
            moveTo(p(plotL, baseY).x, p(plotL, baseY).y)
            lineTo(p(dipC - 150f, baseY).x, p(dipC - 150f, baseY).y)
            lineTo(p(dipC - 80f, dipY).x, p(dipC - 80f, dipY).y)
            lineTo(p(dipC + 80f, dipY).x, p(dipC + 80f, dipY).y)
            lineTo(p(dipC + 150f, baseY).x, p(dipC + 150f, baseY).y)
            lineTo(p(plotR, baseY).x, p(plotR, baseY).y)
        }
        drawPath(path, color = accent, style = Stroke(width = 4f * k))
    }
    CenterLabel(dipC, dipY + 40f, reveal(t, 3.4f).opacity) { Text("BRIGHTNESS DIP", style = k2Label(19f, accent)) }
}

// ───────────────────── 6 · Reading the starlight ─────────────────────

@Composable
fun BoxScope.K2Spectrum(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xc9b8d8)

    Eyebrow("Transmission spectroscopy", accent, 200f, e1)
    Head(headline("Reading the\n", "starlight", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 6f), body("As starlight filters through the planet's air, each gas steals its own colours — leaving ", "dark lines that name the molecules", "."))

    // a little star → planet → beam motif up top
    Sphere(120f, K2_STAR, glow = K2_STAR_GLOW, modifier = Modifier.offset((130f).dp, (560f).dp))
    Sphere(64f, K2_PLANET, glow = K2_PLANET_GLOW, modifier = Modifier.offset((340f).dp, (588f).dp))
    val beam = reveal(t, 1f)
    Canvas(Modifier.fillMaxSize().alpha(beam.opacity)) {
        val k = size.width / 1080f
        drawLine(Color(0x66e8def0), Offset(420f * k, 620f * k), Offset(900f * k, 620f * k), strokeWidth = 3f * k, cap = StrokeCap.Round)
    }

    // the spectrum bar
    val barL = 120f; val barW = 840f; val barY = 1010f; val barH = 120f
    val bar = reveal(t, 1.2f, 1f)
    At(barL, barY, bar.opacity) {
        Box(
            Modifier.size(barW.dp, barH.dp).clip(RoundedCornerShape(14.dp)).background(
                Brush.horizontalGradient(listOf(c(0x6a4fa8), c(0x4f8fd0), c(0x6fd6a0), c(0xf0d860), c(0xe07a4a))),
            ),
        )
    }
    // dark absorption lines biting into the spectrum, appearing one by one
    val lines = listOf(0.18f, 0.34f, 0.49f, 0.5f, 0.68f, 0.83f)
    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        lines.forEachIndexed { i, f ->
            val a = reveal(t, 2.0f + i * 0.22f).opacity
            if (a > 0.01f) {
                val x = (barL + f * barW) * k
                val w = (8f + (i % 2) * 5f) * k
                drawLine(c(0x05060a).copy(alpha = 0.85f * a), Offset(x, barY * k), Offset(x, (barY + barH) * k), strokeWidth = w, cap = StrokeCap.Round)
            }
        }
    }
    // molecule tags
    val tags = listOf("CH₄" to 0.34f, "CO₂" to 0.68f, "H₂O" to 0.83f)
    tags.forEachIndexed { i, (name, f) ->
        val a = reveal(t, 3.2f + i * 0.25f)
        CenterLabel(barL + f * barW, barY + barH + 30f, a.opacity) { Text(name, style = k2Label(22f, accent)) }
    }
}

// ───────────────────── 7 · Water in the air ─────────────────────

@Composable
fun BoxScope.K2Water(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x8fd6ff)
    val cx = 540f; val cy = 980f
    val grow = animate(0f, 1f, 0.6f, 2.2f, Easing.easeOutBack)(t)
    val wob = sin(t * 1.6f) * 6f

    Eyebrow("First for a habitable-zone world · 2019", accent, 200f, e1)
    Head(headline("Water\nin the ", "air", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 6f), body("Telescopes caught the fingerprint of ", "water vapour", " in its sky — the first ever found for a planet in the habitable zone."))

    // a water molecule: one oxygen, two hydrogens at ~104.5°
    Canvas(Modifier.fillMaxSize().alpha(grow.coerceIn(0f, 1f))) {
        val k = size.width / 1080f
        val o = Offset(cx * k, (cy + wob) * k)
        val bond = 150f * k
        val a1 = -2.2f; val a2 = -0.94f
        val h1 = Offset(o.x + cos(a1) * bond, o.y + sin(a1) * bond)
        val h2 = Offset(o.x + cos(a2) * bond, o.y + sin(a2) * bond)
        drawLine(c(0xbfeaff), o, h1, strokeWidth = 14f * k, cap = StrokeCap.Round)
        drawLine(c(0xbfeaff), o, h2, strokeWidth = 14f * k, cap = StrokeCap.Round)
        // oxygen
        drawCircle(
            brush = Brush.radialGradient(0f to c(0x9fd6ff), 0.6f to c(0x3f8fd0), 1f to c(0x163a5a), center = Offset(o.x - 26f * k, o.y - 26f * k), radius = 120f * k),
            radius = 92f * k, center = o,
        )
        // hydrogens
        listOf(h1, h2).forEach { h ->
            drawCircle(
                brush = Brush.radialGradient(0f to Color.White, 0.7f to c(0xcfe6f4), 1f to c(0x8fa6b8), center = Offset(h.x - 14f * k, h.y - 14f * k), radius = 60f * k),
                radius = 48f * k, center = h,
            )
        }
    }

    At(cx - 200f, cy + 200f, reveal(t, 2.6f).opacity) {
        Box(Modifier.size(400.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("H₂O", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 88.sp, color = accent, letterSpacing = (-0.02).em))
        }
    }
}

// ───────────────────── 8 · A Hycean world? ─────────────────────

@Composable
fun BoxScope.K2Hycean(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val cx = 540f; val cy = 1020f
    val grow = animate(0.2f, 1f, 0.5f, 2.2f, Easing.easeOutCubic)(t)

    Eyebrow("A Hycean world?", K2_ACCENT, 200f, e1)
    Head(headline("An ocean under\na ", "hydrogen sky", "."), 80f, 246f, e2)
    BottomLine(1648f, reveal(t, 6f), body("JWST found carbon-bearing gases overhead — a tantalising, ", "still-debated", " hint of a vast global sea."))

    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        val r = 290f * k * grow
        if (r < 1f) return@Canvas
        // hazy hydrogen atmosphere — a soft outer halo
        drawCircle(
            brush = Brush.radialGradient(0f to Color.Transparent, 0.72f to Color.Transparent, 0.9f to c(0xbfe8ff).copy(alpha = 0.28f), 1f to c(0x8fc0e0).copy(alpha = 0.05f), center = ctr, radius = r * 1.18f),
            radius = r * 1.18f, center = ctr,
        )
        // the ocean body
        drawCircle(
            brush = Brush.radialGradient(0f to c(0x9fe0ff), 0.45f to c(0x3f8fc8), 0.85f to c(0x18527e), 1f to c(0x0c2c46), center = Offset(ctr.x - r * 0.3f, ctr.y - r * 0.34f), radius = r * 1.3f),
            radius = r, center = ctr,
        )
        // a few bright wave-glints catching the dim red sun
        for (i in 0 until 5) {
            val yy = ctr.y - r * 0.4f + i * (r * 0.32f)
            val ww = (r * 0.9f) * (1f - kotlin.math.abs(i - 2) * 0.18f)
            drawLine(c(0xdff4ff).copy(alpha = 0.18f), Offset(ctr.x - ww / 2f, yy), Offset(ctr.x + ww / 2f, yy), strokeWidth = 3f * k, cap = StrokeCap.Round)
        }
    }

    val lab = reveal(t, 2.6f)
    CenterLabel(cx, cy - 360f, lab.opacity) { Text("HYDROGEN ATMOSPHERE", style = k2Label(18f, c(0xbfe8ff))) }
    CenterLabel(cx, cy + 320f, lab.opacity) { Text("GLOBAL OCEAN", style = k2Label(18f, K2_ACCENT)) }
}

// ───────────────────── 9 · 124 light-years away ─────────────────────

@Composable
fun BoxScope.K2Distance(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f)
    val num = interpolate(listOf(1.2f, 4.2f), listOf(0f, 124f), Easing.easeOutCubic)(t).roundToInt()
    val sunX = 180f; val k2X = 900f; val mapY = 1260f
    val photon = interpolate(listOf(2f, 6.4f), listOf(sunX, k2X), Easing.easeInOutSine)(t)
    val moving = t in 2f..6.6f

    Eyebrow("How far is it?", K2_ACCENT, 300f, e1)
    BottomLine(1648f, reveal(t, 6.6f), body("Its light left in the age of steam engines — we see K2-18 as it was ", "well over a century ago", "."))
    At(84f, 370f + e2.ty, e2.opacity) {
        Text(String.format(Locale.US, "%,d", num), style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 200.sp, color = Color.White, letterSpacing = (-0.04).em, lineHeight = 200.sp))
    }
    At(84f, 640f + e2.ty, e2.opacity) {
        Text("light-years away.", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 54.sp, color = Color.White))
    }
    At(84f, 726f + e2.ty, e2.opacity) {
        Text("≈ 1.2 quadrillion km · in the constellation Leo", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 26.sp, color = c(0x8794ac)))
    }
    At(sunX, mapY, reveal(t, 1.6f).opacity) {
        Box(Modifier.size((k2X - sunX).dp, 1.dp).background(c(0x3a4660)))
    }
    Sphere(40f, SUN, glow = SUN_GLOW, modifier = Modifier.offset((sunX - 20f).dp, (mapY - 20f).dp))
    Sphere(34f, K2_STAR, glow = K2_STAR_GLOW, modifier = Modifier.offset((k2X - 17f).dp, (mapY - 17f).dp))
    CenterLabel(sunX, mapY + 36f, reveal(t, 2f).opacity) { Text("OUR SUN", style = k2Label(18f, c(0xd9a24e))) }
    CenterLabel(k2X, mapY + 36f, reveal(t, 2f).opacity) { Text("K2-18", style = k2Label(18f, K2_ACCENT)) }
    if (moving) {
        At(photon - 6f, mapY - 6f) { Box(Modifier.size(12.dp).clip(CircleShape).background(Color.White)) }
    }
}
