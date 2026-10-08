package com.kyant.backdrop.catalog.components

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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.catalog.ReceiveIcon
import com.kyant.backdrop.catalog.SendIcon
import com.kyant.backdrop.catalog.ShareFilledIcon
import com.kyant.backdrop.catalog.SwapIcon
import com.kyant.backdrop.catalog.effects.vivoLiquidGlass
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.catalog.physics.OriginOSPopupConfigs
import kotlin.math.roundToInt

data class OriginOSMenuItem(
    val title: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit = {}
)

fun defaultOriginOSMenuItems(): List<OriginOSMenuItem> = listOf(
    OriginOSMenuItem("Select items", ShareFilledIcon),
    OriginOSMenuItem("Create album", SendIcon),
    OriginOSMenuItem("Sort & filter", SwapIcon),
    OriginOSMenuItem("Settings", ReceiveIcon)
)

/**
 * 1:1 OriginOS 7 Drag-and-Drop Dropdown Menu Component.
 *
 * Employs the exact reverse-engineered spring physics engine:
 * - Two-phase spring for entry:
 *   - Scale X: T1=0.36s (b=0.28), T2=0.28s (b=0.01), v0=0.0
 *   - Scale Y: T1=0.29s (b=0.60), T2=0.71s (b=0.01), v0=5.0
 *   - Translation Y: T1=0.32s (b=0.55), T2=0.54s (b=0.01), v0=8.0
 *   - Translation X: T1=0.44s (b=0.24), T2=0.47s (b=0.01), v0=0.0
 * - Single-phase spring for exit:
 *   - Scale X: T=0.32s (b=0.01), v0=8.0
 *   - Scale Y: T=0.39s (b=0.10), v0=8.0
 *   - Translation X: T=0.26s (b=0.0), v0=8.0
 *   - Translation Y: T=0.41s (b=0.0), v0=0.0
 * - Vivo Liquid Glass AGSL shader integration.
 */
@Composable
fun OriginOSDropdownMenu(
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    isGlassEnabled: Boolean = true,
    animationSpeedMultiplier: Float = 1.0f,
    menuItems: List<OriginOSMenuItem> = defaultOriginOSMenuItems(),
    onExpandToggle: () -> Unit = {}
) {
    OriginOSLiquidMorphContainer(
        isExpanded = isExpanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        backdrop = backdrop,
        isGlassEnabled = isGlassEnabled,
        animationSpeedMultiplier = animationSpeedMultiplier,
        anchor = { onClick ->
            OriginOSPillButton(
                onClick = {
                    onClick()
                    onExpandToggle()
                }
            )
        },
        menuContent = {
            OriginOSMenuContent(
                menuItems = menuItems,
                onItemClick = { item ->
                    item.onClick()
                    onDismissRequest()
                }
            )
        }
    )
}

/**
 * Universal 1:1 Drag-and-Drop Liquid Morph Container.
 * Can be wrapped around any anchor and menu content.
 */
@Composable
fun OriginOSLiquidMorphContainer(
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    isGlassEnabled: Boolean = true,
    animationSpeedMultiplier: Float = 1.0f,
    anchorWidth: Dp = 40.dp,
    anchorHeight: Dp = 28.dp,
    anchorCornerRadius: Dp = 14.dp,
    menuWidth: Dp = 200.dp,
    menuHeight: Dp = 224.dp,
    menuCornerRadius: Dp = 28.dp,
    anchor: @Composable (onClick: () -> Unit) -> Unit,
    menuContent: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val density = LocalDensity.current

    // Physics animation state variables
    var currentScaleX by remember { mutableFloatStateOf(0.1f) }
    var currentScaleY by remember { mutableFloatStateOf(0.1f) }
    var currentTransX by remember { mutableFloatStateOf(0f) }
    var currentTransY by remember { mutableFloatStateOf(0f) }

    // Display progress (0.0 = anchor pill, 1.0 = full menu card)
    var morphProgress by remember { mutableFloatStateOf(if (isExpanded) 1f else 0f) }
    var isAnimating by remember { mutableStateOf(false) }

    val startOffsetYPx = with(density) { 8.dp.toPx() }
    val startOffsetXPx = with(density) { 0.dp.toPx() }

    // Precise Choreographer frame-based physics evaluation
    LaunchedEffect(isExpanded, animationSpeedMultiplier) {
        val targetExpanded = isExpanded
        isAnimating = true
        var elapsedSec = 0f
        var lastFrameNanos = 0L

        val totalDuration = if (targetExpanded) {
            OriginOSPopupConfigs.maxEntryDuration
        } else {
            OriginOSPopupConfigs.maxExitDuration
        }

        while (elapsedSec <= totalDuration) {
            withFrameNanos { frameNanos ->
                if (lastFrameNanos == 0L) {
                    lastFrameNanos = frameNanos
                } else {
                    val dt = ((frameNanos - lastFrameNanos) / 1_000_000_000f) * animationSpeedMultiplier
                    elapsedSec += dt
                    lastFrameNanos = frameNanos
                }

                if (targetExpanded) {
                    // ENTRY ANIMATION
                    currentScaleX = OriginOSPopupConfigs.entryScaleX.evaluate(elapsedSec, 0.1f, 1.0f)
                    currentScaleY = OriginOSPopupConfigs.entryScaleY.evaluate(elapsedSec, 0.1f, 1.0f)
                    currentTransX = OriginOSPopupConfigs.entryTranslationX.evaluate(elapsedSec, startOffsetXPx, 0.0f)
                    currentTransY = OriginOSPopupConfigs.entryTranslationY.evaluate(elapsedSec, startOffsetYPx, 0.0f)
                } else {
                    // EXIT ANIMATION
                    currentScaleX = OriginOSPopupConfigs.exitScaleX.evaluate(elapsedSec, 1.0f, 0.05f)
                    currentScaleY = OriginOSPopupConfigs.exitScaleY.evaluate(elapsedSec, 1.0f, 0.05f)
                    currentTransX = OriginOSPopupConfigs.exitTranslationX.evaluate(elapsedSec, 0.0f, startOffsetXPx)
                    currentTransY = OriginOSPopupConfigs.exitTranslationY.evaluate(elapsedSec, 0.0f, startOffsetYPx)
                }

                // Compute normalized morph factor
                morphProgress = if (targetExpanded) {
                    (currentScaleY).coerceIn(0f, 1f)
                } else {
                    (currentScaleY).coerceIn(0f, 1f)
                }
            }
        }

        // Snap to exact terminal values at completion
        if (targetExpanded) {
            currentScaleX = 1.0f
            currentScaleY = 1.0f
            currentTransX = 0f
            currentTransY = 0f
            morphProgress = 1.0f
        } else {
            currentScaleX = 0.1f
            currentScaleY = 0.1f
            currentTransX = startOffsetXPx
            currentTransY = startOffsetYPx
            morphProgress = 0.0f
        }
        isAnimating = false
    }

    // Geometry interpolation
    val curWidthDp = anchorWidth + (menuWidth - anchorWidth) * currentScaleX.coerceIn(0f, 1.2f)
    val curHeightDp = anchorHeight + (menuHeight - anchorHeight) * currentScaleY.coerceIn(0f, 1.2f)
    val curRadiusDp = anchorCornerRadius + (menuCornerRadius - anchorCornerRadius) * morphProgress

    val transOffsetXDp = with(density) { currentTransX.toDp() }
    val transOffsetYDp = with(density) { currentTransY.toDp() }

    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        // --- 1. Idle Anchor Pill (Visible only when completely collapsed) ---
        if (!isExpanded && !isAnimating) {
            anchor {
                // Tapping trigger expands menu
            }
        }

        // --- 2. Live Morphing Shell (Active during animation or when expanded) ---
        if (isExpanded || isAnimating) {
            val shape = RoundedCornerShape(curRadiusDp)

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = with(density) { transOffsetXDp.roundToPx() },
                            y = with(density) { transOffsetYDp.roundToPx() }
                        )
                    }
                    .size(width = curWidthDp, height = curHeightDp)
                    .shadow(
                        elevation = if (morphProgress > 0.3f) (12.dp * morphProgress) else 0.dp,
                        shape = shape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.15f * morphProgress),
                        spotColor = Color.Black.copy(alpha = 0.25f * morphProgress)
                    )
                    .clip(shape)
                    .then(
                        if (backdrop != null && isGlassEnabled) {
                            Modifier.drawBackdrop(
                                backdrop = backdrop,
                                shape = shape,
                                effects = {
                                    blur(16.dp)
                                    vivoLiquidGlass(
                                        size = Size(
                                            with(density) { curWidthDp.toPx() },
                                            with(density) { curHeightDp.toPx() }
                                        ),
                                        cornerRadius = with(density) { curRadiusDp.toPx() },
                                        globalIntensity = 1.0f,
                                        lightIntensity = 1.25f,
                                        lightAngle = 45f
                                    )
                                }
                            )
                        } else {
                            Modifier.background(
                                color = if (isDark) {
                                    Color(0xEE242426)
                                } else {
                                    Color(0xEEF8F8FA)
                                }
                            )
                        }
                    )
            ) {
                // Background tint highlight
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isDark) {
                                Color.White.copy(alpha = 0.06f)
                            } else {
                                Color.Black.copy(alpha = 0.03f)
                            }
                        )
                )

                // Sub-layer A: Anchor 3-dot icon (Fades out rapidly as pill stretches)
                val anchorAlpha = (1f - (morphProgress * 3.5f)).coerceIn(0f, 1f)
                if (anchorAlpha > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(anchorAlpha),
                        contentAlignment = Alignment.Center
                    ) {
                        OriginOSThreeDotsIcon(
                            color = if (isDark) Color.White else Color.Black
                        )
                    }
                }

                // Sub-layer B: Menu Items Content (Fades in and settles upward)
                val menuAlpha = ((morphProgress - 0.40f) / 0.50f).coerceIn(0f, 1f)
                if (menuAlpha > 0.01f) {
                    val slideOffsetDp = ((1f - morphProgress) * 16f).dp
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(y = slideOffsetDp)
                            .alpha(menuAlpha)
                    ) {
                        menuContent()
                    }
                }
            }
        }
    }
}

/**
 * OriginOS 3-dot Pill Button Anchor.
 */
@Composable
fun OriginOSPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .size(width = 40.dp, height = 28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isDark) {
                    Color.White.copy(alpha = 0.15f)
                } else {
                    Color.Black.copy(alpha = 0.08f)
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        OriginOSThreeDotsIcon(
            color = if (isDark) Color.White else Color.Black
        )
    }
}

/**
 * Exact 3 horizontal dots icon used in OriginOS Albums pill button.
 */
@Composable
fun OriginOSThreeDotsIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(3.5.dp)
                    .background(color, CircleShape)
            )
        }
    }
}

/**
 * Standard OriginOS 7 Menu Card Content.
 */
@Composable
fun OriginOSMenuContent(
    menuItems: List<OriginOSMenuItem>,
    onItemClick: (OriginOSMenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val textPrimary = if (isDark) Color.White else Color.Black
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        menuItems.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onItemClick(item) }
                    .padding(vertical = 9.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BasicText(
                    text = item.title,
                    style = TextStyle(
                        color = textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                if (item.icon != null) {
                    androidx.compose.foundation.Image(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier
                            .size(19.dp)
                            .alpha(0.85f),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(textPrimary)
                    )
                }
            }

            if (index < menuItems.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.6.dp)
                        .background(dividerColor)
                )
            }
        }
    }
}
