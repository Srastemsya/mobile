package fefu.prime.srastem.profile.data

import com.russhwolf.settings.Settings
import fefu.prime.srastem.profile.domain.EventStatus
import fefu.prime.srastem.profile.domain.ProfileDetails
import fefu.prime.srastem.profile.domain.ProfileEvent
import fefu.prime.srastem.profile.domain.ProfileRepository
import fefu.prime.srastem.events.domain.EventRepository
import kotlinx.coroutines.flow.first

class SettingsProfileRepository(
    private val settings: Settings,
    private val eventRepository: EventRepository,
) : ProfileRepository {
    override suspend fun getProfile() = ProfileDetails(
        name = settings.getString(KEY_NAME, settings.getString("registration_profile_name", "Ренат")),
        city = settings.getString(KEY_CITY, "Владивосток"),
        birthDate = settings.getString(KEY_BIRTH_DATE, settings.getString("registration_profile_birth_date", "24.08.2001")),
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
        val removed = settings.getString(KEY_REMOVED_SAVED, "").split('|').filter(String::isNotBlank).toSet()
        return (SAVED_EVENTS + readAddedEvents()).distinctBy { it.id }.filterNot { it.id in removed }
    }

    override suspend fun saveEvent(event: ProfileEvent) {
        val events = readAddedEvents().filterNot { it.id == event.id } + event
        settings.putString(KEY_ADDED_SAVED, events.joinToString("\n") { listOf(it.id, it.title, it.subtitle, it.category, it.status.name).joinToString("\t") })
        val removed = settings.getString(KEY_REMOVED_SAVED, "").split('|').filter(String::isNotBlank).toMutableSet()
        removed -= event.id
        settings.putString(KEY_REMOVED_SAVED, removed.joinToString("|"))
    }

    override suspend fun removeSavedEvent(id: String) {
        val added = readAddedEvents().filterNot { it.id == id }
        settings.putString(KEY_ADDED_SAVED, added.joinToString("\n") { listOf(it.id, it.title, it.subtitle, it.category, it.status.name).joinToString("\t") })
        val removed = settings.getString(KEY_REMOVED_SAVED, "").split('|').filter(String::isNotBlank).toMutableSet()
        if (SAVED_EVENTS.any { it.id == id }) removed += id
        settings.putString(KEY_REMOVED_SAVED, removed.joinToString("|"))
    }

    override suspend fun getMyEvents(): List<ProfileEvent> {
        val currentUserId = eventRepository.currentUserId
        return eventRepository.observeEvents().first()
            .filter { event ->
                event.organizerId == currentUserId || event.participants.any { it.id == currentUserId }
            }
            .map { event ->
            ProfileEvent(
                id = event.id,
                title = event.title,
                subtitle = "${event.startsAt} · ${event.address}",
                category = event.category.title,
                status = if (event.isPast) EventStatus.COMPLETED else EventStatus.UPCOMING,
            )
        }
    }

    private fun readAddedEvents(): List<ProfileEvent> = settings.getString(KEY_ADDED_SAVED, "")
        .lineSequence().mapNotNull { line ->
            val fields = line.split('\t')
            if (fields.size != 5) null else ProfileEvent(
                fields[0], fields[1], fields[2], fields[3],
                runCatching { EventStatus.valueOf(fields[4]) }.getOrNull() ?: EventStatus.UPCOMING,
            )
        }.toList()

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

        val SAVED_EVENTS = listOf(
            ProfileEvent("tokarevsky-sunset", "Закат на Токаревском", "Пт, 13 ноя · 17:00 · Токаревская кошка", "Прогулка", EventStatus.UPCOMING),
            ProfileEvent("sup-tobizina", "Сапы на Тобизина", "Сб, 14 ноя · 10:00 · Остров Русский", "Активный отдых", EventStatus.UPCOMING),
            ProfileEvent("quiz-svetlanskaya", "Квиз на Светланской", "Сб, 14 ноя · 19:00 · Светланская, 22", "Игры", EventStatus.UPCOMING),
        )
    }
}
