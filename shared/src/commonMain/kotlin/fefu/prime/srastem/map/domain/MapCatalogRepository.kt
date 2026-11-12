package fefu.prime.srastem.map.domain

import kotlinx.coroutines.flow.Flow

interface MapCatalogRepository {
    fun observeMarkers(): Flow<List<MapMarker>>
    suspend fun refreshMarkers(): List<MapMarker> = emptyList()
    suspend fun searchMarkers(query: String): List<MapMarker> = emptyList()
    suspend fun searchSuggestions(query: String): List<String> = emptyList()
}

class ObserveMapMarkers(private val repository: MapCatalogRepository) {
    operator fun invoke(): Flow<List<MapMarker>> = repository.observeMarkers()
    suspend fun refresh(): List<MapMarker> = repository.refreshMarkers()
    suspend fun search(query: String): List<MapMarker> = repository.searchMarkers(query)
    suspend fun suggestions(query: String): List<String> = repository.searchSuggestions(query)
}
