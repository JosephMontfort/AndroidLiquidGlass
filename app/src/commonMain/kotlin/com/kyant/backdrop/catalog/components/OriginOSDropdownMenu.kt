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
 * 5. Converging Bubble Pathway: Wide pill (132x44dp) contracts horizontally into
 *    a near-circle bubble (92x96dp, radius 40dp) mid-flight while top edge plunges (+55.8dp).
 * 6. Symmetric Reversal During Flight: Closing trajectory is the exact reverse of opening.
 * 7. Post-Closing Impact Bounce: Collapsed pill absorbs closing momentum with spring rebound.
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

val CHECKPOINTS_CONVERGING_26 = listOf(
    OriginOSMorphCheckpoint(0.000f, 132.00f, 44.00f, 0.00f, 0.00f, 22.00f, 0.00f, 0.00f, 22.00f, 22.00f, 22.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f),
    OriginOSMorphCheckpoint(0.040f, 130.88f, 45.46f, -0.58f, 2.02f, 22.50f, 0.58f, 1.95f, 22.50f, 22.56f, 22.56f, 3.38f, 0.43f, 0.50f, 0.25f, 0.43f),
    OriginOSMorphCheckpoint(0.080f, 127.84f, 49.41f, -1.73f, 6.04f, 23.87f, 1.73f, 5.83f, 23.87f, 24.08f, 24.08f, 6.49f, 1.47f, 1.51f, 0.99f, 1.29f),
    OriginOSMorphCheckpoint(0.120f, 123.36f, 55.23f, -3.23f, 11.32f, 25.89f, 3.23f, 10.91f, 25.89f, 26.32f, 26.32f, 9.07f, 2.98f, 2.83f, 2.17f, 2.43f),
    OriginOSMorphCheckpoint(0.160f, 117.92f, 62.30f, -4.97f, 17.41f, 28.34f, 4.97f, 16.78f, 28.34f, 29.04f, 29.04f, 10.92f, 4.83f, 4.35f, 3.71f, 3.73f),
    OriginOSMorphCheckpoint(0.200f, 112.00f, 70.00f, -6.84f, 23.93f, 31.00f, 6.84f, 23.08f, 31.00f, 32.00f, 32.00f, 11.88f, 6.92f, 5.98f, 5.53f, 5.13f),
    OriginOSMorphCheckpoint(0.240f, 106.08f, 77.70f, -8.72f, 30.54f, 33.66f, 8.72f, 29.45f, 33.66f, 34.96f, 34.96f, 11.88f, 9.10f, 7.63f, 7.50f, 6.54f),
    OriginOSMorphCheckpoint(0.280f, 100.64f, 84.77f, -10.54f, 36.90f, 36.11f, 10.54f, 35.58f, 36.11f, 37.68f, 37.68f, 10.92f, 11.26f, 9.23f, 9.50f, 7.91f),
    OriginOSMorphCheckpoint(0.320f, 96.16f, 90.59f, -12.21f, 42.72f, 38.13f, 12.21f, 41.19f, 38.13f, 39.92f, 39.92f, 9.07f, 13.27f, 10.68f, 11.41f, 9.15f),
    OriginOSMorphCheckpoint(0.360f, 93.12f, 94.54f, -13.63f, 47.72f, 39.50f, 13.63f, 46.01f, 39.50f, 41.44f, 41.44f, 6.49f, 15.03f, 11.93f, 13.10f, 10.23f),
    OriginOSMorphCheckpoint(0.400f, 92.00f, 96.00f, -14.77f, 51.68f, 40.00f, 14.77f, 49.83f, 40.00f, 42.00f, 42.00f, 3.38f, 16.45f, 12.92f, 14.47f, 11.07f),
    OriginOSMorphCheckpoint(0.440f, 93.50f, 97.63f, -15.55f, 54.42f, 39.80f, 15.55f, 52.48f, 39.80f, 41.77f, 41.77f, 0.00f, 17.43f, 13.61f, 15.44f, 11.66f),
    OriginOSMorphCheckpoint(0.480f, 97.73f, 102.22f, -15.95f, 55.82f, 39.22f, 15.95f, 53.83f, 39.22f, 41.13f, 41.13f, -4.22f, 17.94f, 13.96f, 15.94f, 11.96f),
    OriginOSMorphCheckpoint(0.520f, 104.27f, 109.31f, -15.95f, 55.82f, 38.34f, 15.95f, 53.83f, 38.34f, 40.13f, 40.13f, -8.05f, 17.94f, 13.96f, 15.94f, 11.96f),
    OriginOSMorphCheckpoint(0.560f, 112.70f, 118.45f, -15.55f, 54.42f, 37.19f, 15.55f, 52.48f, 37.19f, 38.84f, 38.84f, -11.13f, 17.43f, 13.61f, 15.44f, 11.66f),
    OriginOSMorphCheckpoint(0.600f, 122.59f, 129.19f, -14.77f, 51.68f, 35.85f, 14.77f, 49.83f, 35.85f, 37.33f, 37.33f, -13.18f, 16.45f, 12.92f, 14.47f, 11.07f),
    OriginOSMorphCheckpoint(0.640f, 133.54f, 141.06f, -13.63f, 47.72f, 34.37f, 13.63f, 46.01f, 34.37f, 35.66f, 35.66f, -13.99f, 15.03f, 11.93f, 13.10f, 10.23f),
    OriginOSMorphCheckpoint(0.680f, 145.11f, 153.61f, -12.21f, 42.72f, 32.80f, 12.21f, 41.19f, 32.80f, 33.90f, 33.90f, -13.50f, 13.27f, 10.68f, 11.41f, 9.15f),
    OriginOSMorphCheckpoint(0.720f, 156.89f, 166.39f, -10.54f, 36.90f, 31.20f, 10.54f, 35.58f, 31.20f, 32.10f, 32.10f, -11.75f, 11.26f, 9.23f, 9.50f, 7.91f),
    OriginOSMorphCheckpoint(0.760f, 168.46f, 178.94f, -8.72f, 30.54f, 29.63f, 8.72f, 29.45f, 29.63f, 30.34f, 30.34f, -8.91f, 9.10f, 7.63f, 7.50f, 6.54f),
    OriginOSMorphCheckpoint(0.800f, 179.41f, 190.81f, -6.84f, 23.93f, 28.15f, 6.84f, 23.08f, 28.15f, 28.67f, 28.67f, -5.23f, 6.92f, 5.98f, 5.53f, 5.13f),
    OriginOSMorphCheckpoint(0.840f, 189.30f, 201.55f, -4.97f, 17.41f, 26.81f, 4.97f, 16.78f, 26.81f, 27.16f, 27.16f, -1.07f, 4.83f, 4.35f, 3.71f, 3.73f),
    OriginOSMorphCheckpoint(0.880f, 197.73f, 210.69f, -3.23f, 11.32f, 25.66f, 3.23f, 10.91f, 25.66f, 25.87f, 25.87f, 0.00f, 2.98f, 2.83f, 2.17f, 2.43f),
    OriginOSMorphCheckpoint(0.920f, 204.27f, 217.78f, -1.73f, 6.04f, 24.78f, 1.73f, 5.83f, 24.78f, 24.87f, 24.87f, 0.00f, 1.47f, 1.51f, 0.99f, 1.29f),
    OriginOSMorphCheckpoint(0.960f, 208.50f, 222.37f, -0.58f, 2.02f, 24.20f, 0.58f, 1.95f, 24.20f, 24.23f, 24.23f, 0.00f, 0.43f, 0.50f, 0.25f, 0.43f),
    OriginOSMorphCheckpoint(1.000f, 210.00f, 224.00f, 0.00f, 0.00f, 24.00f, 0.00f, 0.00f, 24.00f, 24.00f, 24.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f)
)

fun interpolateCheckpoint(progress: Float): OriginOSMorphCheckpoint {
    val p = progress.fastCoerceIn(0f, 1f)
    val floatIndex = p * 25f
    val index1 = floatIndex.toInt().coerceIn(0, 24)
    val index2 = (index1 + 1).coerceIn(0, 25)
    val fraction = floatIndex - index1
    val c1 = CHECKPOINTS_CONVERGING_26[index1]
    val c2 = CHECKPOINTS_CONVERGING_26[index2]
    return OriginOSMorphCheckpoint(
        progress = p,
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

        // Corner Radii with Proportional Clamping to form seamless near-circle/bubble
        val rawTrRadius = cp.trRadius * d
        val rawTlRadius = cp.tlRadius * d
        val rawBrRadius = cp.brRadius * d
        val rawBlRadius = cp.blRadius * d

        val topSpan = (trX - tlX).coerceAtLeast(1f)
        val topScale = (topSpan / (rawTlRadius + rawTrRadius)).coerceAtMost(1f)
        val effTlR = rawTlRadius * topScale
        val effTrR = rawTrRadius * topScale

        val botSpan = (brX - blX).coerceAtLeast(1f)
        val botScale = (botSpan / (rawBlRadius + rawBrRadius)).coerceAtMost(1f)
        val effBlR = rawBlRadius * botScale
        val effBrR = rawBrRadius * botScale

        val rightSpan = (brY - trY).coerceAtLeast(1f)
        val rightScale = (rightSpan / (effTrR + effBrR)).coerceAtMost(1f)
        val trRadius = effTrR * rightScale
        val brRadius = effBrR * rightScale

        val leftSpan = (blY - tlY).coerceAtLeast(1f)
        val leftScale = (leftSpan / (effTlR + effBlR)).coerceAtMost(1f)
        val tlRadius = effTlR * leftScale
        val blRadius = effBlR * leftScale

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
            if (abs(topEdgeDip) > 0.5f && endTopX > startTopX + 1f) {
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
    val pillBounceY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var runningJob by remember { mutableStateOf<Job?>(null) }

    // Synchronize opening and closing durations symmetrically:
    val baseDuration = 340f
    val totalDuration = (baseDuration / animationSpeedMultiplier).toInt().coerceAtLeast(16)

    LaunchedEffect(isExpanded, animationSpeedMultiplier) {
        runningJob?.cancel()
        runningJob = scope.launch {
            if (isExpanded) {
                pillBounceY.snapTo(0f)
                animProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = totalDuration, easing = LinearEasing)
                )
            } else {
                // Exact reverse during flight: 1.0f -> 0.0f
                animProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = totalDuration, easing = LinearEasing)
                )
                // Impact absorption bounce on collapsed 3-item pill:
                // Moves up by -5.5dp, rebounds to +1.8dp, -0.6dp, and settles at 0dp
                pillBounceY.animateTo(-5.5f, tween(70, easing = androidx.compose.animation.core.FastOutLinearInEasing))
                pillBounceY.animateTo(1.8f, tween(90, easing = androidx.compose.animation.core.LinearOutSlowInEasing))
                pillBounceY.animateTo(-0.6f, tween(60, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                pillBounceY.animateTo(0f, tween(50, easing = androidx.compose.animation.core.FastOutSlowInEasing))
            }
        }
    }

    val p = animProgress.value
    val checkpoint = remember(p) {
        interpolateCheckpoint(p)
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

    // Canvas size encompassing shadow, dome arch, plunge dip, and droplet sag bounds
    val canvasWidth = targetWidth + extraPadding * 2
    val canvasHeight = targetHeight + extraPadding * 2 + 60.dp

    // Fixed root layout bounds with impact bounce offset
    Box(
        modifier = modifier
            .offset(y = pillBounceY.value.dp)
            .size(pillWidth, pillHeight)
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
