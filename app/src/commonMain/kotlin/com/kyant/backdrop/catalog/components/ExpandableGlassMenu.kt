package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.RoundedRectangle
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Compose port of the supplied SwiftUI ExpandableGlassMenu.
 *
 * Motion model:
 *  - `progress` remains the single source of truth and may overshoot while springing.
 *  - The first ~18% is an anticipatory lift/squish: the closed glass button physically
 *    leaves its resting point before the large morph becomes obvious.
 *  - The lift is applied to the whole glass surface, not just the icon/content.
 *  - The surface then grows from the same bottom-trailing anchor and settles at a
 *    noticeably higher/left final position.
 *  - The original SwiftUI label blur, opacity, scale and -75dp travel remain intact.
 */
@Composable
fun ExpandableGlassMenu(
    backdrop: Backdrop,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelSize: Dp = 55.dp,
    cornerRadius: Dp = 30.dp,
    content: @Composable () -> Unit,
    label: @Composable () -> Unit,
) {
    val density = LocalDensity.current

    val p = progress.coerceIn(0f, 1f)

    // The visual expansion intentionally lags the very first instant of the spring.
    // This creates the "lift -> squish -> open" cadence visible in the reference.
    val morphProgress = ((p - 0.055f) / 0.945f).coerceIn(0f, 1f)
    val morphEase = morphProgress * morphProgress * (3f - 2f * morphProgress)

    // Short anticipatory lift. It rises quickly, peaks around 10%, then relaxes
    // while the actual panel expansion takes over.
    val liftPhase = (p / 0.24f).coerceIn(0f, 1f)
    val liftEnvelope = sin((liftPhase * PI).toFloat())

    val settledOffsetX = with(density) { 38.dp.toPx() }
    val settledOffsetY = with(density) { 62.dp.toPx() }
    val anticipatoryLift = with(density) { 24.dp.toPx() }
    val anticipatorySquish = (liftEnvelope * (1f - morphEase * 0.55f)).coerceIn(0f, 1f)

    // The final drift is deliberately more vertical than v4. The whole surface moves,
    // while the early lift provides the unmistakable "button leaves its spot" moment.
    val driftX = settledOffsetX * morphEase + with(density) { 4.dp.toPx() } * liftEnvelope
    val driftY = -(settledOffsetY * morphEase + anticipatoryLift * liftEnvelope)

    // Whole-surface anticipation: very mild horizontal stretch + vertical compression,
    // centered on the bottom-right anchor. This is the requested squish before opening.
    val wholeScaleX = 1f + (0.055f * anticipatorySquish)
    val wholeScaleY = 1f - (0.105f * anticipatorySquish)

    SubcomposeLayout(
        modifier = modifier
            .offset {
                IntOffset(
                    x = -driftX.roundToInt(),
                    y = driftY.roundToInt(),
                )
            }
            .graphicsLayer {
                transformOrigin = TransformOrigin(1f, 1f)
                scaleX = wholeScaleX
                scaleY = wholeScaleY
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(cornerRadius) },
                effects = {
                    vibrancy()
                    blur(8.dp.toPx())
                    lens(
                        refractionHeight = 12.dp.toPx(),
                        refractionAmount = 22.dp.toPx(),
                        depthEffect = true,
                    )
                },
                highlight = { Highlight.Ambient },
                onDrawSurface = {
                    drawRect(
                        androidx.compose.ui.graphics.Color.White.copy(alpha = 0.22f)
                    )
                },
            )
            .clickable(onClick = onClick)
    ) {
        val labelWidthPx = with(this) { labelSize.toPx().roundToInt() }
        val labelHeightPx = labelWidthPx

        val contentPlaceable = subcompose("content") {
            content()
        }[0].measure(
            Constraints(
                minWidth = 0,
                maxWidth = Constraints.Infinity,
                minHeight = 0,
                maxHeight = Constraints.Infinity,
            )
        )

        val contentWidth = contentPlaceable.width
        val contentHeight = contentPlaceable.height

        // Keep the SwiftUI interpolation, but use the delayed morphProgress only for
        // the initial physical anticipation. Once opening starts, the same 0..1 morph
        // math is preserved.
        val widthPx =
            labelWidthPx + ((contentWidth - labelWidthPx) * morphProgress).roundToInt()
        val heightPx =
            labelHeightPx + ((contentHeight - labelHeightPx) * morphProgress).roundToInt()

        val layoutWidth = max(1, widthPx)
        val layoutHeight = max(1, heightPx)

        val labelPlaceable = subcompose("label") {
            label()
        }[0].measure(Constraints.fixed(labelWidthPx, labelHeightPx))

        val contentWidthSafe = max(1, contentWidth)
        val contentHeightSafe = max(1, contentHeight)

        val minAspectScale = min(
            labelWidthPx.toFloat() / contentWidthSafe.toFloat(),
            labelHeightPx.toFloat() / contentHeightSafe.toFloat(),
        )
        val contentScale =
            minAspectScale + ((1f - minAspectScale) * morphProgress)

        val labelOpacity = 1f - min(progress / 0.35f, 1f)
        val contentOpacity = max(progress - 0.35f, 0f) / 0.65f

        // Original SwiftUI triangular blur curve. Keep raw progress here so the
        // spring's little overshoot still produces the reverse blur pulse.
        val blurProgress = if (progress <= 0.5f) {
            progress / 0.5f
        } else {
            (1f - progress) / 0.5f
        }

        layout(layoutWidth, layoutHeight) {
            val anchorFrameX = layoutWidth - labelWidthPx
            val anchorFrameY = layoutHeight - labelHeightPx

            val contentX = anchorFrameX + (labelWidthPx - contentWidth)
            val contentY = anchorFrameY + (labelHeightPx - contentHeight)

            contentPlaceable.placeRelativeWithLayer(
                x = contentX,
                y = contentY,
            ) {
                alpha = contentOpacity.coerceIn(0f, 1f)
                transformOrigin = TransformOrigin(1f, 1f)
                scaleX = contentScale
                scaleY = contentScale
            }

            labelPlaceable.placeRelativeWithLayer(
                x = anchorFrameX,
                y = anchorFrameY,
            ) {
                val labelScale = 1f + (blurProgress.coerceAtLeast(0f) * 0.45f)
                val blurRadiusPx =
                    (14.dp.toPx() * blurProgress).coerceAtLeast(0f)

                alpha = labelOpacity.coerceIn(0f, 1f)
                transformOrigin = TransformOrigin(1f, 1f)
                scaleX = labelScale
                scaleY = labelScale

                // Exact bottom-trailing SwiftUI direction.
                translationY = -75.dp.toPx() * blurProgress

                renderEffect =
                    if (blurRadiusPx > 0.01f) {
                        BlurEffect(blurRadiusPx, blurRadiusPx)
                    } else {
                        null
                    }
            }
        }
    }
}
