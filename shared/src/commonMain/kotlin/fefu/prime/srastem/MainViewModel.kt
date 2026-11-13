package fefu.prime.srastem

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _currentDestination = MutableStateFlow<Destination>(Destination.MainScreen)
    val currentDestination: StateFlow<Destination> = _currentDestination.asStateFlow()

    /**
     * Вход в ленту кнопкой «Свайпать события» с Главной (AC-05): экран выбора режима
     * открывается с предвыбранным `kind=EVENT`. Сбрасывается любым другим переходом,
     * включая тап вкладки «Лента».
     */
    private val _feedEventsEntry = MutableStateFlow(false)
    val feedEventsEntry: StateFlow<Boolean> = _feedEventsEntry.asStateFlow()

    private var beforeFeed: Destination = Destination.MainScreen

    fun navigateTo(destination: Destination) {
        if (destination == Destination.FeedScreen && _currentDestination.value != destination) beforeFeed =
            _currentDestination.value
        if (destination != Destination.FeedScreen) _feedEventsEntry.value = false
        _currentDestination.value = destination
    }

    fun backFromFeed() = navigateTo(beforeFeed)

    fun navigateToFeedWithEventsKind() {
        if (_currentDestination.value != Destination.FeedScreen) beforeFeed =
            _currentDestination.value
        _feedEventsEntry.value = true
        _currentDestination.value = Destination.FeedScreen
    }
}
