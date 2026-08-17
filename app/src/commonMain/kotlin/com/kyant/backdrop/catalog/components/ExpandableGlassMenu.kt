package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
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
 * Compose port of the SwiftUI ExpandableGlassMenu from the supplied reference code.
 *
 * The animation is intentionally driven by a single progress value, matching the
 * original component's AnimatableData architecture:
 *
 *   progress = 0f -> collapsed label
 *   progress = 1f -> expanded content
 *
 * All geometry/opacity/scale/blur/offset calculations are direct translations of
 * the SwiftUI formulas.
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
    SubcomposeLayout(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(cornerRadius) },
                effects = {
                    // SwiftUI's .ultraThinMaterial is substituted with the
                    // library's live backdrop material pipeline.
                    vibrancy()
                    blur(8.dp.toPx())
                    lens(
                        refractionHeight = 12.dp.toPx(),
                        refractionAmount = 22.dp.toPx(),
                        depthEffect = true
                    )
                },
                highlight = { Highlight.Ambient },
                onDrawSurface = {
                    drawRect(
                        androidx.compose.ui.graphics.Color.White.copy(alpha = 0.22f)
                    )
                }
            )
            .clickable(onClick = onClick)
    ) {
        val density = this
        val labelWidthPx = with(density) { labelSize.toPx().roundToInt() }
        val labelHeightPx = labelWidthPx

        val contentPlaceable = subcompose("expandable-content") {
            content()
        }[0].measure(
            // SwiftUI's .fixedSize() measures content at its natural size.
            Constraints(
                minWidth = 0,
                maxWidth = Constraints.Infinity,
                minHeight = 0,
                maxHeight = Constraints.Infinity
            )
        )

        val contentWidth = contentPlaceable.width
        val contentHeight = contentPlaceable.height

        // Direct port of:
        // width = labelWidth + (contentWidth - labelWidth) * progress
        // height = labelHeight + (contentHeight - labelHeight) * progress
        val widthPx = labelWidthPx +
            ((contentWidth - labelWidthPx) * progress).roundToInt()
        val heightPx = labelHeightPx +
            ((contentHeight - labelHeightPx) * progress).roundToInt()

        val safeWidth = max(labelWidthPx, widthPx)
        val safeHeight = max(labelHeightPx, heightPx)

        val labelPlaceable = subcompose("expandable-label") {
            label()
        }[0].measure(
            Constraints.fixed(labelWidthPx, labelHeightPx)
        )

        // Direct port of the SwiftUI scale calculation.
        val contentWidthSafe = max(1, contentWidth)
        val contentHeightSafe = max(1, contentHeight)
        val minAspectScale = min(
            labelWidthPx.toFloat() / contentWidthSafe.toFloat(),
            labelHeightPx.toFloat() / contentHeightSafe.toFloat()
        )
        val scaleDiff = 1f - minAspectScale
        val contentScale = minAspectScale + (scaleDiff * progress)

        // Direct port:
        // labelOpacity = 1 - min(progress / 0.35, 1)
        val labelOpacity = 1f - min(progress / 0.35f, 1f)

        // Direct port:
        // contentOpacity = max(progress - 0.35, 0) / 0.65
        val contentOpacity = max(progress - 0.35f, 0f) / 0.65f

        // Direct port of the triangular blur-progress curve:
        // 0 -> 1 -> 0, with the peak exactly at progress == 0.5.
        val blurProgress = if (progress <= 0.5f) {
            progress / 0.5f
        } else {
            (1f - progress) / 0.5f
        }.coerceIn(0f, 1f)

        layout(safeWidth, safeHeight) {
            // Reference alignment is .bottomTrailing, so the content and label
            // remain locked to the bottom-right corner while the container grows
            // upward and leftward.
            val contentX = safeWidth - contentWidth
            val contentY = safeHeight - contentHeight

            contentPlaceable.placeRelativeWithLayer(
                x = contentX,
                y = contentY
            ) {
                alpha = contentOpacity
                transformOrigin = TransformOrigin(1f, 1f)
                scaleX = contentScale
                scaleY = contentScale
            }

            // The label is a 55x55 frame aligned to the bottom-trailing anchor.
            val labelX = safeWidth - labelWidthPx
            val labelY = safeHeight - labelHeightPx

            labelPlaceable.placeRelativeWithLayer(
                x = labelX,
                y = labelY
            ) {
                alpha = labelOpacity
                transformOrigin = TransformOrigin(0.5f, 0.5f)

                // SwiftUI:
                // scaleEffect(1 + (blurProgress * 0.45))
                val labelScale = 1f + (blurProgress * 0.45f)
                scaleX = labelScale
                scaleY = labelScale

                // SwiftUI .offset for .bottomTrailing:
                // CGSize(width: 0, height: -75 * blurProgress)
                translationY = -75.dp.toPx() * blurProgress

                // SwiftUI .blur(radius: 14 * blurProgress)
                val blurRadiusPx = 14.dp.toPx() * blurProgress
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
