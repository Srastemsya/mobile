package fefu.prime.srastem.profile.data

import com.russhwolf.settings.Settings
import fefu.prime.srastem.backend.BackendApi
import fefu.prime.srastem.backend.BackendException
import fefu.prime.srastem.backend.string
import fefu.prime.srastem.events.domain.EventRepository
import fefu.prime.srastem.profile.domain.EventStatus
import fefu.prime.srastem.profile.domain.ProfileDetails
import fefu.prime.srastem.profile.domain.ProfileEvent
import fefu.prime.srastem.profile.domain.ProfileRepository
import fefu.prime.srastem.profile.domain.ProfileSavedKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class SettingsProfileRepository(
    private val settings: Settings,
    private val api: BackendApi,
    private val eventRepository: EventRepository,
) : ProfileRepository {
    override suspend fun getProfile() = ProfileDetails(
        name = settings.getString(
            KEY_NAME,
            settings.getString("registration_profile_name", "Ренат")
        ),
        city = settings.getString(KEY_CITY, "Владивосток"),
        birthDate = settings.getString(
            KEY_BIRTH_DATE,
            settings.getString("registration_profile_birth_date", "24.08.2001")
        ),
        about = settings.getString(KEY_ABOUT, ""),
        photoUri = settings.getStringOrNull(KEY_PHOTO),
        photoScale = settings.getFloat(KEY_PHOTO_SCALE, 1.24f),
        photoOffsetX = settings.getFloat(KEY_PHOTO_OFFSET_X, 0f),
        photoOffsetY = settings.getFloat(KEY_PHOTO_OFFSET_Y, 0f),
    )

    override suspend fun saveProfile(profile: ProfileDetails) {
        settings.putString(KEY_NAME, profile.name)
        settings.putString(KEY_CITY, profile.city)
        settings.putString(KEY_ABOUT, profile.about)
        profile.photoUri?.let { settings.putString(KEY_PHOTO, it) } ?: settings.remove(KEY_PHOTO)
        settings.putFloat(KEY_PHOTO_SCALE, profile.photoScale)
        settings.putFloat(KEY_PHOTO_OFFSET_X, profile.photoOffsetX)
        settings.putFloat(KEY_PHOTO_OFFSET_Y, profile.photoOffsetY)
    }

    override suspend fun getSavedEvents(): List<ProfileEvent> {
        val removed =
            settings.getString(KEY_REMOVED_SAVED, "").split('|').filter(String::isNotBlank).toSet()
        return readAddedEvents().distinctBy { it.id }.filterNot { it.id in removed }
    }

    override suspend fun saveEvent(event: ProfileEvent) {
        val events = readAddedEvents().filterNot { it.id == event.id } + event
        writeAddedEvents(events)
        val removed =
            settings.getString(KEY_REMOVED_SAVED, "").split('|').filter(String::isNotBlank)
                .toMutableSet()
        removed -= event.id
        settings.putString(KEY_REMOVED_SAVED, removed.joinToString("|"))
    }

    override suspend fun removeSavedEvent(id: String) {
        val added = readAddedEvents().filterNot { it.id == id }
        writeAddedEvents(added)
        val removed =
            settings.getString(KEY_REMOVED_SAVED, "").split('|').filter(String::isNotBlank)
                .toMutableSet()
        removed += id
        settings.putString(KEY_REMOVED_SAVED, removed.joinToString("|"))
    }

    override suspend fun getMyEvents(): List<ProfileEvent> {
        return try {
            val upcoming = fetchMyEvents("UPCOMING", EventStatus.UPCOMING)
            val past = fetchMyEvents("PAST", EventStatus.COMPLETED)
            (upcoming + past).distinctBy { it.id }
        } catch (error: BackendException) {
            if (error.status != 404) throw error
            loadMyEventsFromEventCatalog()
        }
    }

    /** The current staging API has no /me/events route yet; detail responses still expose viewer registration. */
    private suspend fun loadMyEventsFromEventCatalog(): List<ProfileEvent> {
        eventRepository.refreshEvents()
        val currentUserId = eventRepository.currentUserId
        val events = eventRepository.observeEvents().first()
        return buildList {
            for (card in events) {
                val event = try {
                    eventRepository.getEvent(card.id)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    null
                } ?: continue
                if (event.organizerId != currentUserId && !event.isAttending) continue
                add(
                    ProfileEvent(
                        id = event.id,
                        title = event.title,
                        subtitle = "${event.startsAt} · ${event.address}",
                        category = event.category.title,
                        status = if (event.isPast) EventStatus.COMPLETED else EventStatus.UPCOMING,
                    ),
                )
            }
        }
    }

    private suspend fun fetchMyEvents(bucket: String, status: EventStatus): List<ProfileEvent> {
        val events = mutableListOf<ProfileEvent>()
        var cursor: String? = null
        do {
            val path = buildString {
                append("/v1/me/events?bucket=").append(bucket)
                cursor?.let { append("&cursor=").append(it.encodeQueryComponent()) }
            }
            val page = api.get(path).jsonObject
            events += page["items"]?.jsonArray.orEmpty().mapNotNull { row ->
                row.jsonObject.toProfileEvent(status)
            }
            cursor = page.string("nextCursor")
        } while (cursor != null)
        return events
    }

    private fun JsonObject.toProfileEvent(status: EventStatus): ProfileEvent? {
        val card = this["card"]?.jsonObject ?: return null
        val ref = card["ref"]?.jsonObject
        val location = card["location"]?.jsonObject
        val startsAt = card.string("startsAt").orEmpty()
        val address = location?.string("address").orEmpty()
        val subtitle = listOf(startsAt, address).filter(String::isNotBlank).joinToString(" · ")
        return ProfileEvent(
            id = ref?.string("id") ?: card.string("id") ?: return null,
            title = card.string("title").orEmpty(),
            subtitle = subtitle,
            category = card.string("categoryId")?.let(::categoryTitle) ?: "Событие",
            status = status,
        )
    }

    private fun categoryTitle(id: String): String = when (id) {
        "outdoors" -> "Прогулка"
        "food" -> "Еда и бары"
        "games" -> "Игры"
        else -> "Событие"
    }

    private fun readAddedEvents(): List<ProfileEvent> = settings.getString(KEY_ADDED_SAVED, "")
        .lineSequence().mapNotNull { line ->
            val fields = line.split('\t')
            if (fields.size < 5) null else ProfileEvent(
                id = fields[0],
                title = fields[1],
                subtitle = fields[2],
                category = fields[3],
                status = runCatching { EventStatus.valueOf(fields[4]) }.getOrNull() ?: EventStatus.UPCOMING,
                kind = fields.getOrNull(5)?.let { runCatching { ProfileSavedKind.valueOf(it) }.getOrNull() }
                    ?: ProfileSavedKind.EVENT,
                coverKey = fields.getOrNull(6)?.takeIf(String::isNotBlank),
            )
        }.toList()

    private fun writeAddedEvents(events: List<ProfileEvent>) {
        settings.putString(
            KEY_ADDED_SAVED,
            events.joinToString("\n") {
                listOf(
                    it.id,
                    it.title,
                    it.subtitle,
                    it.category,
                    it.status.name,
                    it.kind.name,
                    it.coverKey.orEmpty(),
                ).joinToString("\t")
            },
        )
    }

    private companion object {
        const val KEY_NAME = "profile_name"
        const val KEY_CITY = "profile_city"
        const val KEY_BIRTH_DATE = "profile_birth_date"
        const val KEY_ABOUT = "profile_about"
        const val KEY_PHOTO = "profile_photo_uri"
        const val KEY_PHOTO_SCALE = "profile_photo_scale"
        const val KEY_PHOTO_OFFSET_X = "profile_photo_offset_x"
        const val KEY_PHOTO_OFFSET_Y = "profile_photo_offset_y"
        const val KEY_REMOVED_SAVED = "profile_removed_saved_events"
        const val KEY_ADDED_SAVED = "profile_added_saved_events"

    }
}

private fun String.encodeQueryComponent(): String = buildString {
    this@encodeQueryComponent.encodeToByteArray().forEach { byte ->
        val value = byte.toInt() and 0xff
        if (value in 'a'.code..'z'.code || value in 'A'.code..'Z'.code ||
            value in '0'.code..'9'.code || value == '-'.code || value == '_'.code ||
            value == '.'.code || value == '~'.code
        ) append(value.toChar()) else {
            append('%')
            append("0123456789ABCDEF"[value shr 4])
            append("0123456789ABCDEF"[value and 0x0f])
        }
    }
}
