package com.orbit.starsystems.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Line-art icons rendered in a 0..24 viewbox, mirroring the prototype's `Ico` set. */
@Composable
fun Ico(
    name: String,
    size: Dp = 24.dp,
    color: Color = Color.White,
    filled: Boolean = false,
    sw: Float = 1.8f,
) {
    Canvas(Modifier.size(size)) {
        val k = this.size.width / 24f
        scale(k, k, pivot = Offset.Zero) {
            drawIcon(name, color, filled, sw)
        }
    }
}

private fun DrawScope.strokePath(color: Color, sw: Float, build: Path.() -> Unit) {
    val p = Path().apply(build)
    drawPath(p, color, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawIcon(name: String, color: Color, filled: Boolean, sw: Float) {
    when (name) {
        "back" -> strokePath(color, sw) {
            moveTo(19f, 12f); lineTo(5f, 12f)
            moveTo(11f, 6f); lineTo(5f, 12f); lineTo(11f, 18f)
        }
        "chevR" -> strokePath(color, sw) { moveTo(9f, 6f); lineTo(15f, 12f); lineTo(9f, 18f) }
        "play" -> drawPath(
            Path().apply { moveTo(8f, 5f); lineTo(19f, 12f); lineTo(8f, 19f); close() },
            color,
        )
        "chevUp" -> strokePath(color, sw) { moveTo(6f, 15f); lineTo(12f, 9f); lineTo(18f, 15f) }
        "close" -> strokePath(color, sw) {
            moveTo(6f, 6f); lineTo(18f, 18f)
            moveTo(18f, 6f); lineTo(6f, 18f)
        }
        "bolt" -> {
            val p = Path().apply {
                moveTo(13f, 2f); lineTo(4f, 14f); relativeLineTo(6f, 0f)
                relativeLineTo(-1f, 8f); lineTo(18f, 10f); relativeLineTo(-6f, 0f)
                relativeLineTo(1f, -8f); close()
            }
            if (filled) drawPath(p, color)
            else drawPath(p, color, style = Stroke(width = sw, join = StrokeJoin.Round))
        }
        "saved" -> {
            val p = Path().apply {
                moveTo(7f, 4f); lineTo(17f, 4f); lineTo(18f, 5f); lineTo(18f, 20f)
                lineTo(12f, 16f); lineTo(6f, 20f); lineTo(6f, 5f); close()
            }
            if (filled) drawPath(p, color)
            else drawPath(p, color, style = Stroke(width = sw, join = StrokeJoin.Round, cap = StrokeCap.Round))
        }
        "lock" -> {
            // shackle
            strokePath(color, sw) {
                moveTo(8f, 11f); lineTo(8f, 8f)
                arcTo(Rect(8f, 4f, 16f, 12f), 180f, 180f, false)
                lineTo(16f, 11f)
            }
            // body
            drawPath(
                Path().apply { addRoundRectCompat(5f, 11f, 14f, 9f, 2f) },
                color, style = Stroke(width = sw, join = StrokeJoin.Round),
            )
        }
        "profile" -> {
            drawCircle(color, radius = 3.5f, center = Offset(12f, 8.5f), style = Stroke(width = sw))
            // shoulders (top half of a circle)
            val p = Path().apply { arcTo(Rect(5.5f, 12.5f, 18.5f, 25.5f), 180f, 180f, true) }
            drawPath(p, color, style = Stroke(width = sw, cap = StrokeCap.Round))
        }
        "systems" -> {
            drawCircle(color, radius = 2.4f, center = Offset(12f, 12f))
            // tilted orbit ellipse
            val p = Path().apply { addOval(Rect(2.5f, 8.2f, 21.5f, 15.8f)) }
            rotateAround(12f, 12f, -22f) {
                drawPath(p, color, style = Stroke(width = sw))
            }
        }
        "spotlight" -> {
            // Four-point sparkle: a large star with a small companion.
            val big = Path().apply {
                moveTo(12f, 2.5f); lineTo(14f, 9.5f); lineTo(21f, 12f); lineTo(14f, 14.5f)
                lineTo(12f, 21.5f); lineTo(10f, 14.5f); lineTo(3f, 12f); lineTo(10f, 9.5f); close()
            }
            if (filled) drawPath(big, color)
            else drawPath(big, color, style = Stroke(width = sw, join = StrokeJoin.Round))
            drawCircle(color, radius = 1.3f, center = Offset(19.5f, 5.5f))
        }
    }
}

private fun Path.addRoundRectCompat(x: Float, y: Float, w: Float, h: Float, r: Float) {
    addRoundRect(
        androidx.compose.ui.geometry.RoundRect(
            left = x, top = y, right = x + w, bottom = y + h,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
        ),
    )
}

private fun DrawScope.rotateAround(cx: Float, cy: Float, deg: Float, block: DrawScope.() -> Unit) {
    rotate(deg, Offset(cx, cy)) { block() }
}
