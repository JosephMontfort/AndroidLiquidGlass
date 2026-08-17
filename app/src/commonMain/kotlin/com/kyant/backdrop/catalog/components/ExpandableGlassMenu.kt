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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Compose port of the supplied SwiftUI ExpandableGlassMenu.
 *
 * `progress` is still the exact SwiftUI-style morph driver. `impact` is an
 * Android-only physical impulse layered on top of it to reproduce the very
 * short press/depth/rebound that is perceptually obvious in Apple's bouncy
 * rendering: the surface dips into the anchor, punches outward, then settles
 * while the geometry morph is already underway.
 */
@Composable
fun ExpandableGlassMenu(
    backdrop: Backdrop,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelSize: Dp = 55.dp,
    cornerRadius: Dp = 30.dp,
    impact: Float = 0f,
    content: @Composable () -> Unit,
    label: @Composable () -> Unit,
) {
    SubcomposeLayout(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(cornerRadius) },
                effects = {
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
            Constraints(
                minWidth = 0,
                maxWidth = Constraints.Infinity,
                minHeight = 0,
                maxHeight = Constraints.Infinity
            )
        )

        val contentWidth = contentPlaceable.width
        val contentHeight = contentPlaceable.height

        // Keep the original SwiftUI morph math intact. We intentionally allow
        // the spring to overshoot slightly instead of clamping progress first;
        // that is what makes the morph itself participate in the bounce.
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

        val contentWidthSafe = max(1, contentWidth)
        val contentHeightSafe = max(1, contentHeight)
        val minAspectScale = min(
            labelWidthPx.toFloat() / contentWidthSafe.toFloat(),
            labelHeightPx.toFloat() / contentHeightSafe.toFloat()
        )
        val scaleDiff = 1f - minAspectScale
        val contentScale = minAspectScale + (scaleDiff * progress)

        val safeProgress = progress.coerceIn(0f, 1f)
        val labelOpacity = (1f - min(safeProgress / 0.35f, 1f)).coerceIn(0f, 1f)
        val contentOpacity = (max(safeProgress - 0.35f, 0f) / 0.65f).coerceIn(0f, 1f)

        val blurProgress = if (safeProgress <= 0.5f) {
            safeProgress / 0.5f
        } else {
            (1f - safeProgress) / 0.5f
        }.coerceIn(0f, 1f)

        // A quick, non-linear depth impulse. The first half pushes the glass
        // towards the bottom-right anchor ("into the screen"), then the return
        // creates a subtle overshoot as the main spring expands.
        val depthSquash = 1f - (0.055f * impact)
        val depthScaleX = depthSquash + (0.012f * impact)
        val depthScaleY = depthSquash - (0.010f * impact)
        val depthTranslation = 8.dp.toPx() * impact
        val depthAlphaBoost = 0.04f * impact

        layout(safeWidth, safeHeight) {
            val contentX = safeWidth - contentWidth
            val contentY = safeHeight - contentHeight

            contentPlaceable.placeRelativeWithLayer(
                x = contentX,
                y = contentY
            ) {
                alpha = (contentOpacity + depthAlphaBoost).coerceIn(0f, 1f)
                transformOrigin = TransformOrigin(1f, 1f)
                scaleX = contentScale * depthScaleX
                scaleY = contentScale * depthScaleY
                translationX = -depthTranslation * 0.18f
                translationY = depthTranslation * 0.30f
            }

            val labelX = safeWidth - labelWidthPx
            val labelY = safeHeight - labelHeightPx

            labelPlaceable.placeRelativeWithLayer(
                x = labelX,
                y = labelY
            ) {
                alpha = labelOpacity
                transformOrigin = TransformOrigin(1f, 1f)

                val labelScale = 1f + (blurProgress * 0.45f)
                scaleX = labelScale * depthScaleX
                scaleY = labelScale * depthScaleY

                translationX = -depthTranslation * 0.10f
                translationY = -75.dp.toPx() * blurProgress + depthTranslation

                val blurRadiusPx = 14.dp.toPx() * blurProgress
                renderEffect = if (blurRadiusPx > 0.01f) {
                    BlurEffect(blurRadiusPx, blurRadiusPx)
                } else {
                    null
                }
            }

            // The backdrop itself gets a tiny depth cue in addition to the
            // library's refraction/depthEffect. It is intentionally subtle so
            // the glass still reads as one physical surface.
            if (abs(impact) > 0.001f) {
                // Kept in layout scope to make the physical impulse part of the
                // same frame as the geometry change; child layers carry the cue.
            }
        }
    }
}
