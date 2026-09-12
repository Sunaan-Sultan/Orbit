package com.orbit.starsystems.scenes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.orbit.starsystems.core.Easing
import com.orbit.starsystems.core.animate
import com.orbit.starsystems.core.reveal
import com.orbit.starsystems.ui.OrbitFont
import com.orbit.starsystems.ui.Sphere
import com.orbit.starsystems.ui.Starfield
import kotlin.math.cos
import kotlin.math.sin

private val TG_RED = c(0xe8705a)
private val TG_EMBER_C = c(0xff9a6a)
private val TG_MUTED = c(0x9a8478)
private val TG_FAINT = c(0x7f8898)
private val TG_EARTH = c(0x9cc4ec)

private const val TG_TAU = 6.2831855f

private val TG_STAR_STOPS = arrayOf(
    0f to c(0xffd0a8), 0.34f to c(0xff8a52), 0.72f to c(0xc2401c), 1f to c(0x5e1a0c),
)
private val TG_HALO_STOPS = arrayOf(
    0f to Color(0x50ff6a34), 0.5f to Color(0x14c8401c), 0.85f to Color(0x00000000), 1f to Color(0x00000000),
)
private val TG_SUN_STOPS = arrayOf(
    0f to c(0xfff8e0), 0.45f to c(0xffd267), 0.8f to c(0xf0a52e), 1f to c(0xc57a1e),
)
private val TG_SUN_HALO = arrayOf(
    0f to Color(0x44ffc24a), 0.5f to Color(0x12ffa838), 0.85f to Color(0x00000000), 1f to Color(0x00000000),
)

private val TG_JUPITER = listOf(c(0xe6cfae), c(0xc08f68), c(0x5c402c))
private val TG_WARM = listOf(c(0xf0c0a0), c(0xc07a58), c(0x5e3024))
private val TG_TEMPERATE = listOf(c(0xbfe0cc), c(0x74a890), c(0x2c4a3e))
private val TG_COLD = listOf(c(0xc8d6e6), c(0x7e90aa), c(0x303c50))

private fun tgLabel(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = size.sp,
    letterSpacing = 0.14.em, color = color,
)

private fun tgBody(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = size.sp, color = color,
)

private fun tgRnd(seed: Int, count: Int, each: Int): List<FloatArray> {
    var s = seed
    fun next(): Float {
        s = s * 1664525 + 1013904223
        return ((s ushr 9) and 0xFFFF) / 65535f
    }
    return List(count) { FloatArray(each) { next() } }
}

private class TgPoint(val x: Float, val y: Float, val r: Float)

private val TG_ARIES = listOf(
    TgPoint(766f, 706f, 13f),
    TgPoint(612f, 826f, 10f),
    TgPoint(556f, 876f, 7f),
    TgPoint(318f, 1022f, 8f),
)

private val TG_ARIES_LINES = listOf(0 to 1, 1 to 2, 2 to 3)

@Composable
fun BoxScope.TgIntro(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    Starfield(t)

    val lines = animate(0f, 1f, 0.9f, 2.8f, Easing.easeInOutCubic)(t)
    val markA = animate(0f, 1f, 3.0f, 3.9f, Easing.easeOutBack)(t)
    val emberA = animate(0f, 1f, 4.2f, 5.2f, Easing.easeOutCubic)(t)
    val appear = reveal(t, 0.6f, dur = 0.9f).opacity
    val mx = 468f; val my = 1118f

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f

        TG_ARIES_LINES.forEachIndexed { i, pair ->
            val on = (lines * TG_ARIES_LINES.size - i).coerceIn(0f, 1f)
            if (on <= 0f) return@forEachIndexed
            val a = TG_ARIES[pair.first]; val b = TG_ARIES[pair.second]
            drawLine(
                c(0x8fa4c4).copy(alpha = 0.26f * on),
                Offset(a.x * k, a.y * k),
                Offset((a.x + (b.x - a.x) * on) * k, (a.y + (b.y - a.y) * on) * k),
                strokeWidth = 2f * k,
            )
        }

        TG_ARIES.forEachIndexed { i, st ->
            val a = (lines * 1.7f - i * 0.06f).coerceIn(0f, 1f)
            val tw = 0.82f + 0.18f * sin(t * 1.3f + i * 1.9f)
            drawCircle(Color.White.copy(alpha = 0.13f * a), radius = st.r * 2.2f * k, center = Offset(st.x * k, st.y * k))
            drawCircle(Color.White.copy(alpha = (0.9f * a * tw).coerceIn(0f, 1f)), radius = st.r * 0.5f * k, center = Offset(st.x * k, st.y * k))
        }

        if (markA > 0.01f) {
            val mp = Offset(mx * k, my * k)
            drawCircle(
                TG_RED.copy(alpha = 0.7f * markA), radius = 50f * k * markA, center = mp,
                style = Stroke(width = 2.6f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f * k, 12f * k))),
            )
            if (emberA > 0.01f) {
                val tw = (0.55f + 0.45f * sin(t * 2.4f)).coerceIn(0f, 1f)
                drawCircle(Color(0x50ff6a34).copy(alpha = emberA), radius = 26f * k, center = mp)
                drawCircle(TG_EMBER_C.copy(alpha = emberA * tw), radius = 6f * k, center = mp)
            }
        }
    }

    CenterLabel(766f, 736f, animate(0f, 1f, 1.9f, 2.7f, Easing.easeOutCubic)(t)) {
        Text("HAMAL", style = tgLabel(13f, TG_FAINT))
    }
    CenterLabel(612f, 856f, animate(0f, 1f, 2.4f, 3.2f, Easing.easeOutCubic)(t)) {
        Text("SHERATAN", style = tgLabel(13f, TG_FAINT))
    }
    CenterLabel(mx, my + 68f, markA) { Text("TEEGARDEN", style = tgLabel(17f, TG_RED)) }
    CenterLabel(mx, my + 104f, emberA) { Text("uncharted until 2003", style = tgBody(21f, TG_FAINT)) }

    val s = reveal(t, 4.6f)
    At(90f, 1400f + s.ty, s.opacity) { StatCol("12.5", " ly", "from here", 72f, Color.White, TG_MUTED) }
    At(430f, 1400f + s.ty, s.opacity) { StatCol("2003", null, "when we saw it", 72f, TG_RED, TG_MUTED) }
    At(772f, 1400f + s.ty, s.opacity) { StatCol("M7V", null, "red dwarf", 72f, Color.White, TG_MUTED) }

    Eyebrow("Hiding in the horns of Aries", TG_RED, 200f, e1)
    Head(headline("The star we\nalmost ", "missed", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.8f), body("It sits among our closest neighbours, and yet it went ", "uncatalogued until this century", "."))
}

@Composable
fun BoxScope.TgHidden(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xffb27a)

    val field = tgRnd(0x51AA31, 3, 26)
    val sweep = animate(0f, 3f, 1.0f, 5.2f, Easing.linear)(t)
    val foundA = animate(0f, 1f, 5.0f, 5.9f, Easing.easeOutBack)(t)
    val appear = reveal(t, 0.5f, dur = 0.9f).opacity

    val plateY = floatArrayOf(720f, 1010f, 1300f)
    val shift = floatArrayOf(0f, 54f, 108f)
    val left = 150f; val w = 760f; val h = 214f

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        plateY.forEachIndexed { pi, py ->
            val on = (sweep - pi).coerceIn(0f, 1f)
            if (on <= 0.01f) return@forEachIndexed

            drawRect(
                color = c(0x0c1018).copy(alpha = 0.92f * on),
                topLeft = Offset(left * k, (py - h / 2f) * k),
                size = Size(w * k, h * k),
            )
            drawRect(
                color = c(0x39465c).copy(alpha = 0.5f * on),
                topLeft = Offset(left * k, (py - h / 2f) * k),
                size = Size(w * k, h * k),
                style = Stroke(width = 1.6f * k),
            )

            field[0].indices.forEach { i ->
                val sx = left + 28f + field[0][i] * (w - 56f)
                val sy = py - h / 2f + 24f + field[1][i] * (h - 48f)
                drawCircle(
                    Color.White.copy(alpha = (0.2f + field[2][i] * 0.48f) * on),
                    radius = (1.4f + field[2][i] * 2.4f) * k,
                    center = Offset(sx * k, sy * k),
                )
            }

            val tx = left + 190f + shift[pi]
            drawCircle(TG_EMBER_C.copy(alpha = 0.92f * on), radius = 6.5f * k, center = Offset(tx * k, py * k))
            if (foundA > 0.01f) {
                drawCircle(
                    accent.copy(alpha = 0.75f * foundA), radius = 30f * k, center = Offset(tx * k, py * k),
                    style = Stroke(width = 2.4f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * k, 10f * k))),
                )
            }
        }

        if (foundA > 0.01f) {
            val x0 = left + 190f
            drawLine(
                accent.copy(alpha = 0.5f * foundA),
                Offset(x0 * k, plateY[0] * k), Offset((x0 + shift[2]) * k, plateY[2] * k),
                strokeWidth = 2f * k, cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f * k, 11f * k)),
            )
        }
    }

    CenterLabel(966f, 706f, (sweep - 0f).coerceIn(0f, 1f)) { Text("PLATE I", style = tgLabel(13f, TG_FAINT)) }
    CenterLabel(966f, 996f, (sweep - 1f).coerceIn(0f, 1f)) { Text("PLATE II", style = tgLabel(13f, TG_FAINT)) }
    CenterLabel(966f, 1286f, (sweep - 2f).coerceIn(0f, 1f)) { Text("PLATE III", style = tgLabel(13f, TG_FAINT)) }
    CenterLabel(540f, 1436f, foundA) { Text("THE DOT THAT MOVED", style = tgLabel(17f, accent)) }

    Eyebrow("Found in the archive", accent, 200f, e1)
    Head(headline("Nobody looked.\nSomeone ", "looked again", "."), 80f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.0f), body("It surfaced in survey images taken to hunt asteroids — ", "data already years old", " when anyone noticed."))
}

@Composable
fun BoxScope.TgTiny(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xff9a6a)
    val cy = 1010f

    val grow = animate(0.08f, 1f, 0.5f, 2.4f, Easing.easeOutCubic)(t)
    val popTg = animate(0f, 1f, 1.8f, 3.2f, Easing.easeOutBack)(t)
    val popJup = animate(0f, 1f, 2.4f, 3.8f, Easing.easeOutBack)(t)

    val sunCx = 232f; val sunR = 300f * grow
    val tgCx = 716f; val tgR = 300f * 0.107f * popTg
    val jupCx = 888f; val jupD = 60f * popJup

    RadialDisc(sunCx, cy, sunR + 130f, TG_SUN_HALO, 0.5f, 0.5f, 0.5f)
    RadialDisc(sunCx, cy, sunR, TG_SUN_STOPS, 0.5f, 0.42f, 0.6f)
    RadialDisc(tgCx, cy, tgR * 3.4f, TG_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(tgCx, cy, tgR, TG_STAR_STOPS, 0.5f, 0.42f, 0.6f)

    At(jupCx - jupD / 2f, cy - jupD / 2f, popJup) { Sphere(jupD, TG_JUPITER) }

    CenterLabel(sunCx, cy + sunR + 34f, grow) { Text("THE SUN", style = tgLabel(16f, c(0xffc24a))) }
    CenterLabel(tgCx, cy + 62f, popTg) { Text("TEEGARDEN", style = tgLabel(14f, accent)) }
    CenterLabel(jupCx, cy + 62f, popJup) { Text("JUPITER", style = tgLabel(14f, TG_MUTED)) }

    val s = reveal(t, 4.4f)
    At(90f, 1408f + s.ty, s.opacity) { StatCol("0.09", "×", "the Sun's mass", 64f, Color.White, TG_MUTED) }
    At(430f, 1408f + s.ty, s.opacity) { StatCol("0.11", "×", "its width", 64f, accent, TG_MUTED) }
    At(772f, 1408f + s.ty, s.opacity) { StatCol("100", null, "Jupiters of mass", 64f, Color.White, TG_MUTED) }

    Eyebrow("Barely a star at all", accent, 200f, e1)
    Head(headline("A hundred Jupiters\nin ", "one Jupiter's", " skin."), 76f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.6f), body("A shade less mass and hydrogen would never have caught fire — ", "it would be a brown dwarf", "."))
}

@Composable
fun BoxScope.TgEmber(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xff8a52)

    val appear = reveal(t, 0.6f, dur = 1.0f).opacity
    val barA = animate(0f, 1f, 2.2f, 4.4f, Easing.easeOutCubic)(t)
    val cy = 940f

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(540f * k, cy * k)
        val pulse = 1f + 0.03f * sin(t * 1.6f)
        val r = 206f * k * pulse

        drawCircle(
            brush = Brush.radialGradient(0f to Color(0x46ff6a34), 1f to Color.Transparent, center = ctr, radius = r * 2.2f),
            radius = r * 2.2f, center = ctr,
        )
        drawCircle(
            brush = Brush.radialGradient(
                0f to c(0xffc79c), 0.34f to c(0xff8a52), 0.74f to c(0xbe3c18), 1f to c(0x5a180a),
                center = Offset(ctr.x - r * 0.22f, ctr.y - r * 0.24f), radius = r * 1.25f,
            ),
            radius = r, center = ctr,
        )

        for (i in 0 until 7) {
            val ang = i * (TG_TAU / 7f) + t * 0.16f
            val cc = Offset(ctr.x + cos(ang) * r * 0.5f, ctr.y + sin(ang) * r * 0.5f)
            drawCircle(
                brush = Brush.radialGradient(0f to c(0xffb070).copy(alpha = 0.32f), 1f to Color.Transparent, center = cc, radius = r * 0.34f),
                radius = r * 0.34f, center = cc,
            )
        }
    }

    Canvas(Modifier.fillMaxSize().alpha(barA)) {
        val k = size.width / 1080f
        val l = 130f * k; val rgt = 950f * k; val y = 1300f * k
        drawLine(c(0x3c4658), Offset(l, y), Offset(rgt, y), strokeWidth = 3f * k, cap = StrokeCap.Round)
        for (i in 0..3) {
            val x = l + (rgt - l) * (i / 3f)
            drawLine(c(0x55606e), Offset(x, y - 11f * k), Offset(x, y + 11f * k), strokeWidth = 2.2f * k)
        }
        drawCircle(accent, radius = 13f * k, center = Offset(l, y))
        drawCircle(c(0xffd267), radius = 13f * k, center = Offset(rgt, y))
    }

    CenterLabel(196f, 1332f, barA) { Text("TEEGARDEN", style = tgLabel(13f, accent)) }
    CenterLabel(884f, 1332f, barA) { Text("THE SUN", style = tgLabel(13f, c(0xffc24a))) }
    CenterLabel(540f, 1232f, barA) { Text("each step × 10", style = tgBody(19f, TG_FAINT)) }

    val s = reveal(t, 4.8f)
    At(90f, 1418f + s.ty, s.opacity) { StatCol("2,900", " K", "surface", 62f, accent, TG_MUTED) }
    At(470f, 1418f + s.ty, s.opacity) { StatCol("0.0007", "×", "the Sun's light", 62f, Color.White, TG_MUTED) }

    Eyebrow("An ember, not a furnace", accent, 200f, e1)
    Head(headline("A thousand times\nfainter than ", "our Sun", "."), 80f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.0f), body("Even from twelve light-years away it is ", "thousands of times too dim for the eye", "."))
}

@Composable
fun BoxScope.TgAncient(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xc9a86a)

    val drawT = animate(0f, 1f, 1.0f, 4.4f, Easing.easeInOutCubic)(t)
    val sunMark = animate(0f, 1f, 3.6f, 4.5f, Easing.easeOutBack)(t)
    val nowMark = animate(0f, 1f, 4.6f, 5.4f, Easing.easeOutBack)(t)

    val left = 130f; val right = 950f; val y = 1010f
    val tgFrac = 1f - 8.0f / 13.8f
    val sunFrac = 1f - 4.6f / 13.8f
    val tgX = left + (right - left) * tgFrac
    val sunX = left + (right - left) * sunFrac

    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val yk = y * k

        drawLine(c(0x39414f), Offset(left * k, yk), Offset(right * k, yk), strokeWidth = 3f * k, cap = StrokeCap.Round)
        drawLine(
            accent.copy(alpha = 0.85f), Offset(left * k, yk),
            Offset((left + (right - left) * drawT) * k, yk), strokeWidth = 5f * k, cap = StrokeCap.Round,
        )

        if (drawT >= tgFrac) {
            val tw = (0.6f + 0.4f * sin(t * 2.2f)).coerceIn(0f, 1f)
            drawCircle(Color(0x50ff6a34), radius = 30f * k, center = Offset(tgX * k, yk))
            drawCircle(TG_EMBER_C.copy(alpha = tw), radius = 12f * k, center = Offset(tgX * k, yk))
        }
        if (sunMark > 0.01f) {
            drawCircle(c(0xffd267).copy(alpha = sunMark), radius = 11f * k, center = Offset(sunX * k, yk))
        }
        if (nowMark > 0.01f) {
            drawLine(
                Color.White.copy(alpha = 0.6f * nowMark), Offset(right * k, (y - 44f) * k), Offset(right * k, (y + 44f) * k),
                strokeWidth = 2.4f * k,
            )
        }
    }

    CenterLabel(left + 40f, y - 92f, drawT.coerceIn(0f, 1f)) { Text("BIG BANG", style = tgLabel(13f, TG_FAINT)) }
    CenterLabel(tgX, y - 100f, (drawT * 2.2f - 1.0f).coerceIn(0f, 1f)) { Text("TEEGARDEN LIGHTS", style = tgLabel(13f, TG_EMBER_C)) }
    CenterLabel(sunX, y + 54f, sunMark) { Text("THE SUN LIGHTS", style = tgLabel(13f, c(0xffc24a))) }
    CenterLabel(right - 46f, y - 92f, nowMark) { Text("NOW", style = tgLabel(13f, Color.White)) }

    val s = reveal(t, 5.0f)
    At(90f, 1406f + s.ty, s.opacity) { StatCol("≈ 8", " bn", "years old", 70f, accent, TG_MUTED) }
    At(470f, 1406f + s.ty, s.opacity) { StatCol("4.6", " bn", "the Sun's age", 70f, Color.White, TG_MUTED) }

    Eyebrow("Older than the ground you stand on", accent, 200f, e1)
    Head(headline("Burning since\nbefore ", "the Sun", "."), 82f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.0f), body("Roughly eight billion years alight — and a dwarf this small is ", "barely started", "."))
}

@Composable
fun BoxScope.TgWorlds(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x8fd6a8)
    val cx = 540f; val cy = 1030f

    val appear = reveal(t, 0.5f, dur = 0.9f).opacity
    val ringsA = animate(0f, 1f, 1.0f, 2.6f, Easing.easeOutCubic)(t)

    // Orbits scaled from the real periods: 4.91 d, 11.41 d, 26.13 d.
    val rr = floatArrayOf(150f, 250f, 360f)
    val period = floatArrayOf(4.91f, 11.41f, 26.13f)
    val pops = floatArrayOf(
        animate(0f, 1f, 2.3f, 3.3f, Easing.easeOutBack)(t),
        animate(0f, 1f, 2.9f, 3.9f, Easing.easeOutBack)(t),
        animate(0f, 1f, 3.5f, 4.5f, Easing.easeOutBack)(t),
    )
    val palettes = listOf(TG_TEMPERATE, TG_WARM, TG_COLD)
    val names = listOf("b", "c", "d")
    val sizes = floatArrayOf(44f, 45f, 40f)

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        rr.forEachIndexed { i, r ->
            val on = (ringsA * 3f - i).coerceIn(0f, 1f)
            if (on <= 0f) return@forEachIndexed
            drawCircle(
                c(0x5a6474).copy(alpha = 0.5f * on), radius = r * k, center = ctr,
                style = Stroke(width = 1.8f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f * k, 10f * k))),
            )
        }
        val glow = 1f + 0.04f * sin(t * 1.8f)
        drawCircle(
            brush = Brush.radialGradient(0f to Color(0x60ff6a34), 1f to Color.Transparent, center = ctr, radius = 150f * k * glow),
            radius = 150f * k * glow, center = ctr,
        )
        drawCircle(
            brush = Brush.radialGradient(
                0f to c(0xffd0a8), 0.36f to c(0xff8a52), 1f to c(0x8e2810),
                center = Offset(ctr.x - 12f * k, ctr.y - 12f * k), radius = 60f * k,
            ),
            radius = 48f * k, center = ctr,
        )
    }

    rr.forEachIndexed { i, r ->
        val ang = -TG_TAU * (t / (period[i] * 0.42f)) - 1.1f
        val px = cx + cos(ang) * r
        val py = cy + sin(ang) * r * 0.42f
        At(px - sizes[i] / 2f, py - sizes[i] / 2f, pops[i]) { Sphere(sizes[i], palettes[i]) }
        CenterLabel(px, py + sizes[i] / 2f + 10f, pops[i]) { Text(names[i], style = tgLabel(15f, accent)) }
    }

    val s = reveal(t, 5.0f)
    At(90f, 1412f + s.ty, s.opacity) { StatCol("3", null, "known worlds", 70f, accent, TG_MUTED) }
    At(430f, 1412f + s.ty, s.opacity) { StatCol("1.05", " M⊕", "the heaviest", 70f, Color.White, TG_MUTED) }
    At(772f, 1412f + s.ty, s.opacity) { StatCol("0.82", " M⊕", "the lightest", 70f, Color.White, TG_MUTED) }

    Eyebrow("Three small worlds", accent, 200f, e1)
    Head(headline("Not a gas giant\n", "among them", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.0f), body("Every planet found here weighs about as much as Earth or less — ", "a system of small rocky worlds", "."))
}

@Composable
fun BoxScope.TgEarthlike(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x7fd6a0)

    val appear = reveal(t, 0.6f, dur = 1.0f).opacity
    val popE = animate(0f, 1f, 1.2f, 2.4f, Easing.easeOutBack)(t)
    val popB = animate(0f, 1f, 2.0f, 3.2f, Easing.easeOutBack)(t)
    val meterA = animate(0f, 1f, 3.4f, 5.2f, Easing.easeOutCubic)(t)

    val cy = 930f
    val d = 250f

    At(214f - d / 2f, cy - d / 2f, popE * appear) { Sphere(d, listOf(c(0xbfd8f0), c(0x5f86b4), c(0x1e3350))) }
    At(866f - d / 2f, cy - d / 2f, popB * appear) { Sphere(d, TG_TEMPERATE) }

    CenterLabel(214f, cy + d / 2f + 20f, popE) { Text("EARTH", style = tgLabel(16f, TG_EARTH)) }
    CenterLabel(866f, cy + d / 2f + 20f, popB) { Text("TEEGARDEN b", style = tgLabel(16f, accent)) }

    Canvas(Modifier.fillMaxSize().alpha(meterA)) {
        val k = size.width / 1080f
        val l = 130f * k; val rgt = 950f * k; val y = 1246f * k
        drawLine(c(0x323b48), Offset(l, y), Offset(rgt, y), strokeWidth = 12f * k, cap = StrokeCap.Round)
        val fill = l + (rgt - l) * 0.95f * meterA
        drawLine(
            brush = Brush.horizontalGradient(listOf(c(0x4e8f74), accent), startX = l, endX = rgt),
            start = Offset(l, y), end = Offset(fill, y), strokeWidth = 12f * k, cap = StrokeCap.Round,
        )
        drawCircle(Color.White, radius = 10f * k, center = Offset(fill, y))
    }

    CenterLabel(540f, 1150f, meterA) { Text("EARTH SIMILARITY INDEX", style = tgLabel(15f, TG_MUTED)) }
    CenterLabel(880f, 1278f, meterA) { Text("0.95", style = tgLabel(26f, accent)) }

    val s = reveal(t, 5.2f)
    At(90f, 1400f + s.ty, s.opacity) { StatCol("1.05", " M⊕", "minimum mass", 66f, Color.White, TG_MUTED) }
    At(470f, 1400f + s.ty, s.opacity) { StatCol("0.95", null, "Earth similarity", 66f, accent, TG_MUTED) }

    Eyebrow("Teegarden b", accent, 200f, e1)
    Head(headline("The closest match\nto ", "home", " yet found."), 78f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.2f), body("By the index astronomers use to rank Earth-likeness, ", "nothing else has scored higher", "."))
}

@Composable
fun BoxScope.TgYear(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xffc24a)
    val cy = 1000f

    val appear = reveal(t, 0.5f, dur = 0.9f).opacity
    val mercA = animate(0f, 1f, 1.0f, 2.6f, Easing.easeOutCubic)(t)
    val sysA = animate(0f, 1f, 2.6f, 4.0f, Easing.easeOutCubic)(t)

    // Mercury's orbit as the yardstick: the whole Teegarden system fits inside a quarter of it.
    val mercR = 400f
    val scale = mercR / 0.387f
    val tgR = floatArrayOf(0.0252f, 0.0443f, 0.0894f).map { it * scale }

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(540f * k, cy * k)

        if (mercA > 0.01f) {
            drawCircle(
                c(0x5a6474).copy(alpha = 0.55f * mercA), radius = mercR * k * mercA, center = ctr,
                style = Stroke(width = 2f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * k, 11f * k))),
            )
        }

        tgR.forEachIndexed { i, r ->
            val on = (sysA * 3f - i).coerceIn(0f, 1f)
            if (on <= 0f) return@forEachIndexed
            drawCircle(accent.copy(alpha = 0.62f * on), radius = r * k, center = ctr, style = Stroke(width = 2.2f * k))
        }

        val glow = 1f + 0.05f * sin(t * 2f)
        drawCircle(
            brush = Brush.radialGradient(0f to Color(0x66ff6a34), 1f to Color.Transparent, center = ctr, radius = 86f * k * glow),
            radius = 86f * k * glow, center = ctr,
        )
        drawCircle(
            brush = Brush.radialGradient(0f to c(0xffd0a8), 0.4f to c(0xff8a52), 1f to c(0x8e2810), center = ctr, radius = 34f * k),
            radius = 26f * k, center = ctr,
        )

        // The innermost world, whipping round once every 4.9 days.
        if (sysA > 0.3f) {
            val ang = -TG_TAU * (t / 1.9f)
            val p = Offset(ctr.x + cos(ang) * tgR[0] * k, ctr.y + sin(ang) * tgR[0] * k)
            drawCircle(c(0xbfe0cc), radius = 11f * k, center = p)
        }
    }

    CenterLabel(540f, cy - mercR - 46f, mercA) { Text("MERCURY'S ORBIT", style = tgLabel(15f, TG_FAINT)) }
    CenterLabel(540f, cy + tgR[2] + 22f, sysA) { Text("THE WHOLE SYSTEM", style = tgLabel(15f, accent)) }

    val s = reveal(t, 5.0f)
    At(90f, 1420f + s.ty, s.opacity) { StatCol("4.9", " d", "innermost year", 68f, accent, TG_MUTED) }
    At(430f, 1420f + s.ty, s.opacity) { StatCol("26", " d", "outermost year", 68f, Color.White, TG_MUTED) }
    At(772f, 1420f + s.ty, s.opacity) { StatCol("0.09", " au", "widest orbit", 68f, Color.White, TG_MUTED) }

    Eyebrow("A year in under five days", accent, 200f, e1)
    Head(headline("Three worlds inside\n", "Mercury's", " orbit."), 76f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.0f), body("Around a star this cool, ", "hugging it is the only way to stay warm", "."))
}

@Composable
fun BoxScope.TgSeen(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = TG_EARTH

    val appear = reveal(t, 0.5f, dur = 1.0f).opacity
    val dipA = animate(0f, 1f, 2.6f, 4.0f, Easing.easeOutCubic)(t)
    val cy = 900f
    val sunR = 190f

    // Earth crosses our own Sun, seen from twelve light-years away.
    val cross = ((t - 2.2f) / 5.6f).coerceIn(0f, 1f)
    val ex = 540f - sunR * 1.5f + cross * sunR * 3f

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(540f * k, cy * k)
        val r = sunR * k

        drawCircle(
            brush = Brush.radialGradient(0f to Color(0x40ffc24a), 1f to Color.Transparent, center = ctr, radius = r * 1.9f),
            radius = r * 1.9f, center = ctr,
        )
        drawCircle(
            brush = Brush.radialGradient(
                0f to c(0xfff8e0), 0.5f to c(0xffd267), 1f to c(0xe09a28),
                center = Offset(ctr.x - r * 0.2f, ctr.y - r * 0.2f), radius = r * 1.2f,
            ),
            radius = r, center = ctr,
        )

        if (cross > 0f && cross < 1f) {
            drawCircle(c(0x0a0a0e), radius = 13f * k, center = Offset(ex * k, cy * k))
        }
    }

    // The light-curve an observer there would record.
    Canvas(Modifier.fillMaxSize().alpha(dipA)) {
        val k = size.width / 1080f
        val l = 140f * k; val rgt = 940f * k; val y = 1280f * k
        drawLine(c(0x3c4658), Offset(l, y + 30f * k), Offset(rgt, y + 30f * k), strokeWidth = 2f * k)

        val path = androidx.compose.ui.graphics.Path()
        var first = true
        var x = 0f
        while (x <= 1f) {
            val depth = if (x > 0.34f && x < 0.66f) 1f else 0f
            val px = l + (rgt - l) * x
            val py = y + depth * 26f * k
            if (first) { path.moveTo(px, py); first = false } else path.lineTo(px, py)
            x += 0.01f
        }
        drawPath(path, color = accent.copy(alpha = 0.9f), style = Stroke(width = 3f * k, cap = StrokeCap.Round))
    }

    CenterLabel(540f, cy + sunR + 40f, appear) { Text("OUR SUN, FROM THERE", style = tgLabel(15f, c(0xffc24a))) }
    CenterLabel(540f, 1340f, dipA) { Text("EARTH, CROSSING", style = tgLabel(15f, accent)) }

    val s = reveal(t, 5.4f)
    At(90f, 1420f + s.ty, s.opacity) { StatCol("2.7", " mag", "our Sun in their sky", 62f, Color.White, TG_MUTED) }
    At(500f, 1420f + s.ty, s.opacity) { StatCol("0.008", "%", "the dip Earth makes", 62f, accent, TG_MUTED) }

    Eyebrow("The view back", accent, 200f, e1)
    Head(headline("From there, Earth\ncrosses ", "the Sun", "."), 80f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.2f), body("Teegarden lies in the narrow band of sky that sees Earth transit — ", "they could find us the way we found them", "."))
}

@Composable
fun BoxScope.TgDistance(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xcfe0ff)
    Starfield(t)

    val travel = animate(0f, 1f, 1.2f, 6.0f, Easing.easeInOutCubic)(t)
    val appear = reveal(t, 0.6f, dur = 0.9f).opacity
    val y = 1020f
    val left = 150f; val right = 930f

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val yk = y * k

        drawLine(
            c(0x39414f), Offset(left * k, yk), Offset(right * k, yk),
            strokeWidth = 2.4f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f * k, 13f * k)),
        )
        drawLine(
            accent.copy(alpha = 0.8f), Offset(left * k, yk),
            Offset((left + (right - left) * travel) * k, yk), strokeWidth = 4f * k, cap = StrokeCap.Round,
        )

        drawCircle(c(0xffd267), radius = 15f * k, center = Offset(left * k, yk))

        val tw = (0.6f + 0.4f * sin(t * 2.4f)).coerceIn(0f, 1f)
        drawCircle(Color(0x50ff6a34), radius = 30f * k, center = Offset(right * k, yk))
        drawCircle(TG_EMBER_C.copy(alpha = tw), radius = 10f * k, center = Offset(right * k, yk))

        if (travel > 0.02f && travel < 0.99f) {
            val px = (left + (right - left) * travel) * k
            drawCircle(Color.White, radius = 5.5f * k, center = Offset(px, yk))
        }
    }

    CenterLabel(left, y + 48f, appear) { Text("THE SUN", style = tgLabel(14f, c(0xffc24a))) }
    CenterLabel(right, y + 48f, appear) { Text("TEEGARDEN", style = tgLabel(14f, TG_EMBER_C)) }
    CenterLabel(540f, y - 78f, animate(0f, 1f, 2.0f, 3.0f, Easing.easeOutCubic)(t)) {
        Text("12.5 LIGHT-YEARS", style = tgLabel(20f, accent))
    }

    val s = reveal(t, 5.2f)
    At(90f, 1412f + s.ty, s.opacity) { StatCol("12.5", " ly", "distance", 70f, Color.White, TG_MUTED) }
    At(430f, 1412f + s.ty, s.opacity) { StatCol("3.83", " pc", "in parsecs", 70f, accent, TG_MUTED) }
    At(772f, 1412f + s.ty, s.opacity) { StatCol("24th", null, "nearest system", 70f, Color.White, TG_MUTED) }

    Eyebrow("Twelve and a half light-years", accent, 200f, e1)
    Head(headline("Close enough to\nbe ", "a neighbour", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.2f), body("Light leaving this page today arrives there ", "twelve and a half years from now", "."))
}
