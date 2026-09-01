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
import java.util.Locale
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val BR_TAU = 6.2831855f
private const val BR_RAD = 0.017453292f

private val BR_RED = c(0xf4794a)
private val BR_EMBER = c(0xff8a52)
private val BR_MUTED = c(0x9a8878)
private val BR_FAINT = c(0x7f8898)

private val BR_STAR_STOPS = arrayOf(
    0f to c(0xffe4c4), 0.4f to c(0xff9a5a), 0.78f to c(0xd85a28), 1f to c(0x8e3212),
)
private val BR_HALO_STOPS = arrayOf(
    0f to Color(0x55ff7a3a), 0.5f to Color(0x18e0602a), 0.85f to Color(0x00000000), 1f to Color(0x00000000),
)
private val BR_SUN_STOPS = arrayOf(
    0f to c(0xfff8e0), 0.45f to c(0xffd267), 0.8f to c(0xf0a52e), 1f to c(0xc57a1e),
)
private val BR_SUN_HALO = arrayOf(
    0f to Color(0x44ffc24a), 0.5f to Color(0x12ffa838), 0.85f to Color(0x00000000), 1f to Color(0x00000000),
)

private val BR_ROCK = listOf(c(0xe8b894), c(0xb8734a), c(0x5a2c18))
private val BR_ASH = listOf(c(0xd8c4b0), c(0x9a7a62), c(0x443024))
private val BR_IRON = listOf(c(0xe0a08a), c(0xa85a42), c(0x4e2418))
private val BR_PALE = listOf(c(0xe4d0c0), c(0xa8907e), c(0x4a3a30))

private fun brLabel(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = size.sp,
    letterSpacing = 0.14.em, color = color,
)

private fun brBody(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = size.sp, color = color,
)

private fun brRandoms(seed: Int, count: Int, each: Int): List<FloatArray> {
    var s = seed
    fun rnd(): Float {
        s = s * 1664525 + 1013904223
        return ((s ushr 9) and 0xFFFF) / 65535f
    }
    return List(count) { FloatArray(each) { rnd() } }
}

// ───────────────────────── the star ─────────────────────────

private class BrWorld(val label: String, val size: Float, val colors: List<Color>)

private val BR_WORLDS = listOf(
    BrWorld("BARNARD b", 34f, BR_ROCK),
    BrWorld("BARNARD c", 32f, BR_ASH),
    BrWorld("BARNARD d", 26f, BR_IRON),
    BrWorld("BARNARD e", 29f, BR_PALE),
)

@Composable
fun BoxScope.BrIntro(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    Starfield(t)

    val rise = animate(0.05f, 1f, 0.5f, 2.2f, Easing.easeOutCubic)(t)
    val breathe = 1f + 0.025f * sin(t * 1.1f)
    val starR = 152f * rise * breathe
    val cy = 860f

    RadialDisc(540f, cy, starR + 200f, BR_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(540f, cy, starR, BR_STAR_STOPS, 0.5f, 0.42f, 0.6f)
    CenterLabel(540f, cy + starR + 34f, reveal(t, 2.2f).opacity) {
        Text("BARNARD'S STAR", style = brLabel(19f, BR_RED))
    }

    val rowY = 1330f
    BR_WORLDS.forEachIndexed { i, w ->
        val a = reveal(t, 2.7f + i * 0.28f, dur = 0.7f)
        val x = 258f + i * 188f
        Sphere(
            w.size, w.colors,
            modifier = Modifier
                .offset((x - w.size / 2f).dp, (rowY - w.size / 2f + a.ty).dp)
                .alpha(a.opacity),
        )
        CenterLabel(x, rowY + 52f, a.opacity) { Text(w.label, style = brLabel(13f, BR_MUTED)) }
    }

    Eyebrow("5.96 light-years away", BR_RED, 200f, e1)
    Head(headline("The nearest\nstar ", "on its own", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Only Alpha Centauri sits closer — and that is ", "three stars, not one", "."))
}

@Composable
fun BoxScope.BrDwarf(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xe8703c)
    val cy = 1000f

    val grow = animate(0.08f, 1f, 0.5f, 2.4f, Easing.easeOutCubic)(t)
    val sunR = 300f * grow
    val pop = animate(0f, 1f, 1.6f, 3.0f, Easing.easeOutBack)(t)
    val brR = 300f * 0.19f * pop
    val sunCx = 300f
    val brCx = 812f

    RadialDisc(sunCx, cy, sunR + 130f, BR_SUN_HALO, 0.5f, 0.5f, 0.5f)
    RadialDisc(sunCx, cy, sunR, BR_SUN_STOPS, 0.5f, 0.42f, 0.6f)
    RadialDisc(brCx, cy, brR * 2.8f, BR_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(brCx, cy, brR, BR_STAR_STOPS, 0.5f, 0.42f, 0.6f)

    Canvas(Modifier.fillMaxSize().alpha(animate(0f, 1f, 2.8f, 3.6f, Easing.easeOutCubic)(t))) {
        val k = size.width / 1080f
        val gy = (cy + 300f + 64f) * k
        drawLine(
            Color.White.copy(alpha = 0.18f),
            Offset((sunCx - sunR) * k, gy), Offset((sunCx + sunR) * k, gy),
            strokeWidth = 2f * k,
        )
        drawLine(
            accent.copy(alpha = 0.7f),
            Offset((brCx - brR) * k, gy), Offset((brCx + brR) * k, gy),
            strokeWidth = 4f * k, cap = StrokeCap.Round,
        )
    }

    CenterLabel(sunCx, cy + 300f + 84f, animate(0f, 1f, 2.6f, 3.4f, Easing.easeOutCubic)(t)) {
        Text("THE SUN", style = brLabel(17f, c(0xffd267)))
    }
    CenterLabel(brCx, cy + 300f + 84f, animate(0f, 1f, 3.0f, 3.8f, Easing.easeOutCubic)(t)) {
        Text("BARNARD'S STAR", style = brLabel(17f, accent))
    }

    val s = reveal(t, 4.0f)
    At(90f, 1450f + s.ty, s.opacity) { StatCol("0.16", "×", "the Sun's mass", 72f, Color.White, BR_MUTED) }
    At(430f, 1450f + s.ty, s.opacity) { StatCol("0.19", "×", "the Sun's width", 72f, Color.White, BR_MUTED) }
    At(772f, 1450f + s.ty, s.opacity) { StatCol("1/300", null, "of its light", 72f, accent, BR_MUTED) }

    Eyebrow("Barely a star at all", accent, 200f, e1)
    Head(headline("A ", "sixth", " of\na Sun."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Any smaller and it could not ", "burn hydrogen", " at all."))
}

@Composable
fun BoxScope.BrDim(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xb9a2ff)
    Starfield(t)

    val cx = 540f; val cy = 1010f; val r = 286f
    val ring = animate(0f, 1f, 0.9f, 2.4f, Easing.easeInOutCubic)(t)
    val inner = animate(0f, 1f, 2.5f, 3.7f, Easing.easeOutCubic)(t)

    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        val rr = r * k

        if (ring > 0.01f) {
            drawArc(
                color = accent.copy(alpha = 0.55f),
                startAngle = -90f, sweepAngle = 360f * ring, useCenter = false,
                topLeft = Offset(ctr.x - rr, ctr.y - rr), size = Size(rr * 2f, rr * 2f),
                style = Stroke(width = 3f * k),
            )
            drawCircle(accent.copy(alpha = 0.05f * ring), radius = rr, center = ctr)
        }

        if (inner > 0.01f) {
            for (i in 0..3) {
                val ang = (-135f + i * 90f) * BR_RAD
                val a = 0.28f * inner
                drawLine(
                    accent.copy(alpha = a),
                    Offset(ctr.x + cos(ang) * rr * 0.82f, ctr.y + sin(ang) * rr * 0.82f),
                    Offset(ctr.x + cos(ang) * rr, ctr.y + sin(ang) * rr),
                    strokeWidth = 2f * k,
                )
            }
            val tw = 0.66f + 0.34f * sin(t * 2.2f)
            drawCircle(Color(0x44ff7a3a).copy(alpha = 0.4f * inner), radius = 52f * k * inner, center = ctr)
            drawCircle(BR_EMBER.copy(alpha = (inner * tw).coerceIn(0f, 1f)), radius = 12f * k * inner, center = ctr)
        }
    }

    CenterLabel(cx, cy + 62f, inner) { Text("MAGNITUDE 9.5", style = brLabel(16f, BR_EMBER)) }
    CenterLabel(cx, cy - r - 68f, animate(0f, 1f, 2.0f, 2.9f, Easing.easeOutCubic)(t)) {
        Text("THROUGH BINOCULARS", style = brLabel(14f, accent))
    }
    CenterLabel(cx, cy + r + 44f, animate(0f, 1f, 3.9f, 4.7f, Easing.easeOutCubic)(t)) {
        Text("nothing at all to the naked eye", style = brBody(24f, BR_FAINT))
    }

    val s = reveal(t, 4.4f)
    At(90f, 1430f + s.ty, s.opacity) { StatCol("9.5", null, "how bright it looks", 78f, Color.White, BR_MUTED) }
    At(600f, 1430f + s.ty, s.opacity) { StatCol("6.5", null, "faintest the eye can see", 78f, Color.White, BR_MUTED) }

    Eyebrow("Second-closest star", accent, 200f, e1)
    Head(headline("Too faint\nto ", "ever see", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.5f), body("Nobody picked it out of the sky until ", "1916", "."))
}

private class BrEpoch(val gya: Float, val label: String, val sub: String, val color: Color, val below: Boolean)

private val BR_EPOCHS = listOf(
    BrEpoch(13.8f, "BIG BANG", "13.8 bn yrs ago", c(0x9a86e0), false),
    BrEpoch(10f, "BARNARD'S STAR", "≈ 10 bn yrs ago", c(0xffc98a), true),
    BrEpoch(4.6f, "THE SUN", "4.6 bn yrs ago", c(0xffd267), false),
    BrEpoch(0f, "TODAY", "", c(0x7fc8e8), true),
)

@Composable
fun BoxScope.BrAncient(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xffc98a)
    Starfield(t)

    val axisY = 1080f; val x0 = 150f; val x1 = 950f
    fun px(gya: Float) = x0 + (13.8f - gya) / 13.8f * (x1 - x0)
    val draw = animate(0f, 1f, 0.9f, 3.4f, Easing.easeInOutCubic)(t)
    val edge = x0 + (x1 - x0) * draw

    Canvas(Modifier.fillMaxSize().alpha(reveal(t, 0.6f, dur = 0.8f).opacity)) {
        val k = size.width / 1080f
        val y = axisY * k

        drawLine(Color.White.copy(alpha = 0.10f), Offset(x0 * k, y), Offset(x1 * k, y), strokeWidth = 18f * k, cap = StrokeCap.Round)
        drawLine(
            brush = Brush.horizontalGradient(
                0f to c(0x5a4a8e), 0.28f to c(0xc07a48), 0.66f to c(0xe0a860), 1f to c(0x7fc8e8),
                startX = x0 * k, endX = x1 * k,
            ),
            start = Offset(x0 * k, y), end = Offset(edge * k, y),
            strokeWidth = 18f * k, cap = StrokeCap.Round,
        )

        BR_EPOCHS.forEach { ep ->
            val sx = px(ep.gya)
            val on = ((edge - sx) / 50f).coerceIn(0f, 1f)
            if (on <= 0f) return@forEach
            val stem = if (ep.below) 46f else -46f
            drawLine(
                ep.color.copy(alpha = 0.75f * on),
                Offset(sx * k, y), Offset(sx * k, (axisY + stem) * k),
                strokeWidth = 2f * k,
            )
            drawCircle(ep.color.copy(alpha = 0.3f * on), radius = 22f * k, center = Offset(sx * k, y))
            drawCircle(ep.color.copy(alpha = on), radius = 9f * k, center = Offset(sx * k, y))
        }

        val ma = animate(0f, 1f, 3.4f, 4.2f, Easing.easeOutBack)(t)
        if (ma > 0.01f) {
            val pulse = 0.55f + 0.45f * sin(t * 2.3f)
            drawCircle(
                accent.copy(alpha = (0.85f * ma * pulse).coerceIn(0f, 1f)),
                radius = 34f * k * ma, center = Offset(px(10f) * k, y), style = Stroke(width = 3f * k),
            )
        }
    }

    BR_EPOCHS.forEach { ep ->
        val sx = px(ep.gya)
        val la = ((edge - sx) / 60f).coerceIn(0f, 1f)
        val ly = if (ep.below) axisY + 62f else axisY - 128f
        CenterLabel(sx, ly, la) {
            Text(ep.label, style = brLabel(15f, ep.color))
            if (ep.sub.isNotEmpty()) Text(ep.sub, style = brBody(21f, BR_FAINT))
        }
    }

    val s = reveal(t, 4.4f)
    At(90f, 1400f + s.ty, s.opacity) { StatCol("10", " bn", "years old, about", 82f, accent, BR_MUTED) }
    At(600f, 1400f + s.ty, s.opacity) { StatCol("2×", null, "the age of the Sun", 82f, Color.White, BR_MUTED) }

    Eyebrow("A relic of the young galaxy", accent, 200f, e1)
    Head(headline("Older than\n", "the Sun", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.8f), body("It was already ancient when Earth was still ", "a disc of dust", "."))
}

private fun brSpike(u: Float): Float = when {
    u < 0.50f -> 0f
    u < 0.545f -> (u - 0.50f) / 0.045f
    else -> exp(-(u - 0.545f) * 8.5f)
}

@Composable
fun BoxScope.BrFlare(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xffd15a)
    Starfield(t)

    val x0 = 120f; val x1 = 960f
    val base = 1290f; val peak = 880f
    val draw = animate(0f, 1f, 0.9f, 4.4f, Easing.linear)(t)
    val burst = brSpike(draw)

    val starCy = 700f
    RadialDisc(540f, starCy, 66f + 190f * burst, BR_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(540f, starCy, 62f + 26f * burst, BR_STAR_STOPS, 0.5f, 0.42f, 0.6f)

    Canvas(Modifier.fillMaxSize().alpha(reveal(t, 0.6f, dur = 0.8f).opacity)) {
        val k = size.width / 1080f

        drawLine(
            Color.White.copy(alpha = 0.16f),
            Offset(x0 * k, base * k), Offset(x1 * k, base * k),
            strokeWidth = 2f * k,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f * k, 12f * k)),
        )
        drawLine(
            accent.copy(alpha = 0.22f),
            Offset(x0 * k, peak * k), Offset(x1 * k, peak * k),
            strokeWidth = 2f * k,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f * k, 12f * k)),
        )

        fun cy(u: Float): Float {
            val noise = sin(u * 51f) * 4.5f + sin(u * 23f + 1.3f) * 3f
            return base + noise - (base - peak) * brSpike(u)
        }

        val p = Path()
        var u = 0f
        var first = true
        while (u <= draw) {
            val gx = (x0 + (x1 - x0) * u) * k
            val gy = cy(u) * k
            if (first) { p.moveTo(gx, gy); first = false } else p.lineTo(gx, gy)
            u += 0.004f
        }
        if (!first) {
            drawPath(p, accent.copy(alpha = 0.28f), style = Stroke(width = 12f * k, cap = StrokeCap.Round))
            drawPath(p, accent, style = Stroke(width = 3.4f * k, cap = StrokeCap.Round))
        }

        val hx = (x0 + (x1 - x0) * draw) * k
        val hy = cy(draw) * k
        drawCircle(accent.copy(alpha = 0.30f), radius = 24f * k, center = Offset(hx, hy))
        drawCircle(Color.White, radius = 7f * k, center = Offset(hx, hy))
    }

    At(x0, peak - 58f, animate(0f, 1f, 2.6f, 3.4f, Easing.easeOutCubic)(t)) {
        Text("TWICE AS BRIGHT", style = brLabel(15f, accent))
    }
    At(x0, base + 24f, animate(0f, 1f, 1.4f, 2.2f, Easing.easeOutCubic)(t)) {
        Text("its usual glow", style = brBody(22f, BR_FAINT))
    }

    val s = reveal(t, 4.6f)
    At(90f, 1410f + s.ty, s.opacity) { StatCol("1998", null, "when we caught one", 78f, Color.White, BR_MUTED) }
    At(600f, 1410f + s.ty, s.opacity) { StatCol("×2", null, "brightness, for minutes", 78f, accent, BR_MUTED) }

    Eyebrow("Not as quiet as it looks", accent, 200f, e1)
    Head(headline("The old star\n", "still erupts", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.8f), body("Hard luck for any nearby world trying to hold on to ", "an atmosphere", "."))
}

// ───────────────────────── the runaway ─────────────────────────

@Composable
fun BoxScope.BrRunaway(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = BR_EMBER
    Starfield(t)

    val sx = 208f; val sy = 1350f
    val ex = 880f; val ey = 820f
    val prog = animate(0f, 1f, 1.0f, 4.6f, Easing.easeInOutCubic)(t)
    val year = interpolate(listOf(1.0f, 4.6f), listOf(1916f, 2026f), Easing.easeInOutCubic)(t)
    val appear = reveal(t, 0.6f, dur = 0.9f).opacity

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f

        drawLine(
            accent.copy(alpha = 0.20f),
            Offset(sx * k, sy * k), Offset(ex * k, ey * k),
            strokeWidth = 2f * k,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f * k, 13f * k)),
        )

        for (i in 0..11) {
            val u = i / 11f
            if (u > prog) break
            val gx = (sx + (ex - sx) * u) * k
            val gy = (sy + (ey - sy) * u) * k
            drawCircle(accent.copy(alpha = 0.16f + 0.16f * u), radius = 8f * k, center = Offset(gx, gy))
        }

        val hx = (sx + (ex - sx) * prog) * k
        val hy = (sy + (ey - sy) * prog) * k
        val pulse = (0.6f + 0.4f * sin(t * 2.4f)).coerceIn(0f, 1f)
        drawCircle(Color(0x55ff7a3a), radius = 42f * k, center = Offset(hx, hy))
        drawCircle(c(0xffa066).copy(alpha = pulse), radius = 15f * k, center = Offset(hx, hy))
    }

    val hxu = sx + (ex - sx) * prog
    val hyu = sy + (ey - sy) * prog
    CenterLabel(hxu + 138f, hyu - 26f, appear) {
        Text(year.roundToInt().toString(), style = brLabel(28f, Color.White))
    }
    CenterLabel(sx, sy + 54f, animate(0f, 1f, 1.4f, 2.2f, Easing.easeOutCubic)(t)) {
        Text("1916", style = brLabel(14f, BR_FAINT))
    }
    At(600f, 1418f, animate(0f, 1f, 4.4f, 5.2f, Easing.easeOutCubic)(t)) {
        Text("every other star holds still", style = brBody(24f, BR_FAINT))
    }

    val s = reveal(t, 4.0f)
    At(90f, 1470f + s.ty, s.opacity) { StatCol("10.4", "″/yr", "across our sky", 80f, accent, BR_MUTED) }
    At(600f, 1470f + s.ty, s.opacity) { StatCol("1st", null, "of every star known", 80f, Color.White, BR_MUTED) }

    Eyebrow("Barnard's Runaway Star", accent, 200f, e1)
    Head(headline("The fastest\nstar in ", "our sky", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.8f), body("No other star shifts against the background ", "anywhere near as fast", "."))
}

private val BR_CRATERS = brRandoms(0x2F71C3, 15, 3)

@Composable
fun BoxScope.BrMoon(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xd8c4a8)
    Starfield(t)

    val cx = 540f; val cy = 1030f; val r = 282f
    val appear = reveal(t, 0.5f, dur = 1.0f).opacity
    val grow = animate(0.2f, 1f, 0.5f, 2.2f, Easing.easeOutCubic)(t)
    val walk = animate(0f, 0.635f, 2.2f, 4.8f, Easing.easeInOutCubic)(t)

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        val rr = r * grow * k
        if (rr <= 0f) return@Canvas
        val lit = Offset(ctr.x - rr * 0.3f, ctr.y - rr * 0.3f)

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(0f to c(0xe8e2d6), 0.55f to c(0xa8a094), 1f to c(0x4a4640)),
                center = lit, radius = rr * 1.7f,
            ),
            radius = rr, center = ctr,
        )

        val disc = Path().apply { addOval(Rect(ctr.x - rr, ctr.y - rr, ctr.x + rr, ctr.y + rr)) }
        clipPath(disc) {
            BR_CRATERS.forEach { v ->
                val ang = v[0] * BR_TAU
                val rad = sqrt(v[1]) * 0.84f * rr
                val cr = (10f + v[2] * 34f) * k * grow
                val p = Offset(ctr.x + cos(ang) * rad, ctr.y + sin(ang) * rad)
                drawCircle(Color.Black.copy(alpha = 0.16f), radius = cr, center = p)
                drawCircle(Color.White.copy(alpha = 0.06f), radius = cr * 0.72f, center = Offset(p.x - cr * 0.2f, p.y - cr * 0.2f))
            }
        }

        val ta = animate(0f, 1f, 1.8f, 2.6f, Easing.easeOutCubic)(t)
        if (ta > 0.01f) {
            val ty = ctr.y + rr * 0.1f
            drawLine(
                BR_EMBER.copy(alpha = 0.35f * ta),
                Offset(ctr.x - rr, ty), Offset(ctr.x + rr, ty),
                strokeWidth = 2f * k,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * k, 11f * k)),
            )
            for (i in 0..3) {
                val u = i / 3f
                val mx = ctr.x - rr + rr * 2f * u
                drawLine(
                    BR_EMBER.copy(alpha = 0.30f * ta),
                    Offset(mx, ty - 15f * k), Offset(mx, ty + 15f * k), strokeWidth = 2f * k,
                )
            }
            if (walk > 0.001f) {
                val hx = ctr.x - rr + rr * 2f * walk
                drawLine(BR_EMBER.copy(alpha = 0.85f), Offset(ctr.x - rr, ty), Offset(hx, ty), strokeWidth = 4f * k, cap = StrokeCap.Round)
                drawCircle(Color(0x66ff7a3a), radius = 30f * k, center = Offset(hx, ty))
                drawCircle(c(0xffa066), radius = 12f * k, center = Offset(hx, ty))
            }
        }
    }

    CenterLabel(cx, cy - r - 74f, animate(0f, 1f, 2.4f, 3.2f, Easing.easeOutCubic)(t)) {
        Text("THE FULL MOON · 0.5°", style = brLabel(15f, accent))
    }
    CenterLabel(cx - r + r * 2f * walk, cy + r * 0.1f + 44f, animate(0f, 1f, 4.4f, 5.2f, Easing.easeOutCubic)(t)) {
        Text("TODAY", style = brLabel(14f, BR_EMBER))
    }

    val s = reveal(t, 4.6f)
    At(90f, 1440f + s.ty, s.opacity) { StatCol("180", " yrs", "to cross that disc", 80f, Color.White, BR_MUTED) }
    At(628f, 1440f + s.ty, s.opacity) { StatCol("2/3", null, "of the way since 1916", 80f, BR_EMBER, BR_MUTED) }

    Eyebrow("Fast for a star", accent, 200f, e1)
    Head(headline("A Moon-width\nin ", "a lifetime", "."), 82f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.8f), body("It started this crossing before ", "anyone could photograph it", "."))
}

@Composable
fun BoxScope.BrApproach(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x7fc8e8)
    Starfield(t)

    val x0 = 120f; val x1 = 960f
    val yrMax = 20000f
    fun px(yr: Float) = x0 + yr / yrMax * (x1 - x0)
    fun ly(d: Float) = 860f + (d - 3f) / 5f * 420f
    fun dist(yr: Float): Float {
        val v = (yr - 11800f) * 3.926e-4f
        return sqrt(3.75f * 3.75f + v * v)
    }

    val draw = animate(0f, 1f, 1.0f, 4.2f, Easing.easeInOutCubic)(t)
    val appear = reveal(t, 0.6f, dur = 0.9f).opacity

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f

        drawLine(Color.White.copy(alpha = 0.10f), Offset(x0 * k, ly(8f) * k), Offset(x1 * k, ly(8f) * k), strokeWidth = 2f * k)
        drawLine(
            accent.copy(alpha = 0.18f),
            Offset(x0 * k, ly(3.75f) * k), Offset(x1 * k, ly(3.75f) * k),
            strokeWidth = 2f * k,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f * k, 12f * k)),
        )

        val p = Path()
        var yr = 0f
        var first = true
        val endYr = yrMax * draw
        while (yr <= endYr) {
            val gx = px(yr) * k
            val gy = ly(dist(yr)) * k
            if (first) { p.moveTo(gx, gy); first = false } else p.lineTo(gx, gy)
            yr += 160f
        }
        if (!first) {
            drawPath(p, accent.copy(alpha = 0.26f), style = Stroke(width = 13f * k, cap = StrokeCap.Round))
            drawPath(p, accent, style = Stroke(width = 3.6f * k, cap = StrokeCap.Round))
        }

        drawCircle(Color.White.copy(alpha = 0.9f), radius = 10f * k, center = Offset(px(0f) * k, ly(dist(0f)) * k))

        val ma = animate(0f, 1f, 3.2f, 4.0f, Easing.easeOutBack)(t)
        if (ma > 0.01f) {
            val mp = Offset(px(11800f) * k, ly(3.75f) * k)
            val pulse = (0.55f + 0.45f * sin(t * 2.4f)).coerceIn(0f, 1f)
            drawCircle(BR_EMBER.copy(alpha = 0.85f * ma * pulse), radius = 34f * k * ma, center = mp, style = Stroke(width = 3f * k))
            drawCircle(c(0xffa066), radius = 14f * k * ma, center = mp)
        }
    }

    CenterLabel(px(0f) + 118f, ly(dist(0f)) - 96f, animate(0f, 1f, 1.4f, 2.2f, Easing.easeOutCubic)(t)) {
        Text("TODAY · 5.96 ly", style = brLabel(15f, Color.White))
    }
    CenterLabel(px(11800f), ly(3.75f) + 46f, animate(0f, 1f, 3.8f, 4.6f, Easing.easeOutCubic)(t)) {
        Text("≈ 11,800 CE · 3.75 ly", style = brLabel(16f, BR_EMBER))
    }

    val s = reveal(t, 4.6f)
    At(90f, 1420f + s.ty, s.opacity) { StatCol("110", " km/s", "closing on us", 78f, accent, BR_MUTED) }
    At(628f, 1420f + s.ty, s.opacity) { StatCol("3.75", " ly", "at its nearest", 78f, Color.White, BR_MUTED) }

    Eyebrow("On its way in", accent, 200f, e1)
    Head(headline("It is coming\n", "closer", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.8f), body("For a while it will be nearer than ", "Proxima Centauri", " is today."))
}

// ───────────────────────── the planets ─────────────────────────

private val BR_PLATES = brRandoms(0x7C33A1, 13, 2)

@Composable
fun BoxScope.BrPhantom(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x9aa8c4)
    val ghost = c(0xe0a870)
    Starfield(t)

    val x0 = 130f; val x1 = 950f; val axisY = 1040f
    val drawWob = animate(0f, 1f, 0.9f, 2.8f, Easing.easeInOutCubic)(t)
    val fade = animate(1f, 0.20f, 3.2f, 4.0f, Easing.easeOutCubic)(t)
    val drawFlat = animate(0f, 1f, 3.6f, 4.8f, Easing.easeInOutCubic)(t)
    val appear = reveal(t, 0.6f, dur = 0.8f).opacity

    fun wob(u: Float) = axisY - sin(u * BR_TAU * 1.6f) * 66f

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f

        val p = Path()
        var u = 0f
        var first = true
        while (u <= drawWob) {
            val gx = (x0 + (x1 - x0) * u) * k
            val gy = wob(u) * k
            if (first) { p.moveTo(gx, gy); first = false } else p.lineTo(gx, gy)
            u += 0.006f
        }
        if (!first) drawPath(p, ghost.copy(alpha = fade), style = Stroke(width = 3.2f * k, cap = StrokeCap.Round))

        BR_PLATES.forEachIndexed { i, v ->
            val du = i / (BR_PLATES.size - 1f)
            if (du > drawWob) return@forEachIndexed
            val gx = (x0 + (x1 - x0) * du) * k
            val gy = (wob(du) + (v[0] - 0.5f) * 26f) * k
            val err = (10f + v[1] * 16f) * k
            drawLine(ghost.copy(alpha = 0.45f * fade), Offset(gx, gy - err), Offset(gx, gy + err), strokeWidth = 2f * k)
            drawCircle(ghost.copy(alpha = fade), radius = 6f * k, center = Offset(gx, gy))
        }

        if (drawFlat > 0.01f) {
            drawLine(
                Color.White.copy(alpha = 0.85f),
                Offset(x0 * k, axisY * k), Offset((x0 + (x1 - x0) * drawFlat) * k, axisY * k),
                strokeWidth = 3.4f * k, cap = StrokeCap.Round,
            )
        }
    }

    At(x0, axisY - 178f, animate(0f, 1f, 2.2f, 3.0f, Easing.easeOutCubic)(t)) {
        Text("THE WOBBLE VAN DE KAMP SAW", style = brLabel(15f, ghost))
    }
    At(x0, axisY + 40f, animate(0f, 1f, 4.4f, 5.2f, Easing.easeOutCubic)(t)) {
        Text("his telescope, not a planet", style = brBody(25f, Color.White))
    }

    val s = reveal(t, 5.0f)
    At(90f, 1400f + s.ty, s.opacity) { StatCol("1963", null, "the claim", 78f, ghost, BR_MUTED) }
    At(600f, 1400f + s.ty, s.opacity) { StatCol("1973", null, "the retraction", 78f, accent, BR_MUTED) }

    Eyebrow("Sixty years of false starts", accent, 200f, e1)
    Head(headline("The planets\nthat ", "were not", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.0f), body("The wobble tracked adjustments made to ", "his own telescope", "."))
}

private val BR_RV = brRandoms(0x51AA37, 24, 2)

@Composable
fun BoxScope.BrFound(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x8fe0b4)
    Starfield(t)

    val x0 = 130f; val x1 = 950f; val axisY = 1030f; val amp = 88f
    val draw = animate(0f, 1f, 0.9f, 2.8f, Easing.easeInOutCubic)(t)
    val dots = animate(0f, 1f, 2.6f, 4.2f, Easing.linear)(t)
    val appear = reveal(t, 0.6f, dur = 0.8f).opacity

    fun rv(u: Float) = axisY - sin(u * BR_TAU) * amp

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f

        drawLine(Color.White.copy(alpha = 0.10f), Offset(x0 * k, axisY * k), Offset(x1 * k, axisY * k), strokeWidth = 2f * k)

        val p = Path()
        var u = 0f
        var first = true
        while (u <= draw) {
            val gx = (x0 + (x1 - x0) * u) * k
            val gy = rv(u) * k
            if (first) { p.moveTo(gx, gy); first = false } else p.lineTo(gx, gy)
            u += 0.005f
        }
        if (!first) {
            drawPath(p, accent.copy(alpha = 0.24f), style = Stroke(width = 12f * k, cap = StrokeCap.Round))
            drawPath(p, accent, style = Stroke(width = 3.2f * k, cap = StrokeCap.Round))
        }

        BR_RV.forEachIndexed { i, v ->
            if (i / BR_RV.size.toFloat() > dots) return@forEachIndexed
            val gx = (x0 + (x1 - x0) * v[0]) * k
            val gy = (rv(v[0]) + (v[1] - 0.5f) * 24f) * k
            drawLine(Color.White.copy(alpha = 0.30f), Offset(gx, gy - 13f * k), Offset(gx, gy + 13f * k), strokeWidth = 2f * k)
            drawCircle(Color.White.copy(alpha = 0.92f), radius = 5.5f * k, center = Offset(gx, gy))
        }

        val aa = animate(0f, 1f, 4.0f, 4.8f, Easing.easeOutCubic)(t)
        if (aa > 0.01f) {
            val ax = (x1 - 34f) * k
            drawLine(accent.copy(alpha = 0.7f * aa), Offset(ax, (axisY - amp) * k), Offset(ax, (axisY + amp) * k), strokeWidth = 2f * k)
            drawLine(accent.copy(alpha = 0.7f * aa), Offset(ax - 14f * k, (axisY - amp) * k), Offset(ax + 14f * k, (axisY - amp) * k), strokeWidth = 2f * k)
            drawLine(accent.copy(alpha = 0.7f * aa), Offset(ax - 14f * k, (axisY + amp) * k), Offset(ax + 14f * k, (axisY + amp) * k), strokeWidth = 2f * k)
        }
    }

    At(x0, axisY - 200f, animate(0f, 1f, 2.0f, 2.8f, Easing.easeOutCubic)(t)) {
        Text("THE STAR'S TUG · ESPRESSO", style = brLabel(15f, accent))
    }
    At(x0, axisY + 132f, animate(0f, 1f, 3.4f, 4.2f, Easing.easeOutCubic)(t)) {
        Text("one orbit · 3.15 days", style = brBody(24f, BR_FAINT))
    }
    CenterLabel(x1 - 34f - 152f, axisY - 30f, animate(0f, 1f, 4.4f, 5.2f, Easing.easeOutCubic)(t)) {
        Text("0.9 m/s", style = brLabel(19f, accent))
    }

    val s = reveal(t, 4.8f)
    At(90f, 1400f + s.ty, s.opacity) { StatCol("2024", null, "found for real", 78f, accent, BR_MUTED) }
    At(600f, 1400f + s.ty, s.opacity) { StatCol("0.3", "× Earth", "in mass", 78f, Color.White, BR_MUTED) }

    Eyebrow("A genuine detection", accent, 200f, e1)
    Head(headline("A real world\n", "at last", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.8f), body("Barnard b gives itself away by shifting the star's light ", "less than a walking pace", "."))
}

private class BrPlanet(val label: String, val days: String, val r: Float, val period: Float, val size: Float, val colors: List<Color>)

private val BR_PLANETS = listOf(
    BrPlanet("d", "2.3 d", 118f, 2.34f, 20f, BR_IRON),
    BrPlanet("b", "3.2 d", 168f, 3.15f, 24f, BR_ROCK),
    BrPlanet("c", "4.1 d", 218f, 4.12f, 25f, BR_ASH),
    BrPlanet("e", "6.7 d", 282f, 6.74f, 22f, BR_PALE),
)

@Composable
fun BoxScope.BrFour(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xffb066)
    val cx = 540f; val cy = 1000f
    Starfield(t)

    RadialDisc(cx, cy, 150f, BR_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(cx, cy, 48f, BR_STAR_STOPS, 0.5f, 0.42f, 0.6f)

    BR_PLANETS.forEachIndexed { i, o ->
        val a = animate(0f, 1f, 0.7f + i * 0.3f, 1.8f + i * 0.3f, Easing.easeOutCubic)(t)
        if (a <= 0.01f) return@forEachIndexed
        RingArc(cx, cy, o.r * 2f, o.r * 0.8f, 0f, accent.copy(alpha = 0.22f * a), 2f)
        val ang = t * (4.6f / o.period) + i * 1.4f
        val px = cx + cos(ang) * o.r
        val py = cy + sin(ang) * o.r * 0.4f
        Sphere(
            o.size, o.colors,
            modifier = Modifier.offset((px - o.size / 2f).dp, (py - o.size / 2f).dp).alpha(a),
        )
    }

    BR_PLANETS.forEachIndexed { i, o ->
        val a = animate(0f, 1f, 2.6f + i * 0.24f, 3.4f + i * 0.24f, Easing.easeOutCubic)(t)
        CenterLabel(cx, cy - o.r * 0.4f - 34f, a) {
            Text(o.label + " · " + o.days, style = brLabel(14f, accent))
        }
    }

    val s = reveal(t, 4.2f)
    At(90f, 1420f + s.ty, s.opacity) { StatCol("4", null, "worlds, all sub-Earth", 80f, accent, BR_MUTED) }
    At(430f, 1420f + s.ty, s.opacity) { StatCol("2025", null, "when the last three landed", 80f, Color.White, BR_MUTED) }
    At(830f, 1420f + s.ty, s.opacity) { StatCol("1", " wk", "holds them all", 80f, Color.White, BR_MUTED) }

    Eyebrow("A system inside a week", accent, 200f, e1)
    Head(headline("Four worlds\nsmaller than ", "Earth", "."), 78f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.8f), body("Each weighs a fifth to a third of our planet, and rounds its star ", "in days", "."))
}

private class BrRoastP(val label: String, val au: Float, val size: Float, val colors: List<Color>)

private val BR_ROAST_P = listOf(
    BrRoastP("d", 0.0187f, 22f, BR_IRON),
    BrRoastP("b", 0.0229f, 26f, BR_ROCK),
    BrRoastP("c", 0.0273f, 27f, BR_ASH),
    BrRoastP("e", 0.0379f, 24f, BR_PALE),
)

@Composable
fun BoxScope.BrRoast(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xff6a4a)
    val zone = c(0x6fd8a0)
    Starfield(t)

    val x0 = 140f; val x1 = 950f; val axisY = 1080f
    fun px(au: Float) = x0 + au / 0.10f * (x1 - x0)
    val draw = animate(0f, 1f, 0.8f, 2.6f, Easing.easeInOutCubic)(t)
    val zoneA = animate(0f, 1f, 2.6f, 3.6f, Easing.easeOutCubic)(t)
    val appear = reveal(t, 0.5f, dur = 0.8f).opacity

    RadialDisc(x0, axisY, 132f, BR_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(x0, axisY, 40f, BR_STAR_STOPS, 0.5f, 0.42f, 0.6f)

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val y = axisY * k
        val edge = x0 + (x1 - x0) * draw

        drawLine(
            brush = Brush.horizontalGradient(
                0f to c(0xff5a28), 0.35f to c(0xd06a3a), 0.7f to c(0x5a6a70), 1f to c(0x38485c),
                startX = x0 * k, endX = x1 * k,
            ),
            start = Offset(x0 * k, y), end = Offset(edge * k, y),
            strokeWidth = 10f * k, cap = StrokeCap.Round,
        )

        if (zoneA > 0.01f) {
            val za = px(0.056f) * k
            val zb = px(0.080f) * k
            drawRect(
                color = zone.copy(alpha = 0.14f * zoneA),
                topLeft = Offset(za, (axisY - 108f) * k), size = Size(zb - za, 216f * k),
            )
            drawLine(zone.copy(alpha = 0.6f * zoneA), Offset(za, (axisY - 108f) * k), Offset(za, (axisY + 108f) * k), strokeWidth = 2f * k)
            drawLine(zone.copy(alpha = 0.6f * zoneA), Offset(zb, (axisY - 108f) * k), Offset(zb, (axisY + 108f) * k), strokeWidth = 2f * k)
        }

        val ba = animate(0f, 1f, 4.0f, 4.8f, Easing.easeOutCubic)(t)
        if (ba > 0.01f) {
            val bx0 = px(BR_ROAST_P.first().au) * k - 34f * k
            val bx1 = px(BR_ROAST_P.last().au) * k + 34f * k
            val by = (axisY - 132f) * k
            drawLine(accent.copy(alpha = 0.7f * ba), Offset(bx0, by), Offset(bx1, by), strokeWidth = 2f * k)
            drawLine(accent.copy(alpha = 0.7f * ba), Offset(bx0, by), Offset(bx0, by + 18f * k), strokeWidth = 2f * k)
            drawLine(accent.copy(alpha = 0.7f * ba), Offset(bx1, by), Offset(bx1, by + 18f * k), strokeWidth = 2f * k)
        }
    }

    BR_ROAST_P.forEachIndexed { i, p ->
        val a = animate(0f, 1f, 1.6f + i * 0.22f, 2.4f + i * 0.22f, Easing.easeOutCubic)(t)
        if (a <= 0.01f) return@forEachIndexed
        val x = px(p.au)
        Sphere(
            p.size, p.colors,
            modifier = Modifier.offset((x - p.size / 2f).dp, (axisY - p.size / 2f).dp).alpha(a),
        )
        CenterLabel(x, axisY + 40f, a) { Text(p.label, style = brLabel(15f, accent)) }
    }

    CenterLabel((px(0.056f) + px(0.080f)) / 2f, axisY + 132f, zoneA) {
        Text("THE TEMPERATE ZONE", style = brLabel(14f, zone))
    }
    CenterLabel((px(0.0187f) + px(0.0379f)) / 2f, axisY - 190f, animate(0f, 1f, 4.4f, 5.2f, Easing.easeOutCubic)(t)) {
        Text("ALL FOUR, TOO HOT", style = brLabel(15f, accent))
    }

    val s = reveal(t, 4.8f)
    At(90f, 1420f + s.ty, s.opacity) { StatCol("0.04", " AU", "the outermost orbit", 76f, accent, BR_MUTED) }
    At(628f, 1420f + s.ty, s.opacity) { StatCol("0", null, "in the temperate zone", 76f, Color.White, BR_MUTED) }

    Eyebrow("No room for water", accent, 200f, e1)
    Head(headline("All four sit\n", "too close", "."), 86f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.8f), body("The band where water could pool lies ", "well beyond them all", "."))
}

// ───────────────────────── getting there ─────────────────────────

@Composable
fun BoxScope.BrDaedalus(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xa8c4e8)
    Starfield(t)

    val shipY = 960f
    val appear = reveal(t, 0.6f, dur = 1.0f).opacity
    val prog = animate(0f, 1f, 1.2f, 5.0f, Easing.easeInOutCubic)(t)
    val sx = 250f + prog * 470f
    val years = interpolate(listOf(1.2f, 5.0f), listOf(0f, 50f), Easing.easeInOutCubic)(t)

    RadialDisc(958f, shipY, 118f, BR_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(958f, shipY, 34f, BR_STAR_STOPS, 0.5f, 0.42f, 0.6f)

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val y = shipY * k

        drawLine(
            accent.copy(alpha = 0.20f),
            Offset(130f * k, y), Offset(920f * k, y),
            strokeWidth = 2f * k,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f * k, 13f * k)),
        )

        for (i in 0..13) {
            val ph = ((t * 0.9f) + i * 0.075f) % 1f
            val plx = (sx - 104f - ph * 210f) * k
            val spread = ph * 30f * k
            val a = ((1f - ph) * 0.75f).coerceIn(0f, 1f)
            val wob = sin(t * 6f + i * 1.9f) * spread * 0.6f
            drawCircle(c(0x8fd8ff).copy(alpha = a * 0.55f), radius = (10f - ph * 6f).coerceAtLeast(1f) * k, center = Offset(plx, y + wob))
        }

        val ship = Path().apply {
            moveTo((sx + 62f) * k, y)
            lineTo((sx + 18f) * k, (shipY - 26f) * k)
            lineTo((sx - 40f) * k, (shipY - 26f) * k)
            lineTo((sx - 46f) * k, (shipY - 48f) * k)
            lineTo((sx - 100f) * k, (shipY - 66f) * k)
            lineTo((sx - 100f) * k, (shipY + 66f) * k)
            lineTo((sx - 46f) * k, (shipY + 48f) * k)
            lineTo((sx - 40f) * k, (shipY + 26f) * k)
            lineTo((sx + 18f) * k, (shipY + 26f) * k)
            close()
        }
        drawPath(
            ship,
            brush = Brush.verticalGradient(
                0f to c(0xdce8f4), 0.5f to c(0x8fa4bc), 1f to c(0x3a4756),
                startY = (shipY - 66f) * k, endY = (shipY + 66f) * k,
            ),
        )
        drawPath(ship, accent.copy(alpha = 0.75f), style = Stroke(width = 2.4f * k))
        drawLine(accent.copy(alpha = 0.5f), Offset((sx - 40f) * k, (shipY - 26f) * k), Offset((sx - 40f) * k, (shipY + 26f) * k), strokeWidth = 2f * k)

        val ba = animate(0f, 1f, 2.0f, 2.8f, Easing.easeOutCubic)(t)
        if (ba > 0.01f) {
            val by = 1300f * k
            drawLine(Color.White.copy(alpha = 0.10f), Offset(130f * k, by), Offset(950f * k, by), strokeWidth = 20f * k, cap = StrokeCap.Round)
            drawLine(accent.copy(alpha = 0.9f * ba), Offset(130f * k, by), Offset((130f + 820f * prog) * k, by), strokeWidth = 20f * k, cap = StrokeCap.Round)
        }
    }

    CenterLabel(sx, shipY - 128f, animate(0f, 1f, 1.6f, 2.4f, Easing.easeOutCubic)(t)) {
        Text("DAEDALUS", style = brLabel(17f, accent))
    }
    CenterLabel(958f, shipY + 76f, animate(0f, 1f, 1.0f, 1.8f, Easing.easeOutCubic)(t)) {
        Text("BARNARD'S STAR", style = brLabel(13f, BR_EMBER))
    }
    At(130f, 1226f, animate(0f, 1f, 2.2f, 3.0f, Easing.easeOutCubic)(t)) {
        Text("YEAR " + years.roundToInt() + " OF 50", style = brLabel(15f, accent))
    }

    val s = reveal(t, 4.4f)
    At(90f, 1400f + s.ty, s.opacity) { StatCol("12", "%", "of light speed", 78f, accent, BR_MUTED) }
    At(628f, 1400f + s.ty, s.opacity) { StatCol("50", " yrs", "for the crossing", 78f, Color.White, BR_MUTED) }

    Eyebrow("Project Daedalus · 1973", accent, 200f, e1)
    Head(headline("The starship\n", "aimed here", "."), 86f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.8f), body("Of all the stars in the sky, the designers chose ", "this one", "."))
}

private class BrStop(val ly: Float, val label: String, val size: Float, val color: Color)

private val BR_LADDER = listOf(
    BrStop(0f, "OUR SUN", 22f, c(0xffcf6a)),
    BrStop(4.37f, "ALPHA CENTAURI", 16f, c(0xffcf8a)),
    BrStop(7.86f, "WOLF 359", 12f, c(0xe07a52)),
)

@Composable
fun BoxScope.BrDistance(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f)
    val accent = c(0x9cc4ec)
    Starfield(t)

    val axisY = 1240f; val x0 = 120f; val x1 = 960f
    fun px(ly: Float) = x0 + ly / 8.6f * (x1 - x0)
    val draw = animate(0f, 1f, 1.6f, 3.6f, Easing.easeInOutCubic)(t)
    val num = interpolate(listOf(1.2f, 4.0f), listOf(0f, 5.96f), Easing.easeOutCubic)(t)

    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val y = axisY * k
        val edge = x0 + (x1 - x0) * draw
        drawLine(c(0x3a4660), Offset(x0 * k, y), Offset(edge * k, y), strokeWidth = 2f * k)

        BR_LADDER.forEach { st ->
            val gx = px(st.ly)
            val on = ((edge - gx) / 60f).coerceIn(0f, 1f)
            if (on <= 0f) return@forEach
            drawCircle(st.color.copy(alpha = 0.24f * on), radius = st.size * 2f * k, center = Offset(gx * k, y))
            drawCircle(st.color.copy(alpha = on), radius = st.size * k, center = Offset(gx * k, y))
        }

        val ma = animate(0f, 1f, 3.2f, 4.0f, Easing.easeOutBack)(t)
        if (ma > 0.01f) {
            val pulse = (0.55f + 0.45f * sin(t * 2.4f)).coerceIn(0f, 1f)
            val mp = Offset(px(5.96f) * k, y)
            drawCircle(BR_RED.copy(alpha = 0.8f * ma * pulse), radius = 38f * k * ma, center = mp, style = Stroke(width = 3f * k))
            drawCircle(c(0xff9a5a), radius = 15f * k * ma, center = mp)
        }
    }

    BR_LADDER.forEach { st ->
        val la = animate(0f, 1f, 2.4f + st.ly * 0.09f, 3.2f + st.ly * 0.09f, Easing.easeOutCubic)(t)
        CenterLabel(px(st.ly), axisY + 48f, la) { Text(st.label, style = brLabel(13f, st.color)) }
    }
    CenterLabel(px(5.96f), axisY - 104f, animate(0f, 1f, 3.8f, 4.6f, Easing.easeOutCubic)(t)) {
        Text("BARNARD'S STAR", style = brLabel(17f, BR_RED))
    }

    Eyebrow("How far is it?", accent, 300f, e1)
    At(84f, 370f + e2.ty, e2.opacity) {
        Text(
            String.format(Locale.US, "%.2f", num),
            style = TextStyle(
                fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 190.sp,
                color = Color.White, letterSpacing = (-0.04).em, lineHeight = 190.sp,
            ),
        )
    }
    At(84f, 630f + e2.ty, e2.opacity) { Text("light-years away.", style = brBody(54f, Color.White)) }
    At(84f, 716f + e2.ty, e2.opacity) { Text("≈ 56 trillion km · in the constellation Ophiuchus", style = brBody(25f, c(0x8794ac))) }

    BottomLine(1648f, reveal(t, 6.2f), body("Second on the list of nearest stars, and the closest one that stands ", "alone", "."))
}
