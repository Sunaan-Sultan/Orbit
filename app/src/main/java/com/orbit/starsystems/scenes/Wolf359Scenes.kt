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
import androidx.compose.ui.graphics.PathEffect
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
import com.orbit.starsystems.ui.Starfield
import java.util.Locale
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

private val WF_RED = c(0xe0503c)
private val WF_EMBER = c(0xff7a4a)
private val WF_MUTED = c(0x9a8478)
private val WF_FAINT = c(0x7f8898)

private val WF_STAR_STOPS = arrayOf(
    0f to c(0xffc59a), 0.38f to c(0xff7440), 0.76f to c(0xc43c1a), 1f to c(0x6a1e0e),
)
private val WF_HALO_STOPS = arrayOf(
    0f to Color(0x55ff5a2a), 0.5f to Color(0x16d0401a), 0.85f to Color(0x00000000), 1f to Color(0x00000000),
)
private val WF_SUN_STOPS = arrayOf(
    0f to c(0xfff8e0), 0.45f to c(0xffd267), 0.8f to c(0xf0a52e), 1f to c(0xc57a1e),
)
private val WF_SUN_HALO = arrayOf(
    0f to Color(0x44ffc24a), 0.5f to Color(0x12ffa838), 0.85f to Color(0x00000000), 1f to Color(0x00000000),
)

private val WF_JUPITER = listOf(c(0xe6cfae), c(0xc08f68), c(0x5c402c))
private val WF_WARM = listOf(c(0xf0c8a0), c(0xc07a4e), c(0x5e3020))
private val WF_COLD = listOf(c(0xc4d4e4), c(0x7a8ea8), c(0x2e3c4e))

private fun wfLabel(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = size.sp,
    letterSpacing = 0.14.em, color = color,
)

private fun wfBody(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = size.sp, color = color,
)

private fun wfRandoms(seed: Int, count: Int, each: Int): List<FloatArray> {
    var s = seed
    fun rnd(): Float {
        s = s * 1664525 + 1013904223
        return ((s ushr 9) and 0xFFFF) / 65535f
    }
    return List(count) { FloatArray(each) { rnd() } }
}

private class WfPoint(val x: Float, val y: Float, val r: Float)

private val WF_LEO = listOf(
    WfPoint(766f, 986f, 13f),
    WfPoint(802f, 892f, 8f),
    WfPoint(814f, 800f, 11f),
    WfPoint(792f, 714f, 7f),
    WfPoint(740f, 658f, 8f),
    WfPoint(674f, 652f, 7f),
    WfPoint(472f, 858f, 9f),
    WfPoint(428f, 728f, 10f),
    WfPoint(246f, 760f, 12f),
)

private val WF_LEO_LINES = listOf(
    0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5,
    0 to 6, 6 to 7, 7 to 8, 6 to 8,
)

@Composable
fun BoxScope.WfIntro(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    Starfield(t)

    val lines = animate(0f, 1f, 0.9f, 3.0f, Easing.easeInOutCubic)(t)
    val markA = animate(0f, 1f, 3.2f, 4.1f, Easing.easeOutBack)(t)
    val emberA = animate(0f, 1f, 4.4f, 5.4f, Easing.easeOutCubic)(t)
    val appear = reveal(t, 0.6f, dur = 0.9f).opacity
    val mx = 630f; val my = 1046f

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f

        WF_LEO_LINES.forEachIndexed { i, pair ->
            val on = (lines * WF_LEO_LINES.size - i).coerceIn(0f, 1f)
            if (on <= 0f) return@forEachIndexed
            val a = WF_LEO[pair.first]; val b = WF_LEO[pair.second]
            drawLine(
                c(0x8fa4c4).copy(alpha = 0.28f * on),
                Offset(a.x * k, a.y * k),
                Offset((a.x + (b.x - a.x) * on) * k, (a.y + (b.y - a.y) * on) * k),
                strokeWidth = 2f * k,
            )
        }

        WF_LEO.forEachIndexed { i, st ->
            val a = (lines * 1.6f - i * 0.05f).coerceIn(0f, 1f)
            val tw = 0.82f + 0.18f * sin(t * 1.4f + i * 1.7f)
            drawCircle(Color.White.copy(alpha = 0.14f * a), radius = st.r * 2.1f * k, center = Offset(st.x * k, st.y * k))
            drawCircle(Color.White.copy(alpha = (0.9f * a * tw).coerceIn(0f, 1f)), radius = st.r * 0.52f * k, center = Offset(st.x * k, st.y * k))
        }

        if (markA > 0.01f) {
            val mp = Offset(mx * k, my * k)
            drawCircle(
                WF_RED.copy(alpha = 0.7f * markA), radius = 48f * k * markA, center = mp,
                style = Stroke(width = 2.6f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f * k, 12f * k))),
            )
            if (emberA > 0.01f) {
                val tw = (0.55f + 0.45f * sin(t * 2.6f)).coerceIn(0f, 1f)
                drawCircle(Color(0x55ff5a2a).copy(alpha = emberA), radius = 28f * k, center = mp)
                drawCircle(WF_EMBER.copy(alpha = emberA * tw), radius = 7f * k, center = mp)
            }
        }
    }

    CenterLabel(766f, 1016f, animate(0f, 1f, 2.0f, 2.8f, Easing.easeOutCubic)(t)) {
        Text("REGULUS", style = wfLabel(13f, WF_FAINT))
    }
    CenterLabel(246f, 790f, animate(0f, 1f, 2.6f, 3.4f, Easing.easeOutCubic)(t)) {
        Text("DENEBOLA", style = wfLabel(13f, WF_FAINT))
    }
    CenterLabel(mx, my + 66f, markA) { Text("WOLF 359", style = wfLabel(17f, WF_RED)) }
    CenterLabel(mx, my + 102f, emberA) { Text("nothing the eye can catch", style = wfBody(21f, WF_FAINT)) }

    val s = reveal(t, 4.6f)
    At(90f, 1400f + s.ty, s.opacity) { StatCol("7.86", " ly", "from here", 72f, Color.White, WF_MUTED) }
    At(430f, 1400f + s.ty, s.opacity) { StatCol("5th", null, "closest system", 72f, WF_RED, WF_MUTED) }
    At(772f, 1400f + s.ty, s.opacity) { StatCol("1918", null, "when we noticed", 72f, Color.White, WF_MUTED) }

    Eyebrow("Hiding in the tail of Leo", WF_RED, 200f, e1)
    Head(headline("The faintest\nstar ", "next door", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.8f), body("Four systems lie closer, and ", "two of them are not even stars", "."))
}

@Composable
fun BoxScope.WfTiny(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xff8a4a)
    val cy = 1010f

    val grow = animate(0.08f, 1f, 0.5f, 2.4f, Easing.easeOutCubic)(t)
    val popWf = animate(0f, 1f, 1.7f, 3.1f, Easing.easeOutBack)(t)
    val popJup = animate(0f, 1f, 2.2f, 3.6f, Easing.easeOutBack)(t)

    val sunCx = 232f; val sunR = 300f * grow
    val wfCx = 706f; val wfR = 300f * 0.16f * popWf
    val jupCx = 884f; val jupD = 60f * popJup

    RadialDisc(sunCx, cy, sunR + 130f, WF_SUN_HALO, 0.5f, 0.5f, 0.5f)
    RadialDisc(sunCx, cy, sunR, WF_SUN_STOPS, 0.5f, 0.42f, 0.6f)
    RadialDisc(wfCx, cy, wfR * 3.2f, WF_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(wfCx, cy, wfR, WF_STAR_STOPS, 0.5f, 0.42f, 0.6f)
    if (jupD > 1f) {
        Sphere(
            jupD, WF_JUPITER,
            modifier = Modifier.offset((jupCx - jupD / 2f).dp, (cy - jupD / 2f).dp).alpha(popJup),
        )
    }

    val bars = animate(0f, 1f, 3.0f, 3.9f, Easing.easeOutCubic)(t)
    Canvas(Modifier.fillMaxSize().alpha(bars)) {
        val k = size.width / 1080f
        val gy = (cy + 300f + 62f) * k
        drawLine(Color.White.copy(alpha = 0.16f), Offset((sunCx - 300f) * k, gy), Offset((sunCx + 300f) * k, gy), strokeWidth = 2f * k)
        drawLine(accent.copy(alpha = 0.8f), Offset((wfCx - 48f) * k, gy), Offset((wfCx + 48f) * k, gy), strokeWidth = 4f * k, cap = StrokeCap.Round)
        drawLine(c(0xc08f68).copy(alpha = 0.8f), Offset((jupCx - 30f) * k, gy), Offset((jupCx + 30f) * k, gy), strokeWidth = 4f * k, cap = StrokeCap.Round)
    }

    CenterLabel(sunCx, cy + 384f, animate(0f, 1f, 2.6f, 3.4f, Easing.easeOutCubic)(t)) {
        Text("THE SUN", style = wfLabel(17f, c(0xffd267)))
    }
    CenterLabel(wfCx, cy + 384f, animate(0f, 1f, 3.2f, 4.0f, Easing.easeOutCubic)(t)) {
        Text("WOLF 359", style = wfLabel(16f, accent))
    }
    CenterLabel(jupCx, cy + 384f, animate(0f, 1f, 3.6f, 4.4f, Easing.easeOutCubic)(t)) {
        Text("JUPITER", style = wfLabel(14f, c(0xc9a074)))
    }

    val s = reveal(t, 4.4f)
    At(90f, 1450f + s.ty, s.opacity) { StatCol("0.09", "×", "the Sun's mass", 72f, accent, WF_MUTED) }
    At(430f, 1450f + s.ty, s.opacity) { StatCol("0.16", "×", "the Sun's width", 72f, Color.White, WF_MUTED) }
    At(772f, 1450f + s.ty, s.opacity) { StatCol("90", null, "Jupiters of mass", 72f, Color.White, WF_MUTED) }

    Eyebrow("Nine per cent of a Sun", accent, 200f, e1)
    Head(headline("Barely\na ", "star", " at all."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.6f), body("A shade lighter and hydrogen would ", "never have caught fire", "."))
}

private const val WF_NM_MIN = 300f
private const val WF_NM_MAX = 4000f
private val WF_LOG_MIN = log10(WF_NM_MIN)
private val WF_LOG_SPAN = log10(WF_NM_MAX) - WF_LOG_MIN

private fun wfPlanck(nm: Float, tK: Float): Float {
    val x = (1.4388e7f / (nm * tK)).coerceAtMost(60f)
    val e = exp(x) - 1f
    if (e <= 0f) return 0f
    return (1f / nm.pow(5)) / e
}

private fun wfCurve(nm: Float, tK: Float): Float {
    val pk = wfPlanck(2.8978e6f / tK, tK)
    if (pk <= 0f) return 0f
    return (wfPlanck(nm, tK) / pk).coerceIn(0f, 1f)
}

@Composable
fun BoxScope.WfEmber(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xff6a3c)
    val sunCol = c(0xffd267)
    Starfield(t)

    val x0 = 128f; val x1 = 960f
    val base = 1244f; val top = 812f
    fun px(nm: Float) = x0 + (log10(nm) - WF_LOG_MIN) / WF_LOG_SPAN * (x1 - x0)

    val bandA = animate(0f, 1f, 0.7f, 1.6f, Easing.easeOutCubic)(t)
    val drawSun = animate(0f, 1f, 1.4f, 3.2f, Easing.easeInOutCubic)(t)
    val drawWf = animate(0f, 1f, 2.4f, 4.4f, Easing.easeInOutCubic)(t)
    val appear = reveal(t, 0.5f, dur = 0.8f).opacity

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val vb0 = px(380f) * k; val vb1 = px(750f) * k

        drawRect(
            brush = Brush.horizontalGradient(
                0f to c(0x7a4ad8), 0.22f to c(0x3a7ae0), 0.46f to c(0x46c46a),
                0.72f to c(0xe8d24a), 1f to c(0xe0503c),
                startX = vb0, endX = vb1,
            ),
            topLeft = Offset(vb0, top * k), size = Size(vb1 - vb0, (base - top) * k),
            alpha = 0.14f * bandA,
        )
        drawLine(Color.White.copy(alpha = 0.16f * bandA), Offset(vb0, top * k), Offset(vb0, base * k), strokeWidth = 2f * k)
        drawLine(Color.White.copy(alpha = 0.16f * bandA), Offset(vb1, top * k), Offset(vb1, base * k), strokeWidth = 2f * k)
        drawLine(Color.White.copy(alpha = 0.12f), Offset(x0 * k, base * k), Offset(x1 * k, base * k), strokeWidth = 2f * k)

        fun curvePath(tK: Float, prog: Float): Path {
            val p = Path()
            var u = 0f
            var first = true
            while (u <= prog) {
                val nm = 10f.pow(WF_LOG_MIN + u * WF_LOG_SPAN)
                val gx = (x0 + (x1 - x0) * u) * k
                val gy = (base - (base - top) * wfCurve(nm, tK)) * k
                if (first) { p.moveTo(gx, gy); first = false } else p.lineTo(gx, gy)
                u += 0.005f
            }
            return p
        }

        if (drawSun > 0.005f) {
            val p = curvePath(5772f, drawSun)
            drawPath(p, sunCol.copy(alpha = 0.22f), style = Stroke(width = 11f * k, cap = StrokeCap.Round))
            drawPath(p, sunCol, style = Stroke(width = 3.2f * k, cap = StrokeCap.Round))
        }
        if (drawWf > 0.005f) {
            val p = curvePath(2800f, drawWf)
            drawPath(p, accent.copy(alpha = 0.24f), style = Stroke(width = 12f * k, cap = StrokeCap.Round))
            drawPath(p, accent, style = Stroke(width = 3.6f * k, cap = StrokeCap.Round))
        }

        val ma = animate(0f, 1f, 4.2f, 5.0f, Easing.easeOutCubic)(t)
        if (ma > 0.01f) {
            val peak = px(1035f) * k
            drawLine(
                accent.copy(alpha = 0.55f * ma), Offset(peak, top * k), Offset(peak, base * k),
                strokeWidth = 2f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * k, 11f * k)),
            )
            drawCircle(accent.copy(alpha = ma), radius = 9f * k, center = Offset(peak, top * k))
        }
    }

    CenterLabel((px(380f) + px(750f)) / 2f, base + 26f, bandA) {
        Text("VISIBLE", style = wfLabel(14f, Color.White))
    }
    CenterLabel(px(2000f), base + 26f, animate(0f, 1f, 3.4f, 4.2f, Easing.easeOutCubic)(t)) {
        Text("INFRARED", style = wfLabel(14f, accent))
    }
    CenterLabel(px(502f), top - 62f, animate(0f, 1f, 3.0f, 3.8f, Easing.easeOutCubic)(t)) {
        Text("THE SUN", style = wfLabel(15f, sunCol))
    }
    CenterLabel(px(1035f), top - 62f, animate(0f, 1f, 4.4f, 5.2f, Easing.easeOutCubic)(t)) {
        Text("WOLF 359", style = wfLabel(15f, accent))
    }
    At(x0, base + 74f, animate(0f, 1f, 4.8f, 5.6f, Easing.easeOutCubic)(t)) {
        Text("each curve scaled to its own peak", style = wfBody(20f, WF_FAINT))
    }

    val s = reveal(t, 5.0f)
    At(90f, 1418f + s.ty, s.opacity) { StatCol("2,800", " K", "at the surface", 66f, accent, WF_MUTED) }
    At(430f, 1418f + s.ty, s.opacity) { StatCol("1.0", " µm", "where its light peaks", 66f, Color.White, WF_MUTED) }
    At(772f, 1418f + s.ty, s.opacity) { StatCol("0.50", " µm", "where the Sun's does", 66f, Color.White, WF_MUTED) }

    Eyebrow("Half the Sun's temperature", accent, 200f, e1)
    Head(headline("An ember,\nnot a ", "lamp", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.0f), body("Nearly everything it gives off is ", "infrared heat we cannot see", "."))
}

private class WfMag(val m: Float, val label: String, val color: Color, val below: Boolean)

private val WF_MAGS = listOf(
    WfMag(-1.46f, "SIRIUS", c(0x9cc4ec), false),
    WfMag(0.03f, "VEGA", c(0xbcd0e8), true),
    WfMag(1.98f, "POLARIS", c(0xc8d4e0), false),
    WfMag(6.5f, "NAKED-EYE LIMIT", c(0x9aa8ff), true),
    WfMag(9.5f, "BARNARD'S STAR", c(0xf4794a), false),
    WfMag(13.5f, "WOLF 359", WF_RED, true),
)

@Composable
fun BoxScope.WfInvisible(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x9aa8ff)
    Starfield(t)

    val x0 = 110f; val x1 = 962f; val axisY = 1046f
    fun px(m: Float) = x0 + (m + 2f) / 17f * (x1 - x0)
    val draw = animate(0f, 1f, 0.9f, 3.4f, Easing.easeInOutCubic)(t)
    val veil = animate(0f, 1f, 3.4f, 4.4f, Easing.easeOutCubic)(t)
    val appear = reveal(t, 0.5f, dur = 0.8f).opacity

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val y = axisY * k
        val edge = x0 + (x1 - x0) * draw

        drawLine(Color.White.copy(alpha = 0.10f), Offset(x0 * k, y), Offset(x1 * k, y), strokeWidth = 2f * k)
        drawLine(
            brush = Brush.horizontalGradient(
                0f to c(0xdfe8f4), 0.5f to c(0x7f8ea4), 1f to c(0x3a4152),
                startX = x0 * k, endX = x1 * k,
            ),
            start = Offset(x0 * k, y), end = Offset(edge * k, y), strokeWidth = 6f * k, cap = StrokeCap.Round,
        )

        if (veil > 0.01f) {
            val vx = px(6.5f) * k
            drawRect(
                color = accent.copy(alpha = 0.07f * veil),
                topLeft = Offset(vx, (axisY - 148f) * k), size = Size(x1 * k - vx, 296f * k),
            )
            drawLine(accent.copy(alpha = 0.5f * veil), Offset(vx, (axisY - 148f) * k), Offset(vx, (axisY + 148f) * k), strokeWidth = 2.2f * k)
        }

        WF_MAGS.forEach { st ->
            val gx = px(st.m)
            val on = ((edge - gx) / 60f).coerceIn(0f, 1f)
            if (on <= 0f) return@forEach
            val r = (30f * exp(-0.17f * (st.m + 2f))).coerceAtLeast(2.4f)
            val dim = (1f - (st.m + 2f) / 20f).coerceIn(0.3f, 1f)
            drawCircle(st.color.copy(alpha = 0.18f * on * dim), radius = r * 2.2f * k, center = Offset(gx * k, y))
            drawCircle(st.color.copy(alpha = on * dim), radius = r * k, center = Offset(gx * k, y))
            drawLine(
                st.color.copy(alpha = 0.34f * on),
                Offset(gx * k, y + (if (st.below) 26f else -26f) * k),
                Offset(gx * k, y + (if (st.below) 62f else -62f) * k),
                strokeWidth = 2f * k,
            )
        }

        val ma = animate(0f, 1f, 4.2f, 5.0f, Easing.easeOutBack)(t)
        if (ma > 0.01f) {
            val pulse = (0.55f + 0.45f * sin(t * 2.4f)).coerceIn(0f, 1f)
            drawCircle(
                WF_RED.copy(alpha = 0.85f * ma * pulse), radius = 34f * k * ma,
                center = Offset(px(13.5f) * k, y), style = Stroke(width = 3f * k),
            )
        }
    }

    WF_MAGS.forEach { st ->
        val la = animate(0f, 1f, 2.0f + (st.m + 2f) * 0.11f, 2.8f + (st.m + 2f) * 0.11f, Easing.easeOutCubic)(t)
        CenterLabel(px(st.m), axisY + (if (st.below) 76f else -110f), la) {
            Text(st.label, style = wfLabel(13f, st.color))
        }
    }
    CenterLabel(px(11f), axisY - 176f, veil) { Text("BEYOND THE EYE", style = wfLabel(15f, accent)) }

    val s = reveal(t, 4.8f)
    At(90f, 1418f + s.ty, s.opacity) { StatCol("13.5", null, "how bright it looks", 72f, WF_RED, WF_MUTED) }
    At(430f, 1418f + s.ty, s.opacity) { StatCol("600", "×", "too faint for the eye", 72f, Color.White, WF_MUTED) }
    At(772f, 1418f + s.ty, s.opacity) { StatCol("20", " cm", "of telescope, at least", 72f, Color.White, WF_MUTED) }

    Eyebrow("Eight light-years, and still hidden", accent, 200f, e1)
    Head(headline("Too faint\nto ", "ever see", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.8f), body("Binoculars will not reach it. You need ", "a real telescope", "."))
}

private class WfEra(val yr: Float, val label: String, val color: Color)

private val WF_ERAS = listOf(
    WfEra(1e9f, "1 bn", c(0x8794ac)),
    WfEra(1e10f, "10 bn", c(0x8794ac)),
    WfEra(1e11f, "100 bn", c(0x8794ac)),
    WfEra(1e12f, "1 tn", c(0x8794ac)),
    WfEra(1e13f, "10 tn", c(0x8794ac)),
)

@Composable
fun BoxScope.WfYoung(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x7fd0e8)
    val sunCol = c(0xffd267)
    Starfield(t)

    val x0 = 132f; val x1 = 958f
    val sunY = 918f; val wfY = 1064f; val tickY = 1188f
    fun px(yr: Float) = x0 + ((log10(yr) - 8f) / 5f).coerceIn(0f, 1f) * (x1 - x0)

    val drawSun = animate(0f, 1f, 1.0f, 2.4f, Easing.easeInOutCubic)(t)
    val drawWf = animate(0f, 1f, 2.4f, 5.0f, Easing.easeInOutCubic)(t)
    val appear = reveal(t, 0.6f, dur = 0.9f).opacity

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f

        drawLine(Color.White.copy(alpha = 0.10f), Offset(x0 * k, tickY * k), Offset(x1 * k, tickY * k), strokeWidth = 2f * k)
        WF_ERAS.forEach { er ->
            val gx = px(er.yr) * k
            drawLine(Color.White.copy(alpha = 0.18f), Offset(gx, (tickY - 12f) * k), Offset(gx, (tickY + 12f) * k), strokeWidth = 2f * k)
        }

        val sunEnd = x0 + (px(1e10f) - x0) * drawSun
        drawLine(Color.White.copy(alpha = 0.07f), Offset(x0 * k, sunY * k), Offset(x1 * k, sunY * k), strokeWidth = 22f * k, cap = StrokeCap.Round)
        drawLine(
            brush = Brush.horizontalGradient(
                0f to c(0xffe6a0), 1f to c(0xd07a2a),
                startX = x0 * k, endX = px(1e10f) * k,
            ),
            start = Offset(x0 * k, sunY * k), end = Offset(sunEnd * k, sunY * k),
            strokeWidth = 22f * k, cap = StrokeCap.Round,
        )

        val wfEnd = x0 + (px(1e13f) - x0) * drawWf
        drawLine(Color.White.copy(alpha = 0.07f), Offset(x0 * k, wfY * k), Offset(x1 * k, wfY * k), strokeWidth = 22f * k, cap = StrokeCap.Round)
        drawLine(
            brush = Brush.horizontalGradient(
                0f to c(0xff8a52), 0.5f to c(0xd8503c), 1f to c(0x7f3a5c),
                startX = x0 * k, endX = px(1e13f) * k,
            ),
            start = Offset(x0 * k, wfY * k), end = Offset(wfEnd * k, wfY * k),
            strokeWidth = 22f * k, cap = StrokeCap.Round,
        )

        val nowA = animate(0f, 1f, 3.4f, 4.2f, Easing.easeOutCubic)(t)
        if (nowA > 0.01f) {
            val nx = px(5e8f) * k
            drawLine(
                accent.copy(alpha = 0.7f * nowA), Offset(nx, (sunY - 70f) * k), Offset(nx, (wfY + 70f) * k),
                strokeWidth = 2.4f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * k, 11f * k)),
            )
            val pulse = (0.55f + 0.45f * sin(t * 2.3f)).coerceIn(0f, 1f)
            drawCircle(accent.copy(alpha = nowA * pulse), radius = 9f * k, center = Offset(nx, wfY * k))
        }
    }

    WF_ERAS.forEach { er ->
        val la = animate(0f, 1f, 1.4f + log10(er.yr) * 0.16f, 2.2f + log10(er.yr) * 0.16f, Easing.easeOutCubic)(t)
        CenterLabel(px(er.yr), tickY + 24f, la) { Text(er.label, style = wfBody(21f, er.color)) }
    }
    At(x0 + 14f, sunY - 66f, animate(0f, 1f, 1.6f, 2.4f, Easing.easeOutCubic)(t)) {
        Text("THE SUN", style = wfLabel(16f, sunCol))
    }
    At(x0 + 14f, wfY + 44f, animate(0f, 1f, 3.0f, 3.8f, Easing.easeOutCubic)(t)) {
        Text("WOLF 359", style = wfLabel(16f, WF_EMBER))
    }
    CenterLabel(px(5e8f), sunY - 118f, animate(0f, 1f, 3.8f, 4.6f, Easing.easeOutCubic)(t)) {
        Text("TODAY", style = wfLabel(14f, accent))
    }

    val s = reveal(t, 5.0f)
    At(90f, 1420f + s.ty, s.opacity) { StatCol("<1", " bn", "years old so far", 72f, accent, WF_MUTED) }
    At(430f, 1420f + s.ty, s.opacity) { StatCol("10", " bn", "is the Sun's whole life", 72f, sunCol, WF_MUTED) }
    At(772f, 1420f + s.ty, s.opacity) { StatCol("10", " tn", "and it burns on", 72f, WF_EMBER, WF_MUTED) }

    Eyebrow("Young, and in no hurry", accent, 200f, e1)
    Head(headline("Still burning\nlong after the ", "Sun", "."), 82f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.0f), body("Small stars sip their fuel, and last ", "a thousand times longer", "."))
}

private fun wfBurst(u: Float, at: Float, amp: Float, decay: Float): Float = when {
    u < at -> 0f
    u < at + 0.018f -> amp * (u - at) / 0.018f
    else -> amp * exp(-(u - at - 0.018f) * decay)
}

private fun wfFlares(u: Float): Float =
    wfBurst(u, 0.16f, 0.32f, 26f) + wfBurst(u, 0.40f, 1f, 10f) +
        wfBurst(u, 0.66f, 0.44f, 19f) + wfBurst(u, 0.86f, 0.26f, 30f)

@Composable
fun BoxScope.WfFlare(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xffd15a)
    Starfield(t)

    val x0 = 118f; val x1 = 962f
    val base = 1298f; val peak = 902f
    val draw = animate(0f, 1f, 0.9f, 5.0f, Easing.linear)(t)
    val burst = wfFlares(draw).coerceIn(0f, 1.2f)

    val starCy = 704f
    RadialDisc(540f, starCy, 58f + 210f * burst, WF_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(540f, starCy, 54f + 30f * burst, WF_STAR_STOPS, 0.5f, 0.42f, 0.6f)

    Canvas(Modifier.fillMaxSize().alpha(reveal(t, 0.6f, dur = 0.8f).opacity)) {
        val k = size.width / 1080f

        drawLine(
            Color.White.copy(alpha = 0.16f), Offset(x0 * k, base * k), Offset(x1 * k, base * k),
            strokeWidth = 2f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f * k, 12f * k)),
        )
        drawLine(
            accent.copy(alpha = 0.20f), Offset(x0 * k, peak * k), Offset(x1 * k, peak * k),
            strokeWidth = 2f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f * k, 12f * k)),
        )

        fun cy(u: Float): Float {
            val noise = sin(u * 57f) * 4f + sin(u * 26f + 1.1f) * 3f
            return base + noise - (base - peak) * wfFlares(u)
        }

        val p = Path()
        var u = 0f
        var first = true
        while (u <= draw) {
            val gx = (x0 + (x1 - x0) * u) * k
            val gy = cy(u) * k
            if (first) { p.moveTo(gx, gy); first = false } else p.lineTo(gx, gy)
            u += 0.003f
        }
        if (!first) {
            drawPath(p, accent.copy(alpha = 0.26f), style = Stroke(width = 12f * k, cap = StrokeCap.Round))
            drawPath(p, accent, style = Stroke(width = 3.4f * k, cap = StrokeCap.Round))
        }

        val hx = (x0 + (x1 - x0) * draw) * k
        val hy = cy(draw) * k
        drawCircle(accent.copy(alpha = 0.30f), radius = 24f * k, center = Offset(hx, hy))
        drawCircle(Color.White, radius = 7f * k, center = Offset(hx, hy))
    }

    CenterLabel(540f, starCy + 168f, animate(0f, 1f, 1.0f, 1.8f, Easing.easeOutCubic)(t)) {
        Text("CN LEONIS", style = wfLabel(17f, WF_EMBER))
    }
    At(x0, peak - 56f, animate(0f, 1f, 2.8f, 3.6f, Easing.easeOutCubic)(t)) {
        Text("MINUTES TO PEAK", style = wfLabel(15f, accent))
    }
    At(x0, base + 24f, animate(0f, 1f, 1.6f, 2.4f, Easing.easeOutCubic)(t)) {
        Text("its usual, patient glow", style = wfBody(21f, WF_FAINT))
    }

    val s = reveal(t, 5.0f)
    At(90f, 1428f + s.ty, s.opacity) { StatCol("×3", null, "brighter, in minutes", 76f, accent, WF_MUTED) }
    At(628f, 1428f + s.ty, s.opacity) { StatCol("daily", null, "and then it does it again", 76f, Color.White, WF_MUTED) }

    Eyebrow("A small star with a temper", accent, 200f, e1)
    Head(headline("It erupts,\nover and ", "over", "."), 86f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.0f), body("Every outburst washes ultraviolet and X-rays across ", "anything close", "."))
}

private val WF_PLATE_STARS = wfRandoms(0x3C71B9, 15, 3)
private const val WF_MOVER = 6

@Composable
fun BoxScope.WfWolf(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xc8b48a)
    Starfield(t)

    val pw = 372f; val py = 802f
    val lx = 118f; val rx = 590f
    val plateA = animate(0f, 1f, 0.7f, 1.7f, Easing.easeOutCubic)(t)
    val plateB = animate(0f, 1f, 1.5f, 2.5f, Easing.easeOutCubic)(t)
    val markA = animate(0f, 1f, 3.0f, 3.9f, Easing.easeOutBack)(t)
    val shift = animate(0f, 1f, 4.0f, 4.9f, Easing.easeInOutCubic)(t)
    val dx = 40f; val dy = -24f

    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f

        fun plate(ox: Float, on: Float, moved: Boolean) {
            if (on <= 0.01f) return
            drawRect(
                color = c(0x171310).copy(alpha = on),
                topLeft = Offset(ox * k, py * k), size = Size(pw * k, pw * k),
            )
            drawRect(
                color = accent.copy(alpha = 0.26f * on),
                topLeft = Offset(ox * k, py * k), size = Size(pw * k, pw * k),
                style = Stroke(width = 2f * k),
            )
            WF_PLATE_STARS.forEachIndexed { i, v ->
                val sx = ox + 24f + v[0] * (pw - 48f)
                val sy = py + 24f + v[1] * (pw - 48f)
                val mover = i == WF_MOVER
                val gx = if (mover && moved) sx + dx * shift else sx
                val gy = if (mover && moved) sy + dy * shift else sy
                val r = (3.4f + v[2] * 6f) * k
                drawCircle(Color.White.copy(alpha = 0.12f * on), radius = r * 2.4f, center = Offset(gx * k, gy * k))
                drawCircle(Color.White.copy(alpha = 0.88f * on), radius = r, center = Offset(gx * k, gy * k))
            }
        }

        plate(lx, plateA, false)
        plate(rx, plateB, true)

        if (markA > 0.01f) {
            val v = WF_PLATE_STARS[WF_MOVER]
            val bx = 24f + v[0] * (pw - 48f)
            val by = 24f + v[1] * (pw - 48f)
            val pulse = (0.55f + 0.45f * sin(t * 2.4f)).coerceIn(0f, 1f)

            drawCircle(
                WF_RED.copy(alpha = 0.8f * markA * pulse), radius = 34f * k * markA,
                center = Offset((lx + bx) * k, (py + by) * k), style = Stroke(width = 2.6f * k),
            )
            drawCircle(
                WF_RED.copy(alpha = 0.45f * markA), radius = 30f * k * markA,
                center = Offset((rx + bx) * k, (py + by) * k),
                style = Stroke(width = 2.2f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f * k, 9f * k))),
            )
            if (shift > 0.02f) {
                val sx = (rx + bx) * k; val sy = (py + by) * k
                val ex = (rx + bx + dx * shift) * k; val ey = (py + by + dy * shift) * k
                drawLine(WF_EMBER.copy(alpha = 0.9f), Offset(sx, sy), Offset(ex, ey), strokeWidth = 3f * k, cap = StrokeCap.Round)
                drawCircle(
                    WF_RED.copy(alpha = 0.85f * pulse), radius = 34f * k,
                    center = Offset(ex, ey), style = Stroke(width = 2.6f * k),
                )
            }
        }
    }

    CenterLabel(lx + pw / 2f, py + pw + 22f, plateA) { Text("EARLIER PLATE", style = wfLabel(14f, accent)) }
    CenterLabel(rx + pw / 2f, py + pw + 22f, plateB) { Text("YEARS LATER", style = wfLabel(14f, accent)) }
    CenterLabel(rx + pw / 2f, py - 62f, animate(0f, 1f, 4.6f, 5.4f, Easing.easeOutCubic)(t)) {
        Text("ONE SPECK HAD MOVED", style = wfLabel(16f, WF_EMBER))
    }

    val s = reveal(t, 5.2f)
    At(90f, 1408f + s.ty, s.opacity) { StatCol("359", null, "in Wolf's catalogue", 72f, accent, WF_MUTED) }
    At(430f, 1408f + s.ty, s.opacity) { StatCol("1918", null, "when he spotted it", 72f, Color.White, WF_MUTED) }
    At(772f, 1408f + s.ty, s.opacity) { StatCol("4.7", "″/yr", "across the sky", 72f, WF_EMBER, WF_MUTED) }

    Eyebrow("Max Wolf, on glass plates", accent, 200f, e1)
    Head(headline("The dot\nthat ", "moved", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.0f), body("Everything else on the plate stayed exactly ", "where it was", "."))
}

@Composable
fun BoxScope.WfMaybe(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x8fe0b4)
    Starfield(t)

    val cx = 540f; val cy = 1046f
    val innerR = 128f; val outerR = 384f
    val ringIn = animate(0f, 1f, 0.9f, 2.0f, Easing.easeOutCubic)(t)
    val ringOut = animate(0f, 1f, 1.6f, 2.9f, Easing.easeOutCubic)(t)

    RadialDisc(cx, cy, 138f, WF_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(cx, cy, 44f, WF_STAR_STOPS, 0.5f, 0.42f, 0.6f)

    RingArc(cx, cy, innerR * 2f, innerR * 0.76f, 0f, accent.copy(alpha = 0.30f * ringIn), 2f, dash = true)
    RingArc(cx, cy, outerR * 2f, outerR * 0.76f, 0f, accent.copy(alpha = 0.24f * ringOut), 2f, dash = true)

    val ghost = (0.58f + 0.30f * sin(t * 1.9f)).coerceIn(0f, 1f)
    val pIn = animate(0f, 1f, 2.2f, 3.0f, Easing.easeOutCubic)(t)
    val pOut = animate(0f, 1f, 2.9f, 3.7f, Easing.easeOutCubic)(t)

    val angIn = t * 1.5f
    val ix = cx + cos(angIn) * innerR
    val iy = cy + sin(angIn) * innerR * 0.38f
    if (pIn > 0.01f) {
        Sphere(
            34f, WF_WARM,
            modifier = Modifier.offset((ix - 17f).dp, (iy - 17f).dp).alpha(pIn * ghost),
        )
    }

    val angOut = t * 0.42f + 2.1f
    val ox = cx + cos(angOut) * outerR
    val oy = cy + sin(angOut) * outerR * 0.38f
    if (pOut > 0.01f) {
        Sphere(
            30f, WF_COLD,
            modifier = Modifier.offset((ox - 15f).dp, (oy - 15f).dp).alpha(pOut * ghost),
        )
    }

    CenterLabel(ix, iy - 62f, pIn * ghost) { Text("?", style = wfLabel(30f, accent)) }
    CenterLabel(ox, oy - 60f, pOut * ghost) { Text("?", style = wfLabel(28f, accent)) }

    CenterLabel(cx, cy - innerR * 0.38f - 118f, animate(0f, 1f, 3.4f, 4.2f, Easing.easeOutCubic)(t)) {
        Text("WOLF 359 b · 2.7 DAYS", style = wfLabel(14f, c(0xf0c8a0)))
    }
    CenterLabel(cx, cy + outerR * 0.38f + 46f, animate(0f, 1f, 4.0f, 4.8f, Easing.easeOutCubic)(t)) {
        Text("WOLF 359 c · 2.7 YEARS", style = wfLabel(14f, c(0xa8bcd4)))
    }

    val s = reveal(t, 4.8f)
    At(90f, 1440f + s.ty, s.opacity) { StatCol("2", null, "candidates, not planets", 76f, accent, WF_MUTED) }
    At(628f, 1440f + s.ty, s.opacity) { StatCol("2019", null, "when the signals turned up", 76f, Color.White, WF_MUTED) }

    Eyebrow("Found in the wobble, unconfirmed", accent, 200f, e1)
    Head(headline("Two worlds,\n", "perhaps", "."), 88f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.8f), body("A star that flares this often can ", "counterfeit a planet", "."))
}

@Composable
fun BoxScope.WfHugging(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x6fd8a0)
    Starfield(t)

    val x0 = 150f; val x1 = 958f; val axisY = 1214f
    fun px(au: Float) = x0 + au / 0.45f * (x1 - x0)

    val zx0 = 258f; val zx1 = 906f; val zoomY = 838f
    fun zx(au: Float) = zx0 + au / 0.06f * (zx1 - zx0)

    val draw = animate(0f, 1f, 0.8f, 2.4f, Easing.easeInOutCubic)(t)
    val zoomA = animate(0f, 1f, 2.6f, 3.6f, Easing.easeOutCubic)(t)
    val worldA = animate(0f, 1f, 3.8f, 4.6f, Easing.easeOutBack)(t)
    val appear = reveal(t, 0.5f, dur = 0.8f).opacity

    RadialDisc(x0, axisY, 122f, WF_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(x0, axisY, 36f, WF_STAR_STOPS, 0.5f, 0.42f, 0.6f)

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val edge = x0 + (x1 - x0) * draw

        drawLine(
            brush = Brush.horizontalGradient(
                0f to c(0xff7a48), 0.3f to c(0x9a6a58), 1f to c(0x38485c),
                startX = x0 * k, endX = x1 * k,
            ),
            start = Offset(x0 * k, axisY * k), end = Offset(edge * k, axisY * k),
            strokeWidth = 8f * k, cap = StrokeCap.Round,
        )

        val ha = px(0.020f) * k; val hb = px(0.040f) * k
        drawRect(
            color = accent.copy(alpha = 0.5f),
            topLeft = Offset(ha, (axisY - 34f) * k), size = Size((hb - ha).coerceAtLeast(2f * k), 68f * k),
        )

        val mx = px(0.39f)
        if (draw > 0.88f) {
            drawCircle(c(0xa89a8a), radius = 11f * k, center = Offset(mx * k, axisY * k))
        }

        if (zoomA > 0.01f) {
            drawLine(accent.copy(alpha = 0.30f * zoomA), Offset(ha, (axisY - 34f) * k), Offset(zx0 * k, (zoomY + 58f) * k), strokeWidth = 2f * k)
            drawLine(accent.copy(alpha = 0.30f * zoomA), Offset(hb, (axisY - 34f) * k), Offset(zx1 * k, (zoomY + 58f) * k), strokeWidth = 2f * k)

            drawLine(
                Color.White.copy(alpha = 0.14f * zoomA),
                Offset(zx0 * k, zoomY * k), Offset(zx1 * k, zoomY * k), strokeWidth = 6f * k, cap = StrokeCap.Round,
            )
            val za = zx(0.020f) * k; val zb = zx(0.040f) * k
            drawRect(
                color = accent.copy(alpha = 0.16f * zoomA),
                topLeft = Offset(za, (zoomY - 74f) * k), size = Size(zb - za, 148f * k),
            )
            drawLine(accent.copy(alpha = 0.6f * zoomA), Offset(za, (zoomY - 74f) * k), Offset(za, (zoomY + 74f) * k), strokeWidth = 2f * k)
            drawLine(accent.copy(alpha = 0.6f * zoomA), Offset(zb, (zoomY - 74f) * k), Offset(zb, (zoomY + 74f) * k), strokeWidth = 2f * k)
        }
    }

    if (worldA > 0.01f) {
        val wx = zx(0.030f)
        Sphere(
            42f, WF_WARM,
            modifier = Modifier.offset((wx - 21f).dp, (zoomY - 21f).dp).alpha(worldA),
        )
    }

    CenterLabel((zx(0.020f) + zx(0.040f)) / 2f, zoomY + 96f, zoomA) {
        Text("WHERE WATER COULD POOL", style = wfLabel(15f, accent))
    }
    CenterLabel(zx(0.030f), zoomY - 96f, worldA) {
        Text("A YEAR HERE: ONE WEEK", style = wfLabel(14f, WF_EMBER))
    }
    CenterLabel(px(0.39f), axisY + 48f, animate(0f, 1f, 2.4f, 3.2f, Easing.easeOutCubic)(t)) {
        Text("MERCURY · 0.39 AU", style = wfLabel(13f, c(0xa89a8a)))
    }
    CenterLabel(px(0.030f), axisY + 48f, animate(0f, 1f, 2.0f, 2.8f, Easing.easeOutCubic)(t)) {
        Text("0.03 AU", style = wfLabel(13f, accent))
    }

    val s = reveal(t, 4.8f)
    At(90f, 1432f + s.ty, s.opacity) { StatCol("0.03", " AU", "where warmth sits", 72f, accent, WF_MUTED) }
    At(430f, 1432f + s.ty, s.opacity) { StatCol("1", " wk", "a whole year there", 72f, Color.White, WF_MUTED) }
    At(772f, 1432f + s.ty, s.opacity) { StatCol("1/12", null, "of Mercury's orbit", 72f, Color.White, WF_MUTED) }

    Eyebrow("Warmth on a very short leash", accent, 200f, e1)
    Head(headline("A habitable\nzone ", "days wide", "."), 82f, 246f, e2)
    BottomLine(1648f, reveal(t, 5.8f), body("Anything living there would sit ", "locked facing the flares", "."))
}

private val WF_AQUARIUS = listOf(
    WfPoint(408f, 742f, 10f),
    WfPoint(486f, 806f, 8f),
    WfPoint(562f, 754f, 9f),
    WfPoint(500f, 890f, 7f),
    WfPoint(322f, 902f, 11f),
    WfPoint(672f, 962f, 8f),
    WfPoint(786f, 872f, 9f),
    WfPoint(258f, 1058f, 7f),
)

private val WF_AQ_LINES = listOf(0 to 1, 1 to 2, 1 to 3, 0 to 4, 3 to 5, 5 to 6, 4 to 7)

@Composable
fun BoxScope.WfOurSun(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xffd267)
    Starfield(t)

    val sx = 640f; val sy = 1080f
    val lines = animate(0f, 1f, 0.9f, 2.8f, Easing.easeInOutCubic)(t)
    val sunA = animate(0f, 1f, 3.0f, 4.0f, Easing.easeOutBack)(t)
    val ringA = animate(0f, 1f, 4.0f, 4.8f, Easing.easeOutCubic)(t)
    val appear = reveal(t, 0.6f, dur = 0.9f).opacity

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f

        WF_AQ_LINES.forEachIndexed { i, pair ->
            val on = (lines * WF_AQ_LINES.size - i).coerceIn(0f, 1f)
            if (on <= 0f) return@forEachIndexed
            val a = WF_AQUARIUS[pair.first]; val b = WF_AQUARIUS[pair.second]
            drawLine(
                c(0x8fa4c4).copy(alpha = 0.26f * on),
                Offset(a.x * k, a.y * k),
                Offset((a.x + (b.x - a.x) * on) * k, (a.y + (b.y - a.y) * on) * k),
                strokeWidth = 2f * k,
            )
        }

        WF_AQUARIUS.forEachIndexed { i, st ->
            val a = (lines * 1.6f - i * 0.05f).coerceIn(0f, 1f)
            val tw = 0.8f + 0.2f * sin(t * 1.5f + i * 2.1f)
            drawCircle(Color.White.copy(alpha = 0.14f * a), radius = st.r * 2f * k, center = Offset(st.x * k, st.y * k))
            drawCircle(Color.White.copy(alpha = (0.88f * a * tw).coerceIn(0f, 1f)), radius = st.r * 0.5f * k, center = Offset(st.x * k, st.y * k))
        }

        if (sunA > 0.01f) {
            val p = Offset(sx * k, sy * k)
            val tw = (0.7f + 0.3f * sin(t * 1.8f)).coerceIn(0f, 1f)
            drawCircle(Color(0x44ffc24a).copy(alpha = sunA), radius = 40f * k * sunA, center = p)
            drawCircle(accent.copy(alpha = sunA * tw), radius = 10f * k * sunA, center = p)
            for (i in 0..3) {
                val ang = (i * 45f) * 0.017453292f
                val len = 34f * k * sunA
                drawLine(
                    accent.copy(alpha = 0.4f * sunA * tw),
                    Offset(p.x - cos(ang) * len, p.y - sin(ang) * len),
                    Offset(p.x + cos(ang) * len, p.y + sin(ang) * len),
                    strokeWidth = 2f * k,
                )
            }
        }
        if (ringA > 0.01f) {
            val pulse = (0.55f + 0.45f * sin(t * 2.3f)).coerceIn(0f, 1f)
            drawCircle(
                accent.copy(alpha = 0.7f * ringA * pulse), radius = 62f * k * ringA,
                center = Offset(sx * k, sy * k), style = Stroke(width = 2.6f * k),
            )
        }
    }

    CenterLabel(408f, 700f, animate(0f, 1f, 2.4f, 3.2f, Easing.easeOutCubic)(t)) {
        Text("AQUARIUS", style = wfLabel(14f, WF_FAINT))
    }
    CenterLabel(sx, sy + 78f, ringA) { Text("OUR SUN", style = wfLabel(18f, accent)) }
    CenterLabel(sx, sy + 116f, animate(0f, 1f, 4.6f, 5.4f, Easing.easeOutCubic)(t)) {
        Text("magnitude 1.8", style = wfBody(22f, WF_FAINT))
    }

    val s = reveal(t, 5.0f)
    At(90f, 1428f + s.ty, s.opacity) { StatCol("1.8", null, "how bright the Sun looks", 76f, accent, WF_MUTED) }
    At(628f, 1428f + s.ty, s.opacity) { StatCol("Dubhe", null, "is about as bright", 76f, Color.White, WF_MUTED) }

    Eyebrow("The view back the other way", accent, 200f, e1)
    Head(headline("Our Sun,\njust ", "another star", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.0f), body("Nothing about that dot would tell you ", "anyone lives there", "."))
}

private val WF_FLEET = wfRandoms(0x5AB93F, 39, 3)

@Composable
fun BoxScope.WfBorg(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xa8c4e8)
    Starfield(t)

    val fx0 = 150f; val fx1 = 940f
    val fy0 = 736f; val fy1 = 1178f
    val appear = animate(0f, 1f, 0.7f, 1.8f, Easing.easeOutCubic)(t)
    val kill = animate(0f, 1f, 2.0f, 5.4f, Easing.easeInOutCubic)(t)
    val lost = interpolate(listOf(2.0f, 5.4f), listOf(0f, 39f), Easing.easeInOutCubic)(t)

    RadialDisc(540f, 1330f, 96f, WF_HALO_STOPS, 0.5f, 0.5f, 0.5f)
    RadialDisc(540f, 1330f, 26f, WF_STAR_STOPS, 0.5f, 0.42f, 0.6f)

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val gone = kill * WF_FLEET.size

        WF_FLEET.forEachIndexed { i, v ->
            val bx = fx0 + v[0] * (fx1 - fx0)
            val by = fy0 + v[1] * (fy1 - fy0)
            val drift = sin(t * 0.6f + i * 1.3f) * 7f
            val gx = (bx + drift) * k
            val gy = (by + cos(t * 0.5f + i * 0.9f) * 5f) * k
            val s = (16f + v[2] * 12f) * k
            val dead = (gone - i).coerceIn(0f, 1f)

            if (dead < 1f) {
                val a = 1f - dead
                val ship = Path().apply {
                    moveTo(gx + s * 1.25f, gy)
                    lineTo(gx - s * 0.75f, gy - s * 0.62f)
                    lineTo(gx - s * 0.34f, gy)
                    lineTo(gx - s * 0.75f, gy + s * 0.62f)
                    close()
                }
                drawPath(ship, accent.copy(alpha = 0.24f * a))
                drawPath(ship, accent.copy(alpha = 0.9f * a), style = Stroke(width = 1.8f * k))
            }
            if (dead > 0f) {
                val fade = (1f - dead).coerceIn(0f, 1f)
                val flash = if (dead < 0.35f) 1f - dead / 0.35f else 0f
                drawCircle(c(0xff9a5a).copy(alpha = 0.55f * flash), radius = s * 1.9f * flash, center = Offset(gx, gy))
                drawCircle(c(0x6a5a52).copy(alpha = 0.30f + 0.4f * fade), radius = 2.6f * k, center = Offset(gx, gy))
            }
        }
    }

    At(150f, 1244f, animate(0f, 1f, 2.4f, 3.2f, Easing.easeOutCubic)(t)) {
        Text(lost.roundToInt().toString() + " SHIPS LOST", style = wfLabel(19f, accent))
    }
    At(150f, 1284f, animate(0f, 1f, 3.0f, 3.8f, Easing.easeOutCubic)(t)) {
        Text("television, not astronomy", style = wfBody(21f, WF_FAINT))
    }
    CenterLabel(540f, 1382f, animate(0f, 1f, 5.0f, 5.8f, Easing.easeOutCubic)(t)) {
        Text("THE REAL STAR, UNBOTHERED", style = wfLabel(13f, WF_EMBER))
    }

    val s = reveal(t, 5.4f)
    At(90f, 1462f + s.ty, s.opacity) { StatCol("39", null, "ships, in the story", 74f, accent, WF_MUTED) }
    At(628f, 1462f + s.ty, s.opacity) { StatCol("1990", null, "when it aired", 74f, Color.White, WF_MUTED) }

    Eyebrow("Why the name rings a bell", accent, 200f, e1)
    Head(headline("A battle that\n", "never happened", "."), 78f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.2f), body("Star Trek wanted a real star that was ", "close, and utterly obscure", "."))
}

private class WfStop(val ly: Float, val label: String, val size: Float, val color: Color)

private val WF_LADDER = listOf(
    WfStop(0f, "OUR SUN", 22f, c(0xffcf6a)),
    WfStop(4.37f, "ALPHA CENTAURI", 16f, c(0xffcf8a)),
    WfStop(5.96f, "BARNARD'S STAR", 13f, c(0xf4794a)),
)

@Composable
fun BoxScope.WfDistance(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f)
    val accent = c(0x9cc4ec)
    Starfield(t)

    val axisY = 1240f; val x0 = 120f; val x1 = 960f
    fun px(ly: Float) = x0 + ly / 8.6f * (x1 - x0)
    val draw = animate(0f, 1f, 1.6f, 3.6f, Easing.easeInOutCubic)(t)
    val num = interpolate(listOf(1.2f, 4.0f), listOf(0f, 7.86f), Easing.easeOutCubic)(t)

    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val y = axisY * k
        val edge = x0 + (x1 - x0) * draw
        drawLine(c(0x3a4660), Offset(x0 * k, y), Offset(edge * k, y), strokeWidth = 2f * k)

        WF_LADDER.forEach { st ->
            val gx = px(st.ly)
            val on = ((edge - gx) / 60f).coerceIn(0f, 1f)
            if (on <= 0f) return@forEach
            drawCircle(st.color.copy(alpha = 0.24f * on), radius = st.size * 2f * k, center = Offset(gx * k, y))
            drawCircle(st.color.copy(alpha = on), radius = st.size * k, center = Offset(gx * k, y))
        }

        listOf(6.5f, 7.43f).forEach { ly ->
            val gx = px(ly)
            val on = ((edge - gx) / 60f).coerceIn(0f, 1f)
            if (on <= 0f) return@forEach
            drawCircle(c(0x7a6a8a).copy(alpha = 0.7f * on), radius = 7f * k, center = Offset(gx * k, y))
        }

        val ma = animate(0f, 1f, 3.2f, 4.0f, Easing.easeOutBack)(t)
        if (ma > 0.01f) {
            val pulse = (0.55f + 0.45f * sin(t * 2.4f)).coerceIn(0f, 1f)
            val mp = Offset(px(7.86f) * k, y)
            drawCircle(WF_RED.copy(alpha = 0.8f * ma * pulse), radius = 36f * k * ma, center = mp, style = Stroke(width = 3f * k))
            drawCircle(c(0xff7440), radius = 13f * k * ma, center = mp)
        }
    }

    WF_LADDER.forEach { st ->
        val la = animate(0f, 1f, 2.4f + st.ly * 0.09f, 3.2f + st.ly * 0.09f, Easing.easeOutCubic)(t)
        CenterLabel(px(st.ly), axisY + 48f, la) { Text(st.label, style = wfLabel(13f, st.color)) }
    }
    CenterLabel(px(6.97f), axisY - 88f, animate(0f, 1f, 3.4f, 4.2f, Easing.easeOutCubic)(t)) {
        Text("TWO BROWN DWARFS", style = wfLabel(12f, c(0x9a8aa8)))
    }
    CenterLabel(px(7.86f), axisY + 104f, animate(0f, 1f, 3.8f, 4.6f, Easing.easeOutCubic)(t)) {
        Text("WOLF 359", style = wfLabel(17f, WF_RED))
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
    At(84f, 630f + e2.ty, e2.opacity) { Text("light-years away.", style = wfBody(54f, Color.White)) }
    At(84f, 716f + e2.ty, e2.opacity) { Text("≈ 74 trillion km · in the constellation Leo", style = wfBody(25f, c(0x8794ac))) }

    BottomLine(1648f, reveal(t, 6.2f), body("Only Alpha Centauri and Barnard's Star are ", "closer true stars", "."))
}
