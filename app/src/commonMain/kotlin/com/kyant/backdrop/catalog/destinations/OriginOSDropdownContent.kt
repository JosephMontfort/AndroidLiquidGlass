package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.catalog.components.OriginOSDropdownMenu
import com.kyant.backdrop.catalog.components.OriginOSMorphPhase
import com.kyant.backdrop.catalog.components.OriginOSPhysicsTelemetry
import com.kyant.backdrop.catalog.components.defaultOriginOSMenuItems
import glass.app.generated.resources.Res
import glass.app.generated.resources.wallpaper_light
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

@Composable
fun OriginOSDropdownContent() {
    val isDark = isSystemInDarkTheme()
    val textPrimary = if (isDark) Color.White else Color.Black
    val textSecondary = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
    val cardBg = if (isDark) Color(0x33FFFFFF) else Color(0x15000000)

    val backgroundBackdrop = rememberLayerBackdrop()
    val combinedBackdrop = rememberCombinedBackdrop(backgroundBackdrop)

    var isMenuExpanded by remember { mutableStateOf(false) }
    var isGlassEnabled by remember { mutableStateOf(true) }
    var animationSpeed by remember { mutableFloatStateOf(1.0f) }
    var telemetry by remember { mutableStateOf(OriginOSPhysicsTelemetry()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF121214) else Color(0xFFF2F2F4))
    ) {
        // --- LAYER 0: Wallpaper & Photo Album Content (Captured by Backdrop) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backgroundBackdrop)
        ) {
            Image(
                painter = painterResource(Res.drawable.wallpaper_light),
                contentDescription = "Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dim overlay for contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) Color.Black.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.25f))
            )

            // Photo Albums Grid (OriginOS Gallery Layout)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 80.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AlbumCard("Camera", "2,419 photos", cardBg, textPrimary, textSecondary, Modifier.weight(1f))
                    AlbumCard("Screenshots", "342 photos", cardBg, textPrimary, textSecondary, Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AlbumCard("Favorites", "128 photos", cardBg, textPrimary, textSecondary, Modifier.weight(1f))
                    AlbumCard("WeChat", "891 photos", cardBg, textPrimary, textSecondary, Modifier.weight(1f))
                }

                Spacer(Modifier.height(24.dp))

                // Physics Telemetry Card
                PhysicsTelemetryCard(
                    isExpanded = isMenuExpanded,
                    speed = animationSpeed,
                    isGlass = isGlassEnabled,
                    telemetry = telemetry,
                    onSpeedChange = { animationSpeed = it },
                    onGlassToggle = { isGlassEnabled = !isGlassEnabled },
                    onMenuToggle = { isMenuExpanded = !isMenuExpanded }
                )

                Spacer(Modifier.height(120.dp))
            }
        }

        // --- LAYER 1: Full-screen dismiss scrim when dropdown is open ---
        if (isMenuExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isMenuExpanded = false
                    }
            )
        }

        // --- LAYER 2: OriginOS Header & Dropdown Menu ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // OriginOS Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
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
                    // Battery pill
                    Box(
                        modifier = Modifier
                            .size(width = 22.dp, height = 11.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(textPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicText(
                            text = "100",
                            style = TextStyle(
                                color = if (isDark) Color.Black else Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }

            // Albums Header Row + Dropdown Menu
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
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                // 1:1 OriginOS Dropdown Menu
                OriginOSDropdownMenu(
                    isExpanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    backdrop = combinedBackdrop,
                    isGlassEnabled = isGlassEnabled,
                    animationSpeedMultiplier = animationSpeed,
                    menuItems = defaultOriginOSMenuItems(),
                    onExpandToggle = { isMenuExpanded = !isMenuExpanded },
                    onTelemetryUpdate = { telemetry = it }
                )
            }
        }
    }
}

@Composable
private fun AlbumCard(
    title: String,
    count: String,
    bgColor: Color,
    textColor: Color,
    subTextColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(14.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            BasicText(
                text = title,
                style = TextStyle(
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(2.dp))
            BasicText(
                text = count,
                style = TextStyle(
                    color = subTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

@Composable
private fun PhysicsTelemetryCard(
    isExpanded: Boolean,
    speed: Float,
    isGlass: Boolean,
    telemetry: OriginOSPhysicsTelemetry,
    onSpeedChange: (Float) -> Unit,
    onGlassToggle: () -> Unit,
    onMenuToggle: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textPrimary = if (isDark) Color.White else Color.Black
    val textSecondary = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
    val cardColor = if (isDark) Color(0x99202024) else Color(0xCCFFFFFF)

    val phaseBadgeColor = when (telemetry.phase) {
        OriginOSMorphPhase.IDLE_COLLAPSED -> Color(0xFF8E8E93)
        OriginOSMorphPhase.PINCH_TO_BUBBLE -> Color(0xFF007AFF)
        OriginOSMorphPhase.DROPLET_FLIGHT -> Color(0xFFFF9500)
        OriginOSMorphPhase.BLOOM_BOUNCE -> Color(0xFF34C759)
        OriginOSMorphPhase.IDLE_EXPANDED -> Color(0xFF30D158)
        OriginOSMorphPhase.COLLAPSE_BUBBLE -> Color(0xFFFF3B30)
        OriginOSMorphPhase.UNPINCH_ANCHOR -> Color(0xFFAF52DE)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardColor)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicText(
                text = "OriginOS 7 Fluid Morph (1:1)",
                style = TextStyle(
                    color = textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isExpanded) Color(0xFF34C759) else Color(0xFF007AFF))
                    .clickable { onMenuToggle() }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                BasicText(
                    text = if (isExpanded) "Collapse" else "Expand",
                    style = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Live Phase Status Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(phaseBadgeColor.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(phaseBadgeColor)
                )
                BasicText(
                    text = telemetry.phase.label,
                    style = TextStyle(
                        color = phaseBadgeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            BasicText(
                text = "${(telemetry.progressX * 100).roundToInt()}% / ${(telemetry.progressY * 100).roundToInt()}%",
                style = TextStyle(
                    color = phaseBadgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            )
        }

        Spacer(Modifier.height(10.dp))

        // Live Real-Time Telemetry Metrics
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) Color.Black.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.03f))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                BasicText(
                    text = "DIMENSIONS",
                    style = TextStyle(color = textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                )
                BasicText(
                    text = "W: ${telemetry.widthDp.roundToInt()}dp  H: ${telemetry.heightDp.roundToInt()}dp",
                    style = TextStyle(color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                )
            }

            Column {
                BasicText(
                    text = "CORNER RADIUS",
                    style = TextStyle(color = textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                )
                BasicText(
                    text = "R: ${telemetry.cornerRadiusDp.roundToInt()}dp",
                    style = TextStyle(color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                )
            }

            Column {
                BasicText(
                    text = "CENTER (X, Y)",
                    style = TextStyle(color = textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                )
                BasicText(
                    text = "${telemetry.centerXDp.roundToInt()}dp, ${telemetry.centerYDp.roundToInt()}dp",
                    style = TextStyle(color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Speed Buttons & Glass Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicText(
                text = "Speed:",
                style = TextStyle(color = textSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            )

            listOf(0.15f to "0.15x", 0.35f to "0.35x", 1.0f to "1.0x").forEach { (s, label) ->
                val selected = speed == s
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) Color(0xFF007AFF) else Color.Black.copy(alpha = 0.08f))
                        .clickable { onSpeedChange(s) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    BasicText(
                        text = label,
                        style = TextStyle(
                            color = if (selected) Color.White else textPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Glass toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isGlass) Color(0xFF5856D6) else Color.Black.copy(alpha = 0.08f))
                    .clickable { onGlassToggle() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                BasicText(
                    text = if (isGlass) "Vivo Glass ON" else "Glass OFF",
                    style = TextStyle(
                        color = if (isGlass) Color.White else textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Reverse-Engineered Physics Specs Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.04f))
                .padding(12.dp)
        ) {
            BasicText(
                text = "RECOVERED NATIVE / FRAMEWORK PHYSICS:",
                style = TextStyle(
                    color = textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            )
            Spacer(Modifier.height(6.dp))
            BasicText(
                text = "• Phase 1: Pinch to 28dp circle bubble; dots converge 3.5dp->0dp\n• Phase 2: Scale Y (v0=5.0, b=0.60) & Trans Y (v0=8.0) droplet flight\n• Phase 3: Scale X (v0=0.0) blooms into 200dp squircle (R=28dp)\n• Shader: shader_liquid_glass_effect.agsl (RERF_MAG=0.08)",
                style = TextStyle(color = textPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            )
        }
    }
}
