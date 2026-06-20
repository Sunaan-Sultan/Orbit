package com.orbit.starsystems.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** Shared text style + neutral greys for the app's chrome screens. */
internal fun ts(
    size: Float,
    weight: FontWeight = FontWeight.Normal,
    color: Color = Color.White,
    spacingEm: Float = 0f,
    lineHeight: Float = 0f,
) = TextStyle(
    fontFamily = OrbitFont, fontSize = size.sp, fontWeight = weight, color = color,
    letterSpacing = spacingEm.em,
    lineHeight = if (lineHeight > 0f) lineHeight.sp else TextStyle.Default.lineHeight,
)

internal val Mute = Color(0xFF8A8A8A)
internal val Dim = Color(0xFF7A7A7A)
