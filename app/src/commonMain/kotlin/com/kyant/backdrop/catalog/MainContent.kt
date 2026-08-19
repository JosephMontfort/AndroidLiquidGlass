package com.kyant.backdrop.catalog

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.kyant.backdrop.catalog.destinations.*
import com.kyant.backdrop.catalog.utils.BackHandler

@Composable
fun MainContent() {
    val isLightTheme = !isSystemInDarkTheme()

    CompositionLocalProvider(
        LocalIndication provides ripple(color = if (isLightTheme) Color.Black else Color.White)
    ) {
        var destination by rememberSaveable { mutableStateOf(CatalogDestination.Home) }

        when (destination) {
            CatalogDestination.Home -> HomeContent(onNavigate = { destination = it })
            CatalogDestination.Buttons -> ButtonsContent()
            CatalogDestination.Toggle -> ToggleContent()
            CatalogDestination.Slider -> SliderContent()
            CatalogDestination.BottomTabs -> BottomTabsContent()
            CatalogDestination.Dialog -> DialogContent()
            CatalogDestination.MorphedDropdownMenu -> MorphedDropdownMenuContent()
            CatalogDestination.DropdownMenu -> DropdownMenuContent()
            CatalogDestination.FlippedMorphDropdown -> FlippedMorphDropdownContent()
            CatalogDestination.LockScreen -> LockScreenContent()
            CatalogDestination.ControlCenter -> ControlCenterContent()
            CatalogDestination.Magnifier -> MagnifierContent()
            CatalogDestination.GlassPlayground -> GlassPlaygroundContent()
            CatalogDestination.AdaptiveLuminanceGlass -> AdaptiveLuminanceGlassContent()
            CatalogDestination.ProgressiveBlur -> ProgressiveBlurContent()
            CatalogDestination.ScrollContainer -> ScrollContainerContent()
            CatalogDestination.LazyScrollContainer -> LazyScrollContainerContent()
        }

        BackHandler(destination != CatalogDestination.Home) {
            destination = CatalogDestination.Home
        }
    }
}
