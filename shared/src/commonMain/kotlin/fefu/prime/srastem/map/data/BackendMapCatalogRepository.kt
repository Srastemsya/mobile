package fefu.prime.srastem.map.data

import fefu.prime.srastem.backend.BackendApi
import fefu.prime.srastem.backend.string
import fefu.prime.srastem.events.domain.eventAttendanceSummary
import fefu.prime.srastem.map.domain.GeoPoint
import fefu.prime.srastem.map.domain.MapCatalogRepository
import fefu.prime.srastem.map.domain.MapMarker
import fefu.prime.srastem.map.domain.MapMarkerIcon
import fefu.prime.srastem.map.domain.MapMarkerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class BackendMapCatalogRepository(private val api: BackendApi) : MapCatalogRepository {
    override fun observeMarkers(): Flow<List<MapMarker>> = flow { emit(refreshMarkers()) }

    override suspend fun refreshMarkers(): List<MapMarker> {
        val markers = mutableListOf<MapMarker>()
        listOf("EVENT", "PLACE", "ACTIVITY").forEach { kind ->
            var cursor: String? = null
            do {
                val query = buildString {
                    append("/v1/catalog/map?kind=").append(kind)
                    append("&bbox=131.6,42.8,132.1,43.3")
                    cursor?.let { append("&cursor=").append(it.encodeQueryComponent()) }
                }
                val page = api.get(query).jsonObject
                markers += page["items"]?.jsonArray.orEmpty().mapNotNull { cardElement ->
                    val card = cardElement.jsonObject
                    val ref = card["ref"]?.jsonObject ?: return@mapNotNull null
                    val location = card["location"]?.jsonObject ?: return@mapNotNull null
                    val categoryId = card.string("categoryId").orEmpty()
                    val type = when (kind) {
                        "EVENT" -> MapMarkerType.Event
                        "ACTIVITY" -> MapMarkerType.Activity
                        else -> MapMarkerType.Place
                    }
                    val icon = when {
                        categoryId == "culture" && type == MapMarkerType.Event -> MapMarkerIcon.Quiz
                        type == MapMarkerType.Event -> MapMarkerIcon.Event
                        type == MapMarkerType.Activity -> MapMarkerIcon.Activity
                        else -> MapMarkerIcon.Place
                    }
                    val admission = card["admission"]?.takeUnless { it == JsonNull }?.jsonObject
                    val seats = admission?.get("capacity")?.jsonPrimitive?.intOrNull
                    val confirmed = admission?.get("confirmedCount")?.jsonPrimitive?.intOrNull ?: 0
                    val remaining = admission?.get("remainingCount")?.jsonPrimitive?.intOrNull
                    val date = card.string("startsAt")?.let(::displayDate).orEmpty()
                    val price = card["price"]?.takeUnless { it == JsonNull }?.jsonObject
                        ?.get("amountMinor")?.jsonPrimitive?.intOrNull
                    MapMarker(
                        id = ref.string("id").orEmpty(),
                        location = GeoPoint(
                            latitude = location["latitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: return@mapNotNull null,
                            longitude = location["longitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: return@mapNotNull null,
                        ),
                        type = type,
                        title = card.string("title").orEmpty(),
                        subtitle = date,
                        detail = eventAttendanceSummary(confirmed, seats, remaining) ?: "Свободный вход",
                        price = when {
                            price == null -> "Цена неизвестна"
                            price == 0 -> "Бесплатно"
                            else -> "≈ ${price / 100} ₽"
                        },
                        icon = icon,
                        confirmedCount = admission?.get("confirmedCount")?.jsonPrimitive?.intOrNull,
                        capacity = seats,
                        remainingCount = remaining,
                    )
                }
                cursor = page.string("nextCursor")
            } while (cursor != null)
        }
        return markers
    }

    override suspend fun searchMarkers(query: String): List<MapMarker> {
        val markers = mutableListOf<MapMarker>()
        var cursor: String? = null
        do {
            val path = buildString {
                append("/v1/catalog/search?q=").append(query.encodeQueryComponent())
                cursor?.let { append("&cursor=").append(it.encodeQueryComponent()) }
            }
            val page = api.get(path).jsonObject
            markers += page["items"]?.jsonArray.orEmpty().mapNotNull { element ->
                val card = element.jsonObject
                val ref = card["ref"]?.jsonObject ?: return@mapNotNull null
                val kind = ref.string("kind") ?: return@mapNotNull null
                val location = card["location"]?.jsonObject ?: return@mapNotNull null
                val categoryId = card.string("categoryId").orEmpty()
                val type = when (kind) {
                    "EVENT" -> MapMarkerType.Event
                    "ACTIVITY" -> MapMarkerType.Activity
                    else -> MapMarkerType.Place
                }
                val admission = card["admission"]?.takeUnless { it == JsonNull }?.jsonObject
                val seats = admission?.get("capacity")?.jsonPrimitive?.intOrNull
                val confirmed = admission?.get("confirmedCount")?.jsonPrimitive?.intOrNull ?: 0
                val remaining = admission?.get("remainingCount")?.jsonPrimitive?.intOrNull
                val price = card["price"]?.takeUnless { it == JsonNull }?.jsonObject
                    ?.get("amountMinor")?.jsonPrimitive?.intOrNull
                MapMarker(
                    id = ref.string("id").orEmpty(),
                    location = GeoPoint(
                        latitude = location["latitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: return@mapNotNull null,
                        longitude = location["longitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: return@mapNotNull null,
                    ),
                    type = type,
                    title = card.string("title").orEmpty(),
                    subtitle = card.string("startsAt")?.let(::displayDate) ?: location.string("address").orEmpty(),
                    detail = eventAttendanceSummary(confirmed, seats, remaining) ?: "Свободный вход",
                    price = when {
                        price == null -> "Цена неизвестна"
                        price == 0 -> "Бесплатно"
                        else -> "≈ ${price / 100} ₽"
                    },
                    icon = when {
                        categoryId == "culture" && type == MapMarkerType.Event -> MapMarkerIcon.Quiz
                        type == MapMarkerType.Event -> MapMarkerIcon.Event
                        type == MapMarkerType.Activity -> MapMarkerIcon.Activity
                        else -> MapMarkerIcon.Place
                    },
                    confirmedCount = admission?.get("confirmedCount")?.jsonPrimitive?.intOrNull,
                    capacity = seats,
                    remainingCount = remaining,
                )
            }
            cursor = page.string("nextCursor")
        } while (cursor != null)
        return markers
    }

    override suspend fun searchSuggestions(query: String): List<String> {
        val path = "/v1/catalog/search/suggestions?q=${query.encodeQueryComponent()}"
        return api.get(path).jsonObject["items"]?.jsonArray.orEmpty().mapNotNull { element ->
            element.jsonObject.string("label")?.takeIf(String::isNotBlank)
        }
    }

    private fun displayDate(value: String): String {
        if (value.length < 16) return value
        return "${value.substring(8, 10)}.${value.substring(5, 7)} · ${value.substring(11, 16)}"
    }
}

private fun String.encodeQueryComponent(): String = buildString {
    encodeToByteArray().forEach { byte ->
        val value = byte.toInt() and 0xff
        if ((value in 0x41..0x5a) || (value in 0x61..0x7a) || (value in 0x30..0x39) || value in listOf(0x2d, 0x2e, 0x5f, 0x7e)) {
            append(value.toChar())
        } else {
            append('%')
            append("0123456789ABCDEF"[value ushr 4])
            append("0123456789ABCDEF"[value and 0x0f])
        }
    }
}
