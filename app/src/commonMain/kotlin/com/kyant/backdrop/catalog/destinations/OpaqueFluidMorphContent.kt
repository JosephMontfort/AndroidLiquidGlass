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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.catalog.BluetoothGlyphIcon
import com.kyant.backdrop.catalog.CellularDataGlyphIcon
import com.kyant.backdrop.catalog.HotspotGlyphIcon
import com.kyant.backdrop.catalog.VpnGlyphIcon
import com.kyant.backdrop.catalog.components.LiquidButton
import com.kyant.backdrop.catalog.components.OpaqueFluidMorphContainer
import kotlinx.coroutines.launch

@Composable
fun OpaqueFluidMorphContent() {
    val isLightTheme = !isSystemInDarkTheme()
    val contentColor = if (isLightTheme) Color.Black else Color.White
    val secondaryColor = if (isLightTheme) Color(0xFF6E6E73) else Color(0xFFA1A1A6)
    val cardBackground = if (isLightTheme) Color.White.copy(alpha = 0.5f) else Color(0xFF1E1E1E).copy(alpha = 0.5f)

    val animationScope = rememberCoroutineScope()
    val animatableProgress = remember { Animatable(0f) }
    var isExpanding by remember { mutableStateOf(false) }

    val triggerMorph: (Boolean) -> Unit = { shouldExpand ->
        isExpanding = shouldExpand
        animationScope.launch {
            val target = if (shouldExpand) 1f else 0f
            animatableProgress.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = 0.72f,
                    stiffness = 380f,
                    visibilityThreshold = 0.0005f
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        BasicText(
            "Opaque Fluid Morph",
            Modifier.padding(top = 16.dp, bottom = 4.dp),
            style = TextStyle(contentColor, 26.sp, FontWeight.SemiBold)
        )
        BasicText(
            "Exact 1:1 iOS Control Center Curve (Opaque, Artifact-Free)",
            style = TextStyle(Color(0xFF0088FF), 15.sp, FontWeight.Medium)
        )

        Spacer(Modifier.height(12.dp))

        // INTERACTIVE STAGE PREVIEW
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black.copy(alpha = 0.12f))
                .pointerInput(Unit) {
                    detectTapGestures {
                        if (animatableProgress.targetValue > 0.5f) {
                            triggerMorph(false)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            OpaqueControlCenterReplicaStage(
                animatableProgress = animatableProgress,
                isExpanding = isExpanding,
                onToggle = { triggerMorph(animatableProgress.targetValue <= 0.5f) }
            )
        }

        Spacer(Modifier.height(16.dp))

        // CONTROLS & PHYSICS TUNING SECTION
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
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                BasicText("Live Animation Control", style = TextStyle(contentColor, 18.sp, FontWeight.SemiBold))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidButton(
                        onClick = { triggerMorph(true) },
                        backdrop = com.kyant.backdrop.backdrops.emptyBackdrop(),
                        modifier = Modifier.weight(1f).height(42.dp),
                        tint = if (animatableProgress.targetValue > 0.5f) Color(0xFF0088FF) else Color.Unspecified,
                        surfaceColor = Color.White.copy(0.15f)
                    ) {
                        BasicText("Expand", style = TextStyle(contentColor, 13.sp, FontWeight.SemiBold))
                    }
                    LiquidButton(
                        onClick = { triggerMorph(false) },
                        backdrop = com.kyant.backdrop.backdrops.emptyBackdrop(),
                        modifier = Modifier.weight(1f).height(42.dp),
                        tint = if (animatableProgress.targetValue <= 0.5f) Color(0xFF0088FF) else Color.Unspecified,
                        surfaceColor = Color.White.copy(0.15f)
                    ) {
                        BasicText("Collapse", style = TextStyle(contentColor, 13.sp, FontWeight.SemiBold))
                    }
                }
            }
        }
    }
}

@Composable
private fun OpaqueControlCenterReplicaStage(
    animatableProgress: Animatable<Float, *>,
    isExpanding: Boolean,
    onToggle: () -> Unit,
) {
    val isLightTheme = !isSystemInDarkTheme()
    val contentColor = if (isLightTheme) Color.Black else Color.White
    val tileBg = if (isLightTheme) Color.White else Color(0xFF2C2C2E)
    val cardRadius = 28.dp

    Box(
        modifier = Modifier
            .size(334.dp, 364.dp)
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(32.dp))
            .padding(16.dp)
    ) {
        // BACKGROUND LAYER: OTHER CONTROL CENTER TILES
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(138.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                Spacer(modifier = Modifier.weight(1f))
            }
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

        // FOREGROUND LAYER: THE 1:1 MORPHING BLUETOOTH CONTAINER
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopEnd
        ) {
            OpaqueFluidMorphContainer(
                animatableProgress = animatableProgress,
                isExpanding = isExpanding,
                containerColor = tileBg,
                startWidth = 146.dp,
                startHeight = 138.dp,
                targetWidth = 302.dp,
                targetHeight = 332.dp,
                startCornerRadius = 28.dp,
                targetCornerRadius = 34.dp,
                alignment = Alignment.TopEnd,
                startContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(onClick = onToggle)
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
                                        .size(22.dp)
                                        .paint(
                                            rememberVectorPainter(BluetoothGlyphIcon),
                                            colorFilter = ColorFilter.tint(Color.White)
                                        )
                                )
                            }
                            Column {
                                BasicText("Bluetooth", style = TextStyle(Color.White, 14.sp, FontWeight.SemiBold))
                                BasicText("Zachariah's Ai...", style = TextStyle(Color.White.copy(alpha = 0.65f), 12.sp))
                            }
                        }
                    }
                },
                targetContent = {
                    BluetoothExpandedMenuContent(
                        contentColor = Color.White,
                        secondaryColor = Color.White.copy(alpha = 0.65f),
                        onClose = onToggle
                    )
                }
            )
        }
    }
}
