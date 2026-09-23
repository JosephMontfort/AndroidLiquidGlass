package com.kyant.backdrop.catalog.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * OriginOS 7 Fluid Morph Checkpoint.
 *
 * Meticulously reconstructed from frame-by-frame measurement of the original
 * OriginOS 7 gallery dropdown video.
 *
 * Trajectory behaviors modeled:
 * 1. Top Edge & Corners Plunge: Top-left and top-right corners drop down (+28dp..+29.5dp)
 *    with a concave middle dip (+14dp) mid-flight, necking into a fluid vase.
 * 2. Asymmetric Flanks: Left concave waist (20dp) + outward bell bulge (18dp) and
 *    right neck (14dp).
 * 3. Viscous Droplet Sag: Bottom edge sags downward (+22dp) under fluid momentum.
 * 4. Convex Dome Arch: Top edge rebounds upward into a wide dome arch (-15dp).
 * 5. Squircle Settling: Corners expand to 24dp, dome flattens, settling into 210x224dp card.
 * 6. Asymmetrical Closing: Retracts via hanging droplet neck underneath the anchored pill.
 */
data class OriginOSMorphCheckpoint(
    val progress: Float,
    val width: Float,
    val height: Float,
    val trOffsetX: Float,
    val trOffsetY: Float,
    val trRadius: Float,
    val tlOffsetX: Float,
    val tlOffsetY: Float,
    val tlRadius: Float,
    val blRadius: Float,
    val brRadius: Float,
    val topEdgeDip: Float, // positive = downward dip, negative = upward dome arch
    val bottomSag: Float,  // viscous droplet sag downwards
    val leftWaist: Float,  // concave neck indentation on left flank
    val leftBulge: Float,  // convex belly bulge on lower left flank
    val rightWaist: Float  // concave neck indentation on right flank
)

val CHECKPOINTS_OPENING_26 = listOf(
    OriginOSMorphCheckpoint(0.000f, 132.00f, 44.00f, 0.00f, 0.00f, 22.00f, 0.00f, 0.00f, 22.00f, 22.00f, 22.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f),
    OriginOSMorphCheckpoint(0.040f, 127.20f, 58.28f, -0.48f, 0.70f, 21.87f, 0.38f, 0.67f, 21.88f, 22.30f, 22.30f, 3.86f, 0.98f, 0.89f, 0.80f, 0.62f),
    OriginOSMorphCheckpoint(0.080f, 122.40f, 72.47f, -1.63f, 2.41f, 21.42f, 1.31f, 2.29f, 21.47f, 22.78f, 22.78f, 7.42f, 2.73f, 2.48f, 2.23f, 1.74f),
    OriginOSMorphCheckpoint(0.120f, 124.92f, 86.49f, -3.31f, 4.88f, 20.75f, 2.65f, 4.63f, 20.83f, 23.36f, 23.36f, 10.40f, 4.91f, 4.47f, 4.02f, 3.13f),
    OriginOSMorphCheckpoint(0.160f, 134.70f, 100.24f, -5.37f, 7.92f, 19.90f, 4.30f, 7.52f, 20.04f, 23.99f, 23.99f, 12.58f, 7.36f, 6.69f, 6.02f, 4.68f),
    OriginOSMorphCheckpoint(0.200f, 144.33f, 113.65f, -7.68f, 11.33f, 18.94f, 6.15f, 10.76f, 19.13f, 24.65f, 24.65f, 13.79f, 9.91f, 9.01f, 8.11f, 6.31f),
    OriginOSMorphCheckpoint(0.240f, 153.69f, 126.63f, -10.11f, 14.91f, 17.93f, 8.09f, 14.15f, 18.18f, 25.31f, 25.31f, 13.92f, 12.46f, 11.33f, 10.19f, 7.93f),
    OriginOSMorphCheckpoint(0.280f, 162.68f, 139.09f, -12.51f, 18.45f, 16.93f, 10.01f, 17.51f, 17.24f, 25.94f, 25.94f, 12.98f, 14.88f, 13.53f, 12.17f, 9.47f),
    OriginOSMorphCheckpoint(0.320f, 171.20f, 150.98f, -14.75f, 21.75f, 16.00f, 11.80f, 20.65f, 16.37f, 26.52f, 26.52f, 11.03f, 17.07f, 15.52f, 13.96f, 10.86f),
    OriginOSMorphCheckpoint(0.360f, 179.16f, 162.20f, -16.71f, 24.64f, 15.20f, 13.36f, 23.39f, 15.62f, 27.02f, 27.02f, 8.23f, 18.94f, 17.21f, 15.49f, 12.05f),
    OriginOSMorphCheckpoint(0.400f, 186.47f, 172.69f, -18.27f, 26.95f, 14.58f, 14.62f, 25.58f, 15.03f, 27.44f, 27.44f, 4.79f, 20.40f, 18.55f, 16.69f, 12.98f),
    OriginOSMorphCheckpoint(0.440f, 193.05f, 182.39f, -19.37f, 28.57f, 14.16f, 15.49f, 27.11f, 14.65f, 27.75f, 27.75f, 0.98f, 21.42f, 19.47f, 17.52f, 13.63f),
    OriginOSMorphCheckpoint(0.480f, 198.84f, 191.24f, -19.93f, 29.40f, 13.99f, 15.94f, 27.90f, 14.49f, 27.95f, 27.95f, -2.56f, 21.93f, 19.94f, 17.95f, 13.96f),
    OriginOSMorphCheckpoint(0.520f, 203.75f, 199.18f, -19.93f, 29.40f, 14.07f, 15.94f, 27.90f, 14.57f, 28.03f, 28.03f, -5.84f, 21.93f, 19.94f, 17.95f, 13.96f),
    OriginOSMorphCheckpoint(0.560f, 207.76f, 206.16f, -19.37f, 28.57f, 14.40f, 15.49f, 27.11f, 14.89f, 27.99f, 27.99f, -8.82f, 21.42f, 19.47f, 17.52f, 13.63f),
    OriginOSMorphCheckpoint(0.600f, 210.80f, 212.15f, -18.27f, 26.95f, 14.98f, 14.62f, 25.58f, 15.43f, 27.84f, 27.84f, -11.34f, 20.40f, 18.55f, 16.69f, 12.98f),
    OriginOSMorphCheckpoint(0.640f, 212.84f, 217.09f, -16.71f, 24.64f, 15.76f, 13.36f, 23.39f, 16.18f, 27.58f, 27.58f, -13.27f, 18.94f, 17.21f, 15.49f, 12.05f),
    OriginOSMorphCheckpoint(0.680f, 213.87f, 220.97f, -14.75f, 21.75f, 16.72f, 11.80f, 20.65f, 17.09f, 27.24f, 27.24f, -14.51f, 17.07f, 15.52f, 13.96f, 10.86f),
    OriginOSMorphCheckpoint(0.720f, 213.73f, 223.76f, -12.51f, 18.45f, 17.81f, 10.01f, 17.51f, 18.12f, 26.82f, 26.82f, -14.99f, 14.88f, 13.53f, 12.17f, 9.47f),
    OriginOSMorphCheckpoint(0.760f, 213.20f, 225.44f, -10.11f, 14.91f, 18.97f, 8.09f, 14.15f, 19.22f, 26.35f, 26.35f, -14.70f, 12.46f, 11.33f, 10.19f, 7.93f),
    OriginOSMorphCheckpoint(0.800f, 212.67f, 226.00f, -7.68f, 11.33f, 20.14f, 6.15f, 10.76f, 20.33f, 25.85f, 25.85f, -13.64f, 9.91f, 9.01f, 8.11f, 6.31f),
    OriginOSMorphCheckpoint(0.840f, 212.13f, 225.60f, -5.37f, 7.92f, 21.26f, 4.30f, 7.52f, 21.40f, 25.35f, 25.35f, -11.88f, 7.36f, 6.69f, 6.02f, 4.68f),
    OriginOSMorphCheckpoint(0.880f, 211.60f, 225.20f, -3.31f, 4.88f, 22.27f, 2.65f, 4.63f, 22.35f, 24.88f, 24.88f, -9.50f, 4.91f, 4.47f, 4.02f, 3.13f),
    OriginOSMorphCheckpoint(0.920f, 211.07f, 224.80f, -1.63f, 2.41f, 23.10f, 1.31f, 2.29f, 23.15f, 24.46f, 24.46f, -6.62f, 2.73f, 2.48f, 2.23f, 1.74f),
    OriginOSMorphCheckpoint(0.960f, 210.53f, 224.40f, -0.48f, 0.70f, 23.71f, 0.38f, 0.67f, 23.72f, 24.14f, 24.14f, -3.40f, 0.98f, 0.89f, 0.80f, 0.62f),
    OriginOSMorphCheckpoint(1.000f, 210.00f, 224.00f, 0.00f, 0.00f, 24.00f, 0.00f, 0.00f, 24.00f, 24.00f, 24.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f)
)

val CHECKPOINTS_CLOSING_26 = listOf(
    OriginOSMorphCheckpoint(0.000f, 132.00f, 44.00f, 0.00f, 0.00f, 22.00f, 0.00f, 0.00f, 22.00f, 22.00f, 22.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f),
    OriginOSMorphCheckpoint(0.040f, 133.19f, 45.04f, -0.14f, 0.11f, 22.08f, 0.10f, 0.10f, 22.08f, 22.18f, 22.18f, 0.05f, 0.62f, 0.43f, 0.29f, 0.33f),
    OriginOSMorphCheckpoint(0.080f, 134.92f, 47.16f, -0.49f, 0.37f, 22.16f, 0.33f, 0.33f, 22.16f, 22.49f, 22.49f, 0.16f, 2.12f, 1.47f, 0.98f, 1.14f),
    OriginOSMorphCheckpoint(0.120f, 136.95f, 50.05f, -0.99f, 0.74f, 22.24f, 0.66f, 0.66f, 22.24f, 22.90f, 22.90f, 0.33f, 4.30f, 2.98f, 1.99f, 2.32f),
    OriginOSMorphCheckpoint(0.160f, 139.20f, 53.59f, -1.61f, 1.21f, 22.32f, 1.07f, 1.07f, 22.32f, 23.39f, 23.39f, 0.54f, 6.98f, 4.83f, 3.22f, 3.76f),
    OriginOSMorphCheckpoint(0.200f, 141.63f, 57.71f, -2.31f, 1.73f, 22.40f, 1.54f, 1.54f, 22.40f, 23.94f, 23.94f, 0.77f, 9.99f, 6.92f, 4.61f, 5.38f),
    OriginOSMorphCheckpoint(0.240f, 144.20f, 62.35f, -3.03f, 2.27f, 22.48f, 2.02f, 2.02f, 22.48f, 24.50f, 24.50f, 1.01f, 13.14f, 9.10f, 6.07f, 7.08f),
    OriginOSMorphCheckpoint(0.280f, 146.91f, 67.48f, -3.75f, 2.81f, 22.56f, 2.50f, 2.50f, 22.56f, 25.06f, 25.06f, 1.25f, 16.26f, 11.26f, 7.51f, 8.76f),
    OriginOSMorphCheckpoint(0.320f, 149.73f, 73.07f, -4.42f, 3.32f, 22.64f, 2.95f, 2.95f, 22.64f, 25.59f, 25.59f, 1.47f, 19.17f, 13.27f, 8.85f, 10.32f),
    OriginOSMorphCheckpoint(0.360f, 152.67f, 79.10f, -5.01f, 3.76f, 22.72f, 3.34f, 3.34f, 22.72f, 26.06f, 26.06f, 1.67f, 21.72f, 15.03f, 10.02f, 11.69f),
    OriginOSMorphCheckpoint(0.400f, 155.70f, 85.55f, -5.48f, 4.11f, 22.80f, 3.65f, 3.65f, 22.80f, 26.45f, 26.45f, 1.83f, 23.75f, 16.45f, 10.96f, 12.79f),
    OriginOSMorphCheckpoint(0.440f, 158.83f, 92.39f, -5.81f, 4.36f, 22.88f, 3.87f, 3.87f, 22.88f, 26.75f, 26.75f, 1.94f, 25.18f, 17.43f, 11.62f, 13.56f),
    OriginOSMorphCheckpoint(0.480f, 162.04f, 99.62f, -5.98f, 4.48f, 22.96f, 3.99f, 3.99f, 22.96f, 26.95f, 26.95f, 1.99f, 25.91f, 17.94f, 11.96f, 13.95f),
    OriginOSMorphCheckpoint(0.520f, 165.33f, 107.22f, -5.98f, 4.48f, 23.04f, 3.99f, 3.99f, 23.04f, 27.03f, 27.03f, 1.99f, 25.91f, 17.94f, 11.96f, 13.95f),
    OriginOSMorphCheckpoint(0.560f, 168.71f, 115.18f, -5.81f, 4.36f, 23.12f, 3.87f, 3.87f, 23.12f, 26.99f, 26.99f, 1.94f, 25.18f, 17.43f, 11.62f, 13.56f),
    OriginOSMorphCheckpoint(0.600f, 172.15f, 123.49f, -5.48f, 4.11f, 23.20f, 3.65f, 3.65f, 23.20f, 26.85f, 26.85f, 1.83f, 23.75f, 16.45f, 10.96f, 12.79f),
    OriginOSMorphCheckpoint(0.640f, 175.66f, 132.14f, -5.01f, 3.76f, 23.28f, 3.34f, 3.34f, 23.28f, 26.62f, 26.62f, 1.67f, 21.72f, 15.03f, 10.02f, 11.69f),
    OriginOSMorphCheckpoint(0.680f, 179.25f, 141.12f, -4.42f, 3.32f, 23.36f, 2.95f, 2.95f, 23.36f, 26.31f, 26.31f, 1.47f, 19.17f, 13.27f, 8.85f, 10.32f),
    OriginOSMorphCheckpoint(0.720f, 182.89f, 150.42f, -3.75f, 2.81f, 23.44f, 2.50f, 2.50f, 23.44f, 25.94f, 25.94f, 1.25f, 16.26f, 11.26f, 7.51f, 8.76f),
    OriginOSMorphCheckpoint(0.760f, 186.59f, 160.03f, -3.03f, 2.27f, 23.52f, 2.02f, 2.02f, 23.52f, 25.54f, 25.54f, 1.01f, 13.14f, 9.10f, 6.07f, 7.08f),
    OriginOSMorphCheckpoint(0.800f, 190.36f, 169.96f, -2.31f, 1.73f, 23.60f, 1.54f, 1.54f, 23.60f, 25.14f, 25.14f, 0.77f, 9.99f, 6.92f, 4.61f, 5.38f),
    OriginOSMorphCheckpoint(0.840f, 194.18f, 180.18f, -1.61f, 1.21f, 23.68f, 1.07f, 1.07f, 23.68f, 24.75f, 24.75f, 0.54f, 6.98f, 4.83f, 3.22f, 3.76f),
    OriginOSMorphCheckpoint(0.880f, 198.06f, 190.70f, -0.99f, 0.74f, 23.76f, 0.66f, 0.66f, 23.76f, 24.42f, 24.42f, 0.33f, 4.30f, 2.98f, 1.99f, 2.32f),
    OriginOSMorphCheckpoint(0.920f, 201.99f, 201.52f, -0.49f, 0.37f, 23.84f, 0.33f, 0.33f, 23.84f, 24.17f, 24.17f, 0.16f, 2.12f, 1.47f, 0.98f, 1.14f),
    OriginOSMorphCheckpoint(0.960f, 205.97f, 212.62f, -0.14f, 0.11f, 23.92f, 0.10f, 0.10f, 23.92f, 24.02f, 24.02f, 0.05f, 0.62f, 0.43f, 0.29f, 0.33f),
    OriginOSMorphCheckpoint(1.000f, 210.00f, 224.00f, 0.00f, 0.00f, 24.00f, 0.00f, 0.00f, 24.00f, 24.00f, 24.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f)
)

fun interpolateCheckpoint(p: Float, isExpanding: Boolean): OriginOSMorphCheckpoint {
    val table = if (isExpanding) CHECKPOINTS_OPENING_26 else CHECKPOINTS_CLOSING_26
    val clampedP = p.fastCoerceIn(0f, 1f)
    val floatIndex = clampedP * 25f
    val index1 = floatIndex.toInt().coerceIn(0, 24)
    val index2 = (index1 + 1).coerceIn(0, 25)
    val fraction = floatIndex - index1
    val c1 = table[index1]
    val c2 = table[index2]
    return OriginOSMorphCheckpoint(
        progress = clampedP,
        width = lerp(c1.width, c2.width, fraction),
        height = lerp(c1.height, c2.height, fraction),
        trOffsetX = lerp(c1.trOffsetX, c2.trOffsetX, fraction),
        trOffsetY = lerp(c1.trOffsetY, c2.trOffsetY, fraction),
        trRadius = lerp(c1.trRadius, c2.trRadius, fraction),
        tlOffsetX = lerp(c1.tlOffsetX, c2.tlOffsetX, fraction),
        tlOffsetY = lerp(c1.tlOffsetY, c2.tlOffsetY, fraction),
        tlRadius = lerp(c1.tlRadius, c2.tlRadius, fraction),
        blRadius = lerp(c1.blRadius, c2.blRadius, fraction),
        brRadius = lerp(c1.brRadius, c2.brRadius, fraction),
        topEdgeDip = lerp(c1.topEdgeDip, c2.topEdgeDip, fraction),
        bottomSag = lerp(c1.bottomSag, c2.bottomSag, fraction),
        leftWaist = lerp(c1.leftWaist, c2.leftWaist, fraction),
        leftBulge = lerp(c1.leftBulge, c2.leftBulge, fraction),
        rightWaist = lerp(c1.rightWaist, c2.rightWaist, fraction)
    )
}

/**
 * Liquid morphing shape that generates continuous Bézier paths conforming to the
 * 26 checkpoints for all 4 edges and all 4 corners.
 */
class OriginOSFluidMorphShape(
    val checkpoint: OriginOSMorphCheckpoint,
    val extraPaddingPx: Float,
    val isLiquidFusionEnabled: Boolean = true
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return Outline.Rectangle(Rect.Zero)

        val cp = checkpoint
        val d = density.density
        val anchorRight = w - extraPaddingPx
        val anchorTop = extraPaddingPx

        if (!isLiquidFusionEnabled) {
            val curW = cp.width * d
            val curH = cp.height * d
            val curR = cp.trRadius * d
            val rect = RoundRect(
                left = anchorRight - curW,
                top = anchorTop,
                right = anchorRight,
                bottom = anchorTop + curH,
                cornerRadius = CornerRadius(curR, curR)
            )
            return Outline.Rounded(rect)
        }

        val targetW = cp.width * d
        val targetH = cp.height * d

        // All 4 Corner Positions
        val tlX = anchorRight - targetW + cp.tlOffsetX * d
        val tlY = anchorTop + cp.tlOffsetY * d
        val trX = anchorRight + cp.trOffsetX * d
        val trY = anchorTop + cp.trOffsetY * d
        val blX = anchorRight - targetW
        val blY = anchorTop + targetH
        val brX = anchorRight
        val brY = anchorTop + targetH

        // Corner Radii
        val trRadius = cp.trRadius * d
        val tlRadius = cp.tlRadius * d
        val brRadius = cp.brRadius * d
        val blRadius = cp.blRadius * d

        // Dynamic Curvatures
        val topEdgeDip = cp.topEdgeDip * d
        val botSag = cp.bottomSag * d
        val leftWaist = cp.leftWaist * d
        val leftBulge = cp.leftBulge * d
        val rightWaist = cp.rightWaist * d

        val path = Path().apply {
            val K_tr = 0.5522847f * trRadius
            val K_tl = 0.5522847f * tlRadius
            val K_br = 0.5522847f * brRadius
            val K_bl = 0.5522847f * blRadius

            val startTopX = tlX + tlRadius
            val startTopY = tlY
            val endTopX = trX - trRadius
            val endTopY = trY

            moveTo(startTopX, startTopY)

            // --- 1. Top Edge: dynamic curvature (plunge dip vs rising dome arch) ---
            if (abs(topEdgeDip) > 0.5f && endTopX > startTopX) {
                val midTopX = (startTopX + endTopX) * 0.5f
                val midTopY = (startTopY + endTopY) * 0.5f + topEdgeDip
                val cp1X = startTopX + (midTopX - startTopX) * 0.55f
                val cp1Y = startTopY + topEdgeDip * 0.70f
                val cp2X = midTopX - (midTopX - startTopX) * 0.45f
                val cp2Y = midTopY
                cubicTo(cp1X, cp1Y, cp2X, cp2Y, midTopX, midTopY)

                val cp3X = midTopX + (endTopX - midTopX) * 0.45f
                val cp3Y = midTopY
                val cp4X = endTopX - (endTopX - midTopX) * 0.55f
                val cp4Y = endTopY + topEdgeDip * 0.70f
                cubicTo(cp3X, cp3Y, cp4X, cp4Y, endTopX, endTopY)
            } else {
                lineTo(endTopX, endTopY)
            }

            // --- 2. Top-Right Corner ---
            cubicTo(
                endTopX + K_tr, endTopY,
                trX, trY + trRadius - K_tr,
                trX, trY + trRadius
            )

            // --- 3. Right Flank ---
            val rfStartX = trX
            val rfStartY = trY + trRadius
            val rfEndX = brX
            val rfEndY = brY - brRadius
            if (rightWaist > 0.5f && rfEndY > rfStartY) {
                val rfSpan = rfEndY - rfStartY
                val cp1X = trX - rightWaist * 0.8f
                val cp1Y = rfStartY + rfSpan * 0.35f
                val cp2X = brX - rightWaist * 0.2f
                val cp2Y = rfStartY + rfSpan * 0.75f
                cubicTo(cp1X, cp1Y, cp2X, cp2Y, rfEndX, rfEndY)
            } else {
                lineTo(rfEndX, rfEndY)
            }

            // --- 4. Bottom-Right Corner ---
            cubicTo(
                brX, brY - brRadius + K_br,
                brX - brRadius + K_br, brY,
                brX - brRadius, brY
            )

            // --- 5. Bottom Edge: viscous droplet sag ---
            val startBotX = brX - brRadius
            val startBotY = brY
            val endBotX = blX + blRadius
            val endBotY = blY
            if (botSag > 0.5f && startBotX > endBotX) {
                val midBotX = (startBotX + endBotX) * 0.5f
                val midBotY = (startBotY + endBotY) * 0.5f + botSag
                val bCp1X = startBotX - (startBotX - midBotX) * 0.5f
                val bCp1Y = startBotY + botSag * 0.75f
                val bCp2X = midBotX + (startBotX - midBotX) * 0.5f
                val bCp2Y = midBotY
                cubicTo(bCp1X, bCp1Y, bCp2X, bCp2Y, midBotX, midBotY)

                val bCp3X = midBotX - (midBotX - endBotX) * 0.5f
                val bCp3Y = midBotY
                val bCp4X = endBotX + (midBotX - endBotX) * 0.5f
                val bCp4Y = endBotY + botSag * 0.75f
                cubicTo(bCp3X, bCp3Y, bCp4X, bCp4Y, endBotX, endBotY)
            } else {
                lineTo(endBotX, endBotY)
            }

            // --- 6. Bottom-Left Corner ---
            cubicTo(
                blX + blRadius - K_bl, blY,
                blX, blY - blRadius + K_bl,
                blX, blY - blRadius
            )

            // --- 7. Left Flank: organic bell bulge & waist ---
            val lfStartX = blX
            val lfStartY = blY - blRadius
            val lfEndX = tlX
            val lfEndY = tlY + tlRadius
            if ((leftWaist > 0.5f || leftBulge > 0.5f) && lfStartY > lfEndY) {
                val lfSpan = lfStartY - lfEndY
                val cp1X = blX - leftBulge * 0.6f
                val cp1Y = lfStartY - lfSpan * 0.35f
                val cp2X = tlX + leftWaist * 0.7f
                val cp2Y = lfStartY - lfSpan * 0.75f
                cubicTo(cp1X, cp1Y, cp2X, cp2Y, lfEndX, lfEndY)
            } else {
                lineTo(lfEndX, lfEndY)
            }

            // --- 8. Top-Left Corner ---
            cubicTo(
                tlX, tlY + tlRadius - K_tl,
                tlX + tlRadius - K_tl, tlY,
                tlX + tlRadius, tlY
            )

            close()
        }

        return Outline.Generic(path)
    }
}

data class OriginOSMenuItem(
    val title: String,
    val icon: (@Composable () -> Unit)? = null,
    val onClick: () -> Unit = {}
)

/**
 * 1:1 OriginOS 7 Morphing Dropdown Menu.
 *
 * Sits on the TOPMOST visual layer.
 * Supports:
 * - Liquid Glass Backdrop Mode (with blur, lens, vibrancy, highlight)
 * - 100% Solid Opaque Mode (crisp opaque card with realistic shadow)
 * - Symmetrical, synchronized open & close speeds with user-controlled multipliers
 * - Frame-by-frame 26-checkpoint fluid morphing for all 4 edges & all 4 corners
 */
@Composable
fun OriginOSDropdownMenu(
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    isGlassEnabled: Boolean = true,
    isLiquidFusionEnabled: Boolean = true,
    animationSpeedMultiplier: Float = 1.0f,
    menuItems: List<OriginOSMenuItem> = defaultOriginOSMenuItems(),
    onExpandToggle: () -> Unit = {}
) {
    val density = LocalDensity.current
    val isLightTheme = !isSystemInDarkTheme()
    val textPrimary = if (isLightTheme) Color(0xFF161616) else Color(0xFFEEEEEE)

    val animProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var runningJob by remember { mutableStateOf<Job?>(null) }

    // Synchronize opening and closing durations symmetrically:
    val baseDuration = 340f
    val totalDuration = (baseDuration / animationSpeedMultiplier).toInt().coerceAtLeast(16)

    LaunchedEffect(isExpanded, animationSpeedMultiplier) {
        runningJob?.cancel()
        runningJob = scope.launch {
            if (isExpanded) {
                animProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = totalDuration, easing = LinearEasing)
                )
            } else {
                animProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = totalDuration, easing = LinearEasing)
                )
            }
        }
    }

    val p = animProgress.value
    val checkpoint = remember(p, isExpanded) {
        interpolateCheckpoint(p, isExpanding = isExpanded)
    }

    val extraPadding = 36.dp
    val extraPaddingPx = with(density) { extraPadding.toPx() }

    val morphShape = remember(checkpoint, extraPaddingPx, isLiquidFusionEnabled) {
        OriginOSFluidMorphShape(
            checkpoint = checkpoint,
            extraPaddingPx = extraPaddingPx,
            isLiquidFusionEnabled = isLiquidFusionEnabled
        )
    }

    val pillWidth = 132.dp
    val pillHeight = 44.dp
    val targetWidth = 210.dp
    val targetHeight = 224.dp

    // Canvas size encompassing shadow, dome arch, and droplet sag bounds
    val canvasWidth = targetWidth + extraPadding * 2
    val canvasHeight = targetHeight + extraPadding * 2 + 40.dp

    // Fixed root layout bounds: exactly matches the pill at all times, preventing any screen shift!
    Box(
        modifier = modifier.size(pillWidth, pillHeight)
    ) {
        // Unbounded child positioned so that (anchorRight, anchorTop) aligns exactly with (pillWidth, 0.dp)
        Box(
            modifier = Modifier
                .wrapContentSize(align = Alignment.TopStart, unbounded = true)
                .offset(
                    x = pillWidth - (canvasWidth - extraPadding),
                    y = -extraPadding
                )
                .size(canvasWidth, canvasHeight)
        ) {
            // --- 1. CONTOUR SURFACE CANVAS (Glass vs Opaque) ---
            if (isGlassEnabled) {
                // Liquid Glass Backdrop Mode
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { morphShape },
                            effects = {
                                vibrancy()
                                blur(18f.dp.toPx())
                                lens(14f.dp.toPx(), 22f.dp.toPx(), depthEffect = true)
                            },
                            highlight = { Highlight.Default.copy(alpha = 0.70f) },
                            shadow = { Shadow(radius = 18.dp, color = Color.Black.copy(alpha = 0.16f)) },
                            innerShadow = { InnerShadow(radius = 10.dp, color = Color.White.copy(alpha = 0.35f)) },
                            onDrawSurface = {
                                drawRect(
                                    if (isLightTheme) Color.White.copy(alpha = 0.72f)
                                    else Color(0xFF222224).copy(alpha = 0.78f)
                                )
                            }
                        )
                )
            } else {
                // 100% Solid Opaque Mode (Crisp shadow, zero transparency)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(
                            elevation = 14.dp,
                            shape = morphShape,
                            ambientColor = Color.Black.copy(alpha = 0.14f),
                            spotColor = Color.Black.copy(alpha = 0.20f)
                        )
                        .clip(morphShape)
                        .background(if (isLightTheme) Color.White else Color(0xFF242426))
                )
            }

            // --- 2. Action Pill Content (Search | + | ⋮) ---
            val pillAlpha = (1f - p / 0.12f).fastCoerceIn(0f, 1f)
            if (pillAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .offset(
                            x = canvasWidth - extraPadding - pillWidth,
                            y = extraPadding
                        )
                        .size(pillWidth, pillHeight)
                        .alpha(pillAlpha)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            SearchPillIcon(textPrimary)
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            PlusPillIcon(textPrimary)
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onExpandToggle() },
                            contentAlignment = Alignment.Center
                        ) {
                            ThreeDotsPillIcon(textPrimary)
                        }
                    }
                }
            }

            // --- 3. Dropdown Menu Items Content ---
            val menuAlpha = ((p - 0.65f) / 0.30f).fastCoerceIn(0f, 1f)
            if (menuAlpha > 0.01f) {
                Column(
                    modifier = Modifier
                        .offset(
                            x = canvasWidth - extraPadding - targetWidth,
                            y = extraPadding
                        )
                        .size(targetWidth, targetHeight)
                        .alpha(menuAlpha)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    menuItems.forEachIndexed { index, item ->
                        val itemSlideY = lerp(
                            14f,
                            0f,
                            ((p - 0.65f - index * 0.035f) / 0.25f).fastCoerceIn(0f, 1f)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .graphicsLayer { translationY = itemSlideY }
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    item.onClick()
                                    onDismissRequest()
                                }
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicText(
                                text = item.title,
                                style = TextStyle(
                                    color = if (isLightTheme) Color(0xFF161616) else Color(0xFFEEEEEE),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }

                        if (index < menuItems.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.6.dp)
                                    .padding(horizontal = 16.dp)
                                    .background(
                                        if (isLightTheme) Color.Black.copy(alpha = 0.06f)
                                        else Color.White.copy(alpha = 0.08f)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

fun defaultOriginOSMenuItems(): List<OriginOSMenuItem> = listOf(
    OriginOSMenuItem("Layout"),
    OriginOSMenuItem("Sort and hide"),
    OriginOSMenuItem("Collapse all"),
    OriginOSMenuItem("Settings")
)

@Composable
internal fun SearchPillIcon(tint: Color) {
    Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
            val r = 5.dp.toPx()
            val cx = 7.dp.toPx()
            val cy = 7.dp.toPx()
            drawCircle(
                color = tint,
                radius = r,
                center = Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
            drawLine(
                color = tint,
                start = Offset(cx + r * 0.707f, cy + r * 0.707f),
                end = Offset(14.dp.toPx(), 14.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
internal fun PlusPillIcon(tint: Color) {
    Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
            val stroke = 2.dp.toPx()
            drawLine(tint, Offset(9.dp.toPx(), 4.dp.toPx()), Offset(9.dp.toPx(), 14.dp.toPx()), stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(tint, Offset(4.dp.toPx(), 9.dp.toPx()), Offset(14.dp.toPx(), 9.dp.toPx()), stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }
}

@Composable
internal fun ThreeDotsPillIcon(tint: Color) {
    Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
            val r = 1.6.dp.toPx()
            val cx = 9.dp.toPx()
            drawCircle(tint, r, Offset(cx, 4.dp.toPx()))
            drawCircle(tint, r, Offset(cx, 9.dp.toPx()))
            drawCircle(tint, r, Offset(cx, 14.dp.toPx()))
        }
    }
}
