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
import androidx.compose.ui.unit.Dp
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

/**
 * OriginOS 7 26-Checkpoint Fluid Morph Model.
 *
 * Meticulously reconstructed from frame-by-frame analysis of the OriginOS 7 Gallery
 * dropdown animation (60 frames at 30fps).
 */
data class OriginOSMorphCheckpoint(
    val progress: Float,
    val width: Float,
    val height: Float,
    val trOffsetX: Float, // top-right corner X offset (negative = inward shift)
    val trOffsetY: Float, // top-right corner Y offset (positive = downward shift)
    val trRadius: Float,  // top-right corner radius
    val tlRadius: Float,  // top-left corner radius
    val blRadius: Float,  // bottom-left corner radius
    val brRadius: Float,  // bottom-right corner radius
    val leftWaist: Float, // concave neck indentation on left flank
    val rightWaist: Float,// concave neck indentation on right flank
    val topArch: Float,   // convex dome arch bulge upwards
    val bottomSag: Float  // viscous droplet sag downwards
)

val CHECKPOINTS_26 = listOf(
    OriginOSMorphCheckpoint(0.000f, 132.00f, 44.00f, 0.00f, 0.00f, 22.00f, 22.00f, 22.00f, 22.00f, 0.00f, 0.00f, 0.00f, 0.00f),
    OriginOSMorphCheckpoint(0.040f, 123.08f, 50.50f, -3.84f, 2.31f, 17.93f, 22.13f, 22.13f, 22.13f, 6.34f, 3.86f, 0.00f, 0.00f),
    OriginOSMorphCheckpoint(0.080f, 120.07f, 60.65f, -7.54f, 4.53f, 14.57f, 22.27f, 22.27f, 22.27f, 12.35f, 7.42f, 0.00f, 3.07f),
    OriginOSMorphCheckpoint(0.120f, 124.95f, 72.49f, -10.96f, 6.58f, 12.49f, 22.40f, 22.40f, 22.40f, 17.72f, 10.40f, 0.00f, 7.01f),
    OriginOSMorphCheckpoint(0.160f, 125.03f, 85.28f, -13.97f, 8.38f, 12.05f, 22.53f, 22.53f, 22.53f, 22.17f, 12.58f, 0.94f, 10.58f),
    OriginOSMorphCheckpoint(0.200f, 143.38f, 98.60f, -16.46f, 9.88f, 13.34f, 22.67f, 22.67f, 22.67f, 25.47f, 13.79f, 4.64f, 13.60f),
    OriginOSMorphCheckpoint(0.240f, 159.01f, 112.13f, -18.34f, 11.00f, 16.12f, 22.80f, 22.80f, 22.80f, 27.44f, 13.92f, 8.04f, 15.92f),
    OriginOSMorphCheckpoint(0.280f, 172.14f, 125.62f, -19.53f, 11.72f, 19.92f, 22.93f, 22.93f, 22.93f, 27.99f, 12.98f, 10.93f, 17.41f),
    OriginOSMorphCheckpoint(0.320f, 183.00f, 138.89f, -19.99f, 12.00f, 16.94f, 23.07f, 23.07f, 23.07f, 27.08f, 11.03f, 13.14f, 17.99f),
    OriginOSMorphCheckpoint(0.360f, 191.79f, 151.76f, -19.71f, 11.83f, 18.80f, 23.20f, 23.20f, 23.20f, 24.76f, 8.23f, 14.53f, 17.64f),
    OriginOSMorphCheckpoint(0.400f, 198.75f, 164.08f, -18.70f, 11.22f, 20.59f, 23.33f, 23.33f, 23.33f, 21.16f, 4.79f, 15.00f, 16.37f),
    OriginOSMorphCheckpoint(0.440f, 204.07f, 175.73f, -16.99f, 10.19f, 22.27f, 23.47f, 23.47f, 23.47f, 16.46f, 0.98f, 14.53f, 14.25f),
    OriginOSMorphCheckpoint(0.480f, 207.98f, 186.60f, -14.65f, 8.79f, 23.79f, 23.60f, 23.60f, 23.60f, 10.90f, 0.00f, 13.14f, 11.39f),
    OriginOSMorphCheckpoint(0.520f, 210.70f, 196.57f, -11.76f, 7.05f, 25.12f, 23.73f, 23.73f, 23.73f, 4.77f, 0.00f, 10.93f, 7.94f),
    OriginOSMorphCheckpoint(0.560f, 212.45f, 205.56f, -8.43f, 5.06f, 26.23f, 23.87f, 23.87f, 23.87f, 0.00f, 0.00f, 8.04f, 4.08f),
    OriginOSMorphCheckpoint(0.600f, 213.44f, 213.47f, -4.79f, 2.87f, 27.09f, 24.00f, 24.00f, 24.00f, 0.00f, 0.00f, 4.64f, 0.00f),
    OriginOSMorphCheckpoint(0.640f, 213.88f, 220.22f, -0.97f, 0.58f, 27.67f, 24.00f, 24.00f, 24.00f, 0.00f, 0.00f, 0.94f, 0.00f),
    OriginOSMorphCheckpoint(0.680f, 214.00f, 225.73f, 0.00f, 0.00f, 27.96f, 24.00f, 24.00f, 24.00f, 0.00f, 0.00f, 0.00f, 0.00f),
    OriginOSMorphCheckpoint(0.720f, 213.58f, 227.58f, 0.00f, 0.00f, 27.73f, 24.00f, 24.00f, 24.00f, 0.00f, 0.00f, 0.00f, 0.00f),
    OriginOSMorphCheckpoint(0.760f, 212.76f, 226.76f, 0.00f, 0.00f, 27.20f, 24.00f, 24.00f, 24.00f, 0.00f, 0.00f, 0.00f, 0.00f),
    OriginOSMorphCheckpoint(0.800f, 212.00f, 226.00f, 0.00f, 0.00f, 26.67f, 24.00f, 24.00f, 24.00f, 0.00f, 0.00f, 0.00f, 0.00f),
    OriginOSMorphCheckpoint(0.840f, 211.32f, 225.32f, 0.00f, 0.00f, 26.13f, 24.00f, 24.00f, 24.00f, 0.00f, 0.00f, 0.00f, 0.00f),
    OriginOSMorphCheckpoint(0.880f, 210.76f, 224.76f, 0.00f, 0.00f, 25.60f, 24.00f, 24.00f, 24.00f, 0.00f, 0.00f, 0.00f, 0.00f),
    OriginOSMorphCheckpoint(0.920f, 210.35f, 224.35f, 0.00f, 0.00f, 25.07f, 24.00f, 24.00f, 24.00f, 0.00f, 0.00f, 0.00f, 0.00f),
    OriginOSMorphCheckpoint(0.960f, 210.09f, 224.09f, 0.00f, 0.00f, 24.53f, 24.00f, 24.00f, 24.00f, 0.00f, 0.00f, 0.00f, 0.00f),
    OriginOSMorphCheckpoint(1.000f, 210.00f, 224.00f, 0.00f, 0.00f, 24.00f, 24.00f, 24.00f, 24.00f, 0.00f, 0.00f, 0.00f, 0.00f)
)

fun interpolateCheckpoint(p: Float): OriginOSMorphCheckpoint {
    val clampedP = p.fastCoerceIn(0f, 1f)
    val floatIndex = clampedP * 25f
    val index1 = floatIndex.toInt().coerceIn(0, 24)
    val index2 = (index1 + 1).coerceIn(0, 25)
    val fraction = floatIndex - index1
    val c1 = CHECKPOINTS_26[index1]
    val c2 = CHECKPOINTS_26[index2]
    return OriginOSMorphCheckpoint(
        progress = clampedP,
        width = lerp(c1.width, c2.width, fraction),
        height = lerp(c1.height, c2.height, fraction),
        trOffsetX = lerp(c1.trOffsetX, c2.trOffsetX, fraction),
        trOffsetY = lerp(c1.trOffsetY, c2.trOffsetY, fraction),
        trRadius = lerp(c1.trRadius, c2.trRadius, fraction),
        tlRadius = lerp(c1.tlRadius, c2.tlRadius, fraction),
        blRadius = lerp(c1.blRadius, c2.blRadius, fraction),
        brRadius = lerp(c1.brRadius, c2.brRadius, fraction),
        leftWaist = lerp(c1.leftWaist, c2.leftWaist, fraction),
        rightWaist = lerp(c1.rightWaist, c2.rightWaist, fraction),
        topArch = lerp(c1.topArch, c2.topArch, fraction),
        bottomSag = lerp(c1.bottomSag, c2.bottomSag, fraction)
    )
}

/**
 * Liquid morphing shape that generates continuous Bézier paths conforming to the
 * 26 checkpoints.
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
        val trX = anchorRight + cp.trOffsetX * d
        val trY = anchorTop + cp.trOffsetY * d
        val left = anchorRight - targetW
        val bottom = anchorTop + targetH

        val trRadius = cp.trRadius * d
        val tlRadius = cp.tlRadius * d
        val brRadius = cp.brRadius * d
        val blRadius = cp.blRadius * d

        val topArch = cp.topArch * d
        val botSag = cp.bottomSag * d
        val leftWaist = cp.leftWaist * d
        val rightWaist = cp.rightWaist * d

        val path = Path().apply {
            val K_tr = 0.5522847f * trRadius
            val K_tl = 0.5522847f * tlRadius
            val K_br = 0.5522847f * brRadius
            val K_bl = 0.5522847f * blRadius

            val startTopX = left + tlRadius
            val startTopY = anchorTop
            val endTopX = trX - trRadius
            val endTopY = trY

            moveTo(startTopX, startTopY)

            // 1. Top Edge with convex topArch dome
            if (topArch > 0.5f && endTopX > startTopX) {
                val midTopX = (startTopX + endTopX) * 0.5f
                val midTopY = (startTopY + endTopY) * 0.5f - topArch
                cubicTo(
                    startTopX + (midTopX - startTopX) * 0.5f, startTopY - topArch * 0.75f,
                    midTopX - (midTopX - startTopX) * 0.5f, midTopY,
                    midTopX, midTopY
                )
                cubicTo(
                    midTopX + (endTopX - midTopX) * 0.5f, midTopY,
                    endTopX - (endTopX - midTopX) * 0.5f, endTopY - topArch * 0.75f,
                    endTopX, endTopY
                )
            } else {
                lineTo(endTopX, endTopY)
            }

            // 2. Top-Right Corner (dynamically shifted position & radius)
            cubicTo(
                endTopX + K_tr, endTopY,
                trX, trY + trRadius - K_tr,
                trX, trY + trRadius
            )

            // 3. Right Flank with rightWaist neck
            val rfStartY = trY + trRadius
            val rfEndX = anchorRight
            val rfEndY = bottom - brRadius
            if (rightWaist > 0.5f && rfEndY > rfStartY) {
                val waistY = rfStartY + (rfEndY - rfStartY) * 0.42f
                val waistX = (trX + rfEndX) * 0.5f - rightWaist
                cubicTo(
                    trX, waistY - 18f * d,
                    waistX, waistY - 12f * d,
                    waistX, waistY
                )
                cubicTo(
                    waistX, waistY + 12f * d,
                    rfEndX, waistY + 18f * d,
                    rfEndX, rfEndY
                )
            } else {
                lineTo(rfEndX, rfEndY)
            }

            // 4. Bottom-Right Corner
            cubicTo(
                rfEndX, rfEndY + K_br,
                rfEndX - brRadius + K_br, bottom,
                rfEndX - brRadius, bottom
            )

            // 5. Bottom Edge with bottomSag teardrop
            val bfStartX = rfEndX - brRadius
            val bfEndX = left + blRadius
            if (botSag > 0.5f && bfStartX > bfEndX) {
                val midBotX = (bfStartX + bfEndX) * 0.5f
                val midBotY = bottom + botSag
                cubicTo(
                    bfStartX - (bfStartX - midBotX) * 0.5f, bottom + botSag * 0.75f,
                    midBotX + (bfStartX - midBotX) * 0.5f, midBotY,
                    midBotX, midBotY
                )
                cubicTo(
                    midBotX - (midBotX - bfEndX) * 0.5f, midBotY,
                    bfEndX + (midBotX - bfEndX) * 0.5f, bottom + botSag * 0.75f,
                    bfEndX, bottom
                )
            } else {
                lineTo(bfEndX, bottom)
            }

            // 6. Bottom-Left Corner
            cubicTo(
                bfEndX - K_bl, bottom,
                left, bottom - blRadius + K_bl,
                left, bottom - blRadius
            )

            // 7. Left Flank with leftWaist concave neck
            val lfStartY = bottom - blRadius
            val lfEndY = anchorTop + tlRadius
            if (leftWaist > 0.5f && lfStartY > lfEndY) {
                val waistY = lfEndY + (lfStartY - lfEndY) * 0.52f
                val waistX = left + leftWaist
                cubicTo(
                    left, waistY + 22f * d,
                    waistX, waistY + 16f * d,
                    waistX, waistY
                )
                cubicTo(
                    waistX, waistY - 16f * d,
                    left, waistY - 22f * d,
                    left, lfEndY
                )
            } else {
                lineTo(left, lfEndY)
            }

            // 8. Top-Left Corner
            cubicTo(
                left, lfEndY - K_tl,
                startTopX - K_tl, anchorTop,
                startTopX, anchorTop
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
    val checkpoint = remember(p) { interpolateCheckpoint(p) }

    val extraPadding = 32.dp
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
    val canvasHeight = targetHeight + extraPadding * 2

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
                                ) {
                                    onExpandToggle()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            MoreVerticalPillIcon(textPrimary)
                        }
                    }
                }
            }

            // --- 3. Dropdown Menu Items Content ---
            val menuAlpha = ((p - 0.20f) / 0.35f).fastCoerceIn(0f, 1f)
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
                            ((p - 0.20f - index * 0.035f) / 0.30f).fastCoerceIn(0f, 1f)
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
                end = Offset(15.dp.toPx(), 15.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
internal fun PlusPillIcon(tint: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
        val midX = size.width / 2f
        val midY = size.height / 2f
        val stroke = 2.dp.toPx()
        val len = 6.dp.toPx()
        drawLine(tint, Offset(midX - len, midY), Offset(midX + len, midY), stroke)
        drawLine(tint, Offset(midX, midY - len), Offset(midX, midY + len), stroke)
    }
}

@Composable
internal fun MoreVerticalPillIcon(tint: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
        val cx = size.width / 2f
        val r = 2.5.dp.toPx()
        val stroke = 1.8.dp.toPx()
        drawCircle(
            color = tint,
            radius = r,
            center = Offset(cx, size.height * 0.35f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )
        drawCircle(
            color = tint,
            radius = r,
            center = Offset(cx, size.height * 0.65f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )
    }
}
