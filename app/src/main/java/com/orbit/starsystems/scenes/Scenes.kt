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
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.orbit.starsystems.core.Easing
import com.orbit.starsystems.core.PLANETS
import com.orbit.starsystems.core.Reveal
import com.orbit.starsystems.core.SceneId
import com.orbit.starsystems.core.animate
import com.orbit.starsystems.core.clamp
import com.orbit.starsystems.core.interpolate
import com.orbit.starsystems.core.reveal
import com.orbit.starsystems.ui.OrbitFont
import com.orbit.starsystems.ui.Sphere
import java.util.Locale
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

// ───────────────────────── helpers ─────────────────────────

private fun c(v: Long) = Color(v or 0xFF000000)

private fun fmt(n: Float) = String.format(Locale.US, "%,d", n.roundToInt())

private fun eyebrow(color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 26.sp,
    letterSpacing = 0.42.em, color = color,
)

private fun head(sizeSp: Float, color: Color = Color.White) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = sizeSp.sp,
    letterSpacing = (-0.02).em, lineHeight = (sizeSp * 1.06f).sp, color = color,
)

/** Places [content] at virtual coordinate (x, y) with an optional alpha. */
@Composable
private fun BoxScope.At(
    x: Float,
    y: Float,
    alpha: Float = 1f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.offset(x.dp, y.dp).alpha(alpha.coerceIn(0f, 1f))) { content() }
}

/** Cross-fades a whole scene in and out, matching the prototype's `Scene` wrapper. */
@Composable
fun BoxScope.SceneFade(t: Float, duration: Float, content: @Composable BoxScope.() -> Unit) {
    val fi = 0.7f; val fo = 0.7f
    val end = duration - fo
    val o = when {
        t < fi -> Easing.easeOutCubic(clamp(t / fi, 0f, 1f))
        t > end -> 1f - Easing.easeInCubic(clamp((t - end) / fo, 0f, 1f))
        else -> 1f
    }
    Box(Modifier.fillMaxSize().alpha(o.coerceIn(0f, 1f))) { content() }
}

/**
 * A radial-gradient disc (the Sun, halos) positioned by absolute scene coordinates.
 * Drawn on a scene-sized canvas — the circle can be far larger than the canvas (the Sun
 * is) and only the part overlapping the scene shows, which is exactly the rising limb.
 * Using a normal-sized canvas (rather than an enormous offset node) avoids the huge-layer
 * rendering glitches that shifted the disc off-centre.
 */
@Composable
private fun RadialDisc(
    cxUnits: Float,
    cyUnits: Float,
    rUnits: Float,
    stops: Array<Pair<Float, Color>>,
    centerFracX: Float,
    centerFracY: Float,
    radiusFactor: Float,
) {
    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val cx = cxUnits * k; val cy = cyUnits * k; val r = rUnits * k
        val gcx = cx + r * (2f * centerFracX - 1f)
        val gcy = cy + r * (2f * centerFracY - 1f)
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = stops,
                center = Offset(gcx, gcy),
                radius = (2f * r) * radiusFactor,
            ),
            radius = r,
            center = Offset(cx, cy),
        )
    }
}

// ───────────────────────── dispatcher ─────────────────────────

@Composable
fun BoxScope.RenderScene(scene: SceneId, t: Float, duration: Float) {
    when (scene) {
        SceneId.SIZES -> SceneSizes(t, duration)
        SceneId.SUN -> SceneSun(t, duration)
        SceneId.DISTANCE -> SceneDistance(t, duration)
        SceneId.MOON -> SceneMoon(t, duration)
        SceneId.LIGHT -> SceneLight(t, duration)
        SceneId.STAR -> SceneStar(t, duration)
        SceneId.RINGS -> SceneRings(t, duration)
        SceneId.VOLCANO -> SceneVolcano(t, duration)
        SceneId.STORM -> SceneStorm(t, duration)
        SceneId.DIAMOND -> SceneDiamond(t, duration)
        SceneId.VENUSDAY -> SceneVenusDay(t, duration)
        SceneId.URANUS -> SceneUranus(t, duration)
        SceneId.MERCURYTEMP -> SceneMercuryTemp(t, duration)
        SceneId.SATURNFLOAT -> SceneSaturnFloat(t, duration)
        SceneId.ASTEROIDBELT -> SceneAsteroidBelt(t, duration)
        SceneId.PLUTOYEAR -> ScenePlutoYear(t, duration)
    }
}

// ───────────────────── Scene 1 · relative sizes ─────────────────────

@Composable
fun BoxScope.SceneSizes(t: Float, duration: Float) = SceneFade(t, duration) {
    val scale = 26f; val gap = 16f; val baseline = 1320f
    val sizes = PLANETS.map { it.d * scale }
    val total = sizes.sum() + gap * (PLANETS.size - 1)
    var x = (1080f - total) / 2f
    data class Item(val s: Float, val cx: Float, val idx: Int)
    val items = PLANETS.indices.map { i ->
        val s = sizes[i]; val cx = x + s / 2f; x += s + gap; Item(s, cx, i)
    }
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.4f)

    // Stagger labels onto stacked rows so the tightly-clustered inner planets
    // (Mercury…Mars) don't overlap each other.
    val labelW = 96f
    val lastCenter = ArrayList<Float>()
    val labelRow = IntArray(items.size)
    items.forEach { item ->
        var r = 0
        while (r < lastCenter.size && item.cx - lastCenter[r] < labelW) r++
        if (r == lastCenter.size) lastCenter.add(item.cx) else lastCenter[r] = item.cx
        labelRow[item.idx] = r
    }

    At(90f, 220f + e1.ty, e1.opacity) {
        androidx.compose.material3.Text("THE SOLAR SYSTEM", style = eyebrow(c(0x8c8c8c)))
    }
    At(88f, 268f + e2.ty, e2.opacity) {
        androidx.compose.material3.Text(
            buildAnnotatedString {
                append("Eight worlds,\n")
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("wildly") }
                append(" different sizes.")
            },
            style = head(96f), modifier = Modifier.width(920.dp),
        )
    }
    items.forEach { (s, cx, i) ->
        val planet = PLANETS[i]
        val a = reveal(t, 1.5f + i * 0.18f, 0.7f, Easing.easeOutBack)
        val sc = 0.2f + 0.8f * a.opacity
        Sphere(
            sizeUnits = s, colors = planet.c,
            glow = if (planet.name == "JUPITER") c(0x2Ec8a072) else null,
            ring = if (planet.name == "SATURN") Color(0x8Ee0cd96) else null,
            modifier = Modifier
                .offset((cx - s / 2f).dp, (baseline - s).dp)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 1f)
                    scaleX = sc; scaleY = sc; this.alpha = a.opacity.coerceIn(0f, 1f)
                },
        )
        val la = reveal(t, 4.6f + i * 0.05f)
        At(cx - 200f, baseline + 26f + labelRow[i] * 30f, la.opacity) {
            androidx.compose.material3.Text(
                planet.name,
                style = TextStyle(
                    fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 17.sp,
                    letterSpacing = 0.18.em, color = c(0x7e7e7e), textAlign = TextAlign.Center,
                ),
                modifier = Modifier.width(400.dp),
            )
        }
    }
    At(90f, 1560f + e3.ty, e3.opacity, modifier = Modifier.width(900.dp)) {
        androidx.compose.material3.Text(
            buildAnnotatedString {
                append("Over ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("1,300") }
                append(" Earths could fit inside Jupiter alone.")
            },
            style = TextStyle(
                fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 40.sp,
                color = Color.White, lineHeight = 50.sp,
            ),
        )
    }
}

// ───────────────────── Scene 2 · the Sun ─────────────────────

@Composable
fun BoxScope.SceneSun(t: Float, duration: Float) = SceneFade(t, duration) {
    val rise = animate(80f, 0f, 0f, 2.4f, Easing.easeOutCubic)(t)
    val count = interpolate(listOf(1.6f, 4.6f), listOf(0f, 1300000f), Easing.easeOutExpo)(t)
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    // A much larger radius than the original (1750) keeps the visible limb nearly
    // horizontal across the full canvas width; `810 + sunR` keeps the limb's top edge
    // at the same height the design framed it at.
    val sunR = 3600f; val sunCx = 540f; val sunCy = 810f + sunR + rise

    RadialDisc(
        cxUnits = sunCx, cyUnits = sunCy, rUnits = sunR + 260f,
        stops = arrayOf(0f to Color(0x4Dffb240), 0.55f to Color(0x0Dff8c28), 0.7f to Color(0x00ff8c28), 1f to Color(0x00ff8c28)),
        centerFracX = 0.5f, centerFracY = 0.5f, radiusFactor = 0.5f,
    )
    RadialDisc(
        cxUnits = sunCx, cyUnits = sunCy, rUnits = sunR,
        stops = arrayOf(0f to c(0xfff7d6), 0.34f to c(0xffd451), 0.66f to c(0xff9e34), 1f to c(0xff7a1f)),
        centerFracX = 0.5f, centerFracY = 0.36f, radiusFactor = 0.66f,
    )
    At(90f, 210f + e1.ty, e1.opacity) {
        androidx.compose.material3.Text("AND THEN, THE SUN", style = eyebrow(c(0xd9a24e)))
    }
    At(90f, 300f + e2.ty, e2.opacity) {
        Box {
            androidx.compose.material3.Text(
                fmt(count),
                style = TextStyle(
                    fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 150.sp,
                    color = Color.White, letterSpacing = (-0.03).em, lineHeight = 150.sp,
                ),
            )
        }
    }
    At(90f, 470f + e2.ty, e2.opacity) {
        androidx.compose.material3.Text(
            "Earths would fit inside it.",
            style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 44.sp, color = Color.White),
        )
    }
    val ra = reveal(t, 2.2f)
    At(90f, 760f, ra.opacity) {
        Row(verticalAlignment = Alignment.Bottom) {
            PLANETS.forEach { p ->
                val s = max(2f, p.d * 2.9f)
                Sphere(sizeUnits = s, colors = p.c)
                Spacer(Modifier.width(7.dp))
            }
            androidx.compose.material3.Text(
                "← ALL EIGHT PLANETS, SAME SCALE",
                style = TextStyle(
                    fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp,
                    letterSpacing = 0.16.em, color = c(0x7e7e7e),
                ),
                modifier = Modifier.padding(start = 9.dp, bottom = 4.dp),
            )
        }
    }
}

// ───────────────────── Scene 3 · distances ─────────────────────

@Composable
fun BoxScope.SceneDistance(t: Float, duration: Float) = SceneFade(t, duration) {
    val topY = 270f; val auScale = 51f
    val draw = animate(0f, 1f, 0.8f, 5.5f, Easing.easeInOutSine)(t)
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val lineBottom = topY + 30.07f * auScale
    val drawnTo = topY + (lineBottom - topY) * draw

    At(90f, 50f + e1.ty, e1.opacity) {
        androidx.compose.material3.Text("TRUE ORBITAL DISTANCES", style = eyebrow(c(0x8c8c8c)))
    }
    At(88f, 96f + e2.ty, e2.opacity) {
        androidx.compose.material3.Text(
            buildAnnotatedString {
                append("Mostly ")
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("emptiness") }
                append(".")
            },
            style = head(88f),
        )
    }
    // spine
    At(150f, topY) {
        Box(
            Modifier
                .size(2.dp, ((lineBottom - topY) * draw).coerceAtLeast(0f).dp)
                .background(Brush.verticalGradient(listOf(c(0x666666), c(0x222222)))),
        )
    }
    Sphere(
        sizeUnits = 34f, colors = listOf(c(0xfff7d6), c(0xffc23a), c(0xff8a26)),
        glow = Color(0x80ffa03c), modifier = Modifier.offset((150f - 17f).dp, (topY - 17f).dp),
    )
    val sa = reveal(t, 1f)
    At(200f, topY - 14f, sa.opacity) {
        androidx.compose.material3.Text(
            "SUN",
            style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, letterSpacing = 0.14.em, color = c(0xd9a24e)),
        )
    }
    PLANETS.forEach { p ->
        val y = topY + p.au * auScale
        val passed = drawnTo >= y
        val a = if (passed) 1f else 0f
        val dot = max(4f, p.d * 2.0f)
        Sphere(sizeUnits = dot, colors = p.c, modifier = Modifier.offset((150f - dot / 2f).dp, (y - dot / 2f).dp).alpha(a))
        At(200f, y - 18f, a) {
            androidx.compose.material3.Text(
                p.name,
                style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, letterSpacing = 0.12.em, color = c(0xcfcfcf)),
            )
        }
        At(200f, y + 5f, a) {
            androidx.compose.material3.Text(
                String.format(Locale.US, "%.2f AU", p.au),
                style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 18.sp, color = c(0x6f6f6f)),
            )
        }
    }
    val ba = reveal(t, 6f)
    At(90f, 1850f, ba.opacity, modifier = Modifier.width(900.dp)) {
        androidx.compose.material3.Text(
            buildAnnotatedString {
                append("Sunlight takes ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("4 hours") }
                append(" to crawl out to Neptune.")
            },
            style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 34.sp, color = Color.White, lineHeight = 44.sp),
        )
    }
}

// ───────────────────── Scene 4 · Earth & Moon ─────────────────────

@Composable
fun BoxScope.SceneMoon(t: Float, duration: Float) = SceneFade(t, duration) {
    val earthCy = 250f; val earthD = 50f; val moonCy = 1660f; val moonD = 13.5f
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val gapTop = earthCy + earthD / 2f + 6f
    val gapBot = moonCy - moonD / 2f - 6f
    val sumD = PLANETS.sumOf { it.d.toDouble() }.toFloat()
    val fillScale = (gapBot - gapTop) / sumD
    var yy = gapTop
    data class S(val idx: Int, val s: Float, val cy: Float)
    val stack = PLANETS.indices.map { i ->
        val s = PLANETS[i].d * fillScale; val cy = yy + s / 2f; yy += s; S(i, s, cy)
    }

    At(90f, 86f + e1.ty, e1.opacity) {
        androidx.compose.material3.Text("EARTH & MOON · TO SCALE", style = eyebrow(c(0x8c8c8c)))
    }
    At(88f, 130f + e2.ty, e2.opacity) {
        androidx.compose.material3.Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("384,400 km") }
                append(" apart.")
            },
            style = head(70f),
        )
    }
    // dashed connector
    val ca = reveal(t, 1.4f)
    Canvas(Modifier.matchParentSize().alpha(0.5f * ca.opacity)) {
        val k = size.width / 1080f
        drawLine(
            color = c(0x555555),
            start = Offset(540f * k, earthCy * k),
            end = Offset(540f * k, moonCy * (size.height / 1920f)),
            strokeWidth = 1.4f * k,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f * k, 8f * k)),
        )
    }
    Sphere(sizeUnits = earthD, colors = listOf(c(0x9cc4ec), c(0x3d72b8), c(0x1a3360)), glow = Color(0x40508cd2), modifier = Modifier.offset((540f - earthD / 2f).dp, (earthCy - earthD / 2f).dp))
    Sphere(sizeUnits = moonD, colors = listOf(c(0xd8d4ca), c(0xa8a49a), c(0x5e5a52)), modifier = Modifier.offset((540f - moonD / 2f).dp, (moonCy - moonD / 2f).dp))
    val la = reveal(t, 1f)
    At(600f, earthCy - 14f, la.opacity) {
        androidx.compose.material3.Text("EARTH", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, letterSpacing = 0.14.em, color = c(0x9cc4ec)))
    }
    At(600f, moonCy - 14f, la.opacity) {
        androidx.compose.material3.Text("MOON", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, letterSpacing = 0.14.em, color = c(0xb9b5ab)))
    }
    stack.forEach { (i, s, cy) ->
        val a = reveal(t, 3.6f + i * 0.16f, 0.5f, Easing.easeOutCubic)
        Sphere(
            sizeUnits = s, colors = PLANETS[i].c,
            ring = if (PLANETS[i].name == "SATURN" && s > 40f) Color(0x80e0cd96) else null,
            modifier = Modifier.offset((540f - s / 2f + (1f - a.opacity) * 60f).dp, (cy - s / 2f).dp).alpha(a.opacity),
        )
    }
    val ta = reveal(t, 3.6f)
    At(90f, 1748f, ta.opacity, modifier = Modifier.width(900.dp)) {
        androidx.compose.material3.Text(
            buildAnnotatedString {
                append("Every other planet would fit in the gap — ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("with room to spare.") }
            },
            style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 34.sp, color = Color.White, lineHeight = 44.sp),
        )
    }
}

// ───────────────────── Scene 5 · speed of light ─────────────────────

@Composable
fun BoxScope.SceneLight(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val trackL = 150f; val trackR = 930f; val midY = 760f
    val px = interpolate(listOf(3.6f, 8f), listOf(trackL, trackR), Easing.linear)(t)
    val secs = interpolate(listOf(3.6f, 8f), listOf(0f, 500f), Easing.linear)(t).roundToInt()
    val mm = secs / 60; val ss = secs % 60
    val moving = t in 3.6f..8.2f

    At(90f, 230f + e1.ty, e1.opacity) {
        androidx.compose.material3.Text("THE SPEED OF LIGHT", style = eyebrow(c(0x8c8c8c)))
    }
    At(88f, 276f + e2.ty, e2.opacity) {
        androidx.compose.material3.Text(
            buildAnnotatedString {
                append("Even light\nneeds ")
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("time") }
                append(".")
            },
            style = head(84f),
        )
    }
    // track
    At(trackL, midY) {
        Box(Modifier.size((trackR - trackL).dp, 1.dp).background(c(0x3a3a3a)))
    }
    Sphere(sizeUnits = 40f, colors = listOf(c(0xfff7d6), c(0xffc23a), c(0xff8a26)), glow = Color(0x80ffa03c), modifier = Modifier.offset((trackL - 20f).dp, (midY - 20f).dp))
    Sphere(sizeUnits = 22f, colors = listOf(c(0x9cc4ec), c(0x3d72b8), c(0x1a3360)), modifier = Modifier.offset((trackR - 11f).dp, (midY - 11f).dp))
    At(trackL - 4f, midY + 28f) {
        androidx.compose.material3.Text("SUN", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, letterSpacing = 0.14.em, color = c(0xd9a24e)))
    }
    At(trackR - 30f, midY + 28f) {
        androidx.compose.material3.Text("EARTH", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, letterSpacing = 0.14.em, color = c(0x9cc4ec)))
    }
    if (moving) {
        At(trackL, midY - 1.5f) {
            Box(Modifier.size((px - trackL).coerceAtLeast(0f).dp, 3.dp).background(Brush.horizontalGradient(listOf(Color(0x00FFFFFF), Color(0xD9FFFFFF)))))
        }
        Sphere(sizeUnits = 14f, colors = listOf(Color.White, Color.White, Color.White), glow = Color(0xE6FFFFFF), modifier = Modifier.offset((px - 7f).dp, (midY - 7f).dp))
    }
    val ka = reveal(t, 3.2f)
    At(90f, midY + 130f, ka.opacity) {
        androidx.compose.material3.Text(
            String.format(Locale.US, "%d:%02d", mm, ss),
            style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 130.sp, color = Color.White, letterSpacing = (-0.02).em, lineHeight = 130.sp),
        )
    }
    At(90f, midY + 290f, ka.opacity) {
        androidx.compose.material3.Text(
            "for sunlight to reach Earth.",
            style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 36.sp, color = Color.White),
        )
    }
    val ba = reveal(t, 7.4f)
    At(90f, 1700f, ba.opacity, modifier = Modifier.width(900.dp)) {
        androidx.compose.material3.Text(
            buildAnnotatedString {
                append("The light on your skin right now left the Sun ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) { append("8 minutes ago.") }
            },
            style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 34.sp, color = c(0xbdbdbd), lineHeight = 44.sp),
        )
    }
}

// ───────────────────── Scene 6 · nearest star ─────────────────────

@Composable
fun BoxScope.SceneStar(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f)
    val ssScale = animate(1f, 0.04f, 0.4f, 3f, Easing.easeInOutCubic)(t)
    val proxima = reveal(t, 3.4f, 1.2f)
    val endLine = reveal(t, 5.6f, 1.0f)

    At(90f, 220f + e1.ty, e1.opacity) {
        androidx.compose.material3.Text("THE NEAREST STAR", style = eyebrow(c(0xc86a44)))
    }
    At(88f, 266f + e2.ty, e2.opacity) {
        androidx.compose.material3.Text("Proxima Centauri", style = head(92f))
    }
    // our Sun, shrinking to a speck
    val sunSize = 120f * ssScale
    Sphere(sizeUnits = sunSize, colors = listOf(c(0xfff7d6), c(0xffc23a), c(0xff8a26)), glow = Color(0x80ffa03c), modifier = Modifier.offset((200f - sunSize / 2f).dp, (720f - sunSize / 2f).dp))
    val ola = reveal(t, 2.6f)
    At(222f, 706f, ola.opacity) {
        androidx.compose.material3.Text("OUR SUN", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, letterSpacing = 0.14.em, color = c(0xd9a24e)))
    }
    // Proxima far bottom-right
    Sphere(sizeUnits = 26f, colors = listOf(c(0xffd9b0), c(0xe0744a), c(0x7a2c14)), glow = Color(0x80e0744a), modifier = Modifier.offset((820f - 13f).dp, (1180f - 13f).dp).alpha(proxima.opacity))
    At(660f, 1206f, proxima.opacity, modifier = Modifier.width(200.dp)) {
        androidx.compose.material3.Text("PROXIMA CENTAURI", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, letterSpacing = 0.12.em, color = c(0xd98a64), textAlign = TextAlign.End))
    }
    // distance callout
    At(90f, 880f, proxima.opacity) {
        androidx.compose.material3.Text("4.24", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 116.sp, color = Color.White, letterSpacing = (-0.02).em, lineHeight = 116.sp))
    }
    At(90f, 1010f, proxima.opacity) {
        androidx.compose.material3.Text("light-years away", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 40.sp, color = Color.White))
    }
    At(90f, 1078f, proxima.opacity) {
        androidx.compose.material3.Text("≈ 40,000,000,000,000 km", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 26.sp, color = c(0x7e7e7e)))
    }
    At(90f, 1600f + endLine.ty, endLine.opacity, modifier = Modifier.width(900.dp)) {
        androidx.compose.material3.Text(
            buildAnnotatedString {
                append("You are tiny.\n")
                withStyle(SpanStyle(color = c(0x8c8c8c))) { append("And you get to witness all of it.") }
            },
            style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 46.sp, color = Color.White, lineHeight = 56.sp),
        )
    }
}

// ═══════════════════ Worlds Up Close — shared helpers ═══════════════════

/** CSS-style camera: scales the scene around an absolute (cx, cy) pivot point. */
@Composable
private fun BoxScope.Camera(scale: Float, cx: Float, cy: Float, content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier.matchParentSize().graphicsLayer {
            scaleX = scale; scaleY = scale
            transformOrigin = TransformOrigin(cx / 1080f, cy / 1920f)
        },
    ) { content() }
}

/** A stroked ring / orbit ellipse in scene units; optional dash and clip-to-below-[clipTop]. */
@Composable
private fun RingArc(
    cx: Float, cy: Float, w: Float, h: Float, deg: Float, color: Color, bw: Float,
    dash: Boolean = false, clipTop: Float? = null,
) {
    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val stroke = if (dash) Stroke(bw * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f * k, 12f * k))) else Stroke(bw * k)
        val oval = {
            rotate(deg, pivot = Offset(cx * k, cy * k)) {
                drawOval(color = color, topLeft = Offset((cx - w / 2f) * k, (cy - h / 2f) * k), size = Size(w * k, h * k), style = stroke)
            }
        }
        if (clipTop != null) clipRect(top = clipTop * k) { oval() } else oval()
    }
}

@Composable
private fun BoxScope.Eyebrow(text: String, color: Color, y: Float, r: Reveal) {
    At(90f, y + r.ty, r.opacity) { Text(text.uppercase(), style = eyebrow(color)) }
}

@Composable
private fun BoxScope.Head(text: AnnotatedString, sizeSp: Float, y: Float, r: Reveal) {
    At(88f, y + r.ty, r.opacity, modifier = Modifier.width(920.dp)) { Text(text, style = head(sizeSp)) }
}

@Composable
private fun BoxScope.BottomLine(y: Float, r: Reveal, text: AnnotatedString, color: Color = Color.White) {
    At(90f, y + r.ty, r.opacity, modifier = Modifier.width(900.dp)) {
        Text(text, style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 34.sp, color = color, lineHeight = 44.sp))
    }
}

/** A big-number + caption stat column (number may carry a smaller trailing unit). */
@Composable
private fun StatCol(value: String, unit: String?, label: String, valueSize: Float, numColor: Color, labelColor: Color) {
    Column {
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = valueSize.sp, color = numColor)) { append(value) }
                if (unit != null) withStyle(SpanStyle(fontWeight = FontWeight.Light, fontSize = (valueSize * 0.44f).sp, color = numColor)) { append(unit) }
            },
            style = TextStyle(fontFamily = OrbitFont, lineHeight = valueSize.sp),
        )
        Text(label, style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 22.sp, color = labelColor), modifier = Modifier.padding(top = 4.dp))
    }
}

/** A small eyebrow label sitting above a big number (Venus / Mercury read-outs). */
@Composable
private fun LabeledStat(top: String, topColor: Color, value: String, unit: String?, sub: String?, valueSize: Float) {
    Column {
        Text(top, style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, letterSpacing = 0.16.em, color = topColor))
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = valueSize.sp, color = Color.White)) { append(value) }
                if (unit != null) withStyle(SpanStyle(fontWeight = FontWeight.Light, fontSize = (valueSize * 0.42f).sp, color = Color.White)) { append(unit) }
            },
            style = TextStyle(fontFamily = OrbitFont, lineHeight = valueSize.sp),
        )
        if (sub != null) Text(sub, style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 20.sp, color = c(0x9a9078)))
    }
}

@Composable
private fun BoxScope.CenterLabel(cx: Float, y: Float, alpha: Float, content: @Composable () -> Unit) {
    At(cx - 200f, y, alpha) {
        Column(Modifier.width(400.dp), horizontalAlignment = Alignment.CenterHorizontally) { content() }
    }
}

/** Builds a two-weight headline: [plain] then [emph] in semibold. */
private fun headline(plain: String, emph: String, tail: String = ""): AnnotatedString = buildAnnotatedString {
    append(plain)
    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(emph) }
    if (tail.isNotEmpty()) append(tail)
}

private fun body(plain: String, bold: String, tail: String = ""): AnnotatedString = buildAnnotatedString {
    append(plain)
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(bold) }
    if (tail.isNotEmpty()) append(tail)
}

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
    val dist = hypot(cx - sunX, cy - sunY)
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
