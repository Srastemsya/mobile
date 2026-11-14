package fefu.prime.srastem.events.data

import fefu.prime.srastem.backend.BackendApi
import fefu.prime.srastem.backend.BackendException
import fefu.prime.srastem.backend.jsonObjectOf
import fefu.prime.srastem.backend.string
import fefu.prime.srastem.events.domain.EventAdmission
import fefu.prime.srastem.events.domain.EventCatalogCategory
import fefu.prime.srastem.events.domain.EventCategory
import fefu.prime.srastem.events.domain.EventDraft
import fefu.prime.srastem.events.domain.EventParticipant
import fefu.prime.srastem.events.domain.EventRepository
import fefu.prime.srastem.events.domain.SrEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class BackendEventRepository(private val api: BackendApi) : EventRepository {
    private val events = MutableStateFlow<List<SrEvent>>(emptyList())
    private var pendingCreateKey: String? = null

    override val currentUserId: String get() = api.userId()
    override fun observeEvents(): Flow<List<SrEvent>> = events.asStateFlow()

    override suspend fun getEventCategories(): List<EventCatalogCategory> =
        api.get("/v1/catalog/categories").jsonObject["items"]?.jsonArray.orEmpty()
            .mapNotNull { element ->
                val category = element.jsonObject
                val kinds =
                    category["kinds"]?.jsonArray?.map { it.jsonPrimitive.contentOrNull }.orEmpty()
                if ("EVENT" !in kinds) null else EventCatalogCategory(
                    id = category.string("id").orEmpty(),
                    label = category.string("label").orEmpty(),
                )
            }

    override suspend fun refreshEvents() {
        val cards = mutableListOf<SrEvent>()
        var cursor: String? = null
        var restartedAfterExpiredCursor = false
        while (true) {
            val path = buildString {
                append("/v1/events")
                cursor?.let { append("?cursor=").append(it.encodeQueryComponent()) }
            }
            val response = try {
                api.get(path).jsonObject
            } catch (error: BackendException) {
                if (error.code == "CURSOR_EXPIRED" && !restartedAfterExpiredCursor) {
                    cards.clear()
                    cursor = null
                    restartedAfterExpiredCursor = true
                    continue
                }
                throw error
            }
            cards += response["items"]?.jsonArray.orEmpty()
                .map { card -> eventFromCard(card.jsonObject) }
            cursor = response.string("nextCursor")
            if (cursor == null) break
        }
        events.value = cards
    }

    override suspend fun getEvent(id: String): SrEvent? {
        val response = api.get("/v1/events/$id").jsonObject
        var event = parseEvent(response)
        if (response["viewer"]?.jsonObject?.string("role") == "ORGANIZER" && event.capacity != null) {
            val registrations = mutableListOf<kotlinx.serialization.json.JsonElement>()
            var cursor: String? = null
            do {
                val path = buildString {
                    append("/v1/events/$id/registrations")
                    cursor?.let { append("?cursor=").append(it.encodeQueryComponent()) }
                }
                val page = api.get(path).jsonObject
                registrations += page["items"]?.jsonArray.orEmpty()
                cursor = page.string("nextCursor")
            } while (cursor != null)
            val participants = registrations.mapNotNull { item ->
                val row = item.jsonObject
                val registration = row["registration"]?.jsonObject ?: return@mapNotNull null
                val participant = row["participant"]?.jsonObject ?: return@mapNotNull null
                EventParticipant(
                    id = participant.string("id").orEmpty(),
                    name = participant.string("displayName") ?: "Участник",
                    registrationId = registration.string("id"),
                )
            }
            event = event.copy(participants = participants)
        }
        events.value = listOf(event) + events.value.filterNot { it.id == event.id }
        return event
    }

    override suspend fun createEvent(draft: EventDraft): SrEvent {
        val start = parseDraftDate(draft.startsAt)
        val capacity = draft.capacity.toIntOrNull()
        val priceRub = draft.priceRub.toIntOrNull() ?: 0
        val categoryId = draft.categoryId
        val admission = if (draft.admission == EventAdmission.FREE) {
            JsonObject(mapOf("type" to JsonPrimitive("OPEN")))
        } else {
            require(capacity != null && capacity > 0) { "Количество мест должно быть больше нуля" }
            require(priceRub == 0) { "Платная запись пока не поддерживается сервером" }
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("REGISTRATION"),
                    "capacity" to JsonPrimitive(capacity)
                )
            )
        }
        val price = if (draft.priceRub.isBlank()) JsonNull else buildJsonObject {
            put("amountMinor", JsonPrimitive(priceRub * 100))
            put("currency", JsonPrimitive("RUB"))
        }
        val body = buildJsonObject {
            put("title", JsonPrimitive(draft.title.trim()))
            put(
                "description",
                JsonPrimitive(draft.description.trim().ifBlank { draft.title.trim() })
            )
            put("categoryId", JsonPrimitive(categoryId))
            put("location", buildJsonObject {
                put("latitude", JsonPrimitive(draft.latitude))
                put("longitude", JsonPrimitive(draft.longitude))
                put("address", JsonPrimitive(draft.address.trim()))
            })
            put("price", price)
            put("startsAt", JsonPrimitive(start))
            put("timeZone", JsonPrimitive("Asia/Vladivostok"))
            put("admission", admission)
        }
        val key =
            pendingCreateKey ?: api.idempotencyKey("publish-event").also { pendingCreateKey = it }
        try {
            val created = api.post("/v1/events", body, key).value?.jsonObject
                ?: error("Сервер не вернул событие")
            pendingCreateKey = null
            api.clearIdempotencyKey("publish-event")
            val event = parseEvent(created)
            events.value = listOf(event) + events.value.filterNot { it.id == event.id }
            return event
        } catch (error: BackendException) {
            if (error.status in 400..499) {
                pendingCreateKey = null
                api.clearIdempotencyKey("publish-event")
            }
            throw error
        }
    }

    override suspend fun joinEvent(eventId: String, userId: String) {
        val scope = "register-event:$eventId"
        val response = try {
            api.postEmpty(
                "/v1/events/$eventId/registrations",
                api.idempotencyKey(scope)
            ).value?.jsonObject
        } catch (error: BackendException) {
            if (error.status in 400..499) api.clearIdempotencyKey(scope)
            throw error
        }
        api.clearIdempotencyKey(scope)
        val count =
            response?.get("admission")?.jsonObject?.get("confirmedCount")?.jsonPrimitive?.intOrNull
        getEvent(eventId)?.let { current ->
            events.value = events.value.map {
                if (it.id == eventId) current.copy(
                    isAttending = true,
                    canRegister = false,
                    confirmedCount = count ?: current.confirmedCount + 1
                ) else it
            }
        }
    }

    override suspend fun leaveEvent(eventId: String, userId: String) {
        throw UnsupportedOperationException("Серверный контракт пока не поддерживает отмену собственной записи")
    }

    override suspend fun removeParticipant(
        eventId: String,
        organizerId: String,
        participantId: String
    ) {
        val participant =
            events.value.firstOrNull { it.id == eventId }?.participants?.firstOrNull { it.id == participantId }
                ?: error("Участник больше не найден")
        val registrationId =
            participant.registrationId ?: error("Не удалось определить запись участника")
        api.delete("/v1/events/$eventId/registrations/$registrationId")
        getEvent(eventId)
    }

    override suspend fun deleteEvent(eventId: String, organizerId: String) {
        throw UnsupportedOperationException("Удаление события пока не поддерживается серверным API")
    }

    private fun parseEvent(json: JsonObject): SrEvent {
        val card = json
        val admissionJson = card["admission"]?.jsonObject
        val admission =
            if (admissionJson?.string("type") == "REGISTRATION") EventAdmission.BOOKING else EventAdmission.FREE
        val viewer = card["viewer"]?.jsonObject
        val registration = viewer?.get("registration")?.takeUnless { it == JsonNull }?.jsonObject
        val categoryId = card.string("categoryId").orEmpty()
        val location = card["location"]?.jsonObject
        val count = admissionJson?.get("confirmedCount")?.jsonPrimitive?.intOrNull ?: 0
        val capacity = admissionJson?.get("capacity")?.jsonPrimitive?.intOrNull
        val minor =
            card["price"]?.takeUnless { it == JsonNull }?.jsonObject?.get("amountMinor")?.jsonPrimitive?.intOrNull
        val start = card.string("startsAt").orEmpty()
        return SrEvent(
            id = card.string("id") ?: card["ref"]?.jsonObject?.string("id").orEmpty(),
            title = card.string("title").orEmpty(),
            category = category(categoryId),
            startsAt = displayDate(start),
            address = location?.string("address").orEmpty(),
            description = card.string("description") ?: "",
            admission = admission,
            capacity = capacity,
            priceRub = (minor ?: 0) / 100,
            organizerId = card.string("organizerId").orEmpty(),
            organizerName = "Организатор",
            coverUri = card["cover"]?.takeUnless { it == JsonNull }?.jsonObject?.string("url")
                ?.let(api::resolveMediaUrl),
            latitude = location?.get("latitude")?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
                ?: 43.1155,
            longitude = location?.get("longitude")?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
                ?: 131.8855,
            participants = emptyList(),
            isPast = card.string("phase") == "PAST",
            confirmedCount = count,
            isAttending = viewer?.string("role") == "ATTENDEE" || registration != null,
            canRegister = viewer?.get("canRegister")?.jsonPrimitive?.contentOrNull == "true",
            canCancelRegistration = false,
            canDelete = false,
        )
    }

    private fun eventFromCard(card: JsonObject): SrEvent {
        val admission = card["admission"]?.jsonObject
        val price =
            card["price"]?.takeUnless { it == JsonNull }?.jsonObject?.get("amountMinor")?.jsonPrimitive?.intOrNull
        val location = card["location"]?.jsonObject
        val start = card.string("startsAt").orEmpty()
        return SrEvent(
            id = card["ref"]?.jsonObject?.string("id").orEmpty(),
            title = card.string("title").orEmpty(),
            category = category(card.string("categoryId").orEmpty()),
            startsAt = displayDate(start),
            address = location?.string("address").orEmpty(),
            description = "",
            admission = if (admission?.string("type") == "REGISTRATION") EventAdmission.BOOKING else EventAdmission.FREE,
            capacity = admission?.get("capacity")?.jsonPrimitive?.intOrNull,
            priceRub = (price ?: 0) / 100,
            organizerId = "",
            organizerName = "Организатор",
            coverUri = card["cover"]?.takeUnless { it == JsonNull }?.jsonObject?.string("url")
                ?.let(api::resolveMediaUrl),
            latitude = location?.get("latitude")?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
                ?: 43.1155,
            longitude = location?.get("longitude")?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
                ?: 131.8855,
            confirmedCount = admission?.get("confirmedCount")?.jsonPrimitive?.intOrNull ?: 0,
            canRegister = admission?.string("type") == "REGISTRATION",
            canCancelRegistration = false,
            canDelete = false,
        )
    }

    private fun category(id: String) = when (id) {
        "outdoors" -> EventCategory.WALK
        "food" -> EventCategory.FOOD
        "games" -> EventCategory.GAMES
        else -> EventCategory.OTHER
    }

    private fun parseDraftDate(value: String): String {
        val match = Regex("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4}).*?(\\d{1,2}):(\\d{2})").find(value)
            ?: error("Укажите дату и время события")
        val (day, month, year, hour, minute) = match.destructured
        return "${year.padStart(4, '0')}-${month.padStart(2, '0')}-${
            day.padStart(
                2,
                '0'
            )
        }T${hour.padStart(2, '0')}:$minute:00+10:00"
    }

    private fun displayDate(value: String): String {
        if (value.length < 16) return value
        return "${value.substring(8, 10)}.${value.substring(5, 7)}.${
            value.substring(
                0,
                4
            )
        } · ${value.substring(11, 16)}"
    }
}

private fun String.encodeQueryComponent(): String = buildString {
    encodeToByteArray().forEach { byte ->
        val value = byte.toInt() and 0xff
        if ((value in 0x41..0x5a) || (value in 0x61..0x7a) || (value in 0x30..0x39) || value == 0x2d || value == 0x2e || value == 0x5f || value == 0x7e) {
            append(value.toChar())
        } else {
            append('%')
            append("0123456789ABCDEF"[value ushr 4])
            append("0123456789ABCDEF"[value and 0x0f])
        }
    }
}
