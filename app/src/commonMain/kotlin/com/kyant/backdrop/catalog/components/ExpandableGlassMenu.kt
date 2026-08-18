package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TransformOrigin
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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Compose port of the supplied SwiftUI ExpandableGlassMenu.
 *
 * The component has one source of truth: `progress`.
 *
 * - The menu size morphs from the circular label size to the measured content size.
 * - The label fades, blurs, scales and travels on the same progress curve.
 * - The WHOLE glass surface drifts away from its initial button location while opening,
 *   then settles at a small offset. This is deliberate for the Android demo so the
 *   physical "button becomes panel" motion is visible instead of looking like a
 *   rectangle growing underneath a stationary button.
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

    /*
     * Whole-surface physical travel.
     *
     * Unlike a graphics-layer-only translation, Modifier.offset changes the
     * component's actual placed position, so the glass surface, the circular
     * label, and the clickable area all move together. The parent remains
     * bottom-end aligned, while the visible object drifts up/left as it morphs.
     */
    val clampedProgress = progress.coerceIn(0f, 1f)
    val smoothProgress = clampedProgress * clampedProgress *
        (3f - (2f * clampedProgress))

    val settledDrift = with(density) { 28.dp.toPx() }
    val transientDrift = with(density) { 6.dp.toPx() }
    val openingOvershoot = max(progress - 1f, 0f)
    val closingOvershoot = min(progress, 0f)

    val drift =
        (settledDrift * smoothProgress) +
            (transientDrift * openingOvershoot) +
            (transientDrift * closingOvershoot)

    SubcomposeLayout(
        modifier = modifier
            .offset {
                IntOffset(
                    x = -drift.roundToInt(),
                    y = -drift.roundToInt(),
                )
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

        /*
         * Preserve the SwiftUI interpolation, including small spring overshoot.
         * The spring is responsible for the "rubbery" size response.
         */
        val widthPx =
            labelWidthPx + ((contentWidth - labelWidthPx) * progress).roundToInt()
        val heightPx =
            labelHeightPx + ((contentHeight - labelHeightPx) * progress).roundToInt()

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
            minAspectScale + ((1f - minAspectScale) * progress)

        val labelOpacity = 1f - min(progress / 0.35f, 1f)
        val contentOpacity = max(progress - 0.35f, 0f) / 0.65f

        /*
         * Exact SwiftUI triangular blur curve.
         * Do not use abs()/clamp here: the spring's final overshoot gives the
         * label a natural reverse pulse as it settles.
         */
        val blurProgress = if (progress <= 0.5f) {
            progress / 0.5f
        } else {
            (1f - progress) / 0.5f
        }

        layout(layoutWidth, layoutHeight) {
            /*
             * The expanded surface is anchored from the bottom-right internally:
             * the content and label occupy the same 55x55 anchor frame used by
             * the SwiftUI implementation.
             */
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
                val labelScale = 1f + (blurProgress * 0.45f)
                val blurRadiusPx =
                    (14.dp.toPx() * blurProgress).coerceAtLeast(0f)

                alpha = labelOpacity.coerceIn(0f, 1f)
                transformOrigin = TransformOrigin(1f, 1f)
                scaleX = labelScale
                scaleY = labelScale

                // Original SwiftUI bottom-trailing offset.
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
