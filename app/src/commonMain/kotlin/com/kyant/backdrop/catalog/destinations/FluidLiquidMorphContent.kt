package com.kyant.backdrop.catalog.destinations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.catalog.BackdropDemoScaffold
import com.kyant.backdrop.catalog.BluetoothGlyphIcon
import com.kyant.backdrop.catalog.CellularDataGlyphIcon
import com.kyant.backdrop.catalog.HeadphonesGlyphIcon
import com.kyant.backdrop.catalog.HotspotGlyphIcon
import com.kyant.backdrop.catalog.VpnGlyphIcon
import com.kyant.backdrop.catalog.WatchGlyphIcon
import com.kyant.backdrop.catalog.components.FluidMorphContainer
import com.kyant.backdrop.catalog.components.LiquidButton
import com.kyant.backdrop.catalog.components.LiquidSlider
import com.kyant.backdrop.catalog.components.LiquidToggle
import com.kyant.backdrop.catalog.components.MorphShapeType
import kotlinx.coroutines.launch

enum class DemoPreset(val label: String) {
    ControlCenter("iOS Control Center (1:1)"),
    CircleToCard("Circle ➔ Card"),
    PillToSquircle("Pill ➔ Squircle"),
    CustomModular("Custom Modular")
}

@Composable
fun FluidLiquidMorphContent() {
    val isLightTheme = !isSystemInDarkTheme()
    val contentColor = if (isLightTheme) Color.Black else Color.White
    val secondaryColor = if (isLightTheme) Color(0xFF6E6E73) else Color(0xFFA1A1A6)
    val cardBackground = if (isLightTheme) Color.White.copy(alpha = 0.5f) else Color(0xFF1E1E1E).copy(alpha = 0.5f)

    var selectedPreset by remember { mutableStateOf(DemoPreset.ControlCenter) }

    // --- PHYSICS & TIMING TUNING ---
    var springStiffness by remember { mutableFloatStateOf(380f) }
    var springDamping by remember { mutableFloatStateOf(0.72f) }
    var viscosityBulge by remember { mutableFloatStateOf(0.40f) }
    var meniscusSagDp by remember { mutableFloatStateOf(38f) }

    // --- GLASS DESIGN PARAMS ---
    var isGlassEnabled by remember { mutableStateOf(true) }
    var blurRadiusDp by remember { mutableFloatStateOf(14f) }
    var refractionHeightDp by remember { mutableFloatStateOf(18f) }
    var refractionAmountDp by remember { mutableFloatStateOf(24f) }
    var chromaticAberration by remember { mutableStateOf(false) }

    // --- CUSTOM MODULAR DIMENSIONS ---
    var customStartWidth by remember { mutableFloatStateOf(146f) }
    var customStartHeight by remember { mutableFloatStateOf(138f) }
    var customTargetWidth by remember { mutableFloatStateOf(302f) }
    var customTargetHeight by remember { mutableFloatStateOf(332f) }
    var customStartRadius by remember { mutableFloatStateOf(28f) }
    var customTargetRadius by remember { mutableFloatStateOf(32f) }
    var customStartShape by remember { mutableStateOf(MorphShapeType.Squircle) }
    var customTargetShape by remember { mutableStateOf(MorphShapeType.Squircle) }

    val animationScope = rememberCoroutineScope()
    val animatableProgress = remember { Animatable(0f) }
    var isExpanding by remember { mutableStateOf(true) }

    val triggerMorph: (Boolean) -> Unit = { shouldExpand ->
        isExpanding = shouldExpand
        animationScope.launch {
            val target = if (shouldExpand) 1f else 0f
            animatableProgress.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = springDamping,
                    stiffness = springStiffness,
                    visibilityThreshold = 0.0005f
                )
            )
            animatableProgress.snapTo(target)
        }
    }

    BackdropDemoScaffold { backdrop ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            BasicText(
                "iOS Fluid Liquid Morph",
                Modifier.padding(top = 16.dp, bottom = 4.dp),
                style = TextStyle(contentColor, 26.sp, FontWeight.SemiBold)
            )
            BasicText(
                "1:1 iOS Control Center Visceral Glass Animation",
                style = TextStyle(Color(0xFF0088FF), 15.sp, FontWeight.Medium)
            )

            Spacer(Modifier.height(12.dp))

            // --- INTERACTIVE STAGE PREVIEW ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.12f))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            if (animatableProgress.value > 0.5f) {
                                triggerMorph(false)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                when (selectedPreset) {
                    DemoPreset.ControlCenter -> {
                        ControlCenterReplicaStage(
                            animatableProgress = animatableProgress,
                            isExpanding = isExpanding,
                            onToggle = { triggerMorph(animatableProgress.value <= 0.5f) },
                            backdrop = backdrop,
                            viscosityBulge = viscosityBulge,
                            meniscusSagDp = meniscusSagDp,
                            isGlassEnabled = isGlassEnabled,
                            blurRadius = blurRadiusDp,
                            refractionHeight = refractionHeightDp,
                            refractionAmount = refractionAmountDp,
                            chromaticAberration = chromaticAberration
                        )
                    }
                    DemoPreset.CircleToCard -> {
                        FluidMorphContainer(
                            animatableProgress = animatableProgress,
                            isExpanding = isExpanding,
                            backdrop = backdrop,
                            startWidth = 64.dp,
                            startHeight = 64.dp,
                            targetWidth = 300.dp,
                            targetHeight = 330.dp,
                            startCornerRadius = 32.dp,
                            targetCornerRadius = 32.dp,
                            startShapeType = MorphShapeType.Circle,
                            targetShapeType = MorphShapeType.Rounded,
                            alignment = Alignment.Center,
                            viscosityBulge = viscosityBulge,
                            meniscusSagDp = meniscusSagDp,
                            isGlassEnabled = isGlassEnabled,
                            blurRadius = blurRadiusDp,
                            refractionHeight = refractionHeightDp,
                            refractionAmount = refractionAmountDp,
                            chromaticAberration = chromaticAberration,
                            startContent = {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clickable { triggerMorph(true) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .paint(
                                                rememberVectorPainter(BluetoothGlyphIcon),
                                                colorFilter = ColorFilter.tint(Color.White)
                                            )
                                    )
                                }
                            },
                            targetContent = {
                                BluetoothExpandedMenuContent(
                                    contentColor = contentColor,
                                    secondaryColor = secondaryColor,
                                    onClose = { triggerMorph(false) }
                                )
                            }
                        )
                    }
                    DemoPreset.PillToSquircle -> {
                        FluidMorphContainer(
                            animatableProgress = animatableProgress,
                            isExpanding = isExpanding,
                            backdrop = backdrop,
                            startWidth = 180.dp,
                            startHeight = 56.dp,
                            targetWidth = 300.dp,
                            targetHeight = 330.dp,
                            startCornerRadius = 28.dp,
                            targetCornerRadius = 32.dp,
                            startShapeType = MorphShapeType.Capsule,
                            targetShapeType = MorphShapeType.Squircle,
                            alignment = Alignment.Center,
                            viscosityBulge = viscosityBulge,
                            meniscusSagDp = meniscusSagDp,
                            isGlassEnabled = isGlassEnabled,
                            blurRadius = blurRadiusDp,
                            refractionHeight = refractionHeightDp,
                            refractionAmount = refractionAmountDp,
                            chromaticAberration = chromaticAberration,
                            startContent = {
                                Row(
                                    modifier = Modifier
                                        .size(180.dp, 56.dp)
                                        .clickable { triggerMorph(true) }
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF007AFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .paint(
                                                    rememberVectorPainter(BluetoothGlyphIcon),
                                                    colorFilter = ColorFilter.tint(Color.White)
                                                )
                                        )
                                    }
                                    BasicText(
                                        "Connected",
                                        style = TextStyle(contentColor, 15.sp, FontWeight.Medium)
                                    )
                                }
                            },
                            targetContent = {
                                BluetoothExpandedMenuContent(
                                    contentColor = contentColor,
                                    secondaryColor = secondaryColor,
                                    onClose = { triggerMorph(false) }
                                )
                            }
                        )
                    }
                    DemoPreset.CustomModular -> {
                        FluidMorphContainer(
                            animatableProgress = animatableProgress,
                            isExpanding = isExpanding,
                            backdrop = backdrop,
                            startWidth = customStartWidth.dp,
                            startHeight = customStartHeight.dp,
                            targetWidth = customTargetWidth.dp,
                            targetHeight = customTargetHeight.dp,
                            startCornerRadius = customStartRadius.dp,
                            targetCornerRadius = customTargetRadius.dp,
                            startShapeType = customStartShape,
                            targetShapeType = customTargetShape,
                            alignment = Alignment.Center,
                            viscosityBulge = viscosityBulge,
                            meniscusSagDp = meniscusSagDp,
                            isGlassEnabled = isGlassEnabled,
                            blurRadius = blurRadiusDp,
                            refractionHeight = refractionHeightDp,
                            refractionAmount = refractionAmountDp,
                            chromaticAberration = chromaticAberration,
                            startContent = {
                                Box(
                                    modifier = Modifier
                                        .size(customStartWidth.dp, customStartHeight.dp)
                                        .clickable { triggerMorph(true) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .paint(
                                                    rememberVectorPainter(BluetoothGlyphIcon),
                                                    colorFilter = ColorFilter.tint(contentColor)
                                                )
                                        )
                                        BasicText(
                                            "Tap Morph",
                                            style = TextStyle(contentColor, 13.sp, FontWeight.SemiBold)
                                        )
                                    }
                                }
                            },
                            targetContent = {
                                BluetoothExpandedMenuContent(
                                    contentColor = contentColor,
                                    secondaryColor = secondaryColor,
                                    onClose = { triggerMorph(false) }
                                )
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- CONTROLS & PHYSICS TUNING SECTION ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. PRESET SELECTOR
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BasicText("Morph Mode / Preset", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DemoPreset.entries.take(2).forEach { preset ->
                            val isSelected = selectedPreset == preset
                            LiquidButton(
                                onClick = { selectedPreset = preset },
                                backdrop = emptyBackdrop(),
                                modifier = Modifier.weight(1f).height(42.dp),
                                tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified,
                                surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.15f)
                            ) {
                                BasicText(
                                    preset.label,
                                    style = TextStyle(if (isSelected) Color.White else contentColor, 12.sp, FontWeight.Medium)
                                )
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DemoPreset.entries.drop(2).forEach { preset ->
                            val isSelected = selectedPreset == preset
                            LiquidButton(
                                onClick = { selectedPreset = preset },
                                backdrop = emptyBackdrop(),
                                modifier = Modifier.weight(1f).height(42.dp),
                                tint = if (isSelected) Color(0xFF0088FF) else Color.Unspecified,
                                surfaceColor = if (isSelected) Color.Unspecified else Color.White.copy(0.15f)
                            ) {
                                BasicText(
                                    preset.label,
                                    style = TextStyle(if (isSelected) Color.White else contentColor, 12.sp, FontWeight.Medium)
                                )
                            }
                        }
                    }
                }

                // 2. INTERACTIVE ANIMATION CONTROLLER
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BasicText("Live Animation Control", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidButton(
                            onClick = { triggerMorph(true) },
                            backdrop = emptyBackdrop(),
                            modifier = Modifier.weight(1f).height(42.dp),
                            tint = if (animatableProgress.targetValue > 0.5f) Color(0xFF0088FF) else Color.Unspecified,
                            surfaceColor = Color.White.copy(0.15f)
                        ) {
                            BasicText("Expand (Viscous)", style = TextStyle(contentColor, 13.sp, FontWeight.SemiBold))
                        }
                        LiquidButton(
                            onClick = { triggerMorph(false) },
                            backdrop = emptyBackdrop(),
                            modifier = Modifier.weight(1f).height(42.dp),
                            tint = if (animatableProgress.targetValue <= 0.5f) Color(0xFF0088FF) else Color.Unspecified,
                            surfaceColor = Color.White.copy(0.15f)
                        ) {
                            BasicText("Collapse (Meniscus)", style = TextStyle(contentColor, 13.sp, FontWeight.SemiBold))
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Scrub Progress", style = TextStyle(contentColor, 14.sp))
                            BasicText("${(animatableProgress.value.coerceIn(0f, 1f) * 100).toInt()}%", style = TextStyle(secondaryColor, 14.sp))
                        }
                        LiquidSlider(
                            value = { animatableProgress.value.coerceIn(0f, 1f) },
                            onValueChange = { newVal ->
                                isExpanding = newVal >= animatableProgress.value
                                animationScope.launch { animatableProgress.snapTo(newVal) }
                            },
                            valueRange = 0f..1f,
                            visibilityThreshold = 0.001f,
                            backdrop = emptyBackdrop()
                        )
                    }
                }

                // 3. FLUID & SPRING PHYSICS TUNING
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BasicText("Fluid Dynamics & Spring Physics", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold))

                    TuningControl(
                        name = "Viscosity Bulge",
                        description = "Simulates fluid momentum belly expanding horizontally during downward stretch.",
                        value = viscosityBulge,
                        defaultValue = 0.35f,
                        onValueChange = { viscosityBulge = it },
                        valueRange = 0f..1f,
                        formatValue = { "${(it * 100).toInt()}%" },
                        backdrop = emptyBackdrop(),
                        contentColor = contentColor,
                        secondaryColor = secondaryColor
                    )

                    TuningControl(
                        name = "Meniscus Droplet Sag",
                        description = "Simulates liquid surface tension delay and tear-drop snapback on retraction.",
                        value = meniscusSagDp,
                        defaultValue = 38f,
                        onValueChange = { meniscusSagDp = it },
                        valueRange = 0f..80f,
                        formatValue = { "${it.toInt()} dp" },
                        backdrop = emptyBackdrop(),
                        contentColor = contentColor,
                        secondaryColor = secondaryColor
                    )

                    TuningControl(
                        name = "Spring Stiffness",
                        description = "iOS critically damped spring tension.",
                        value = springStiffness,
                        defaultValue = 340f,
                        onValueChange = { springStiffness = it },
                        valueRange = 100f..800f,
                        formatValue = { "${it.toInt()}" },
                        backdrop = emptyBackdrop(),
                        contentColor = contentColor,
                        secondaryColor = secondaryColor
                    )

                    TuningControl(
                        name = "Spring Damping Ratio",
                        description = "Damping value (< 1.0 creates natural liquid overshoot bounce).",
                        value = springDamping,
                        defaultValue = 0.74f,
                        onValueChange = { springDamping = it },
                        valueRange = 0.4f..1f,
                        formatValue = { "${(it * 100).toInt()}%" },
                        backdrop = emptyBackdrop(),
                        contentColor = contentColor,
                        secondaryColor = secondaryColor
                    )
                }

                // 4. CUSTOM MODULAR SHAPE & SIZE TUNING (If Custom preset selected)
                if (selectedPreset == DemoPreset.CustomModular) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        BasicText("Modular Size & Shape Configuration", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold))

                        TuningControl(
                            name = "Start Width",
                            value = customStartWidth,
                            defaultValue = 146f,
                            onValueChange = { customStartWidth = it },
                            valueRange = 60f..300f,
                            formatValue = { "${it.toInt()} dp" },
                            backdrop = emptyBackdrop(),
                            contentColor = contentColor,
                            secondaryColor = secondaryColor
                        )

                        TuningControl(
                            name = "Start Height",
                            value = customStartHeight,
                            defaultValue = 138f,
                            onValueChange = { customStartHeight = it },
                            valueRange = 50f..250f,
                            formatValue = { "${it.toInt()} dp" },
                            backdrop = emptyBackdrop(),
                            contentColor = contentColor,
                            secondaryColor = secondaryColor
                        )

                        TuningControl(
                            name = "Target Width",
                            value = customTargetWidth,
                            defaultValue = 302f,
                            onValueChange = { customTargetWidth = it },
                            valueRange = 160f..360f,
                            formatValue = { "${it.toInt()} dp" },
                            backdrop = emptyBackdrop(),
                            contentColor = contentColor,
                            secondaryColor = secondaryColor
                        )

                        TuningControl(
                            name = "Target Height",
                            value = customTargetHeight,
                            defaultValue = 332f,
                            onValueChange = { customTargetHeight = it },
                            valueRange = 200f..560f,
                            formatValue = { "${it.toInt()} dp" },
                            backdrop = emptyBackdrop(),
                            contentColor = contentColor,
                            secondaryColor = secondaryColor
                        )

                        TuningControl(
                            name = "Start Corner Radius",
                            value = customStartRadius,
                            defaultValue = 28f,
                            onValueChange = { customStartRadius = it },
                            valueRange = 4f..60f,
                            formatValue = { "${it.toInt()} dp" },
                            backdrop = emptyBackdrop(),
                            contentColor = contentColor,
                            secondaryColor = secondaryColor
                        )

                        TuningControl(
                            name = "Target Corner Radius",
                            value = customTargetRadius,
                            defaultValue = 32f,
                            onValueChange = { customTargetRadius = it },
                            valueRange = 4f..60f,
                            formatValue = { "${it.toInt()} dp" },
                            backdrop = emptyBackdrop(),
                            contentColor = contentColor,
                            secondaryColor = secondaryColor
                        )
                    }
                }

                // 5. GLASS OPTICS TUNING
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BasicText("Glass Shading & Refraction", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicText("Enable Liquid Glass", style = TextStyle(contentColor, 14.sp))
                        LiquidToggle(
                            selected = { isGlassEnabled },
                            onSelect = { isGlassEnabled = it },
                            backdrop = emptyBackdrop()
                        )
                    }

                    TuningControl(
                        name = "Blur Radius",
                        value = blurRadiusDp,
                        defaultValue = 14f,
                        onValueChange = { blurRadiusDp = it },
                        valueRange = 0f..40f,
                        formatValue = { "${it.toInt()} dp" },
                        backdrop = emptyBackdrop(),
                        contentColor = contentColor,
                        secondaryColor = secondaryColor
                    )

                    TuningControl(
                        name = "Refraction Height",
                        value = refractionHeightDp,
                        defaultValue = 18f,
                        onValueChange = { refractionHeightDp = it },
                        valueRange = 0f..40f,
                        formatValue = { "${it.toInt()} dp" },
                        backdrop = emptyBackdrop(),
                        contentColor = contentColor,
                        secondaryColor = secondaryColor
                    )

                    TuningControl(
                        name = "Refraction Amount",
                        value = refractionAmountDp,
                        defaultValue = 24f,
                        onValueChange = { refractionAmountDp = it },
                        valueRange = 0f..50f,
                        formatValue = { "${it.toInt()} dp" },
                        backdrop = emptyBackdrop(),
                        contentColor = contentColor,
                        secondaryColor = secondaryColor
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicText("Chromatic Aberration", style = TextStyle(contentColor, 14.sp))
                        LiquidToggle(
                            selected = { chromaticAberration },
                            onSelect = { chromaticAberration = it },
                            backdrop = emptyBackdrop()
                        )
                    }
                }

                // Bottom spacer to ensure controls are not obscured by the scaffold FAB
                Spacer(Modifier.height(72.dp))
            }
        }
    }
}

/**
 * Exact 1:1 replica of the iOS Control Center Connectivity Block from the uploaded video.
 */
@Composable
private fun ControlCenterReplicaStage(
    animatableProgress: Animatable<Float, *>,
    isExpanding: Boolean,
    onToggle: () -> Unit,
    backdrop: Backdrop,
    viscosityBulge: Float,
    meniscusSagDp: Float,
    isGlassEnabled: Boolean,
    blurRadius: Float,
    refractionHeight: Float,
    refractionAmount: Float,
    chromaticAberration: Boolean
) {
    val isLightTheme = !isSystemInDarkTheme()
    val contentColor = if (isLightTheme) Color.Black else Color.White
    val subColor = if (isLightTheme) Color(0xFF8E8E93) else Color(0xFFA1A1A6)
    val tileBg = if (isLightTheme) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.12f)
    val cardRadius = 28.dp

    Box(
        modifier = Modifier
            .size(334.dp, 364.dp)
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(32.dp))
            .padding(16.dp)
    ) {
        // --- BACKGROUND LAYER: OTHER CONTROL CENTER TILES (STATIONARY UNDERNEATH) ---
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Cellular Data (Left) and Placeholder Space for Bluetooth (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(138.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cellular Data Tile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(cardRadius))
                        .background(tileBg)
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34C759)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .paint(
                                        rememberVectorPainter(CellularDataGlyphIcon),
                                        colorFilter = ColorFilter.tint(Color.White)
                                    )
                            )
                        }
                        Column {
                            BasicText("Cellular Data", style = TextStyle(Color.White, 14.sp, FontWeight.SemiBold))
                            BasicText("Primary", style = TextStyle(Color.White.copy(alpha = 0.65f), 12.sp))
                        }
                    }
                }

                // Space where Bluetooth sits
                Spacer(modifier = Modifier.weight(1f))
            }

            // Row 2: Personal Hotspot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(cardRadius))
                    .background(tileBg)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .paint(
                                rememberVectorPainter(HotspotGlyphIcon),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                    )
                }
                Column {
                    BasicText("Personal Hotspot", style = TextStyle(Color.White, 14.sp, FontWeight.SemiBold))
                    BasicText("Off", style = TextStyle(Color.White.copy(alpha = 0.65f), 12.sp))
                }
            }

            // Row 3: VPN
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(cardRadius))
                    .background(tileBg)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .paint(
                                rememberVectorPainter(VpnGlyphIcon),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                    )
                }
                Column {
                    BasicText("VPN", style = TextStyle(Color.White, 14.sp, FontWeight.SemiBold))
                    BasicText("Off", style = TextStyle(Color.White.copy(alpha = 0.65f), 12.sp))
                }
            }
        }

        // --- FOREGROUND LAYER: THE 1:1 LIQUID MORPHING BLUETOOTH CONTAINER ---
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopEnd
        ) {
            FluidMorphContainer(
                animatableProgress = animatableProgress,
                isExpanding = isExpanding,
                backdrop = backdrop,
                startWidth = 146.dp,
                startHeight = 138.dp,
                targetWidth = 302.dp,
                targetHeight = 332.dp,
                startCornerRadius = cardRadius,
                targetCornerRadius = 32.dp,
                startShapeType = MorphShapeType.Squircle,
                targetShapeType = MorphShapeType.Squircle,
                alignment = Alignment.TopEnd,
                viscosityBulge = viscosityBulge,
                meniscusSagDp = meniscusSagDp,
                isGlassEnabled = isGlassEnabled,
                blurRadius = blurRadius,
                refractionHeight = refractionHeight,
                refractionAmount = refractionAmount,
                chromaticAberration = chromaticAberration,
                startContent = {
                    // RESTING TILE CONTENT
                    Box(
                        modifier = Modifier
                            .size(146.dp, 138.dp)
                            .clickable { onToggle() }
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF007AFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .paint(
                                            rememberVectorPainter(BluetoothGlyphIcon),
                                            colorFilter = ColorFilter.tint(Color.White)
                                        )
                                )
                            }
                            Column {
                                BasicText("Bluetooth", style = TextStyle(Color.White, 14.sp, FontWeight.SemiBold))
                                BasicText(
                                    "Zachariah's AirPods Pro...",
                                    style = TextStyle(Color.White.copy(alpha = 0.70f), 11.sp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                },
                targetContent = {
                    // EXPANDED MENU CONTENT (Exact items from the video)
                    BluetoothExpandedMenuContent(
                        contentColor = contentColor,
                        secondaryColor = subColor,
                        onClose = onToggle
                    )
                }
            )
        }
    }
}

/**
 * Exact replica of the expanded Bluetooth devices menu seen in the user's video.
 */
@Composable
fun BluetoothExpandedMenuContent(
    contentColor: Color,
    secondaryColor: Color,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(302.dp)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DeviceRow(
            title = "Drop-BMR1",
            status = "Not Connected",
            isConnected = false,
            contentColor = contentColor,
            secondaryColor = secondaryColor,
            onClick = onClose
        )
        DeviceRow(
            title = "Maruti Suzuki",
            status = "Not Connected",
            isConnected = false,
            contentColor = contentColor,
            secondaryColor = secondaryColor,
            onClick = onClose
        )
        DeviceRow(
            title = "S-cross",
            status = "Not Connected",
            isConnected = false,
            contentColor = contentColor,
            secondaryColor = secondaryColor,
            onClick = onClose
        )
        DeviceRow(
            title = "ZACH-PC",
            status = "Not Connected",
            isConnected = false,
            contentColor = contentColor,
            secondaryColor = secondaryColor,
            onClick = onClose
        )
        DeviceRow(
            title = "Zachariah's AirPods Pro #4",
            status = "Connected",
            isConnected = true,
            icon = HeadphonesGlyphIcon,
            contentColor = contentColor,
            secondaryColor = secondaryColor,
            onClick = onClose
        )
        DeviceRow(
            title = "Zachariah's Apple Watch",
            status = "Connected",
            isConnected = true,
            icon = WatchGlyphIcon,
            contentColor = contentColor,
            secondaryColor = secondaryColor,
            onClick = onClose
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(contentColor.copy(alpha = 0.15f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClose() }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            BasicText(
                "Bluetooth Settings...",
                style = TextStyle(contentColor, 14.sp, FontWeight.Medium)
            )
        }
    }
}

@Composable
private fun DeviceRow(
    title: String,
    status: String,
    isConnected: Boolean,
    icon: ImageVector? = null,
    contentColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                title,
                style = TextStyle(contentColor, 14.sp, FontWeight.SemiBold),
                maxLines = 1
            )
            BasicText(
                status,
                style = TextStyle(if (isConnected) Color(0xFF007AFF) else secondaryColor, 12.sp)
            )
        }
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .paint(rememberVectorPainter(icon), colorFilter = ColorFilter.tint(contentColor.copy(alpha = 0.7f)))
            )
        }
    }
}
