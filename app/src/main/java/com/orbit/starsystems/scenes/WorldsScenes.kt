package com.orbit.starsystems.scenes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlin.math.roundToInt
import kotlin.math.sin

// ───────────────────── Scene 7 · Saturn's rings ─────────────────────

@Composable
fun BoxScope.SceneRings(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.4f)
    val zoom = animate(0.6f, 1.42f, 0.5f, 8f, Easing.easeInOutSine)(t)
    val cx = 540f; val cy = 1200f; val r = 250f; val tilt = -19f
    data class Band(val rm: Float, val th: Float, val color: Color)
    val bands = listOf(
        Band(1.22f, 0.13f, Color(0x80b09c6c)),
        Band(1.44f, 0.24f, Color(0xE6ecdaa6)),
        Band(1.82f, 0.20f, Color(0xBDd2be8a)),
        Band(2.06f, 0.07f, Color(0x6Bb09c6c)),
    )
    Camera(zoom, cx, cy) {
        bands.forEach { b -> RingArc(cx, cy, r * 2 * b.rm, r * 2 * b.rm * 0.30f, tilt, b.color, max(2f, b.th * r)) }
        Sphere(r * 2, listOf(c(0xf3e6bd), c(0xd8bd82), c(0x9c7c4a)), glow = Color(0x29d8bd82), modifier = Modifier.offset((cx - r).dp, (cy - r).dp))
        bands.forEach { b -> RingArc(cx, cy, r * 2 * b.rm, r * 2 * b.rm * 0.30f, tilt, b.color, max(2f, b.th * r), clipTop = cy) }
    }
    Eyebrow("Saturn", c(0xe0cd96), 200f, e1)
    Head(headline("Wide as a world,\n", "thin", " as a whisper."), 88f, 246f, e2)
    val sa = reveal(t, 2.2f)
    At(90f, 470f, sa.opacity) {
        Row {
            StatCol("282,000", " km", "edge to edge", 80f, Color.White, c(0x9a9078))
            Spacer(Modifier.width(54.dp))
            StatCol("~10", " m", "average thickness", 80f, Color.White, c(0x9a9078))
        }
    }
    BottomLine(1640f, e3, body("They span most of the Earth–Moon gap — yet, to scale, ", "thinner than a sheet of paper."))
}

// ───────────────────── Scene 8 · Olympus Mons ─────────────────────

@Composable
fun BoxScope.SceneVolcano(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.4f)
    val zoom = animate(2.1f, 1.0f, 0.4f, 4.2f, Easing.easeOutCubic)(t)
    val everestIn = reveal(t, 3.0f, 1.0f)
    Camera(zoom, 380f, 1180f) {
        Canvas(Modifier.fillMaxSize()) {
            val k = size.width / 1080f
            fun px(x: Float) = x * k
            fun py(y: Float) = (560f + y) * k
            drawRect(
                brush = Brush.verticalGradient(0f to Color(0x00281206), 0.7f to Color(0x405a2214), 1f to Color(0x6678321C)),
                topLeft = Offset(0f, py(0f)), size = Size(size.width, 940f * k),
            )
            val mount = Path().apply {
                moveTo(px(30f), py(900f))
                cubicTo(px(150f), py(720f), px(280f), py(300f), px(380f), py(70f))
                cubicTo(px(480f), py(300f), px(610f), py(720f), px(730f), py(900f))
                close()
            }
            drawPath(mount, brush = Brush.verticalGradient(0f to c(0xd56a40), 0.6f to c(0x9c3f24), 1f to c(0x5f2012), startY = py(70f), endY = py(900f)))
            drawPath(mount, color = Color(0x2EFFC8A0), style = Stroke(2f * k))
            val crater = Path().apply {
                moveTo(px(348f), py(86f))
                quadraticBezierTo(px(380f), py(114f), px(412f), py(86f))
                quadraticBezierTo(px(394f), py(126f), px(380f), py(126f))
                quadraticBezierTo(px(366f), py(126f), px(348f), py(86f))
                close()
            }
            drawPath(crater, Color(0x8C3C160C))
            drawLine(Color(0x59FFAA82), Offset(0f, py(900f)), Offset(size.width, py(900f)), 2f * k)
        }
        if (everestIn.opacity > 0.01f) {
            Canvas(Modifier.fillMaxSize().alpha(everestIn.opacity)) {
                val k = size.width / 1080f
                fun px(x: Float) = x * k
                fun py(y: Float) = (560f + y) * k
                val ev = Path().apply {
                    moveTo(px(812f), py(900f)); lineTo(px(905f), py(566f)); lineTo(px(922f), py(578f)); lineTo(px(1000f), py(900f)); close()
                }
                drawPath(ev, brush = Brush.verticalGradient(0f to c(0xcfd6e0), 1f to c(0x5a6270), startY = py(566f), endY = py(900f)))
                val cap = Path().apply { moveTo(px(905f), py(566f)); lineTo(px(922f), py(578f)); lineTo(px(900f), py(616f)); close() }
                drawPath(cap, Color(0xD9FFFFFF))
            }
        }
    }
    CenterLabel(250f, 1490f, reveal(t, 1.6f).opacity) {
        Text("OLYMPUS MONS", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, letterSpacing = 0.12.em, color = c(0xe8915c), textAlign = TextAlign.Center))
        Text("22 km", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 18.sp, color = c(0xb07a5e)))
    }
    CenterLabel(905f, 1490f, everestIn.opacity) {
        Text("EVEREST", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, letterSpacing = 0.12.em, color = c(0xcfd6e0), textAlign = TextAlign.Center))
        Text("8.8 km", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 15.sp, color = c(0x8a93a4)))
    }
    Eyebrow("Mars", c(0xe8915c), 180f, e1)
    Head(headline("The tallest peak\nwe've ", "ever found."), 84f, 226f, e2)
    BottomLine(1620f, e3, body("Olympus Mons stands ", "2.5× higher than Everest", ", on a base that would blanket Arizona."))
}

// ───────────────────── Scene 9 · Great Red Spot ─────────────────────

@Composable
fun BoxScope.SceneStorm(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val zoom = animate(0.78f, 2.3f, 0.6f, 8f, Easing.easeInOutCubic)(t)
    val cx = 540f; val cy = 1180f; val r = 300f
    val sx = cx - 96f; val sy = cy + 70f
    val swirl = t * 26f
    val earthIn = reveal(t, 3.4f, 1.1f)
    val bandStops = arrayOf(
        0f to c(0xe9d6b2), 0.09f to c(0xcdab78), 0.17f to c(0xe4cb9e), 0.27f to c(0xb98c5e),
        0.37f to c(0xefdcb8), 0.47f to c(0xc49a6a), 0.57f to c(0xe7cfa2), 0.67f to c(0xb58a5c),
        0.78f to c(0xecd9b6), 0.88f to c(0xc8a472), 1f to c(0xddc59a),
    )
    Camera(zoom, sx, sy) {
        Canvas(Modifier.offset((cx - r).dp, (cy - r).dp).size((r * 2).dp).clip(CircleShape)) {
            val d = size.width
            drawRect(Brush.verticalGradient(colorStops = bandStops, startY = 0f, endY = d))
            drawCircle(
                Brush.radialGradient(0.5f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.55f), center = Offset(d * 0.34f, d * 0.30f), radius = d * 0.95f),
                radius = d / 2f, center = Offset(d / 2f, d / 2f),
            )
            val rs = Offset(d * 204f / 600f, d * 370f / 600f)
            rotate(swirl, pivot = rs) {
                drawOval(
                    brush = Brush.radialGradient(listOf(c(0xf1c79c), c(0xd2703f), c(0xa8431f), c(0x7a2c14)), center = rs, radius = d * 100f / 600f),
                    topLeft = Offset(rs.x - d * 95f / 600f, rs.y - d * 66f / 600f), size = Size(d * 190f / 600f, d * 132f / 600f),
                )
                drawOval(color = Color(0x4D7a2c14), topLeft = Offset(rs.x - d * 78f / 600f, rs.y - d * 52f / 600f), size = Size(d * 156f / 600f, d * 104f / 600f), style = Stroke(d * 6f / 600f))
            }
        }
        At(sx + 150f, sy - 56f, earthIn.opacity) { Sphere(112f, listOf(c(0x9cc4ec), c(0x3d72b8), c(0x1a3360)), glow = Color(0x4D508cd2)) }
        CenterLabel(sx + 150f + 56f, sy - 56f + 122f, earthIn.opacity) {
            Text("EARTH", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = 0.12.em, color = c(0x9cc4ec), textAlign = TextAlign.Center))
        }
    }
    Eyebrow("Jupiter", c(0xe0a06a), 190f, e1)
    Head(headline("A storm older than\nthe ", "telescope."), 84f, 236f, e2)
    BottomLine(1620f, e3, body("The Great Red Spot has raged for ", "350+ years", " — a single hurricane wider than the whole Earth."))
}

// ───────────────────── Scene 10 · Diamond rain on Neptune ─────────────────────

@Composable
fun BoxScope.SceneDiamond(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val zoom = animate(0.82f, 1.5f, 0.5f, 8f, Easing.easeInOutSine)(t)
    val cx = 540f; val cy = 1180f; val r = 300f
    data class Gem(val x: Float, val yoff: Float, val sp: Float, val sz: Float, val ph: Float)
    val gems = remember {
        var s = 4127L
        val rnd = { s = (s * 9301 + 49297) % 233280; s / 233280f }
        List(46) { Gem(rnd(), rnd(), 0.18f + rnd() * 0.5f, 5f + rnd() * 12f, rnd() * 6.28f) }
    }
    Camera(zoom, cx, cy) {
        Canvas(Modifier.offset((cx - r).dp, (cy - r).dp).size((r * 2).dp).clip(CircleShape)) {
            val d = size.width
            drawCircle(
                Brush.radialGradient(listOf(c(0x6f9be8), c(0x2b4f9e), c(0x16265c)), center = Offset(d * 0.36f, d * 0.30f), radius = d * 0.92f),
                radius = d / 2f, center = Offset(d / 2f, d / 2f),
            )
            gems.forEach { g ->
                val prog = (g.yoff + t * g.sp) % 1f
                val gy = prog * d; val gx = g.x * d
                val tw = 0.5f + 0.5f * sin(t * 2f + g.ph)
                val sz = g.sz * (d / 600f)
                rotate(45f, pivot = Offset(gx, gy)) {
                    drawRect(
                        brush = Brush.linearGradient(listOf(c(0xf2f8ff), c(0xa9c8f2))),
                        topLeft = Offset(gx - sz / 2f, gy - sz / 2f), size = Size(sz, sz),
                        alpha = (0.45f + 0.55f * tw).coerceIn(0f, 1f),
                    )
                }
            }
        }
    }
    Eyebrow("Neptune", c(0x7fa8e6), 200f, e1)
    Head(headline("Where it rains\n", "diamonds."), 88f, 246f, e2)
    BottomLine(1640f, e3, body("Thousands of km down, heat and pressure split methane apart — and ", "carbon falls as solid diamond."))
}

// ───────────────────── Scene 11 · Venus, day longer than year ─────────────────────

@Composable
fun BoxScope.SceneVenusDay(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.8f)
    val zoom = animate(1.95f, 0.82f, 0.5f, 5.5f, Easing.easeInOutCubic)(t)
    val cx = 540f; val cy = 1040f; val r = 230f
    val sunX = 150f; val sunY = 470f
    val dist = kotlin.math.hypot(cx - sunX, cy - sunY)
    val spin = -t * 7f
    val sysIn = reveal(t, 3.2f, 1.0f)
    Camera(zoom, cx, cy) {
        RingArc(sunX, sunY, dist * 2f, dist * 2f * 0.42f, -8f, Color(0x59dcb37e), 2f, dash = true)
        At(sunX - 75f, sunY - 75f, sysIn.opacity) { Sphere(150f, listOf(c(0xfff7d6), c(0xffd451), c(0xff7a1f)), glow = Color(0x66ff9632)) }
        Canvas(Modifier.offset((cx - r).dp, (cy - r).dp).size((r * 2).dp).clip(CircleShape)) {
            val d = size.width
            drawCircle(
                Brush.radialGradient(listOf(c(0xf6e6c2), c(0xdcb37e), c(0x9a7440)), center = Offset(d * 0.36f, d * 0.30f), radius = d * 0.92f),
                radius = d / 2f, center = Offset(d / 2f, d / 2f),
            )
            rotate(spin, pivot = Offset(d / 2f, d / 2f)) {
                drawCircle(
                    Brush.sweepGradient(
                        listOf(Color(0x00FFEEC8), Color(0x38FFEEC8), Color(0x00966E3C), Color(0x38FFEEC8), Color(0x00966E3C), Color(0x38FFEEC8), Color(0x00FFEEC8)),
                        center = Offset(d / 2f, d / 2f),
                    ),
                    radius = d / 2f, center = Offset(d / 2f, d / 2f),
                )
            }
        }
    }
    Eyebrow("Venus", c(0xdcb37e), 170f, e1)
    Head(headline("A day longer\nthan its ", "year."), 86f, 216f, e2)
    At(90f, 1300f, sysIn.opacity) {
        Row {
            LabeledStat("ONE DAY", c(0xdcb37e), "243", null, "Earth days", 92f)
            Spacer(Modifier.width(64.dp))
            LabeledStat("ONE YEAR", c(0x9a9078), "225", null, "Earth days", 92f)
        }
    }
    BottomLine(1640f, e3, body("Venus turns ", "backwards", ", so slowly that one spin outlasts a full lap around the Sun."))
}

// ───────────────────── Scene 12 · Uranus on its side ─────────────────────

@Composable
fun BoxScope.SceneUranus(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val zoom = animate(0.68f, 1.5f, 0.5f, 8f, Easing.easeInOutSine)(t)
    val cx = 540f; val cy = 1190f; val r = 250f
    Camera(zoom, cx, cy) {
        RingArc(cx, cy, r * 2 * 1.55f, r * 2 * 1.55f * 0.30f, 84f, Color(0x8Cb2dee0), max(2f, r * 0.05f))
        RingArc(cx, cy, r * 2 * 1.9f, r * 2 * 1.9f * 0.30f, 84f, Color(0x47b2dee0), max(2f, r * 0.022f))
        Sphere(r * 2, listOf(c(0xd2eff0), c(0x9ac8cb), c(0x5a8d91)), glow = Color(0x299ac8cb), modifier = Modifier.offset((cx - r).dp, (cy - r).dp))
        RingArc(cx, cy, r * 2 * 1.55f, r * 2 * 1.55f * 0.30f, 84f, Color(0x8Cb2dee0), max(2f, r * 0.05f), clipTop = cy)
        RingArc(cx, cy, r * 2 * 1.9f, r * 2 * 1.9f * 0.30f, 84f, Color(0x47b2dee0), max(2f, r * 0.022f), clipTop = cy)
    }
    Eyebrow("Uranus", c(0x9ac8cb), 196f, e1)
    Head(headline("The planet that\n", "rolls", " on its side."), 88f, 242f, e2)
    val sa = reveal(t, 2.2f)
    At(90f, 470f, sa.opacity) {
        Row {
            StatCol("98", "°", "axial tilt", 86f, Color.White, c(0x7fa8a8))
            Spacer(Modifier.width(60.dp))
            StatCol("42", " yrs", "of unbroken night", 86f, Color.White, c(0x7fa8a8))
        }
    }
    BottomLine(1640f, e3, body("Tipped almost fully over, Uranus orbits the Sun like a barrel — each pole bakes in ", "42 years of daylight", ", then 42 of dark."))
}

// ───────────────────── Scene 13 · Mercury, fire & ice ─────────────────────

@Composable
fun BoxScope.SceneMercuryTemp(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val zoom = animate(0.72f, 1.4f, 0.5f, 8f, Easing.easeInOutSine)(t)
    val cx = 540f; val cy = 1200f; val r = 290f
    val hot = interpolate(listOf(1.4f, 3.4f), listOf(20f, 430f), Easing.easeOutCubic)(t).roundToInt()
    val cold = interpolate(listOf(3.8f, 5.8f), listOf(20f, -180f), Easing.easeOutCubic)(t).roundToInt()
    Camera(zoom, cx, cy) {
        Canvas(Modifier.fillMaxSize()) {
            val k = size.width / 1080f
            drawCircle(
                Brush.radialGradient(listOf(Color(0x6BFF9632), Color(0x10FF7828), Color(0x00FF7828)), center = Offset(130f * k, cy * k), radius = 360f * k),
                radius = 360f * k, center = Offset(130f * k, cy * k),
            )
        }
        Canvas(Modifier.offset((cx - r).dp, (cy - r).dp).size((r * 2).dp).clip(CircleShape)) {
            val d = size.width
            drawCircle(
                Brush.radialGradient(listOf(c(0xd8ccba), c(0x9c9078), c(0x4f4738)), center = Offset(d * 0.40f, d * 0.32f), radius = d * 0.92f),
                radius = d / 2f, center = Offset(d / 2f, d / 2f),
            )
            drawCircle(Color(0x38000000), radius = d * 0.05f, center = Offset(d * 0.30f, d * 0.60f))
            drawCircle(Color(0x2E000000), radius = d * 0.045f, center = Offset(d * 0.62f, d * 0.40f))
            drawCircle(Color(0x29000000), radius = d * 0.03f, center = Offset(d * 0.70f, d * 0.72f))
            drawRect(Brush.horizontalGradient(0f to Color(0xB8FF8C28), 0.42f to Color(0x29FF7828), 0.56f to Color(0x00FF7828)))
            drawRect(Brush.horizontalGradient(0f to Color(0x005082D2), 0.44f to Color(0x005082D2), 0.7f to Color(0x1F5082D2), 1f to Color(0x995082D2)))
        }
    }
    Eyebrow("Mercury", c(0xff9e34), 190f, e1)
    Head(headline("Fire on one side,\n", "ice", " on the other."), 84f, 236f, e2)
    val sa = reveal(t, 2.2f)
    At(90f, 470f, sa.opacity) {
        Row {
            LabeledStat("DAY SIDE", c(0xff9e34), "+$hot", "°C", null, 80f)
            Spacer(Modifier.width(54.dp))
            LabeledStat("NIGHT SIDE", c(0x7fa8e6), "$cold", "°C", null, 80f)
        }
    }
    BottomLine(1640f, e3, body("With almost no air to move the heat around, Mercury swings over ", "600°C between day and night", " — the widest gap of any planet."))
}

// ───────────────────── Scene 14 · Saturn would float ─────────────────────

@Composable
fun BoxScope.SceneSaturnFloat(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val zoom = animate(1.75f, 0.86f, 0.5f, 5f, Easing.easeOutCubic)(t)
    val cx = 540f; val cy = 1120f; val r = 230f; val tilt = -19f
    val bob = sin(t * 1.1f) * 12f
    val waterY = cy + r * 0.42f
    val oceanIn = reveal(t, 2.2f, 1.0f)
    Camera(zoom, cx, cy) {
        RingArc(cx, cy + bob, r * 2 * 1.44f, r * 2 * 1.44f * 0.30f, tilt, Color(0xD9ecdaa6), max(2f, r * 0.12f))
        RingArc(cx, cy + bob, r * 2 * 1.86f, r * 2 * 1.86f * 0.30f, tilt, Color(0x99d2be8a), max(2f, r * 0.07f))
        Sphere(r * 2, listOf(c(0xf3e6bd), c(0xd8bd82), c(0x9c7c4a)), glow = Color(0x29d8bd82), modifier = Modifier.offset((cx - r).dp, (cy - r + bob).dp))
        At(0f, waterY, oceanIn.opacity) {
            Box(Modifier.size(1080.dp, 900.dp).background(Brush.verticalGradient(0f to Color(0x9E3a78c8), 0.4f to Color(0xDB183c78), 1f to Color(0xF20c1e46))))
        }
        At(0f, waterY, oceanIn.opacity) { Box(Modifier.size(1080.dp, 4.dp).background(Color(0xB3b4e1ff))) }
    }
    Eyebrow("Saturn", c(0xe0cd96), 196f, e1)
    Head(headline("Light enough\nto ", "float."), 88f, 242f, e2)
    val sa = reveal(t, 1.8f)
    At(90f, 470f, sa.opacity) {
        Row {
            StatCol("0.69", null, "Saturn · g/cm³", 80f, Color.White, c(0xb6a479))
            Spacer(Modifier.width(54.dp))
            StatCol("1.00", null, "water · g/cm³", 80f, c(0x7fb0ff), c(0x7fa0c8))
        }
    }
    BottomLine(1660f, e3, body("Saturn is the only planet less dense than water — drop it in a big enough ocean and ", "it would bob on the surface."))
}

// ───────────────────── Scene 15 · The empty asteroid belt ─────────────────────

@Composable
fun BoxScope.SceneAsteroidBelt(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val zoom = animate(2.5f, 0.86f, 0.4f, 5.2f, Easing.easeOutCubic)(t)
    val sun = 540f; val cyc = 1080f
    val marsR = 250f; val jupR = 560f; val beltMin = 320f; val beltMax = 470f; val sq = 0.34f
    data class Rock(val x: Float, val y: Float, val sz: Float, val o: Float)
    val rocks = remember {
        var s = 7723L
        val rnd = { s = (s * 9301 + 49297) % 233280; s / 233280f }
        List(90) {
            val ang = rnd() * 6.2832f; val rad = beltMin + rnd() * (beltMax - beltMin)
            Rock(sun + cos(ang) * rad, cyc + sin(ang) * rad * sq, 2f + rnd() * 5f, 0.4f + rnd() * 0.5f)
        }
    }
    val heroCy = cyc + beltMax * sq
    Camera(zoom, sun, heroCy) {
        RingArc(sun, cyc, marsR * 2f, marsR * 2f * sq, 0f, Color(0x66b5462a), 1f)
        RingArc(sun, cyc, jupR * 2f, jupR * 2f * sq, 0f, Color(0x66c8a072), 1f)
        Sphere(30f, listOf(c(0xfff7d6), c(0xffc23a), c(0xff8a26)), glow = Color(0x80ffa03c), modifier = Modifier.offset((sun - 15f).dp, (cyc - 15f).dp))
        Sphere(14f, listOf(c(0xe8915c), c(0xb5462a), c(0x5f2012)), modifier = Modifier.offset((sun + marsR - 7f).dp, (cyc - 7f).dp))
        Sphere(40f, listOf(c(0xecd8b4), c(0xc8a072), c(0x8f6a40)), modifier = Modifier.offset((sun + jupR - 20f).dp, (cyc - 20f).dp))
        Canvas(Modifier.fillMaxSize()) {
            val k = size.width / 1080f
            rocks.forEach { rk ->
                rotate(30f, pivot = Offset(rk.x * k, rk.y * k)) {
                    drawRect(Color(0xFFa89c84).copy(alpha = rk.o), topLeft = Offset((rk.x - rk.sz / 2f) * k, (rk.y - rk.sz / 2f) * k), size = Size(rk.sz * k, rk.sz * k))
                }
            }
        }
    }
    Eyebrow("The asteroid belt", c(0xb6a479), 190f, e1)
    Head(headline("Millions of rocks,\n", "almost", " all empty."), 86f, 236f, e2)
    val sa = reveal(t, 2.4f)
    At(90f, 470f, sa.opacity) {
        Row {
            StatCol("1M+", null, "known asteroids", 80f, Color.White, c(0x9a9078))
            Spacer(Modifier.width(54.dp))
            StatCol("~1M", " km", "typical gap apart", 80f, Color.White, c(0x9a9078))
        }
    }
    BottomLine(1640f, e3, body("They're spread so thinly that every probe we've sent ", "flew straight through", " without coming near a single one."))
}

// ───────────────────── Scene 16 · Pluto's 248-year orbit ─────────────────────

@Composable
fun BoxScope.ScenePlutoYear(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val zoom = animate(1.9f, 0.82f, 0.5f, 5.2f, Easing.easeOutCubic)(t)
    val cx = 540f; val cy = 1080f; val rx = 430f; val ry = 300f; val rot = -12f
    val yrs = interpolate(listOf(1.6f, 4.6f), listOf(0f, 248f), Easing.easeOutExpo)(t).roundToInt()
    val ang = -3.1416f * 0.62f + t * 0.12f
    val pxx = cx + cos(ang) * rx; val pyy = cy + sin(ang) * ry
    Camera(zoom, cx, cy) {
        RingArc(cx, cy, rx, ry, rot, Color(0x663f72b8), 1f)
        RingArc(cx, cy, rx * 2f, ry * 2f, rot, Color(0x8Cc9b8d8), 1f, dash = true)
        Sphere(56f, listOf(c(0xfff7d6), c(0xffc23a), c(0xff8a26)), glow = Color(0x80ffa03c), modifier = Modifier.offset((cx - 28f).dp, (cy - 28f).dp))
        Sphere(16f, listOf(c(0xe4d6e6), c(0xc9b8d8), c(0x7a6e88)), glow = Color(0x66c9b8d8), modifier = Modifier.offset((pxx - 8f).dp, (pyy - 8f).dp))
    }
    Eyebrow("Pluto", c(0xc9b8d8), 196f, e1)
    Head(headline("One lap takes\n", "248", " years."), 88f, 242f, e2)
    val sa = reveal(t, 1.8f)
    At(90f, 470f, sa.opacity) {
        Column {
            Text("$yrs", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 130.sp, color = Color.White, letterSpacing = (-0.02).em, lineHeight = 130.sp))
            Text("Earth years per orbit", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 40.sp, color = Color.White), modifier = Modifier.padding(top = 6.dp))
        }
    }
    BottomLine(1640f, e3, body("Pluto creeps so far out that since its discovery in ", "1930, it still hasn't finished a single orbit."))
}
