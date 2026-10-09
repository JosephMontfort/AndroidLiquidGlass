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
import com.kyant.backdrop.catalog.physics.OriginOSPopupConfigs
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 7 discrete phases of OriginOS 7 fluid morphing cycle.
 */
enum class OriginOSMorphPhase(val label: String) {
    IDLE_COLLAPSED("IDLE_COLLAPSED"),
    PINCH_TO_BUBBLE("PHASE 1: PINCH TO BUBBLE"),
    DROPLET_FLIGHT("PHASE 2: DROPLET FLIGHT"),
    BLOOM_BOUNCE("PHASE 3: BLOOM & BOUNCE"),
    IDLE_EXPANDED("IDLE_EXPANDED"),
    COLLAPSE_BUBBLE("PHASE 4: COLLAPSE TO BUBBLE"),
    UNPINCH_ANCHOR("PHASE 5: UNPINCH TO ANCHOR")
}

/**
 * Live physical telemetry emitted by OriginOSDropdownMenu on every frame.
 */
data class OriginOSPhysicsTelemetry(
    val phase: OriginOSMorphPhase = OriginOSMorphPhase.IDLE_COLLAPSED,
    val widthDp: Float = 40f,
    val heightDp: Float = 28f,
    val centerXDp: Float = 20f,
    val centerYDp: Float = 14f,
    val progressX: Float = 0f,
    val progressY: Float = 0f,
    val cornerRadiusDp: Float = 14f
)

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
 * Implements the unified living glass shell:
 * - Phase 1: Horizontal pinch into a 28dp circle bubble with converging 3-dots.
 * - Phase 2: OriginOS two-phase spring flight stretching vertically into liquid droplet.
 * - Phase 3: Blooming into squircle popup menu card with rebound damping.
 * - Symmetrical collapse & unpinch cycle on dismiss.
 * - Live continuous AGSL liquid glass shader.
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
    onExpandToggle: () -> Unit = {},
    onTelemetryUpdate: ((OriginOSPhysicsTelemetry) -> Unit)? = null
) {
    OriginOSLiquidMorphContainer(
        isExpanded = isExpanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        backdrop = backdrop,
        isGlassEnabled = isGlassEnabled,
        animationSpeedMultiplier = animationSpeedMultiplier,
        onExpandToggle = onExpandToggle,
        onTelemetryUpdate = onTelemetryUpdate,
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
 *
 * Uses a single unified living shell for the anchor and expanded card to eliminate
 * any flicker, pop-in, or layout shifts in the parent row.
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
    onExpandToggle: () -> Unit = {},
    onTelemetryUpdate: ((OriginOSPhysicsTelemetry) -> Unit)? = null,
    menuContent: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val density = LocalDensity.current

    val bubbleSize = minOf(anchorWidth, anchorHeight) // 28.dp
    val bubbleRadius = bubbleSize / 2f // 14.dp

    // Anchor center in local container coordinates
    val anchorCenterX = anchorWidth / 2f // 20.dp
    val anchorCenterY = anchorHeight / 2f // 14.dp

    // Target menu position (top sits 8dp below anchor, right aligns with anchor right edge)
    val targetCenterX = anchorWidth - (menuWidth / 2f) // -60.dp
    val targetCenterY = (anchorHeight + 8.dp) + (menuHeight / 2f) // 148.dp

    val deltaCenterX = targetCenterX - anchorCenterX // -80.dp
    val deltaCenterY = targetCenterY - anchorCenterY // +134.dp

    // Dynamic morph state variables
    var morphPhase by remember { mutableStateOf(if (isExpanded) OriginOSMorphPhase.IDLE_EXPANDED else OriginOSMorphPhase.IDLE_COLLAPSED) }
    var currentWidthDp by remember { mutableStateOf(if (isExpanded) menuWidth else anchorWidth) }
    var currentHeightDp by remember { mutableStateOf(if (isExpanded) menuHeight else anchorHeight) }
    var currentRadiusDp by remember { mutableStateOf(if (isExpanded) menuCornerRadius else anchorCornerRadius) }
    var currentCenterXDp by remember { mutableStateOf(if (isExpanded) targetCenterX else anchorCenterX) }
    var currentCenterYDp by remember { mutableStateOf(if (isExpanded) targetCenterY else anchorCenterY) }

    // Dots converging animation state
    var dotsSpacingDp by remember { mutableStateOf(if (isExpanded) 0.dp else 3.5.dp) }
    var dotsScale by remember { mutableFloatStateOf(if (isExpanded) 0f else 1f) }
    var dotsAlpha by remember { mutableFloatStateOf(if (isExpanded) 0f else 1f) }

    // Menu content fade/slide animation state
    var menuContentAlpha by remember { mutableFloatStateOf(if (isExpanded) 1f else 0f) }
    var menuSlideOffsetDp by remember { mutableStateOf(if (isExpanded) 0.dp else 12.dp) }

    var shadowProgress by remember { mutableFloatStateOf(if (isExpanded) 1f else 0f) }
    var isAnimating by remember { mutableStateOf(false) }

    // Continuous Choreographer physics evaluation loop
    LaunchedEffect(isExpanded, animationSpeedMultiplier) {
        val targetExpanded = isExpanded
        isAnimating = true

        val pinchDuration = 0.12f // 120ms horizontal pinch to bubble
        val unpinchDuration = 0.10f // 100ms unpinch from bubble to anchor
        val entrySpringDuration = OriginOSPopupConfigs.maxEntryDuration
        val exitSpringDuration = OriginOSPopupConfigs.maxExitDuration

        val totalDuration = if (targetExpanded) {
            pinchDuration + entrySpringDuration
        } else {
            exitSpringDuration + unpinchDuration
        }

        var elapsedSec = 0f
        var lastFrameNanos = 0L

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
                    // ==========================================
                    // ENTRY CYCLE (Pill -> Bubble -> Droplet -> Card)
                    // ==========================================
                    if (elapsedSec <= pinchDuration) {
                        // Phase 1: Pinch to Bubble
                        val p = (elapsedSec / pinchDuration).coerceIn(0f, 1f)
                        val ease = sin(p * (PI.toFloat() / 2f)) // Smooth ease-out sine

                        morphPhase = OriginOSMorphPhase.PINCH_TO_BUBBLE
                        currentWidthDp = anchorWidth - (anchorWidth - bubbleSize) * ease
                        currentHeightDp = anchorHeight
                        currentRadiusDp = bubbleRadius
                        currentCenterXDp = anchorCenterX
                        currentCenterYDp = anchorCenterY

                        dotsSpacingDp = 3.5.dp * (1f - ease)
                        dotsScale = 1f - ease
                        dotsAlpha = 1f - ease

                        menuContentAlpha = 0f
                        menuSlideOffsetDp = 12.dp
                        shadowProgress = 0.05f * ease

                        onTelemetryUpdate?.invoke(
                            OriginOSPhysicsTelemetry(
                                phase = morphPhase,
                                widthDp = currentWidthDp.value,
                                heightDp = currentHeightDp.value,
                                centerXDp = currentCenterXDp.value,
                                centerYDp = currentCenterYDp.value,
                                progressX = 0f,
                                progressY = 0f,
                                cornerRadiusDp = currentRadiusDp.value
                            )
                        )
                    } else {
                        // Phase 2 & 3: Droplet Flight & Bloom Squircle Card
                        val tSpring = elapsedSec - pinchDuration
                        val ux = OriginOSPopupConfigs.entryScaleX.evaluate(tSpring, 0.0f, 1.0f)
                        val uy = OriginOSPopupConfigs.entryScaleY.evaluate(tSpring, 0.0f, 1.0f)
                        val utx = OriginOSPopupConfigs.entryTranslationX.evaluate(tSpring, 0.0f, 1.0f)
                        val uty = OriginOSPopupConfigs.entryTranslationY.evaluate(tSpring, 0.0f, 1.0f)

                        morphPhase = if (ux < 0.45f) {
                            OriginOSMorphPhase.DROPLET_FLIGHT
                        } else {
                            OriginOSMorphPhase.BLOOM_BOUNCE
                        }

                        currentWidthDp = bubbleSize + (menuWidth - bubbleSize) * ux
                        currentHeightDp = bubbleSize + (menuHeight - bubbleSize) * uy
                        currentRadiusDp = bubbleRadius + (menuCornerRadius - bubbleRadius) * ux.coerceIn(0f, 1f)
                        currentCenterXDp = anchorCenterX + deltaCenterX * utx
                        currentCenterYDp = anchorCenterY + deltaCenterY * uty

                        dotsAlpha = 0f
                        dotsScale = 0f
                        dotsSpacingDp = 0.dp

                        // Menu items fade in during squircle bloom
                        val revealProgress = ((ux - 0.40f) / 0.50f).coerceIn(0f, 1f)
                        menuContentAlpha = revealProgress
                        menuSlideOffsetDp = ((1f - uy.coerceIn(0f, 1f)) * 12f).dp
                        shadowProgress = uy.coerceIn(0f, 1f)

                        onTelemetryUpdate?.invoke(
                            OriginOSPhysicsTelemetry(
                                phase = morphPhase,
                                widthDp = currentWidthDp.value,
                                heightDp = currentHeightDp.value,
                                centerXDp = currentCenterXDp.value,
                                centerYDp = currentCenterYDp.value,
                                progressX = ux,
                                progressY = uy,
                                cornerRadiusDp = currentRadiusDp.value
                            )
                        )
                    }
                } else {
                    // ==========================================
                    // EXIT CYCLE (Card -> Bubble -> Unpinch -> Pill)
                    // ==========================================
                    if (elapsedSec <= exitSpringDuration) {
                        // Phase 4: Collapse back to bubble
                        val ux = OriginOSPopupConfigs.exitScaleX.evaluate(elapsedSec, 1.0f, 0.0f)
                        val uy = OriginOSPopupConfigs.exitScaleY.evaluate(elapsedSec, 1.0f, 0.0f)
                        val utx = OriginOSPopupConfigs.exitTranslationX.evaluate(elapsedSec, 1.0f, 0.0f)
                        val uty = OriginOSPopupConfigs.exitTranslationY.evaluate(elapsedSec, 1.0f, 0.0f)

                        morphPhase = OriginOSMorphPhase.COLLAPSE_BUBBLE
                        currentWidthDp = bubbleSize + (menuWidth - bubbleSize) * ux
                        currentHeightDp = bubbleSize + (menuHeight - bubbleSize) * uy
                        currentRadiusDp = bubbleRadius + (menuCornerRadius - bubbleRadius) * ux.coerceIn(0f, 1f)
                        currentCenterXDp = anchorCenterX + deltaCenterX * utx
                        currentCenterYDp = anchorCenterY + deltaCenterY * uty

                        // Menu fades out instantly
                        menuContentAlpha = (1f - (elapsedSec / 0.08f)).coerceIn(0f, 1f)
                        menuSlideOffsetDp = ((elapsedSec / 0.08f).coerceIn(0f, 1f) * 12f).dp
                        shadowProgress = uy.coerceIn(0f, 1f)

                        dotsAlpha = 0f
                        dotsScale = 0f
                        dotsSpacingDp = 0.dp

                        onTelemetryUpdate?.invoke(
                            OriginOSPhysicsTelemetry(
                                phase = morphPhase,
                                widthDp = currentWidthDp.value,
                                heightDp = currentHeightDp.value,
                                centerXDp = currentCenterXDp.value,
                                centerYDp = currentCenterYDp.value,
                                progressX = ux,
                                progressY = uy,
                                cornerRadiusDp = currentRadiusDp.value
                            )
                        )
                    } else {
                        // Phase 5: Unpinch from bubble back to anchor pill
                        val tUnpinch = elapsedSec - exitSpringDuration
                        val p = (tUnpinch / unpinchDuration).coerceIn(0f, 1f)
                        val ease = sin(p * (PI.toFloat() / 2f))

                        morphPhase = OriginOSMorphPhase.UNPINCH_ANCHOR
                        currentWidthDp = bubbleSize + (anchorWidth - bubbleSize) * ease
                        currentHeightDp = anchorHeight
                        currentRadiusDp = anchorCornerRadius
                        currentCenterXDp = anchorCenterX
                        currentCenterYDp = anchorCenterY

                        dotsSpacingDp = 3.5.dp * ease
                        dotsScale = ease
                        dotsAlpha = ease

                        menuContentAlpha = 0f
                        shadowProgress = 0f

                        onTelemetryUpdate?.invoke(
                            OriginOSPhysicsTelemetry(
                                phase = morphPhase,
                                widthDp = currentWidthDp.value,
                                heightDp = currentHeightDp.value,
                                centerXDp = currentCenterXDp.value,
                                centerYDp = currentCenterYDp.value,
                                progressX = 0f,
                                progressY = 0f,
                                cornerRadiusDp = currentRadiusDp.value
                            )
                        )
                    }
                }
            }
        }

        // Terminal states
        if (targetExpanded) {
            morphPhase = OriginOSMorphPhase.IDLE_EXPANDED
            currentWidthDp = menuWidth
            currentHeightDp = menuHeight
            currentRadiusDp = menuCornerRadius
            currentCenterXDp = targetCenterX
            currentCenterYDp = targetCenterY
            dotsAlpha = 0f
            dotsScale = 0f
            dotsSpacingDp = 0.dp
            menuContentAlpha = 1.0f
            menuSlideOffsetDp = 0.dp
            shadowProgress = 1.0f
        } else {
            morphPhase = OriginOSMorphPhase.IDLE_COLLAPSED
            currentWidthDp = anchorWidth
            currentHeightDp = anchorHeight
            currentRadiusDp = anchorCornerRadius
            currentCenterXDp = anchorCenterX
            currentCenterYDp = anchorCenterY
            dotsSpacingDp = 3.5.dp
            dotsScale = 1.0f
            dotsAlpha = 1.0f
            menuContentAlpha = 0f
            shadowProgress = 0f
        }

        onTelemetryUpdate?.invoke(
            OriginOSPhysicsTelemetry(
                phase = morphPhase,
                widthDp = currentWidthDp.value,
                heightDp = currentHeightDp.value,
                centerXDp = currentCenterXDp.value,
                centerYDp = currentCenterYDp.value,
                progressX = if (targetExpanded) 1.0f else 0.0f,
                progressY = if (targetExpanded) 1.0f else 0.0f,
                cornerRadiusDp = currentRadiusDp.value
            )
        )
        isAnimating = false
    }

    // Outer layout anchor box - stays fixed at 40dp x 28dp so Albums header never shifts
    Box(
        modifier = modifier.size(width = anchorWidth, height = anchorHeight),
        contentAlignment = Alignment.TopStart
    ) {
        val shape = RoundedCornerShape(currentRadiusDp)

        // Unified Living Glass Shell
        Box(
            modifier = Modifier
                .wrapContentSize(Alignment.TopStart, unbounded = true)
                .offset {
                    IntOffset(
                        x = (currentCenterXDp - (currentWidthDp / 2f)).roundToPx(),
                        y = (currentCenterYDp - (currentHeightDp / 2f)).roundToPx()
                    )
                }
                .size(width = currentWidthDp, height = currentHeightDp)
                .shadow(
                    elevation = (12.dp * shadowProgress).coerceAtLeast(0.dp),
                    shape = shape,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.15f * shadowProgress),
                    spotColor = Color.Black.copy(alpha = 0.25f * shadowProgress)
                )
                .clip(shape)
                .then(
                    if (backdrop != null && isGlassEnabled) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { shape },
                            effects = {
                                blur(with(density) { (12.dp + 6.dp * shadowProgress).toPx() })
                                vivoLiquidGlass(
                                    size = Size(
                                        with(density) { currentWidthDp.toPx() },
                                        with(density) { currentHeightDp.toPx() }
                                    ),
                                    cornerRadius = with(density) { currentRadiusDp.toPx() },
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
                .then(
                    if (!isExpanded && !isAnimating) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onExpandToggle()
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            // Glass tint highlight
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.08f)
                        else Color.Black.copy(alpha = 0.04f)
                    )
            )

            // Sub-layer 1: 3-Dots Icon (Converges and dissolves into bubble)
            if (dotsAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(dotsAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    OriginOSThreeDotsIcon(
                        color = if (isDark) Color.White else Color.Black,
                        spacing = dotsSpacingDp,
                        dotScale = dotsScale
                    )
                }
            }

            // Sub-layer 2: Menu Items Content (Fades and slides into squircle card)
            if (menuContentAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = menuSlideOffsetDp)
                        .alpha(menuContentAlpha)
                ) {
                    menuContent()
                }
            }
        }
    }
}

/**
 * OriginOS 3-dot Pill Button Anchor (Fallback / Standalone).
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
                if (isDark) Color.White.copy(alpha = 0.15f)
                else Color.Black.copy(alpha = 0.08f)
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
 * OriginOS 3 horizontal dots icon with dynamic converging spacing and scaling.
 */
@Composable
fun OriginOSThreeDotsIcon(
    color: Color,
    spacing: Dp = 3.5.dp,
    dotScale: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val currentDotSize = 3.5.dp * dotScale.coerceIn(0f, 1f)
        if (currentDotSize > 0.1.dp) {
            val step = (3.5.dp + spacing) * dotScale.coerceIn(0f, 1f)
            // Left dot converging inward
            Box(
                modifier = Modifier
                    .offset(x = -step)
                    .size(currentDotSize)
                    .background(color, CircleShape)
            )
            // Center dot
            Box(
                modifier = Modifier
                    .size(currentDotSize)
                    .background(color, CircleShape)
            )
            // Right dot converging inward
            Box(
                modifier = Modifier
                    .offset(x = step)
                    .size(currentDotSize)
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
