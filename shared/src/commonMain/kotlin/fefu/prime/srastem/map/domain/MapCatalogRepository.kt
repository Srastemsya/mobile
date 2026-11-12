package fefu.prime.srastem.map.domain

import kotlinx.coroutines.flow.Flow

interface MapCatalogRepository {
    fun observeMarkers(): Flow<List<MapMarker>>
}

class ObserveMapMarkers(private val repository: MapCatalogRepository) {
    operator fun invoke(): Flow<List<MapMarker>> = repository.observeMarkers()
}
