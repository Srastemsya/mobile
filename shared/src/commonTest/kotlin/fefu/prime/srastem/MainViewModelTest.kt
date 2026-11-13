package fefu.prime.srastem

import kotlin.test.Test
import kotlin.test.assertEquals

class MainViewModelTest {
    @Test
    fun backFromFeedReturnsToPreviousTab() {
        val model = MainViewModel()
        model.navigateTo(Destination.MapScreen)
        model.navigateTo(Destination.FeedScreen)
        model.backFromFeed()
        assertEquals(Destination.MapScreen, model.currentDestination.value)
    }

    @Test
    fun eventFeedBackReturnsToHome() {
        val model = MainViewModel()
        model.navigateToFeedWithEventsKind()
        model.backFromFeed()
        assertEquals(Destination.MainScreen, model.currentDestination.value)
    }
}
