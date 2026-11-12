package fefu.prime.srastem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.srastemsya.uikit.SrTab
import ru.srastemsya.uikit.SrTabBar
import ru.srastemsya.uikit.SrGlassMode

@Composable
fun BottomNavigationBar(
    selectedDestination: Destination,
    modifier: Modifier = Modifier,
    onNavigate: (Destination) -> Unit,
    mode: SrGlassMode = SrGlassMode.Liquid,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        SrTabBar(
            selected = selectedDestination.toSrTab(),
            onSelect = { onNavigate(it.toDestination()) },
            mode = mode,
            translucentFallback = selectedDestination == Destination.MapScreen,
            tabs = if (selectedDestination == Destination.MainScreen) {
                listOf(SrTab.Home, SrTab.Map, SrTab.Events, SrTab.Profile)
            } else SrTab.entries,
        )
    }
}

private fun Destination.toSrTab(): SrTab = when (this) {
    Destination.MainScreen -> SrTab.Home
    Destination.MapScreen -> SrTab.Map
    Destination.FeedScreen -> SrTab.Feed
    Destination.RoomScreen, Destination.AiScreen -> SrTab.Feed
    Destination.EventsScreen -> SrTab.Events
    Destination.Profile -> SrTab.Profile
}

private fun SrTab.toDestination(): Destination = when (this) {
    SrTab.Home -> Destination.MainScreen
    SrTab.Map -> Destination.MapScreen
    SrTab.Feed -> Destination.FeedScreen
    SrTab.Events -> Destination.EventsScreen
    SrTab.Profile -> Destination.Profile
}
