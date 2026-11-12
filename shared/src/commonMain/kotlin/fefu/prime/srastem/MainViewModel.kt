package fefu.prime.srastem

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _currentDestination = MutableStateFlow<Destination>(Destination.MainScreen)
    val currentDestination: StateFlow<Destination> = _currentDestination.asStateFlow()

    fun navigateTo(destination: Destination) {
        _currentDestination.value = destination
    }
}
