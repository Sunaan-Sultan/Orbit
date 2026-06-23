package com.orbit.starsystems.scenes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
import kotlin.math.sin

private val SIR_A = listOf(c(0xe9f2ff), c(0xb7d4f5), c(0x6f93c4))
private val SIR_A_GLOW = Color(0xA696C3FF)
private val SIR_B = listOf(c(0xffffff), c(0xdbe8ff), c(0x9fb9dd))

private fun sirLabel(size: Float, color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = size.sp, letterSpacing = 0.12.em, color = color,
)

// ───────────────────── 1 · The brightest star ─────────────────────

@Composable
fun BoxScope.SirBrightest(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 5.5f)
    val cx = 540f; val cy = 760f
    val grow = animate(0f, 1f, 0.4f, 2.2f, Easing.easeOutBack)(t)
    val tw = 0.82f + 0.18f * sin(t * 3.0f)
    val spin = t * 7f
    Eyebrow("Sirius · the brightest star", c(0x8fc0ff), 150f, e1)
    Head(headline("The brightest\nstar in our ", "sky."), 86f, 196f, e2)
    // rotating diffraction spikes
    Canvas(Modifier.fillMaxSize().alpha(grow.coerceIn(0f, 1f))) {
        val k = size.width / 1080f
        val ctr = Offset(cx * k, cy * k)
        val spikes = listOf(Triple(0f, 760f * tw, 6f), Triple(90f, 760f * tw, 6f), Triple(45f, 520f * tw, 3.5f), Triple(135f, 520f * tw, 3.5f))
        spikes.forEach { (rot, len, w) ->
            rotate(spin + rot, pivot = ctr) {
                val ww = w * k; val hh = len * k
                drawRect(
                    brush = Brush.verticalGradient(listOf(Color(0x00C8E1FF), Color(0xE6C8E1FF), Color(0x00C8E1FF)), startY = ctr.y - hh / 2f, endY = ctr.y + hh / 2f),
                    topLeft = Offset(ctr.x - ww / 2f, ctr.y - hh / 2f), size = Size(ww, hh),
                )
            }
        }
    }
    Sphere(210f * grow, SIR_A, glow = SIR_A_GLOW, modifier = Modifier.offset((cx - 105f * grow).dp, (cy - 105f * grow).dp))
    CenterLabel(cx, cy + 158f, reveal(t, 2.4f).opacity) {
        Text("APPARENT MAGNITUDE −1.46", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = 0.16.em, color = c(0xbcd6ff)))
    }
    val others = listOf(Triple("Canopus", "−0.74", Offset(220f, 1190f)), Triple("Arcturus", "−0.05", Offset(540f, 1320f)), Triple("Vega", "0.03", Offset(860f, 1190f)))
    others.forEachIndexed { i, (name, mag, p) ->
        val a = reveal(t, 3.4f + i * 0.3f)
        At(p.x - 100f, p.y - 30f, a.opacity) {
            Column(Modifier.size(200.dp, 80.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(13.dp).clip(CircleShape).background(c(0xcdddf5)))
                Text(name, style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color.White), modifier = Modifier.padding(top = 9.dp))
                Text("mag $mag", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 14.sp, color = c(0x8aa0bf)), modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
    BottomLine(1640f, e3, body("No other star comes close — Sirius blazes ", "nearly twice as bright", " as the next, Canopus."))
}

// ───────────────────── 2 · A hidden companion ─────────────────────

@Composable
fun BoxScope.SirBinary(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val zoom = animate(1.3f, 0.96f, 0.5f, 5f, Easing.easeOutCubic)(t)
    val cx = 540f; val cy = 1120f; val r = 300f; val sq = 0.5f; val tilt = -18f
    val ang = t * 0.9f
    val rad = tilt * 0.017453292f
    val bx = cx + (cos(ang) * r) * cos(rad) - (sin(ang) * r * sq) * sin(rad)
    val by = cy + (cos(ang) * r) * sin(rad) + (sin(ang) * r * sq) * cos(rad)
    val bIn = reveal(t, 2.2f, 1.0f)
    Camera(zoom, cx, cy) {
        RingArc(cx, cy, r * 2f, r * 2f * sq, tilt, Color(0x4Db4cdff), 1f, dash = true, alpha = bIn.opacity)
        Sphere(180f, SIR_A, glow = SIR_A_GLOW, modifier = Modifier.offset((cx - 90f).dp, (cy - 90f).dp))
        At(bx - 17f, by - 17f, bIn.opacity) { Sphere(34f, SIR_B, glow = Color(0xBFcde1ff)) }
        CenterLabel(cx, cy + 116f, reveal(t, 1.4f).opacity) { Text("SIRIUS A", style = sirLabel(20f, c(0xbcd6ff))) }
        CenterLabel(bx, by - 42f, bIn.opacity) { Text("SIRIUS B · white dwarf", style = sirLabel(15f, Color.White)) }
    }
    Eyebrow("A hidden companion", c(0x8fc0ff), 200f, e1)
    Head(headline("Not one star,\n", "but two."), 88f, 246f, e2)
    BottomLine(1640f, e3, body("A faint white dwarf circles Sirius A every ", "50 years", " — first betrayed in 1844 by a wobble in the bright star's path."))
}

// ───────────────────── 3 · Bigger, hotter, brighter ─────────────────────

@Composable
fun BoxScope.SirHotter(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 5f)
    val cy = 700f
    val grow = animate(0f, 1f, 0.5f, 2.2f, Easing.easeOutCubic)(t)
    data class RowDef(val l: String, val v: String, val frac: Float)
    val rows = listOf(RowDef("Mass", "2.0×", 0.42f), RowDef("Radius", "1.7×", 0.36f), RowDef("Luminosity", "25×", 1.0f), RowDef("Surface heat", "1.8×", 0.40f))
    val barL = 110f; val barW = 540f; val top0 = 1090f; val rowH = 132f
    val draw = animate(0f, 1f, 1.4f, 4f, Easing.easeOutCubic)(t)
    Eyebrow("Sirius A vs. the Sun", c(0x8fc0ff), 160f, e1)
    Head(headline("Bigger, hotter,\n", "far brighter."), 80f, 206f, e2)
    Sphere(250f * grow, SIR_A, glow = SIR_A_GLOW, modifier = Modifier.offset((340f - 125f * grow).dp, (cy - 125f * grow).dp))
    Sphere(150f * grow, listOf(c(0xfff7d6), c(0xffc23a), c(0xff8a26)), glow = Color(0x80ffa03c), modifier = Modifier.offset((770f - 75f * grow).dp, (cy - 75f * grow).dp))
    CenterLabel(340f, cy + 150f, reveal(t, 2f).opacity) { Text("SIRIUS A", style = sirLabel(20f, c(0xbcd6ff))) }
    CenterLabel(770f, cy + 100f, reveal(t, 2f).opacity) { Text("SUN", style = sirLabel(20f, c(0xffc23a))) }
    rows.forEachIndexed { i, r ->
        val y = top0 + i * rowH
        val a = reveal(t, 1.2f + i * 0.18f)
        At(barL, y - 42f, a.opacity) { Text(r.l, style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, letterSpacing = 0.06.em, color = c(0x9fb6d6))) }
        At(barL, y, a.opacity) { Box(Modifier.size(barW.dp, 14.dp).clip(RoundedCornerShape(7.dp)).background(Color.White.copy(alpha = 0.08f))) }
        At(barL, y, a.opacity) { Box(Modifier.size((barW * r.frac * draw).coerceAtLeast(0f).dp, 14.dp).clip(RoundedCornerShape(7.dp)).background(Brush.horizontalGradient(listOf(Color(0x668fc0ff), c(0xbcd6ff))))) }
        At(barL + barW + 18f, y - 13f, a.opacity) { Text(r.v, style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Color.White)) }
    }
    BottomLine(1648f, e3, body("Every figure dwarfs the Sun's — Sirius A pours out ", "25 times more light."))
}

// ───────────────────── 4 · A sun the size of Earth ─────────────────────

@Composable
fun BoxScope.SirWhiteDwarf(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val cx = 540f; val cy = 840f
    val r = interpolate(listOf(1.4f, 3.6f), listOf(290f, 72f), Easing.easeInOutCubic)(t)
    val collapsed = t > 3.3f
    val earthIn = reveal(t, 3.9f, 1.0f)
    Eyebrow("Sirius B · a white dwarf", c(0xcfe0ff), 160f, e1)
    Head(headline("A whole sun,\nthe size of ", "Earth."), 80f, 206f, e2)
    Sphere(r * 2f, SIR_B, glow = Color(0x99c8deff), modifier = Modifier.offset((cx - r).dp, (cy - r).dp))
    At(cx + 140f, cy - 65f, earthIn.opacity) {
        Box {
            Sphere(130f, listOf(c(0x9cc4ec), c(0x3d72b8), c(0x16223f)))
        }
    }
    CenterLabel(cx + 140f + 65f, cy - 65f + 150f, earthIn.opacity) { Text("EARTH, TO SCALE", style = sirLabel(16f, c(0x9cc4ec))) }
    CenterLabel(cx, if (collapsed) cy + 110f else cy + r + 36f, reveal(t, 1f).opacity) {
        Text(if (collapsed) "SIRIUS B" else "A DYING STAR", style = sirLabel(20f, c(0xcfe0ff)))
    }
    At(90f, 1170f, reveal(t, 4.4f).opacity) {
        Column {
            Text(buildAnnotatedString { withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 78.sp)) { append("1") }; withStyle(SpanStyle(fontWeight = FontWeight.Light, fontSize = 30.sp)) { append(" solar mass") } }, style = TextStyle(fontFamily = OrbitFont, color = Color.White, lineHeight = 78.sp))
            Text("packed into an Earth-sized ball", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 22.sp, color = c(0x9fb6d6)), modifier = Modifier.padding(top = 4.dp))
        }
    }
    At(90f, 1370f, reveal(t, 4.9f).opacity) {
        Column {
            Text(buildAnnotatedString { withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 78.sp)) { append("≈ 5 t") }; withStyle(SpanStyle(fontWeight = FontWeight.Light, fontSize = 30.sp)) { append(" per teaspoon") } }, style = TextStyle(fontFamily = OrbitFont, color = Color.White, lineHeight = 78.sp))
            Text("as heavy as a small car", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 22.sp, color = c(0x9fb6d6)), modifier = Modifier.padding(top = 4.dp))
        }
    }
    BottomLine(1648f, e3, body("When a Sun-like star dies, its core collapses into a ", "white dwarf", " — impossibly dense, cooling slowly forever."))
}

// ───────────────────── 5 · The Dog Star ─────────────────────

@Composable
fun BoxScope.SirDogStar(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val e3 = reveal(t, 6.6f)
    val riseP = animate(0f, 1f, 0.8f, 5f, Easing.easeOutCubic)(t)
    val sx = 360f + riseP * 120f; val sy = 1640f - riseP * 540f
    val dawn = riseP
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                0.30f to Color.Transparent,
                0.62f to Color(0.235f, 0.157f, 0.282f, 0.25f * dawn),
                0.85f to Color(0.588f, 0.353f, 0.282f, 0.5f * dawn),
                1f to Color(0.871f, 0.549f, 0.361f, 0.62f * dawn),
            ),
        ),
    )
    Eyebrow("The Dog Star", c(0x8fc0ff), 150f, e1)
    Head(headline("Herald of\nthe ", "flood."), 84f, 196f, e2)
    val dogStars = listOf(Offset(300f, 740f), Offset(430f, 880f), Offset(560f, 1010f), Offset(700f, 950f), Offset(650f, 1170f), Offset(810f, 1090f))
    dogStars.forEachIndexed { i, d ->
        At(d.x, d.y, 0.45f * reveal(t, 3.4f + i * 0.15f).opacity) { Box(Modifier.size(6.dp).clip(CircleShape).background(c(0xcdddf5))) }
    }
    Sphere(118f, SIR_A, glow = Color(0xB396C3FF), modifier = Modifier.offset((sx - 59f).dp, (sy - 59f).dp))
    // jagged horizon
    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        val top = 1620f
        fun pt(xf: Float, yf: Float) = Offset(xf * 1080f * k, (top + yf * 300f) * k)
        val path = Path().apply {
            moveTo(pt(0f, 0.30f).x, pt(0f, 0.30f).y)
            lineTo(pt(0.30f, 0.24f).x, pt(0.30f, 0.24f).y)
            lineTo(pt(0.55f, 0.34f).x, pt(0.55f, 0.34f).y)
            lineTo(pt(0.78f, 0.22f).x, pt(0.78f, 0.22f).y)
            lineTo(pt(1f, 0.30f).x, pt(1f, 0.30f).y)
            lineTo(pt(1f, 1f).x, pt(1f, 1f).y)
            lineTo(pt(0f, 1f).x, pt(0f, 1f).y)
            close()
        }
        drawPath(path, brush = Brush.verticalGradient(listOf(c(0x1a0f12), c(0x060305)), startY = top * k, endY = 1920f * k))
    }
    BottomLine(1640f, e3, body("Its first dawn rising once marked the ", "Nile's annual flood", " — and gave us the \"dog days\" of summer."))
}

// ───────────────────── 6 · 8.6 light-years ─────────────────────

@Composable
fun BoxScope.SirDistance(t: Float, duration: Float) = SceneFade(t, duration) {
    val e1 = reveal(t, 0.3f); val e2 = reveal(t, 0.9f); val endL = reveal(t, 6.4f, 1.0f)
    val num = interpolate(listOf(1.2f, 4f), listOf(0f, 8.6f), Easing.easeOutCubic)(t)
    val topY = 600f; val botY = 1320f
    val p = interpolate(listOf(2f, 6f), listOf(topY, botY), Easing.easeInOutSine)(t)
    val moving = t in 2f..6.2f
    Eyebrow("How far is Sirius?", c(0x8fc0ff), 230f, e1)
    At(84f, 300f + e2.ty, e2.opacity) {
        Column {
            Text(String.format(Locale.US, "%.1f", num), style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 230.sp, color = Color.White, letterSpacing = (-0.04).em, lineHeight = 207.sp))
            Text("light-years away.", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 52.sp, color = Color.White), modifier = Modifier.padding(top = 8.dp))
            Text("one of the Sun's nearest neighbours", style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 28.sp, color = c(0x7e94b4)), modifier = Modifier.padding(top = 16.dp))
        }
    }
    val la = reveal(t, 1.6f)
    Canvas(Modifier.fillMaxSize().alpha(la.opacity)) {
        val k = size.width / 1080f
        var y = topY
        while (y < botY) {
            drawLine(c(0x3a4a66), Offset(880f * k, y * k), Offset(880f * k, (y + 8f) * k), strokeWidth = 2f * k)
            y += 16f
        }
    }
    Sphere(56f, SIR_A, glow = SIR_A_GLOW, modifier = Modifier.offset((880f - 28f).dp, (topY - 28f).dp))
    At(912f, topY - 12f, reveal(t, 2f).opacity) { Text("SIRIUS", style = sirLabel(18f, c(0xbcd6ff))) }
    At(880f - 22f, botY - 22f, reveal(t, 2f).opacity) { Box(Modifier.size(44.dp).clip(CircleShape).border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)) }
    At(912f, botY - 12f, reveal(t, 2f).opacity) { Text("YOU, TONIGHT", style = sirLabel(18f, c(0x9fb6d6))) }
    if (moving) {
        At(880f - 6f, p - 6f) { Box(Modifier.size(12.dp).clip(CircleShape).background(Color.White)) }
    }
    At(90f, 1560f, endL.opacity, modifier = Modifier.size(900.dp, 200.dp)) {
        Text(body("The light you see tonight ", "left Sirius 8.6 years ago."), style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 38.sp, color = Color.White, lineHeight = 50.sp))
    }
}
