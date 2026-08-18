package com.kyant.backdrop.catalog.destinations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
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
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.launch
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

    fun getSpec(): AnimationSpec<Float> = when (this) {
        Bouncy -> spring(dampingRatio = 0.65f, stiffness = 250f)
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

    var progress by remember { mutableFloatStateOf(0f) }
    var selectedAlignment by remember { mutableStateOf(MenuAlignment.TopLeading) }
    var selectedPreset by remember { mutableStateOf(MenuAnimationPreset.Bouncy) }

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

            BasicText(
                "Preview",
                style = TextStyle(Color(0xFF0088FF), 15f.sp, FontWeight.Medium)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340f.dp)
                    .clip(RoundedRectangle(20f.dp))
                    .background(Color.Black.copy(alpha = 0.08f))
            ) {
                ExpandableGlassMenu(
                    progress = progress,
                    alignment = selectedAlignment,
                    backdrop = backdrop,
                    modifier = Modifier.padding(16f.dp),
                    label = {
                        Box(
                            modifier = Modifier
                                .size(55f.dp)
                                .clickable {
                                    val target = if (progress > 0.5f) 0f else 1f
                                    animationScope.launch {
                                        animatableProgress.snapTo(progress)
                                        animatableProgress.animateTo(target, selectedPreset.getSpec()) {
                                            progress = value
                                        }
                                    }
                                },
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
                ) {
                    MenuRow(
                        icon = Icons.Send,
                        title = "Send",
                        description = "This is a sample text description",
                        contentColor = contentColor,
                        secondaryColor = secondaryColor
                    )
                    MenuRow(
                        icon = Icons.Swap,
                        title = "Swap",
                        description = "This is a sample text description",
                        contentColor = contentColor,
                        secondaryColor = secondaryColor
                    )
                    MenuRow(
                        icon = Icons.Receive,
                        title = "Receive",
                        description = "This is a sample text description",
                        contentColor = contentColor,
                        secondaryColor = secondaryColor
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedRectangle(24f.dp) },
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
                BasicText(
                    "Properties",
                    style = TextStyle(contentColor, 18f.sp, FontWeight.SemiBold)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BasicText("Progress", style = TextStyle(contentColor, 14f.sp))
                        BasicText("${(progress * 100).toInt()}%", style = TextStyle(secondaryColor, 14f.sp))
                    }
                    LiquidSlider(
                        value = { progress },
                        onValueChange = {
                            progress = it
                            animationScope.launch { animatableProgress.snapTo(it) }
                        },
                        valueRange = 0f..1f,
                        visibilityThreshold = 0.001f,
                        backdrop = controlsBackdrop
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                    BasicText("Alignment", style = TextStyle(contentColor, 14f.sp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8f.dp)
                    ) {
                        MenuAlignment.entries.forEach { align ->
                            val isSelected = selectedAlignment == align
                            LiquidButton(
                                onClick = { selectedAlignment = align },
                                backdrop = controlsBackdrop,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40f.dp),
                                tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified,
                                surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.15f)
                            ) {
                                BasicText(
                                    align.label,
                                    style = TextStyle(
                                        if (isSelected) Color.White else contentColor,
                                        12f.sp,
                                        FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8f.dp)) {
                    BasicText("Animation Trigger", style = TextStyle(contentColor, 14f.sp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8f.dp)
                    ) {
                        MenuAnimationPreset.entries.forEach { preset ->
                            val isSelected = selectedPreset == preset
                            LiquidButton(
                                onClick = {
                                    selectedPreset = preset
                                    val target = if (progress > 0.5f) 0f else 1f
                                    animationScope.launch {
                                        animatableProgress.snapTo(progress)
                                        animatableProgress.animateTo(target, preset.getSpec()) {
                                            progress = value
                                        }
                                    }
                                },
                                backdrop = controlsBackdrop,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42f.dp),
                                tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified,
                                surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.15f)
                            ) {
                                BasicText(
                                    preset.label,
                                    style = TextStyle(
                                        if (isSelected) Color.White else contentColor,
                                        13f.sp,
                                        FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16f.dp))
        }
    }
}

@Composable
fun ExpandableGlassMenu(
    progress: Float,
    alignment: MenuAlignment,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 30f.dp,
    labelSize: Size = Size(55f, 55f),
    label: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var contentMeasuredSize by remember { mutableStateOf(Size.Zero) }

    val labelSizePx = with(density) { Size(labelSize.width.dp.toPx(), labelSize.height.dp.toPx()) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = alignment.composeAlignment
    ) {
        GlassEffectContainer(
            progress = progress,
            alignment = alignment,
            backdrop = backdrop,
            cornerRadius = cornerRadius,
            labelSize = labelSizePx,
            contentSize = contentMeasuredSize,
            label = label,
            content = {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max) // Ensures uniform width for rows
                        .onSizeChanged {
                            if (it.width > 0 && it.height > 0) {
                                contentMeasuredSize = it.toSize()
                            }
                        }
                        .padding(10f.dp),
                    verticalArrangement = Arrangement.spacedBy(12f.dp)
                ) {
                    content()
                }
            }
        )
    }
}

@Composable
fun GlassEffectContainer(
    progress: Float,
    alignment: MenuAlignment,
    backdrop: Backdrop,
    cornerRadius: Dp,
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
    val contentOpacity = contentProgress

    val minAspectScale = if (contentSize.width > 0f && contentSize.height > 0f) {
        min(labelSize.width / contentSize.width, labelSize.height / contentSize.height)
    } else {
        1f
    }
    val contentScale = minAspectScale + (1f - minAspectScale) * ((progress - 0.35f) / 0.65f).coerceAtLeast(0f)

    val blurProgress = if (progress > 0.5f) (1f - progress) / 0.5f else progress / 0.5f
    val squishScale = 1f - (blurProgress.coerceIn(0f, 1f) * 0.05f)

    val maxOffsetPx = with(density) { 75f.dp.toPx() }
    val offsetY = alignment.calculateOffsetY(blurProgress, maxOffsetPx)
    val transformOrigin = alignment.transformOrigin

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationY = offsetY
                scaleX = squishScale
                scaleY = squishScale
                this.transformOrigin = transformOrigin
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(cornerRadius) },
                effects = {
                    // Removed heavy vibrancy and SDF lens during size animation for ultra fluidity
                    blur((2f + 6f * blurProgress.coerceIn(0f, 1f)).dp.toPx())
                },
                highlight = { Highlight.Default.copy(alpha = 0.65f) },
                shadow = { Shadow(radius = 18f.dp, color = Color.Black.copy(alpha = 0.12f)) },
                innerShadow = { InnerShadow(radius = 10f.dp, color = Color.White.copy(alpha = 0.25f)) },
                onDrawSurface = {
                    drawRect(
                        if (isLightTheme) Color.White.copy(alpha = 0.28f + 0.12f * progress)
                        else Color(0xFF1E1E1E).copy(alpha = 0.35f + 0.15f * progress)
                    )
                }
            )
            .clip(RoundedRectangle(cornerRadius)) // Critical: Keeps unconstrained items inside the glass
            .size(
                width = with(density) { currentWidthPx.toDp() },
                height = with(density) { currentHeightPx.toDp() }
            ),
        contentAlignment = alignment.composeAlignment
    ) {
        // Content view (scales and fades in)
        Box(
            modifier = Modifier
                // Critical: This breaks the parent boundaries to measure its full, 
                // native size matching SwiftUI's `.fixedSize()` modifier logic.
                .wrapContentSize(unbounded = true, align = alignment.composeAlignment) 
                .graphicsLayer {
                    alpha = contentOpacity
                    scaleX = contentScale
                    scaleY = contentScale
                    this.transformOrigin = transformOrigin
                }
        ) {
            content()
        }

        // Label icon view (fades out)
        Box(
            modifier = Modifier
                .size(
                    width = with(density) { labelSize.width.toDp() },
                    height = with(density) { labelSize.height.toDp() }
                )
                .graphicsLayer {
                    alpha = 1f - labelOpacity
                },
            contentAlignment = Alignment.Center
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
    secondaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth() // Perfectly matches the IntrinsicSize.Max provided by parent Column
            .clip(Capsule())
            .clickable {}
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
            BasicText(
                title,
                style = TextStyle(contentColor, 15f.sp, FontWeight.SemiBold)
            )
            BasicText(
                description,
                style = TextStyle(secondaryColor, 12f.sp),
                maxLines = 1
            )
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
