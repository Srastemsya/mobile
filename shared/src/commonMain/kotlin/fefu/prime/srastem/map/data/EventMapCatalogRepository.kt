package fefu.prime.srastem.map.data

import fefu.prime.srastem.events.domain.EventCategory
import fefu.prime.srastem.events.domain.EventRepository
import fefu.prime.srastem.map.domain.GeoPoint
import fefu.prime.srastem.map.domain.MapCatalogRepository
import fefu.prime.srastem.map.domain.MapMarker
import fefu.prime.srastem.map.domain.MapMarkerIcon
import fefu.prime.srastem.map.domain.MapMarkerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Projects catalog event records into map markers while retaining the shared event ID. */
class EventMapCatalogRepository(private val events: EventRepository) : MapCatalogRepository {
    override fun observeMarkers(): Flow<List<MapMarker>> = events.observeEvents().map { items ->
        items.filterNot { it.isPast }.map { event ->
            val categoryIcon = when (event.category) {
                EventCategory.GAMES -> MapMarkerIcon.Quiz
                EventCategory.ACTIVE -> MapMarkerIcon.Activity
                else -> MapMarkerIcon.Event
            }
            val places =
                event.capacity?.let { "Записалось ${event.participants.size} · осталось ${event.remainingPlaces} места" }
                    ?: "Свободный вход"
            MapMarker(
                id = event.id,
                location = GeoPoint(event.latitude, event.longitude),
                type = MapMarkerType.Event,
                title = event.title,
                subtitle = event.startsAt,
                detail = places,
                price = if (event.priceRub == 0) "Бесплатно" else "≈ ${event.priceRub} ₽",
                icon = categoryIcon,
            )
        }
    }
}
