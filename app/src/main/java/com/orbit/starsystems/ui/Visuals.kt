package com.orbit.starsystems.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Everything in a scene is authored in this fixed virtual space, then scaled to fit. */
const val SCENE_W = 1080f
const val SCENE_H = 1920f

val OrbitFont = FontFamily.SansSerif

/**
 * Hosts scene content in the [SCENE_W] x [SCENE_H] virtual space and scales it to fill
 * (cover) or fit (contain) the available area, centred and clipped — mirroring the
 * `MiniStage` / `Stage` scalers from the prototype.
 */
@Composable
fun SceneCanvas(
    modifier: Modifier = Modifier,
    cover: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val realDensity = LocalDensity.current
    BoxWithConstraints(
        modifier
            .clipToBounds()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        // Map the SCENE_W x SCENE_H virtual space onto the viewport by overriding the
        // ambient density: 1 virtual unit (authored as 1.dp) becomes `pxPerUnit` pixels.
        // This keeps each scene's render buffers bounded to the viewport size instead of
        // inflating a full 1080x1920.dp node and scaling it down (which exhausted memory).
        val wPx = maxWidth.value * realDensity.density
        val hPx = maxHeight.value * realDensity.density
        val pxPerUnit = if (cover) max(wPx / SCENE_W, hPx / SCENE_H) else min(wPx / SCENE_W, hPx / SCENE_H)
        val sceneDensity = Density(density = pxPerUnit, fontScale = realDensity.fontScale)
        CompositionLocalProvider(LocalDensity provides sceneDensity) {
            Box(Modifier.requiredSize(SCENE_W.dp, SCENE_H.dp)) { content() }
        }
    }
}

/** A lit sphere with a 3-stop radial body, optional outer glow and Saturn-style ring. */
@Composable
fun Sphere(
    sizeUnits: Float,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    glow: Color? = null,
    ring: Color? = null,
) {
    Canvas(modifier.size(sizeUnits.dp)) {
        val d = size.width
        val c = Offset(d / 2f, d / 2f)

        // Outer glow halo (drawn behind, allowed to bleed past the sphere bounds).
        glow?.let {
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(0f to it, 0.6f to it.copy(alpha = it.alpha * 0.4f), 1f to it.copy(alpha = 0f)),
                    center = c,
                    radius = d * 0.95f,
                ),
                radius = d * 0.95f,
                center = c,
            )
        }

        // Ring (ellipse, tilted), behind the body so the planet overlaps its near edge.
        ring?.let {
            val rw = d * 2.3f
            val rh = d * 0.62f
            val sw = max(2f, d * 0.045f)
            rotate(degrees = -17f, pivot = c) {
                drawOval(
                    color = it,
                    topLeft = Offset(c.x - rw / 2f, c.y - rh / 2f),
                    size = Size(rw, rh),
                    style = Stroke(width = sw),
                )
            }
        }

        // Body — lit from the upper-left.
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(0f to colors[0], 0.52f to colors[1], 1f to colors[2]),
                center = Offset(d * 0.34f, d * 0.30f),
                radius = d * 0.92f,
            ),
            radius = d / 2f,
            center = c,
        )
        // Inset shadow deepening the far (lower-right) limb.
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(0.45f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.5f)),
                center = Offset(d * 0.34f, d * 0.30f),
                radius = d * 0.95f,
            ),
            radius = d / 2f,
            center = c,
        )
    }
}

private class Star(
    val x: Float, val y: Float, val r: Float,
    val base: Float, val sp: Float, val ph: Float,
)

/** Deterministic twinkling starfield, authored across the full virtual space. */
@Composable
fun Starfield(time: Float) {
    val stars = remember {
        var s = 9301L
        val rnd = { s = (s * 9301 + 49297) % 233280; s / 233280f }
        List(140) {
            Star(
                x = rnd() * 1080f, y = rnd() * 1920f, r = 0.5f + rnd() * 1.6f,
                base = 0.18f + rnd() * 0.5f, sp = 0.4f + rnd() * 1.6f, ph = rnd() * 6.28f,
            )
        }
    }
    Canvas(Modifier.fillMaxSize()) {
        val k = size.width / 1080f
        stars.forEach { st ->
            val op = (st.base * (0.55f + 0.45f * sin(time * st.sp + st.ph))).coerceIn(0f, 1f)
            drawCircle(
                color = Color.White.copy(alpha = op),
                radius = st.r * k,
                center = Offset(st.x * k, st.y * (size.height / 1920f)),
            )
        }
    }
}

/** Soft vignette that sinks the edges of every scene into black. */
@Composable
fun Vignette() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colorStops = arrayOf(0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.85f)),
                center = Offset(size.width / 2f, size.height * 0.42f),
                radius = size.height * 0.62f,
            ),
        )
    }
}
