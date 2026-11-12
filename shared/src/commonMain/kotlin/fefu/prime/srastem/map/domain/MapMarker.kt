package fefu.prime.srastem.map.domain

/** Geographic coordinate in decimal degrees, independent of any map renderer. */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

enum class MapMarkerType { Event, Place, Activity }

/** Visual category icon used by the shared map pin component. */
enum class MapMarkerIcon { Event, Place, Activity, Quiz }

/** A map item that can later be supplied by the events and places repositories. */
data class MapMarker(
    val id: String,
    val location: GeoPoint,
    val type: MapMarkerType,
    val title: String,
    val subtitle: String,
    val detail: String,
    val price: String,
    val icon: MapMarkerIcon = when (type) {
        MapMarkerType.Event -> MapMarkerIcon.Event
        MapMarkerType.Place -> MapMarkerIcon.Place
        MapMarkerType.Activity -> MapMarkerIcon.Activity
    },
)
