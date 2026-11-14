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

@Composable
fun BottomNavigationBar(
    selectedDestination: Destination,
    modifier: Modifier = Modifier,
    onNavigate: (Destination) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(vertical = 8.dp),
    ) {
        SrTabBar(
            selected = selectedDestination.toSrTab(),
            onSelect = { onNavigate(it.toDestination()) },
            tabs = listOf(SrTab.Home, SrTab.Map, SrTab.Events, SrTab.Profile),
        )
    }
}

private fun Destination.toSrTab(): SrTab = when (this) {
    Destination.MainScreen -> SrTab.Home
    Destination.MapScreen -> SrTab.Map
    // Swipe mode is opened from the Home screen; there is no Feed tab to select.
    Destination.FeedScreen -> SrTab.Home
    Destination.RoomScreen, Destination.AiScreen -> SrTab.Events
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
