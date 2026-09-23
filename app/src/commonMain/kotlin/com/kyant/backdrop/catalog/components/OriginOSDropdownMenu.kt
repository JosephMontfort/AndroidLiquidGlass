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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.unit.IntOffset
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
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Exact spring physics converted from OriginOS 7 frameworkui:
 * `ConversionUtils.java` and `VLiquidConfig.VListPopupWindow`.
 */
object OriginOSSpringPhysics {
    // Formula from ConversionUtils:
    fun convertBounceToDampingRatio(bounce: Float): Float = 1.0f / (bounce + 1.0f)

    fun convertDurationPhase1ToNaturalFreq(duration: Float, dampingRatio: Float): Float {
        if (dampingRatio >= 1.0f) return (2.0f * PI.toFloat() / ln(2.0f)) / duration
        return (PI.toFloat() / (duration * sqrt(1.0f - dampingRatio * dampingRatio)))
    }

    fun convertDurationPhase2ToNaturalFreq(duration: Float, dampingRatio: Float): Float {
        if (dampingRatio >= 1.0f) return (2.0f * PI.toFloat() / ln(2.0f)) / duration
        return (ln(1000.0f / sqrt(1.0f - dampingRatio * dampingRatio)) / (dampingRatio * duration))
    }

    fun convertSingleToNaturalFreq(duration: Float, dampingRatio: Float): Float {
        if (dampingRatio >= 1.0f) return (2.0f * PI.toFloat() / ln(2.0f)) / duration
        return (ln(dampingRatio / (0.001f * sqrt(1.0f - dampingRatio * dampingRatio))) / (dampingRatio * duration))
    }

    // Two-phase spring evaluation for damped harmonic oscillator:
    // x(t) = target - (target - x0) * exp(-zeta * w * t) * (cos(wd * t) + ((zeta * w - v0) / wd) * sin(wd * t))
    fun evaluateSpring(
        t: Float,
        x0: Float,
        target: Float,
        durationP1: Float,
        bounceP1: Float,
        durationP2: Float,
        bounceP2: Float,
        initialVelocity: Float = 0f
    ): Float {
        if (t <= 0f) return x0
        val z1 = convertBounceToDampingRatio(bounceP1)
        val w1 = convertDurationPhase1ToNaturalFreq(durationP1, z1)

        if (t <= durationP1) {
            val wd1 = w1 * sqrt(max(0.0001f, 1.0f - z1 * z1))
            val envelope = exp(-z1 * w1 * t)
            val coeff = (z1 * w1 - initialVelocity) / wd1
            val oscillation = cos(wd1 * t) + coeff * sin(wd1 * t)
            return target - (target - x0) * envelope * oscillation
        } else {
            // Phase 2 transition
            val t2 = t - durationP1
            val z2 = convertBounceToDampingRatio(bounceP2)
            val w2 = convertDurationPhase2ToNaturalFreq(durationP2, z2)
            val wd2 = w2 * sqrt(max(0.0001f, 1.0f - z2 * z2))
            val envelope2 = exp(-z2 * w2 * t2)
            // Near critical damping settling
            return target - (target - 1.0f) * envelope2 * cos(wd2 * t2)
        }
    }

    // Single spring for exit
    fun evaluateSingleSpring(
        t: Float,
        x0: Float,
        target: Float,
        duration: Float,
        bounce: Float,
        initialVelocity: Float = 8.0f
    ): Float {
        if (t <= 0f) return x0
        val z = convertBounceToDampingRatio(bounce)
        val w = convertSingleToNaturalFreq(duration, z)
        val wd = w * sqrt(max(0.0001f, 1.0f - z * z))
        val envelope = exp(-z * w * t)
        val coeff = (z * w - initialVelocity) / wd
        val oscillation = cos(wd * t) + coeff * sin(wd * t)
        return target + (x0 - target) * envelope * oscillation
    }
}

/**
 * OriginOS 7 Liquid Fusion Shape that computes the exact organic droplet and
 * concave neck bridge between Shape A (anchor pill) and Shape B (popup card).
 */
class OriginOSLiquidFusionShape(
    val scaleX: Float,
    val scaleY: Float,
    val dynamicK: Float,
    val isExpanding: Boolean,
    val isLiquidFusionEnabled: Boolean = true,
    val anchorWidthPx: Float,
    val anchorHeightPx: Float,
    val anchorRadiusPx: Float,
    val targetWidthPx: Float,
    val targetHeightPx: Float,
    val targetRadiusPx: Float,
    val extraPaddingPx: Float
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return Outline.Rectangle(Rect.Zero)

        val right = w - extraPaddingPx
        val top = extraPaddingPx

        // Current popup dimensions
        val curW = targetWidthPx * scaleX.fastCoerceIn(0.05f, 1.15f)
        val curH = targetHeightPx * scaleY.fastCoerceIn(0.05f, 1.15f)
        val curR = targetRadiusPx * min(scaleX, scaleY).fastCoerceIn(0.2f, 1.0f)

        val popupLeft = right - curW
        val popupTop = top + 8f // Offset right below anchor pill
        val popupBottom = popupTop + curH

        // If liquid fusion is disabled or dynamicK has decayed to 0, use clean squircle
        if (!isLiquidFusionEnabled || dynamicK <= 0.001f) {
            val popupRect = RoundRect(
                left = popupLeft,
                top = popupTop,
                right = right,
                bottom = popupBottom,
                cornerRadius = CornerRadius(curR, curR)
            )
            return Outline.Rounded(popupRect)
        }

        // --- LIQUID METABALL FUSION BRIDGE ---
        // Shape A (anchor button top-right):
        val aRight = right
        val aTop = top
        val aLeft = right - anchorWidthPx
        val aBottom = top + anchorHeightPx
        val aRadius = anchorRadiusPx

        // Viscous waist factor derived from dynamicK [0..30]
        val kFactor = (dynamicK / 30f).fastCoerceIn(0f, 1f)
        val waistPull = kFactor * 22f

        val path = Path().apply {
            // Start at top-left of anchor button
            moveTo(aLeft + aRadius, aTop)
            lineTo(aRight - aRadius, aTop)
            // Top-right corner of anchor
            cubicTo(
                aRight, aTop,
                aRight, aTop + aRadius * 0.5f,
                aRight, aTop + aRadius
            )

            // Right flank: joins smoothly down to popup card right edge
            val rightWaist = kFactor * 4f
            val midYRight = (aBottom + popupTop) * 0.5f
            cubicTo(
                aRight - rightWaist, midYRight,
                right, popupTop,
                right, popupTop + curR
            )

            // Right vertical edge of popup card
            lineTo(right, popupBottom - curR)

            // Bottom-right corner of popup card
            cubicTo(
                right, popupBottom,
                right - curR * 0.5f, popupBottom,
                right - curR, popupBottom
            )

            // Bottom edge (with subtle viscous droplet inertia sag while dynamicK is high)
            val dropletSag = kFactor * 8f
            val midXBottom = (popupLeft + right) * 0.5f
            quadraticBezierTo(
                midXBottom, popupBottom + dropletSag,
                popupLeft + curR, popupBottom
            )

            // Bottom-left corner of popup card
            cubicTo(
                popupLeft, popupBottom,
                popupLeft, popupBottom - curR * 0.5f,
                popupLeft, popupBottom - curR
            )

            // Left vertical edge of popup card
            lineTo(popupLeft, popupTop + curR)

            // LEFT METABALL BRIDGE: Concave neck connecting popup card to anchor pill!
            // This is the defining visual characteristic of OriginOS 7 Liquid Morph.
            val waistControlX = (aLeft + popupLeft) * 0.5f + waistPull
            val waistControlY = (aBottom + popupTop) * 0.5f
            cubicTo(
                popupLeft + waistPull * 0.5f, popupTop,
                waistControlX, waistControlY,
                aLeft, aBottom
            )

            // Bottom-left of anchor pill
            lineTo(aLeft, aTop + aRadius)
            cubicTo(
                aLeft, aTop,
                aLeft + aRadius * 0.5f, aTop,
                aLeft + aRadius, aTop
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
 * 1:1 OriginOS 7 Dropdown Menu with two-phase spring physics and dynamicK liquid fusion.
 */
@Composable
fun OriginOSDropdownMenu(
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    isLiquidFusionEnabled: Boolean = true,
    animationSpeedMultiplier: Float = 1.0f,
    menuItems: List<OriginOSMenuItem> = defaultOriginOSMenuItems(),
    anchorContent: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val isLightTheme = !isSystemInDarkTheme()

    // Animation elapsed time clock in seconds
    val animTime = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var runningJob by remember { mutableStateOf<Job?>(null) }

    // OriginOS Constants from VLiquidConfig.VListPopupWindow:
    val targetWidth = 210.dp
    val targetHeight = 224.dp
    val anchorWidth = 44.dp
    val anchorHeight = 44.dp
    val anchorRadius = 22.dp
    val targetRadius = 24.dp
    val extraPadding = 36.dp

    val anchorWidthPx = with(density) { anchorWidth.toPx() }
    val anchorHeightPx = with(density) { anchorHeight.toPx() }
    val anchorRadiusPx = with(density) { anchorRadius.toPx() }
    val targetWidthPx = with(density) { targetWidth.toPx() }
    val targetHeightPx = with(density) { targetHeight.toPx() }
    val targetRadiusPx = with(density) { targetRadius.toPx() }
    val extraPaddingPx = with(density) { extraPadding.toPx() }

    // Launch or reverse animation on state change
    LaunchedEffect(isExpanded, animationSpeedMultiplier) {
        runningJob?.cancel()
        runningJob = scope.launch {
            if (isExpanded) {
                // Entry animation: total duration ~0.75s scaled by speed
                val totalDuration = (750f / animationSpeedMultiplier).toInt()
                animTime.snapTo(0f)
                animTime.animateTo(
                    targetValue = 0.75f,
                    animationSpec = tween(durationMillis = totalDuration, easing = LinearEasing)
                )
            } else {
                // Exit animation: duration ~0.35s
                val totalDuration = (350f / animationSpeedMultiplier).toInt()
                animTime.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = totalDuration, easing = LinearEasing)
                )
            }
        }
    }

    val t = animTime.value

    // Scale X from OriginOS multi-phase spring:
    // entryScaleXConfig: multiple(0.1, 1.0, 0.36, 0.28, 0.28, 0.01, 0.0)
    val scaleX = remember(t, isExpanded) {
        if (isExpanded) {
            OriginOSSpringPhysics.evaluateSpring(
                t = t,
                x0 = 0.10f,
                target = 1.0f,
                durationP1 = 0.36f,
                bounceP1 = 0.28f,
                durationP2 = 0.28f,
                bounceP2 = 0.01f,
                initialVelocity = 0.0f
            )
        } else {
            // Exit: single(1.0, 0.05, 0.32, 0.01, 8.0)
            OriginOSSpringPhysics.evaluateSingleSpring(
                t = (0.75f - t).fastCoerceIn(0f, 0.32f),
                x0 = 1.0f,
                target = 0.05f,
                duration = 0.32f,
                bounce = 0.01f,
                initialVelocity = 8.0f
            )
        }
    }

    // Scale Y from OriginOS multi-phase spring:
    // entryScaleYConfig: multiple(0.1, 1.0, 0.29, 0.60, 0.71, 0.01, 5.0)
    val scaleY = remember(t, isExpanded) {
        if (isExpanded) {
            OriginOSSpringPhysics.evaluateSpring(
                t = t,
                x0 = 0.10f,
                target = 1.0f,
                durationP1 = 0.29f,
                bounceP1 = 0.60f,
                durationP2 = 0.71f,
                bounceP2 = 0.01f,
                initialVelocity = 5.0f
            )
        } else {
            // Exit: single(1.0, 0.05, 0.39, 0.1, 8.0)
            OriginOSSpringPhysics.evaluateSingleSpring(
                t = (0.75f - t).fastCoerceIn(0f, 0.39f),
                x0 = 1.0f,
                target = 0.05f,
                duration = 0.39f,
                bounce = 0.10f,
                initialVelocity = 8.0f
            )
        }
    }

    // Dynamic K calculation (gap monitoring):
    // dynamicK starts at 30.0f, then as gap separates (t > 0.18s), decays over 0.15s
    val dynamicK = remember(t, isExpanded, isLiquidFusionEnabled) {
        if (!isLiquidFusionEnabled || !isExpanded) 0f
        else if (t < 0.16f) 30.0f
        else if (t < 0.32f) {
            val decayRatio = (t - 0.16f) / 0.16f
            lerp(30.0f, 0.0f, decayRatio)
        } else 0f
    }

    val liquidShape = remember(scaleX, scaleY, dynamicK, isExpanded, isLiquidFusionEnabled) {
        OriginOSLiquidFusionShape(
            scaleX = scaleX,
            scaleY = scaleY,
            dynamicK = dynamicK,
            isExpanding = isExpanded,
            isLiquidFusionEnabled = isLiquidFusionEnabled,
            anchorWidthPx = anchorWidthPx,
            anchorHeightPx = anchorHeightPx,
            anchorRadiusPx = anchorRadiusPx,
            targetWidthPx = targetWidthPx,
            targetHeightPx = targetHeightPx,
            targetRadiusPx = targetRadiusPx,
            extraPaddingPx = extraPaddingPx
        )
    }

    // Container box sizing
    val totalBoxWidth = targetWidth + (extraPadding * 2)
    val totalBoxHeight = targetHeight + (extraPadding * 2)

    Box(modifier = modifier) {
        // Anchor button slot (top pill)
        anchorContent()

        // Dropdown menu container
        if (t > 0.001f || isExpanded) {
            Box(
                modifier = Modifier
                    .size(totalBoxWidth, totalBoxHeight)
                    .offset(x = extraPadding, y = -extraPadding),
                contentAlignment = Alignment.TopEnd
            ) {
                // Glass & fluid contour
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { liquidShape },
                            effects = {
                                vibrancy()
                                blur(24f.dp.toPx())
                                lens(
                                    16f.dp.toPx(),
                                    18f.dp.toPx()
                                )
                            },
                            highlight = { Highlight.Default },
                            shadow = { Shadow(radius = 16.dp, color = Color.Black.copy(alpha = 0.12f)) },
                            onDrawSurface = {
                                drawRect(
                                    if (isLightTheme) Color.White.copy(alpha = 0.82f)
                                    else Color(0xFF202022).copy(alpha = 0.86f)
                                )
                            }
                        )
                ) {
                    // Specular inner highlight rim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = if (isLightTheme) 0.55f else 0.20f),
                                        Color.White.copy(alpha = 0.05f),
                                        Color.Black.copy(alpha = 0.03f)
                                    )
                                )
                            )
                    )

                    // Menu items list (fading in and sliding up gracefully)
                    val contentAlpha = if (isExpanded) {
                        ((t - 0.18f) / 0.20f).fastCoerceIn(0f, 1f)
                    } else {
                        (t * 3.5f).fastCoerceIn(0f, 1f)
                    }

                    if (contentAlpha > 0.01f) {
                        Column(
                            modifier = Modifier
                                .offset(
                                    x = totalBoxWidth - targetWidth - extraPadding,
                                    y = extraPadding + 8.dp
                                )
                                .width(targetWidth)
                                .height(targetHeight)
                                .alpha(contentAlpha)
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            menuItems.forEachIndexed { index, item ->
                                // Staggered item entry
                                val itemSlideY = if (isExpanded) {
                                    lerp(12f, 0f, ((t - 0.18f - index * 0.03f) / 0.20f).fastCoerceIn(0f, 1f))
                                } else 0f

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
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
    }
}

fun defaultOriginOSMenuItems(): List<OriginOSMenuItem> = listOf(
    OriginOSMenuItem("Layout"),
    OriginOSMenuItem("Sort and hide"),
    OriginOSMenuItem("Collapse all"),
    OriginOSMenuItem("Settings")
)
