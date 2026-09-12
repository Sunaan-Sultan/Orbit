package com.orbit.starsystems.scenes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.orbit.starsystems.core.Easing
import com.orbit.starsystems.core.animate
import com.orbit.starsystems.core.reveal
import com.orbit.starsystems.ui.OrbitFont
import com.orbit.starsystems.ui.Starfield
import kotlin.math.cos
import kotlin.math.sin

private const val SPX_TAU = 6.2831855f

private val SPX_MUTED = c(0x9a9484)

private fun spxLabel(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = size.sp,
    letterSpacing = 0.14.em, color = color,
)

private fun spxBody(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = size.sp, color = color,
)

private fun spxRnd(seed: Int, count: Int, each: Int): List<FloatArray> {
    var s = seed
    fun next(): Float {
        s = s * 1664525 + 1013904223
        return ((s ushr 9) and 0xFFFF) / 65535f
    }
    return List(count) { FloatArray(each) { next() } }
}

@Composable
fun BoxScope.SpTitan(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xf0b45a)

    val appear = reveal(t, 0.5f, dur = 1.1f).opacity
    val lakesA = animate(0f, 1f, 2.4f, 4.2f, Easing.easeOutCubic)(t)
    val rainA = animate(0f, 1f, 3.4f, 5.0f, Easing.easeOutCubic)(t)

    val cx = 540f; val cy = 960f; val r = 300f
    val drops = spxRnd(0x7A1D55, 3, 30)

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        val rk = r * k

        // The thick nitrogen haze that hides the surface from ordinary cameras.
        drawCircle(
            brush = Brush.radialGradient(0f to c(0xffcf82).copy(alpha = 0.44f), 1f to Color.Transparent, center = ctr, radius = rk * 1.55f),
            radius = rk * 1.55f, center = ctr,
        )
        drawCircle(
            brush = Brush.radialGradient(
                0f to c(0xffd68f), 0.45f to c(0xe09a3c), 0.82f to c(0xa25f1c), 1f to c(0x53300c),
                center = Offset(ctr.x - rk * 0.26f, ctr.y - rk * 0.28f), radius = rk * 1.25f,
            ),
            radius = rk, center = ctr,
        )

        // Northern seas — Kraken and its neighbours, dark against the orange.
        if (lakesA > 0.01f) {
            val seas = listOf(
                Triple(-0.30f, -0.46f, 0.20f),
                Triple(0.10f, -0.56f, 0.13f),
                Triple(0.30f, -0.34f, 0.10f),
                Triple(-0.04f, -0.30f, 0.08f),
            )
            seas.forEachIndexed { i, (sx, sy, sr) ->
                val on = (lakesA * 4f - i).coerceIn(0f, 1f)
                if (on <= 0f) return@forEachIndexed
                drawCircle(
                    c(0x241a10).copy(alpha = 0.82f * on),
                    radius = rk * sr * on,
                    center = Offset(ctr.x + rk * sx, ctr.y + rk * sy),
                )
            }
        }

        // Shadow on the far limb so the globe reads as a sphere.
        drawCircle(
            brush = Brush.radialGradient(
                0f to Color.Transparent, 0.66f to Color.Transparent, 1f to c(0x1c0e04).copy(alpha = 0.72f),
                center = Offset(ctr.x - rk * 0.26f, ctr.y - rk * 0.28f), radius = rk * 1.32f,
            ),
            radius = rk, center = ctr,
        )
    }

    // Methane rain, falling slowly in the low gravity.
    Canvas(Modifier.fillMaxSize().alpha(rainA)) {
        val k = size.width / 1080f
        drops[0].indices.forEach { i ->
            val x = 120f + drops[0][i] * 840f
            val span = 300f + drops[1][i] * 260f
            val y = 470f + ((t * (44f + drops[2][i] * 30f) + drops[1][i] * span) % span)
            drawLine(
                c(0xffd9a0).copy(alpha = 0.34f + drops[2][i] * 0.24f),
                Offset(x * k, y * k), Offset(x * k, (y + 20f + drops[2][i] * 14f) * k),
                strokeWidth = 1.8f * k, cap = StrokeCap.Round,
            )
        }
    }

    CenterLabel(cx - 110f, cy - 190f, lakesA) { Text("SEAS OF METHANE", style = spxLabel(15f, c(0xffe0b0))) }
    CenterLabel(cx, cy + r + 40f, appear) { Text("TITAN · MOON OF SATURN", style = spxLabel(16f, accent)) }

    val s = reveal(t, 5.4f)
    At(90f, 1420f + s.ty, s.opacity) { StatCol("−179", " °C", "at the surface", 62f, Color.White, SPX_MUTED) }
    At(470f, 1420f + s.ty, s.opacity) { StatCol("1.45", "×", "Earth's air pressure", 62f, accent, SPX_MUTED) }

    Eyebrow("Titan · rivers that are not water", accent, 200f, e1)
    Head(headline("It rains here.\nJust not ", "water", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.0f), body("Titan is the only world besides Earth with ", "rivers, rain and seas on its surface", " — all of them liquid methane."))
}

@Composable
fun BoxScope.SpOort(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0x9cc4ec)
    Starfield(t)

    val shellA = animate(0f, 1f, 1.0f, 3.6f, Easing.easeOutCubic)(t)
    val innerA = animate(0f, 1f, 2.4f, 3.8f, Easing.easeOutCubic)(t)
    val probeA = animate(0f, 1f, 4.0f, 5.4f, Easing.easeOutCubic)(t)

    val cx = 540f; val cy = 980f
    val ice = spxRnd(0x3C99B1, 3, 150)

    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)

        // A hollow shell of comets, thinning towards the middle.
        ice[0].indices.forEach { i ->
            val ang = ice[0][i] * SPX_TAU
            val rad = (250f + ice[1][i] * 210f) * shellA
            val tw = 0.5f + 0.5f * sin(t * 1.1f + i * 0.7f)
            drawCircle(
                accent.copy(alpha = (0.22f + ice[2][i] * 0.5f) * shellA * tw),
                radius = (1.2f + ice[2][i] * 2.2f) * k,
                center = Offset(ctr.x + cos(ang) * rad * k, ctr.y + sin(ang) * rad * 0.86f * k),
            )
        }

        if (innerA > 0.01f) {
            drawCircle(
                c(0x4e5c70).copy(alpha = 0.5f * innerA), radius = 92f * k, center = ctr,
                style = Stroke(width = 1.8f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f * k, 10f * k))),
            )
            drawCircle(
                brush = Brush.radialGradient(0f to Color(0x66ffc24a), 1f to Color.Transparent, center = ctr, radius = 60f * k),
                radius = 60f * k, center = ctr,
            )
            drawCircle(c(0xffd267).copy(alpha = innerA), radius = 12f * k, center = ctr)
        }

        if (probeA > 0.01f) {
            val px = ctr.x + 150f * k * probeA
            val py = ctr.y - 86f * k * probeA
            drawLine(
                Color.White.copy(alpha = 0.5f * probeA), ctr, Offset(px, py),
                strokeWidth = 1.8f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f * k, 9f * k)),
            )
            drawCircle(Color.White.copy(alpha = probeA), radius = 5.5f * k, center = Offset(px, py))
        }
    }

    CenterLabel(cx + 150f, cy - 128f, probeA) { Text("VOYAGER 1, TODAY", style = spxLabel(13f, Color.White)) }
    CenterLabel(cx, cy + 118f, innerA) { Text("EVERYTHING YOU KNOW", style = spxLabel(13f, c(0xffc24a))) }
    CenterLabel(cx, cy + 470f, shellA) { Text("THE OORT CLOUD", style = spxLabel(17f, accent)) }

    val s = reveal(t, 5.4f)
    At(90f, 1420f + s.ty, s.opacity) { StatCol("1.5", " ly", "to the far edge", 64f, accent, SPX_MUTED) }
    At(470f, 1420f + s.ty, s.opacity) { StatCol("30,000", " yr", "for Voyager to cross", 64f, Color.White, SPX_MUTED) }

    Eyebrow("The true edge of our system", accent, 200f, e1)
    Head(headline("The Solar System\nis ", "mostly empty shell", "."), 72f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.2f), body("Beyond the planets lies a sphere of icy bodies so vast that Voyager 1 needs ", "three hundred years just to reach its inner edge", "."))
}

@Composable
fun BoxScope.SpGwave(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xb08aff)

    val appear = reveal(t, 0.5f, dur = 1.0f).opacity
    val chirpA = animate(0f, 1f, 3.2f, 4.6f, Easing.easeOutCubic)(t)

    val cx = 540f; val cy = 900f
    // Two black holes spiralling in, then a ringdown.
    val inspiral = ((t - 0.8f) / 3.4f).coerceIn(0f, 1f)
    val sep = 200f * (1f - inspiral) + 22f
    val spin = t * (2.2f + inspiral * 9f)
    val merged = inspiral >= 1f

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)

        // Outgoing ripples in the shape of spacetime itself.
        for (w in 0 until 5) {
            val phase = (t * 0.42f + w * 0.2f) % 1f
            val rr = (120f + phase * 420f) * k
            val a = (1f - phase) * 0.5f * (0.4f + inspiral * 0.6f)
            drawCircle(
                accent.copy(alpha = a), radius = rr, center = ctr,
                style = Stroke(width = 3f * k),
            )
            drawCircle(
                accent.copy(alpha = a * 0.4f), radius = rr * 1.04f, center = ctr,
                style = Stroke(width = 1.6f * k),
            )
        }

        if (!merged) {
            listOf(0f, SPX_TAU / 2f).forEach { off ->
                val ang = spin + off
                val p = Offset(ctr.x + cos(ang) * sep * k, ctr.y + sin(ang) * sep * 0.5f * k)
                drawCircle(
                    brush = Brush.radialGradient(0f to c(0xd8b8ff).copy(alpha = 0.6f), 1f to Color.Transparent, center = p, radius = 46f * k),
                    radius = 46f * k, center = p,
                )
                drawCircle(c(0x05030a), radius = 21f * k, center = p)
                drawCircle(accent.copy(alpha = 0.85f), radius = 21f * k, center = p, style = Stroke(width = 2.4f * k))
            }
        } else {
            val flash = (1f - ((t - 4.2f) / 0.8f).coerceIn(0f, 1f))
            drawCircle(
                brush = Brush.radialGradient(0f to Color.White.copy(alpha = 0.8f * flash), 1f to Color.Transparent, center = ctr, radius = 150f * k),
                radius = 150f * k, center = ctr,
            )
            drawCircle(c(0x05030a), radius = 34f * k, center = ctr)
            drawCircle(accent, radius = 34f * k, center = ctr, style = Stroke(width = 3f * k))
        }
    }

    // The chirp: the actual shape of the signal, rising in pitch then cutting out.
    Canvas(Modifier.fillMaxSize().alpha(chirpA)) {
        val k = size.width / 1080f
        val l = 130f * k; val rgt = 950f * k; val y = 1266f * k
        val path = Path()
        var x = 0f
        var first = true
        while (x <= 1f) {
            val freq = 6f + x * x * 90f
            val amp = (10f + x * 44f) * k * (if (x > 0.93f) (1f - x) / 0.07f else 1f)
            val px = l + (rgt - l) * x
            val py = y + sin(x * freq) * amp
            if (first) { path.moveTo(px, py); first = false } else path.lineTo(px, py)
            x += 0.002f
        }
        drawPath(path, color = accent.copy(alpha = 0.92f), style = Stroke(width = 2.6f * k, cap = StrokeCap.Round))
    }

    CenterLabel(cx, 1352f, chirpA) { Text("THE CHIRP · 0.2 SECONDS", style = spxLabel(15f, accent)) }

    val s = reveal(t, 5.4f)
    At(90f, 1424f + s.ty, s.opacity) { StatCol("2015", null, "first detection", 64f, Color.White, SPX_MUTED) }
    At(470f, 1424f + s.ty, s.opacity) { StatCol("1.3", " bn ly", "away", 64f, accent, SPX_MUTED) }

    Eyebrow("Gravitational waves", accent, 200f, e1)
    Head(headline("Two black holes\nrang ", "spacetime", "."), 84f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.2f), body("The wave stretched a four-kilometre detector by ", "less than a ten-thousandth of a proton", " — and we caught it."))
}

@Composable
fun BoxScope.SpOumuamua(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xd8c4a0)
    Starfield(t)

    val appear = reveal(t, 0.6f, dur = 1.0f).opacity
    val pathA = animate(0f, 1f, 1.0f, 5.6f, Easing.easeInOutCubic)(t)
    val labelA = animate(0f, 1f, 4.4f, 5.4f, Easing.easeOutCubic)(t)

    val cx = 540f; val cy = 1010f

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)

        drawCircle(
            brush = Brush.radialGradient(0f to Color(0x55ffc24a), 1f to Color.Transparent, center = ctr, radius = 96f * k),
            radius = 96f * k, center = ctr,
        )
        drawCircle(c(0xffd267), radius = 24f * k, center = ctr)

        // A hyperbolic pass: in from nowhere, whipped round the Sun, gone forever.
        val path = Path()
        var first = true
        var u = -1.25f
        while (u <= 1.25f) {
            val px = ctr.x + u * 470f * k
            val py = ctr.y - (170f / (0.34f + u * u) - 170f / 1.9f) * k
            if (first) { path.moveTo(px, py); first = false } else path.lineTo(px, py)
            u += 0.02f
        }
        drawPath(
            path, color = c(0x55627a).copy(alpha = 0.6f),
            style = Stroke(width = 2f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f * k, 12f * k))),
        )

        val u0 = -1.25f + 2.5f * pathA
        val ox = ctr.x + u0 * 470f * k
        val oy = ctr.y - (170f / (0.34f + u0 * u0) - 170f / 1.9f) * k

        val trail = Path()
        var tu = -1.25f
        var tFirst = true
        while (tu <= u0) {
            val px = ctr.x + tu * 470f * k
            val py = ctr.y - (170f / (0.34f + tu * tu) - 170f / 1.9f) * k
            if (tFirst) { trail.moveTo(px, py); tFirst = false } else trail.lineTo(px, py)
            tu += 0.02f
        }
        drawPath(trail, color = accent.copy(alpha = 0.85f), style = Stroke(width = 3f * k, cap = StrokeCap.Round))

        // A tumbling splinter, several times longer than it is wide.
        rotate(t * 108f, pivot = Offset(ox, oy)) {
            drawRoundRect(
                color = c(0xe8d8b8),
                topLeft = Offset(ox - 42f * k, oy - 8f * k),
                size = androidx.compose.ui.geometry.Size(84f * k, 16f * k),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f * k, 8f * k),
            )
        }
    }

    CenterLabel(cx, cy + 62f, appear) { Text("THE SUN", style = spxLabel(14f, c(0xffc24a))) }
    CenterLabel(cx, 1348f, labelA) { Text("NEVER COMING BACK", style = spxLabel(17f, accent)) }

    val s = reveal(t, 5.4f)
    At(90f, 1424f + s.ty, s.opacity) { StatCol("2017", null, "first of its kind", 64f, accent, SPX_MUTED) }
    At(470f, 1424f + s.ty, s.opacity) { StatCol("87", " km/s", "past the Sun", 64f, Color.White, SPX_MUTED) }

    Eyebrow("1I/ʻOumuamua", accent, 200f, e1)
    Head(headline("Something fell in\nfrom ", "another star", "."), 78f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.2f), body("It arrived on a path the Sun could never have bent, tumbled past in weeks, and left — ", "the first interstellar visitor we ever caught", "."))
}

@Composable
fun BoxScope.SpCmb(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val accent = c(0xffb0c8)

    val appear = reveal(t, 0.5f, dur = 1.2f).opacity
    val scaleA = animate(0f, 1f, 2.8f, 4.6f, Easing.easeOutCubic)(t)

    val blobs = spxRnd(0x6BD2A7, 4, 46)
    val cy = 900f

    Canvas(Modifier.fillMaxSize().alpha(appear)) {
        val k = size.width / 1080f
        val ctr = Offset(540f * k, cy * k)
        val rk = 320f * k

        // The all-sky map: hot and cold patches a hundred-thousandth of a degree apart.
        blobs[0].indices.forEach { i ->
            val ang = blobs[0][i] * SPX_TAU
            val rad = blobs[1][i] * 0.98f
            val px = ctr.x + cos(ang) * rad * rk
            val py = ctr.y + sin(ang) * rad * rk
            val hot = blobs[2][i] > 0.5f
            val br = (44f + blobs[3][i] * 92f) * k
            val drift = 0.82f + 0.18f * sin(t * 0.5f + i * 0.9f)
            drawCircle(
                brush = Brush.radialGradient(
                    0f to (if (hot) c(0xff7a9c) else c(0x5a8cff)).copy(alpha = 0.5f * drift),
                    1f to Color.Transparent,
                    center = Offset(px, py), radius = br,
                ),
                radius = br, center = Offset(px, py),
            )
        }

        drawCircle(Color.White.copy(alpha = 0.16f), radius = rk, center = ctr, style = Stroke(width = 2f * k))
    }

    // A timeline pinning the light to the first 380,000 years.
    Canvas(Modifier.fillMaxSize().alpha(scaleA)) {
        val k = size.width / 1080f
        val l = 130f * k; val rgt = 950f * k; val y = 1290f * k
        drawLine(c(0x3c4658), Offset(l, y), Offset(rgt, y), strokeWidth = 3f * k, cap = StrokeCap.Round)
        val mark = l + (rgt - l) * 0.03f
        drawLine(accent, Offset(l, y), Offset(mark, y), strokeWidth = 7f * k, cap = StrokeCap.Round)
        drawCircle(accent, radius = 9f * k, center = Offset(mark, y))
        drawCircle(Color.White, radius = 7f * k, center = Offset(rgt, y))
    }

    CenterLabel(210f, 1322f, scaleA) { Text("380,000 YEARS", style = spxLabel(13f, accent)) }
    CenterLabel(880f, 1322f, scaleA) { Text("TODAY", style = spxLabel(13f, Color.White)) }
    CenterLabel(540f, 1216f, scaleA) { Text("13.8 billion years of everything since", style = spxBody(19f, SPX_MUTED)) }

    val s = reveal(t, 5.4f)
    At(90f, 1416f + s.ty, s.opacity) { StatCol("2.725", " K", "its temperature now", 60f, accent, SPX_MUTED) }
    At(500f, 1416f + s.ty, s.opacity) { StatCol("13.8", " bn yr", "since it set out", 60f, Color.White, SPX_MUTED) }

    Eyebrow("The cosmic microwave background", accent, 200f, e1)
    Head(headline("The oldest light\nthere ", "is", "."), 86f, 246f, e2)
    BottomLine(1648f, reveal(t, 6.2f), body("It has been travelling since the universe first turned clear, and it is ", "falling on you right now, from every direction", "."))
}
