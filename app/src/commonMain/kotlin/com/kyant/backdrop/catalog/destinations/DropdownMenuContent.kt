package com.kyant.backdrop.catalog.destinations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.catalog.BackdropDemoScaffold
import com.kyant.backdrop.catalog.ReceiveIcon
import com.kyant.backdrop.catalog.SendIcon
import com.kyant.backdrop.catalog.ShareFilledIcon
import com.kyant.backdrop.catalog.SwapIcon
import com.kyant.backdrop.catalog.components.ExpandableGlassMenu
import kotlinx.coroutines.launch

@Composable
fun DropdownMenuContent() {
    val animationScope = rememberCoroutineScope()
    val progressAnimation = remember { Animatable(0f) }
    var expanded by remember { mutableStateOf(false) }

    BackdropDemoScaffold { backdrop ->
        Box(
            Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 96.dp)
        ) {
            ExpandableGlassMenu(
                backdrop = backdrop,
                progress = progressAnimation.value,
                onClick = {
                    val opening = !expanded
                    expanded = opening
                    val target = if (opening) 1f else 0f

                    animationScope.launch {
                        // Drive ONE progress value. The component intentionally
                        // consumes the raw spring value, including overshoot, so
                        // width, height, scale, blur, label travel and opacity all
                        // morph together as one physical object.
                        progressAnimation.animateTo(
                            targetValue = target,
                            animationSpec = spring(
                                dampingRatio = 0.50f,
                                stiffness = 900f,
                                visibilityThreshold = 0.0005f,
                            ),
                        )
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd),
                content = {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DropdownRow(SendIcon, "Send")
                        DropdownRow(SwapIcon, "Swap")
                        DropdownRow(ReceiveIcon, "Receive")
                    }
                },
                label = {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberVectorPainter(ShareFilledIcon),
                            contentDescription = "Open menu",
                            modifier = Modifier.size(22.dp),
                            colorFilter = ColorFilter.tint(Color.Black)
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun DropdownRow(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(5.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberVectorPainter(icon),
                contentDescription = title,
                modifier = Modifier.size(21.dp),
                colorFilter = ColorFilter.tint(Color.Black)
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BasicText(
                title,
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            BasicText(
                "This is a sample text description",
                style = TextStyle(
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            )
        }
    }
}
