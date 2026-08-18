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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.catalog.BackdropDemoScaffold
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
import kotlin.math.abs
import kotlin.math.min

enum class MenuAlignment(val label: String) {
    TopLeading("T-Left"),
    TopTrailing("T-Right"),
    BottomLeading("B-Left"),
    BottomTrailing("B-Right");

    val composeAlignment: Alignment
        get() = when (this) {
            TopLeading -> Alignment.TopStart
            TopTrailing -> Alignment.TopEnd
            BottomLeading -> Alignment.BottomStart
            BottomTrailing -> Alignment.BottomEnd
        }

    val transformOrigin: TransformOrigin
        get() = when (this) {
            TopLeading -> TransformOrigin(0f, 0f)
            TopTrailing -> TransformOrigin(1f, 0f)
            BottomLeading -> TransformOrigin(0f, 1f)
            BottomTrailing -> TransformOrigin(1f, 1f)
        }

    fun calculateOffsetY(blurProgress: Float, maxOffsetPx: Float): Float {
        return when (this) {
            TopLeading, TopTrailing -> maxOffsetPx * blurProgress
            BottomLeading, BottomTrailing -> -maxOffsetPx * blurProgress
        }
    }
}

enum class MenuAnimationPreset(val label: String) {
    Bouncy("Bouncy"),
    Smooth("Smooth"),
    Snappy("Snappy");

    fun getSpec(isClosing: Boolean = false): AnimationSpec<Float> = when (this) {
        Bouncy -> if (isClosing) spring(dampingRatio = 0.8f, stiffness = 300f) else spring(dampingRatio = 0.65f, stiffness = 250f)
        Smooth -> tween(durationMillis = 650, easing = FastOutSlowInEasing)
        Snappy -> spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
    }
}

@Composable
fun ExpandableGlassMenuContent() {
    val isLightTheme = !isSystemInDarkTheme()
    val contentColor = if (isLightTheme) Color.Black else Color.White
    val secondaryColor = if (isLightTheme) Color.Gray else Color(0xFFAAAAAA)
    val cardBackground = if (isLightTheme) Color.White.copy(alpha = 0.5f) else Color(0xFF1E1E1E).copy(alpha = 0.5f)

    var selectedAlignment by remember { mutableStateOf(MenuAlignment.TopLeading) }
    var selectedPreset by remember { mutableStateOf(MenuAnimationPreset.Bouncy) }

    // Glass properties state
    var isGlassEnabled by remember { mutableStateOf(true) }
    var cornerRadiusDp by remember { mutableFloatStateOf(30f) }
    var blurRadiusDp by remember { mutableFloatStateOf(12f) }
    var refractionHeightDp by remember { mutableFloatStateOf(16f) }
    var refractionAmountDp by remember { mutableFloatStateOf(24f) }
    var chromaticAberration by remember { mutableStateOf(false) }

    val animationScope = rememberCoroutineScope()
    val animatableProgress = remember { Animatable(0f) }

    BackdropDemoScaffold { backdrop ->
        val controlsBackdrop = rememberLayerBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16f.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16f.dp)
        ) {
            BasicText(
                "Expandable Glass Menu",
                Modifier.padding(top = 16f.dp, bottom = 4f.dp),
                style = TextStyle(contentColor, 26f.sp, FontWeight.SemiBold)
            )

            BasicText("Preview", style = TextStyle(Color(0xFF0088FF), 15f.sp, FontWeight.Medium))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340f.dp)
                    .clip(RoundedCornerShape(20f.dp))
                    .background(Color.Black.copy(alpha = 0.08f))
                    .pointerInput(selectedPreset) {
                        detectTapGestures {
                            if (animatableProgress.value > 0.1f) {
                                animationScope.launch {
                                    animatableProgress.animateTo(0f, selectedPreset.getSpec(isClosing = true))
                                }
                            }
                        }
                    }
            ) {
                ExpandableGlassMenu(
                    animatableProgress = animatableProgress,
                    animationPreset = selectedPreset,
                    alignment = selectedAlignment,
                    backdrop = backdrop,
                    isGlassEnabled = isGlassEnabled,
                    cornerRadius = cornerRadiusDp.dp,
                    blurRadius = blurRadiusDp,
                    refractionHeight = refractionHeightDp,
                    refractionAmount = refractionAmountDp,
                    chromaticAberration = chromaticAberration,
                    modifier = Modifier.padding(16f.dp),
                    label = {
                        Box(
                            modifier = Modifier.size(55f.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24f.dp)
                                    .paint(
                                        rememberVectorPainter(Icons.Share),
                                        colorFilter = ColorFilter.tint(contentColor)
                                    )
                            )
                        }
                    }
                ) { globalTouch, closeMenu, hoveredIndex, setHovered ->
                    MenuRow(
                        icon = Icons.Send,
                        title = "Send",
                        description = "This is a sample text description",
                        contentColor = contentColor,
                        secondaryColor = secondaryColor,
                        isHovered = hoveredIndex == 0,
                        onHoverChange = { if (it) setHovered(0) else if (hoveredIndex == 0) setHovered(null) },
                        globalTouchPosition = globalTouch,
                        onClick = closeMenu
                    )
                    MenuRow(
                        icon = Icons.Swap,
                        title = "Swap",
                        description = "This is a sample text description",
                        contentColor = contentColor,
                        secondaryColor = secondaryColor,
                        isHovered = hoveredIndex == 1,
                        onHoverChange = { if (it) setHovered(1) else if (hoveredIndex == 1) setHovered(null) },
                        globalTouchPosition = globalTouch,
                        onClick = closeMenu
                    )
                    MenuRow(
                        icon = Icons.Receive,
                        title = "Receive",
                        description = "This is a sample text description",
                        contentColor = contentColor,
                        secondaryColor = secondaryColor,
                        isHovered = hoveredIndex == 2,
                        onHoverChange = { if (it) setHovered(2) else if (hoveredIndex == 2) setHovered(null) },
                        globalTouchPosition = globalTouch,
                        onClick = closeMenu
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(24f.dp) },
                        effects = {
                            vibrancy()
                            blur(8f.dp.toPx())
                            lens(16f.dp.toPx(), 32f.dp.toPx())
                        },
                        highlight = { Highlight.Plain },
                        exportedBackdrop = controlsBackdrop,
                        onDrawSurface = { drawRect(cardBackground) }
                    )
                    .padding(20f.dp),
                verticalArrangement = Arrangement.spacedBy(16f.dp)
            ) {
                BasicText("Properties", style = TextStyle(contentColor, 18f.sp, FontWeight.SemiBold))

                Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Progress", style = TextStyle(contentColor, 14f.sp))
                        BasicText("${(animatableProgress.value * 100).toInt()}%", style = TextStyle(secondaryColor, 14f.sp))
                    }
                    LiquidSlider(
                        value = { animatableProgress.value },
                        onValueChange = {
                            animationScope.launch { animatableProgress.snapTo(it) }
                        },
                        valueRange = 0f..1f,
                        visibilityThreshold = 0.001f,
                        backdrop = controlsBackdrop
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                    BasicText("Alignment", style = TextStyle(contentColor, 14f.sp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8f.dp)) {
                        MenuAlignment.entries.forEach { align ->
                            val isSelected = selectedAlignment == align
                            LiquidButton(
                                onClick = { selectedAlignment = align },
                                backdrop = controlsBackdrop,
                                modifier = Modifier.weight(1f).height(40f.dp),
                                tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified,
                                surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.15f)
                            ) {
                                BasicText(
                                    align.label,
                                    style = TextStyle(if (isSelected) Color.White else contentColor, 12f.sp, FontWeight.Medium)
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                    BasicText("Animation Trigger", style = TextStyle(contentColor, 14f.sp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8f.dp)) {
                        MenuAnimationPreset.entries.forEach { preset ->
                            val isSelected = selectedPreset == preset
                            LiquidButton(
                                onClick = {
                                    selectedPreset = preset
                                    val target = if (animatableProgress.value > 0.5f) 0f else 1f
                                    animationScope.launch {
                                        animatableProgress.animateTo(target, preset.getSpec(isClosing = target == 0f))
                                    }
                                },
                                backdrop = controlsBackdrop,
                                modifier = Modifier.weight(1f).height(42f.dp),
                                tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified,
                                surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.15f)
                            ) {
                                BasicText(
                                    preset.label,
                                    style = TextStyle(if (isSelected) Color.White else contentColor, 13f.sp, FontWeight.Medium)
                                )
                            }
                        }
                    }
                }

                // Glass Effects Section
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    BasicText("Glass Effect", style = TextStyle(contentColor, 14f.sp))
                    LiquidToggle(
                        selected = { isGlassEnabled },
                        onSelect = { isGlassEnabled = it },
                        backdrop = controlsBackdrop
                    )
                }

                if (isGlassEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Corner Radius", style = TextStyle(contentColor, 14f.sp))
                            BasicText("${cornerRadiusDp.toInt()} dp", style = TextStyle(secondaryColor, 14f.sp))
                        }
                        LiquidSlider(
                            value = { cornerRadiusDp },
                            onValueChange = { cornerRadiusDp = it },
                            valueRange = 0f..64f,
                            visibilityThreshold = 0.1f,
                            backdrop = controlsBackdrop
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Blur Radius", style = TextStyle(contentColor, 14f.sp))
                            BasicText("${blurRadiusDp.toInt()} dp", style = TextStyle(secondaryColor, 14f.sp))
                        }
                        LiquidSlider(
                            value = { blurRadiusDp },
                            onValueChange = { blurRadiusDp = it },
                            valueRange = 0f..32f,
                            visibilityThreshold = 0.1f,
                            backdrop = controlsBackdrop
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Refraction Height", style = TextStyle(contentColor, 14f.sp))
                            BasicText("${refractionHeightDp.toInt()} dp", style = TextStyle(secondaryColor, 14f.sp))
                        }
                        LiquidSlider(
                            value = { refractionHeightDp },
                            onValueChange = { refractionHeightDp = it },
                            valueRange = 0f..48f,
                            visibilityThreshold = 0.1f,
                            backdrop = controlsBackdrop
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Refraction Amount", style = TextStyle(contentColor, 14f.sp))
                            BasicText("${refractionAmountDp.toInt()} dp", style = TextStyle(secondaryColor, 14f.sp))
                        }
                        LiquidSlider(
                            value = { refractionAmountDp },
                            onValueChange = { refractionAmountDp = it },
                            valueRange = 0f..64f,
                            visibilityThreshold = 0.1f,
                            backdrop = controlsBackdrop
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BasicText("Chromatic Aberration", style = TextStyle(contentColor, 14f.sp))
                        LiquidToggle(
                            selected = { chromaticAberration },
                            onSelect = { chromaticAberration = it },
                            backdrop = controlsBackdrop
                        )
                    }
                }
            }
            Spacer(Modifier.height(16f.dp))
        }
    }
}

@Composable
fun ExpandableGlassMenu(
    animatableProgress: Animatable<Float, *>,
    animationPreset: MenuAnimationPreset,
    alignment: MenuAlignment,
    backdrop: Backdrop,
    isGlassEnabled: Boolean,
    cornerRadius: Dp,
    blurRadius: Float,
    refractionHeight: Float,
    refractionAmount: Float,
    chromaticAberration: Boolean,
    modifier: Modifier = Modifier,
    labelSize: Size = Size(55f, 55f),
    label: @Composable () -> Unit,
    content: @Composable (globalTouchPosition: Offset, closeMenu: () -> Unit, hoveredIndex: Int?, setHovered: (Int?) -> Unit) -> Unit
) {
    val density = LocalDensity.current
    val viewConfiguration = LocalViewConfiguration.current
    val animationScope = rememberCoroutineScope()
    
    var contentMeasuredSize by remember { mutableStateOf(Size.Zero) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isPressed by remember { mutableStateOf(false) }
    var globalTouchPosition by remember { mutableStateOf(Offset.Unspecified) }
    var labelCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    val setHovered: (Int?) -> Unit = { hoveredIndex = it }

    val labelSizePx = with(density) { Size(labelSize.width.dp.toPx(), labelSize.height.dp.toPx()) }

    val closeMenu: () -> Unit = {
        hoveredIndex = null
        animationScope.launch {
            animatableProgress.animateTo(0f, animationPreset.getSpec(isClosing = true))
        }
    }

    LaunchedEffect(animatableProgress.value) {
        if (animatableProgress.value == 0f) hoveredIndex = null
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = alignment.composeAlignment
    ) {
        GlassEffectContainer(
            progress = animatableProgress.value,
            dragOffset = dragOffset,
            isPressed = isPressed,
            alignment = alignment,
            backdrop = backdrop,
            isGlassEnabled = isGlassEnabled,
            cornerRadius = cornerRadius,
            blurRadius = blurRadius,
            refractionHeight = refractionHeight,
            refractionAmount = refractionAmount,
            chromaticAberration = chromaticAberration,
            labelSize = labelSizePx,
            contentSize = contentMeasuredSize,
            label = {
                Box(
                    modifier = Modifier
                        .size(with(density) { labelSizePx.width.toDp() })
                        .onGloballyPositioned { labelCoordinates = it }
                        .pointerInput(animationPreset) {
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
                                    animationScope.launch {
                                        animatableProgress.animateTo(1f, animationPreset.getSpec(isClosing = false))
                                    }
                                }

                                var tracking = upEvent == null
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

                                if (upEvent != null && !isSimpleDrag && !isLongPress && timeoutResult != null) {
                                    upEvent?.consume()
                                    val target = if (isExpanded) 0f else 1f
                                    animationScope.launch {
                                        animatableProgress.animateTo(target, animationPreset.getSpec(isClosing = target == 0f))
                                    }
                                } else if ((isSimpleDrag || isLongPress) && isExpanded) {
                                    if (dragOffset.getDistance() > 20f || hoveredIndex != null) {
                                        closeMenu()
                                    }
                                }
                                
                                dragOffset = Offset.Zero
                                globalTouchPosition = Offset.Unspecified
                                hoveredIndex = null
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    label()
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .onSizeChanged {
                            if (it.width > 0 && it.height > 0) contentMeasuredSize = it.toSize()
                        }
                        .padding(10f.dp),
                    verticalArrangement = Arrangement.spacedBy(12f.dp)
                ) {
                    content(globalTouchPosition, closeMenu, hoveredIndex, setHovered)
                }
            }
        )
    }
}

@Composable
fun GlassEffectContainer(
    progress: Float,
    dragOffset: Offset,
    isPressed: Boolean,
    alignment: MenuAlignment,
    backdrop: Backdrop,
    isGlassEnabled: Boolean,
    cornerRadius: Dp,
    blurRadius: Float,
    refractionHeight: Float,
    refractionAmount: Float,
    chromaticAberration: Boolean,
    labelSize: Size,
    contentSize: Size,
    label: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val isLightTheme = !isSystemInDarkTheme()

    val widthDiff = (contentSize.width - labelSize.width).coerceAtLeast(0f)
    val heightDiff = (contentSize.height - labelSize.height).coerceAtLeast(0f)

    val currentWidthPx = labelSize.width + widthDiff * progress
    val currentHeightPx = labelSize.height + heightDiff * progress

    val labelOpacity = (progress / 0.35f).coerceIn(0f, 1f)
    val contentProgress = ((progress - 0.35f) / 0.65f).coerceIn(0f, 1f)

    val minAspectScale = if (contentSize.width > 0f && contentSize.height > 0f) {
        min(labelSize.width / contentSize.width, labelSize.height / contentSize.height)
    } else 1f
    
    val contentScale = minAspectScale + (1f - minAspectScale) * ((progress - 0.35f) / 0.65f).coerceAtLeast(0f)

    val blurProgress = if (progress > 0.5f) (1f - progress) / 0.5f else progress / 0.5f
    val squishScale = 1f - (blurProgress.coerceIn(0f, 1f) * 0.05f)

    val maxOffsetPx = with(density) { 75f.dp.toPx() }
    val offsetY = alignment.calculateOffsetY(blurProgress, maxOffsetPx)
    val transformOrigin = alignment.transformOrigin

    val animatedDragX by animateFloatAsState(dragOffset.x, spring(stiffness = 400f, dampingRatio = 0.6f))
    val animatedDragY by animateFloatAsState(dragOffset.y, spring(stiffness = 400f, dampingRatio = 0.6f))

    // 1. Collapsed Physics (LiquidButton native jelly pull)
    val jellyMaxOffset = with(density) { 55f.dp.toPx() }
    val initialDerivative = 0.05f
    val jellyTx = jellyMaxOffset * kotlin.math.tanh(initialDerivative * animatedDragX / jellyMaxOffset)
    val jellyTy = jellyMaxOffset * kotlin.math.tanh(initialDerivative * animatedDragY / jellyMaxOffset)
    
    val jellyDragScale = with(density) { 4f.dp.toPx() / 55f.dp.toPx() }
    val jellyAngle = kotlin.math.atan2(animatedDragY, animatedDragX)
    val jellySx = 1f + jellyDragScale * abs(kotlin.math.cos(jellyAngle) * animatedDragX / jellyMaxOffset)
    val jellySy = 1f + jellyDragScale * abs(kotlin.math.sin(jellyAngle) * animatedDragY / jellyMaxOffset)

    // 2. Expanded Physics (Menu tight pull resistance)
    val stretchTx = -animatedDragX * 0.05f
    val stretchTy = -animatedDragY * 0.05f
    val stretchSx = (1f + abs(animatedDragX) * 0.00005f - abs(animatedDragY) * 0.00005f).coerceIn(0.99f, 1.01f)
    val stretchSy = (1f + abs(animatedDragY) * 0.00005f - abs(animatedDragX) * 0.00005f).coerceIn(0.99f, 1.01f)

    // Blend physics perfectly
    val tx = jellyTx * (1f - progress) + stretchTx * progress
    val ty = offsetY + jellyTy * (1f - progress) + stretchTy * progress
    val sx = squishScale * (jellySx * (1f - progress) + stretchSx * progress)
    val sy = squishScale * (jellySy * (1f - progress) + stretchSy * progress)

    // Simple touch tap scale-down interaction
    val isButtonActivelyPressed = isPressed || dragOffset != Offset.Zero || progress > 0f
    val pressScale by animateFloatAsState(if (isButtonActivelyPressed && progress == 0f) 0.95f else 1f, spring(0.5f, 300f))

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = tx
                translationY = ty
                scaleX = sx * pressScale
                scaleY = sy * pressScale
                this.transformOrigin = transformOrigin
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(cornerRadius) },
                effects = {
                    if (isGlassEnabled) {
                        // All sliders strictly control the visual pipeline dynamically
                        vibrancy()
                        blur(blurRadius.dp.toPx())
                        lens(
                            refractionHeight = refractionHeight.dp.toPx(),
                            refractionAmount = refractionAmount.dp.toPx(),
                            depthEffect = true,
                            chromaticAberration = chromaticAberration
                        )
                    }
                },
                highlight = { if (isGlassEnabled) Highlight.Default.copy(alpha = 0.65f) else null },
                shadow = { Shadow(radius = 18f.dp, color = Color.Black.copy(alpha = 0.12f)) },
                innerShadow = { if (isGlassEnabled) InnerShadow(radius = 10f.dp, color = Color.White.copy(alpha = 0.25f)) else null },
                onDrawSurface = {
                    val baseAlpha = if (isLightTheme) 0.28f else 0.35f
                    val progressBoost = 0.12f * progress
                    drawRect(Color.White.copy(alpha = baseAlpha + progressBoost))
                    
                    // Natively replicates LiquidButton highlight touch-glow illumination
                    if (isButtonActivelyPressed && progress == 0f) {
                        drawRect(Color.White.copy(alpha = 0.15f))
                    }
                }
            )
            .clip(RoundedCornerShape(cornerRadius))
            .size(
                width = with(density) { currentWidthPx.toDp() },
                height = with(density) { currentHeightPx.toDp() }
            ),
        contentAlignment = alignment.composeAlignment
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize(unbounded = true, align = alignment.composeAlignment) 
                .graphicsLayer {
                    alpha = contentProgress
                    scaleX = contentScale
                    scaleY = contentScale
                    this.transformOrigin = transformOrigin
                }
        ) {
            content()
        }

        Box(
            modifier = Modifier.graphicsLayer { alpha = 1f - labelOpacity }
        ) {
            label()
        }
    }
}

@Composable
fun MenuRow(
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
    
    LaunchedEffect(contains) {
        onHoverChange(contains)
    }

    val hoverAlpha by animateFloatAsState(if (isHovered) 0.1f else 0f, tween(150))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { rowCoords = it }
            .clip(RoundedCornerShape(14f.dp))
            .clickable { onClick() }
            .background(contentColor.copy(alpha = hoverAlpha))
            .padding(horizontal = 10f.dp, vertical = 6f.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14f.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24f.dp)
                .paint(
                    rememberVectorPainter(icon),
                    colorFilter = ColorFilter.tint(contentColor)
                )
        )

        Column(verticalArrangement = Arrangement.spacedBy(2f.dp)) {
            BasicText(title, style = TextStyle(contentColor, 15f.sp, FontWeight.SemiBold))
            BasicText(description, style = TextStyle(secondaryColor, 12f.sp), maxLines = 1)
        }
    }
}

private object Icons {
    val Share: ImageVector
        get() = ImageVector.Builder(
            name = "Share",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 16.08f)
                curveToRelative(-0.76f, 0f, -1.44f, 0.3f, -1.96f, 0.77f)
                lineTo(8.91f, 12.7f)
                curveToRelative(0.05f, -0.23f, 0.09f, -0.46f, 0.09f, -0.7f)
                reflectiveCurveToRelative(-0.04f, -0.47f, -0.09f, -0.7f)
                lineTo(15.96f, 7.19f)
                curveToRelative(0.54f, 0.5f, 1.25f, 0.81f, 2.04f, 0.81f)
                curveToRelative(1.66f, 0f, 3f,-1.34f, 3f,-3f)
                reflectiveCurveToRelative(-1.34f,-3f,-3f,-3f)
                reflectiveCurveToRelative(-3f,1.34f,-3f,3f)
                curveToRelative(0f,0.24f,0.04f,0.47f,0.09f,0.7f)
                lineTo(8.04f, 9.81f)
                curveTo(7.5f, 9.31f, 6.79f, 9f, 6f, 9f)
                curveToRelative(-1.66f,0f,-3f,1.34f,-3f,3f)
                reflectiveCurveToRelative(1.34f,3f,3f,3f)
                curveToRelative(0.79f,0f,1.5f,-0.31f,2.04f,-0.81f)
                lineToRelative(7.12f,4.16f)
                curveToRelative(-0.05f,0.21f,-0.08f,0.43f,-0.08f,0.65f)
                curveToRelative(0f,1.61f,1.31f,2.92f,2.92f,2.92f)
                reflectiveCurveToRelative(2.92f,-1.31f,2.92f,-2.92f)
                curveToRelative(0f,-1.61f,-1.31f,-2.92f,-2.92f,-2.92f)
                close()
            }
        }.build()

    val Send: ImageVector
        get() = ImageVector.Builder(
            name = "Send",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(2.01f, 21f)
                lineTo(23f, 12f)
                lineTo(2.01f, 3f)
                lineTo(2f, 10f)
                lineToRelative(15f, 2f)
                lineToRelative(-15f, 2f)
                close()
            }
        }.build()

    val Swap: ImageVector
        get() = ImageVector.Builder(
            name = "Swap",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6.99f, 11f)
                lineTo(3f, 15f)
                lineToRelative(3.99f, 4f)
                verticalLineToRelative(-3f)
                horizontalLineTo(14f)
                verticalLineToRelative(-2f)
                horizontalLineTo(6.99f)
                verticalLineToRelative(-3f)
                close()
                moveTo(21f, 9f)
                lineToRelative(-3.99f, -4f)
                verticalLineToRelative(3f)
                horizontalLineTo(10f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(7.01f)
                verticalLineToRelative(3f)
                lineTo(21f, 9f)
                close()
            }
        }.build()

    val Receive: ImageVector
        get() = ImageVector.Builder(
            name = "Receive",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 12f)
                lineToRelative(-1.41f, -1.41f)
                lineTo(13f, 16.17f)
                verticalLineTo(4f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(12.17f)
                lineToRelative(-5.58f, -5.59f)
                lineTo(4f, 12f)
                lineToRelative(8f, 8f)
                lineToRelative(8f, -8f)
                close()
            }
        }.build()
}
