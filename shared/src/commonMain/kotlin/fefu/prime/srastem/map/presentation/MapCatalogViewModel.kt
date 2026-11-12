package fefu.prime.srastem.map.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fefu.prime.srastem.map.domain.MapMarker
import fefu.prime.srastem.map.domain.ObserveMapMarkers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class MapCatalogUiState(
    val eventMarkers: List<MapMarker> = emptyList(),
    val error: String? = null,
)

class MapCatalogViewModel(observeMapMarkers: ObserveMapMarkers) : ViewModel() {
    private val _state = MutableStateFlow(MapCatalogUiState())
    val state: StateFlow<MapCatalogUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeMapMarkers().catch { error ->
                if (error is CancellationException) throw error
                _state.value = _state.value.copy(error = error.message ?: "Не удалось обновить события карты")
            }.collect { markers -> _state.value = MapCatalogUiState(eventMarkers = markers) }
        }
    }
}
