package com.kyant.backdrop.catalog.components

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.PI
import kotlin.math.sin

enum class MorphShapeType(val label: String) {
    Squircle("Squircle"),
    Rounded("Rounded"),
    Capsule("Capsule"),
    Circle("Circle")
}

class FluidMorphShape(
    val progress: Float,
    val isExpanding: Boolean,
    // Legacy Dp fields for older catalog components
    val startWidth: Dp = 0.dp,
    val startHeight: Dp = 0.dp,
    val targetWidth: Dp = 0.dp,
    val targetHeight: Dp = 0.dp,
    val startRadius: Dp = 28.dp,
    val targetRadius: Dp = 34.dp,
    val startShapeType: MorphShapeType = MorphShapeType.Squircle,
    val targetShapeType: MorphShapeType = MorphShapeType.Squircle,
    val alignment: Alignment = Alignment.TopEnd,
    val viscosityBulge: Float = 0.40f,
    val meniscusSagDp: Float = 38f,
    // Hardware-accelerated Px fields for the new Opaque container
    val startWidthPx: Float = -1f,
    val startHeightPx: Float = -1f,
    val targetWidthPx: Float = -1f,
    val targetHeightPx: Float = -1f,
    val startRadiusPx: Float = -1f,
    val targetRadiusPx: Float = -1f,
    val extraPaddingPx: Float = 0f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return Outline.Rectangle(Rect.Zero)

        val p = progress.coerceIn(0f, 1f)

        // Seamlessly bridge both constructors
        val sWPx = if (startWidthPx >= 0f) startWidthPx else with(density) { startWidth.toPx() }
        val sHPx = if (startHeightPx >= 0f) startHeightPx else with(density) { startHeight.toPx() }
        val tWPx = if (targetWidthPx >= 0f) targetWidthPx else with(density) { targetWidth.toPx() }
        val tHPx = if (targetHeightPx >= 0f) targetHeightPx else with(density) { targetHeight.toPx() }
        val sRPx = if (startRadiusPx >= 0f) startRadiusPx else with(density) { startRadius.toPx() }
        val tRPx = if (targetRadiusPx >= 0f) targetRadiusPx else with(density) { targetRadius.toPx() }

        val currentW = lerp(sWPx, tWPx, p)
        val currentH = lerp(sHPx, tHPx, p)
        val currentR = lerp(sRPx, tRPx, p)

        val fluidFactor = sin(p * PI.toFloat())

        val right = w - extraPaddingPx
        val top = extraPaddingPx
        val left = right - currentW
        val bottom = top + currentH

        val sagTop = fluidFactor * (currentH * 0.04f)
        val sagRight = fluidFactor * (currentW * 0.035f)
        val bellyLeft = fluidFactor * (currentW * 0.18f) * (viscosityBulge / 0.40f)
        val sagBottom = fluidFactor * (currentH * 0.08f)

        val rTR = currentR
        val rTL = currentR * (1f + fluidFactor * 0.15f)
        val rBL = currentR * (1f + fluidFactor * 0.45f)
        val rBR = currentR * (1f + fluidFactor * 0.10f)

        val kappa = 0.55228475f
        val path = Path().apply {
            moveTo(left + rTL, top + sagTop)
            val topMidX = (left + rTL + right - rTR) / 2f
            quadraticBezierTo(topMidX, top + sagTop * 1.4f, right - rTR, top)
            cubicTo(right - rTR * (1f - kappa), top, right, top + rTR * (1f - kappa), right, top + rTR)
            val rightMidY = (top + rTR + bottom - rBR) / 2f
            quadraticBezierTo(right - sagRight, rightMidY, right, bottom - rBR)
            cubicTo(right, bottom - rBR * (1f - kappa), right - rBR * (1f - kappa), bottom + sagBottom * 0.3f, right - rBR, bottom + sagBottom * 0.3f)
            val botMidX = (left + rBL + right - rBR) / 2f
            quadraticBezierTo(botMidX, bottom + sagBottom, left + rBL, bottom + sagBottom)
            cubicTo(left + rBL * (1f - kappa), bottom + sagBottom, left - bellyLeft * 0.5f, bottom - rBL * (1f - kappa), left - bellyLeft * 0.6f, bottom - rBL)
            val leftMidY = (top + rTL + bottom - rBL) / 2f
            cubicTo(left - bellyLeft, leftMidY + (bottom - leftMidY) * 0.4f, left - bellyLeft * 0.85f, leftMidY - (leftMidY - top) * 0.3f, left, top + rTL)
            cubicTo(left, top + rTL * (1f - kappa), left + rTL * (1f - kappa), top + sagTop, left + rTL, top + sagTop)
            close()
        }
        return Outline.Generic(path)
    }
}
