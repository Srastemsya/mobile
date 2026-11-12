package fefu.prime.srastem.map.presentation

import com.russhwolf.settings.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fefu.prime.srastem.map.domain.MapMarker
import fefu.prime.srastem.map.domain.ObserveMapMarkers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class MapCatalogUiState(
    val eventMarkers: List<MapMarker> = emptyList(),
    val searchResults: List<MapMarker> = emptyList(),
    val searchSuggestions: List<String> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val error: String? = null,
)

class MapCatalogViewModel(
    private val observeMapMarkers: ObserveMapMarkers,
    private val settings: Settings = Settings(),
) : ViewModel() {
    private val _state = MutableStateFlow(
        MapCatalogUiState(recentSearches = settings.getString(KEY_RECENT_SEARCHES, "").split('|').filter(String::isNotBlank).take(5)),
    )
    val state: StateFlow<MapCatalogUiState> = _state.asStateFlow()
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            observeMapMarkers().catch { error ->
                if (error is CancellationException) throw error
                _state.value = _state.value.copy(error = error.message ?: "Не удалось обновить события карты")
            }.collect { markers -> _state.value = _state.value.copy(eventMarkers = markers) }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(eventMarkers = observeMapMarkers.refresh())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = _state.value.copy(error = error.message ?: "Не удалось обновить карту")
            }
        }
    }

    fun search(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.value = _state.value.copy(searchResults = emptyList(), searchSuggestions = emptyList())
            return
        }
        _state.value = _state.value.copy(searchResults = emptyList(), searchSuggestions = emptyList(), error = null)
        searchJob = viewModelScope.launch {
            delay(250)
            try {
                val normalized = query.trim()
                val results = observeMapMarkers.search(normalized)
                val suggestions = observeMapMarkers.suggestions(normalized)
                _state.value = _state.value.copy(searchResults = results, searchSuggestions = suggestions, error = null)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = _state.value.copy(error = error.message ?: "Не удалось выполнить поиск")
            }
        }
    }

    fun recordSearch(query: String) {
        val normalized = query.trim().takeIf(String::isNotBlank) ?: return
        val recent = (listOf(normalized) + _state.value.recentSearches.filterNot { it.equals(normalized, ignoreCase = true) })
            .take(5)
        _state.value = _state.value.copy(recentSearches = recent)
        settings.putString(KEY_RECENT_SEARCHES, recent.joinToString("|"))
    }

    private companion object {
        const val KEY_RECENT_SEARCHES = "map_recent_searches"
    }
}
