package com.kyant.backdrop.catalog.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

/**
 * Modular Liquid Morph Container.
 *
 * Can morph ANY starting size and shape into ANY target size and shape
 * with the exact fluid teardrop / visceral droplet physics of iOS 26.
 */
@Composable
fun FluidMorphContainer(
    animatableProgress: Animatable<Float, *>,
    isExpanding: Boolean,
    modifier: Modifier = Modifier,
    backdrop: Backdrop,
    startWidth: Dp = 146.dp,
    startHeight: Dp = 138.dp,
    targetWidth: Dp = 302.dp,
    targetHeight: Dp = 332.dp,
    startCornerRadius: Dp = 28.dp,
    targetCornerRadius: Dp = 34.dp,
    startShapeType: MorphShapeType = MorphShapeType.Squircle,
    targetShapeType: MorphShapeType = MorphShapeType.Squircle,
    alignment: Alignment = Alignment.TopEnd,
    viscosityBulge: Float = 0.40f,
    meniscusSagDp: Float = 38f,
    isGlassEnabled: Boolean = true,
    blurRadius: Float = 16f,
    refractionHeight: Float = 20f,
    refractionAmount: Float = 25f,
    chromaticAberration: Boolean = false,
    startContent: @Composable () -> Unit,
    targetContent: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val isLightTheme = !isSystemInDarkTheme()

    val morphShape = FluidMorphShape(
        progress = animatableProgress.value.coerceIn(0f, 1f),
        isExpanding = isExpanding,
        startWidth = startWidth,
        startHeight = startHeight,
        targetWidth = targetWidth,
        targetHeight = targetHeight,
        startRadius = startCornerRadius,
        targetRadius = targetCornerRadius,
        startShapeType = startShapeType,
        targetShapeType = targetShapeType,
        alignment = alignment,
        viscosityBulge = viscosityBulge,
        meniscusSagDp = meniscusSagDp
    )

    Box(
        modifier = modifier
            // ZERO-RECOMPOSITION DYNAMIC MEASUREMENT
            .layout { measurable, constraints ->
                val p = animatableProgress.value.coerceIn(0f, 1f)
                val startWPx = with(density) { startWidth.toPx() }
                val startHPx = with(density) { startHeight.toPx() }
                val targetWPx = with(density) { targetWidth.toPx() }
                val targetHPx = with(density) { targetHeight.toPx() }

                val curW: Float
                val curH: Float

                if (isExpanding) {
                    val hProg = sin(p * (PI.toFloat() / 2f)).pow(0.55f).coerceIn(0f, 1f)
                    val wBottomProg = sin(p * (PI.toFloat() / 2f)).pow(0.65f).coerceIn(0f, 1f)
                    val wTopProg = p.pow(1.7f).coerceIn(0f, 1f)

                    val wTop = lerp(startWPx, targetWPx, wTopProg)
                    val wBottom = lerp(startWPx, targetWPx, wBottomProg)
                    val bulgeExtra = sin(p * PI.toFloat()) * with(density) { 26.dp.toPx() } * viscosityBulge

                    curW = max(wTop, wBottom) + bulgeExtra
                    curH = lerp(startHPx, targetHPx, hProg)
                } else {
                    val q = 1f - p
                    val wProg = p.pow(1.4f).coerceIn(0f, 1f)
                    val hProg = p.pow(0.60f).coerceIn(0f, 1f)
                    val dropletSag = sin(q * PI.toFloat()).pow(1.8f) * with(density) { meniscusSagDp.dp.toPx() }

                    curW = lerp(startWPx, targetWPx, wProg)
                    curH = lerp(startHPx, targetHPx, hProg) + dropletSag
                }

                val boundedW = curW.toInt().coerceIn(constraints.minWidth, constraints.maxWidth)
                val boundedH = curH.toInt().coerceIn(constraints.minHeight, constraints.maxHeight)

                val placeable = measurable.measure(
                    Constraints(
                        minWidth = boundedW,
                        maxWidth = boundedW,
                        minHeight = boundedH,
                        maxHeight = boundedH
                    )
                )
                layout(placeable.width, placeable.height) {
                    placeable.place(0, 0)
                }
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { morphShape },
                effects = {
                    if (isGlassEnabled) {
                        val p = animatableProgress.value.coerceIn(0f, 1f)
                        val motionBlur = 24f * sin(p * PI.toFloat())
                        vibrancy()
                        blur((blurRadius + motionBlur).dp.toPx())
                        lens(
                            (refractionHeight + 8f * sin(p * PI.toFloat())).dp.toPx(),
                            (refractionAmount + 14f * sin(p * PI.toFloat())).dp.toPx(),
                            depthEffect = true,
                            chromaticAberration = chromaticAberration
                        )
                    }
                },
                highlight = {
                    if (isGlassEnabled) {
                        val p = animatableProgress.value.coerceIn(0f, 1f)
                        val extraGlow = 0.35f * sin(p * PI.toFloat())
                        Highlight.Default.copy(alpha = 0.75f + extraGlow)
                    } else null
                },
                shadow = {
                    Shadow(radius = 26.dp, color = Color.Black.copy(alpha = 0.25f))
                },
                innerShadow = {
                    if (isGlassEnabled) InnerShadow(radius = 12.dp, color = Color.White.copy(alpha = 0.36f)) else null
                },
                onDrawSurface = {
                    val p = animatableProgress.value.coerceIn(0f, 1f)
                    val baseAlpha = if (isLightTheme) 0.30f + 0.16f * p else 0.40f + 0.18f * p
                    val tintColor = if (isLightTheme) Color.White.copy(alpha = baseAlpha) else Color(0xFF252528).copy(alpha = baseAlpha)
                    drawRect(tintColor)
                }
            )
            .clip(morphShape),
        contentAlignment = alignment
    ) {
        // EXPANDED TARGET CONTENT SLOT (Cascades in as liquid drop hits bottom and settles)
        Box(
            modifier = Modifier
                .wrapContentSize(unbounded = true, align = Alignment.TopStart)
                .size(targetWidth, targetHeight)
                .graphicsLayer {
                    val p = animatableProgress.value.coerceIn(0f, 1f)
                    val contentAlpha = if (isExpanding) {
                        ((p - 0.75f) / 0.25f).coerceIn(0f, 1f)
                    } else {
                        ((p - 0.90f) / 0.10f).coerceIn(0f, 1f)
                    }

                    alpha = contentAlpha
                    val animFactor = if (isExpanding) ((p - 0.75f) / 0.25f).coerceIn(0f, 1f) else 1f
                    val scaleFactor = lerp(0.94f, 1f, animFactor)
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                    translationY = with(density) { lerp(12.dp.toPx(), 0f, animFactor) }
                    transformOrigin = TransformOrigin.Center
                },
            contentAlignment = alignment
        ) {
            targetContent()
        }

        // STARTING TILE CONTENT SLOT (Expands and dissolves in first 18% of movement)
        Box(
            modifier = Modifier
                .wrapContentSize(unbounded = true, align = Alignment.TopEnd)
                .size(startWidth, startHeight)
                .graphicsLayer {
                    val p = animatableProgress.value.coerceIn(0f, 1f)
                    val startAlpha = if (isExpanding) {
                        (1f - p / 0.18f).coerceIn(0f, 1f)
                    } else {
                        ((0.30f - p) / 0.30f).coerceIn(0f, 1f)
                    }

                    alpha = startAlpha
                    val scaleFactor = if (isExpanding) {
                        lerp(1f, 1.15f, (p / 0.18f).coerceIn(0f, 1f))
                    } else {
                        lerp(0.90f, 1f, ((0.30f - p) / 0.30f).coerceIn(0f, 1f))
                    }
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                    transformOrigin = TransformOrigin.Center
                },
            contentAlignment = alignment
        ) {
            startContent()
        }
    }
}
