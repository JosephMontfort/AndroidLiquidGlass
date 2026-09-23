package com.kyant.backdrop.catalog.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun OpaqueFluidMorphContainer(
    animatableProgress: Animatable<Float, *>,
    isExpanding: Boolean,
    containerColor: Color,
    startWidth: Dp = 146.dp,
    startHeight: Dp = 138.dp,
    targetWidth: Dp = 302.dp,
    targetHeight: Dp = 332.dp,
    startCornerRadius: Dp = 28.dp,
    targetCornerRadius: Dp = 34.dp,
    alignment: Alignment = Alignment.TopEnd,
    startContent: @Composable () -> Unit,
    targetContent: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val progress = animatableProgress.value.coerceIn(0f, 1f)

    // Margin buffer so the organic bulge never clips against layout bounds
    val extraPadding = 32.dp
    val totalBoxWidth = targetWidth + (extraPadding * 2)
    val totalBoxHeight = targetHeight + (extraPadding * 2)

    val startWPx = with(density) { startWidth.toPx() }
    val startHPx = with(density) { startHeight.toPx() }
    val targetWPx = with(density) { targetWidth.toPx() }
    val targetHPx = with(density) { targetHeight.toPx() }
    val startRPx = with(density) { startCornerRadius.toPx() }
    val targetRPx = with(density) { targetCornerRadius.toPx() }
    val extraPaddingPx = with(density) { extraPadding.toPx() }

    val morphShape = remember(progress, isExpanding) {
        FluidMorphShape(
            progress = progress,
            isExpanding = isExpanding,
            startWidthPx = startWPx,
            startHeightPx = startHPx,
            targetWidthPx = targetWPx,
            targetHeightPx = targetHPx,
            startRadiusPx = startRPx,
            targetRadiusPx = targetRPx,
            extraPaddingPx = extraPaddingPx
        )
    }

    Box(
        modifier = Modifier
            .size(totalBoxWidth, totalBoxHeight)
            .offset(x = extraPadding, y = -extraPadding), // Balance anchor positioning
        contentAlignment = Alignment.TopEnd
    ) {
        // The deformed fluid background membrane
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(morphShape)
                .background(containerColor)
        ) {
            // Expanded Device List (Revealed without zoom/scaling artifacts)
            val expandedAlpha = ((progress - 0.25f) / 0.75f).coerceIn(0f, 1f)
            if (expandedAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .offset(
                            x = totalBoxWidth - targetWidth - extraPadding,
                            y = extraPadding
                        )
                        .size(targetWidth, targetHeight)
                        .alpha(expandedAlpha)
                ) {
                    targetContent()
                }
            }

            // Small Collapsed Tile (Crossfades without zoom/scaling artifacts)
            val collapsedAlpha = (1f - (progress * 3.2f)).coerceIn(0f, 1f)
            if (collapsedAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .offset(
                            x = totalBoxWidth - startWidth - extraPadding,
                            y = extraPadding
                        )
                        .size(startWidth, startHeight)
                        .alpha(collapsedAlpha)
                ) {
                    startContent()
                }
            }
        }
    }
}
