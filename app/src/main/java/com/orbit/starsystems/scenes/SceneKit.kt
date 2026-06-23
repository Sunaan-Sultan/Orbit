package com.orbit.starsystems.scenes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.orbit.starsystems.core.Easing
import com.orbit.starsystems.core.Reveal
import com.orbit.starsystems.core.SceneId
import com.orbit.starsystems.core.clamp
import com.orbit.starsystems.ui.OrbitFont
import java.util.Locale
import kotlin.math.roundToInt

// All scenes are authored in the SCENE_W x SCENE_H virtual space (see ui/Visuals.kt)
// and draw against these shared building blocks.

// ───────────────────────── colour & type ─────────────────────────

internal fun c(v: Long) = Color(v or 0xFF000000)

internal fun fmt(n: Float) = String.format(Locale.US, "%,d", n.roundToInt())

internal fun eyebrow(color: Color) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.Bold, fontSize = 26.sp,
    letterSpacing = 0.42.em, color = color,
)

internal fun head(sizeSp: Float, color: Color = Color.White) = TextStyle(
    fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = sizeSp.sp,
    letterSpacing = (-0.02).em, lineHeight = (sizeSp * 1.06f).sp, color = color,
)

/** Builds a two-weight headline: [plain] then [emph] in semibold. */
internal fun headline(plain: String, emph: String, tail: String = ""): AnnotatedString = buildAnnotatedString {
    append(plain)
    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(emph) }
    if (tail.isNotEmpty()) append(tail)
}

/** Builds a body line: [plain] then [bold] in bold. */
internal fun body(plain: String, bold: String, tail: String = ""): AnnotatedString = buildAnnotatedString {
    append(plain)
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(bold) }
    if (tail.isNotEmpty()) append(tail)
}

// ───────────────────────── layout primitives ─────────────────────────

/** Places [content] at virtual coordinate (x, y) with an optional alpha. */
@Composable
internal fun BoxScope.At(
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
internal fun BoxScope.SceneFade(t: Float, duration: Float, content: @Composable BoxScope.() -> Unit) {
    val fi = 0.7f; val fo = 0.7f
    val end = duration - fo
    val o = when {
        t < fi -> Easing.easeOutCubic(clamp(t / fi, 0f, 1f))
        t > end -> 1f - Easing.easeInCubic(clamp((t - end) / fo, 0f, 1f))
        else -> 1f
    }
    Box(Modifier.fillMaxSize().alpha(o.coerceIn(0f, 1f))) { content() }
}

/** CSS-style camera: scales the scene around an absolute (cx, cy) pivot point. */
@Composable
internal fun BoxScope.Camera(scale: Float, cx: Float, cy: Float, content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier.matchParentSize().graphicsLayer {
            scaleX = scale; scaleY = scale
            transformOrigin = TransformOrigin(cx / 1080f, cy / 1920f)
        },
    ) { content() }
}

// ───────────────────────── drawing primitives ─────────────────────────

/**
 * A radial-gradient disc (the Sun, halos) positioned by absolute scene coordinates.
 * Drawn on a scene-sized canvas so the circle can far exceed the canvas (the Sun does);
 * only the overlapping part shows, which is exactly the rising limb.
 */
@Composable
internal fun RadialDisc(
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
            brush = Brush.radialGradient(colorStops = stops, center = Offset(gcx, gcy), radius = (2f * r) * radiusFactor),
            radius = r, center = Offset(cx, cy),
        )
    }
}

/** A stroked ring / orbit ellipse in scene units; optional dash, fade and clip-to-below-[clipTop]. */
@Composable
internal fun RingArc(
    cx: Float, cy: Float, w: Float, h: Float, deg: Float, color: Color, bw: Float,
    dash: Boolean = false, clipTop: Float? = null, alpha: Float = 1f,
) {
    Canvas(if (alpha < 1f) Modifier.fillMaxSize().alpha(alpha) else Modifier.fillMaxSize()) {
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

// ───────────────────────── text kit ─────────────────────────

@Composable
internal fun BoxScope.Eyebrow(text: String, color: Color, y: Float, r: Reveal) {
    At(90f, y + r.ty, r.opacity) { Text(text.uppercase(), style = eyebrow(color)) }
}

@Composable
internal fun BoxScope.Head(text: AnnotatedString, sizeSp: Float, y: Float, r: Reveal) {
    At(88f, y + r.ty, r.opacity, modifier = Modifier.width(920.dp)) { Text(text, style = head(sizeSp)) }
}

@Composable
internal fun BoxScope.BottomLine(y: Float, r: Reveal, text: AnnotatedString, color: Color = Color.White) {
    At(90f, y + r.ty, r.opacity, modifier = Modifier.width(900.dp)) {
        Text(text, style = TextStyle(fontFamily = OrbitFont, fontWeight = FontWeight.Light, fontSize = 34.sp, color = color, lineHeight = 44.sp))
    }
}

/** A big-number + caption stat column (number may carry a smaller trailing unit). */
@Composable
internal fun StatCol(value: String, unit: String?, label: String, valueSize: Float, numColor: Color, labelColor: Color) {
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
internal fun LabeledStat(top: String, topColor: Color, value: String, unit: String?, sub: String?, valueSize: Float) {
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
internal fun BoxScope.CenterLabel(cx: Float, y: Float, alpha: Float, content: @Composable () -> Unit) {
    At(cx - 200f, y, alpha) {
        Column(Modifier.width(400.dp), horizontalAlignment = Alignment.CenterHorizontally) { content() }
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
        SceneId.AC_TRIPLE -> SceneAcTriple(t, duration)
        SceneId.AC_WALTZ -> SceneAcWaltz(t, duration)
        SceneId.AC_TWIN -> SceneAcTwin(t, duration)
        SceneId.AC_PROXIMA -> SceneAcProxima(t, duration)
        SceneId.AC_PROXIMAB -> SceneAcProximaB(t, duration)
        SceneId.AC_TRAVEL -> SceneAcTravel(t, duration)
        SceneId.TP_INTRO -> TpIntro(t, duration)
        SceneId.TP_STAR -> TpStarSize(t, duration)
        SceneId.TP_MERCURY -> TpMercury(t, duration)
        SceneId.TP_ORBITS -> TpOrbits(t, duration)
        SceneId.TP_HABITABLE -> TpHabitable(t, duration)
        SceneId.TP_TIDAL -> TpTidal(t, duration)
        SceneId.TP_SKY -> TpSky(t, duration)
        SceneId.TP_DISTANCE -> TpDistance(t, duration)
        SceneId.SIR_BRIGHTEST -> SirBrightest(t, duration)
        SceneId.SIR_BINARY -> SirBinary(t, duration)
        SceneId.SIR_HOTTER -> SirHotter(t, duration)
        SceneId.SIR_WHITEDWARF -> SirWhiteDwarf(t, duration)
        SceneId.SIR_DOGSTAR -> SirDogStar(t, duration)
        SceneId.SIR_DISTANCE -> SirDistance(t, duration)
        SceneId.KEP_EIGHT -> KepEight(t, duration)
        SceneId.KEP_SUNLIKE -> KepSunlike(t, duration)
        SceneId.KEP_CROWDED -> KepCrowded(t, duration)
        SceneId.KEP_AI -> KepAi(t, duration)
        SceneId.KEP_DISTANCE -> KepDistance(t, duration)
    }
}
