package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
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
 * Faithful Compose port of the supplied SwiftUI ExpandableGlassMenu.
 *
 * The important detail is that `progress` is allowed to overshoot. SwiftUI's
 * Animatable protocol receives the spring's raw interpolated value; it is NOT
 * clamped to [0, 1]. That overshoot is what makes width/height, scale, blur,
 * label offset and label scale all participate in the same physical bounce.
 *
 * The bottom-right corner is the fixed anchor for this demo, exactly like the
 * original `.bottomTrailing` composition. The visible circular label travels
 * away from that anchor while the container grows, then comes back as the
 * spring settles.
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
    // The SwiftUI component only offsets the label, but on Android that motion
    // is substantially less perceptible because the backdrop itself remains
    // perfectly pinned while the label is rapidly blurred/faded. The reference
    // video makes the physical "pop" read as the whole glass surface briefly
    // travelling away from its anchor. This is kept small and is driven by the
    // same progress/blur curve rather than a second independent animation.
    val motionProgress = if (progress <= 0.5f) {
        progress / 0.5f
    } else {
        (1f - progress) / 0.5f
    }

    SubcomposeLayout(
        modifier = modifier
            .graphicsLayer {
                // Bottom-trailing anchor: lift the entire morphing surface
                // up/left for the transient pop, then return it to the exact
                // original location as the morph settles.
                val travel = with(density) { 18.dp.toPx() } * motionProgress
                translationX = -travel
                translationY = -travel
                val depth = (0.045f * motionProgress).coerceAtLeast(0f)
                scaleX = 1f + depth
                scaleY = 1f + depth
                transformOrigin = TransformOrigin(1f, 1f)
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

        // SwiftUI:
        // width = label + (content - label) * progress
        // height = label + (content - label) * progress
        // Do NOT clamp progress here: spring overshoot is intentional.
        val widthPx = labelWidthPx + ((contentWidth - labelWidthPx) * progress).roundToInt()
        val heightPx = labelHeightPx + ((contentHeight - labelHeightPx) * progress).roundToInt()

        // Avoid invalid Compose layout sizes on the rare negative part of the
        // closing spring, while preserving positive overshoot on opening.
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
        val contentScale = minAspectScale + ((1f - minAspectScale) * progress)

        // Exact SwiftUI formulae. Clamp only at the final layer alpha because
        // RenderNode alpha itself must remain in [0, 1]; the source calculations
        // remain unbounded so the spring can overshoot naturally.
        val labelOpacity = 1f - min(progress / 0.35f, 1f)
        val contentOpacity = max(progress - 0.35f, 0f) / 0.65f

        // Exact triangular blurProgress from SwiftUI, intentionally unbounded.
        val blurProgress = if (progress <= 0.5f) {
            progress / 0.5f
        } else {
            (1f - progress) / 0.5f
        }

        layout(layoutWidth, layoutHeight) {
            // The content lives in a 55x55 frame aligned to bottom-trailing,
            // then gets scaled around that same anchor. This is the crucial
            // part of the SwiftUI structure: the expanded panel grows away
            // from the original button instead of recentering the content.
            val anchorFrameX = layoutWidth - labelWidthPx
            val anchorFrameY = layoutHeight - labelHeightPx

            // Content is fixed-size inside the 55x55 anchor frame with
            // bottom-trailing alignment, then scaled about its bottom-trailing
            // corner. Its rendered bounds therefore emerge from the button.
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

            // Same 55x55 frame, same bottom-trailing anchor, then the original
            // directional offset is applied. During the spring overshoot,
            // blurProgress can briefly become negative, which makes the label
            // reverse direction for the final rebound exactly as the SwiftUI
            // formula permits.
            val labelX = anchorFrameX
            val labelY = anchorFrameY
            val labelScale = 1f + (blurProgress * 0.45f)
            val blurRadiusPx = (14.dp.toPx() * blurProgress).coerceAtLeast(0f)

            labelPlaceable.placeRelativeWithLayer(
                x = labelX,
                y = labelY,
            ) {
                alpha = labelOpacity.coerceIn(0f, 1f)
                transformOrigin = TransformOrigin(1f, 1f)
                scaleX = labelScale
                scaleY = labelScale
                translationY = -75.dp.toPx() * blurProgress
                renderEffect = if (blurRadiusPx > 0.01f) {
                    BlurEffect(blurRadiusPx, blurRadiusPx)
                } else {
                    null
                }
            }
        }
    }
}
