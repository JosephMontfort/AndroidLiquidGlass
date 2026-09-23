package com.kyant.backdrop.catalog.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

/**
 * Organic fluid morph shape that draws the exact iOS 26 bubble/teardrop path.
 *
 * Frame-by-frame analysis reveals:
 * - EXPANSION: Shape is an egg/pear — wider at top, narrower bottom, left flank bows outward
 * - COLLAPSE: Shape is a hanging teardrop — narrow body, rounded bottom cap
 * - The shape is NEVER a rectangle during animation — always organic
 */
class BubbleMorphShape(
    val progress: Float,
    val isExpanding: Boolean,
    val cornerRadiusPx: Float = 70f
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) {
            return Outline.Rectangle(androidx.compose.ui.geometry.Rect.Zero)
        }

        val p = progress.coerceIn(0f, 1f)

        // At resting states, use hardware-accelerated RoundRect
        if (p <= 0.001f || p >= 0.999f) {
            val r = min(cornerRadiusPx, min(w, h) / 2f)
            return Outline.Rounded(RoundRect(0f, 0f, w, h, CornerRadius(r)))
        }

        // The organic bubble effect intensity peaks at ~40% expansion
        val bubbleIntensity = sin(p * PI.toFloat())
        val kappa = 0.5523f

        val r = min(cornerRadiusPx, min(w, h) / 2f)

        val path = Path()

        if (isExpanding) {
            // EXPANSION: Pear/egg shape
            // The left flank bows outward, bottom is narrower than top initially
            // Top-right is anchored

            // How much the left side bows outward (belly)
            val bellyBow = bubbleIntensity * w * 0.08f
            // How much the bottom sags down
            val bottomSag = bubbleIntensity * h * 0.03f
            // How much the bottom is narrower than top (pear effect)
            // At early stages bottom is significantly narrower
            val bottomNarrow = bubbleIntensity * w * 0.06f

            // Corner radii — bottom-left gets rounder during blob phase
            val rTR = r
            val rTL = r + bubbleIntensity * r * 0.3f
            val rBL = r + bubbleIntensity * r * 0.6f
            val rBR = r + bubbleIntensity * r * 0.15f

            val clampedRTR = min(rTR, min(w, h) / 2f)
            val clampedRTL = min(rTL, min(w, h) / 2f)
            val clampedRBL = min(rBL, min(w, h) / 2f)
            val clampedRBR = min(rBR, min(w, h) / 2f)

            // Key points:
            // Top-left: (0, 0) but shifted right by belly bow at top
            // Top-right: (w, 0) — anchored
            // Bottom-right: (w, h + bottomSag)
            // Bottom-left: (bottomNarrow, h + bottomSag) — narrower

            val topLeftX = 0f
            val bottomLeftX = bottomNarrow
            val bottomY = h + bottomSag

            // Start at top-left corner (after radius)
            path.moveTo(topLeftX + clampedRTL, 0f)

            // Top edge
            path.lineTo(w - clampedRTR, 0f)

            // Top-right corner (anchored, clean squircle)
            path.cubicTo(
                w - clampedRTR * (1f - kappa), 0f,
                w, clampedRTR * (1f - kappa),
                w, clampedRTR
            )

            // Right edge — straight or very slight curve
            path.lineTo(w, bottomY - clampedRBR)

            // Bottom-right corner
            path.cubicTo(
                w, bottomY - clampedRBR * (1f - kappa),
                w - clampedRBR * (1f - kappa), bottomY,
                w - clampedRBR, bottomY
            )

            // Bottom edge — slight sag curve
            val bottomMidX = (bottomLeftX + clampedRBL + w - clampedRBR) / 2f
            path.cubicTo(
                bottomMidX + (w - clampedRBR - bottomMidX) * 0.5f, bottomY + bottomSag * 0.3f,
                bottomMidX - (bottomMidX - bottomLeftX - clampedRBL) * 0.5f, bottomY + bottomSag * 0.3f,
                bottomLeftX + clampedRBL, bottomY
            )

            // Bottom-left corner (very round during blob phase)
            path.cubicTo(
                bottomLeftX + clampedRBL * (1f - kappa), bottomY,
                bottomLeftX, bottomY - clampedRBL * (1f - kappa),
                bottomLeftX, bottomY - clampedRBL
            )

            // LEFT FLANK — the organic S-curve belly that bows outward!
            // This is the KEY to the iOS look. Control points push LEFT.
            val leftMidY1 = bottomY - clampedRBL - (bottomY - clampedRBL - clampedRTL) * 0.35f
            val leftMidY2 = clampedRTL + (bottomY - clampedRBL - clampedRTL) * 0.25f
            path.cubicTo(
                bottomLeftX - bellyBow, leftMidY1,
                topLeftX - bellyBow * 0.6f, leftMidY2,
                topLeftX, clampedRTL
            )

            // Top-left corner
            path.cubicTo(
                topLeftX, clampedRTL * (1f - kappa),
                topLeftX + clampedRTL * (1f - kappa), 0f,
                topLeftX + clampedRTL, 0f
            )
        } else {
            // COLLAPSE: Hanging teardrop / narrow blob
            // Width narrows, bottom forms rounded droplet cap

            // Waist pinch — sides pinch inward
            val waistPinch = bubbleIntensity * w * 0.08f
            // Bottom teardrop sag
            val dropSag = bubbleIntensity * h * 0.06f

            val rTR = r
            val rTL = r
            val rBL = r + bubbleIntensity * r * 0.5f
            val rBR = r + bubbleIntensity * r * 0.5f

            val clampedRTR = min(rTR, min(w, h) / 2f)
            val clampedRTL = min(rTL, min(w, h) / 2f)
            val clampedRBL = min(rBL, min(w, h) / 2f)
            val clampedRBR = min(rBR, min(w, h) / 2f)

            val bodyBottom = h
            val dropBottom = h + dropSag

            // Start at top-left
            path.moveTo(clampedRTL, 0f)

            // Top edge
            path.lineTo(w - clampedRTR, 0f)

            // Top-right corner
            path.cubicTo(
                w - clampedRTR * (1f - kappa), 0f,
                w, clampedRTR * (1f - kappa),
                w, clampedRTR
            )

            // Right flank with waist pinch — curves inward at middle
            val pinchY = bodyBottom * 0.6f
            path.cubicTo(
                w, pinchY * 0.5f,
                w - waistPinch, pinchY,
                w - waistPinch * 0.3f, bodyBottom - clampedRBR
            )

            // Bottom cap — hanging droplet bulb
            if (dropSag > 2f) {
                val midX = w / 2f
                val bulbW = min(w * 0.4f, clampedRBR)
                path.cubicTo(
                    w - waistPinch * 0.3f, dropBottom * 0.95f,
                    midX + bulbW, dropBottom,
                    midX, dropBottom
                )
                path.cubicTo(
                    midX - bulbW, dropBottom,
                    waistPinch * 0.3f, dropBottom * 0.95f,
                    waistPinch * 0.3f, bodyBottom - clampedRBL
                )
            } else {
                // Standard bottom corners
                path.cubicTo(
                    w, bodyBottom - clampedRBR * (1f - kappa),
                    w - clampedRBR * (1f - kappa), bodyBottom,
                    w - clampedRBR, bodyBottom
                )
                path.lineTo(clampedRBL, bodyBottom)
                path.cubicTo(
                    clampedRBL * (1f - kappa), bodyBottom,
                    0f, bodyBottom - clampedRBL * (1f - kappa),
                    0f, bodyBottom - clampedRBL
                )
            }

            // Left flank with waist pinch
            path.cubicTo(
                waistPinch, pinchY,
                0f, pinchY * 0.5f,
                0f, clampedRTL
            )

            // Top-left corner
            path.cubicTo(
                0f, clampedRTL * (1f - kappa),
                clampedRTL * (1f - kappa), 0f,
                clampedRTL, 0f
            )
        }

        path.close()
        return Outline.Generic(path)
    }
}
