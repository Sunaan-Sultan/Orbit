package com.orbit.starsystems.scenes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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
import com.orbit.starsystems.core.SceneId
import com.orbit.starsystems.core.animate
import com.orbit.starsystems.core.clamp
import com.orbit.starsystems.core.interpolate
import com.orbit.starsystems.core.reveal
import com.orbit.starsystems.ui.OrbitFont
import com.orbit.starsystems.ui.Sphere
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

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

/** A radial-gradient disc (the Sun, halos) drawn so the gradient centre can be offset. */
@Composable
private fun RadialDisc(
    dUnits: Float,
    stops: Array<Pair<Float, Color>>,
    centerFracX: Float,
    centerFracY: Float,
    radiusFactor: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.size(dUnits.dp)) {
        val w = size.width
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = stops,
                center = Offset(w * centerFracX, w * centerFracY),
                radius = w * radiusFactor,
            ),
            radius = w / 2f,
            center = Offset(w / 2f, w / 2f),
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
        At(cx - 200f, baseline + 26f, la.opacity) {
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
    val sunR = 1750f; val sunCx = 540f; val sunCy = 2560f + rise

    RadialDisc(
        dUnits = (sunR + 260f) * 2f,
        stops = arrayOf(0f to Color(0x4Dffb240), 0.55f to Color(0x0Dff8c28), 0.7f to Color(0x00ff8c28), 1f to Color(0x00ff8c28)),
        centerFracX = 0.5f, centerFracY = 0.5f, radiusFactor = 0.5f,
        modifier = Modifier.offset((sunCx - sunR - 260f).dp, (sunCy - sunR - 260f).dp),
    )
    RadialDisc(
        dUnits = sunR * 2f,
        stops = arrayOf(0f to c(0xfff7d6), 0.34f to c(0xffd451), 0.66f to c(0xff9e34), 1f to c(0xff7a1f)),
        centerFracX = 0.5f, centerFracY = 0.36f, radiusFactor = 0.66f,
        modifier = Modifier.offset((sunCx - sunR).dp, (sunCy - sunR).dp),
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
    val topY = 230f; val auScale = 51f
    val draw = animate(0f, 1f, 0.8f, 5.5f, Easing.easeInOutSine)(t)
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.8f)
    val lineBottom = topY + 30.07f * auScale
    val drawnTo = topY + (lineBottom - topY) * draw

    At(90f, 150f + e1.ty, e1.opacity) {
        androidx.compose.material3.Text("TRUE ORBITAL DISTANCES", style = eyebrow(c(0x8c8c8c)))
    }
    At(88f, 196f + e2.ty, e2.opacity) {
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
    At(90f, 1760f, ba.opacity, modifier = Modifier.width(900.dp)) {
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
