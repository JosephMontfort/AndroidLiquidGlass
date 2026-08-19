package com.kyant.backdrop.catalog.destinations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.catalog.BackdropDemoScaffold
import com.kyant.backdrop.catalog.components.LiquidButton
import com.kyant.backdrop.catalog.components.LiquidToggle
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tanh

@Composable
fun DropdownMenuContent() {
    val isLightTheme = !isSystemInDarkTheme()
    val contentColor = if (isLightTheme) Color.Black else Color.White
    val secondaryColor = if (isLightTheme) Color.Gray else Color(0xFFAAAAAA)
    val cardBackground = if (isLightTheme) Color.White.copy(alpha = 0.5f) else Color(0xFF1E1E1E).copy(alpha = 0.5f)

    // Base Properties
    var selectedAlignment by remember { mutableStateOf(MenuAlignment.TopTrailing) }
    var selectedPreset by remember { mutableStateOf(MenuAnimationPreset.Bouncy) }
    var isGlassEnabled by remember { mutableStateOf(true) }
    var isHapticsEnabled by remember { mutableStateOf(true) }
    
    // Dynamic Options Data
    var itemCount by remember { mutableFloatStateOf(4f) }
    var selectedIndex by remember { mutableStateOf(0) }
    
    // Glass Design State
    var cornerRadiusDp by remember { mutableFloatStateOf(20f) }
    var blurRadiusDp by remember { mutableFloatStateOf(10f) }
    var refractionHeightDp by remember { mutableFloatStateOf(16f) }
    var refractionAmountDp by remember { mutableFloatStateOf(20f) }
    var chromaticAberration by remember { mutableStateOf(false) }
    var horizontalOffsetDp by remember { mutableFloatStateOf(0f) }
    var verticalOffsetDp by remember { mutableFloatStateOf(0f) }

    // Advanced Physics Tuning State
    var animationSpeedMultiplier by remember { mutableFloatStateOf(1f) }
    var containerBulgeMultiplier by remember { mutableFloatStateOf(0.395f) }
    var contentBulgeMultiplier by remember { mutableFloatStateOf(0.15f) }
    var contentPopX by remember { mutableFloatStateOf(0.03f) }
    var contentPopY by remember { mutableFloatStateOf(0.01f) }
    var dragJellyTension by remember { mutableFloatStateOf(0.05f) }
    var motionBlurAmount by remember { mutableFloatStateOf(25f) }
    var arcYOffsetDp by remember { mutableFloatStateOf(75f) }

    val animationScope = rememberCoroutineScope()
    val animatableProgress = remember { Animatable(0f) }

    BackdropDemoScaffold { backdrop ->
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 16.dp)
        ) {
            BasicText("Settings Row Dropdown", Modifier.padding(top = 16.dp, bottom = 4.dp), style = TextStyle(contentColor, 26.sp, FontWeight.SemiBold))
            BasicText("Preview", style = TextStyle(Color(0xFF0088FF), 15.sp, FontWeight.Medium))

            // PREVIEW BOX (Horizontal Layout)
            Box(
                modifier = Modifier.fillMaxWidth().height(340.dp).clip(RoundedCornerShape(20.dp)).background(Color.Black.copy(alpha = 0.08f))
                    .pointerInput(selectedPreset, animationSpeedMultiplier) {
                        detectTapGestures {
                            if (animatableProgress.value > 0.1f) {
                                animationScope.launch { animatableProgress.animateTo(0f, selectedPreset.getSpec(isClosing = true, speedMultiplier = animationSpeedMultiplier)) }
                            }
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top // Ensures dropdown expands downward relative to the row
                ) {
                    BasicText("Playback Quality", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold), modifier = Modifier.padding(top = 10.dp))
                    
                    StandardGlassDropdownMenu(
                        animatableProgress = animatableProgress,
                        animationPreset = selectedPreset,
                        animationSpeedMultiplier = animationSpeedMultiplier,
                        containerBulgeMultiplier = containerBulgeMultiplier,
                        contentBulgeMultiplier = contentBulgeMultiplier,
                        contentPopX = contentPopX,
                        contentPopY = contentPopY,
                        dragJellyTension = dragJellyTension,
                        motionBlurAmount = motionBlurAmount,
                        arcYOffsetDp = arcYOffsetDp,
                        alignment = selectedAlignment,
                        backdrop = backdrop,
                        isGlassEnabled = isGlassEnabled,
                        isHapticsEnabled = isHapticsEnabled,
                        cornerRadius = cornerRadiusDp.dp,
                        blurRadius = blurRadiusDp,
                        refractionHeight = refractionHeightDp,
                        refractionAmount = refractionAmountDp,
                        chromaticAberration = chromaticAberration,
                        horizontalOffset = horizontalOffsetDp,
                        verticalOffset = verticalOffsetDp,
                        label = {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                BasicText("Option ${selectedIndex + 1}", style = TextStyle(contentColor, 16.sp, FontWeight.Medium))
                                BasicText("▼", style = TextStyle(contentColor.copy(0.6f), 10.sp))
                            }
                        }
                    ) { globalTouch, closeMenu, hoveredIndex, setHovered ->
                        repeat(itemCount.toInt()) { index ->
                            DropdownItemRow(
                                title = "Option ${index + 1}",
                                contentColor = contentColor,
                                isSelected = selectedIndex == index,
                                isHovered = hoveredIndex == index,
                                onHoverChange = { if (it) setHovered(index) else if (hoveredIndex == index) setHovered(null) },
                                globalTouchPosition = globalTouch,
                                onClick = {
                                    selectedIndex = index
                                    closeMenu()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 16.dp).clip(RoundedCornerShape(24.dp)).background(cardBackground).verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // CORE PROPERTIES SECTION
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BasicText("Core Properties", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold))

                    TuningControl("Menu Items", "Number of options to display in opened menu.", itemCount, 4f, { itemCount = it }, 1f..10f, { "${it.toInt()}" }, emptyBackdrop(), contentColor, secondaryColor)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BasicText("Animation Trigger", style = TextStyle(contentColor, 14.sp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MenuAnimationPreset.entries.forEach { preset ->
                                val isSelected = selectedPreset == preset
                                LiquidButton(onClick = { selectedPreset = preset; val target = if (animatableProgress.value > 0.5f) 0f else 1f; animationScope.launch { animatableProgress.animateTo(target, preset.getSpec(isClosing = target == 0f, speedMultiplier = animationSpeedMultiplier)) } }, backdrop = emptyBackdrop(), modifier = Modifier.weight(1f).height(42.dp), tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified, surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.15f)) {
                                    BasicText(preset.label, style = TextStyle(if (isSelected) Color.White else contentColor, 13.sp, FontWeight.Medium))
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BasicText("Haptic Feedback", style = TextStyle(contentColor, 14.sp))
                        LiquidToggle(selected = { isHapticsEnabled }, onSelect = { isHapticsEnabled = it }, backdrop = emptyBackdrop())
                    }
                }

                // LAYOUT OFFSETS SECTION
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BasicText("Layout Translation", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold))

                    TuningControl("Horizontal Offset", "Static layout shift.", horizontalOffsetDp, 0f, { horizontalOffsetDp = it }, -150f..150f, { "${it.toInt()} dp" }, emptyBackdrop(), contentColor, secondaryColor)
                    TuningControl("Vertical Offset", "Static layout shift.", verticalOffsetDp, 0f, { verticalOffsetDp = it }, -150f..150f, { "${it.toInt()} dp" }, emptyBackdrop(), contentColor, secondaryColor)
                }

                // ADVANCED PHYSICS TUNING SECTION
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BasicText("Advanced Physics Tuning", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold))

                    TuningControl("Animation Speed", "Multiplies the entire spring natural frequency & tween duration.", animationSpeedMultiplier, 1f, { animationSpeedMultiplier = it }, 0.1f..4f, { "${String.format("%.2f", it)}x" }, emptyBackdrop(), contentColor, secondaryColor)
                    TuningControl("Container Bulge Ratio", "Friction dampener for physical bounds expansion during spring overshoot.", containerBulgeMultiplier, 0.395f, { containerBulgeMultiplier = it }, 0f..1f, { String.format("%.3f", it) }, emptyBackdrop(), contentColor, secondaryColor)
                    TuningControl("Inner Content Bulge Ratio", "Friction dampener applied to inner text scaling during overshoot.", contentBulgeMultiplier, 0.15f, { contentBulgeMultiplier = it }, 0f..1f, { String.format("%.3f", it) }, emptyBackdrop(), contentColor, secondaryColor)
                    TuningControl("Content Pop X Amplitude", "Horizontal absolute scale addition during the animation sine wave.", contentPopX, 0.03f, { contentPopX = it }, 0f..0.2f, { String.format("%.3f", it) }, emptyBackdrop(), contentColor, secondaryColor)
                    TuningControl("Content Pop Y Amplitude", "Vertical absolute scale addition during the animation sine wave.", contentPopY, 0.01f, { contentPopY = it }, 0f..0.2f, { String.format("%.3f", it) }, emptyBackdrop(), contentColor, secondaryColor)
                    TuningControl("Drag Jelly Tension", "The hyperbolic tangent derivative controlling fluid squish resistance.", dragJellyTension, 0.05f, { dragJellyTension = it }, 0.01f..0.2f, { String.format("%.3f", it) }, emptyBackdrop(), contentColor, secondaryColor)
                    TuningControl("Motion Blur Peak", "Maximum directional blur generated at highest velocity points.", motionBlurAmount, 25f, { motionBlurAmount = it }, 0f..80f, { "${it.toInt()} dp" }, emptyBackdrop(), contentColor, secondaryColor)
                    TuningControl("Arc Y-Axis Limit", "The physical ceiling of the triangular wave driving the vertical translation arc.", arcYOffsetDp, 75f, { arcYOffsetDp = it }, 0f..250f, { "${it.toInt()} dp" }, emptyBackdrop(), contentColor, secondaryColor)
                }

                // GLASS RENDERING SECTION
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BasicText("Glass Rendering", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BasicText("Glass Effect", style = TextStyle(contentColor, 14.sp))
                        LiquidToggle(selected = { isGlassEnabled }, onSelect = { isGlassEnabled = it }, backdrop = emptyBackdrop())
                    }

                    if (isGlassEnabled) {
                        TuningControl("Corner Radius", null, cornerRadiusDp, 20f, { cornerRadiusDp = it }, 0f..64f, { "${it.toInt()} dp" }, emptyBackdrop(), contentColor, secondaryColor)
                        TuningControl("Blur Radius", null, blurRadiusDp, 10f, { blurRadiusDp = it }, 0f..32f, { "${it.toInt()} dp" }, emptyBackdrop(), contentColor, secondaryColor)
                        TuningControl("Refraction Height", null, refractionHeightDp, 16f, { refractionHeightDp = it }, 0f..48f, { "${it.toInt()} dp" }, emptyBackdrop(), contentColor, secondaryColor)
                        TuningControl("Refraction Amount", null, refractionAmountDp, 20f, { refractionAmountDp = it }, 0f..64f, { "${it.toInt()} dp" }, emptyBackdrop(), contentColor, secondaryColor)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            BasicText("Chromatic Aberration", style = TextStyle(contentColor, 14.sp))
                            LiquidToggle(selected = { chromaticAberration }, onSelect = { chromaticAberration = it }, backdrop = emptyBackdrop())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun StandardGlassDropdownMenu(
    animatableProgress: Animatable<Float, *>,
    animationPreset: MenuAnimationPreset,
    animationSpeedMultiplier: Float = 1f,
    containerBulgeMultiplier: Float = 0.395f,
    contentBulgeMultiplier: Float = 0.15f,
    contentPopX: Float = 0.03f,
    contentPopY: Float = 0.01f,
    dragJellyTension: Float = 0.05f,
    motionBlurAmount: Float = 25f,
    arcYOffsetDp: Float = 75f,
    alignment: MenuAlignment,
    backdrop: Backdrop,
    isGlassEnabled: Boolean,
    isHapticsEnabled: Boolean,
    cornerRadius: Dp,
    blurRadius: Float,
    refractionHeight: Float,
    refractionAmount: Float,
    chromaticAberration: Boolean,
    horizontalOffset: Float,
    verticalOffset: Float,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    content: @Composable (globalTouchPosition: Offset, closeMenu: () -> Unit, hoveredIndex: Int?, setHovered: (Int?) -> Unit) -> Unit
) {
    val density = LocalDensity.current
    val viewConfiguration = LocalViewConfiguration.current
    val haptic = LocalHapticFeedback.current
    val animationScope = rememberCoroutineScope()
    
    var contentMeasuredSize by remember { mutableStateOf(Size.Zero) }
    var labelMeasuredSize by remember { mutableStateOf(Size.Zero) }
    
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isPressed by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var globalTouchPosition by remember { mutableStateOf(Offset.Unspecified) }
    var labelCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    val setHovered: (Int?) -> Unit = { hoveredIndex = it }

    val labelSizePx = if (labelMeasuredSize != Size.Zero) labelMeasuredSize else with(density) { Size(100.dp.toPx(), 40.dp.toPx()) }

    LaunchedEffect(hoveredIndex) {
        if (isHapticsEnabled && isDragging && hoveredIndex != null) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val closeMenu: () -> Unit = {
        hoveredIndex = null
        animationScope.launch { animatableProgress.animateTo(0f, animationPreset.getSpec(isClosing = true, speedMultiplier = animationSpeedMultiplier)) }
    }

    Box(modifier = modifier, contentAlignment = alignment.composeAlignment) {
        StandardGlassEffectContainer(
            animatableProgress = animatableProgress,
            dragOffset = dragOffset,
            isPressed = isPressed,
            containerBulgeMultiplier = containerBulgeMultiplier,
            contentBulgeMultiplier = contentBulgeMultiplier,
            contentPopX = contentPopX,
            contentPopY = contentPopY,
            dragJellyTension = dragJellyTension,
            motionBlurAmount = motionBlurAmount,
            arcYOffsetDp = arcYOffsetDp,
            alignment = alignment,
            backdrop = backdrop,
            isGlassEnabled = isGlassEnabled,
            cornerRadius = cornerRadius,
            blurRadius = blurRadius,
            refractionHeight = refractionHeight,
            refractionAmount = refractionAmount,
            chromaticAberration = chromaticAberration,
            horizontalOffset = horizontalOffset,
            verticalOffset = verticalOffset,
            labelSize = labelSizePx,
            contentSize = contentMeasuredSize,
            label = {
                Box(
                    modifier = Modifier.onSizeChanged { labelMeasuredSize = it.toSize() }.onGloballyPositioned { labelCoordinates = it }
                        .pointerInput(animationPreset, animationSpeedMultiplier) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                isPressed = true
                                val downPos = down.position
                                val tapTimeout = viewConfiguration.longPressTimeoutMillis
                                
                                var isLongPress = false
                                var isSimpleDrag = false
                                var upEvent: androidx.compose.ui.input.pointer.PointerInputChange? = null
                                
                                val isExpanded = animatableProgress.value > 0.5f
                                
                                val timeoutResult = withTimeoutOrNull(tapTimeout) {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Main)
                                        val change = event.changes.firstOrNull()
                                        if (change != null) {
                                            if (!change.pressed) {
                                                upEvent = change
                                                break
                                            } else if ((change.position - downPos).getDistance() > viewConfiguration.touchSlop) {
                                                isSimpleDrag = true
                                                break
                                            }
                                        }
                                    }
                                    true
                                }

                                if (timeoutResult == null && !isExpanded) {
                                    isLongPress = true
                                    animationScope.launch { animatableProgress.animateTo(1f, animationPreset.getSpec(isClosing = false, speedMultiplier = animationSpeedMultiplier)) }
                                }

                                var tracking = upEvent == null
                                isDragging = true
                                while (tracking) {
                                    val event = awaitPointerEvent(PointerEventPass.Main)
                                    val change = event.changes.firstOrNull()
                                    if (change == null || !change.pressed) {
                                        tracking = false
                                    } else {
                                        dragOffset = change.position - downPos
                                        if (isLongPress || isExpanded) {
                                            globalTouchPosition = labelCoordinates?.localToWindow(change.position) ?: Offset.Unspecified
                                        }
                                        change.consume()
                                    }
                                }

                                isPressed = false
                                isDragging = false
                                val finalHoveredIndex = hoveredIndex

                                if (upEvent != null && !isSimpleDrag && !isLongPress && timeoutResult != null) {
                                    upEvent.consume()
                                    val target = if (isExpanded) 0f else 1f
                                    animationScope.launch { animatableProgress.animateTo(target, animationPreset.getSpec(isClosing = target == 0f, speedMultiplier = animationSpeedMultiplier)) }
                                } else if ((isSimpleDrag || isLongPress) && isExpanded) {
                                    if (finalHoveredIndex != null || dragOffset.getDistance() > 20f) {
                                        closeMenu()
                                    }
                                }
                                
                                dragOffset = Offset.Zero
                                globalTouchPosition = Offset.Unspecified
                                if (animatableProgress.targetValue == 0f) hoveredIndex = null
                            }
                        },
                    contentAlignment = Alignment.Center
                ) { label() }
            },
            content = {
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max).onSizeChanged { if (it.width > 0 && it.height > 0) contentMeasuredSize = it.toSize() }.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    content(globalTouchPosition, closeMenu, hoveredIndex, setHovered)
                }
            }
        )
    }
}

@Composable
private fun StandardGlassEffectContainer(
    animatableProgress: Animatable<Float, *>,
    dragOffset: Offset,
    isPressed: Boolean,
    containerBulgeMultiplier: Float,
    contentBulgeMultiplier: Float,
    contentPopX: Float,
    contentPopY: Float,
    dragJellyTension: Float,
    motionBlurAmount: Float,
    arcYOffsetDp: Float,
    alignment: MenuAlignment,
    backdrop: Backdrop,
    isGlassEnabled: Boolean,
    cornerRadius: Dp,
    blurRadius: Float,
    refractionHeight: Float,
    refractionAmount: Float,
    chromaticAberration: Boolean,
    horizontalOffset: Float,
    verticalOffset: Float,
    labelSize: Size,
    contentSize: Size,
    label: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val isLightTheme = !isSystemInDarkTheme()

    val animatedDragX by animateFloatAsState(dragOffset.x, spring(stiffness = 400f, dampingRatio = 0.6f))
    val animatedDragY by animateFloatAsState(dragOffset.y, spring(stiffness = 400f, dampingRatio = 0.6f))
    
    val isPressing = isPressed && animatableProgress.targetValue == 0f
    val buttonPressProgress by animateFloatAsState(if (isPressing) 1f else 0f, spring(dampingRatio = 0.6f, stiffness = 400f))

    Box(
        modifier = Modifier
            .layout { measurable, constraints ->
                val p = animatableProgress.value
                val widthDiff = (contentSize.width - labelSize.width).coerceAtLeast(0f)
                val heightDiff = (contentSize.height - labelSize.height).coerceAtLeast(0f)

                // CRASH FIX: Clamp the layout physics so undershoot does not invert physical dimensions
                val safeP = p.coerceAtLeast(0f)
                val widthProgress = if (safeP > 1f) 1f + (safeP - 1f) * containerBulgeMultiplier else (safeP * safeP)
                val heightProgress = if (safeP > 1f) 1f + (safeP - 1f) * containerBulgeMultiplier else sin(safeP * (PI / 2f)).toFloat()

                // Final safety clamp guarantees dimensions stay purely positive
                val currentWidthPx = (labelSize.width + widthDiff * widthProgress).coerceAtLeast(0f)
                val currentHeightPx = (labelSize.height + heightDiff * heightProgress).coerceAtLeast(0f)

                val placeable = measurable.measure(
                    constraints.copy(
                        minWidth = currentWidthPx.toInt(),
                        maxWidth = currentWidthPx.toInt(),
                        minHeight = currentHeightPx.toInt(),
                        maxHeight = currentHeightPx.toInt()
                    )
                )
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
            .graphicsLayer {
                val progress = animatableProgress.value
                val blurProgress = if (progress > 0.5f) ((1f - progress) / 0.5f).coerceAtLeast(0f) else (progress / 0.5f).coerceAtLeast(0f)
                val squishScale = 1f - (blurProgress * 0.05f)

                val maxOffsetPx = with(density) { arcYOffsetDp.dp.toPx() }
                val offsetY = alignment.calculateOffsetY(blurProgress, maxOffsetPx)

                val w = size.width
                val h = size.height
                val minDim = min(w, h)
                val maxDim = max(w, h)

                val maxJellyOffset = minDim
                val initialDerivative = dragJellyTension
                val jellyTx = if (maxJellyOffset > 0f) maxJellyOffset * tanh(initialDerivative * animatedDragX / maxJellyOffset) else 0f
                val jellyTy = if (maxJellyOffset > 0f) maxJellyOffset * tanh(initialDerivative * animatedDragY / maxJellyOffset) else 0f
                
                val baseScale = lerp(1f, 1f + with(density) { 4f.dp.toPx() } / h, buttonPressProgress)
                val maxDragScale = with(density) { 4f.dp.toPx() / h }
                val offsetAngle = atan2(animatedDragY, animatedDragX)
                
                val jellySx = baseScale + maxDragScale * abs(cos(offsetAngle) * animatedDragX / maxDim) * (w / h).coerceAtMost(1f)
                val jellySy = baseScale + maxDragScale * abs(sin(offsetAngle) * animatedDragY / maxDim) * (h / w).coerceAtMost(1f)

                val expandedTx = -animatedDragX * 0.03f
                val expandedTy = -animatedDragY * 0.03f
                val expandedStretchX = (1f + abs(animatedDragX) * 0.00005f - abs(animatedDragY) * 0.00005f).coerceIn(0.99f, 1.01f)
                val expandedStretchY = (1f + abs(animatedDragY) * 0.00005f - abs(animatedDragX) * 0.00005f).coerceIn(0.99f, 1.01f)

                translationX = lerp(jellyTx, expandedTx, progress) + with(density) { horizontalOffset.dp.toPx() * progress }
                translationY = offsetY + lerp(jellyTy, expandedTy, progress) + with(density) { verticalOffset.dp.toPx() * progress }
                scaleX = squishScale * lerp(jellySx, expandedStretchX, progress)
                scaleY = squishScale * lerp(jellySy, expandedStretchY, progress)
                this.transformOrigin = alignment.transformOrigin
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(cornerRadius) },
                effects = {
                    val progress = animatableProgress.value
                    // Container Opacity creates the "appearing out of nothing" illusion
                    val containerOpacity = (progress / 0.1f).coerceIn(0f, 1f)
                    
                    if (isGlassEnabled && containerOpacity > 0f) {
                        val blurProgress = if (progress > 0.5f) ((1f - progress) / 0.5f).coerceAtLeast(0f) else (progress / 0.5f).coerceAtLeast(0f)
                        val motionBlur = motionBlurAmount * sin(progress.coerceIn(0f, 1f) * PI).toFloat()
                        vibrancy()
                        blur((blurRadius * containerOpacity).dp.toPx() + (motionBlur * blurProgress).dp.toPx())
                        lens(
                            refractionHeight.dp.toPx() * containerOpacity,
                            refractionAmount.dp.toPx() * containerOpacity,
                            depthEffect = true,
                            chromaticAberration = chromaticAberration
                        )
                    }
                },
                highlight = { 
                    val containerOpacity = (animatableProgress.value / 0.1f).coerceIn(0f, 1f)
                    if (isGlassEnabled && containerOpacity > 0f) Highlight.Default.copy(alpha = 0.65f * containerOpacity) else null 
                },
                shadow = { 
                    val containerOpacity = (animatableProgress.value / 0.1f).coerceIn(0f, 1f)
                    Shadow(radius = 18.dp, color = Color.Black.copy(alpha = 0.12f * containerOpacity)) 
                },
                innerShadow = { 
                    val containerOpacity = (animatableProgress.value / 0.1f).coerceIn(0f, 1f)
                    if (isGlassEnabled && containerOpacity > 0f) InnerShadow(radius = 10.dp, color = Color.White.copy(alpha = 0.25f * containerOpacity)) else null 
                },
                onDrawSurface = {
                    val progress = animatableProgress.value
                    val containerOpacity = (progress / 0.1f).coerceIn(0f, 1f)
                    val cr = cornerRadius.toPx()
                    
                    if (containerOpacity > 0f) {
                        val baseAlpha = if (isLightTheme) 0.28f + 0.12f * progress else 0.35f + 0.15f * progress
                        drawRoundRect(
                            color = if (isLightTheme) Color.White.copy(alpha = baseAlpha * containerOpacity) else Color(0xFF1E1E1E).copy(alpha = baseAlpha * containerOpacity),
                            cornerRadius = CornerRadius(cr, cr)
                        )
                    }
                    
                    if (progress == 0f && buttonPressProgress > 0f) {
                        drawRoundRect(color = Color.White.copy(alpha = 0.1f * buttonPressProgress), cornerRadius = CornerRadius(cr, cr), blendMode = BlendMode.Plus)
                    }
                }
            )
            .clip(RoundedCornerShape(cornerRadius)),
        contentAlignment = alignment.composeAlignment
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize(unbounded = true, align = alignment.composeAlignment) 
                .graphicsLayer {
                    val progress = animatableProgress.value
                    val rawContentProgress = if (progress > 1f) 1f + (progress - 1f) * contentBulgeMultiplier else (progress - 0.35f) / 0.65f
                    val minAspectScale = if (contentSize.width > 0f && contentSize.height > 0f) { min(labelSize.width / contentSize.width, labelSize.height / contentSize.height) } else 1f
                    val baseScale = minAspectScale + (1f - minAspectScale) * rawContentProgress
                    
                    val pop = sin(progress.coerceIn(0f, 1f) * PI).toFloat()
                    
                    alpha = rawContentProgress.coerceIn(0f, 1f)
                    scaleX = baseScale + pop * contentPopX
                    scaleY = baseScale + pop * contentPopY
                    this.transformOrigin = TransformOrigin.Center
                }
        ) {
            content()
        }

        Box(
            modifier = Modifier.graphicsLayer { 
                val progress = animatableProgress.value
                val labelOpacity = (progress / 0.35f).coerceIn(0f, 1f)
                alpha = 1f - labelOpacity 
            }
        ) {
            label()
        }
    }
}

@Composable
private fun DropdownItemRow(
    title: String,
    contentColor: Color,
    isSelected: Boolean,
    isHovered: Boolean,
    onHoverChange: (Boolean) -> Unit,
    globalTouchPosition: Offset,
    onClick: () -> Unit
) {
    var rowCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    
    val contains = remember(globalTouchPosition, rowCoords) {
        if (globalTouchPosition.isUnspecified || rowCoords == null) false
        else rowCoords!!.boundsInWindow().contains(globalTouchPosition)
    }
    
    LaunchedEffect(contains) { onHoverChange(contains) }

    val hoverAlpha by animateFloatAsState(if (isHovered) 0.1f else 0f, tween(150))

    Row(
        modifier = Modifier.fillMaxWidth().onGloballyPositioned { rowCoords = it }.clip(RoundedCornerShape(12.dp)).clickable { onClick() }.background(contentColor.copy(alpha = hoverAlpha)).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BasicText(title, style = TextStyle(contentColor, 16.sp, FontWeight.Medium))
        if (isSelected) {
            BasicText("✓", style = TextStyle(Color(0xFF0088FF), 16.sp, FontWeight.Bold))
        }
    }
}
