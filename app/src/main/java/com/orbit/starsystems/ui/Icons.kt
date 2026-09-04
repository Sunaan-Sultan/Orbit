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
        "compare" -> {
            // a small disc beside a larger one — the size-comparison glyph
            drawCircle(color, radius = 2.6f, center = Offset(7.5f, 14f), style = Stroke(width = sw))
            drawCircle(color, radius = 5.4f, center = Offset(16f, 12f), style = Stroke(width = sw))
        }
        "star" -> {
            // Five-point rating star.
            val p = Path().apply {
                moveTo(12f, 3f); lineTo(14.6f, 9.2f); lineTo(21f, 9.8f); lineTo(16.2f, 14.1f)
                lineTo(17.6f, 20.5f); lineTo(12f, 17.1f); lineTo(6.4f, 20.5f); lineTo(7.8f, 14.1f)
                lineTo(3f, 9.8f); lineTo(9.4f, 9.2f); close()
            }
            if (filled) drawPath(p, color)
            else drawPath(p, color, style = Stroke(width = sw, join = StrokeJoin.Round, cap = StrokeCap.Round))
        }
        "share" -> {
            // Three nodes joined by two links — the platform share glyph.
            strokePath(color, sw) {
                moveTo(8.6f, 10.8f); lineTo(15.4f, 7.2f)
                moveTo(8.6f, 13.2f); lineTo(15.4f, 16.8f)
            }
            drawCircle(color, radius = 2.6f, center = Offset(6f, 12f), style = Stroke(width = sw))
            drawCircle(color, radius = 2.6f, center = Offset(18f, 6f), style = Stroke(width = sw))
            drawCircle(color, radius = 2.6f, center = Offset(18f, 18f), style = Stroke(width = sw))
        }
        "mail" -> {
            drawPath(
                Path().apply { addRoundRectCompat(3f, 5.5f, 18f, 13f, 2.5f) },
                color, style = Stroke(width = sw, join = StrokeJoin.Round),
            )
            // The flap, inset so it reads as a fold rather than touching the corners.
            strokePath(color, sw) { moveTo(4.5f, 7.5f); lineTo(12f, 13f); lineTo(19.5f, 7.5f) }
        }
        "refresh" -> {
            // Open circular arrow: a gap at the top-right holds the arrowhead.
            drawArc(
                color = color,
                startAngle = -50f, sweepAngle = 300f, useCenter = false,
                topLeft = Offset(4f, 4f),
                size = androidx.compose.ui.geometry.Size(16f, 16f),
                style = Stroke(width = sw, cap = StrokeCap.Round),
            )
            strokePath(color, sw) {
                moveTo(14.4f, 3.4f); lineTo(18.3f, 6.1f); lineTo(15.2f, 9.4f)
            }
        }
        "info" -> {
            drawCircle(color, radius = 9f, center = Offset(12f, 12f), style = Stroke(width = sw))
            drawCircle(color, radius = 1.05f, center = Offset(12f, 7.8f))
            strokePath(color, sw) { moveTo(12f, 11f); lineTo(12f, 16.6f) }
        }
        "trash" -> {
            strokePath(color, sw) {
                moveTo(4f, 7f); lineTo(20f, 7f)
                moveTo(10f, 4.2f); lineTo(14f, 4.2f)
            }
            drawPath(
                // Tapered bin so the silhouette isn't a plain rectangle.
                Path().apply {
                    moveTo(6.2f, 7f); lineTo(17.8f, 7f); lineTo(16.6f, 20f); lineTo(7.4f, 20f); close()
                },
                color, style = Stroke(width = sw, join = StrokeJoin.Round),
            )
            strokePath(color, sw * 0.85f) {
                moveTo(10.3f, 10.5f); lineTo(10.6f, 16.8f)
                moveTo(13.7f, 10.5f); lineTo(13.4f, 16.8f)
            }
        }
        "external" -> {
            strokePath(color, sw) {
                moveTo(14f, 4f); lineTo(20f, 4f); lineTo(20f, 10f)
                moveTo(20f, 4f); lineTo(11f, 13f)
            }
            strokePath(color, sw) {
                moveTo(18f, 14.5f); lineTo(18f, 19f); lineTo(5f, 19f); lineTo(5f, 6f); lineTo(9.5f, 6f)
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
