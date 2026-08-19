package com.kyant.backdrop.catalog.destinations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
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
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.catalog.BackdropDemoScaffold
import com.kyant.backdrop.catalog.ReceiveIcon
import com.kyant.backdrop.catalog.SendIcon
import com.kyant.backdrop.catalog.ShareFilledIcon
import com.kyant.backdrop.catalog.SwapIcon
import com.kyant.backdrop.catalog.components.LiquidButton
import com.kyant.backdrop.catalog.components.LiquidSlider
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
fun FlippedMorphDropdownContent() {
    val isLightTheme = !isSystemInDarkTheme()
    val contentColor = if (isLightTheme) Color.Black else Color.White
    val secondaryColor = if (isLightTheme) Color.Gray else Color(0xFFAAAAAA)
    val cardBackground = if (isLightTheme) Color.White.copy(alpha = 0.5f) else Color(0xFF1E1E1E).copy(alpha = 0.5f)

    // Base Properties
    var selectedAlignment by remember { mutableStateOf(MenuAlignment.TopLeading) }
    var selectedPreset by remember { mutableStateOf(MenuAnimationPreset.Bouncy) }
    var isGlassEnabled by remember { mutableStateOf(true) }
    var isHapticsEnabled by remember { mutableStateOf(true) }
    
    // Glass Design State
    var cornerRadiusDp by remember { mutableFloatStateOf(30f) }
    var blurRadiusDp by remember { mutableFloatStateOf(10f) }
    var refractionHeightDp by remember { mutableFloatStateOf(16f) }
    var refractionAmountDp by remember { mutableFloatStateOf(20f) }
    var chromaticAberration by remember { mutableStateOf(false) }
    
    // Offset State
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
    
    // 3D Flip Tuning State
    var flipDegreesX by remember { mutableFloatStateOf(180f) }
    var flipDegreesY by remember { mutableFloatStateOf(0f) }
    var flipCameraDistance by remember { mutableFloatStateOf(32f) }
    var flipOvershootMultiplier by remember { mutableFloatStateOf(0.5f) }
    var flipDepthScale by remember { mutableFloatStateOf(0.1f) }

    val animationScope = rememberCoroutineScope()
    val animatableProgress = remember { Animatable(0f) }

    BackdropDemoScaffold { backdrop ->
        val controlsBackdrop = rememberLayerBackdrop()

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 16.dp)) {
            BasicText("Flipped Morph Dropdown", Modifier.padding(top = 16.dp, bottom = 4.dp), style = TextStyle(contentColor, 26.sp, FontWeight.SemiBold))
            BasicText("Preview", style = TextStyle(Color(0xFF0088FF), 15.sp, FontWeight.Medium))

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
                FlippedExpandableGlassMenu(
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
                    flipDegreesX = flipDegreesX,
                    flipDegreesY = flipDegreesY,
                    flipCameraDistance = flipCameraDistance,
                    flipOvershootMultiplier = flipOvershootMultiplier,
                    flipDepthScale = flipDepthScale,
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
                    modifier = Modifier.padding(16.dp),
                    label = {
                        Box(modifier = Modifier.size(55.dp), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(24.dp).paint(rememberVectorPainter(ShareFilledIcon), colorFilter = ColorFilter.tint(contentColor)))
                        }
                    }
                ) { globalTouch, closeMenu, hoveredIndex, setHovered ->
                    FlippedMenuRow(SendIcon, "Send", "This is a sample text description", contentColor, secondaryColor, hoveredIndex == 0, { if (it) setHovered(0) else if (hoveredIndex == 0) setHovered(null) }, globalTouchPosition = globalTouch, onClick = closeMenu)
                    FlippedMenuRow(SwapIcon, "Swap", "This is a sample text description", contentColor, secondaryColor, hoveredIndex == 1, { if (it) setHovered(1) else if (hoveredIndex == 1) setHovered(null) }, globalTouchPosition = globalTouch, onClick = closeMenu)
                    FlippedMenuRow(ReceiveIcon, "Receive", "This is a sample text description", contentColor, secondaryColor, hoveredIndex == 2, { if (it) setHovered(2) else if (hoveredIndex == 2) setHovered(null) }, globalTouchPosition = globalTouch, onClick = closeMenu)
                }
            }

            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 16.dp).clip(RoundedCornerShape(24.dp)).drawBackdrop(backdrop = backdrop, shape = { RoundedCornerShape(24.dp) }, effects = { vibrancy(); blur(8.dp.toPx()); lens(16.dp.toPx(), 32.dp.toPx()) }, highlight = { Highlight.Plain }, exportedBackdrop = controlsBackdrop, onDrawSurface = { drawRect(cardBackground) }).verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // CORE PROPERTIES SECTION
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BasicText("Core Properties", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Progress", style = TextStyle(contentColor, 14.sp)); BasicText("${(animatableProgress.value * 100).toInt()}%", style = TextStyle(secondaryColor, 14.sp))
                        }
                        LiquidSlider(value = { animatableProgress.value }, onValueChange = { animationScope.launch { animatableProgress.snapTo(it) } }, valueRange = 0f..1f, visibilityThreshold = 0.001f, backdrop = controlsBackdrop)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BasicText("Alignment", style = TextStyle(contentColor, 14.sp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MenuAlignment.entries.forEach { align ->
                                val isSelected = selectedAlignment == align
                                LiquidButton(onClick = { selectedAlignment = align }, backdrop = controlsBackdrop, modifier = Modifier.weight(1f).height(40.dp), tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified, surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.15f)) {
                                    BasicText(align.label, style = TextStyle(if (isSelected) Color.White else contentColor, 12.sp, FontWeight.Medium))
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BasicText("Animation Trigger", style = TextStyle(contentColor, 14.sp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MenuAnimationPreset.entries.forEach { preset ->
                                val isSelected = selectedPreset == preset
                                LiquidButton(onClick = { selectedPreset = preset; val target = if (animatableProgress.value > 0.5f) 0f else 1f; animationScope.launch { animatableProgress.animateTo(target, preset.getSpec(isClosing = target == 0f, speedMultiplier = animationSpeedMultiplier)) } }, backdrop = controlsBackdrop, modifier = Modifier.weight(1f).height(42.dp), tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified, surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.15f)) {
                                    BasicText(preset.label, style = TextStyle(if (isSelected) Color.White else contentColor, 13.sp, FontWeight.Medium))
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BasicText("Haptic Feedback", style = TextStyle(contentColor, 14.sp))
                        LiquidToggle(selected = { isHapticsEnabled }, onSelect = { isHapticsEnabled = it }, backdrop = controlsBackdrop)
                    }
                }

                // LAYOUT OFFSETS SECTION (Moved above Glass Settings)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BasicText("Layout Translation", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold))

                    TuningControl("Horizontal Offset", "Static layout shift.", horizontalOffsetDp, 0f, { horizontalOffsetDp = it }, -150f..150f, { "${it.toInt()} dp" }, controlsBackdrop, contentColor, secondaryColor)
                    TuningControl("Vertical Offset", "Static layout shift.", verticalOffsetDp, 0f, { verticalOffsetDp = it }, -150f..150f, { "${it.toInt()} dp" }, controlsBackdrop, contentColor, secondaryColor)
                }

                // 3D FLIP TUNING SECTION
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BasicText("3D Flip Mechanics", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold))
                    
                    TuningControl("Flip Axis X", "Vertical rotation magnitude in degrees.", flipDegreesX, 180f, { flipDegreesX = it }, -360f..360f, { "${it.toInt()}°" }, controlsBackdrop, contentColor, secondaryColor)
                    TuningControl("Flip Axis Y", "Horizontal rotation magnitude in degrees.", flipDegreesY, 0f, { flipDegreesY = it }, -360f..360f, { "${it.toInt()}°" }, controlsBackdrop, contentColor, secondaryColor)
                    TuningControl("Camera Distance", "Controls perspective distortion. Higher values flatten the 3D effect.", flipCameraDistance, 32f, { flipCameraDistance = it }, 1f..100f, { String.format("%.1f", it) }, controlsBackdrop, contentColor, secondaryColor)
                    TuningControl("Flip Depth Scale", "Amount the card shrinks into the Z-axis during flip to add 3D clearance.", flipDepthScale, 0.1f, { flipDepthScale = it }, 0f..0.5f, { String.format("%.3f", it) }, controlsBackdrop, contentColor, secondaryColor)
                    TuningControl("Flip Overshoot Dampener", "Friction applied specifically to 3D rotation during spring overshoot.", flipOvershootMultiplier, 0.5f, { flipOvershootMultiplier = it }, 0f..1f, { String.format("%.3f", it) }, controlsBackdrop, contentColor, secondaryColor)
                }

                // ADVANCED PHYSICS TUNING SECTION
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BasicText("Advanced Physics Tuning", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold))

                    TuningControl("Animation Speed", "Multiplies the entire spring natural frequency & tween duration.", animationSpeedMultiplier, 1f, { animationSpeedMultiplier = it }, 0.1f..4f, { "${String.format("%.2f", it)}x" }, controlsBackdrop, contentColor, secondaryColor)
                    TuningControl("Container Bulge Ratio", "Friction dampener for physical bounds expansion during spring overshoot.", containerBulgeMultiplier, 0.395f, { containerBulgeMultiplier = it }, 0f..1f, { String.format("%.3f", it) }, controlsBackdrop, contentColor, secondaryColor)
                    TuningControl("Inner Content Bulge Ratio", "Friction dampener applied to inner text scaling during overshoot.", contentBulgeMultiplier, 0.15f, { contentBulgeMultiplier = it }, 0f..1f, { String.format("%.3f", it) }, controlsBackdrop, contentColor, secondaryColor)
                    TuningControl("Content Pop X Amplitude", "Horizontal absolute scale addition during the animation sine wave.", contentPopX, 0.03f, { contentPopX = it }, 0f..0.2f, { String.format("%.3f", it) }, controlsBackdrop, contentColor, secondaryColor)
                    TuningControl("Content Pop Y Amplitude", "Vertical absolute scale addition during the animation sine wave.", contentPopY, 0.01f, { contentPopY = it }, 0f..0.2f, { String.format("%.3f", it) }, controlsBackdrop, contentColor, secondaryColor)
                    TuningControl("Drag Jelly Tension", "The hyperbolic tangent derivative controlling fluid squish resistance.", dragJellyTension, 0.05f, { dragJellyTension = it }, 0.01f..0.2f, { String.format("%.3f", it) }, controlsBackdrop, contentColor, secondaryColor)
                    TuningControl("Motion Blur Peak", "Maximum directional blur generated at highest velocity points.", motionBlurAmount, 25f, { motionBlurAmount = it }, 0f..80f, { "${it.toInt()} dp" }, controlsBackdrop, contentColor, secondaryColor)
                    TuningControl("Arc Y-Axis Limit", "The physical ceiling of the triangular wave driving vertical arcs.", arcYOffsetDp, 75f, { arcYOffsetDp = it }, 0f..250f, { "${it.toInt()} dp" }, controlsBackdrop, contentColor, secondaryColor)
                }

                // GLASS RENDERING SECTION
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BasicText("Glass Rendering", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BasicText("Glass Effect", style = TextStyle(contentColor, 14.sp))
                        LiquidToggle(selected = { isGlassEnabled }, onSelect = { isGlassEnabled = it }, backdrop = controlsBackdrop)
                    }

                    if (isGlassEnabled) {
                        TuningControl("Corner Radius", null, cornerRadiusDp, 30f, { cornerRadiusDp = it }, 0f..64f, { "${it.toInt()} dp" }, controlsBackdrop, contentColor, secondaryColor)
                        TuningControl("Blur Radius", null, blurRadiusDp, 10f, { blurRadiusDp = it }, 0f..32f, { "${it.toInt()} dp" }, controlsBackdrop, contentColor, secondaryColor)
                        TuningControl("Refraction Height", null, refractionHeightDp, 16f, { refractionHeightDp = it }, 0f..48f, { "${it.toInt()} dp" }, controlsBackdrop, contentColor, secondaryColor)
                        TuningControl("Refraction Amount", null, refractionAmountDp, 20f, { refractionAmountDp = it }, 0f..64f, { "${it.toInt()} dp" }, controlsBackdrop, contentColor, secondaryColor)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            BasicText("Chromatic Aberration", style = TextStyle(contentColor, 14.sp))
                            LiquidToggle(selected = { chromaticAberration }, onSelect = { chromaticAberration = it }, backdrop = controlsBackdrop)
                        }
                    }
                }

                // Dedicated spacer to prevent FAB overlay obstruction
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun FlippedExpandableGlassMenu(
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
    flipDegreesX: Float = 180f,
    flipDegreesY: Float = 0f,
    flipCameraDistance: Float = 32f,
    flipOvershootMultiplier: Float = 0.5f,
    flipDepthScale: Float = 0.1f,
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
    labelSize: Size = Size(55f, 55f),
    label: @Composable () -> Unit,
    content: @Composable (globalTouchPosition: Offset, closeMenu: () -> Unit, hoveredIndex: Int?, setHovered: (Int?) -> Unit) -> Unit
) {
    val density = LocalDensity.current
    val viewConfiguration = LocalViewConfiguration.current
    val haptic = LocalHapticFeedback.current
    val animationScope = rememberCoroutineScope()
    
    var contentMeasuredSize by remember { mutableStateOf(Size.Zero) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isPressed by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var globalTouchPosition by remember { mutableStateOf(Offset.Unspecified) }
    var labelCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    val setHovered: (Int?) -> Unit = { hoveredIndex = it }

    val labelSizePx = with(density) { Size(labelSize.width.dp.toPx(), labelSize.height.dp.toPx()) }

    LaunchedEffect(hoveredIndex) {
        if (isHapticsEnabled && isDragging && hoveredIndex != null) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val closeMenu: () -> Unit = {
        hoveredIndex = null
        animationScope.launch { animatableProgress.animateTo(0f, animationPreset.getSpec(isClosing = true, speedMultiplier = animationSpeedMultiplier)) }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = alignment.composeAlignment) {
        FlippedGlassEffectContainer(
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
            flipDegreesX = flipDegreesX,
            flipDegreesY = flipDegreesY,
            flipCameraDistance = flipCameraDistance,
            flipOvershootMultiplier = flipOvershootMultiplier,
            flipDepthScale = flipDepthScale,
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
                    modifier = Modifier.size(with(density) { labelSizePx.width.toDp() }).onGloballyPositioned { labelCoordinates = it }
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
                    modifier = Modifier.width(IntrinsicSize.Max).onSizeChanged { if (it.width > 0 && it.height > 0) contentMeasuredSize = it.toSize() }.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content(globalTouchPosition, closeMenu, hoveredIndex, setHovered)
                }
            }
        )
    }
}

@Composable
fun FlippedGlassEffectContainer(
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
    flipDegreesX: Float,
    flipDegreesY: Float,
    flipCameraDistance: Float,
    flipOvershootMultiplier: Float,
    flipDepthScale: Float,
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

                val widthProgress = if (p > 1f) 1f + (p - 1f) * containerBulgeMultiplier else (p * p)
                val heightProgress = if (p > 1f) 1f + (p - 1f) * containerBulgeMultiplier else sin(p * (PI / 2f)).toFloat()

                val currentWidthPx = labelSize.width + widthDiff * widthProgress
                val currentHeightPx = labelSize.height + heightDiff * heightProgress

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
            // OUTER LAYER: Translates alignment perfectly without shifting geometry.
            .graphicsLayer {
                val progress = animatableProgress.value
                val blurProgress = if (progress > 0.5f) (1f - progress) / 0.5f else progress / 0.5f
                val squishScale = 1f - (blurProgress.coerceIn(0f, 1f) * 0.05f)

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
                
                // NEW: 3D Depth Shrink Illusion
                val depthProgress = sin(progress.coerceIn(0f, 1f) * PI).toFloat()
                val depthShrink = 1f - (depthProgress * flipDepthScale)

                translationX = lerp(jellyTx, expandedTx, progress) + with(density) { horizontalOffset.dp.toPx() * progress }
                translationY = offsetY + lerp(jellyTy, expandedTy, progress) + with(density) { verticalOffset.dp.toPx() * progress }
                
                scaleX = squishScale * lerp(jellySx, expandedStretchX, progress) * depthShrink
                scaleY = squishScale * lerp(jellySy, expandedStretchY, progress) * depthShrink
                
                this.transformOrigin = alignment.transformOrigin
            }
            // INNER LAYER: Absolute Center 3D Flip (ZERO rotation at resting state)
            .graphicsLayer {
                val progress = animatableProgress.value
                val flipProgress = if (progress > 1f) 1f + (progress - 1f) * flipOvershootMultiplier else progress
                
                rotationX = flipDegreesX * flipProgress
                rotationY = flipDegreesY * flipProgress
                cameraDistance = flipCameraDistance
                this.transformOrigin = TransformOrigin.Center
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(cornerRadius) },
                effects = {
                    val progress = animatableProgress.value
                    if (isGlassEnabled) {
                        val blurProgress = if (progress > 0.5f) (1f - progress) / 0.5f else progress / 0.5f
                        val motionBlur = motionBlurAmount * sin(progress.coerceIn(0f, 1f) * PI).toFloat()
                        vibrancy()
                        blur((2f + blurRadius * blurProgress.coerceIn(0f, 1f) + motionBlur).dp.toPx())
                        lens(refractionHeight.dp.toPx(), refractionAmount.dp.toPx(), depthEffect = true, chromaticAberration = chromaticAberration)
                    }
                },
                highlight = { if (isGlassEnabled) Highlight.Default.copy(alpha = 0.65f) else null },
                shadow = { Shadow(radius = 18.dp, color = Color.Black.copy(alpha = 0.12f)) },
                innerShadow = { if (isGlassEnabled) InnerShadow(radius = 10.dp, color = Color.White.copy(alpha = 0.25f)) else null },
                onDrawSurface = {
                    val progress = animatableProgress.value
                    val cr = cornerRadius.toPx()
                    drawRoundRect(
                        color = if (isLightTheme) Color.White.copy(alpha = 0.28f + 0.12f * progress) else Color(0xFF1E1E1E).copy(alpha = 0.35f + 0.15f * progress),
                        cornerRadius = CornerRadius(cr, cr)
                    )
                    if (progress == 0f && buttonPressProgress > 0f) {
                        drawRoundRect(color = Color.White.copy(alpha = 0.15f * buttonPressProgress), cornerRadius = CornerRadius(cr, cr), blendMode = BlendMode.Plus)
                    }
                }
            )
            .clip(RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center // Center content so flip origin is unified
    ) {
        // BACK FACE: Menu Content
        Box(
            modifier = Modifier
                .wrapContentSize(unbounded = true, align = Alignment.Center) 
                .graphicsLayer {
                    val progress = animatableProgress.value
                    
                    // Fade in perfectly AFTER the card passes the edge-on point
                    val contentOpacity = ((progress - 0.5f) / 0.5f).coerceIn(0f, 1f)
                    val contentScaleProgress = if (progress > 1f) 1f + (progress - 1f) * contentBulgeMultiplier else contentOpacity
                    
                    val minAspectScale = if (contentSize.width > 0f && contentSize.height > 0f) { min(labelSize.width / contentSize.width, labelSize.height / contentSize.height) } else 1f
                    val baseScale = minAspectScale + (1f - minAspectScale) * contentScaleProgress
                    val pop = sin(progress.coerceIn(0f, 1f) * PI).toFloat()
                    
                    alpha = contentOpacity
                    scaleX = baseScale + pop * contentPopX
                    scaleY = baseScale + pop * contentPopY
                    
                    // Pre-counter-rotate the back face so it's readable when the card flips over
                    rotationX = -flipDegreesX
                    rotationY = -flipDegreesY
                    this.transformOrigin = TransformOrigin.Center
                }
        ) {
            content()
        }

        // FRONT FACE: FAB Label
        Box(
            modifier = Modifier.graphicsLayer { 
                val progress = animatableProgress.value
                
                // Fade out exactly as the card reaches edge-on
                val labelOpacity = (progress / 0.5f).coerceIn(0f, 1f)
                alpha = 1f - labelOpacity 
                
                // Zero rotation required: it lives natively on the front face
                this.transformOrigin = TransformOrigin.Center
            }
        ) {
            label()
        }
    }
}

@Composable
fun FlippedMenuRow(
    icon: ImageVector,
    title: String,
    description: String,
    contentColor: Color,
    secondaryColor: Color,
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
        modifier = Modifier.fillMaxWidth().onGloballyPositioned { rowCoords = it }.clip(RoundedCornerShape(14.dp)).clickable { onClick() }.background(contentColor.copy(alpha = hoverAlpha)).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(modifier = Modifier.size(24.dp).paint(rememberVectorPainter(icon), colorFilter = ColorFilter.tint(contentColor)))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(title, style = TextStyle(contentColor, 15.sp, FontWeight.SemiBold))
            BasicText(description, style = TextStyle(secondaryColor, 12.sp), maxLines = 1)
        }
    }
}
