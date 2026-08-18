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
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
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

    var selectedAlignment by remember { mutableStateOf(MenuAlignment.TopLeading) }
    var selectedPreset by remember { mutableStateOf(MenuAnimationPreset.Bouncy) }

    var isGlassEnabled by remember { mutableStateOf(true) }
    var cornerRadiusDp by remember { mutableFloatStateOf(30f) }
    var blurRadiusDp by remember { mutableFloatStateOf(10f) }
    var refractionHeightDp by remember { mutableFloatStateOf(16f) }
    var refractionAmountDp by remember { mutableFloatStateOf(20f) }
    var chromaticAberration by remember { mutableStateOf(false) }
    
    var horizontalOffsetDp by remember { mutableFloatStateOf(0f) }
    var verticalOffsetDp by remember { mutableFloatStateOf(0f) }

    val animationScope = rememberCoroutineScope()
    val animatableProgress = remember { Animatable(0f) }

    BackdropDemoScaffold { backdrop ->
        val controlsBackdrop = rememberLayerBackdrop()

        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 16f.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16f.dp)
        ) {
            BasicText("Flipped Morph Dropdown", Modifier.padding(top = 16f.dp, bottom = 4f.dp), style = TextStyle(contentColor, 26f.sp, FontWeight.SemiBold))
            BasicText("Preview", style = TextStyle(Color(0xFF0088FF), 15f.sp, FontWeight.Medium))

            Box(
                modifier = Modifier.fillMaxWidth().height(340f.dp).clip(RoundedCornerShape(20f.dp)).background(Color.Black.copy(alpha = 0.08f))
                    .pointerInput(selectedPreset) {
                        detectTapGestures {
                            if (animatableProgress.value > 0.1f) {
                                animationScope.launch { animatableProgress.animateTo(0f, selectedPreset.getSpec(isClosing = true)) }
                            }
                        }
                    }
            ) {
                FlippedExpandableGlassMenu(
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
                    horizontalOffset = horizontalOffsetDp,
                    verticalOffset = verticalOffsetDp,
                    modifier = Modifier.padding(16f.dp),
                    label = {
                        Box(modifier = Modifier.size(55f.dp), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(24f.dp).paint(rememberVectorPainter(ShareFilledIcon), colorFilter = ColorFilter.tint(contentColor)))
                        }
                    }
                ) { globalTouch, closeMenu, hoveredIndex, setHovered ->
                    MenuRow(SendIcon, "Send", "This is a sample text description", contentColor, secondaryColor, hoveredIndex == 0, { if (it) setHovered(0) }, globalTouchPosition = globalTouch, onClick = closeMenu)
                    MenuRow(SwapIcon, "Swap", "This is a sample text description", contentColor, secondaryColor, hoveredIndex == 1, { if (it) setHovered(1) }, globalTouchPosition = globalTouch, onClick = closeMenu)
                    MenuRow(ReceiveIcon, "Receive", "This is a sample text description", contentColor, secondaryColor, hoveredIndex == 2, { if (it) setHovered(2) }, globalTouchPosition = globalTouch, onClick = closeMenu)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().drawBackdrop(backdrop = backdrop, shape = { RoundedCornerShape(24f.dp) }, effects = { vibrancy(); blur(8f.dp.toPx()); lens(16f.dp.toPx(), 32f.dp.toPx()) }, highlight = { Highlight.Plain }, exportedBackdrop = controlsBackdrop, onDrawSurface = { drawRect(cardBackground) }).padding(20f.dp),
                verticalArrangement = Arrangement.spacedBy(16f.dp)
            ) {
                BasicText("Properties", style = TextStyle(contentColor, 18f.sp, FontWeight.SemiBold))

                Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Progress", style = TextStyle(contentColor, 14f.sp)); BasicText("${(animatableProgress.value * 100).toInt()}%", style = TextStyle(secondaryColor, 14f.sp))
                    }
                    LiquidSlider(value = { animatableProgress.value }, onValueChange = { animationScope.launch { animatableProgress.snapTo(it) } }, valueRange = 0f..1f, visibilityThreshold = 0.001f, backdrop = controlsBackdrop)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                    BasicText("Alignment", style = TextStyle(contentColor, 14f.sp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8f.dp)) {
                        MenuAlignment.entries.forEach { align ->
                            val isSelected = selectedAlignment == align
                            LiquidButton(onClick = { selectedAlignment = align }, backdrop = controlsBackdrop, modifier = Modifier.weight(1f).height(40f.dp), tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified, surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.15f)) {
                                BasicText(align.label, style = TextStyle(if (isSelected) Color.White else contentColor, 12f.sp, FontWeight.Medium))
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                    BasicText("Animation Trigger", style = TextStyle(contentColor, 14f.sp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8f.dp)) {
                        MenuAnimationPreset.entries.forEach { preset ->
                            val isSelected = selectedPreset == preset
                            LiquidButton(onClick = { selectedPreset = preset; val target = if (animatableProgress.value > 0.5f) 0f else 1f; animationScope.launch { animatableProgress.animateTo(target, preset.getSpec(isClosing = target == 0f)) } }, backdrop = controlsBackdrop, modifier = Modifier.weight(1f).height(42f.dp), tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified, surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.15f)) {
                                BasicText(preset.label, style = TextStyle(if (isSelected) Color.White else contentColor, 13f.sp, FontWeight.Medium))
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    BasicText("Glass Effect", style = TextStyle(contentColor, 14f.sp))
                    LiquidToggle(selected = { isGlassEnabled }, onSelect = { isGlassEnabled = it }, backdrop = controlsBackdrop)
                }

                if (isGlassEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { BasicText("Corner Radius", style = TextStyle(contentColor, 14f.sp)); BasicText("${cornerRadiusDp.toInt()} dp", style = TextStyle(secondaryColor, 14f.sp)) }
                        LiquidSlider(value = { cornerRadiusDp }, onValueChange = { cornerRadiusDp = it }, valueRange = 0f..64f, visibilityThreshold = 0.1f, backdrop = controlsBackdrop)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { BasicText("Blur Radius", style = TextStyle(contentColor, 14f.sp)); BasicText("${blurRadiusDp.toInt()} dp", style = TextStyle(secondaryColor, 14f.sp)) }
                        LiquidSlider(value = { blurRadiusDp }, onValueChange = { blurRadiusDp = it }, valueRange = 0f..32f, visibilityThreshold = 0.1f, backdrop = controlsBackdrop)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { BasicText("Refraction Height", style = TextStyle(contentColor, 14f.sp)); BasicText("${refractionHeightDp.toInt()} dp", style = TextStyle(secondaryColor, 14f.sp)) }
                        LiquidSlider(value = { refractionHeightDp }, onValueChange = { refractionHeightDp = it }, valueRange = 0f..48f, visibilityThreshold = 0.1f, backdrop = controlsBackdrop)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { BasicText("Refraction Amount", style = TextStyle(contentColor, 14f.sp)); BasicText("${refractionAmountDp.toInt()} dp", style = TextStyle(secondaryColor, 14f.sp)) }
                        LiquidSlider(value = { refractionAmountDp }, onValueChange = { refractionAmountDp = it }, valueRange = 0f..64f, visibilityThreshold = 0.1f, backdrop = controlsBackdrop)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BasicText("Chromatic Aberration", style = TextStyle(contentColor, 14f.sp))
                        LiquidToggle(selected = { chromaticAberration }, onSelect = { chromaticAberration = it }, backdrop = controlsBackdrop)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { BasicText("Horizontal Offset", style = TextStyle(contentColor, 14f.sp)); BasicText("${horizontalOffsetDp.toInt()} dp", style = TextStyle(secondaryColor, 14f.sp)) }
                        LiquidSlider(value = { horizontalOffsetDp }, onValueChange = { horizontalOffsetDp = it }, valueRange = -150f..150f, visibilityThreshold = 1f, backdrop = controlsBackdrop)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { BasicText("Vertical Offset", style = TextStyle(contentColor, 14f.sp)); BasicText("${verticalOffsetDp.toInt()} dp", style = TextStyle(secondaryColor, 14f.sp)) }
                        LiquidSlider(value = { verticalOffsetDp }, onValueChange = { verticalOffsetDp = it }, valueRange = -150f..150f, visibilityThreshold = 1f, backdrop = controlsBackdrop)
                    }
                }
            }
            Spacer(Modifier.height(16f.dp))
        }
    }
}

@Composable
fun FlippedExpandableGlassMenu(
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
    horizontalOffset: Float,
    verticalOffset: Float,
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
        animationScope.launch { animatableProgress.animateTo(0f, animationPreset.getSpec(isClosing = true)) }
    }

    LaunchedEffect(animatableProgress.value) {
        if (animatableProgress.value == 0f) hoveredIndex = null
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = alignment.composeAlignment) {
        FlippedGlassEffectContainer(
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
            horizontalOffset = horizontalOffset,
            verticalOffset = verticalOffset,
            labelSize = labelSizePx,
            contentSize = contentMeasuredSize,
            label = {
                Box(
                    modifier = Modifier.size(with(density) { labelSizePx.width.toDp() }).onGloballyPositioned { labelCoordinates = it }
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
                                    animationScope.launch { animatableProgress.animateTo(1f, animationPreset.getSpec(isClosing = false)) }
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
                                    animationScope.launch { animatableProgress.animateTo(target, animationPreset.getSpec(isClosing = target == 0f)) }
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
                ) { label() }
            },
            content = {
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max).onSizeChanged { if (it.width > 0 && it.height > 0) contentMeasuredSize = it.toSize() }.padding(10f.dp),
                    verticalArrangement = Arrangement.spacedBy(12f.dp)
                ) {
                    content(globalTouchPosition, closeMenu, hoveredIndex, setHovered)
                }
            }
        )
    }
}

@Composable
fun FlippedGlassEffectContainer(
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
    horizontalOffset: Float,
    verticalOffset: Float,
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

    val minAspectScale = if (contentSize.width > 0f && contentSize.height > 0f) { min(labelSize.width / contentSize.width, labelSize.height / contentSize.height) } else 1f
    val contentScale = minAspectScale + (1f - minAspectScale) * ((progress - 0.35f) / 0.65f).coerceAtLeast(0f)

    val blurProgress = if (progress > 0.5f) (1f - progress) / 0.5f else progress / 0.5f
    val squishScale = 1f - (blurProgress.coerceIn(0f, 1f) * 0.05f)

    val maxOffsetPx = with(density) { 75f.dp.toPx() }
    val offsetY = alignment.calculateOffsetY(blurProgress, maxOffsetPx)
    val transformOrigin = alignment.transformOrigin

    val animatedDragX by animateFloatAsState(dragOffset.x, spring(stiffness = 400f, dampingRatio = 0.6f))
    val animatedDragY by animateFloatAsState(dragOffset.y, spring(stiffness = 400f, dampingRatio = 0.6f))
    val buttonPressProgress by animateFloatAsState(if (isPressed && progress == 0f) 1f else 0f, spring(dampingRatio = 0.6f, stiffness = 400f))

    Box(
        modifier = Modifier
            .graphicsLayer {
                val w = size.width
                val h = size.height
                val minDim = min(w, h)
                val maxDim = max(w, h)

                val maxJellyOffset = minDim
                val initialDerivative = 0.05f
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
                rotationX = 180f * progress
                this.transformOrigin = transformOrigin
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(cornerRadius) },
                effects = {
                    if (isGlassEnabled) {
                        vibrancy()
                        blur((2f + blurRadius * blurProgress.coerceIn(0f, 1f)).dp.toPx())
                        lens(refractionHeight.dp.toPx(), refractionAmount.dp.toPx(), depthEffect = true, chromaticAberration = chromaticAberration)
                    }
                },
                highlight = { if (isGlassEnabled) Highlight.Default.copy(alpha = 0.65f) else null },
                shadow = { Shadow(radius = 18f.dp, color = Color.Black.copy(alpha = 0.12f)) },
                innerShadow = { if (isGlassEnabled) InnerShadow(radius = 10f.dp, color = Color.White.copy(alpha = 0.25f)) else null },
                onDrawSurface = {
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
            .clip(RoundedCornerShape(cornerRadius))
            .size(width = with(density) { currentWidthPx.toDp() }, height = with(density) { currentHeightPx.toDp() }),
        contentAlignment = alignment.composeAlignment
    ) {
        Box(modifier = Modifier.wrapContentSize(unbounded = true, align = alignment.composeAlignment).graphicsLayer { alpha = contentProgress; scaleX = contentScale; scaleY = contentScale; rotationX = 180f * progress; this.transformOrigin = transformOrigin }) { content() }
        Box(modifier = Modifier.graphicsLayer { alpha = 1f - labelOpacity }) { label() }
    }
}
