package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.catalog.BackdropDemoScaffold
import com.kyant.backdrop.catalog.components.LiquidButton
import com.kyant.backdrop.catalog.components.LiquidSlider
import com.kyant.backdrop.catalog.components.LiquidToggle
import com.kyant.backdrop.catalog.components.OriginOSDropdownMenu
import com.kyant.backdrop.catalog.components.defaultOriginOSMenuItems

@Composable
fun OriginOSDropdownContent() {
    val isLightTheme = !isSystemInDarkTheme()
    val bgTheme = if (isLightTheme) Color(0xFFF3F3F5) else Color(0xFF121214)
    val textPrimary = if (isLightTheme) Color(0xFF161616) else Color(0xFFEEEEEE)
    val textSecondary = if (isLightTheme) Color(0xFF86868A) else Color(0xFF9E9EA4)

    var isMenuExpanded by remember { mutableStateOf(false) }
    var isLiquidFusionEnabled by remember { mutableStateOf(true) }
    var animationSpeed by remember { mutableFloatStateOf(1.0f) }

    BackdropDemoScaffold { backdrop ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgTheme)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // --- 1. SYSTEM STATUS BAR (OriginOS 7) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp, start = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicText(
                        text = "09:40",
                        style = TextStyle(
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BasicText(
                            text = "5G",
                            style = TextStyle(
                                color = textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        // 4 Signal Bars
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                            modifier = Modifier.height(10.dp)
                        ) {
                            Box(Modifier.width(2.5.dp).height(3.dp).background(textPrimary, RoundedCornerShape(0.5.dp)))
                            Box(Modifier.width(2.5.dp).height(5.dp).background(textPrimary, RoundedCornerShape(0.5.dp)))
                            Box(Modifier.width(2.5.dp).height(8.dp).background(textPrimary, RoundedCornerShape(0.5.dp)))
                            Box(Modifier.width(2.5.dp).height(10.dp).background(textPrimary, RoundedCornerShape(0.5.dp)))
                        }

                        // Battery Pill [ 100 ]
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(textPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            BasicText(
                                text = "100",
                                style = TextStyle(
                                    color = if (isLightTheme) Color.White else Color.Black,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }
                }

                // --- 2. ALBUMS HEADER & TOP PILL BUTTON ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicText(
                        text = "Albums",
                        style = TextStyle(
                            color = textPrimary,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // OriginOS Dropdown Menu (Morphs directly from the pill, zero layout shift)
                    OriginOSDropdownMenu(
                        isExpanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false },
                        backdrop = backdrop,
                        isLiquidFusionEnabled = isLiquidFusionEnabled,
                        animationSpeedMultiplier = animationSpeed,
                        menuItems = defaultOriginOSMenuItems(),
                        onExpandToggle = { isMenuExpanded = !isMenuExpanded }
                    )
                }

                // --- 3. SECTION: MOST USED ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ChevronUpIcon(textSecondary)
                        BasicText(
                            text = "Most used",
                            style = TextStyle(
                                color = textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    ChevronRightIcon(textSecondary)
                }

                // --- 4. ALBUM PREVIEW TILES (Rich Backdrop Layer) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tile 1: "All" (4-in-1 Collage Preview)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isLightTheme) Color.White else Color(0xFF1E1E20))
                    ) {
                        Column(Modifier.fillMaxSize()) {
                            Row(Modifier.weight(1f).fillMaxWidth()) {
                                Box(
                                    Modifier.weight(1f).fillMaxSize().background(
                                        Brush.linearGradient(listOf(Color(0xFF2C3E50), Color(0xFF000000)))
                                    )
                                )
                                Box(
                                    Modifier.weight(1f).fillMaxSize().background(
                                        Brush.linearGradient(listOf(Color(0xFFE74C3C), Color(0xFF8E44AD)))
                                    )
                                )
                            }
                            Row(Modifier.weight(1f).fillMaxWidth()) {
                                Box(
                                    Modifier.weight(1f).fillMaxSize().background(
                                        Brush.linearGradient(listOf(Color(0xFF16A085), Color(0xFF2ECC71)))
                                    )
                                )
                                Box(
                                    Modifier.weight(1f).fillMaxSize().background(
                                        Brush.linearGradient(listOf(Color(0xFF2980B9), Color(0xFF6DD5FA)))
                                    )
                                )
                            }
                        }

                        // Gradient label overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                                        startY = 100f
                                    )
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Column {
                                BasicText("2,550", style = TextStyle(Color.White.copy(alpha = 0.85f), 12.sp, FontWeight.Medium))
                                BasicText("All", style = TextStyle(Color.White, 16.sp, FontWeight.Bold))
                            }
                        }
                    }

                    // Tile 2: "Camera" (Canyon sunset gradient)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFFD35400),
                                        Color(0xFFE67E22),
                                        Color(0xFFF39C12),
                                        Color(0xFF2980B9)
                                    )
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                                        startY = 100f
                                    )
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Column {
                                BasicText("698", style = TextStyle(Color.White.copy(alpha = 0.85f), 12.sp, FontWeight.Medium))
                                BasicText("Camera", style = TextStyle(Color.White, 16.sp, FontWeight.Bold))
                            }
                        }
                    }

                    // Tile 3: "Favorites" (Spiral architecture)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF00C9FF),
                                        Color(0xFF92FE9D),
                                        Color(0xFF141E30)
                                    )
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                                        startY = 100f
                                    )
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Column {
                                BasicText("142", style = TextStyle(Color.White.copy(alpha = 0.85f), 12.sp, FontWeight.Medium))
                                BasicText("Favorites", style = TextStyle(Color.White, 16.sp, FontWeight.Bold))
                            }
                        }
                    }
                }

                // Promotional caption from OriginOS 7 showcase
                BasicText(
                    text = "while Fluid Motion makes every transition feel natural",
                    modifier = Modifier.padding(top = 18.dp, bottom = 24.dp),
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                // --- 5. INTERACTIVE CONTROL PANEL & PHYSICS TELEMETRY ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isLightTheme) Color.White else Color(0xFF1C1C1E))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                BasicText(
                                    text = "OriginOS 7 Physics Engine",
                                    style = TextStyle(textPrimary, 17.sp, FontWeight.Bold)
                                )
                                BasicText(
                                    text = "VLiquidFusionLayoutV2 + ConversionUtils",
                                    style = TextStyle(Color(0xFF007AFF), 12.sp, FontWeight.Medium)
                                )
                            }

                            LiquidButton(
                                onClick = { isMenuExpanded = !isMenuExpanded },
                                backdrop = backdrop
                            ) {
                                BasicText(
                                    text = if (isMenuExpanded) "Close" else "Open Menu",
                                    style = TextStyle(Color.White, 14.sp, FontWeight.SemiBold)
                                )
                            }
                        }

                        // Toggle: Fluid Fusion vs Standard Scale
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                BasicText(
                                    text = "Liquid Fusion Bridge",
                                    style = TextStyle(textPrimary, 15.sp, FontWeight.SemiBold)
                                )
                                BasicText(
                                    text = if (isLiquidFusionEnabled) "Active: DynamicK metaball neck with decay snap"
                                    else "Disabled: Ordinary rectangle scale animation",
                                    style = TextStyle(textSecondary, 12.sp)
                                )
                            }
                            LiquidToggle(
                                selected = { isLiquidFusionEnabled },
                                onSelect = { isLiquidFusionEnabled = it },
                                backdrop = backdrop
                            )
                        }

                        // Slider: Speed Multiplier (Slow Motion)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                BasicText(
                                    text = "Playback Speed",
                                    style = TextStyle(textPrimary, 15.sp, FontWeight.SemiBold)
                                )
                                BasicText(
                                    text = "${(animationSpeed * 100).toInt()}% ${if (animationSpeed < 0.9f) "(Slow Motion)" else ""}",
                                    style = TextStyle(Color(0xFF007AFF), 13.sp, FontWeight.Bold)
                                )
                            }
                            LiquidSlider(
                                value = { animationSpeed },
                                onValueChange = { animationSpeed = it },
                                valueRange = 0.1f..1.0f,
                                visibilityThreshold = 0.001f,
                                backdrop = backdrop
                            )
                        }

                        // Live Telemetry Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isLightTheme) Color(0xFFF2F2F7) else Color(0xFF2C2C2E))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                BasicText(
                                    text = "ORIGINOS 7 FLUID MORPH TELEMETRY:",
                                    style = TextStyle(textSecondary, 11.sp, FontWeight.Bold)
                                )
                                BasicText(
                                    text = "• 26 Checkpoints: Frame-by-frame trajectory from OriginOS 7 decompilation",
                                    style = TextStyle(textPrimary, 11.sp, fontFamily = FontFamily.Monospace)
                                )
                                BasicText(
                                    text = "• Top-Right Corner: Inward necking (ΔX=-20dp, ΔY=+12dp) ➔ Convex dome ➔ Squircle settling",
                                    style = TextStyle(textPrimary, 11.sp, fontFamily = FontFamily.Monospace)
                                )
                                BasicText(
                                    text = "• Organic Flanks: Left concave waist (28dp) + Right waist (14dp) + Droplet sag (18dp)",
                                    style = TextStyle(textPrimary, 11.sp, fontFamily = FontFamily.Monospace)
                                )
                                BasicText(
                                    text = "• Zero Layout Shift: Anchored pill container with invariant measured bounds (132x44dp)",
                                    style = TextStyle(textPrimary, 11.sp, fontFamily = FontFamily.Monospace)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun ChevronUpIcon(tint: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(14.dp)) {
        val stroke = 2.dp.toPx()
        drawLine(tint, Offset(2.dp.toPx(), 9.dp.toPx()), Offset(7.dp.toPx(), 4.dp.toPx()), stroke)
        drawLine(tint, Offset(7.dp.toPx(), 4.dp.toPx()), Offset(12.dp.toPx(), 9.dp.toPx()), stroke)
    }
}

@Composable
private fun ChevronRightIcon(tint: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(14.dp)) {
        val stroke = 2.dp.toPx()
        drawLine(tint, Offset(4.dp.toPx(), 2.dp.toPx()), Offset(9.dp.toPx(), 7.dp.toPx()), stroke)
        drawLine(tint, Offset(9.dp.toPx(), 7.dp.toPx()), Offset(4.dp.toPx(), 12.dp.toPx()), stroke)
    }
}
